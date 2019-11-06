/**
 * Neodynamica System Library
 * Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.gp;

import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;
import com.neodynamica.lib.sample.Dataset;
import com.neodynamica.backendinterface.Backend;
import com.neodynamica.backendinterface.GenerationBean;
import com.neodynamica.lib.sample.io.JavaIdentifierConverter;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.engine.Problem;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.engine.AdaptiveEngine;
import io.jenetics.ext.moea.UFTournamentSelector;
import io.jenetics.ext.moea.Vec;
import io.jenetics.ext.util.Tree;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.EphemeralConst;
import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;
import io.jenetics.prog.regression.Regression;
import io.jenetics.prog.regression.Error;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Symbolic Regression Algorithm using genetic programming to find
 * the best fitted solution
 *
 * @version 1.0
 * @since 1.0
 */
public class SymbolicRegression {

    private Backend api;
    private SearchParameter searchParameter;
    private Dataset dataset;

    private ISeq<Op<Double>> operators;
    private ISeq<Op<Double>> terminals;

    private Regression<Double> regression;
    private boolean multiObjective;

    private Executor executor;

    private double bestFitness;
    private Vec<double[]> mooBestFitness;
    private ProgramGene<Double> bestGene;

    /**
     * stores data about each generation
     */
    private List<GenerationBean> generationBeans = new ArrayList<>();

    // Limits
    private int maxGenerations;
    private int steadyFitnessLimit;
    private int timeLimit;

    // Regression algorithm variables
    private int populationSize;
    private int[] includeIntegers;
    private int[] includeDecimals;
    private int includeDecimalPlaces;
    private int offspringSampleSize;
    private int survivorsSampleSize;
    private double offspringFraction;
    private double mutatorProbability;
    private double crossoverProbability;

    private RunState runState;

    private volatile Thread thread;

    private final Lock pauseLock = new ReentrantLock();
    private final Condition pauseCondition = pauseLock.newCondition();

    /**
     * True if search is terminated.
     */
    private boolean terminated;

    /**
     * True if search is paused.
     */
    private boolean paused;

    /**
     * Initialise values.
     *
     * @param searchParameter Search parameter object contain all parameters to be used for
     * algorithm
     * @param dataset Dataset containing set of inputs/output values
     */
    public SymbolicRegression(Backend api, SearchParameter searchParameter, Dataset dataset)
            throws SearchParameterException {

        this.api = api;
        this.dataset = dataset;

        this.bestFitness = Double.MAX_VALUE;
        this.mooBestFitness = Vec.of(Double.MAX_VALUE, Double.MAX_VALUE);

        // Set up symbolic regression
        this.setup(searchParameter);

        //TODO - maybe add another state called 'initialised'?
        this.runState = RunState.PAUSED;
    }

    /**
     * Set up symbolic regression instance based off parameters
     */
    public void setup(SearchParameter searchParameter) throws SearchParameterException {
        this.searchParameter = searchParameter;

        // Exception hack: Set maximum generation and population size.
        this.maxGenerations = this.searchParameter.getMaxGenerations();
        this.populationSize = this.searchParameter.getPopulationSize();

        // Get operators
        String[] ops = this.searchParameter.getOperators().split(",");
        List<Op<Double>> opsList = new LinkedList<>();

        for (int i = 0; i < ops.length; i++) {
            opsList.add(MathOp.valueOf(ops[i].toUpperCase()));
        }

        //add solution skeleton to potential ops if one was specified in searchParameters
        if (searchParameter.getSkeleton() != null && !searchParameter.getSkeleton().isEmpty()) {
            opsList.add(SolutionSkeleton.createOpFromString(searchParameter.getSkeleton()));
        }

        this.operators = ISeq.of(opsList);

        // Get terminals
        List<Op<Double>> terminalsList = new LinkedList<>();
        String[] labels = this.dataset.getInputLabels();


        //add input variables as potential terminals
        for(int i = 0; i < labels.length; i++) {
            //convert labels to be valid java identifiers to keep Jenetics 5.0 happy
            terminalsList.add(Var.of(JavaIdentifierConverter.convertToJavaValidIdentifier(labels[i]), i));
        }

        // Get integer constant range
        String[] stringInts = this.searchParameter.getIncludeIntegers().split(",");
        this.includeIntegers = new int[]{Integer.parseInt(stringInts[0]),
                Integer.parseInt(stringInts[1])};

        // Get integer constant range
        String[] stringDecimals = this.searchParameter.getIncludeDecimals().split(",");
        this.includeDecimals = new int[]{Integer.parseInt(stringDecimals[0]),
                Integer.parseInt(stringDecimals[1])};

        // Get decimal places
        this.includeDecimalPlaces = this.searchParameter.getIncludeDecimalPlaces();

        //add both integers and decimals as potential terminal options
        terminalsList.add(
                EphemeralConst.of(
                        new RandomInt(this.includeIntegers[0], this.includeIntegers[1])
                )
        );
        terminalsList.add(
                EphemeralConst.of(
                        new RandomDouble(this.includeDecimals[0], this.includeDecimals[1], this.includeDecimalPlaces)
                )
        );

        this.terminals = ISeq.of(terminalsList);

        this.offspringFraction = this.searchParameter.getOffspringFraction();
        this.mutatorProbability = this.searchParameter.getMutatorProbability();
        this.crossoverProbability = this.searchParameter.getCrossoverProbability();
        this.offspringSampleSize = this.searchParameter.getOffspringSampleSize();
        this.survivorsSampleSize = this.searchParameter.getSurvivorsSampleSize();

        this.timeLimit = this.searchParameter.getTimeLimit();
        this.steadyFitnessLimit = this.searchParameter.getSteadyFitnessLimit();

        this.updateRegression();
    }

    /**
     * Run the symbolic regression
     */
    public void run() throws SearchParameterException {
        // Run GP Engine
        runState = RunState.RUNNING;

        try {
            multiObjective = this.searchParameter.getMultiObjectiveOptimisation();
        } catch (SearchParameterException e) {
            // Do nothing, leave it as false and continue
        }

        //if a valid seed was given, run the slower, reproducible single-threaded engine
        try {
            searchParameter
                    .getSeed(); //will throw a SearchParameter exception here if no seed specified
            // Set executor as single threaded for seeding
            executor = Runnable::run;
        } catch (SearchParameterException e) {
            // No valid seed exists so use default executor for multi threaded operation
            if (e.getMessage().equals("No seed was specified")) {
                executor = ForkJoinPool.commonPool();
            } else {
                // @TODO: Better handle complete crash here
                throw e;
            }
        } finally {
            final Thread engineThread;
            engineThread = new Thread(() -> {
                // If multi objective
                if (multiObjective) {
                    final Problem<Tree<Op<Double>, ?>, ProgramGene<Double>, Vec<double[]>> problem = Problem.of(prog -> {
                        return Vec.of(
                                // The regression error
                                regression.error(prog),
                                // The complexity measure - number of nodes
                                this.getModelComplexity(prog.size())
                        );
                    }, regression.codec());

                    new AdaptiveEngine<ProgramGene<Double>, Vec<double[]>> (s -> Engine.builder(problem)
                        .populationSize(this.populationSize)
                        .offspringSelector(new TournamentSelector<>(this.offspringSampleSize)) // Jenetics default is Tournament Selector, used here to provide sample size
                        .survivorsSelector(UFTournamentSelector.ofVec())
                        .alterers(
                                new Mutator<>(this.mutatorProbability),
                                new SingleNodeCrossover<>(this.crossoverProbability)
                        )
                        .offspringFraction(this.offspringFraction)
                        .minimizing()
                        .executor(executor)
                        .mapping(EvolutionResult.toUniquePopulation(1))
                        .build()
                        .limit(this.maxGenerations))
                    .stream()
                    .limit(Limits.byExecutionTime(Duration.ofSeconds(this.timeLimit)))
                    .limit(Limits.bySteadyFitness(this.steadyFitnessLimit))
                    .limit(results -> !Thread.currentThread().isInterrupted())
                    .peek(this::addMultiObjectiveGenerationBean)
                    .forEach(r -> {
                        onEachGeneration(r.getGeneration());
                    });
                } else {
                    new AdaptiveEngine<ProgramGene<Double>, Double> (s -> Engine.builder(regression)
                        .populationSize(this.populationSize)
                        .offspringSelector(new TournamentSelector<>(this.offspringSampleSize)) // Jenetics default is Tournament Selector, used here to provide sample size
                        .survivorsSelector(new TournamentSelector<>(this.survivorsSampleSize)) // Jenetics default is Tournament Selector, used here to provide sample size
                        .alterers(
                                new Mutator<>(this.mutatorProbability),
                                new SingleNodeCrossover<>(this.crossoverProbability)
                        )
                        .offspringFraction(this.offspringFraction)
                        .minimizing()
                        .executor(executor)
                        //.mapping(EvolutionResult.toUniquePopulation(1))
                        .build()
                        .limit(this.maxGenerations))
                    .stream()
                    .limit(Limits.byExecutionTime(Duration.ofSeconds(this.timeLimit)))
                    .limit(Limits.bySteadyFitness(this.steadyFitnessLimit))
                    .limit(results -> !Thread.currentThread().isInterrupted())
                    .peek(this::addGenerationBean)
                    .forEach(r -> {
                        onEachGeneration(r.getGeneration());
                    });
                }

                // Evolution ended, let the backend know
                this.runState = RunState.ENDED;
                this.terminated = true;
                // @TODO: tell the api why it ended
                api.evolutionEnded();
            });

            engineThread.start();
            this.thread = engineThread;
        }
    }

    private void onEachGeneration(long generation) {
        if (this.paused) {
            //this.population = r.getPopulation();
        } else if (this.terminated) {
            this.stop();
            return;
        }

        if (generation >= this.maxGenerations) {
            this.runState = RunState.ENDED;
            this.terminated = true;
        }

        this.waiting();
    }

    /**
     * Generates a GenerationBean based on the supplied generation, and adds it to the
     * generationBeans list
     *
     * @param result - the EvolutionResult after a generation of SymbolicRegression running.
     */
    private void addGenerationBean(final EvolutionResult<ProgramGene<Double>, Double> result) {
        GenerationBean<Double> generationBean = new GenerationBean<>();
        generationBean.setIndex(result.getGeneration());
        generationBean.setWorstFitness(result.getWorstFitness());
        generationBean.setInputVariableLabels(dataset.getInputLabels());

        //store population from ImmutableSequence into a List, so we can sort to find median
        //we will save time by filling the list whilst also iterating the sequence to calculate mean
        ArrayList<Phenotype<ProgramGene<Double>,Double>> populationAsList = new ArrayList<>();

        //calculate average fitness
        int numOfScores = 0;
        Iterator<Phenotype<ProgramGene<Double>, Double>> iter = result.getPopulation().iterator();
        double fitnessSum = 0;
        while (iter.hasNext()) {

            Phenotype<ProgramGene<Double>,Double> solution = iter.next();
            double fitness = solution.getFitness();
            //skip over results with infinite error (e.g. those with divide by zero errors)
            if (!Double.isNaN(fitness) && !Double.isInfinite(fitness)) {
                fitnessSum += fitness;
                numOfScores++;
            }

            //while we're iterating over the population already, build up a List of the Phenotypes
            //which we can then sort and find the median
            populationAsList.add(solution);
        }
        generationBean.setAverageFitness(fitnessSum / numOfScores);

        //sort the list version and grab the median fitness
        Collections.sort(populationAsList);
        generationBean.setMedianFitness(populationAsList.get(populationAsList.size()/2).getFitness());

        // calculate standard deviation
        iter = result.getPopulation().iterator();
        double deviation = 0;
        while (iter.hasNext()) {
            Double fitness = iter.next().getFitness();
            //skip over results with infinite error (e.g. those with divide by zero errors)
            if (!Double.isNaN(fitness) && !Double.isInfinite(fitness)) {
                double dm = fitness - generationBean.getAverageFitness();
                deviation += dm * dm;
            }
        }
        generationBean.setFitnessStandardDeviation(Math.sqrt(deviation / numOfScores));

        //determine if all-time best fitness has improved this generation, updating if so
        if (result.getBestFitness() < bestFitness) {
            this.bestFitness = result.getBestFitness();
            this.bestGene = result.getBestPhenotype().getGenotype().getGene();
            generationBean.setNewBest(true);
        } else {
            generationBean.setNewBest(false);
        }

        //store the all-time best gene/fitness found
        generationBean.setBestGene(this.bestGene);
        generationBean.setBestFitness(this.bestFitness);


        //add this generation to the list
        generationBeans.add(generationBean);

        //notify api that a new generation has been added
        api.newGeneration();
    }

    /**
     * Generates a GenerationBean based on the supplied generation, and adds it to the
     * generationBeans list
     *
     * @param result - the EvolutionResult after a generation of SymbolicRegression running.
     */
    private void addMultiObjectiveGenerationBean(
            final EvolutionResult<ProgramGene<Double>, Vec<double[]>> result) {
        GenerationBean<Vec<double[]>> generationBean = new GenerationBean<>();
        generationBean.setIndex(result.getGeneration());
        generationBean.setPopulation(result.getPopulation());
        generationBean.setBestFitness(result.getBestFitness());
        generationBean.setWorstFitness(result.getWorstFitness());
        generationBean.setInputVariableLabels(dataset.getInputLabels());

        /* median is not clearly defined for multi-objective
         * for now, just compile a list of the first objective errors to sort so we have
         * something for median.
         */
        ArrayList<Double> errorScoresAsList = new ArrayList<>();

        //calculate average fitness
        int numOfScores = 0;
        Iterator<Phenotype<ProgramGene<Double>, Vec<double[]>>> iter = result.getPopulation()
                .iterator();
        double errorSum = 0;
        double complexitySum = 0;

        while (iter.hasNext()) {
            double[] fitness = iter.next().getFitness().data();
            //skip over results with infinite error (i.e. those with divide by zero errors)
            if (!Double.isNaN(fitness[0]) &&
                    !Double.isInfinite(fitness[0]) &&
                    !Double.isNaN(fitness[1]) &&
                    !Double.isInfinite(fitness[1])) {
                errorSum += fitness[0];
                complexitySum += fitness[1];
                numOfScores++;
            }

            //while we're iterating over the population already, build up a List of the objective1 errors
            //which we can then sort and find the median
            errorScoresAsList.add(fitness[0]);
        }

        //find median fitness from sorted list of first-objective fitnesses
        Collections.sort(errorScoresAsList);
        //set second objective of median fitness to -1, as we have nothing meaningful to put there
        generationBean.setMedianFitness(Vec.of(errorScoresAsList.get(errorScoresAsList.size()/2),-1));

        double errorAvg = errorSum / numOfScores;
        double complexityAvg = complexitySum / numOfScores;

        Vec<double[]> avgFitness = Vec.of(errorAvg, complexityAvg);
        generationBean.setAverageFitness(avgFitness);

        // calculate standard deviation
        iter = result.getPopulation().iterator();
        double errorDeviation = 0;
        double complexityDeviation = 0;

        while (iter.hasNext()) {
            double[] fitness = iter.next().getFitness().data();
            //skip over results with infinite error (i.e. those with divide by zero errors)
            if (!Double.isNaN(fitness[0]) &&
                    !Double.isInfinite(fitness[0]) &&
                    !Double.isNaN(fitness[1]) &&
                    !Double.isInfinite(fitness[1])) {
                double errorDm = fitness[0] - errorAvg;
                errorDeviation += errorDm * errorDm;

                double complexityDm = fitness[1] - complexityAvg;
                complexityDeviation += complexityDm * complexityDm;
            }
        }

        Vec<double[]> standardDeviation = Vec
                .of(Math.sqrt(errorDeviation / numOfScores),
                        Math.sqrt(complexityDeviation / numOfScores));
        generationBean.setFitnessStandardDeviation(standardDeviation);

        //determine if best fitness has improved this generation
        if (result.getBestFitness().dominance(mooBestFitness) > 0) {
            mooBestFitness = result.getBestFitness();
            generationBean.setNewBest(true);
        } else {
            generationBean.setNewBest(false);
        }

        generationBean.setBestGene((result.getBestPhenotype().getGenotype().getGene()));

        //add this generation to the list
        generationBeans.add(generationBean);

        //notify api that a new generation has been added
        api.newGeneration();
    }

    /**
     * Stop current evolution.
     */
    public void stop() {
        // Set run state
        this.runState = RunState.ENDED;

        this.terminated = true;

        final Thread _thread = this.thread;

        if (_thread != null) {
            _thread.interrupt();

            try {
                _thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                thread = null;
            }
        }
    }

    /**
     * Pause current evolution
     */
    public void pause() {
        this.pauseLock.lock();

        // Set run state
        this.runState = RunState.PAUSED;

        this.paused = true;
        this.pauseLock.unlock();
    }

    /**
     * Resume current evolution.
     */
    public void resume() {
        this.pauseLock.lock();

        //Set run state
        this.runState = RunState.RUNNING;

        this.paused = false;
        this.pauseCondition.signalAll();
        this.pauseLock.unlock();
    }

    /**
     * Waits for the evolution thread
     *
     * @throws InterruptedException if the calling thread has been interrupted
     */
    public void join() throws InterruptedException {
        final Thread _thread = thread;
        if (_thread != null) {
            _thread.join();
        }
    }

    /**
     * Get run state of symbolic regression
     *
     * @return Run state
     */
    public RunState getRunState() {
        return this.runState;
    }

    /**
     * Get current search parameters
     *
     * @return Search parameters object
     */
    public SearchParameter getSearchParameter() {
        return this.searchParameter;
    }

    /**
     * Get generationBeans - used by Backend
     *
     * @return List of GenerationBeans containing information about each completed generation
     */
    public List<GenerationBean> getGenerationBeans() {
        return generationBeans;
    }

    /* ================================================
     * Helper Methods
     * ================================================
     */

    /**
     * Thread waiting.
     */
    private void waiting() {
        this.pauseLock.lock();

        try {
            while (this.paused) {
                this.pauseCondition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            this.pauseLock.unlock();
        }
    }

    /**
     * Update the instances codec Triggered by setup or if codec variables change
     */
    private void updateRegression() throws SearchParameterException {

        int initialSolutionDepth = this.searchParameter.getInitialSolutionDepth();
        int maxSolutionNodes = this.searchParameter.getMaxSolutionNodes();

        ErrorFunction.setErrorType(this.searchParameter.getErrorFunction());

        this.regression = Regression.of(
                Regression.codecOf(this.operators, this.terminals, initialSolutionDepth, ch -> ch.getRoot().size() <= maxSolutionNodes),
                Error.of(ErrorFunction::calculateError),
                this.dataset.toSamples()
        );
    }

    /**
     * RandomInt
     *
     * Helper class to generate EphemeralConst values.
     * This is required as the main Symbolic Regression class is not serializable,
     * and EphemeralConst objects are included in the EvolutionResult serialization and export
     */
    private static final class RandomInt implements Supplier<Double>, Serializable {
        private final int from;
        private final int to;
        RandomInt(final int from, final int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public Double get() {
            double rand = (RandomRegistry.getRandom().nextDouble() * ((to - from) + 1)) + from;
            return (double) Math.round(rand);
        }
    }

    /**
     * RandomDouble
     *
     * Helper class to generate EphemeralConst values.
     * This is required as the main Symbolic Regression class is not serializable,
     * and EphemeralConst objects are included in the EvolutionResult serialization and export
     */
    private static final class RandomDouble implements Supplier<Double>, Serializable {
        private final double from;
        private final double to;
        private final int decimals;
        RandomDouble(final double from, final double to, final int decimals) {
            this.from = from;
            this.to = to;
            this.decimals = decimals;
        }

        @Override
        public Double get() {
            double rand = (RandomRegistry.getRandom().nextDouble() * ((to - from) + 1)) + from;
            return new BigDecimal(rand).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
        }
    }

    /**
     * Compute complexity of the program
     *
     * Ref: Eq 4.2.3 from Jenetics User Guide
     *
     * @param numNode Current number of node
     *
     * @return Complexity
     */
    private double getModelComplexity(int numNode) {
        int maxNode = 0;

        try {
            maxNode = this.searchParameter.getMaxSolutionNodes();
        } catch(SearchParameterException e) {
            e.printStackTrace();
        }
        
        int min = Math.min(numNode, maxNode);

        return (1 - Math.sqrt(1 - ((Math.pow(min, 2)) / (Math.pow(maxNode, 2)))));
    }
}

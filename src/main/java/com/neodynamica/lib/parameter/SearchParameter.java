/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter;

import com.neodynamica.lib.parameter.io.ParserUtils;
import com.neodynamica.lib.parameter.validator.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Search parameters to be used in symbolic regression algorithm.
 *
 * @version 1.0
 * @since 1.0
 */
public class SearchParameter {

    private int maxGenerations;
    private int populationSize;
    private int initialSolutionDepth;
    private int maxSolutionNodes;
    private int targetColumnIndex;
    private int includeDecimalPlaces;
    private int offspringSampleSize;
    private int survivorsSampleSize;
    private int steadyFitnessLimit;
    private int timeLimit;

    private long seed;

    private double offspringFraction;
    private double mutatorProbability;
    private double crossoverProbability;

    private String skeleton;
    private String dataFilePath;
    private String errorFunction;
    private String operators;
    private String inputColumnIndices;
    private String includeIntegers;
    private String includeDecimals;

    private Boolean multiObjectiveOptimisation;

    /**
     * Initialise default values
     */
    public SearchParameter() {
        this.maxGenerations = 0;
        this.populationSize = 0;
        this.initialSolutionDepth = 0;
        this.maxSolutionNodes = 0;
        this.targetColumnIndex = -2;
        this.skeleton = null;
        this.dataFilePath = null;
        this.errorFunction = null;
        this.operators = null;
        this.inputColumnIndices = null;
        this.seed = 0; //if left as zero, will use a random seed
        this.multiObjectiveOptimisation = null;
        this.includeIntegers = null;
        this.includeDecimalPlaces = -1;
        this.offspringFraction = -1;
        this.mutatorProbability = -1;
        this.crossoverProbability = -1;
        this.offspringSampleSize = -1;
        this.survivorsSampleSize = -1;
        this.steadyFitnessLimit = -1;
        this.timeLimit = -1;
    }

    /**
     * Copy-constructor
     */
    public SearchParameter(SearchParameter sp) throws SearchParameterException {
        overrideFrom(sp);
    }

    /**
     * Set maximum number of generation
     *
     * @param maxGenerations Maximum number of generation
     */
    public void setMaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    /**
     * Set population size
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * Set initial solution depth
     */
    public void setInitialSolutionDepth(int initialSolutionDepth) {
        this.initialSolutionDepth = initialSolutionDepth;
    }

    /**
     * Set maximum number of solution nodes
     */
    public void setMaxSolutionNodes(int maxSolutionNodes) {
        this.maxSolutionNodes = maxSolutionNodes;
    }

    /**
     * Set target column index.
     */
    public void setTargetColumnIndex(int targetColumnIndex) {
        this.targetColumnIndex = targetColumnIndex;
    }

    /**
     * Set seed for randomiser
     *
     * @param seed is the seed to be used by the randomiser
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }

    /**
     * Set flag to determine if multi objective functions will be used
     *
     * @param multiObjectiveOptimisation is determining flag
     */
    public void setMultiObjectiveOptimisation(Boolean multiObjectiveOptimisation) {
        this.multiObjectiveOptimisation = multiObjectiveOptimisation;
    }

    /**
     * Set the lower and upper bound for integers used in leaf nodes
     */
    public void setIncludeIntegers(String includeIntegers) {
        this.includeIntegers = includeIntegers;
    }

    /**
     * Set the lower and upper bound for decimals used in leaf nodes
     */
    public void setIncludeDecimals(String includeDecimals) {
        this.includeDecimals = includeDecimals;
    }

    /**
     * Set the number of decimal places used for leaf node constants
     */
    public void setIncludeDecimalPlaces(int includeDecimalPlaces) {
        this.includeDecimalPlaces = includeDecimalPlaces;
    }

    /**
     * Set skeleton
     */
    public void setSkeleton(String skeleton) {
        this.skeleton = skeleton;
    }

    /**
     * Set data file path
     */
    public void setDataFilePath(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }

    /**
     * Set error function
     */
    public void setErrorFunction(String errorFunction) {
        this.errorFunction = errorFunction;
    }

    /**
     * Set operators
     */
    public void setOperators(String operators) {
        this.operators = operators;
    }

    /**
     * Set input column indices
     */
    public void setInputColumnIndices(String inputColumnIndices) {
        this.inputColumnIndices = inputColumnIndices;
    }

    /**
     * Set the offspring fraction
     */
    public void setOffspringFraction(double offspringFraction) {
        this.offspringFraction = offspringFraction;
    }

    /**
     * Set the mutatorProbability
     */
    public void setMutatorProbability(double mutatorProbability) {
        this.mutatorProbability = mutatorProbability;
    }

    /**
     * Set the crossoverProbability
     */
    public void setCrossoverProbability(double crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    /**
     * Set the offspringSampleSize
     */
    public void setOffspringSampleSize(int offspringSampleSize) {
        this.offspringSampleSize = offspringSampleSize;
    }

    /**
     * Set the survivorsSampleSize
     */
    public void setSurvivorsSampleSize(int survivorsSampleSize) {
        this.survivorsSampleSize = survivorsSampleSize;
    }

    /**
     * Set the steadyFitnessLimit
     */
    public void setSteadyFitnessLimit(int steadyFitnessLimit) {
        this.steadyFitnessLimit = steadyFitnessLimit;
    }

    /**
     * Set the timeLimit
     */
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * Get maximum number of generations
     *
     * @return Maximum number of generations
     */
    public int getMaxGenerations() throws SearchParameterException {
        MaxGenerationsValidator.validate(Integer.toString(this.maxGenerations));
        return this.maxGenerations;
    }

    /**
     * Get population size
     *
     * @return Population size
     */
    public int getPopulationSize() throws SearchParameterException {
        PopulationSizeValidator.validate(Integer.toString(this.populationSize));
        return this.populationSize;
    }

    /**
     * Get initial solution depth.
     *
     * @return Initial solution depth.
     */
    // TODO: This doesn't actually get used, and may not need to
    public int getInitialSolutionDepth() throws SearchParameterException {
        InitialSolutionDepthValidator.validate(Integer.toString(this.initialSolutionDepth));
        return this.initialSolutionDepth;
    }

    /**
     * Get maximum number of solution nodes.
     *
     * @return Maximum number of solution nodes.
     */
    public int getMaxSolutionNodes() throws SearchParameterException {
        MaxSolutionNodesValidator.validate(Integer.toString(this.maxSolutionNodes));
        return this.maxSolutionNodes;
    }

    /**
     * Get target column index.
     *
     * @return Target column index.
     */
    public int getTargetColumnIndex() throws SearchParameterException {
        TargetColumnIndexValidator.validate(Integer.toString(this.targetColumnIndex));
        return this.targetColumnIndex;
    }

    /**
     * Get the current seed if available
     */
    public long getSeed() throws SearchParameterException {
        SeedValidator.validate(Long.toString(seed));
        return this.seed;
    }

    /**
     * Get value for multi objective flag
     */
    public Boolean getMultiObjectiveOptimisation() throws SearchParameterException {
        MultiObjectiveOptimisationValidator.validate(this.multiObjectiveOptimisation);
        return this.multiObjectiveOptimisation;
    }

    /**
     * Get skeleton
     *
     * @return Skeleton
     */
    public String getSkeleton() throws SearchParameterException {
        SkeletonValidator.validate(this.skeleton);
        return this.skeleton;
    }

    /**
     * Get data file path
     *
     * @return Data file path
     */
    public String getDataFilePath() throws SearchParameterException {
        DataFilePathValidator.validate(this.dataFilePath);
        return this.dataFilePath;
    }

    /**
     * Get error function
     *
     * @return Error function
     */
    public String getErrorFunction() throws SearchParameterException {
        ErrorFunctionValidator.validate(this.errorFunction);
        return this.errorFunction;
    }

    /**
     * Get list of operators
     *
     * @return Operators
     */
    public String getOperators() throws SearchParameterException {
        OperatorsValidator.validate(this.operators);
        return this.operators;
    }

    /**
     * Get input column indices
     *
     * @return Input column indices
     */
    public String getInputColumnIndices() throws SearchParameterException {
        InputColumnIndicesValidator.validate(this.inputColumnIndices);
        return this.inputColumnIndices;
    }

    /**
     * Get include integer values
     *
     * @return Include Integer values
     */
    public String getIncludeIntegers() throws SearchParameterException {
        IncludeIntegersValidator.validate(this.includeIntegers);
        return this.includeIntegers;
    }

    /**
     * Get include decimals values
     *
     * @return Include decimals values
     */
    public String getIncludeDecimals() throws SearchParameterException {
        IncludeDecimalsValidator.validate(this.includeDecimals);
        return this.includeDecimals;
    }

    /**
     * Get include decimals places
     *
     * @return Include decimal place value
     */
    public int getIncludeDecimalPlaces() throws SearchParameterException {
        IncludeDecimalPlacesValidator.validate(Integer.toString(this.includeDecimalPlaces));
        return this.includeDecimalPlaces;
    }

    /**
     * Get offspring fraction value
     *
     * @return Offspring fraction value
     */
    public double getOffspringFraction() throws SearchParameterException {
        OffspringFractionValidator.validate(Double.toString(this.offspringFraction));
        return this.offspringFraction;
    }

    /**
     * Get mutator probability value
     *
     * @return Mutator probability value
     */
    public double getMutatorProbability() throws SearchParameterException {
        MutatorProbabilityValidator.validate(Double.toString(this.mutatorProbability));
        return this.mutatorProbability;
    }

    /**
     * Get crossover probability value
     *
     * @return Crossover probability value
     */
    public double getCrossoverProbability() throws SearchParameterException {
        CrossoverProbabilityValidator.validate(Double.toString(this.crossoverProbability));
        return this.crossoverProbability;
    }

    /**
     * Get offspringSampleSize value
     *
     * @return offspringSampleSize value
     */
    public int getOffspringSampleSize() throws SearchParameterException {
        OffspringSampleSizeValidator.validate(Integer.toString(this.offspringSampleSize));
        return this.offspringSampleSize;
    }

    /**
     * Get survivorsSampleSize value
     *
     * @return survivorsSampleSize value
     */
    public int getSurvivorsSampleSize() throws SearchParameterException {
        SurvivorsSampleSizeValidator.validate(Integer.toString(this.survivorsSampleSize));
        return this.survivorsSampleSize;
    }

    /**
     * Get steadyFitnessLimit value
     *
     * @return steadyFitnessLimit value
     */
    public int getSteadyFitnessLimit() throws SearchParameterException {
        SteadyFitnessLimitValidator.validate(Integer.toString(this.steadyFitnessLimit));
        return this.steadyFitnessLimit;
    }

    /**
     * Get timeLimit value
     *
     * @return timeLimit value
     */
    public int getTimeLimit() throws SearchParameterException {
        TimeLimitValidator.validate(Integer.toString(this.timeLimit));
        return this.timeLimit;
    }

    public void overrideFrom(SearchParameter searchParameter) {
        for (Field p : SearchParameter.class.getDeclaredFields()) {
            try {
                //Get the method with the name identical to the property name, with "get" prepended
                Method getter = searchParameter.getClass()
                        .getMethod(ParserUtils.getterFromPropertyName(p.getName()));
                //Get the method with the name identical to the property name, with 'set' prepended and 1
                //argument of the appropriate type
                Method setter = this
                        .getClass()
                        .getMethod(ParserUtils.setterFromPropertyName(p.getName()), p.getType());

                setter.invoke(this,
                        getter.invoke(searchParameter));
            } catch (Exception ignored) {
            }
        }
    }
}

package com.neodynamica.backendinterface;

import com.neodynamica.lib.gp.RunState;
import com.neodynamica.lib.gp.SymbolicRegression;
import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;
import com.neodynamica.lib.parameter.io.SearchParameterParser;
import com.neodynamica.lib.parameter.validator.*;
import com.neodynamica.lib.sample.Dataset;
import com.neodynamica.lib.sample.io.CSVParser;
import io.jenetics.prngine.LCG64ShiftRandom;
import io.jenetics.util.RandomRegistry;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Backend implements BackendInterface {

    /**
     * The symbolic regression backend
     */
    private SymbolicRegression symbolicRegression;

    /**
     * Observer pattern (see: https://www.baeldung.com/java-observer-pattern)
     */
    private PropertyChangeSupport support;


    private String configFilePath;
    private SearchParameter searchParameters;
    private Dataset dataset;

    /**
     * index in 'generations' of the next generation which hasn't been read by frontend used in
     * getNextGeneration()
     */
    private int nextUnreadGeneration = 0;

    //Timekeeping
    /**
     * Time that the symbolic regression was most recently started or resumed
     */
    private long lastStartOrResumeTime;
    /**
     * duration symbolic regression has been run for, updated when pause() or stop() called
     */
    private long durationAtLastPauseOrStop = 0;

    public Backend() throws SearchParameterException, IOException {
        //set searchParameters initially to default
        searchParameters = (new SearchParameterParser(null)).parse().getSearchParameterObject();
        support = new PropertyChangeSupport(this);
    }

    // run state methods
    public void start() throws SearchParameterException, IOException {
        updateDataSet();

        //seed Jenetics RNG for this run if a custom seed was specified i.e. not 0
        //throws SearchParameterException on getSeed() if seed is 0
        try {
            RandomRegistry.setRandom(new LCG64ShiftRandom.ThreadSafe(searchParameters.getSeed()));
        } catch (SearchParameterException e) {
            //if no seed specified, use ThreadLocal version
            //which is faster for multi-threaded environments but not reproducible
            RandomRegistry.setRandom(new LCG64ShiftRandom.ThreadLocal());
        }

        symbolicRegression = new SymbolicRegression(this, searchParameters, dataset);
        symbolicRegression.run();

        //reset duration, record start time
        durationAtLastPauseOrStop = 0;
        lastStartOrResumeTime = System.currentTimeMillis();
    }

    public void pause() throws InvalidRunStateException {
        if (RunState.ENDED.equals(getRunState())) {
            throw new InvalidRunStateException("Cannot pause - symbolic regression has ended.");
        }
        if (symbolicRegression == null) {
            throw new InvalidRunStateException(
                    "Cannot pause - symbolic regression has not been started");
        }

        //update running duration with time since start/most recent resume
        durationAtLastPauseOrStop = calculateCurrentDuration();

        symbolicRegression.pause();
    }

    public void resume() throws SearchParameterException, IOException, InvalidRunStateException {
        if (RunState.ENDED.equals(getRunState())) {
            throw new InvalidRunStateException("Cannot resume - symbolic regression has ended.");
        }
        if (symbolicRegression == null) {
            throw new InvalidRunStateException(
                    "Cannot resume - symbolic regression has not been started");
        }

        //record resume time if we actually were paused before
        if(RunState.PAUSED.equals(getRunState())){
            lastStartOrResumeTime = System.currentTimeMillis();
        }

        //update parameters that might have been changed while paused
        updateDataSet();
        symbolicRegression.setup(new SearchParameter(searchParameters));
        symbolicRegression.resume();
    }

    public void stop() throws InvalidRunStateException {
        if (symbolicRegression == null) {
            throw new InvalidRunStateException(
                    "Cannot stop - symbolic regression has not been started");
        }
        symbolicRegression.stop();

        //update running duration with time since start/most recent resume
        durationAtLastPauseOrStop = calculateCurrentDuration();
    }

    public RunState getRunState() {
        if (symbolicRegression == null) {
            return RunState.PAUSED;
        }

        return symbolicRegression.getRunState();
    }

    // getters for generations
    public GenerationBean getGeneration(int n) {
        return symbolicRegression.getGenerationBeans().get(n);
    }

    /**
     * Get the next unread generationBean if one is available. Returns null if there are no new
     * generationBeans since the last one was retrieved this way.
     */
    public GenerationBean getNextGeneration() {
        //check if there's a new generation to get
        if (symbolicRegression.getGenerationBeans().size() <= nextUnreadGeneration) {
            return null;
        } else {
            int temp = nextUnreadGeneration;
            //increment the counter so the next element is returned next time
            nextUnreadGeneration++;
            return symbolicRegression.getGenerationBeans().get(temp);
        }
    }

    public GenerationBean getLatestGeneration() {
        if (symbolicRegression.getGenerationBeans().size() == 0) {
            return null;
        }

        return symbolicRegression.getGenerationBeans()
                .get(symbolicRegression.getGenerationBeans().size() - 1);
    }

    // observer pattern methods
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public void newGeneration() {
        support.firePropertyChange("generation", "oldGeneration", "newGeneration");
    }

    public void evolutionEnded() {
        //update running duration with time since start/most recent resume
        durationAtLastPauseOrStop = calculateCurrentDuration();

        support.firePropertyChange("evolutionEnded", "running", "ended");
    }

    // Parameter Getters
    public String getConfigFilePath() {
        return configFilePath;
    }

    public String getDataFilePath() throws SearchParameterException {
        return searchParameters.getDataFilePath();
    }

    public int getMaxGenerations() throws SearchParameterException {
        return searchParameters.getMaxGenerations();
    }

    public int getPopulationSize() throws SearchParameterException {
        return searchParameters.getPopulationSize();
    }

    public String getSkeleton() throws SearchParameterException {
        return searchParameters.getSkeleton();
    }

    public String getOperators() throws SearchParameterException {
        return searchParameters.getOperators();
    }

    public String getErrorFunction() throws SearchParameterException {
        return searchParameters.getErrorFunction();
    }

    public int getTargetColumnIndex() throws SearchParameterException {
        return searchParameters.getTargetColumnIndex();
    }

    public String[] getDatasetColumnLabels() throws SearchParameterException {
        if (dataset == null) {
            throw new SearchParameterException(
                    "Can't get dataset column labels, dataset hasn't been set.");
        }
        return dataset.getLabels();
    }

    public String[] getInputColumnLabels() throws SearchParameterException {
        if (dataset == null) {
            throw new SearchParameterException(
                    "Can't get dataset input column labels, dataset hasn't been set.");
        }
        return dataset.getInputLabels();
    }

    public String getTargetColumnLabel() throws SearchParameterException {
        if (dataset == null) {
            throw new SearchParameterException(
                    "Can't get dataset target column label, dataset hasn't been set.");
        }
        return dataset.getOutputLabel();
    }

    public Double[][] getDatasetValues() throws SearchParameterException {
        if (dataset == null) {
            throw new SearchParameterException(
                    "Can't get dataset values, dataset hasn't been set.");
        }
        return dataset.getValues();
    }

    public String getInputColumnIndices() throws SearchParameterException {
        return searchParameters.getInputColumnIndices();
    }

    public String getBestSolutionParenthesisString() {
        return getLatestGeneration().getBestSolutionParenthesesString();
    }

    public String getBestSolutionTreeString() {
        return getLatestGeneration().getBestSolutionTree();
    }

    public int getMaxSolutionNodes() throws SearchParameterException {
        return searchParameters.getMaxSolutionNodes();
    }

    public int getInitialSolutionDepth() throws SearchParameterException {
        return searchParameters.getInitialSolutionDepth();
    }

    public long getSeed() throws SearchParameterException {
        return searchParameters.getSeed();
    }

    public Boolean getMultiObjectiveOptimisation() throws SearchParameterException {
        return searchParameters.getMultiObjectiveOptimisation();
    }

    public String getIncludeIntegers() throws SearchParameterException {
        return searchParameters.getIncludeIntegers();
    }

    public String getIncludeDecimals() throws SearchParameterException {
        return searchParameters.getIncludeDecimals();
    }

    public int getIncludeDecimalPlaces() throws SearchParameterException {
        return searchParameters.getIncludeDecimalPlaces();
    }

    public double getOffspringFraction() throws SearchParameterException {
        return searchParameters.getOffspringFraction();
    }

    public double getMutatorProbability() throws SearchParameterException {
        return searchParameters.getMutatorProbability();
    }

    public double getCrossoverProbability() throws SearchParameterException {
        return searchParameters.getCrossoverProbability();
    }

    public int getOffspringSampleSize() throws SearchParameterException {
        return searchParameters.getOffspringSampleSize();
    }

    public int getSurvivorsSampleSize() throws SearchParameterException {
        return searchParameters.getSurvivorsSampleSize();
    }

    public int getSteadyFitnessLimit() throws SearchParameterException {
        return searchParameters.getSteadyFitnessLimit();
    }

    public int getTimeLimit() throws SearchParameterException {
        return searchParameters.getTimeLimit();
    }

    // Parameter Setters
    public void setConfigFilePath(String configFilePath)
            throws SearchParameterException, IOException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        //override parameters with any set in config file
        searchParameters
                .overrideFrom(new SearchParameterParser(configFilePath).parse()
                        .getSearchParameterObject());
        this.configFilePath = configFilePath;

        updateDataSet();//update the dataset, in case affecting values were set/changed
    }

    public void setDataFilePath(String dataFilePath)
            throws SearchParameterException, IOException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        DataFilePathValidator.validate(dataFilePath);//validate before setting
        searchParameters.setDataFilePath(dataFilePath);

        updateDataSet();//update dataset accordingly
    }

    public void setMaxGenerations(int maxGenerations)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        MaxGenerationsValidator.validate(Integer.toString(maxGenerations));//validate before setting
        searchParameters.setMaxGenerations(maxGenerations);
    }

    public void setPopulationSize(int populationSize)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        PopulationSizeValidator.validate(Integer.toString(populationSize));//validate before setting
        searchParameters.setPopulationSize(populationSize);
    }

    public void setSkeleton(String skeleton)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        SkeletonValidator.validate(skeleton);//validate before setting
        searchParameters.setSkeleton(skeleton);
    }

    public void setOperators(String operators)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        OperatorsValidator.validate(operators);//validate before setting
        searchParameters.setOperators(operators);
    }

    public void setErrorFunction(String errorFunction)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        ErrorFunctionValidator.validate(errorFunction);//validate before setting
        searchParameters.setErrorFunction(errorFunction);
    }

    public void setMaxSolutionNodes(int maxSolutionNodes)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        MaxSolutionNodesValidator
                .validate(Integer.toString(maxSolutionNodes));//validate before setting
        searchParameters.setMaxSolutionNodes(maxSolutionNodes);
    }

    public void setTargetColumnIndex(int targetColumnIndex)
            throws SearchParameterException, InvalidRunStateException, IOException {
        checkRunStateAllowsParameterChanges();
        TargetColumnIndexValidator
                .validate(Integer.toString(targetColumnIndex));//validate before setting
        searchParameters.setTargetColumnIndex(targetColumnIndex);
    }

    public void setInputColumnIndices(String indices)
            throws SearchParameterException, InvalidRunStateException, IOException {
        checkRunStateAllowsParameterChanges();
        InputColumnIndicesValidator.validate(indices);//validate before setting
        searchParameters.setInputColumnIndices(indices);
    }


    public void setInitialSolutionDepth(int depth)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        InitialSolutionDepthValidator.validate(Integer.toString(depth));//validate before setting
        searchParameters.setInitialSolutionDepth(depth);
    }

    public void setMultiObjectiveOptimisation(Boolean multiObjectiveOptimisation)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        MultiObjectiveOptimisationValidator.validate(multiObjectiveOptimisation);
        searchParameters.setMultiObjectiveOptimisation(multiObjectiveOptimisation);
    }

    public void setIncludeIntegers(String includeIntegers)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        IncludeIntegersValidator.validate(includeIntegers);
        searchParameters.setIncludeIntegers(includeIntegers);
    }

    public void setIncludeDecimals(String includeDecimals)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        IncludeDecimalsValidator.validate(includeDecimals);
        searchParameters.setIncludeDecimals(includeDecimals);
    }

    public void setIncludeDecimalPlaces(int includeDecimalPlaces)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        IncludeDecimalPlacesValidator.validate(Integer.toString(includeDecimalPlaces));
        searchParameters.setIncludeDecimalPlaces(includeDecimalPlaces);
    }

    public void setOffspringFraction(double offspringFraction)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        OffspringFractionValidator.validate(Double.toString(offspringFraction));
        searchParameters.setOffspringFraction(offspringFraction);
    }

    public void setMutatorProbability(double mutatorProbability)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        MutatorProbabilityValidator.validate(Double.toString(mutatorProbability));
        searchParameters.setMutatorProbability(mutatorProbability);
    }

    public void setCrossoverProbability(double crossoverProbability)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        CrossoverProbabilityValidator.validate(Double.toString(crossoverProbability));
        searchParameters.setCrossoverProbability(crossoverProbability);
    }

    public void setOffspringSampleSize(int offspringSampleSize)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        OffspringSampleSizeValidator.validate(Integer.toString(offspringSampleSize));
        searchParameters.setOffspringSampleSize(offspringSampleSize);
    }

    public void setSurvivorsSampleSize(int survivorsSampleSize)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        SurvivorsSampleSizeValidator.validate(Integer.toString(survivorsSampleSize));
        searchParameters.setSurvivorsSampleSize(survivorsSampleSize);
    }

    public void setSteadyFitnessLimit(int steadyFitnessLimit)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        SteadyFitnessLimitValidator.validate(Integer.toString(steadyFitnessLimit));
        searchParameters.setSteadyFitnessLimit(steadyFitnessLimit);
    }

    public void setTimeLimit(int timeLimit)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        TimeLimitValidator.validate(Integer.toString(timeLimit));
        searchParameters.setTimeLimit(timeLimit);
    }

    public void setSeed(long seed)
            throws SearchParameterException, InvalidRunStateException {
        checkRunStateAllowsParameterChanges();
        SeedValidator.validate(Long.toString(seed));
        searchParameters.setSeed(seed);
    }

    /* DRYing methods */
    public boolean checkRunStateAllowsParameterChanges() throws InvalidRunStateException {
        if (!RunState.PAUSED.equals(getRunState())) {
            throw new InvalidRunStateException(
                    "Cannot modify search parameters unless symbolic regression is first paused - currrent state: "
                            + getRunState().toString());
        }

        return true;
    }

    /**
     * Writes a new csv file to the location specified, with a row for each row of the dataset, containing:
     * <p>
     *  - the value of each of the input variables included in the search (corresponding to "inputColumnIndices" parameter)
     * <p>
     *  - the expected target value (from the original dataset csv, at column "targetColumnIndex")
     * <p>
     *  - the predicted target value, using the current best solution.
     * @param newFilePath - the path/name for the new csv file to be written to. Specifying ".csv" is optional,
     * it will be automatically appended if not.
     * @throws IOException - thrown if there are issues writing the file
     * @throws InvalidRunStateException - thrown if this method is called before the symbolic regression has
     * actually run and generated any solutions to predict with.
     */
    public void generateBestSolutionPredictionsFile(String newFilePath)
            throws IOException, InvalidRunStateException {
        //use the most recent generation's best solution for predictions
        writePredictionFile(newFilePath, this.getLatestGeneration());
    }

    /**
     * Writes a new csv file to the location specified, with a row for each row of the dataset,
     * containing: - the value of each of the input variables included in the search (corresponding
     * to "inputColumnIndices" parameter) - the expected target value (from the original dataset
     * csv, at column "targetColumnIndex") - the predicted target value, using the best solution of
     * the generationBean provided.
     *
     * @param newFileName - the path/name for the new csv file to be written to. Specifying ".csv"
     * is optional, it will be automatically appended if not.
     * @param generationIndex - the generation number whose best solution should be used for the
     * predictions
     * @throws IOException - thrown if there are issues writing the file
     * @throws InvalidRunStateException - thrown if this method is called before the symbolic
     * regression has actually run and generated any solutions/datasets to predict with.
     */
    public void generateBestSolutionPredictionsFile(String newFileName, int generationIndex)
            throws IOException, InvalidRunStateException {
        //use the specified generation's best solution for predictions
        writePredictionFile(newFileName, getGeneration(generationIndex));
    }

    /**
     * Generalised method which does any work shared between methods called
     * "generateBestSolutionPredictionsFile", which have different parameters, using a parmeterised
     * generationBean. Cuts down duplicate code. See @generateBestSolutionPredictionsFile javadocs
     * for details
     */
    private void writePredictionFile(String newFileName, GenerationBean generationBean)
            throws IOException, InvalidRunStateException {
        //check that the backend has actually been running so there's a dataset and solution to make predictions with
        if (dataset == null || symbolicRegression == null || getLatestGeneration() == null) {
            throw new InvalidRunStateException(
                    "Can't generate predictions before search has started running");
        }

        //append .csv to end of filename if user didn't explicitly write it
        if (!newFileName.matches(".*\\.csv")) {
            newFileName += ".csv";
        }

        //open writer to file
        try (FileWriter fileWriter = new FileWriter(newFileName);
                PrintWriter printWriter = new PrintWriter(fileWriter)) {
            StringBuilder headerRow = new StringBuilder();

            //input variable labels
            for (String label : dataset.getInputLabels()) {
                headerRow.append(label + ",");
            }

            //output column header
            headerRow.append("ACTUAL " + dataset.getOutputLabel() + ",");
            //prediction column header
            headerRow.append("PREDICTED " + dataset.getOutputLabel() + " = " + generationBean
                    .getBestSolutionParenthesesString() + System.lineSeparator());

            //print header row
            printWriter.write(headerRow.toString());

            /* Print sample rows with predictions */
            //loop through each row of the dataset
            for (int r = 0; r < dataset.size(); r++) {
                StringBuilder row = new StringBuilder();
                //collect input variables
                for (int i = 0; i < dataset.getInputValues()[r].length; i++) {
                    row.append(dataset.getInputValues()[r][i] + ",");
                }

                //target
                row.append(dataset.getOutputValues()[r] + ",");

                //prediction
                row.append(
                        generationBean.predictWithBestSolution(dataset.getInputValues()[r]) + System
                                .lineSeparator());

                //write completed row
                printWriter.write(row.toString());
            }
        }
    }

    private void updateDataSet() throws SearchParameterException, IOException {
        CSVParser csvParser = new CSVParser(",", searchParameters.getDataFilePath());
        csvParser.setOutputIndex(searchParameters.getTargetColumnIndex());
        csvParser.setInputIndices(searchParameters.getInputColumnIndices());

        dataset = csvParser.parse().getDatasetObject();
    }

    /**
     * Returns the duration in milliseconds that the SymbolicRegression has been running for.
     * This "stopwatch" starts when backend.start() is called,
     * is paused by backend.pause() and backend.end(),
     * and resumed by backend.resume().
     * Updates in real time.
     * @return - the total duration in milliseconds that symbolic regression has run for since started.
     */
    public long getCurrentDuration(){
        //if regression is still running, combine duration up until last pause/stop (durationAtLastPauseOrStop)
        // with the duration since last paused with
        if(RunState.RUNNING.equals(getRunState())){
            return calculateCurrentDuration();
        }
        //if it's not running, simply return the duration as calculated when it was paused/stopped
        return durationAtLastPauseOrStop;
    }

    /**
     * Calculates the current duration, by adding the time since regression was most
     * recently resumed or started and adding it to the duration calculated at the previous pause/stop.
     * Note: if you want to update durationAtLastPauseOrStop, this will need to be done explicitly,
     * i.e. durationAtLastPauseOrStop = calculateCurrentDuration()
     * @return the current total duration the symbolic regression has been running for in milliseconds
     */
    private long calculateCurrentDuration(){
        return durationAtLastPauseOrStop + (System.currentTimeMillis() - lastStartOrResumeTime);
    }
}

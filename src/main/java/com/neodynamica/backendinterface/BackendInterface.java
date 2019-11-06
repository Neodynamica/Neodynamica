package com.neodynamica.backendinterface;

import com.neodynamica.lib.gp.RunState;
import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;
import com.neodynamica.lib.sample.Dataset;
import com.neodynamica.backendinterface.GenerationBean;

import java.beans.PropertyChangeListener;
import java.io.IOException;

public interface BackendInterface {

    // parameter getters
    String getConfigFilePath() throws SearchParameterException;

    String getDataFilePath() throws SearchParameterException;

    int getMaxGenerations() throws SearchParameterException;

    int getPopulationSize() throws SearchParameterException;

    String getSkeleton() throws SearchParameterException;

    String getOperators() throws SearchParameterException;

    String getErrorFunction() throws SearchParameterException;

    int getTargetColumnIndex() throws SearchParameterException;

    int getInitialSolutionDepth() throws SearchParameterException;

    // dataset getters
    Double[][] getDatasetValues() throws SearchParameterException;

    String[] getDatasetColumnLabels() throws SearchParameterException;

    String[] getInputColumnLabels() throws SearchParameterException;

    String getTargetColumnLabel() throws SearchParameterException;

    // result getters
    String getBestSolutionParenthesisString();

    String getBestSolutionTreeString();

    // setters
    void setConfigFilePath(String configFilePath)
            throws SearchParameterException, IOException, InvalidRunStateException;

    void setDataFilePath(String dataFilePath)
            throws SearchParameterException, IOException, InvalidRunStateException;

    void setMaxGenerations(int maxGenerations)
            throws SearchParameterException, InvalidRunStateException;

    void setPopulationSize(int populationSize)
            throws SearchParameterException, InvalidRunStateException;

    void setSkeleton(String skeleton) throws SearchParameterException, InvalidRunStateException;

    void setOperators(String operators) throws SearchParameterException, InvalidRunStateException;

    void setErrorFunction(String errorFunction)
            throws SearchParameterException, InvalidRunStateException;

    void setTargetColumnIndex(int i)
            throws SearchParameterException, InvalidRunStateException, IOException;

    void setInitialSolutionDepth(int depth)
            throws SearchParameterException, InvalidRunStateException;


    // state commands
    void start() throws SearchParameterException, IOException;

    void pause() throws InvalidRunStateException;

    void resume() throws SearchParameterException, InvalidRunStateException, IOException;

    void stop() throws InvalidRunStateException;

    RunState getRunState();

    // event messages from backend
    // queue of regression notifications
    GenerationBean getGeneration(int n);

    GenerationBean getLatestGeneration();

    GenerationBean getNextGeneration();

    // in implementation: private PropertyChangeSupport support;
    void addPropertyChangeListener(PropertyChangeListener pcl);

    void removePropertyChangeListener(PropertyChangeListener pcl);

    void newGeneration(); // backend calls this when stream of events is updated TODO - not sure this fits our design any more

    void evolutionEnded(); // backend calls this when evolution stream has ended.
    // implementation should have a `private PropertyChangeSupport support;` which is used in setStream with
    // `support.firePropertyChange("stream", this.SymbolicRegressionEventStream, value);`
    // The front-end object using the interface then implements PropertyChangeListener`.
    // See part 4 of this article: https://www.baeldung.com/java-observer-pattern

}

/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.sample;

import io.jenetics.prog.regression.Sample;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Dataset object that store all values and column labels from dataset.
 *
 * @version 1.0
 * @since 1.0
 */
public class Dataset {

    /**
     * Actual labels from CSV dataset.
     */
    private final String[] labels;

    /**
     * Symbols mapped to actual labels. This is usually short and used in mathematical formula.
     */
    private String[] symbols;

    /**
     * Values from CSV dataset. Each value is associate with the index of the column (i.e. symbols)
     */
    private final Double[][] values;

    /**
     * Initialise values
     *
     * @param labels Labels of dataset
     * @param symbols Symbols mapped to labels
     * @param values Values of each labels
     */
    public Dataset(String[] labels, String[] symbols, Double[][] values) {
        this.labels = labels;
        this.symbols = symbols;
        this.values = values;
    }

    /**
     * Return sample set as an Iterable<Sample<Double>> for use in Regression
     */
    public List<Sample<Double>> toSamples() {
        Sample<Double>[] samples = new Sample[this.values.length];

        for (int i = 0; i < this.values.length; i++) {
            samples[i] = Sample.of(this.values[i]);
        }
        return Arrays.asList(samples);
    }

    /**
     * Get labels of the CSV Dataset.
     *
     * @return Labels of CSV Dataset.
     */
    public String[] getLabels() {
        return this.labels;
    }

    /**
     * Get an actual label from symbol.
     *
     * @return Actual label associate with symbol. Null if not exists.
     */
    public String getLabelFromSymbol(String symbol) {

        // Get symbol index
        int symbolIndex = -1;

        for (int i = 0; i < this.symbols.length; i++) {
            if (this.symbols[i].equals(symbol)) {
                symbolIndex = i;
                break;
            }
        }

        if (symbolIndex == -1) {
            return null;
        }

        return this.labels[symbolIndex];
    }

    /**
     * Get an actual label from symbol with index value.
     *
     * @param symbolIndex Index of symbol value.
     * @return Actual label associate with symbol.
     */
    public String getLabelFromSymbol(int symbolIndex) {
        return this.labels[symbolIndex];
    }

    /**
     * Get an actual label with index.
     *
     * @param index Index of an actual labels.
     * @return Actual label of dataset.
     */
    public String getLabel(int index) {
        return this.labels[index];
    }

    /**
     * Get input labels of all variables.
     *
     * @return Input labels of all variables
     */
    public String[] getInputLabels() {

        //LinkedList<String> inputLabels = new LinkedList<>();

        String[] inputLabels = new String[this.labels.length - 1];

        for (int i = 0; i < this.labels.length - 1; i++) {
            inputLabels[i] = this.labels[i];
        }

        return inputLabels;
    }

    /**
     * Get output label of dataset.
     *
     * @return Output label of dataset.
     */
    public String getOutputLabel() {
        return this.labels[this.labels.length - 1];
    }

    /**
     * Get symbols representation of labels.
     *
     * @return Symbols
     */
    public String[] getSymbols() {
        return this.symbols;
    }

    /**
     * Get symbol by index.
     *
     * @param index Index of symbol
     * @return Symbol
     */
    public String getSymbol(int index) {
        return this.symbols[index];
    }

    /**
     * Get all input symbols of all variables.
     *
     * @return Input symbols
     */
    public String[] getInputSymbols() {

        String[] inputSymbols = new String[this.symbols.length - 1];

        for (int i = 0; i < this.symbols.length - 1; i++) {
            inputSymbols[i] = this.symbols[i];
        }

        return inputSymbols;
    }

    /**
     * Get output symbol
     *
     * @return Output symbol
     */
    public String getOutputSymbol() {
        return this.symbols[this.symbols.length - 1];
    }

    /**
     * Get all values of dataset.
     *
     * @return Values of dataset.
     */
    public Double[][] getValues() {
        return this.values;
    }

    /**
     * Get input values of dataset.
     *
     * @return Input values of dataset
     */
    public Double[][] getInputValues() {

        Double[][] inputValues = new Double[this.values.length][this.labels.length - 1];

        for (int i = 0; i < this.values.length; i++) {
            for (int j = 0; j < this.labels.length - 1; j++) {
                inputValues[i][j] = this.values[i][j];
            }
        }

        return inputValues;
    }

    /**
     * Get output values of dataset.
     *
     * @return List of output values.
     */
    public Double[] getOutputValues() {

        Double[] outputValues = new Double[this.values.length];

        for (int i = 0; i < this.values.length; i++) {
            outputValues[i] = this.values[i][this.labels.length - 1];
        }

        return outputValues;
    }

    /**
     * Get number of samples.
     *
     * @return Number of samples.
     */
    public int size() {
        return this.values.length;
    }

    /**
     * Check if symbol exists
     *
     * @return True if it exists, false otherwise.
     */
    public boolean symbolExists(String symbol) {

        for (int i = 0; i < this.symbols.length; i++) {
            if (this.symbols.equals(symbol)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Map label to symbol with label value
     *
     * @param label Actual label of dataset
     * @param symbol Symbol associate with actual label
     * @return True if it's successfully set, false otherwise.
     */
    public boolean setSymbol(String label, String symbol) {

        // Check if label exists
        for (int i = 0; i < this.labels.length; i++) {
            if (this.labels[i].equals(label)) {
                this.symbols[i] = symbol;

                return true;
            }
        }

        return false;
    }

    /**
     * Map label to symbol with label index
     *
     * @param labelIndex Index of actual label of dataset
     * @param symbol Symbol associate with actual label
     * @return True if it's successfully set, false otherwise.
     */
    public boolean setSymbol(int labelIndex, String symbol) {

        // Check if label exists
        if (labelIndex < 0 || labelIndex >= this.labels.length) {
            return false;
        }

        this.symbols[labelIndex] = symbol;

        return true;
    }

}
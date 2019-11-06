/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.sample.io;

import com.neodynamica.lib.sample.Dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Parser to parse CSV file for samples to be used in Symbolic Regression Algorithm
 *
 * @version 1.0
 * @since 1.0
 */
public class CSVParser {

    /**
     * Actual labels from CSV dataset.
     */
    private LinkedList<String> labels;

    /**
     * Symbols mapped to actual labels. This is usually short and used in mathematical formula.
     */
    private LinkedList<String> symbols;

    /**
     * Values from CSV dataset. Each value is associate with the index of the column (i.e. symbols)
     */
    private LinkedList<Map<Integer, Double>> values;

    /**
     * Delimiter of CSV dataset
     */
    private String delimiter;

    /**
     * CSV dataset file
     */
    private String csvFile;

    /**
     * Number of row in the dataset
     */
    private int row;

    /**
     * Index of output column
     */
    private int outputIndex;

    /**
     * Indices of input columns
     */
    private List<Integer> inputIndices;

    /**
     * Initialise default values
     */
    public CSVParser() {
        this.labels = new LinkedList<>();
        this.symbols = new LinkedList<>();
        this.values = new LinkedList<>();
        this.delimiter = "";
        this.csvFile = "";
        this.row = 0;
        this.outputIndex = -1;
        this.inputIndices = new LinkedList<>();

    }

    /**
     * Initialise delimiter and CSV file
     *
     * @param delimiter Delimiter of CSV Dataset.
     * @param csvFile CSV Dataset File
     */
    public CSVParser(String delimiter, String csvFile) {
        this.labels = new LinkedList<>();
        this.symbols = new LinkedList<>();
        this.values = new LinkedList<>();
        this.delimiter = delimiter;
        this.csvFile = csvFile;
        this.row = 0;
        this.outputIndex = -1;
        this.inputIndices = new LinkedList<>();
    }

    /**
     * Parser CSV Dataset file
     *
     * @return This object.
     */
    public CSVParser parse() throws IOException {

        // Default symbols to be mapped to actual label.
        String[] defaultSymbols = {
                "a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o",
                "p", "q", "r", "s", "t",
                "u", "v", "w", "x", "y",
                "z"
        };

        // Read CSV Dataset
        FileReader fileReader = new FileReader(this.csvFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line = "";

        while ((line = bufferedReader.readLine()) != null) {

            // To store the values of the dataset
            Map<Integer, Double> value = new HashMap<>();

            // Make sure there is no empty string
            if (line.length() > 0) {

                // Get each column
                String[] cols = line.split(this.delimiter);

                for(int i = 0; i < cols.length; i++) {

                    if (cols[i].length() > 0) {
                        // First row is for labels
                        if (this.row == 0) {
                            // Remove a zero-width space
                            this.labels.add(cols[i].replaceAll("^\uFEFF", ""));
                            this.symbols.add(defaultSymbols[i]);
                        } else {
                            value.put(i, Double.parseDouble(cols[i]));
                        }
                    }

                }

                if (this.row > 0) {
                    if (!value.isEmpty()) {
                        this.values.add(value);
                    }
                }

                // Count number of row
                this.row++;
            }

        }

        bufferedReader.close();
        fileReader.close();

        return this;
    }

    /**
     * Set output index.
     */
    public void setOutputIndex(int outputIndex) {
        this.outputIndex = outputIndex;
    }

    /**
     * Set input indices
     */
    public void setInputIndices(String inputIndices) {
        //if the inputIndices parameter was left as default "ALL", then leave input columns as
        //every non-output column by default/
        //otherwise we set inputs specifically to the columns given
        if (!inputIndices.equalsIgnoreCase("ALL")) {

            for (String index : inputIndices.split(",")) {
                this.inputIndices.add(Integer.parseInt(index));
            }
        }
    }

    /**
     * Get row count
     *
     * @return Number or row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Get dataset object
     *
     * @return Dataset object
     */
    public Dataset getDatasetObject() {

        // Make sure output index is in the boundary
        if (this.outputIndex <= -1 || this.outputIndex >= this.labels.size()) {
            this.outputIndex = this.labels.size() - 1;
        }

        // Check if input indices are specified.
        // Add default inputs if not specified.
        // Default inputs are all columns except output column.
        if (this.inputIndices.isEmpty()) {
            for (int i = 0; i < this.labels.size(); i++) {

                // Exclude output column
                if (this.outputIndex != i) {
                    this.inputIndices.add(i);
                }

            }
        }

        // Convert list of labels into array of labels
        String[] aLabels = new String[this.inputIndices.size() + 1];

        for (int i = 0, j = 0; i < this.labels.size(); i++) {

            if (this.inputIndices.contains(i)) {
                aLabels[j] = this.labels.get(i);
                j++;
            }

        }

        // Add label at output index into last position
        aLabels[this.inputIndices.size()] = this.labels.get(this.outputIndex);

        // Convert list of symbols into array of symbols
        String[] aSymbols = new String[this.inputIndices.size() + 1];

        for (int i = 0, j = 0; i < this.symbols.size(); i++) {

            if (this.inputIndices.contains(i)) {
                aSymbols[j] = this.symbols.get(i);
                j++;
            }

        }

        // Add symbol at output index into last position
        aSymbols[this.inputIndices.size()] = this.symbols.get(this.outputIndex);

        // Convert list of values into array of values
        Double[][] aValues = new Double[this.values.size()][this.inputIndices.size() + 1];

        // Row of values
        int i = 0;

        // Get each values from list
        for (Map<Integer, Double> map : this.values) {

            // Column index
            int j = 0;

            // Get each items from map
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {

                Integer k = entry.getKey();
                Double v = entry.getValue();

                // Add all input values
                if (this.inputIndices.contains(k.intValue())) {
                    aValues[i][j] = v;
                    j++;
                }

            }

            // Add value at output index into last position
            aValues[i][j] = map.get(this.outputIndex);

            i++;
        }

        return new Dataset(aLabels, aSymbols, aValues);
    }

}

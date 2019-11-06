package com.neodynamica;

import com.neodynamica.backendinterface.Backend;
import com.neodynamica.backendinterface.GenerationBean;
import com.neodynamica.backendinterface.InvalidRunStateException;
import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main implements PropertyChangeListener {

    Backend backend;

    public static void main(String[] args) {
        Main tf = new Main();
        tf.run();
    }

    public void run() {
        try {
            backend = new Backend();

            backend.addPropertyChangeListener(this);

            backend.setConfigFilePath("preset1.config");

//            backend.setTargetColumnIndex(2);

            String[] labels = backend.getDatasetColumnLabels();
            Double[][] values = backend.getDatasetValues();
            printDataset(labels, values);

            System.out.println("STARTING SYMBOLIC REGRESSION >>>");

            backend.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SearchParameterException se) {
            se.printStackTrace();
        } catch (InvalidRunStateException ire) {
            ire.printStackTrace();
        }
    }

    public static void printDataset(String[] labels, Double[][] values) {
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                System.out.print(labels[j] + " : " + values[i][j] + " | ");
            }
            System.out.println();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if ("generation".equals(evt.getPropertyName())) {
                GenerationBean newGeneration = backend.getNextGeneration();
                printGeneration(newGeneration);
            } else if ("evolutionEnded".equals(evt.getPropertyName())) {
                System.out.println(">>> SYMBOLIC REGRESSION ENDED");
                printBest(1);
                long millis = backend.getCurrentDuration();
                System.out.println("Total search duration: " + String.format("%02d:%02d.%03d",
                        TimeUnit.MILLISECONDS.toMinutes(millis),
                        TimeUnit.MILLISECONDS.toSeconds(millis),
                        TimeUnit.MILLISECONDS.toMillis(millis)
                ));
                try {
                    backend.generateBestSolutionPredictionsFile("test.csv");
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }

            }
        } catch (SearchParameterException spe) {
            // do nothing
        } catch (IOException ioe) {
            // do nothing
        }
    }

    public void printGeneration(GenerationBean g) throws SearchParameterException, IOException {
        printlnIndent(0, "Generation " + g.getIndex());
        printlnIndent(1, "Error");
        printlnIndent(2, "    Best: " + g.getBestFitness());
        printlnIndent(2, "    Mean: " + g.getAverageFitness());
        printlnIndent(2, "  Median: " + g.getMedianFitness());
        printlnIndent(2, "Std Dvtn: " + g.getFitnessStandardDeviation());

        if (g.isNewBest()) {
            printlnIndent(1, "--- NEW BEST! ---");
            printBest(2);
        }
    }

    public void printBest(int indentLevel) throws SearchParameterException, IOException {
        printIndent(indentLevel, "FORMULA:\n");
        printIndent(indentLevel + 1, "" + backend.getTargetColumnLabel());
        printIndent(0, " = " + backend.getBestSolutionParenthesisString() + "\n");
        printIndent(0, "Error: " + backend.getLatestGeneration().getBestFitness().toString() + "\n");
        printIndent(indentLevel, "TREE:\n");

        String indentedTree = backend.getBestSolutionTreeString()
                .replaceAll(System.getProperty("line.separator"),
                        System.getProperty("line.separator") + buildIndent(indentLevel + 1));

        printIndent(indentLevel + 1, indentedTree);

        printIndent(indentLevel, "\nSIMPLIFIED TREE:\n");
        String simplifiedIndentedTree = backend.getBestSolutionTreeString()
                .replaceAll(System.getProperty("line.separator"),
                        System.getProperty("line.separator") + buildIndent(indentLevel + 1));

        printIndent(indentLevel + 1, simplifiedIndentedTree);

        printlnIndent(indentLevel, "");
    }

    private String buildIndent(int indentLevel) {
        String indent = "";

        for (int i = 0; i < indentLevel; i++) {
            indent += "    ";
        }

        return indent;
    }

    private void printlnIndent(int indentLevel, String str) {
        String indent = buildIndent(indentLevel);
        System.out.println(indent + str);
    }

    private void printIndent(int indentLevel, String str) {
        String indent = buildIndent(indentLevel);
        System.out.print(indent + str);
    }
}

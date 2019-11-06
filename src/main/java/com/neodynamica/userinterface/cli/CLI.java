package com.neodynamica.userinterface.cli;

import com.neodynamica.backendinterface.Backend;
import com.neodynamica.backendinterface.GenerationBean;
import com.neodynamica.backendinterface.InvalidRunStateException;
import com.neodynamica.lib.parameter.SearchParameterException;

import org.apache.commons.cli.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.lang.System.lineSeparator;
import static java.lang.System.out;

/**
 * The CLI class implements a command-line interface front end for the Neodynamica system.
 * It takes command line options, parses them, and passes data and commands to the back end of the system.
 */
public class CLI implements PropertyChangeListener {

    // full names for command line options
    private static final String HELP_OPTION = "help";
    private static final String USAGE_OPTION = "usage";

    private static final String NOT_VERBOSE_OPTION = "notVerbose";
    private static final String GENERATION_OVERWRITE_OPTION = "generationOverwrite";
    private static final String NO_GENERATION_OVERWRITE_OPTION = "noGenerationOverwrite";

    private static final String CONFIG_FILE_PATH_OPTION = "configFilePath";
    private static final String PREDICTIONS_FILE_PATH_OPTION = "predictionsFilePath";

    private static final String DATA_FILE_PATH_OPTION = "dataFilePath";
    private static final String TARGET_COLUMN_INDEX_OPTION = "targetColumnIndex";
    private static final String INPUT_COLUMN_INDICES_OPTION = "inputColumnIndices";
    private static final String PRINT_DATASET_OPTION = "printDataset";

    private static final String MAX_GENERATIONS_OPTION = "maxGenerations";
    private static final String ERROR_FUNCTION_OPTION = "errorFunction";
    private static final String OPERATORS_OPTION = "operators";
    private static final String SKELETON_OPTION = "skeleton";
    private static final String POPULATION_OPTION = "populationSize";
    private static final String MAX_SOLUTION_NODES_OPTION = "maxSolutionNodes";
    private static final String INITIAL_SOLUTION_DEPTH_OPTION = "initialSolutionDepth";
    private static final String SEED_OPTION = "seed";
    private static final String MULTI_OBJECTIVE_OPTIMISATION_OPTION = "multiObjectiveOptimisation";
    private static final String INCLUDE_INTEGERS_OPTION = "includeIntegers";
    private static final String INCLUDE_DECIMALS_OPTION = "includeDecimals";
    private static final String INCLUDE_DECIMAL_PLACES_OPTION = "includeDecimalPlaces";
    private static final String OFFSPRING_FRACTION_OPTION = "offspringFraction";
    private static final String MUTATOR_PROBABILITY_OPTION = "mutatorProbability";
    private static final String CROSSOVER_PROBABILITY_OPTION = "crossOverProbability";
    private static final String OFFSPRING_SAMPLE_SIZE_OPTION = "offspringSampleSize";
    private static final String SURVIVORS_SAMPLE_SIZE_OPTION = "survivorsSampleSize";
    private static final String STEADY_FITNESS_LIMIT_OPTION = "steadyFitnessLimit";
    private static final String TIME_LIMIT_OPTION = "timeLimit";

    // symbolic regression values
    private Backend backend;
    private String predictionsFilePath = null;
    private boolean printDataset = false;
    private int verbosityLevel = 4;
    private boolean isAlreadyFinished = false;
    private boolean isOverwritingGenerations = false;

    BufferedReader br = new BufferedReader(
            new InputStreamReader(System.in));

    /**
     * CLI entry point
     *
     * @param args
     */
    public static void main(String[] args) {
        CLI cli = new CLI();
        cli.run(args);
    }

    /**
     * Primary runner method
     *
     * @param args
     */
    public void run(String[] args) {
        /*
         * First we must check whether a help / usage command was used.
         * This must be done separately, with a separate options set,
         * or the 'required' conditions on arguments would still be enforced.
         */
        Scanner readInput = new Scanner(System.in);

        final Options helpOptions = generateHelpOptions();
        final CommandLine helpCommandLine = generateCommandLine(helpOptions, args);
        final Options options = generateOptions();

        if (helpCommandLine == null) {
            printUsage(options);
            return;
        }

        if (helpCommandLine.hasOption(HELP_OPTION)) {
            printHelp(options);
            return;
        }

        if (helpCommandLine.hasOption(USAGE_OPTION)) {
            printUsage(options);
            return;
        }

        final CommandLine commandLine = generateCommandLine(options, args);

        // If the command-line couldn't be parsed, exit early.
        if (commandLine == null) {
            printUsage(options);
            return;
        }

        try {
            backend = new Backend();
            backend.addPropertyChangeListener(this);

            setSearchParameters(commandLine);

            if (printDataset) {
                printlnIndent(0, "DATASET");
                printlnIndent();
                printDataset(backend.getDatasetColumnLabels(), backend.getDatasetValues());
                printlnIndent();
            }

            printSearchParameters(1);
            printlnIndent(1);
            printlnIndent(1, 0, "STARTING SYMBOLIC REGRESSION >>>");

            if (isOverwritingGenerations) {
                for (int i = 0; i < 9; i++) {
                    printlnIndent(1);
                }
            }

            backend.start();

            while (backend.getLatestGeneration() == null ||
                    backend.getLatestGeneration().getIndex() < backend.getMaxGenerations()) {

                while (!br.ready() && !isAlreadyFinished) {
                    Thread.sleep(200);
                }

                if (!isAlreadyFinished) {
                    String enterKey = br.readLine();

                    TimeUnit.MILLISECONDS.sleep(200);

                    if ("".equals(enterKey)) {
                        switch (backend.getRunState()) {
                            case PAUSED:
                                showResumeMessage();
                                backend.resume();
                                break;
                            case STARTED:
                            case RUNNING:
                            case RESUMED:
                                backend.pause();
                                showPausePrompt();
                                backend.resume();
                                break;
                            case ENDED:
                                printlnIndent(1, 0, "EXITING.");
                                break;
                        }
                    }
                }
            }
        } catch (IOException | SearchParameterException | InvalidRunStateException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate command line options when a help command is detected.
     *
     * @return Definition of command-line options.
     */
    private static Options generateHelpOptions() {
        return generateOptions(true);
    }

    /**
     * Generate options when help command not detected
     * (mandatory fields should be made non-mandatory in this case)
     *
     * @return Definition of command-line options.
     */
    private static Options generateOptions() {
        return generateOptions(false);
    }

    /**
     * Definition of all command line options
     *
     * @param help
     * @return Definition of command-line options.
     */
    private static Options generateOptions(boolean help) {
        final Options options = new Options();

        // Data File Path
        options.addOption(
                Option.builder("d")
                        .required(false)
                        .longOpt(DATA_FILE_PATH_OPTION)
                        .hasArg(true)
                        .desc("Path of data file. DEFAULT: None. Must be specified on CLI or in config file.")
                        .build());

        // Not Verbose
        options.addOption(
                Option.builder("V")
                        .required(false)
                        .longOpt(NOT_VERBOSE_OPTION)
                        .hasArg(false)
                        .desc("Run in non-verbose mode. Print details parameters and for each generation. DEFAULT: off")
                        .build());

        // Overwrite Generation Print
        options.addOption(
                Option.builder("w")
                        .required(false)
                        .longOpt(GENERATION_OVERWRITE_OPTION)
                        .hasArg(false)
                        .desc("Replace the information printed for each generation rather than printing each generation sequentially. DEFAULT: off")
                        .build());

        // Do Not Overwrite Generation Print
        options.addOption(
                Option.builder("W")
                        .required(false)
                        .longOpt(NO_GENERATION_OVERWRITE_OPTION)
                        .hasArg(false)
                        .desc("Option for turning off generation overwriting. For use in pause. DEFAULT: off")
                        .build());

        // Print Dataset
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(PRINT_DATASET_OPTION)
                        .hasArg(false)
                        .desc("If used, dataset with be printed to screen on startup. DEFAULT: off")
                        .build());

        // Help
        options.addOption(
                Option.builder("h")
                        .required(false)
                        .hasArg(false)
                        .longOpt(HELP_OPTION)
                        .desc("Display help information.")
                        .build());

        // Usage
        options.addOption(
                Option.builder("u")
                        .required(false)
                        .longOpt(USAGE_OPTION)
                        .hasArg(false)
                        .desc("Display usage information.")
                        .build());

        // Config file path
        options.addOption(
                Option.builder("c")
                        .required(false)
                        .longOpt(CONFIG_FILE_PATH_OPTION)
                        .hasArg(true)
                        .desc("Path of a configuration file. If none is specified, " +
                                "all options must be specified by command-line or come from defaults. DEFAULT: ''")
                        .build());

        // Prediction CSV output file path
        options.addOption(
                Option.builder("r")
                        .required(false)
                        .longOpt(PREDICTIONS_FILE_PATH_OPTION)
                        .hasArg(true)
                        .desc("Path to save an output CSV file with the input data and the calculated output of the best " +
                                "formula for the inputs of each row. If an existing file path is specified, it will be overwritten. " +
                                "If none is specified, file will not be generated. DEFAULT: none")
                        .build());

        // Max generations
        options.addOption(
                Option.builder("g")
                        .required(false)
                        .longOpt(MAX_GENERATIONS_OPTION)
                        .hasArg(true)
                        .desc("An integer limit to the number of iterations of the symbolic regression run. " +
                                "DEFAULT: '2000'")
                        .build());

        // Error Function
        options.addOption(
                Option.builder("e")
                        .required(false)
                        .longOpt(ERROR_FUNCTION_OPTION)
                        .hasArg(true)
                        .desc("The name of the error function to be used. DEFAULT: 'RMSE'")
                        .build());

        // Population
        options.addOption(
                Option.builder("p")
                        .required(false)
                        .longOpt(POPULATION_OPTION)
                        .hasArg(true)
                        .desc("An integer specifying the number of candidate solutions per iteration. DEFAULT: '100'")
                        .build());

        // Operators
        options.addOption(
                Option.builder("o")
                        .required(false)
                        .longOpt(OPERATORS_OPTION)
                        .hasArg(true)
                        .desc("A string specifying the operators possible to be used in solutions (e.g., " +
                                "`ADD,SUB,MUL,DIV`). DEFAULT: 'ADD,SUB,MUL,DIV,SIN,COS,TAN'")
                        .build());

        // Skeleton
        options.addOption(
                Option.builder("s")
                        .required(false)
                        .longOpt(SKELETON_OPTION)
                        .hasArg(true)
                        .desc("Specify a complex segment you expect to be in the solution (e.g., `?^2` " +
                                "where '?' indicates a 'wildcard'. Leave blank to specify none. DEFAULT: ''")
                        .build());

        // Max solution nodes
        options.addOption(
                Option.builder("n")
                        .required(false)
                        .longOpt(MAX_SOLUTION_NODES_OPTION)
                        .hasArg(true)
                        .desc("Specify the maximum number of nodes (operators, variables, and literals) " +
                                "in the solution. DEFAULT: '100'")
                        .build());

        // Target column index
        options.addOption(
                Option.builder("t")
                        .required(false)
                        .longOpt(TARGET_COLUMN_INDEX_OPTION)
                        .hasArg(true)
                        .desc("Specify the index of the column that is the target output for the mathematical model. " +
                                "Use '-1' to indicate the right-most non-empty column. DEFAULT: '-1'")
                        .build());

        // Input Column indices
        options.addOption(
                Option.builder("i")
                        .required(false)
                        .longOpt(INPUT_COLUMN_INDICES_OPTION)
                        .hasArg(true)
                        .desc("Specify the indices, separated by commas, of the columns that can be used as inputs for" +
                                " the mathematical model. Use 'ALL' to use all columns that are not the target column. " +
                                "DEFAULT: 'ALL'")
                        .build());

        // Initial solutions' depth
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(INITIAL_SOLUTION_DEPTH_OPTION)
                        .hasArg(true)
                        .desc("Specify depth of solutions generated for the initial populations. " + "DEFAULT: '2'")
                        .build());

        // Seed
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(SEED_OPTION)
                        .hasArg(true)
                        .desc("Specify an integer seed for the random generator. Specifying a value for this makes the symbolic regression reproducable, running in single-threaded mode (slower). If set to 0, the seed will be randomly generated and the program will be run in multi-threaded mode." + "DEFAULT: 0")
                        .build());

        // Multi Objective Optimisation
        options.addOption(
                Option.builder("m")
                        .required(false)
                        .longOpt(MULTI_OBJECTIVE_OPTIMISATION_OPTION)
                        .hasArg(false)
                        .desc("Flag to turn on multi objective optimation mode" + "DEFAULT: off")
                        .build());

        // Include integers
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(INCLUDE_INTEGERS_OPTION)
                        .hasArg(true)
                        .desc("Specify two integers bounding the range of integers that may be included in solutions as constants. Of the form '0,10' (bottom of range, comma, top of range). " + "DEFAULT: '0,10'")
                        .build());

        // Include decimals
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(INCLUDE_DECIMALS_OPTION)
                        .hasArg(true)
                        .desc("Specify two integers bounding range of decimals that may be included in solutions as constants. Of the form '0,10' (bottom of range, comma, top of range). " + "DEFAULT: '0,10'")
                        .build());

        // Include decimal places
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(INCLUDE_DECIMAL_PLACES_OPTION)
                        .hasArg(true)
                        .desc("Specify the number of decimal places to be used in decimal constants. " + "DEFAULT: '2'")
                        .build());

        // Offspring fraction
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(OFFSPRING_FRACTION_OPTION)
                        .hasArg(true)
                        .desc("Specify the ratio (as a decimal 0-1) of offspring to survivors at each evolution step. " + "DEFAULT: '0.6'")
                        .build());


        // Mutator probability
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(MUTATOR_PROBABILITY_OPTION)
                        .hasArg(true)
                        .desc("Specify the probability of mutation as a decimal 0-1. " + "DEFAULT: '0.01'")
                        .build());

        // Crossover probability
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(CROSSOVER_PROBABILITY_OPTION)
                        .hasArg(true)
                        .desc("Specify the probability of crossover as a decimal 0-1. " + "DEFAULT: '0.05'")
                        .build());

        // Offspring sample size
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(OFFSPRING_SAMPLE_SIZE_OPTION)
                        .hasArg(true)
                        .desc("Specify a positive integer for the sample size of the offspring selector. " + "DEFAULT: '3'")
                        .build());

        // Survivor sample size
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(SURVIVORS_SAMPLE_SIZE_OPTION)
                        .hasArg(true)
                        .desc("Specify a positive integer for the sample size of the surivors selector. " + "DEFAULT: '3'")
                        .build());

        // Steady fitness limit
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(STEADY_FITNESS_LIMIT_OPTION)
                        .hasArg(true)
                        .desc("Specify a positive integer for the number of generations of steady fitness that should cause the symbolic regression to terminate. " + "DEFAULT: '99999'")
                        .build());

        // Evolution time limit
        options.addOption(
                Option.builder()
                        .required(false)
                        .longOpt(TIME_LIMIT_OPTION)
                        .hasArg(true)
                        .desc("Specify a time limit in seconds for terminating the symbolic regression. " + "DEFAULT: '3600'")
                        .build());


        return options;
    }

    /**
     * "Parsing" stage of command-line processing using Apache Commons CLI.
     *
     * @param options              Options from "definition" stage.
     * @param commandLineArguments Command-line arguments provided to application.
     * @return Instance of CommandLine as parsed from the provided Options and command line
     * arguments; may be {@code null} if there is an exception encountered while attempting to parse
     * the command line options.
     */
    private static CommandLine generateCommandLine(final Options options,
                                                   final String[] commandLineArguments) {
        final CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = cmdLineParser.parse(options, commandLineArguments);
        } catch (ParseException parseException) {
            out.println(parseException.getMessage());
        }
        return commandLine;
    }

    /**
     * Generating usage information using Apache Commons CLI.
     *
     * @param options Instance of Options to be used to prepare usage formatter.
     * @return HelpFormatter instance that can be used to print usage information.
     */
    private static void printUsage(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = "nda";
        final PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, syntax, options);
        pw.println("Use 'nda -h' or 'nda --help' to show details for options.");
        pw.flush();
    }

    /**
     * Generating help information using Apache Commons CLI.
     *
     * @param options Instance of Options to be used to prepare help formatter.
     * @return HelpFormatter instance that can be used to print help information.
     */
    private static void printHelp(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = "nda";
        final String usageHeader = "\nOptions:";
        final String usageFooter = "See online documentation for more details: <TBA>."; //TODO: add URL here
        final PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, syntax, usageHeader, options, 4, 4, usageFooter, true);
        pw.flush();
    }

    /**
     * Method run by Backend to alert CLI that a new generation is available.
     *
     * @param evt
     */
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (!isAlreadyFinished) {
                GenerationBean newGeneration = backend.getNextGeneration();
                printGeneration(1, newGeneration);

                if (newGeneration.getIndex() >= backend.getMaxGenerations()) {
                    isAlreadyFinished = true;

                    printlnIndent(1);
                    printlnIndent(1, 0, ">>> SYMBOLIC REGRESSION ENDED");
                    printlnIndent(1);
                    printlnIndent(1, 1, "TOTAL RUNNING TIME: " + currentTimeString());
                    printlnIndent(1);
                    printlnIndent(0, 1, "BEST FORMULA FOUND: ");
                    printBest(0, 2);

                    generatePredictionsCsvFileAndPrintFilePath();
                    printlnIndent();
                }
            }
        } catch (SearchParameterException | IOException | InvalidRunStateException ignored) {
            // do nothing
        }
    }

    /**
     * Print information for each iteration of the symbolic regression.
     *
     * @param g
     * @throws SearchParameterException
     * @throws IOException
     */
    public void printGeneration(int verbosityLevel, GenerationBean g) throws SearchParameterException, IOException {
        StringBuilder generationStringBuilder = new StringBuilder();

        generationStringBuilder.append("" + lineSeparator());
        generationStringBuilder.append(buildIndent(0) + "Generation " + g.getIndex() + lineSeparator());
        generationStringBuilder.append(buildIndent(1) + "Error (" + backend.getErrorFunction() + ")" + lineSeparator());
        generationStringBuilder.append(buildIndent(2) + "    Best: " + g.getBestFitness() + "    " + lineSeparator());
        generationStringBuilder.append(buildIndent(2) + "    Mean: " + g.getAverageFitness() + "    " + lineSeparator());
        generationStringBuilder.append(buildIndent(2) + "  Median: " + g.getMedianFitness() + "    " + lineSeparator());
        generationStringBuilder.append(buildIndent(2) + "Std Dvtn: " + g.getFitnessStandardDeviation() + "    " + lineSeparator());

        if (isOverwritingGenerations) {
            generationStringBuilder.append(buildIndent(1) + "Current Best Formula: " + lineSeparator());
            String formulaString = backend.getTargetColumnLabel() +
                    " = " +
                    backend.getBestSolutionParenthesisString();

            if (formulaString.length() > 100) {
                formulaString = formulaString.substring(0, 99) + " ...";
            }

            generationStringBuilder.append(buildIndent(2) + formulaString + lineSeparator());
        }

        String generationString = generationStringBuilder.toString();

        if (isOverwritingGenerations && verbosityLevel <= this.verbosityLevel) {
            int count = 9;
            for (int i = 0; i < count; i++) {
                System.out.print("\033[A"); // Move up
                System.out.print("\033[2K"); // Erase line content
            }
        }

        printIndent(verbosityLevel, 0, generationString);

        if (!isOverwritingGenerations && g.isNewBest()) {
            printlnIndent(verbosityLevel);
            printlnIndent(verbosityLevel, 0, "--- NEW BEST! ---");
            printBest(verbosityLevel, 1);
        }
    }

    /**
     * Print out the best solution as a formula and a tree representation.
     *
     * @param indentLevel
     * @throws SearchParameterException
     * @throws IOException
     */
    public void printBest(int verbosityLevel, int indentLevel) throws SearchParameterException, IOException {
        printlnIndent(verbosityLevel, indentLevel, "ERROR (" + backend.getErrorFunction() + "): " + backend.getLatestGeneration().getBestFitness());
        printlnIndent(verbosityLevel, indentLevel, "FORMULA: ");
        printlnIndent(verbosityLevel, indentLevel + 1, backend.getTargetColumnLabel() + " = " + backend.getBestSolutionParenthesisString());
        printlnIndent(verbosityLevel, indentLevel, "TREE:");

        String indentedTree = backend.getBestSolutionTreeString()
                .replaceAll("(\r\n|\n|" + System.lineSeparator() + ")",
                        System.lineSeparator() + buildIndent(indentLevel + 1));

        printIndent(verbosityLevel, indentLevel + 1, indentedTree);
        printlnIndent(verbosityLevel);
    }

    /**
     * Build indents given an integer level of indent.
     *
     * @param indentLevel
     * @return
     */
    public static String buildIndent(int indentLevel) {
        final String indentUnit = "    "; // 4 spaces for an indent

        StringBuilder builtIndent = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            builtIndent.append(indentUnit);
        }

        return builtIndent.toString();
    }

    /**
     * Utility method for printing empty line in manner consistent with the other indent
     * printing methods. Includes a verbosity filter.
     */
    public void printlnIndent(int verbosityLevel) {
        if (verbosityLevel <= this.verbosityLevel) {
            printlnIndent();
        }
    }

    /**
     * Utility method for printing empty line in manner consistent with the other indent
     * printing methods
     */
    public void printlnIndent() {
        printlnIndent(0, "");
    }

    /**
     * Utility method for printing empty line in manner consistent with the other indent
     * printing methods
     */
    public void printlnIndent(String str) {
        printlnIndent(0, "");
    }

    /**
     * Utility method for printing with indents, with a newline afterwards. Includes verbosityLevel filter.
     *
     * @param indentLevel
     * @param str
     */
    public void printlnIndent(int verbosityLevel, int indentLevel, String str) {
        if (verbosityLevel <= this.verbosityLevel) {
            printlnIndent(indentLevel, str);
        }
    }

    /**
     * Utility method for printing with indents, with a newline afterwards.
     *
     * @param indentLevel
     * @param str
     */
    public void printlnIndent(int indentLevel, String str) {
        String indent = buildIndent(indentLevel);
        System.out.println(indent + str);
    }

    /**
     * Utility method for printing with indents. Includes verbosityLevel filter.
     *
     * @param indentLevel
     * @param str
     */
    public void printIndent(int verbosityLevel, int indentLevel, String str) {
        if (verbosityLevel <= this.verbosityLevel) {
            printIndent(indentLevel, str);
        }
    }

    /**
     * Utility method for printing with indents.
     *
     * @param indentLevel
     * @param str
     */
    public void printIndent(int indentLevel, String str) {
        String indent = buildIndent(indentLevel);
        System.out.print(indent + str);
    }

    /**
     * Print the search parameters
     *
     * @throws SearchParameterException
     */
    public void printSearchParameters(int verbosityLevel) throws SearchParameterException {
        int indentLevel = 0;
        printlnIndent(verbosityLevel, indentLevel, "SEARCH PARAMETERS");
        indentLevel += 1;

        String unsetLabel = "NOT SET";

        String configFilePath = backend.getConfigFilePath();
        if (configFilePath == null) {
            configFilePath = unsetLabel;
        }
        printlnIndent(verbosityLevel, indentLevel, "configFilePath: " + configFilePath);

        boolean verbose = true;
        if (verbosityLevel == 1) {
            verbose = false;
        }
        printlnIndent(verbosityLevel, indentLevel, "notVerbose: " + verbose);

        String predictionsFilePath = this.predictionsFilePath;
        if (predictionsFilePath == null) {
            predictionsFilePath = unsetLabel;
        }
        printlnIndent(verbosityLevel, indentLevel, "predictionsFilePath: " + predictionsFilePath);

        printlnIndent(verbosityLevel, indentLevel, "dataFilePath: " + backend.getDataFilePath());
        printlnIndent(verbosityLevel, indentLevel, "errorFunction: " + backend.getErrorFunction());
        printlnIndent(verbosityLevel, indentLevel, "populationSize: " + backend.getPopulationSize());
        printlnIndent(verbosityLevel, indentLevel, "operators: " + backend.getOperators());

        String skeleton = backend.getSkeleton();
        if (skeleton == null) {
            skeleton = unsetLabel;
        }
        printlnIndent(verbosityLevel, indentLevel, "skeleton: " + skeleton);
        printlnIndent(verbosityLevel, indentLevel, "targetColumnIndex: " + backend.getTargetColumnIndex());
        printlnIndent(verbosityLevel, indentLevel, "inputColumnIndices: " + backend.getInputColumnIndices());
        try {
            printlnIndent(verbosityLevel, indentLevel, "seed: " + backend.getSeed());
        } catch (SearchParameterException spe) {
            printlnIndent(verbosityLevel, indentLevel, "seed: " + unsetLabel);
        }
        printlnIndent(verbosityLevel, indentLevel, "multiObjectiveOptimisation: " + backend.getMultiObjectiveOptimisation());
        printlnIndent(verbosityLevel, indentLevel, "includeIntegers: " + backend.getIncludeIntegers());
        printlnIndent(verbosityLevel, indentLevel, "includeDecimals: " + backend.getIncludeDecimals());
        printlnIndent(verbosityLevel, indentLevel, "includeDecimalPlaces: " + backend.getIncludeDecimalPlaces());
        printlnIndent(verbosityLevel, indentLevel, "offspringFraction: " + backend.getOffspringFraction());
        printlnIndent(verbosityLevel, indentLevel, "offspringSampleSize: " + backend.getOffspringSampleSize());
        printlnIndent(verbosityLevel, indentLevel, "survivorsSampleSize: " + backend.getSurvivorsSampleSize());

        printlnIndent(verbosityLevel, indentLevel, "maxGenerations: " + backend.getMaxGenerations());
        printlnIndent(verbosityLevel, indentLevel, "steadyFitnessLimit: " + backend.getSteadyFitnessLimit());
        printlnIndent(verbosityLevel, indentLevel, "timeLimit: " + backend.getTimeLimit());

    }

    /**
     * Reads in a command line to change parameters in the pause state
     *
     * @throws IOException
     * @throws InvalidRunStateException
     * @throws SearchParameterException
     */
    private void readChangedParameters()
            throws IOException, InvalidRunStateException, SearchParameterException {
        Pattern pattern = Pattern.compile(" ");
        String[] args;

        System.out.println("Current Running Time: " + currentTimeString() + "    " );
        System.out.println();
        System.out.println("Enter new parameters: ");
        System.out.print("$ nda ");

        Scanner in = new Scanner(System.in);
        String line = in.nextLine();
        args = pattern.split(line);

        if ("nda".equals(args[0])) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        Options options = generateOptions();

        final CommandLine commandLine = generateCommandLine(options, args);

        setSearchParameters(commandLine);
    }

    /**
     * Shows a message on resuming
     *
     * @throws InterruptedException
     */
    private void showResumeMessage() throws InterruptedException {
        printlnIndent();
        printlnIndent(0, "|> REGRESSION RESUMED");
        printlnIndent();
        TimeUnit.MILLISECONDS.sleep(300);
    }

    /**
     * Provides the command prompt on pausing a symbolic regression
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws InvalidRunStateException
     * @throws SearchParameterException
     */
    private void showPausePrompt() throws InterruptedException, IOException, InvalidRunStateException, SearchParameterException {
        TimeUnit.MILLISECONDS.sleep(300);
        printlnIndent();
        printlnIndent(0, "|| REGRESSION PAUSED");
        printlnIndent();
        readChangedParameters();
        printlnIndent();
        printSearchParameters(1);
        printlnIndent();
        printlnIndent(0, "|> REGRESSION RESUMED");
        printlnIndent();
    }

    public static void printDataset(String[] labels, Double[][] values) {
        int columnWidth = 15;

        System.out.print("  ");
        for (int i = 0; i < labels.length; i++) {
            System.out.print(String.format("%-" + columnWidth + "s", labels[i].substring(0, Math.min(labels[i].length(), 15))));
            if (i < values[0].length - 1) {
                System.out.print("  |  ");
            }
        }
        System.out.println();

        System.out.print("--");
        for (int i = 0; i < labels.length; i++) {
            System.out.print(String.format("%-" + columnWidth + "s", "").replace(' ', '-'));
            if (i < values[0].length - 1) {
                System.out.print("--+--");
            }
        }
        System.out.println();

        for (int i = 0; i < values.length; i++) {
            System.out.print("  ");
            for (int j = 0; j < values[0].length; j++) {
                System.out.print(String.format("%-" + columnWidth + "s", values[i][j]));
                if (j < values[0].length - 1) {
                    System.out.print("  |  ");
                }
            }
            System.out.println();
        }
    }

    /**
     * Sends command to Backend to generate the predictions CSV file with the specified file path.
     * Prints a message to screen to state that this has been done.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    private void generatePredictionsCsvFileAndPrintFilePath() throws InvalidRunStateException, IOException {
        if (predictionsFilePath != null) {
            predictionsFilePath = addCsvFileExtension(predictionsFilePath);
            printlnIndent(1, "SAVING PREDICTIONS CSV FILE TO '" + predictionsFilePath + "'");
            backend.generateBestSolutionPredictionsFile(predictionsFilePath);
        }
    }

    /**
     * Adds a '.csv' file extension to a string if it's not already present.
     *
     * @param filePath - the path which is specified for saving the file as
     * @throws InterruptedException
     * @throws IOException
     */
    private String addCsvFileExtension(String filePath) throws InvalidRunStateException, IOException {
        if (filePath.length() < 5 ||
                !filePath.substring(filePath.length() - 4).equalsIgnoreCase(".csv")) {
            filePath = filePath + ".csv";
        }
        return filePath;
    }

    private String currentTimeString() {
        long currentDurationInMilliseconds = backend.getCurrentDuration();
        long currentDurationInSeconds = currentDurationInMilliseconds / 1000;
        long hours = currentDurationInSeconds / 60 / 60;
        long minutes = currentDurationInSeconds / 60 % 60;
        long seconds = currentDurationInSeconds % 60;

        String timeString = "";

        if (hours > 0) {
            timeString += hours + "h";
        }

        if (hours > 0 || minutes > 0) {
            timeString += minutes + "m";
        }

        timeString += seconds + "s";

        if (hours > 0 || minutes > 0) {
            timeString += " (" + currentDurationInSeconds + "s" + ")";
        }

        return timeString;
    }

    /**
     * Take search parameters parsed from command line and set them on the back end
     *
     * @param commandLine
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws SearchParameterException
     * @throws InvalidRunStateException
     */
    private void setSearchParameters(CommandLine commandLine)
            throws IllegalArgumentException, IOException, SearchParameterException, InvalidRunStateException {
        // options that are present in config files
        final String configFilePath = commandLine.getOptionValue(CONFIG_FILE_PATH_OPTION);
        if (configFilePath != null && !configFilePath.isEmpty()) {
            backend.setConfigFilePath(configFilePath);
        }

        // options that are present in config files
        final String predictionsFilePath = commandLine.getOptionValue(PREDICTIONS_FILE_PATH_OPTION);
        if (predictionsFilePath != null && !predictionsFilePath.isEmpty()) {
            this.predictionsFilePath = predictionsFilePath;
        }

        final String dataFilePath = commandLine.getOptionValue(DATA_FILE_PATH_OPTION);
        if (dataFilePath != null && !dataFilePath.isEmpty()) {
            backend.setDataFilePath(dataFilePath);
        }

        final String maxGenerations = commandLine.getOptionValue(MAX_GENERATIONS_OPTION);
        if (maxGenerations != null && !maxGenerations.isEmpty()) {
            backend.setMaxGenerations(Integer.parseInt(maxGenerations));
        }

        final String errorFunction = commandLine.getOptionValue(ERROR_FUNCTION_OPTION);
        if (errorFunction != null && !errorFunction.isEmpty()) {
            backend.setErrorFunction(errorFunction);
        }

        final String population = commandLine.getOptionValue(POPULATION_OPTION);
        if (population != null && !population.isEmpty()) {
            backend.setPopulationSize(Integer.parseInt(population));
        }

        final String operators = commandLine.getOptionValue(OPERATORS_OPTION);
        if (operators != null && !operators.isEmpty()) {
            backend.setOperators(operators);
        }

        final String skeleton = commandLine.getOptionValue(SKELETON_OPTION);
        if (skeleton != null && !skeleton.isEmpty()) {
            //sp.setSkeleton(SolutionSkeleton.createOpFromString(skeleton));
            backend.setSkeleton(skeleton);
        }

        final String maxSolutionNodes = commandLine.getOptionValue(MAX_SOLUTION_NODES_OPTION);
        if (maxSolutionNodes != null && !maxSolutionNodes.isEmpty()) {
            backend.setMaxSolutionNodes(Integer.parseInt(maxSolutionNodes));
        }

        final String targetColumnIndex = commandLine.getOptionValue(TARGET_COLUMN_INDEX_OPTION);
        if (targetColumnIndex != null && !targetColumnIndex.isEmpty()) {
            backend.setTargetColumnIndex(Integer.parseInt(targetColumnIndex));
        }

        final String inputColumnIndices = commandLine.getOptionValue(INPUT_COLUMN_INDICES_OPTION);
        if (inputColumnIndices != null && !inputColumnIndices.isEmpty()) {
            backend.setInputColumnIndices(inputColumnIndices);
        }

        final String initialSolutionDepth = commandLine.getOptionValue(INITIAL_SOLUTION_DEPTH_OPTION);
        if (initialSolutionDepth != null && !initialSolutionDepth.isEmpty()) {
            backend.setInitialSolutionDepth(Integer.parseInt(initialSolutionDepth));
        }

        final String seed = commandLine.getOptionValue(SEED_OPTION);
        if (seed != null && !seed.isEmpty()) {
            backend.setSeed(Long.parseLong(seed));
        }

        final Boolean isMultiObjectiveOptimisation = commandLine.hasOption(MULTI_OBJECTIVE_OPTIMISATION_OPTION);
        if (isMultiObjectiveOptimisation) {
            backend.setMultiObjectiveOptimisation(isMultiObjectiveOptimisation);
        }

        final String includeIntegers = commandLine.getOptionValue(INCLUDE_INTEGERS_OPTION);
        if (includeIntegers != null && !includeIntegers.isEmpty()) {
            backend.setIncludeIntegers(includeIntegers);
        }

        final String includeDecimals = commandLine.getOptionValue(INCLUDE_DECIMALS_OPTION);
        if (includeDecimals != null && !includeDecimals.isEmpty()) {
            backend.setIncludeDecimals(includeDecimals);
        }

        final String includeDecimalPlaces = commandLine.getOptionValue(INCLUDE_DECIMAL_PLACES_OPTION);
        if (includeDecimalPlaces != null && !includeDecimalPlaces.isEmpty()) {
            backend.setIncludeDecimalPlaces(Integer.parseInt(includeDecimalPlaces));
        }

        final String offspringFraction = commandLine.getOptionValue(OFFSPRING_FRACTION_OPTION);
        if (offspringFraction != null && !offspringFraction.isEmpty()) {
            backend.setOffspringFraction(Double.parseDouble(offspringFraction));
        }

        final String mutatorProbability = commandLine.getOptionValue(MUTATOR_PROBABILITY_OPTION);
        if (mutatorProbability != null && !mutatorProbability.isEmpty()) {
            backend.setMutatorProbability(Double.parseDouble(mutatorProbability));
        }

        final String crossoverProbability = commandLine.getOptionValue(CROSSOVER_PROBABILITY_OPTION);
        if (crossoverProbability != null && !crossoverProbability.isEmpty()) {
            backend.setCrossoverProbability(Double.parseDouble(crossoverProbability));
        }

        final String offspringSampleSize = commandLine.getOptionValue(OFFSPRING_SAMPLE_SIZE_OPTION);
        if (offspringSampleSize != null && !offspringSampleSize.isEmpty()) {
            backend.setOffspringSampleSize(Integer.parseInt(offspringSampleSize));
        }

        final String survivorsSampleSize = commandLine.getOptionValue(SURVIVORS_SAMPLE_SIZE_OPTION);
        if (survivorsSampleSize != null && !survivorsSampleSize.isEmpty()) {
            backend.setSurvivorsSampleSize(Integer.parseInt(survivorsSampleSize));
        }

        final String steadyFitnessLimit = commandLine.getOptionValue(STEADY_FITNESS_LIMIT_OPTION);
        if (steadyFitnessLimit != null && !steadyFitnessLimit.isEmpty()) {
            backend.setSteadyFitnessLimit(Integer.parseInt(steadyFitnessLimit));
        }

        final String timeLimit = commandLine.getOptionValue(TIME_LIMIT_OPTION);
        if (timeLimit != null && !timeLimit.isEmpty()) {
            backend.setTimeLimit(Integer.parseInt(timeLimit));
        }

        final Boolean printDataset = commandLine.hasOption(PRINT_DATASET_OPTION);
        this.printDataset = printDataset;

        final Boolean notVerbose = commandLine.hasOption(NOT_VERBOSE_OPTION);
        this.verbosityLevel = (notVerbose ? 0 : 1);

        final Boolean isOverwritingGenerations = commandLine.hasOption(GENERATION_OVERWRITE_OPTION);
        this.isOverwritingGenerations = this.isOverwritingGenerations || isOverwritingGenerations;

        final Boolean isNotOverwritingGenerations = commandLine.hasOption(NO_GENERATION_OVERWRITE_OPTION);
        if (isNotOverwritingGenerations) {
            this.isOverwritingGenerations = false;
        }
    }
}

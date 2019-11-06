package com.neodynamica.userinterface.cli;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;

import static java.lang.System.in;
import static org.junit.jupiter.api.Assertions.*;
import static java.lang.System.lineSeparator;

class CLITest {

    // unit tests

    /**
     * returns a string of spaces of length = 4 * indentLevel
     */
    @Tag("unit")
    @Test
    void buildIndentWorks() {
        try {
            String indentString = CLI.buildIndent(10);

            assertEquals(indentString.length(), 40, "Indent is of wrong length.");
            assertFalse(indentString.contains("[^ ]"), "Indent contains non-space characters.");

        } catch (Exception e) {
            fail("An exception occurred", e);
        }
    }

    // system tests
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);

        // keep for debugging
        //System.out.println("out: " + outContent.toString());
        //System.out.println("err: " + errContent.toString());
    }

    public String[] argsArray(String argsString) {
        return argsString.split("\\s");
    }

    public void callCLI(String optionString) {
        CLI.main(optionString.split("\\s"));
    }

    /**
     * Test functional requirement / use-case, 'FR001 - Reading Dataset'
     */
    @Tag("system")
    @Test
    public void fr001ReadingDataset() {
        callCLI("-d datasets/sin_a.csv -V --maxGenerations 1 --printDataset");

        String actualPrint = outContent.toString();

        String selectedActualLines = selectLinesInRange(actualPrint, 0, 33);

        assertEquals(CLITestStrings.fr001ExpectedDatasetPrint, selectedActualLines);
    }

    /**
     * Test functional requirement / use-case, 'FR002 - Display Data Set - NO MODIFY'
     */
    @Tag("system")
    @Test
    public void fr002DisplayDataSet() {
        callCLI("-d datasets/sin_a.csv -V --maxGenerations 1 --printDataset");

        String actualPrint = outContent.toString();

        String selectedActualLines = selectLinesInRange(actualPrint, 0, 33);

        assertEquals(CLITestStrings.fr001ExpectedDatasetPrint, selectedActualLines);
    }

    /**
     * Test functional requirement / use-case, 'FR003 - Specify Search Parameters'
     */
    @Tag("system")
    @Test
    public void fr003SpecifySearchParameters() {
        String commandString =
                "--configFilePath UnitTestFiles/unit_test_config_2.config " + " " +
                        "" + " " +
                        "--predictionsFilePath UnitTestFiles/unit_test_prediction_file.csv" + " " +
                        "-d UnitTestFiles/sin_a.csv" + " " +
                        "--errorFunction MSE" + " " +
                        "--populationSize 10" + " " +
                        "--operators POW" + " " +
                        "--skeleton ?*?" + " " +
                        "--targetColumnIndex 0" + " " +
                        "--inputColumnIndices 1" + " " +
                        "--seed 54321" + " " +
                        "-m" + " " +
                        "--includeIntegers 1,2" + " " +
                        "--includeDecimals 3,4" + " " +
                        "--includeDecimalPlaces 4" + " " +
                        "--offspringFraction 0.123" + " " +
                        "--offspringSampleSize 4" + " " +
                        "--survivorsSampleSize 6" + " " +
                        "--maxGenerations 5" + " " +
                        "--steadyFitnessLimit 5678" + " " +
                        "--timeLimit 3321" + " " +
                        "";


        callCLI(commandString);

        String actualPrint = outContent.toString();

        int lastLineToCheck = 24;
        String actualLines = selectLinesInRange(actualPrint, 0, lastLineToCheck);
        String expectedLines = CLITestStrings.fr003ExpectedParametersPrint;

        String selectedExpectedLine;
        String selectedActualLine;
        for (int i = 0; i <= lastLineToCheck; i++) {
            selectedExpectedLine = selectLinesInRange(expectedLines, i, i);
            selectedActualLine = selectLinesInRange(actualLines, i, i);
            assertEquals(selectedExpectedLine,
                    selectedActualLine,
                    "Non-matching strings on line " + i + lineSeparator() +
                            "EXPECTED: '" + selectedExpectedLine + "' != '" + lineSeparator() +
                            "  ACTUAL: '" + selectedActualLine + "'");
        }
    }

    /**
     * Test functional requirement / use-case, 'FR004 - Save Search Parameters'
     */
    @Tag("system")
    @Disabled
    @Test
    public void fr004SaveSearchParameters() {
        // not implemented
    }

    /**
     * Test functional requirement / use-case, 'FR005 - Load Search Parameters'
     */
    @Tag("system")
    @Test
    public void fr005LoadSearchParameters() {
        callCLI("-c UnitTestFiles/unit_test_config_1.config");

        String actualPrint = outContent.toString();

        int lastLineToCheck = 24;
        String actualLines = selectLinesInRange(actualPrint, 0, lastLineToCheck);
        String expectedLines = CLITestStrings.fr005ExpectedParametersPrint;

        String selectedExpectedLine;
        String selectedActualLine;
        for (int i = 0; i <= lastLineToCheck; i++) {
            selectedExpectedLine = selectLinesInRange(expectedLines, i, i);
            selectedActualLine = selectLinesInRange(actualLines, i, i);
            assertEquals(selectedExpectedLine,
                    selectedActualLine,
                    "Non-matching strings on line " + i + lineSeparator() +
                            "EXPECTED: '" + selectedExpectedLine + "' != '" + lineSeparator() +
                            "  ACTUAL: '" + selectedActualLine + "'");
        }
    }

    /**
     * Test functional requirement / use-case, 'FR006 - Run Symbolic Regression'
     */
    @Tag("system")
    @Test
    public void fr006RunSymbolicRegression() {
        callCLI("-d UnitTestFiles/sin_a.csv -c UnitTestFiles/unit_test_config_2.config -w --maxGenerations 3 --seed 1234");

        String actualPrint = outContent.toString();

        int lastLineToCheck = 44;
        String actualLines = selectLinesInRange(actualPrint, 0, lastLineToCheck);
        String expectedLines = CLITestStrings.fr006ExpectedSymbolicRegressionPrint;

        String selectedExpectedLine;
        String selectedActualLine;
        for (int i = 0; i <= lastLineToCheck; i++) {
            selectedExpectedLine = selectLinesInRange(expectedLines, i, i);
            selectedActualLine = selectLinesInRange(actualLines, i, i);
            assertEquals(selectedExpectedLine,
                    selectedActualLine,
                    "Non-matching strings on line " + i + lineSeparator() +
                            "EXPECTED: '" + selectedExpectedLine + "' != '" + lineSeparator() +
                            "  ACTUAL: '" + selectedActualLine + "'");
        }
    }

    /**
     * Test functional requirement / use-case, 'FR007 - Indicate Run State'
     */
    @Tag("system")
    @Test
    public void fr007IndicateRunState() {
        callCLI("-d UnitTestFiles/sin_a.csv -c UnitTestFiles/unit_test_config_2.config -w --maxGenerations 3 --seed 1234");

        String actualPrint = outContent.toString();

        int firstLineToCheck = 0;
        int lastLineToCheck = 44;
        String actualLines = actualPrint;
        String expectedLines = CLITestStrings.fr006ExpectedSymbolicRegressionPrint;

        String selectedExpectedLine;
        String selectedActualLine;

        // check starting message displayed
        int checkLine;

        checkLine = 23;
        selectedExpectedLine = selectLinesInRange(expectedLines, checkLine, checkLine);
        selectedActualLine = selectLinesInRange(actualLines, checkLine, checkLine);
        assertEquals(selectedExpectedLine,
                selectedActualLine,
                "Non-matching strings on line " + checkLine + lineSeparator() +
                        "EXPECTED: '" + selectedExpectedLine + "' != '" + lineSeparator() +
                        "  ACTUAL: '" + selectedActualLine + "'");

        // check termination message displayed
        checkLine = 61;
        selectedExpectedLine = selectLinesInRange(expectedLines, checkLine, checkLine);
        selectedActualLine = selectLinesInRange(actualLines, checkLine, checkLine);
        assertEquals(selectedExpectedLine,
                selectedActualLine,
                "Non-matching strings on line " + checkLine + lineSeparator() +
                        "EXPECTED: '" + selectedExpectedLine + "' != '" + lineSeparator() +
                        "  ACTUAL: '" + selectedActualLine + "'");
    }

    /**
     * Test functional requirement / use-case, 'FR008 - Incremental Output'
     */
    @Tag("system")
    @Test
    public void fr008IncrementalOutput() {
        callCLI("-d UnitTestFiles/sin_a.csv -c UnitTestFiles/unit_test_config_2.config -w --maxGenerations 3 --seed 1234");

        String actualPrint = outContent.toString();

        int startLine = 33;
        int endLine = 59;
        String actualLines = actualPrint;
        String expectedLines = CLITestStrings.fr006ExpectedSymbolicRegressionPrint;

        String selectedExpectedLine;
        String selectedActualLine;
        for (int i = startLine; i <= endLine; i++) {
            selectedExpectedLine = selectLinesInRange(expectedLines, i, i);
            selectedActualLine = selectLinesInRange(actualLines, i, i);
            assertEquals(selectedExpectedLine,
                    selectedActualLine,
                    "Non-matching strings on line " + i + lineSeparator() +
                            "EXPECTED: '" + selectedExpectedLine + "' != '" + lineSeparator() +
                            "  ACTUAL: '" + selectedActualLine + "'");
        }
    }

    /**
     * Test functional requirement / use-case, 'FR009 - Pause Search'
     */
    @Tag("system")
    @Disabled
    @Test
    public void fr009PauseSearch() {
        // can't test with JUnit at keypresses can't be simulated any way that I know
    }

    /**
     * Test functional requirement / use-case, 'FR010 - Modify Parameters for Paused Search'
     */
    @Tag("system")
    @Disabled
    @Test
    public void fr010ModifyParametersForPausedSearch() {
        // can't test with JUnit at keypresses can't be simulated any way that I know
    }

    /**
     * Test functional requirement / use-case, 'FR011 - Resume Search'
     */
    @Tag("system")
    @Disabled
    @Test
    public void fr011ResumeSearch() {
        // can't test with JUnit at keypresses can't be simulated any way that I know
    }

    /**
     * Test functional requirement / use-case, 'FR012 - Manually Terminal Search'
     */
    @Tag("system")
    @Disabled
    @Test
    public void fr012ManuallyTerminateSearch() {
        // can't test with JUnit at keypresses can't be simulated any way that I know
    }

    /**
     * Test functional requirement / use-case, 'FR0013 - Finalised Output'
     */
    @Tag("system")
    @Test
    public void fr013FinalisedOutput() {
        callCLI("-d UnitTestFiles/sin_a.csv -c UnitTestFiles/unit_test_config_2.config -w --maxGenerations 3 --seed 1234");

        String actualPrint = outContent.toString();

        int startLine = 63;
        int endLine = 69;
        String actualLines = actualPrint;
        String expectedLines = CLITestStrings.fr006ExpectedSymbolicRegressionPrint;

        String selectedExpectedLine;
        String selectedActualLine;
        for (int i = startLine; i <= endLine; i++) {
            selectedExpectedLine = selectLinesInRange(expectedLines, i, i);
            selectedActualLine = selectLinesInRange(actualLines, i, i);
            assertEquals(selectedExpectedLine,
                    selectedActualLine,
                    "Non-matching strings on line " + i + lineSeparator() +
                            "EXPECTED: '" + selectedExpectedLine + "' != '" + lineSeparator() +
                            "  ACTUAL: '" + selectedActualLine + "'");
        }
    }

    /**
     * Test functional requirement / use-case, 'FR014 - Export Data'
     */
    @Tag("system")
    @Test
    public void fr014ExportData() throws IOException {
        String actualFilePath = "UnitTestFiles/unit_test_predictions_file_actual.csv";
        String expectedFilePath = "UnitTestFiles/unit_test_predictions_file_expected.csv";

        callCLI("-d UnitTestFiles/sin_a.csv -c UnitTestFiles/unit_test_config_2.config -w " +
                " --maxGenerations 3 --seed 1234 " +
                "--predictionsFilePath " + actualFilePath);


        try {
            BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFilePath));
            BufferedReader actualReader = new BufferedReader(new FileReader(actualFilePath));

            String expectedLine, actualLine;
            int lineCounter = 0;
            while (((expectedLine = expectedReader.readLine()) != null) &&
                    ((actualLine = actualReader.readLine()) != null)) {
                assertEquals(expectedLine,
                        actualLine,
                        "Non-matching strings on line " + lineCounter + lineSeparator() +
                                "EXPECTED: '" + expectedLine + "' != '" + lineSeparator() +
                                "  ACTUAL: '" + actualLine + "'");
                lineCounter += 1;
            }
        } catch (IOException x) {
            throw x;
        }
    }

    /**
     * Test functional requirement / use-case, 'FR015 - Suggest Setup'
     */
    @Tag("system")
    @Disabled
    @Test
    public void fr015SuggestSetup() {
        // not implemented in code at least, just using suggested configs, right?
    }

    /**
     * Test functional requirement / use-case, 'FR016 - Create Solution Skeleton'
     */
    @Tag("system")
    @Test
    public void fr016CreateSolutionSkeleton() {
        String commandString =
                "--configFilePath UnitTestFiles/unit_test_config_2.config " + " " +
                        "" + " " +
                        "--predictionsFilePath UnitTestFiles/unit_test_prediction_file.csv" + " " +
                        "-d UnitTestFiles/sin_a.csv" + " " +
                        "--errorFunction MSE" + " " +
                        "--populationSize 10" + " " +
                        "--operators POW" + " " +
                        "--skeleton ?*?/2^?" + " " +
                        "--targetColumnIndex 0" + " " +
                        "--inputColumnIndices 1" + " " +
                        "--seed 54321" + " " +
                        "-m" + " " +
                        "--includeIntegers 1,2" + " " +
                        "--includeDecimals 3,4" + " " +
                        "--includeDecimalPlaces 4" + " " +
                        "--offspringFraction 0.123" + " " +
                        "--offspringSampleSize 4" + " " +
                        "--survivorsSampleSize 6" + " " +
                        "--maxGenerations 5" + " " +
                        "--steadyFitnessLimit 5678" + " " +
                        "--timeLimit 3321" + " " +
                        "";


        callCLI(commandString);

        String actualPrint = outContent.toString();

        int lineToCheck = 8;
        String actualLine = selectLinesInRange(actualPrint, 8, 8);
        String expectedLine = "    skeleton: ?*?/2^?";

        String selectedExpectedLine;
        String selectedActualLine;

        assertEquals(expectedLine,
                actualLine,
                "Non-matching strings on line " + lineToCheck + lineSeparator() +
                        "EXPECTED: '" + expectedLine + "' != '" + lineSeparator() +
                        "  ACTUAL: '" + actualLine + "'");
    }

    /**
     * Test functional requirement / use-case, 'FR0'
     */
    @Tag("system")
    @Disabled
    @Test
    public void fr0xx() {
    }

    // utility methods
    public static String selectLinesInRange(String str, int firstLine, int lastLine) {
        lastLine += 1;

        String[] linesArray = str.split(lineSeparator());

        linesArray = Arrays.copyOfRange(linesArray, firstLine, lastLine);

        String selectedLines = String.join(lineSeparator(), linesArray);

        return selectedLines;
    }
}

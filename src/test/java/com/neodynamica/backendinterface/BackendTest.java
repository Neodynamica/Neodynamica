package com.neodynamica.backendinterface;

import com.neodynamica.lib.gp.RunState;
import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;
import com.neodynamica.lib.parameter.io.ParserUtils;
import com.neodynamica.lib.parameter.io.SearchParameterParser;
import org.junit.jupiter.api.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BackendTest {

    private Backend backend;
    private final String TEST_CSV = "UnitTestFiles/3cxc+sin(b)-4xa.csv";
    private final String TEST_CONFIG = "UnitTestFiles/backendUnitTest.config";
    private final String BLANK_CONFIG = "UnitTestFiles/blank.config";

    private final SearchParameter DEAFULT_PARAMS = getParams(BLANK_CONFIG);
    private final SearchParameter TEST_PARAMS = getParams(TEST_CONFIG);

    @BeforeEach
    void setUp() {
        try {
            backend = new Backend();
        } catch (Exception irrelevant) {
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    @AfterEach
    void tearDown() {
        //stop symbolic regression if it was started in a test, before beginning the next test
        try {
            backend.stop();
        } catch (Exception irrelevant) {
        }
    }

    /**
     * returns a SearchParameter object from a config file
     */
    SearchParameter getParams(String configFile) {
        try {
            //obtain default parameters
            SearchParameterParser parser = new SearchParameterParser(configFile);
            parser.parse();
            return parser.getSearchParameterObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * meta-test: not related to functionality of Backend, but makes sure the default params loaded
     * correctly, as this will throw off many other tests if it didn't work (not Backend's
     * responsibility though)
     */
    @Test
    @Tag("meta")
    void defaultParamsLoaded() {
        assertNotEquals(null, TEST_PARAMS,
                "Test params couldn't be loaded, tests may not work as intended");
        assertNotEquals(null, DEAFULT_PARAMS,
                "Default params couldn't be loaded, tests may not work as intended");
    }

    @Test
    @Tag("RunState")
    void testRunStateInitiallyPaused() {
        //initially
        assertEquals(RunState.PAUSED, backend.getRunState(),
                "Runstate should be 'PAUSED' before start() is called");
    }

    @Test
    @Tag("RunState")
    void testCannotStartWithoutDataset() {
        //run without setting anything - should throw an exception, as no data file has been provided to search on
        Throwable exception = assertThrows(SearchParameterException.class, () -> {
            backend.start();
        });
        assertEquals("Data file path is not set.", exception.getMessage(),
                "Shouldn't be able to call 'start()' without a dataFile having been set somewhere");
    }

            @Test
    @Tag("RunState")
    void testStartUpdatesRunState() {
        try {
            //run with data file - should not throw an exception
            backend.setDataFilePath(TEST_CSV);
            assertDoesNotThrow(() -> backend.start(),
                    "Should be able to start as long as there's a dataFilePath set.");

            //check run state has changed accordingly
            assertEquals(RunState.RUNNING, backend.getRunState(),
                    "Runstate should be 'RUNNING' after start()");
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    @Test
    @Tag("RunState")
    void testPause() {
        try {
            //test pause works before starting
            assertThrows(InvalidRunStateException.class, () -> backend.pause(),
                    "Shouldn't be able to pause before symbolicRegression has been created");

            //test pause works after starting
            backend.setDataFilePath(TEST_CSV);
            backend.start();
            assertDoesNotThrow(() -> backend.pause(),
                    "Should be able to pause as long as symbolic regression hasn't ended");
            assertEquals(RunState.PAUSED, backend.getRunState(),
                    "Runstate should be 'PAUSED' after pause()");

            //test pause works after resuming
            backend.resume();
            assertDoesNotThrow(() -> backend.pause(),
                    "Should be able to pause as long as symbolic regression hasn't ended");

            //test pause does not work after ending
            backend.stop();
            assertThrows(InvalidRunStateException.class, () -> backend.pause(),
                    "Shouldn't be able to pause after regression has ended");
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    @Test
    @Tag("RunState")
    void testResume() {
        try {
            //test pause doesn't work before starting
            assertThrows(InvalidRunStateException.class, () -> backend.resume(),
                    "Shouldn't be able to resume before symbolicRegression has been created");

            //start regression
            backend.setDataFilePath(TEST_CSV);
            backend.start();
            backend.pause();

            //test resume is allowed and sets runState accordingly
            assertDoesNotThrow(() -> backend.resume(),
                    "Should be able to resume as long as symbolic regression hasn't ended");
            assertEquals(RunState.RUNNING, backend.getRunState(),
                    "Runstate should be 'RUNNING' after resume()");

            //test resume does not work after ending
            backend.stop();
            assertThrows(InvalidRunStateException.class, () -> backend.pause(),
                    "Shouldn't be able to pause after regression has ended");
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }


    @Test
    @Tag("RunState")
    void testStop() {
        //test stop works before starting
        assertThrows(InvalidRunStateException.class, () -> backend.stop(),
                "Shouldn't be able to stop before symbolicRegression has been created");

        //test that stop works on a running regression
        assertDoesNotThrow(() -> {
            backend.setDataFilePath(TEST_CSV);
            backend.start();
            backend.stop();
        });
    }

    /**
     * Test that parameter changes are allowed/not allowed at the appropriate points during the
     * Backend's lifecycle - allowed before starting - not allowed while running - allowed when
     * paused mid-run - not allowed after search has ended
     */
    @Test
    void testCheckRunStateAllowsParameterChanges() {
        //test that parameter changes are legal before starting
        assertDoesNotThrow(() -> backend.checkRunStateAllowsParameterChanges(),
                "Parameters should be able to be set before calling start()");
        try {
            //test that parameter changes are not legal while running
            backend.setDataFilePath(TEST_CSV);
            backend.start();
            assertThrows(InvalidRunStateException.class,
                    () -> backend.checkRunStateAllowsParameterChanges(),
                    "Parameters should not be allowed to be set while the search is running.");

            //test that parameter changes are legal while paused mid-search
            backend.pause();
            assertDoesNotThrow(() -> backend.checkRunStateAllowsParameterChanges(),
                    "Parameters should be able to be set while paused mid search.");

            //test that parameter changes are not legal after search has ended
            backend.stop();
            assertThrows(InvalidRunStateException.class,
                    () -> backend.checkRunStateAllowsParameterChanges(),
                    "Parameters should not be allowed to be set after the search has ended.");
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * Test that add/remove propertyChangeListener methods work as expected
     */
    @Test
    void testPropertyChangeListener() {
        //make quick class to use as a listener for a Backend and count how many events it hears
        class TestListener implements PropertyChangeListener {

            public Backend myBackend;
            public int eventsHeard = 0;

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                eventsHeard++;
            }
        }

        try {
            //create an instance and set it to listen to its backend instance
            TestListener testListener = new TestListener();
            testListener.myBackend = new Backend();
            testListener.myBackend.addPropertyChangeListener(testListener);

            //run the backend for a bit, and see if it heard any events (triggered each generation)
            int delay = 500;//time to run before checking if events were heard
            //intialise backend
            testListener.myBackend.setDataFilePath(TEST_CSV);
            testListener.myBackend.setConfigFilePath(TEST_CONFIG);
            testListener.myBackend.setMaxGenerations(
                    10000);//make sure it doesn't end before the test tries to pause, which would cause issues
            //run for delay milliseconds, then pause to check
            testListener.myBackend.start();
            TimeUnit.MILLISECONDS.sleep(delay);
            testListener.myBackend.pause();
            //check that the 'events heard' counter has gone up - 500ms should be more than enough time for generation
            int eventsHeardStage1 = testListener.eventsHeard;
            assertNotEquals(0, eventsHeardStage1,
                    "PropertyChangeListener didn't hear any events after " + delay
                            + "ms of running");

            //now remove the listener, and check that the 'heard' counter doesn't change
            // when running for the same amount of time
            testListener.myBackend.removePropertyChangeListener(testListener);
            testListener.myBackend.resume();
            TimeUnit.MILLISECONDS.sleep(delay);

            //the +1 is for (my guess) the generation which was in progress when paused, which sometimes
            // still sends 1 last event even after removed for whatever (unimportant) reason
            assertEquals(eventsHeardStage1 == testListener.eventsHeard
                            || eventsHeardStage1 + 1 == testListener.eventsHeard, true,
                    "Listener is supposed to have been removed, but events are still being heard.");

        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }

    }

    /**
     * INTEGRATION TEST
     * Tests each of the three generation getters after the search has run for 2 generations. -
     * getNextGeneration - returns new generationBean each call - returns null if there's no more
     * new generations - getGeneration(index) - returns the generation it says it should (zero based
     * index) - getLatestGeneration - returns the last generation
     */
    @Test
    void testGenerationGetters() {
        //make quick class to use as a listener for a Backend and use its getters after 2 generations
        class TestListener implements PropertyChangeListener {

            public Backend myBackend;
            public int generations = 0;

            public void runTests() {
                try {
                    //create a backend instance listen to it
                    myBackend = new Backend();
                    myBackend.addPropertyChangeListener(this);
                    myBackend.setConfigFilePath(TEST_CONFIG);

                    //first, test that all getters return null if there's no generations/symbolic regression hasn't started
                    assertAll(
                            "Generation getters should all return null if search hasn't been started yet.",
                            () -> assertThrows(NullPointerException.class,
                                    () -> myBackend.getLatestGeneration()),
                            () -> assertThrows(NullPointerException.class,
                                    () -> myBackend.getNextGeneration()),
                            () -> assertThrows(NullPointerException.class,
                                    () -> myBackend.getGeneration(0))
                    );

                    //start the backend. The testListener will automatically do the tests after 2 generations
                    myBackend.start();
                    //check that getLatestGeneration returns null if there's no generations yet
                    assertEquals(null, myBackend.getLatestGeneration());
                } catch (Exception irrelevant) {
                    fail("An unrelated error from somewhere higher up is preventing this test from working",
                            irrelevant);
                    //do nothing - these exceptions should be tested for where they occur - not here
                }
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                generations++;
                if (generations == 2) {
                    try {
                        myBackend.stop();
                        doTestsAfter2Generations();
                    } catch (Exception irrelevant) {
                        fail("An unrelated error from somewhere higher up is preventing this test from working",
                                irrelevant);
                    }
                }
            }

            public void doTestsAfter2Generations() {
                //check that getNextGeneration returns a non-null, distinct (i.e. the next) generation on a subsequent call
                GenerationBean firstGeneration = myBackend.getNextGeneration();
                assertNotEquals(null, firstGeneration,
                        "getNextGeneration is returning null even after 2 generations have been completed.");
                assertNotEquals(firstGeneration, myBackend.getNextGeneration(),
                        "getNextGeneration is returning the same generation each call rather than the next.");

                //we've called getNextGeneration twice now, so there should be no new generations
                // for the method to return, resulting in a 'null' return instead
                assertEquals(null, myBackend.getNextGeneration());

                //test that getGeneration(index) is returning the correct generation
                assertEquals(firstGeneration, myBackend.getGeneration(0));

                //test that getLatestGeneration works, returning a non-null generation which == the 2nd one
                assertAll("getLatestGeneration is not returning the latest generation.",
                        () -> assertNotEquals(null, myBackend.getLatestGeneration()),
                        () -> assertEquals(myBackend.getGeneration(1),
                                myBackend.getLatestGeneration())
                );
            }
        }

        TestListener testListener = new TestListener();
        testListener.runTests();

    }

//
//    @Test
//    void addPropertyChangeListener() {
//    }
//
//    @Test
//    void removePropertyChangeListener() {
//    }
//
//
// Just a simple getter, doesn't need testing
//    @Test
//    void getRunState() {}

    /**
     * For parameter 'maxGenerations', tests - the default value from default.config is set if
     * nothing else given - a value from a given configFile will overwrite the default value if
     * given - a value given by Backend's corresponding 'setter' method overwrites that again
     */
    @Test
    @Tag("Parameter")
    void testMaxGenerations() {
        try {
            //check that default value is set correctly
            backend.setDataFilePath(TEST_CSV);
            assertEquals(DEAFULT_PARAMS.getMaxGenerations(), backend.getMaxGenerations());

            //check that a config file will overwrite this value correctly
            backend.setConfigFilePath(TEST_CONFIG);
            backend.start();
            backend.pause();
            assertEquals(TEST_PARAMS.getMaxGenerations(), backend.getMaxGenerations());

            //check that manual parameter override via setter will overwrite this again
            backend.setMaxGenerations(12);
            backend.resume();//combined parameters update only upon resume
            backend.pause();

            assertEquals(12, backend.getMaxGenerations());
        } catch (SearchParameterException e) {
            fail(
                    "Rules for valid parameters must have changed to make one or more of the test values invalid",
                    e);
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * For parameter 'populationSize', tests - the default value from default.config is set if
     * nothing else given - a value from a given configFile will overwrite the default value if
     * given - a value given by Backend's corresponding 'setter' method overwrites that again
     */
    @Test
    @Tag("Parameter")
    void testPopulationSize() {
        try {
            //check that default value is set correctly
            backend.setDataFilePath(TEST_CSV);
            assertEquals(DEAFULT_PARAMS.getPopulationSize(), backend.getPopulationSize());

            //check that a config file will overwrite this value correctly
            backend.setConfigFilePath(TEST_CONFIG);
            backend.start();
            backend.pause();
            assertEquals(TEST_PARAMS.getPopulationSize(), backend.getPopulationSize());

            //check that manual parameter override via setter will overwrite this again
            backend.setPopulationSize(12);
            backend.resume();//combined parameters update only upon resume
            backend.pause();

            assertEquals(12, backend.getPopulationSize());
        } catch (SearchParameterException e) {
            fail(
                    "Rules for valid parameters must have changed to make one or more of the test values invalid",
                    e);
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * For parameter 'populationSize', tests - the default value from default.config is set if
     * nothing else given - a value from a given configFile will overwrite the default value if
     * given - a value given by Backend's corresponding 'setter' method overwrites that again
     */
    @Test
    @Tag("Parameter")
    void testMaxSolutionNodes() {
        try {
            //check that default value is set correctly
            backend.setDataFilePath(TEST_CSV);
            assertEquals(DEAFULT_PARAMS.getMaxSolutionNodes(), backend.getMaxSolutionNodes());

            //check that a config file will overwrite this value correctly
            backend.setConfigFilePath(TEST_CONFIG);
            backend.start();
            backend.pause();
            assertEquals(TEST_PARAMS.getMaxSolutionNodes(), backend.getMaxSolutionNodes());

            //check that manual parameter override via setter will overwrite this again
            backend.setMaxSolutionNodes(12);
            backend.resume();//combined parameters update only upon resume
            backend.pause();

            assertEquals(12, backend.getMaxSolutionNodes());
        } catch (SearchParameterException e) {
            fail(
                    "Rules for valid parameters must have changed to make one or more of the test values invalid",
                    e);
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * Want to make skeleton have valid default value before this will work the same as every other
     * test. Will write and enable once this is done.
     */
    @Test
    @Disabled
    void testSkeleton() {
    }

    /**
     * For parameter 'operators', tests - the default value from default.config is set if nothing
     * else given - a value from a given configFile will overwrite the default value if given - a
     * value given by Backend's corresponding 'setter' method overwrites that again
     */
    @Test
    @Tag("Parameter")
    void testOperators() {
        try {
            //check that default value is set correctly
            backend.setDataFilePath(TEST_CSV);
            assertEquals(DEAFULT_PARAMS.getOperators(), backend.getOperators());

            //check that a config file will overwrite this value correctly
            backend.setConfigFilePath(TEST_CONFIG);
            backend.start();
            backend.pause();
            assertEquals(TEST_PARAMS.getOperators(), backend.getOperators());

            //check that manual parameter override via setter will overwrite this again
            backend.setOperators("ADD,SUB");
            backend.resume();//combined parameters update only upon resume
            backend.pause();

            assertEquals("ADD,SUB", backend.getOperators());
        } catch (SearchParameterException e) {
            fail(
                    "Rules for valid parameters must have changed to make one or more of the test values invalid",
                    e);
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * For parameter 'errorFunction', tests - the default value from default.config is set if
     * nothing else given - a value from a given configFile will overwrite the default value if
     * given - a value given by Backend's corresponding 'setter' method overwrites that again
     */
    @Test
    @Tag("Parameter")
    void testErrorFunction() {
        try {
            //check that default value is set correctly
            backend.setDataFilePath(TEST_CSV);
            assertEquals(DEAFULT_PARAMS.getErrorFunction(), backend.getErrorFunction());

            //check that a config file will overwrite this value correctly
            backend.setConfigFilePath(TEST_CONFIG);
            backend.start();
            backend.pause();
            assertEquals(TEST_PARAMS.getErrorFunction(), backend.getErrorFunction());

            //check that manual parameter override via setter will overwrite this again
            backend.setErrorFunction("IQAE");
            backend.resume();//combined parameters update only upon resume
            backend.pause();

            assertEquals("IQAE", backend.getErrorFunction());
        } catch (SearchParameterException e) {
            fail(
                    "Rules for valid parameters must have changed to make one or more of the test values invalid",
                    e);
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * For parameter 'initialSolutionDepth', tests - the default value from default.config is set if
     * nothing else given - a value from a given configFile will overwrite the default value if
     * given - a value given by Backend's corresponding 'setter' method overwrites that again
     */
    @Test
    @Tag("Parameter")
    void testInitialSolutionDepth() {
        try {
            //check that default value is set correctly
            backend.setDataFilePath(TEST_CSV);
            assertEquals(DEAFULT_PARAMS.getInitialSolutionDepth(),
                    backend.getInitialSolutionDepth());

            //check that a config file will overwrite this value correctly
            backend.setConfigFilePath(TEST_CONFIG);
            backend.start();
            backend.pause();
            assertEquals(TEST_PARAMS.getInitialSolutionDepth(), backend.getInitialSolutionDepth());

            //check that manual parameter override via setter will overwrite this again
            backend.setInitialSolutionDepth(3);
            backend.resume();//combined parameters update only upon resume
            backend.pause();

            assertEquals(3, backend.getInitialSolutionDepth());
        } catch (SearchParameterException e) {
            fail(
                    "Rules for valid parameters must have changed to make one or more of the test values invalid",
                    e);
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * Getter/setter test for parameter 'targetColumnIndex'. Tests: - the default value from
     * default.config is set if nothing else given - a value from a given configFile will overwrite
     * the default value if given - a value given by Backend's corresponding 'setter' method
     * overwrites that again
     */
    @Test
    @Tag("Parameter")
    void testTargetColumnIndex() {
        try {
            //check that default value is set correctly
            backend.setDataFilePath(
                    TEST_CSV); //will update/set combinedParameters to overwrite || config
            assertEquals(DEAFULT_PARAMS.getTargetColumnIndex(), backend.getTargetColumnIndex());

            //check that a config file will overwrite this value correctly
            //modifying targetColIndex mid-run may not be legal, so we need to test on a new instance
            backend = new Backend();

            backend.setConfigFilePath(TEST_CONFIG);
            backend.setDataFilePath(
                    TEST_CSV); //will update/set combinedParameters to overwrite || config

            assertEquals(TEST_PARAMS.getTargetColumnIndex(), backend.getTargetColumnIndex());

            //check that manual parameter override via setter will overwrite this again
            backend = new Backend();
            backend.setConfigFilePath(TEST_CONFIG);//overwrite default with config file
            backend.setTargetColumnIndex(3);//overwrite config file with setter value
            backend.setDataFilePath(
                    TEST_CSV); //will update/set combinedParameters to overwrite || config

            assertEquals(3, backend.getTargetColumnIndex());
        } catch (SearchParameterException e) {
            fail(
                    "Rules for valid parameters must have changed to make one or more of the test values invalid",
                    e);
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * Getter/setter test for parameter 'inputColumnIndices'. Tests: - the default value from
     * default.config is set if nothing else given - a value from a given configFile will overwrite
     * the default value if given - a value given by Backend's corresponding 'setter' method
     * overwrites that again
     */
    @Test
    @Tag("Parameter")
    void testInputColumnIndices() {
        try {
            //check that default value is set correctly
            backend.setDataFilePath(TEST_CSV);
            assertEquals(DEAFULT_PARAMS.getInputColumnIndices(), backend.getInputColumnIndices());

            //check that a config file will overwrite this value correctly
            backend.setConfigFilePath(TEST_CONFIG);
            backend.start();
            backend.pause();
            assertEquals(TEST_PARAMS.getInputColumnIndices(), backend.getInputColumnIndices());

            //check that manual parameter override via setter will overwrite this again
            backend.setInputColumnIndices("0");
            backend.resume();//combined parameters update only upon resume
            backend.pause();

            assertEquals("0", backend.getInputColumnIndices());
        } catch (SearchParameterException e) {
            fail(
                    "Rules for valid parameters must have changed to make one or more of the test values invalid",
                    e);
        } catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /**
     * Test that dataset is being set correctly by any means backend should do so: - via
     * setConfigFilePath - via setDataFilePath - check that there's no dataset initially The actual
     * contents of the dataset are not the concern of this test - this should be handled in
     * 'TestDataset'.
     */
    @Test
    @Tag("Parameter")
    void testDataset() {
        //dataset should not be set initially, throwing a SearchParameterException
        assertThrows(SearchParameterException.class, () -> backend.getDatasetColumnLabels(),
                "Should return null if dataset hasn't been set yet");

        //check that dataset is loaded from config file via setConfigFile
        assertDoesNotThrow(() -> {
            backend.setConfigFilePath(TEST_CONFIG);
            backend.getDatasetColumnLabels();
        });

        //check that dataset is loaded from setDataFilePath
        assertDoesNotThrow(() -> {
            backend = new Backend();
            backend.setDataFilePath(TEST_CSV);
            backend.getDatasetColumnLabels();
        });
    }

    @Test
    public void testCurrentDuration(){
        try {
            //test that duration is initially zero
            assertEquals(0, backend.getCurrentDuration(),
                    "Duration should start at zero");

            //start regression
            backend.setConfigFilePath(TEST_CONFIG);
            backend.setMaxGenerations(2000);//bump this up to make sure it doesn't end prematurely
            backend.start();

            //run for 500 millis, then pause
            Thread.sleep(500);
            backend.pause();
            long durationAfterPause = backend.getCurrentDuration();

            //check that duration timer was paused when backend.pause() was called,
            //wait another 500 millis to allow it to erroneously keep counting if it is going to
            Thread.sleep(500);

            assertEquals(durationAfterPause, backend.getCurrentDuration(),
                    "Duration should not increase while paused.");

            backend.resume();
            //resume for 500 millis, then end
            Thread.sleep(500);
            backend.stop();
            //check that duration has changed since we resumed
            long durationAfterStop = backend.getCurrentDuration();
            assertNotEquals(durationAfterPause, durationAfterStop, "Duration should have increased after resuming.");

            //check that duration timer was paused when backend.stop() was called
            //wait another 500 millis to allow it to erroneously keep counting if it is going to
            Thread.sleep(500);

            assertEquals(durationAfterStop, backend.getCurrentDuration(),
                    "Duration should not have changed after stopping.");

        }
        catch (InterruptedException e) {
            fail(e.getMessage());
        }
        catch (Exception irrelevant) {
            fail("An unrelated error from somewhere higher up is preventing this test from working",
                    irrelevant);
            //do nothing - these exceptions should be tested for where they occur - not here
        }
    }

    /* These methods are responsibility of Dataset tests
     * void getDatasetColumnLabels() {}
     * void getInputColumnLabels() {}
     * void getTargetColumnLabel() {}
     * void getDatasetValues() {}
     * */

    /* This stuff will be responsibility of GenerationBean to test
     * void getBestSolutionParenthesisString() {}
     * void getBestSolutionTreeString() {}
     */

//    Already covered by all individual parameter methods
//    void setConfigFilePath() {}
}
package com.neodynamica.userinterface.cli;

import static java.lang.System.lineSeparator;

public class CLITestStrings {
    public static String fr001ExpectedDatasetPrint =
            "DATASET" + lineSeparator() +
                    "" + lineSeparator() +
                    "  a                |  b                |  c                |  output         " + lineSeparator() +
                    "-------------------+-------------------+-------------------+-----------------" + lineSeparator() +
                    "  0.787311366      |  7.600414244      |  68.61670675      |  0.708458325    " + lineSeparator() +
                    "  0.844950804      |  2.174804538      |  53.55243348      |  0.747938458    " + lineSeparator() +
                    "  0.455513667      |  9.589623922      |  92.84844416      |  0.439923663    " + lineSeparator() +
                    "  0.432618311      |  8.91438114       |  79.06885595      |  0.419249326    " + lineSeparator() +
                    "  0.758395743      |  6.894180357      |  96.07839033      |  0.687757736    " + lineSeparator() +
                    "  0.650013911      |  2.226507504      |  36.55902568      |  0.60519748     " + lineSeparator() +
                    "  0.688828813      |  7.633076809      |  77.2962628       |  0.635633473    " + lineSeparator() +
                    "  0.702727252      |  6.081616801      |  48.43306741      |  0.646301206    " + lineSeparator() +
                    "  0.488981592      |  2.426169317      |  36.72940255      |  0.469727069    " + lineSeparator() +
                    "  0.505246907      |  6.527627889      |  40.90525668      |  0.484023512    " + lineSeparator() +
                    "  0.940677398      |  5.883191557      |  32.60383006      |  0.807957436    " + lineSeparator() +
                    "  5.11837E-4       |  6.649593671      |  12.45167334      |  5.11837E-4     " + lineSeparator() +
                    "  0.123573883      |  4.155071402      |  30.10896257      |  0.123259617    " + lineSeparator() +
                    "  0.231246498      |  3.275814898      |  63.99902017      |  0.229191019    " + lineSeparator() +
                    "  0.210529761      |  5.218055711      |  89.04650431      |  0.208977994    " + lineSeparator() +
                    "  0.601054531      |  1.232929535      |  32.95153813      |  0.565512502    " + lineSeparator() +
                    "  0.475222549      |  6.990782356      |  85.16826707      |  0.457536347    " + lineSeparator() +
                    "  0.224693447      |  7.552900652      |  7.123622296      |  0.222807525    " + lineSeparator() +
                    "  0.086810964      |  9.828330573      |  33.95320041      |  0.086701968    " + lineSeparator() +
                    "  0.904866532      |  0.156104367      |  65.16795092      |  0.786342707    " + lineSeparator() +
                    "  0.063716364      |  5.895802635      |  17.74055977      |  0.06367326     " + lineSeparator() +
                    "  0.134517832      |  1.000233502      |  74.54449211      |  0.134112515    " + lineSeparator() +
                    "  0.568194106      |  7.20645141       |  42.50618533      |  0.538110785    " + lineSeparator() +
                    "  0.093587147      |  1.275904547      |  51.76247944      |  0.093450592    " + lineSeparator() +
                    "  0.11422256       |  5.541335039      |  49.58022041      |  0.113974349    " + lineSeparator() +
                    "  0.866880281      |  7.030509013      |  34.25153299      |  0.762313543    " + lineSeparator() +
                    "  0.192539155      |  7.521030877      |  79.31953168      |  0.191351744    " + lineSeparator() +
                    "  0.217532642      |  1.837560701      |  77.98173202      |  0.215821073    " + lineSeparator() +
                    "  0.853458983      |  5.346076247      |  97.72120239      |  0.753558777    " + lineSeparator() +
                    "  0.473184292      |  2.941234794      |  25.83497921      |  0.455722998    " +
                    "";

    public static String fr003ExpectedParametersPrint =
            "SEARCH PARAMETERS" + lineSeparator() +
                    "    configFilePath: UnitTestFiles/unit_test_config_2.config" + lineSeparator() +
                    "    notVerbose: false" + lineSeparator() +
                    "    predictionsFilePath: UnitTestFiles/unit_test_prediction_file.csv" + lineSeparator() +
                    "    dataFilePath: UnitTestFiles/sin_a.csv" + lineSeparator() +
                    "    errorFunction: MSE" + lineSeparator() +
                    "    populationSize: 10" + lineSeparator() +
                    "    operators: POW" + lineSeparator() +
                    "    skeleton: ?*?" + lineSeparator() +
                    "    targetColumnIndex: 0" + lineSeparator() +
                    "    inputColumnIndices: 1" + lineSeparator() +
                    "    seed: 54321" + lineSeparator() +
                    "    multiObjectiveOptimisation: true" + lineSeparator() +
                    "    includeIntegers: 1,2" + lineSeparator() +
                    "    includeDecimals: 3,4" + lineSeparator() +
                    "    includeDecimalPlaces: 4" + lineSeparator() +
                    "    offspringFraction: 0.123" + lineSeparator() +
                    "    offspringSampleSize: 4" + lineSeparator() +
                    "    survivorsSampleSize: 6" + lineSeparator() +
                    "    maxGenerations: 5" + lineSeparator() +
                    "    steadyFitnessLimit: 5678" + lineSeparator() +
                    "    timeLimit: 3321" + lineSeparator() +
                    lineSeparator() +
                    "STARTING SYMBOLIC REGRESSION >>>";

    public static String fr005ExpectedParametersPrint =
            "SEARCH PARAMETERS" + lineSeparator() +
                    "    configFilePath: UnitTestFiles/unit_test_config_1.config" + lineSeparator() +
                    "    notVerbose: false" + lineSeparator() +
                    "    predictionsFilePath: NOT SET" + lineSeparator() +
                    "    dataFilePath: datasets/sin_a.csv" + lineSeparator() +
                    "    errorFunction: MSE" + lineSeparator() +
                    "    populationSize: 20" + lineSeparator() +
                    "    operators: SQRT" + lineSeparator() +
                    "    skeleton: ?+?" + lineSeparator() +
                    "    targetColumnIndex: 0" + lineSeparator() +
                    "    inputColumnIndices: 1" + lineSeparator() +
                    "    seed: 1234" + lineSeparator() +
                    "    multiObjectiveOptimisation: true" + lineSeparator() +
                    "    includeIntegers: 1,11" + lineSeparator() +
                    "    includeDecimals: 2,12" + lineSeparator() +
                    "    includeDecimalPlaces: 4" + lineSeparator() +
                    "    offspringFraction: 0.2" + lineSeparator() +
                    "    offspringSampleSize: 2" + lineSeparator() +
                    "    survivorsSampleSize: 4" + lineSeparator() +
                    "    maxGenerations: 1" + lineSeparator() +
                    "    steadyFitnessLimit: 99998" + lineSeparator() +
                    "    timeLimit: 3601" + lineSeparator() +
                    lineSeparator() +
                    "STARTING SYMBOLIC REGRESSION >>>";





    public static String fr006ExpectedSymbolicRegressionPrint =
            "SEARCH PARAMETERS" + lineSeparator() +
                    "    configFilePath: UnitTestFiles/unit_test_config_2.config" + lineSeparator() +
                    "    notVerbose: false" + lineSeparator() +
                    "    predictionsFilePath: NOT SET" + lineSeparator() +
                    "    dataFilePath: UnitTestFiles/sin_a.csv" + lineSeparator() +
                    "    errorFunction: RMSE" + lineSeparator() +
                    "    populationSize: 200" + lineSeparator() +
                    "    operators: ADD,SUB,MUL,DIV,SIN" + lineSeparator() +
                    "    skeleton: NOT SET" + lineSeparator() +
                    "    targetColumnIndex: -1" + lineSeparator() +
                    "    inputColumnIndices: ALL" + lineSeparator() +
                    "    seed: 1234" + lineSeparator() +
                    "    multiObjectiveOptimisation: false" + lineSeparator() +
                    "    includeIntegers: 0,10" + lineSeparator() +
                    "    includeDecimals: 0,10" + lineSeparator() +
                    "    includeDecimalPlaces: 2" + lineSeparator() +
                    "    offspringFraction: 0.6" + lineSeparator() +
                    "    offspringSampleSize: 3" + lineSeparator() +
                    "    survivorsSampleSize: 3" + lineSeparator() +
                    "    maxGenerations: 3" + lineSeparator() +
                    "    steadyFitnessLimit: 99999" + lineSeparator() +
                    "    timeLimit: 3600" + lineSeparator() +
                    "" + lineSeparator() +
                    "STARTING SYMBOLIC REGRESSION >>>" + lineSeparator() +
                    "" + lineSeparator() +
                    "" + lineSeparator() +
                    "" + lineSeparator() +
                    "" + lineSeparator() +
                    "" + lineSeparator() +
                    "" + lineSeparator() +
                    "" + lineSeparator() +
                    "" + lineSeparator() +
                    "" + lineSeparator() +
                    "\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K" + lineSeparator() +
                    "Generation 1" + lineSeparator() +
                    "    Error (RMSE)" + lineSeparator() +
                    "            Best: 0.037482578988358724    " + lineSeparator() +
                    "            Mean: 11.031633443507443    " + lineSeparator() +
                    "          Median: 0.6937122760472814    " + lineSeparator() +
                    "        Std Dvtn: 56.179358217243404    " + lineSeparator() +
                    "    Current Best Formula: " + lineSeparator() +
                    "        output = sin(sin(a))" + lineSeparator() +
                    "\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K" + lineSeparator() +
                    "Generation 2" + lineSeparator() +
                    "    Error (RMSE)" + lineSeparator() +
                    "            Best: 0.037482578988358724    " + lineSeparator() +
                    "            Mean: 0.8129496782951281    " + lineSeparator() +
                    "          Median: 0.4770808381432811    " + lineSeparator() +
                    "        Std Dvtn: 3.5700638340671147    " + lineSeparator() +
                    "    Current Best Formula: " + lineSeparator() +
                    "        output = sin(sin(a))" + lineSeparator() +
                    "\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K\u001B[A\u001B[2K" + lineSeparator() +
                    "Generation 3" + lineSeparator() +
                    "    Error (RMSE)" + lineSeparator() +
                    "            Best: 3.7256272977250053E-10    " + lineSeparator() +
                    "            Mean: 9.468310874983802    " + lineSeparator() +
                    "          Median: 0.2870903987452136    " + lineSeparator() +
                    "        Std Dvtn: 125.808787143549    " + lineSeparator() +
                    "    Current Best Formula: " + lineSeparator() +
                    "        output = sin(a)" + lineSeparator() +
                    "" + lineSeparator() +
                    ">>> SYMBOLIC REGRESSION ENDED" + lineSeparator() +
                    "" + lineSeparator() +
                    "    BEST FORMULA FOUND: " + lineSeparator() +
                    "        ERROR (RMSE): 3.7256272977250053E-10" + lineSeparator() +
                    "        FORMULA: " + lineSeparator() +
                    "            output = sin(a)" + lineSeparator() +
                    "        TREE:" + lineSeparator() +
                    "            sin" + lineSeparator() +
                    "            └── a" + lineSeparator() +
                    "";

}

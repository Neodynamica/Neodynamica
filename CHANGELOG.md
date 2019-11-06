# CHANGELOG

## New changes
* [new user-facing change present in this code]

## Prototype v3.0 (2019-10-09)
### New
* GUI - can be run using `nda-gui`
* CLI pause, change, and resume by pressing enter key during a symbolic regression
* CLI option to specify input columns (`--inputColumnIndices`/`-i`)
* Additional information in help function of CLI (`--help`)
* Command-line option `-c`/`--configFilePath` is no longer mandatory
* `default.config` is now no longer user-visible
* `template.config` is included now to document the config file format

### Bug fixes
* Fix CLI option `--targetColumnIndex` 

## Prototype v2.0 (2019-09-02)
### General
* Symbolic regression runs in multithreading environment
* New error functions implemented, replacing hardcoded 'Total Absolute Error' from previous prototype. - specify using either the full name or abbreviation:
    * MeanAbsoluteError (MAE)
    * MeanSquaredError (MSE)
    * RootMeanSquareError (RMSE)
    * R2GoodnessOfFit (RGF)
    * WorstCase (WC)
    * PearsonsCorrelationCoefficient (PCC)
    * MedianAbsoluteError (MED)
    * InterquartileAbsoluteError (IQAE)
    * SizeOfSolution (SOS)
* New parameters settable via `.config` file or CLI:
    * maxSolutionNodes - limit max number of operators, terminals a solution tree can have
    * targetColumnIndex - index of column in input csv which the symbolic regression will try to solve (-1 = last column)
    * errorFunction - name of error function to be used for fitness calculation
* Fixed bug which caused some parameters to be ignored if no solutionSkeleton was specified in `.config` file

### CLI
* End of run formula in CLI now shows which variable the formula is calculating
* Print parameters at start of symbolic regression
* Display help message when invalid CLI command entered
* Fixed bug causing incorrect medianFitness printout at each generation.
 
## Prototype v1.1 (2019-08-09)
* `nda` shell script now runs CLI from .jar
* Logging of additional details while running

## Prototype v1.0
* Initial minimum feature set

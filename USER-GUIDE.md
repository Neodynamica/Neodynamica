
# Running CLI

## Running CLI under bash

Make sure there is a .jar is named `neodynamica.jar` in the same folder as the shell script file `nda`.

To run the `nda` shell script, either:
* (recommended) Add the directory containing the `nda` script to your bash's `PATH` variable and run:
    1. run in bash: `export PATH="/path/to/this/directory/:$PATH"`
    2. (optional, but recommended) Add the above line to your `.bash_profile` as well (should be in your `/home/<user>` directory) so that it will run every time you log in
    3. Test with `nda --help`
    
    This option is recommended, as you can run using just `nda` from any location on your system. With step 2 it will be a permanent change. 

* Run using `source`: 
    * `source ./nda --help` 
    * Or: `. ./nda --help` (`.` is equivalent to `source`)

In both cases you may need to set the file as executable with something like: `chmod 774 nda`

## Options and configuration

You must always specify a configuration file with the option `-c example.config`

The order of precedence of all other options is:
1. Command-line options (see `nda --help`)
2. Specified configuration (`-c` option)
3. Default configuration file (hard-coded)

Note that if a config file is specified when a symbolic regression has been paused, it's values will override any options entered in the first command, unless they are entered again in that command.

## CLI Pause and resume
A symbolic regression may be paused at any point by pressing enter. A new `nda` command with different options may be specified here to change parameters where it makes sense to do so, e.g. `--maxGenerations` (an exhaustive list of which parameters may be changed will be included in a future releases). Note that currently this behaviour has not been tested for all parameters, nor are there checks in place to restrict which parameters may be changed.

# Running GUI

## Running GUI from bash

Make sure there is a .jar is named `neodynamica.jar` in the same folder as the shell script file `nda-gui`.

To run the `nda-gui` shell script, either:
* (recommended) Add the directory containing the `nda-gui` script to your bash's `PATH` variable and run:
    1. run in bash: `export PATH="/path/to/this/directory/:$PATH"`
    2. (optional, but recommended) Add the above line to your `.bash_profile` as well (should be in your `/home/<user>` directory) so that it will run every time you log in
    3. Test with `nda-gui`
    
    This option is recommended, as you can run using just `nda-gui` from any location on your system. With step 2 it will be a permanent change. 

* Run using `source`: 
    * `source ./nda-gui --help` 
    * Or: `. ./nda-gui --help` (`.` is equivalent to `source`)

In both cases you may need to set the file as executable with something like: `chmod 774 nda`

# Building the Project

Clone the repo, `cd` into the project folder and type:  
`mvn package`

The build artifact will be available in the `target` folder and will look something like this (note version may vary):  
`target/snout-1.0-SNAPSHOT-bin.zip`

# Running the app

Unzip the build artifact by double clicking on it in Finder or at the command line type:  
`unzip target/snout-1.0-SNAPSHOT-bin.zip`

You will have a decompressed folder named `target/snout-1.0-SNAPSHOT` which has the fully, self-contained application. You can copy this folder wherever you like to "install" the application on your computer.

To execute the program run:  
`./target/snout-1.0-SNAPSHOT/bin/main.sh <config-file> <tests-file>`

You can use the sample files bundled with the source code under `src/test/resources` so it would look something like this:

```
CONFIG=src/test/resources/config.json
TESTS=src/test/resources/tests.json
./target/snout-1.0-SNAPSHOT/bin/main.sh $CONFIG $TESTS
```

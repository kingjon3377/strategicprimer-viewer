import ceylon.file {
    parsePath,
    Nil
}

import java.io {
    IOException
}
import java.lang {
    IllegalArgumentException
}

import strategicprimer.viewer.drivers {
    DriverFailedException,
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    ICLIHelper,
    SPOptions,
    IncorrectUsageException
}
import strategicprimer.model.map {
    Point,
    pointFactory
}
// FIXME: Rename file to miningCLI.ceylon
"""A driver to create a spreadsheet model of a mine. Its parameters are the name of the
   file to write the CSV to and the value at the top center (as an index into the
   LodeStatus values array).""""
shared object miningCLI satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-i";
        longOption = "--mining";
        paramsWanted = ParamCount.two;
        shortDescription = "Create a model of a mine";
        longDescription = "Create a CSV spreadsheet representing a mine's area";
        firstParamDescription = "output.csv";
        subsequentParamDescription = "status";
        supportedOptionsTemp = [ "--seed=NN" ];
    };
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (exists filename = args.first, exists second = args.rest.first,
                is Integer statusIndex = Integer.parse(second), args.size == 2) {
            Integer seed;
            if (options.hasOption("--seed")) {
                value temp = Integer.parse(options.getArgument("--seed"));
                if (is Integer temp) {
                    seed = temp;
                } else {
                    throw DriverFailedException(temp, "Seed must be numeric");
                }
            } else {
                seed = system.milliseconds;
            }
            Integer actualIndex;
            MineKind mineKind;
            if (statusIndex >= 0) {
                actualIndex = statusIndex;
                mineKind = MineKind.normal;
            } else {
                actualIndex = -statusIndex;
                mineKind = MineKind.banded;
            }
            LodeStatus? initial = `LodeStatus`.caseValues[actualIndex];
            if (!initial exists) {
                throw DriverFailedException(IllegalArgumentException(
                    "Status must be the valid index of a LodeStatus"));
            }
            assert (exists initial);
            MiningModel model = MiningModel(initial, seed, mineKind);
            Point lowerRight = model.maximumPoint;
            value path = parsePath(filename);
            if (is Nil loc = path.resource) {
                value file = loc.createFile();
                try (writer = file.Overwriter()) {
                    for (row in 0..(lowerRight.row + 1)) {
                        for (col in 0..(lowerRight.column + 1)) {
                            writer.write("``model.statusAt(
                                pointFactory(row, col)).ratio``,");
                        }
                        writer.writeLine();
                    }
                }
            } else {
                throw DriverFailedException(IOException(
                    "Output file ``filename`` already exists"));
            }
        } else {
            throw IncorrectUsageException(usage);
        }
    }
}
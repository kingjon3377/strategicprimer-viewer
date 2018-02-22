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

import strategicprimer.model.map {
    Point,
    pointFactory
}
import strategicprimer.drivers.common {
    DriverFailedException,
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    IncorrectUsageException
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
"""A driver to create a spreadsheet model of a mine. Its parameters are the name of the
   file to write the CSV to and the value at the top center (as an index into the
   LodeStatus values array).""""
shared object miningCLI satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-i", "--mining"];
        paramsWanted = ParamCount.two;
        shortDescription = "Create a model of a mine";
        longDescription = "Create a CSV spreadsheet representing a mine's area";
        firstParamDescription = "output.csv";
        subsequentParamDescription = "status";
        supportedOptionsTemp = [ "--seed=NN", "--banded" ];
    };
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (exists filename = args.first, exists second = args.rest.first, args.size == 2) {
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
            LodeStatus initial;
            if (is LodeStatus specified = LodeStatus.parse(second)) {
                initial = specified;
            } else if (is Integer index = Integer.parse(second),
                     exists specified = `LodeStatus`.caseValues[index]) {
                initial = specified;
            } else {
                throw DriverFailedException(IllegalArgumentException(
                    "Status must be a valid status or the index of a valid status"));
            }
            MineKind mineKind;
            // TODO: Support distance-from-center deposits
            if (options.hasOption("--banded")) {
                mineKind = MineKind.banded;
            } else {
                mineKind = MineKind.normal;
            }
            MiningModel model = MiningModel(initial, seed, mineKind);
            Point lowerRight = model.maximumPoint;
            value path = parsePath(filename);
            if (is Nil loc = path.resource) {
                value file = loc.createFile();
                try (writer = file.Overwriter()) {
                    for (row in 0..(lowerRight.row)) {
                        for (col in 0..(lowerRight.column)) {
                            writer.write("``model.statusAt(
                                pointFactory(row, col))?.ratio else -1``,");
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

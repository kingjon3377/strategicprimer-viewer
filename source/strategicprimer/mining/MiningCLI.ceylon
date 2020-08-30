import ceylon.file {
    parsePath,
    Nil
}

import strategicprimer.drivers.common {
    DriverFailedException,
    UtilityDriver,
    SPOptions,
    IncorrectUsageException
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"""A driver to create a spreadsheet model of a mine. Its parameters are the name of the
   file to write the CSV to and the value at the top center (as an index into the
   LodeStatus values array)."""
// TODO: Write GUI to allow user to visually explore a mine
native("jvm") // TODO: Try removing once strategicprimer.drivers.common isn't declared entirely "native".
shared class MiningCLI(ICLIHelper cli, options) satisfies UtilityDriver {
    shared actual SPOptions options;
    shared actual void startDriver(String* args) {
        if (exists filename = args.first, exists second = args.rest.first,
                args.size == 2) {
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
                     exists specified = sort(`LodeStatus`.caseValues)
                         .getFromFirst(index)) {
                initial = specified;
            } else {
                throw DriverFailedException(AssertionError(
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
            value [lowerRightRow, lowerRightColumn] = model.maximumPoint;

            value path = parsePath(filename);
            if (is Nil loc = path.resource) {
                value file = loc.createFile();
                try (writer = file.Overwriter()) {
                    for (row in 0..lowerRightRow) {
                        for (col in 0..lowerRightColumn) {
                            writer.write("``model.statusAt(
                                [row, col])?.ratio else -1``,");
                        }
                        writer.writeLine();
                    }
                }
            } else {
                throw DriverFailedException(Exception(
                    "Output file ``filename`` already exists"));
            }
        } else {
            throw IncorrectUsageException(MiningCLIFactory.staticUsage);
        }
    }
}

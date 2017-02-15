import controller.map.drivers {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper
}
import model.mining {
    MiningModel,
    LodeStatus
}
import java.lang {
    IllegalArgumentException
}
import model.map {
    Point,
    PointFactory
}
import ceylon.file {
    parsePath,
    Nil
}
import java.io {
    IOException
}
"""A driver to create a spreadsheet model of a mine. Its parameters are the name of the
   file to write the CSV to and the value at the top center (as an index into the
   LodeStatus values array).""""
object miningCLI satisfies UtilityDriver {
    DriverUsage usageObject = DriverUsage(false, "-i", "--mining", ParamCount.two,
        "Create a model of a mine",
        "Create a CSV spreadsheet representing a mine's area");
    usageObject.addSupportedOption("--seed=NN");
    usageObject.firstParamDesc = "output.csv";
    usageObject.subsequentParamDesc = "status";
    shared actual IDriverUsage usage = usageObject;
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
                    throw DriverFailedException("Seed must be numeric", temp);
                }
            } else {
                seed = system.milliseconds;
            }
            Integer actualIndex;
            MiningModel.MineKind mineKind;
            if (statusIndex >= 0) {
                actualIndex = statusIndex;
                mineKind = MiningModel.MineKind.normal;
            } else {
                actualIndex = -statusIndex;
                mineKind = MiningModel.MineKind.banded;
            }
            LodeStatus? initial = LodeStatus.values()[actualIndex];
            if (!initial exists) {
                throw DriverFailedException(IllegalArgumentException(
                    "Status must be the valid index of a LodeStatus"));
            }
            MiningModel model = MiningModel(initial, seed, mineKind);
            Point lowerRight = model.maxPoint;
            value path = parsePath(filename);
            if (is Nil loc = path.resource) {
                value file = loc.createFile();
                try (writer = file.Overwriter()) {
                    for (row in 0..(lowerRight.row + 1)) {
                        for (col in 0..(lowerRight.col + 1)) {
                            writer.write("``model.statusAt(
                                PointFactory.point(row, col)).ratio``,");
                        }
                        writer.writeLine();
                    }
                }
            } else {
                throw DriverFailedException(IOException(
                    "Output file ``filename`` already exists"));
            }
        } else {
            throw IncorrectUsageException(usageObject);
        }
    }
}
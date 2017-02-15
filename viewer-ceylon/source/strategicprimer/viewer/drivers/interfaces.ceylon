import controller.map.drivers {
    ISPDriver,
    SPOptions,
    DriverFailedException,
    SimpleDriver,
    ParamCount,
    IncorrectUsageException
}
import java.lang {
    IllegalStateException
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import controller.map.misc {
    ICLIHelper,
    MapReaderAdapter
}
import util {
    Warning
}
import java.nio.file {
    JPaths = Paths
}
"""An interface to allow utility drivers, which operate on files rather than a map model,
   to be a "functional" (single-method-to-implement) interface"""
interface UtilityDriver satisfies ISPDriver {
    shared actual default void startDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        throw DriverFailedException(IllegalStateException(
            "A utility driver can't operate on a driver model"));
    }
}
"An interface for drivers which operate on a map model of some kind and want to write it
 out again to file when they finish."
interface SimpleCLIDriver satisfies SimpleDriver {
    "Run the driver. This is the one method that implementations must implement."
    shared actual formal void startDriver(ICLIHelper cli, SPOptions options,
        IDriverModel model);
    "Run the driver. If the driver is a GUIDriver, this should use
     SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary. This
     default implementation assumes a CLI driver, and writes the model back to file(s)
     after calling startDriver with the model."
    // TODO: remove optional from String args after ISPDriver ported
    shared actual default void startDriver(ICLIHelper cli, SPOptions options, String?* args) {
        switch (usage().paramsWanted)
        case (ParamCount.none) {
            if (args.size == 0) {
                super.startDriver();
                return;
            } else {
                throw IncorrectUsageException(usage());
            }
        }
        case (ParamCount.anyNumber) {
            if (args.size == 0) {
                super.startDriver();
                return;
            }
        }
        case (ParamCount.atLeastOne) {
            if (args.size == 0) {
                throw IncorrectUsageException(usage());
            }
        }
        case (ParamCount.one) {
            if (args.size != 1) {
                throw IncorrectUsageException(usage());
            }
        }
        case (ParamCount.two) {
            if (args.size != 2) {
                throw IncorrectUsageException(usage());
            }
        }
        case (ParamCount.atLeastTwo) {
            if (args.size < 2) {
                throw IncorrectUsageException(usage());
            }
        }
        MapReaderAdapter reader = MapReaderAdapter();
        assert (exists firstArg = args.first);
        // We declare this as IMultiMapModel so we can correct the current turn in all
        // maps if needed.
        IMultiMapModel model = reader.readMultiMapModel(Warning.ignore,
            JPaths.get(firstArg), *MapReaderAdapter.namesToFiles(false, *args.rest));
        if (options.hasOption("--current-turn")) {
            if (is Integer currentTurn =
                    Integer.parse(options.getArgument("--current-turn"))) {
                for (pair in model.allMaps) {
                    pair.first().setCurrentTurn(currentTurn);
                }
            } else {
                cli.println("--current-turn must be an integer");
            }
        }
        startDriver(cli, options, model);
        reader.writeModel(model);
    }
}
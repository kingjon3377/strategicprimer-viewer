import controller.map.drivers {
    SPOptions,
    DriverFailedException,
    ParamCount,
    IncorrectUsageException,
    IDriverUsage
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
    MapReaderAdapter,
    FileChooser
}
import util {
    Warning
}
import java.nio.file {
    JPaths = Paths, JPath = Path
}
import model.map {
    IMutableMapNG,
    HasName
}
import java.util {
    JOptional = Optional
}
import java.io {
    IOException
}
import lovelace.util.common {
    todo
}
"""An interface to allow utility drivers, which operate on files rather than a map model,
   to be a "functional" (single-method-to-implement) interface"""
interface UtilityDriver satisfies ISPDriver {
    shared actual default void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        throw DriverFailedException(IllegalStateException(
            "A utility driver can't operate on a driver model"));
    }
}
"An interface for drivers which operate on a map model of some kind."
interface SimpleDriver satisfies ISPDriver {
    "(Try to) run the driver. If the driver does not need arguments, it should
     override this default method to support that; otherwise, this will throw,
     because nearly all drivers do need arguments."
    shared default void startDriverNoArgs() {
        throw DriverFailedException("Driver does not support no-arg operation",
            IllegalStateException("Driver does not support no-arg operation"));
    }
    "The one method that satisfying classes have to implement."
    shared actual formal void startDriverOnModel(ICLIHelper cli, SPOptions options,
        IDriverModel model);
    "Ask the user to choose a file."
    JPath askUserForFile() {
        try {
            return FileChooser(JOptional.empty<JPath>()).file;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException("Choice interrupted or user didn't choose",
                except);
        }
    }
    """Run the driver. If the driver is a GUI driver, this should use
       SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary. This
       default implementation does *not* write to file after running the driver on the
       driver model, as GUIs will expose a "save" option in their UI."""
    shared actual default void startDriverOnArguments(ICLIHelper cli,
            SPOptions options, String* args) {
        ParamCount desiderata = usage.paramsWanted;
        Anything(IMutableMapNG) turnFixer;
        if (options.hasOption("--current-turn")) {
            if (is Integer currentTurn =
                    Integer.parse(options.getArgument("--current-turn"))) {
                turnFixer = (IMutableMapNG map) => map.setCurrentTurn(currentTurn);
            } else {
                log.warn("--current-turn must be an integer");
                turnFixer = (IMutableMapNG map) {};
            }
        } else {
            turnFixer = (IMutableMapNG map) {};
        }
        if (args.size == 0) {
            if ({ ParamCount.none, ParamCount.anyNumber }.contains(desiderata)) {
                // FIXME: Make "no-arg" form take CLI and options
                // The Java version called startDriver(cli, options), which recurses.
                startDriverNoArgs();
            } else if ({ ParamCount.two, ParamCount.atLeastTwo }.contains(desiderata)) {
                JPath masterPath = askUserForFile();
                JPath subordinatePath = askUserForFile();
                IMultiMapModel mapModel = MapReaderAdapter()
                    .readMultiMapModel(Warning.default, masterPath, subordinatePath);
                for (pair in mapModel.allMaps) {
                    turnFixer(pair.first());
                }
                startDriverOnModel(cli, options, mapModel);
            } else {
                IMultiMapModel mapModel = MapReaderAdapter()
                    .readMultiMapModel(Warning.default, askUserForFile());
                for (pair in mapModel.allMaps) {
                    turnFixer(pair.first());
                }
                startDriverOnModel(cli, options, mapModel);
            }
        } else if (ParamCount.none == desiderata) {
            throw IncorrectUsageException(usage);
        } else if (args.size == 1,
                {ParamCount.two, ParamCount.atLeastTwo}.contains(desiderata)) {
            assert (exists firstArg = args.first);
            IMultiMapModel mapModel = MapReaderAdapter()
                .readMultiMapModel(Warning.default, JPaths.get(firstArg),
                    askUserForFile());
            for (pair in mapModel.allMaps) {
                turnFixer(pair.first());
            }
            startDriverOnModel(cli, options, mapModel);
        } else {
            assert (exists firstArg = args.first);
            IMultiMapModel mapModel = MapReaderAdapter()
                .readMultiMapModel(Warning.default, JPaths.get(firstArg),
                    *MapReaderAdapter.namesToFiles(false, *args.rest));
            for (pair in mapModel.allMaps) {
                turnFixer(pair.first());
            }
            startDriverOnModel(cli, options, mapModel);
        }
    }
}
"An interface for drivers, so one main() method can start different drivers based on
 options."
interface ISPDriver satisfies HasName {
    // In the Java implementation, these were overloads; Ceylon doesn't allow that,
    // which is in general better but a pain here.
    "Run the driver. If the driver is a GUI driver, this should use
     SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary.
     This form should only be used if the caller doesn't have an ICLIHelper to pass in."
    todo("Return exception instead of throwing?")
    shared default void startDriverOnArgumentsNoCLI(SPOptions options, String* args) {
        try (ICLIHelper cli = ConstructorWrapper.cliHelper()) {
            startDriverOnArguments(cli, options, *args);
        } catch (IOException except) { // TODO: what will a Ceylon ICLIHelper throw?
            throw DriverFailedException("I/O error interacting with user", except);
        }
    }
    "Run the driver. If the driver is a GUI driver, this should use
     SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary."
    shared formal void startDriverOnArguments(
        "The interface to interact with the user, either on the console or in a window
         emulating a console"
        ICLIHelper cli,
        "Any (already-processed) command-line options"
        SPOptions options,
        "Any command-line arguments that should be passed to the driver"
        String* args
    );
    "Run the driver on a driver model.
     This form should only be used if the caller doesn't have an ICLIHelper to pass in."
    shared default void startDriverOnModelNoCLI(SPOptions options, IDriverModel model) {
        try (ICLIHelper cli = ConstructorWrapper.cliHelper()) {
            startDriverOnModel(cli, options, model);
        } catch (IOException except) { // TODO: what will a Ceylon ICLIHelper throw?
            throw DriverFailedException("I/O error interacting with user", except);
        }
    }
    "Run the driver on a driver model."
    todo("Rename back to startDriver() and take String*|IDriverModel")
    shared formal void startDriverOnModel(
        "The interface to interact with the user, either on the console or in a window
         emulating a console"
        ICLIHelper cli,
        "Any (already-processed) command-line options"
        SPOptions options,
        "The driver-model that should be used by the app."
        IDriverModel model
    );
    """The usage object for the driver. The default implementation throws, to allow
       satisfying interfaces to be "functional" (single-formal-method) interfaces, but
       implementations *should* implement this."""
    shared formal IDriverUsage usage;
    "What to call this driver in a CLI list."
    shared actual default String name => usage.shortDescription;
}
"An interface for drivers which operate on a map model of some kind and want to write it
 out again to file when they finish."
interface SimpleCLIDriver satisfies SimpleDriver {
    "Run the driver. This is the one method that implementations must implement."
    shared actual formal void startDriverOnModel(ICLIHelper cli, SPOptions options,
        IDriverModel model);
    "Run the driver. If the driver is a GUIDriver, this should use
     SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary. This
     default implementation assumes a CLI driver, and writes the model back to file(s)
     after calling startDriver with the model."
    // TODO: remove optional from String args after ISPDriver ported
    shared actual default void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        switch (usage.paramsWanted)
        case (ParamCount.none) {
            if (args.size == 0) {
                super.startDriverNoArgs();
                return;
            } else {
                throw IncorrectUsageException(usage);
            }
        }
        case (ParamCount.anyNumber) {
            if (args.size == 0) {
                super.startDriverNoArgs();
                return;
            }
        }
        case (ParamCount.atLeastOne) {
            if (args.size == 0) {
                throw IncorrectUsageException(usage);
            }
        }
        case (ParamCount.one) {
            if (args.size != 1) {
                throw IncorrectUsageException(usage);
            }
        }
        case (ParamCount.two) {
            if (args.size != 2) {
                throw IncorrectUsageException(usage);
            }
        }
        case (ParamCount.atLeastTwo) {
            if (args.size < 2) {
                throw IncorrectUsageException(usage);
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
        startDriverOnModel(cli, options, model);
        reader.writeModel(model);
    }
}
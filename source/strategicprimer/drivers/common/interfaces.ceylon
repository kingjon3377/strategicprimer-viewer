import ceylon.collection {
    MutableMap,
    HashMap
}

import java.nio.file {
    JPaths=Paths,
    JPath=Path
}

import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    IMutableMapNG,
    HasName
}
import strategicprimer.model.xmlio {
    warningLevels,
    mapIOHelper
}
import strategicprimer.drivers.common {
    IMultiMapModel,
    IDriverModel,
    mapReaderAdapter
}
import strategicprimer.drivers.common.cli {
    ICLIHelper,
    CLIHelper
}
"""An interface for the command-line options passed by the user. At this point we
   assume that if any option is passed to an app more than once, the subsequent option
   overrides the previous, and any option passed without argument has an implied argument
   of "true".""" // TODO: Maybe satisfy Correspondence to condense callers? // Satisfying Category causes backend error, eclipse/ceylon#7385
shared interface SPOptions satisfies {<String->String>*} {
    "Whether the specified option was given, with or without an argument."
    shared formal Boolean hasOption(String option);
    """Get the argument provided for the given argument ("true" if given without one,
       "false" if not given by the user)."""
    shared formal String getArgument(String option);
    "Clone the object."
    shared formal SPOptions copy();
}
"""The command-line options passed by the user. At this point we assume that if any option
   is passed to an app more than once, the subsequent option overrides the previous, and
   any option passed without argument has an implied argument of "true"."""
shared class SPOptionsImpl({<String->String>*} existing = []) satisfies SPOptions {
    MutableMap<String, String> options = HashMap<String, String>();
    options.putAll(existing);
    shared void addOption(String option, String argument = "true") {
        if ("false" == argument) {
            options.remove(option);
        } else {
            options[option] = argument;
        }
    }
    shared actual Boolean hasOption(String option) => options.defines(option);
    shared actual String getArgument(String option) => options[option] else "false";
    shared actual SPOptionsImpl copy() => SPOptionsImpl(options);
    shared actual String string {
        StringBuilder builder = StringBuilder();
        for (key->val in options) {
            if (val == "true") {
                builder.append(key);
            } else {
                builder.append("``key``=``val``");
            }
            builder.appendNewline();
        }
        return builder.string;
    }
    shared actual Iterator<String->String> iterator() => options.iterator();

}
"""An interface to allow utility drivers, which operate on files rather than a map model,
   to be a "functional" (single-method-to-implement) interface"""
shared interface UtilityDriver satisfies ISPDriver {
    shared actual default void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        throw DriverFailedException.illegalState(
            "A utility driver can't operate on a driver model");
    }
}
"An interface for drivers which operate on a map model of some kind."
shared interface SimpleDriver satisfies ISPDriver {
    "(Try to) run the driver. If the driver does not need arguments, it should
     override this default method to support that; otherwise, this will throw,
     because nearly all drivers do need arguments."
    shared default void startDriverNoArgs(ICLIHelper cli = CLIHelper(),
            SPOptions options = SPOptionsImpl()) {
        throw DriverFailedException.illegalState(
            "Driver does not support no-arg operation");
    }
    "Run the driver."
    shared actual formal void startDriverOnModel(ICLIHelper cli, SPOptions options,
        IDriverModel model);
    "Ask the user to choose a file or files. (Or do something equivalent to produce a
     filename.)"
    shared formal {JPath*} askUserForFiles();
    """Run the driver. If the driver is a GUI driver, this should use
       SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary. This
       default implementation does *not* write to file after running the driver on the
       driver model, as GUIs will expose a "save" option in their UI."""
    shared actual default void startDriverOnArguments(ICLIHelper cli,
            SPOptions options, String* args) {
        log.trace("In SimpleDriver.startDriverOnArguments()");
        ParamCount desiderata = usage.paramsWanted;
        Anything(IMutableMapNG) turnFixer;
        if (options.hasOption("--current-turn")) {
            if (is Integer currentTurn =
                    Integer.parse(options.getArgument("--current-turn"))) {
                turnFixer = IMutableMapNG.currentTurn;
            } else {
                log.warn("--current-turn must be an integer");
                turnFixer = noop;
            }
        } else {
            turnFixer = noop;
        }
        if (args.size == 0) {
            switch (desiderata)
            case (ParamCount.none|ParamCount.anyNumber) {
                log.trace("No arguments, none needed");
                startDriverNoArgs(cli, options);
            }
            case (ParamCount.two) {
                log.trace("No arguments, two needed");
                value firstSelectedPaths = askUserForFiles();
                if (firstSelectedPaths.empty) {
                    throw IncorrectUsageException(usage);
                }
                assert (exists masterPath = firstSelectedPaths.first);
                log.trace("Got the first from the user");
                {JPath+} secondSelectedPaths;
                if (is {JPath+} temp = firstSelectedPaths.rest) {
                    secondSelectedPaths = temp;
                } else if (is {JPath+} temp = askUserForFiles()) {
                    secondSelectedPaths = temp;
                } else {
                    throw IncorrectUsageException(usage);
                }
                if (secondSelectedPaths.rest.first exists) {
                    throw IncorrectUsageException(usage);
                }
                log.trace("Got the second from the user");
                value subordinatePath = secondSelectedPaths.first;
                IMultiMapModel mapModel =
                        mapReaderAdapter.readMultiMapModel(warningLevels.default,
                            masterPath, subordinatePath);
                log.trace("Read maps from the two arguments");
                mapModel.allMaps.map(Entry.key).each(turnFixer);
                startDriverOnModel(cli, options, mapModel);
            }
            case (ParamCount.atLeastTwo) {
                log.trace("No arguments, needed at least two");
                value firstSelectedPaths = askUserForFiles();
                if (firstSelectedPaths.empty) {
                    throw IncorrectUsageException(usage);
                }
                assert (exists masterPath = firstSelectedPaths.first);
                log.trace("Got first file from the user");
                {JPath+} secondSelectedPaths;
                if (is {JPath+} temp = firstSelectedPaths.rest) {
                    secondSelectedPaths = temp;
                } else if (is {JPath+} temp = askUserForFiles()) {
                    secondSelectedPaths = temp;
                } else {
                    throw IncorrectUsageException(usage);
                }
                log.trace("Got second and following from the user");
                IMultiMapModel mapModel =
                        mapReaderAdapter.readMultiMapModel(warningLevels.default,
                            masterPath, *secondSelectedPaths);
                log.trace("Read maps from the arguments");
                mapModel.allMaps.map(Entry.key).each(turnFixer);
                startDriverOnModel(cli, options, mapModel);
            }
            case (ParamCount.one) {
                value chosenFiles = askUserForFiles();
                if (exists chosenFile = chosenFiles.first,
                        !chosenFiles.rest.first exists) {
                    IDriverModel mapModel = mapReaderAdapter.readMapModel(chosenFile,
                        warningLevels.default);
                    turnFixer(mapModel.map);
                    startDriverOnModel(cli, options, mapModel);
                } else {
                    throw IncorrectUsageException(usage);
                }
            }
            case (ParamCount.atLeastOne) {
                log.trace("No arguments, needed at least one");
                value chosenFiles = askUserForFiles();
                log.trace("Asked the user to choose a file");
                if (exists chosenFile = chosenFiles.first) {
                    IMultiMapModel mapModel =
                            mapReaderAdapter.readMultiMapModel(warningLevels.default,
                                chosenFile, *chosenFiles.rest);
                    log.trace("Parsed map(s) from file(s)");
                    mapModel.allMaps.map(Entry.key).each(turnFixer);
                    startDriverOnModel(cli, options, mapModel);
                } else {
                    log.error("No file chosen");
                    throw IncorrectUsageException(usage);
                }
            }
            else {
                log.error("Not any expected ParamCount");
                throw IncorrectUsageException(usage);
            }
        } else if (ParamCount.none == desiderata) {
            log.trace("Got an argument when driver doesn't take any");
            throw IncorrectUsageException(usage);
        } else if (args.size == 1, ParamCount.two == desiderata) {
            log.trace("Got one argument, needed exactly two");
            assert (exists firstArg = args.first);
            value chosenFiles = askUserForFiles();
            log.trace("Asked user for second file");
            if (chosenFiles.empty || chosenFiles.rest.first exists) {
                throw IncorrectUsageException(usage);
            } else {
                IMultiMapModel mapModel =
                        mapReaderAdapter.readMultiMapModel(warningLevels.default,
                            JPaths.get(firstArg), *chosenFiles);
                log.trace("Parsed maps from the two files");
                mapModel.allMaps.map(Entry.key).each(turnFixer);
                startDriverOnModel(cli, options, mapModel);
            }
        } else if (args.size == 1, ParamCount.atLeastTwo == desiderata) {
            log.trace("Got one argument, needed at least two");
            assert (exists firstArg = args.first);
            value chosenFiles = askUserForFiles();
            log.trace("Asked user for subsequent files");
            if (chosenFiles.empty) {
                throw IncorrectUsageException(usage);
            } else {
                IMultiMapModel mapModel =
                        mapReaderAdapter.readMultiMapModel(warningLevels.default,
                            JPaths.get(firstArg), *chosenFiles);
                log.trace("Parsed maps from the files");
                mapModel.allMaps.map(Entry.key).each(turnFixer);
                startDriverOnModel(cli, options, mapModel);
            }
        } else if (args.size == 1) {
            log.trace("Got one argument, don't need more");
            assert (exists arg = args.first);
            IDriverModel mapModel =
                    mapReaderAdapter.readMapModel(JPaths.get(arg), warningLevels.default);
            log.trace("Parsed map from file");
            turnFixer(mapModel.map);
            startDriverOnModel(cli, options, mapModel);
        } else {
            log.trace("Got enough arguments");
            assert (exists firstArg = args.first);
            assert (nonempty others = args.rest);
            IMultiMapModel mapModel =
                    mapReaderAdapter.readMultiMapModel(warningLevels.default,
                        JPaths.get(firstArg), *mapIOHelper.namesToFiles(*others));
            log.trace("Parsed paths from arguments");
            mapModel.allMaps.map(Entry.key).each(turnFixer);
            startDriverOnModel(cli, options, mapModel);
        }
    }
}
"An interface for drivers, so one main() method can start different drivers based on
 options."
shared interface ISPDriver satisfies HasName {
    // In the Java implementation, these were overloads; Ceylon doesn't allow that,
    // which is in general better but a pain here.
    "Run the driver. If the driver is a GUI driver, this should use
     SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary.
     This form should only be used if the caller doesn't have an ICLIHelper to pass in."
    // We'd like to combine with [[startDriverOnModelNoCLI]], but we can't take a union of
    // a variadic and a non-variadic parameter.
    shared default void startDriverOnArgumentsNoCLI(SPOptions options, String* args) =>
            startDriverOnArguments(CLIHelper(), options, *args);
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
    shared default void startDriverOnModelNoCLI(SPOptions options, IDriverModel model) =>
            startDriverOnModel(CLIHelper(), options, model);
    "Run the driver on a driver model."
    // We'd like to combine with [[startDriverOnArguments]] (and rename back to just
    // `startDriver`), but `String*|IDriverModel` isn't a possible type.
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
shared interface SimpleCLIDriver satisfies SimpleDriver {
    "Run the driver. This is the one method that implementations must implement."
    shared actual formal void startDriverOnModel(ICLIHelper cli, SPOptions options,
        IDriverModel model);
    "As CLI drivers can't ask the user to choose a file using a file-chooser dialog, we
     simply return the empty stream here as a default."
    shared actual default {JPath*} askUserForFiles() => [];
    "Run the driver. If the driver is a GUIDriver, this should use
     SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary. This
     default implementation assumes a CLI driver, and writes the model back to file(s)
     after calling startDriver with the model."
    shared actual default void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        log.trace("In SimpleCLIDriver.startDriverOnArguments");
        switch (usage.paramsWanted)
        case (ParamCount.none) {
            if (args.size == 0) {
                log.trace("No arguments wanted, none given");
                super.startDriverNoArgs(cli, options);
                return;
            } else {
                throw IncorrectUsageException(usage);
            }
        }
        case (ParamCount.anyNumber) {
            if (args.size == 0) {
                log.trace("Zero or more arguments wanted, none given");
                super.startDriverNoArgs(cli, options);
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
        assert (exists firstArg = args.first);
        IDriverModel model;
        if (nonempty rest = args.rest) {
	        // We declare this as IMultiMapModel so we can correct the current turn in all
	        // maps if needed.
	        log.trace("About to read maps from files");
	        model = mapReaderAdapter.readMultiMapModel(warningLevels.ignore,
	            JPaths.get(firstArg), *mapIOHelper.namesToFiles(*rest));
	        assert (is IMultiMapModel model);
	        log.trace("Finished reading maps from files");
	        if (options.hasOption("--current-turn")) {
	            if (is Integer currentTurn =
	                    Integer.parse(options.getArgument("--current-turn"))) {
	                for (map->path in model.allMaps) {
	                    map.currentTurn = currentTurn;
	                }
	            } else {
	                cli.println("--current-turn must be an integer");
	            }
	        }
	    } else {
	        log.trace("About to read map from file");
	        model = mapReaderAdapter.readMapModel(JPaths.get(firstArg), warningLevels.ignore);
	        log.trace("Finished reading map from file");
	        if (options.hasOption("--currentTurn")) {
	            if (is Integer currentTurn =
                        Integer.parse(options.getArgument("--current-turn"))) {
	                model.map.currentTurn = currentTurn;
		        } else {
		            cli.println("--current-turn must be an integer");
		        }
		    }
	    }
        startDriverOnModel(cli, options, model);
        mapReaderAdapter.writeModel(model);
    }
}
"An exception to throw whenever a driver fails, so drivers only have to directly handle
 one exception class."
todo("Is this really necessary any more?")
shared class DriverFailedException
        extends Exception {
    shared new (Throwable cause,
            String message = "The driver could not start because of an exception:")
            extends Exception(message, cause) {}
    shared new illegalState(String message) extends
            DriverFailedException(AssertionError(message), message) {}
 }
"An exception to throw when a driver fails because the user tried to use it improperly."
shared class IncorrectUsageException(correctUsage)
        extends DriverFailedException(AssertionError("Incorrect usage"),
            "Incorrect usage") {
    """The "usage object" for the driver, describing its correct usage."""
    shared IDriverUsage correctUsage;
}
"Possible numbers of (non-option?) parameters a driver might shared want."
shared class ParamCount of none | one | two | atLeastOne | atLeastTwo | anyNumber {
    "None at all."
    shared new none { }
    "Exactly one."
    shared new one { }
    "Exactly two."
    shared new two { }
    "One or more."
    shared new atLeastOne { }
    "Two or more."
    shared new atLeastTwo { }
    "Zero or more."
    shared new anyNumber { }
}
"An interface for objects representing usage information for drivers, for use in the app
 starter and in help text."
shared interface IDriverUsage {
    "Whether the driver is a GUI."
    shared formal Boolean graphical;
    "Options with which the user can select this driver."
    shared formal {String+} invocations;
    "How many non-option parameters this driver wants."
    shared formal ParamCount paramsWanted;
    "A short (one-line at most) description of the driver."
    shared formal String shortDescription;
    "A long(er) description of the driver."
    shared formal String longDescription;
    "A description of the first parameter for use in a usage statement."
    shared formal String firstParamDescription;
    "A description of parameters other than the first for use in a usage statement."
    shared formal String subsequentParamDescription;
    """Options this driver supports. (To show the user, so "=NN" to mean a numeric option
       is reasonable."""
    shared formal {String*} supportedOptions;
    "Whether this driver should be included in the list presented for the user to choose
     from."
    shared formal Boolean includeInList(
            "If true, this is a GUI list; if false, a CLI list" Boolean gui);
}
shared class DriverUsage(
    "Whether this driver is graphical or not."
    shared actual Boolean graphical,
    "Options with which one can invoke this driver. Usually there's a short (if possible
     one-character) option and a longer (and probably more memorable and descriptive)
     option."
    shared actual {String+} invocations,
    "How many parameters this driver wants."
    shared actual ParamCount paramsWanted,
    "A short description of the driver"
    shared actual String shortDescription,
    "A longer description of the driver"
    shared actual String longDescription,
    "Whether to include in a CLI list for the user to choose from."
    Boolean includeInCLIList,
    "Whether to include in a GUI list for the user to choose from."
    Boolean includeInGUIList,
    "A description of the first (non-option) parameter, for use in a usage statement."
    shared actual String firstParamDescription = "filename.xml",
    "A description of a later parameter, for use in a usage statement. (We assume that all
     parameters after the first should be described similarly.)"
    shared actual String subsequentParamDescription = "filename.xml",
    "The options this driver supports."
    String* supportedOptionsTemp
) satisfies IDriverUsage {
    shared actual {String*} supportedOptions =
            supportedOptionsTemp;
    shared actual Boolean includeInList(Boolean gui) =>
            (gui) then includeInGUIList else includeInCLIList;
}

import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    todo
}
import strategicprimer.model.common.map {
    HasName
}
import strategicprimer.drivers.common {
    IDriverModel
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import ceylon.file {
    Path
}
"""An interface for the command-line options passed by the user. At this point we
   assume that if any option is passed to an app more than once, the subsequent option
   overrides the previous, and any option passed without argument has an implied argument
   of "true".""" // TODO: Simplify callers using Correspondence and/or Category syntax sugar
shared interface SPOptions satisfies {<String->String>*}&Correspondence<String, String>&
        Category<Object> {
    "Whether the specified option was given, with or without an argument."
    shared formal Boolean hasOption(String option);
    """Get the argument provided for the given argument ("true" if given without one,
       "false" if not given by the user)."""
    shared formal String getArgument(String option);
    "Clone the object."
    shared formal SPOptions copy();
    shared default actual Boolean contains(Object key) {
        if (is String key) {
            return hasOption(key);
        } else {
            return super.contains(key);
        }
    }
}
"""The command-line options passed by the user. At this point we assume that if any option
   is passed to an app more than once, the subsequent option overrides the previous, and
   any option passed without argument has an implied argument of "true"."""
shared class SPOptionsImpl({<String->String>*} existing = [])
        satisfies SPOptions&KeyedCorrespondenceMutator<String, String> {
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
    shared actual Boolean defines(String key) => options.defines(key);
    shared actual String get(String key) => options[key] else "false";
    shared actual void put(String key, String item) => addOption(key, item);
}
"""An interface to allow utility drivers, which operate on files rather than a map model,
   to be a "functional" (single-method-to-implement) interface"""
shared interface UtilityDriver satisfies ISPDriver {
    "Run the driver. If the driver is a GUI driver, this should use
     SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary."
    shared formal void startDriverOnArguments(
            "The interface to interact with the user, either on the console or in a window
             emulating a console"
            ICLIHelper cli,
            "Any (already-processed) command-line options"
            SPOptions options,
            "Any command-line arguments, such as filenames, that should be passed to the
             driver. This will not include options."
            String* args);
}
"An interface for drivers, so one main() method can start different drivers based on
 options."
shared interface ISPDriver of UtilityDriver|ModelDriver satisfies HasName {
    """The usage object for the driver. The default implementation throws, to allow
       satisfying interfaces to be "functional" (single-formal-method) interfaces, but
       implementations *should* implement this."""
    shared formal IDriverUsage usage;
    "What to call this driver in a CLI list."
    shared actual default String name => usage.shortDescription;
}
"An interface for drivers which operate on a map model of some kind."
shared interface ModelDriver of CLIDriver|ReadOnlyDriver|GUIDriver satisfies ISPDriver {
    "Run the driver on a driver model."
    shared formal void startDriverOnModel(
            "The interface to interact with the user, either on the console or in a window
             emulating a console"
            ICLIHelper cli,
            "Any (already-processed) command-line options"
            SPOptions options,
            "The driver-model that should be used by the app."
            IDriverModel model);
}
"An interface for drivers which operate on a map model of some kind and should have maps
 written back to file when they exit."
shared interface CLIDriver satisfies ModelDriver {}
"An interface for drivers which operate on a map model of some kind; being GUIs, do
 not need to have the maps written back to file automatically; and have a way to get
 additional files from the user."
shared interface GUIDriver satisfies ModelDriver {
    "Ask the user to choose a file or files. (Or do something equivalent to produce a
     filename.)"
    shared formal {Path*} askUserForFiles();
}
"An interface for drivers which operate on a map model of some kind but never want to
 have its contents written back to disk (automatically)."
shared interface ReadOnlyDriver satisfies ModelDriver {}
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

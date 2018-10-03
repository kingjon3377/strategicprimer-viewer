shared class DriverUsage( // TODO: Convert to constructor so we can get rid of the 'temp' in supportedOptionsTemp
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

shared class DriverUsage satisfies IDriverUsage {
    "Whether this driver is graphical or not."
    shared actual Boolean graphical;

    "Options with which one can invoke this driver. Usually there's a short (if possible
     one-character) option and a longer (and probably more memorable and descriptive)
     option."
    shared actual {String+} invocations;

    "How many parameters this driver wants."
    shared actual ParamCount paramsWanted;

    "A short description of the driver"
    shared actual String shortDescription;

    "A longer description of the driver"
    shared actual String longDescription;

    "Whether to include in a CLI list for the user to choose from."
    Boolean includeInCLIList;

    "Whether to include in a GUI list for the user to choose from."
    Boolean includeInGUIList;

    "A description of the first (non-option) parameter, for use in a usage statement."
    shared actual String firstParamDescription;

    "A description of a later parameter, for use in a usage statement. (We assume that all
     parameters after the first should be described similarly.)"
    shared actual String subsequentParamDescription;

    "The options this driver supports."
    shared actual {String*} supportedOptions;

    shared new (
            "Whether this driver is graphical or not."
            Boolean graphical,
            "Options with which one can invoke this driver. Usually there's a short (if possible
             one-character) option and a longer (and probably more memorable and descriptive)
             option."
            {String+} invocations,
            "How many parameters this driver wants."
            ParamCount paramsWanted,
            "A short description of the driver"
            String shortDescription,
            "A longer description of the driver"
            String longDescription,
            "Whether to include in a CLI list for the user to choose from."
            Boolean includeInCLIList,
            "Whether to include in a GUI list for the user to choose from."
            Boolean includeInGUIList,
            "A description of the first (non-option) parameter, for use in a usage statement."
            String firstParamDescription = "filename.xml",
            "A description of a later parameter, for use in a usage statement. (We assume that all
             parameters after the first should be described similarly.)"
            String subsequentParamDescription = "filename.xml",
            "The options this driver supports."
            String* supportedOptions) {
        this.graphical = graphical;
        this.invocations = invocations;
        this.paramsWanted = paramsWanted;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.includeInCLIList = includeInCLIList;
        this.includeInGUIList = includeInGUIList;
        this.firstParamDescription = firstParamDescription;
        this.subsequentParamDescription = subsequentParamDescription;
        this.supportedOptions = supportedOptions;
    }

    shared actual Boolean includeInList(Boolean gui) =>
            (gui) then includeInGUIList else includeInCLIList;
}

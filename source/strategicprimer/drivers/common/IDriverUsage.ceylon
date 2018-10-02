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

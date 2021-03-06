import strategicprimer.model.common.xmlio {
    Warning,
    warningLevels
}

"An interface for a factory that XML-reading code can use to register IDs and produce
 not-yet-used IDs."
shared interface IDRegistrar {
    "Whether the given ID number is unused."
    shared formal Boolean isIDUnused(Integer id);

    "Register, and return, an ID, firing a warning if it's already used on the given
     [[Warning]] instance."
    shared formal Integer register(Integer id, Warning warning = warningLevels.default,
            "The location ([row, column]) in some XML that this is coming from. Null if
             caller isn't an XML reader."
            [Integer, Integer]? location = null);

    "Generate and register an ID that hasn't been previously registered."
    shared formal Integer createID();
}

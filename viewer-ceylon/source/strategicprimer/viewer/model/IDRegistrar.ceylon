import util {
    Warning
}
"An interface for a factory that XML-reading code can use to register IDs and produce
 not-yet-used IDs."
shared interface IDRegistrar {
    "Whether the given ID number is unused."
    shared formal Boolean isIDUnused(Integer id);
    "Register, and return, an ID, firing a warning if it's already used on the given
     [[Warning]] instance."
    shared formal Integer register(Integer id, Warning warning = Warning.default);
    "Generate and register an ID that hasn't been previously registered."
    shared formal Integer createID();
}
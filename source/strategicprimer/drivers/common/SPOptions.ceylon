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

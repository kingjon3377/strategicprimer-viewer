package drivers.common;

import java.util.Map;

/**
 * An interface for the command-line options passed by the user. At this point
 * we assume that if any option is passed to an app more than once, the
 * subsequent option overrides the previous, and any option passed without
 * argument has an implied argument of "true".
 */
public interface SPOptions extends Iterable<Map.Entry<String, String>> {
    /**
     * Whether the specified option was given, with or without an argument.
     */
    boolean hasOption(String option);

    /**
     * Get the argument provided for the given argument ("true" if given
     * without one, "false" if not given by the user).
     */
    String getArgument(String option);

    /**
     * Clone the object.
     */
    SPOptions copy();
}

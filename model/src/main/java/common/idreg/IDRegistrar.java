package common.idreg;

import common.xmlio.Warning;

import javax.xml.stream.Location;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for a factory that XML-reading code can use to register IDs and
 * produce not-yet-used IDs.
 *
 * TODO: Should we use longs instead of ints for IDs?
 */
public interface IDRegistrar {
    /**
     * Whether the given ID number is unused.
     */
    boolean isIDUnused(int id);

    /**
     * Register, and return, an ID, firing a warning on the given {@link
     * Warning} instance if it's already recorded as in use.
     *
     * @param warning how to handle a warning
     * @param location The location in some XML that this is coming from,
     *        if the caller is an XML reader.
     */
    int register(int id, Warning warning, @Nullable Location location);

    default int register(final int id, final Warning warning) {
        return register(id, warning, null);
    }

    default int register(final int id) {
        return register(id, Warning.getDefaultHandler(), null);
    }

    /**
     * Generate and register an ID that hasn't been previously registered.
     */
    int createID();
}

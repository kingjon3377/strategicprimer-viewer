package legacy.idreg;

import common.xmlio.Warning;

import javax.xml.stream.Location;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

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
	 * @param warning  how to handle a warning
	 * @param location The location in some XML that this is coming from,
	 *                 if the caller is an XML reader.
	 */
	int register(int id, Warning warning, @Nullable Pair<@Nullable Path, Location> location);

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

	/**
	 * Create a copy of our current state. This is primarily useful for test code, but possibly for other use cases
	 * as well.
	 * @return a copy of this registrar in its current state
	 */
	IDRegistrar copy();
}

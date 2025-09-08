package legacy.idreg;

import common.entity.LegacyIdentifier;
import common.idreg.DuplicateIDException;
import org.javatuples.Pair;
import org.roaringbitmap.RoaringBitmap;

import common.xmlio.Warning;

import javax.xml.stream.Location;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A class to register IDs with and produce not-yet-used IDs. We use a bit-set
 * implementation that's as fast as we can find, but we don't want to go to
 * random IDs because we want them to be as low as possible.
 */
public final class IDFactory implements IDRegistrar {
	/**
	 * The set of IDs used already.
	 */
	private final RoaringBitmap usedIDs = new RoaringBitmap();

	/**
	 * Whether the given ID is unused.
	 */
	@Override
	public boolean isIDUnused(final int id) {
		return id >= 0 && !usedIDs.contains(id);
	}

	/**
	 * Register, and return, the given ID, using the given Warning instance
	 * to report if it has already been registered.
	 */
	public int register(final int id, final Warning warning, final @Nullable Pair<@Nullable Path, Location> location) {
		if (id >= 0) {
			if (usedIDs.contains(id)) {
				if (Objects.isNull(location)) {
					warning.handle(new DuplicateIDException(new LegacyIdentifier(id)));
				} else if (Objects.isNull(location.getValue0())) {
					warning.handle(new DuplicateIDException(new LegacyIdentifier(id),
							location.getValue1().getLineNumber(), location.getValue1().getColumnNumber()));
				} else {
					warning.handle(new DuplicateIDException(new LegacyIdentifier(id), location.getValue0(),
							location.getValue1().getLineNumber(), location.getValue1().getColumnNumber()));
				}
			}
			usedIDs.add(id);
		}
		return id;
	}
	/**
	 * Generate and register an ID that hasn't been previously registered.
	 */
	@Override
	public int createID() {
		final int retval;
		synchronized (usedIDs) {
			assert (usedIDs.getCardinality() < Integer.MAX_VALUE - 2);
			retval = register((int) usedIDs.nextAbsentValue(0));
		}
		assert (retval >= 0);
		return retval;
	}

	/**
	 * Create a copy of this factory for testing purposes. (So that we
	 * don't "register" IDs that don't end up getting used.)
	 *
	 * TODO: Tests should cover this method.
	 */
	@Override
	public IDRegistrar copy() {
		final IDFactory retval = new IDFactory();
		retval.usedIDs.or(usedIDs);
		return retval;
	}
}

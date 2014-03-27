package controller.map.misc;

import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMap;
import model.map.ITileCollection;
import model.map.Point;
import model.misc.IMultiMapModel;
import util.Pair;

/**
 * A class to create an IDFactory with all IDs in a map, or in a collection of
 * fixtures, already registered as used.
 *
 * @author Jonathan Lovelace
 */
public final class IDFactoryFiller {
	/**
	 * Don't instantiate.
	 */
	private IDFactoryFiller() {
		// Only static methods.
	}

	/**
	 * @param map a map
	 * @return an ID factory that won't generate an ID the map already uses
	 */
	public static IDFactory createFactory(final IMap map) {
		final IDFactory retval = new IDFactory();
		final ITileCollection tiles = map.getTiles();
		for (final Point point : tiles) {
			if (point != null) {
				recursiveRegister(retval, tiles.getTile(point));
			}
		}
		return retval;
	}

	/**
	 * @param model a collection of maps
	 * @return an ID factory that won't generate an ID any of the maps already
	 *         uses.
	 */
	public static IDFactory createFactory(final IMultiMapModel model) {
		final IDFactory retval = new IDFactory();
		for (final Pair<IMap, String> pair : model.getAllMaps()) {
			final ITileCollection tiles = pair.first().getTiles();
			for (final Point point : tiles) {
				if (point != null) {
					recursiveRegister(retval, tiles.getTile(point));
				}
			}
		}
		return retval;
	}

	/**
	 * @param iter a collection of fixtures
	 * @return an ID factory that won't generate an ID already used in the
	 *         collection
	 */
	public static IDFactory createFactory(final FixtureIterable<?> iter) {
		final IDFactory retval = new IDFactory();
		recursiveRegister(retval, iter);
		return retval;
	}

	/**
	 * @param idf an IDFactory instance
	 * @param iter a collection of fixtures, all of which (recursively) should
	 *        have their IDs marked as used.
	 */
	private static void recursiveRegister(final IDFactory idf,
			final FixtureIterable<?> iter) {
		for (final IFixture fix : iter) {
			final int idNum = fix.getID();
			if (!idf.used(idNum)) {
				// We don't want to set off duplicate-ID warnings for the same
				// fixture in multiple maps. Or for Mountains and the like.
				idf.register(idNum);
			}
			if (fix instanceof FixtureIterable<?>) {
				recursiveRegister(idf, (FixtureIterable<?>) fix);
			}
		}
	}
}

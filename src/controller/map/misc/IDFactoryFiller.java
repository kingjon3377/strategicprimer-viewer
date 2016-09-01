package controller.map.misc;

import java.nio.file.Path;
import java.util.Optional;
import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Point;
import model.misc.IMultiMapModel;
import org.eclipse.jdt.annotation.NonNull;
import util.Pair;

/**
 * A class to create an IDFactory with all IDs in a map, or in a collection of fixtures,
 * already registered as used.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
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
	public static IDRegistrar createFactory(final IMapNG map) {
		final IDRegistrar retval = new IDFactory();
		for (final Point point : map.locations()) {
			// Ground, Forest, Rivers, and Mountains do not have IDs, so we
			// can skip them and just test the "other" fixtures
			for (final IFixture fixture : map.getOtherFixtures(point)) {
				final int idNum = fixture.getID();
				if (retval.isIDUnused(idNum)) {
					// We don't want to set off duplicate-ID warnings for
					// the same fixture in multiple maps.
					retval.register(idNum);
				}
				if (fixture instanceof FixtureIterable) {
					recursiveRegister(retval, (FixtureIterable<@NonNull ?>) fixture);
				}
			}
		}
		return retval;
	}

	/**
	 * @param model a collection of maps
	 * @return an ID factory that won't generate an ID any of the maps already uses.
	 */
	public static IDRegistrar createFactory(final IMultiMapModel model) {
		final IDRegistrar retval = new IDFactory();
		for (final Pair<IMutableMapNG, Optional<Path>> pair : model.getAllMaps()) {
			for (final Point point : pair.first().locations()) {
				// Ground, Forest, Rivers, and Mountains do not have IDs, so we
				// can skip them and just test the "other" fixtures
				for (final IFixture fixture : pair.first().getOtherFixtures(point)) {
					final int idNum = fixture.getID();
					if (retval.isIDUnused(idNum)) {
						// We don't want to set off duplicate-ID warnings for
						// the same fixture in multiple maps.
						retval.register(idNum);
					}
					if (fixture instanceof FixtureIterable) {
						recursiveRegister(retval, (FixtureIterable<@NonNull ?>) fixture);
					}
				}
			}
		}
		return retval;
	}

	/**
	 * @param iter a collection of fixtures
	 * @return an ID factory that won't generate an ID already used in the collection
	 */
	public static IDRegistrar createFactory(final Iterable<@NonNull ? extends IFixture>
												iter) {
		final IDRegistrar retval = new IDFactory();
		recursiveRegister(retval, iter);
		return retval;
	}

	/**
	 * @param idf  an IDFactory instance
	 * @param iter a collection of fixtures, all of which (recursively) should have their
	 *             IDs marked as used.
	 */
	private static void recursiveRegister(final IDRegistrar idf,
										final Iterable<@NonNull ? extends IFixture> iter) {
		for (final IFixture fix : iter) {
			final int idNum = fix.getID();
			if (idf.isIDUnused(idNum)) {
				// We don't want to set off duplicate-ID warnings for the same
				// fixture in multiple maps. Or for Mountains and the like.
				idf.register(idNum);
			}
			if (fix instanceof FixtureIterable) {
				recursiveRegister(idf, (FixtureIterable<@NonNull ?>) fix);
			}
		}
	}
}

package controller.map.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.resources.CacheFixture;
import util.LineEnd;

/**
 * A utility class to remove duplicate hills, forests, etc. from the map (to reduce the
 * size it takes up on disk and the memory and CPU it takes to deal with it).
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
public class DuplicateFixtureRemover {
	/**
	 * "Remove" (at first we just report) duplicate fixtures (i.e. hills, forests of the
	 * same kind, oases, etc.---we use TileFixture#equalsIgnoringID(TileFixture)) from
	 * every tile in a map.
	 *
	 * @param map     the map to filter
	 * @param ostream the stream to report IDs of removed fixtures on.
	 * @throws IOException on I/O error writing to stream
	 */
	public static void filter(final IMutableMapNG map, final Appendable ostream)
			throws IOException {
		for (final Point point : map.locations()) {
			filter(map, point, ostream);
		}
	}

	/**
	 * "Remove" (at first we just report) duplicate fixtures (i.e. hills, forests of the
	 * same kind, oases, etc.---we use TileFixture#equalsIgnoringID(TileFixture)) from a
	 * tile.
	 *
	 * @param map      the map
	 * @param location the location being considered now
	 * @param ostream  the stream to report IDs of removed fixtures on.
	 * @throws IOException on I/O error writing to stream
	 */
	private static void filter(final IMutableMapNG map, final Point location,
							  final Appendable ostream) throws IOException {
		final Collection<TileFixture> fixtures = new ArrayList<TileFixture>();
		final Collection<TileFixture> toRemove = new ArrayList<TileFixture>();
		// We ignore ground and forests because they don't have IDs.
		// TODO: Try to use Streams API instead of complicated loop
		for (final TileFixture fix : map.getOtherFixtures(location)) {
			if (((fix instanceof IUnit) && ((IUnit) fix).getKind().contains("TODO")) ||
						(fix instanceof CacheFixture)) {
				continue;
			}
			if (fixtures.stream()
						.anyMatch(keptFixture -> keptFixture.equalsIgnoringID(fix))) {
				ostream.append(fix.getClass().getName());
				ostream.append(' ');
				ostream.append(Integer.toString(fix.getID()));
				ostream.append(LineEnd.LINE_SEP);
				toRemove.add(fix);
			} else {
				fixtures.add(fix);
			}
		}
		for (final TileFixture fix : toRemove) {
			map.removeFixture(location, fix);
		}
	}
}
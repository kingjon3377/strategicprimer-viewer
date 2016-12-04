package controller.map.misc;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.towns.Fortress;
import util.Pair;

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
public final class DuplicateFixtureRemover {
	/**
	 * No need to instantiate.
	 */
	private DuplicateFixtureRemover() {
		// No need to instantiate.
	}
	/**
	 * "Remove" (at first we just report) duplicate fixtures (i.e. hills, forests of the
	 * same kind, oases, etc.---we use TileFixture#equalsIgnoringID(TileFixture)) from
	 * every tile in a map.
	 *
	 * @param map     the map to filter
	 * @param cli the interface to talk to the user
	 * @throws IOException on I/O error interacting with user
	 */
	public static void filter(final IMutableMapNG map, final ICLIHelper cli)
			throws IOException {
		for (final Point point : map.locations()) {
			filter(map, point, cli);
		}
	}

	/**
	 * Offer to remove duplicate fixtures (i.e. hills, forests of the same kind, oases,
	 * etc.---we use TileFixture#equalsIgnoringID(TileFixture)) from a tile. Also offer
	 * to combine resource piles that differ only in quantity.
	 *
	 * @param map      the map
	 * @param location the location being considered now
	 * @param cli	to talk to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void filter(final IMutableMapNG map, final Point location,
							  final ICLIHelper cli) throws IOException {
		final Collection<TileFixture> fixtures = new ArrayList<>();
		final Collection<TileFixture> toRemove = new ArrayList<>();
		// We ignore ground and forests because they don't have IDs.
		// TODO: Try to use Streams API instead of complicated loop
		for (final TileFixture fix : map.getOtherFixtures(location)) {
			if (((fix instanceof IUnit) && ((IUnit) fix).getKind().contains("TODO")) ||
						(fix instanceof CacheFixture)) {
				continue;
			}
			final Optional<TileFixture> matching =
					fixtures.stream().filter(match -> match.equalsIgnoringID(fix))
							.findAny();
			if (matching.isPresent() && cli.inputBooleanInSeries(String.format(
					"Remove '%s', of class '%s', ID #%d, which matches '%s', of class " +
							"'%s', ID #%d? ",
					fix.shortDesc(), fix.getClass().getSimpleName(),
					Integer.valueOf(fix.getID()), matching.get().shortDesc(),
					matching.get().getClass().getSimpleName(),
					Integer.valueOf(matching.get().getID())), "duplicate")) {
				toRemove.add(fix);
			} else {
				fixtures.add(fix);
				if (fix instanceof FixtureIterable) {
					coalesceResources((FixtureIterable<?>) fix, cli);
				}
			}
		}
		for (final TileFixture fix : toRemove) {
			map.removeFixture(location, fix);
		}
	}
	/**
	 * Offer to combine like resources in a unit or fortress.
	 * @param iter a collection of fixtures.
	 * @param cli the interface to talk to the user
	 * @throws IOException on I/O error interacting with the user
	 */
	private static void coalesceResources(final Iterable<? extends IFixture> iter,
										  final ICLIHelper cli) throws IOException {
		final Map<Pair<String, Pair<String, Pair<String, Integer>>>, List<ResourcePile>>
				resources = new HashMap<>();
		for (final IFixture fix : iter) {
			if (fix instanceof FixtureIterable) {
				coalesceResources((FixtureIterable<?>) fix, cli);
			} else if (fix instanceof ResourcePile) {
				final ResourcePile res = (ResourcePile) fix;
				final Pair<String, Pair<String, Pair<String, Integer>>> key = Pair.of(res.getKind(),
						Pair.of(res.getContents(), Pair.of(res.getUnits(),
								Integer.valueOf(res.getCreated()))));
				final List<ResourcePile> piles;
				if (resources.containsKey(key)) {
					piles = resources.get(key);
				} else {
					piles = new ArrayList<>();
					resources.put(key, piles);
				}
				piles.add(res);
			}
		}
		if (!(iter instanceof Unit) && !(iter instanceof Fortress)) {
			// We can't add items to or remove them from any other iterable
			return;
		}
		for (final List<ResourcePile> list : resources.values()) {
			if (list.size() <= 1) {
				continue;
			}
			cli.println("The following resources could be combined:");
			list.stream().map(Object::toString).forEach(cli::println);
			if (cli.inputBooleanInSeries("Combine them? ")) {
				final ResourcePile top = list.get(0);
				final ResourcePile combined =
						new ResourcePile(top.getID(), top.getKind(), top.getContents(),
												list.stream()
														.map(ResourcePile::getQuantity)
														.map
																 (DuplicateFixtureRemover::toBigDecimal)
														.reduce(BigDecimal.ZERO,
																BigDecimal::add),
												top.getUnits());
				combined.setCreated(top.getCreated());
				if (iter instanceof Unit) {
					final Unit unit = (Unit) iter;
					list.forEach(unit::removeMember);
					unit.addMember(combined);
				} else if (iter instanceof Fortress) {
					final Fortress fortress = (Fortress) iter;
					list.forEach(fortress::removeMember);
					fortress.addMember(combined);
				}
			}
		}
	}
	/**
	 * Convert a Number to a BigDecimal.
	 * @param num the number to convert
	 */
	private static BigDecimal toBigDecimal(final Number num) {
		if (num instanceof BigDecimal) {
			return (BigDecimal) num;
		} else if (num instanceof BigInteger) {
			return new BigDecimal((BigInteger) num);
		} else if (num instanceof Integer || num instanceof Long) {
			return BigDecimal.valueOf(num.longValue());
		} else if (num instanceof Float || num instanceof Double) {
			return BigDecimal.valueOf(num.doubleValue());
		} else {
			return new BigDecimal(num.toString());
		}
	}
}
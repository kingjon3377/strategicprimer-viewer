package controller.map.report;

import java.util.ArrayList;
import java.util.List;

import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import model.map.FixtureIterable;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.Fortress;
import model.report.AbstractReportNode;
import model.report.RootReportNode;
import util.DelayedRemovalMap;
import util.IntMap;
import util.NullCleaner;
import util.Pair;

/**
 * A class to produce a report based on a map for a player.
 *
 * TODO: Use an IR for lists, producing "" if empty, to simplify these methods!
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ReportGenerator {
	/**
	 * No non-static members anymore.
	 */
	private ReportGenerator() {
		// So don't instantiate.
	}

	/**
	 * @param map the map to base the report on
	 * @return the report, in HTML, as a String
	 */
	public static String createReport(final IMapNG map) {
		// The full report for the world map, as of turn 11, is 8 megs. So we
		// make a 10 meg buffer.
		final StringBuilder builder = new StringBuilder(10485760)
				.append("<html>\n");
		builder.append("<head><title>Strategic Primer map ").append(
				"summary report</title></head>\n");
		builder.append("<body>");
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
				getFixtures(map);
		final Player player = map.getCurrentPlayer();
		builder.append(new FortressReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new UnitReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new TextReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new TownReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new ExplorableReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new HarvestableReportGenerator().produce(fixtures,
				map, player));
		fixtures.coalesce();
		builder.append(new AnimalReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new VillageReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new ImmortalsReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append("</body>\n</html>\n");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			final IFixture fix = pair.second();
			if (fix instanceof Hill || fix instanceof Sandbar
					|| fix instanceof Oasis) {
				fixtures.remove(Integer.valueOf(fix.getID()));
				continue;
			}
			System.out.print("Unhandled fixture:\t");
			System.out.println(fix);
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * Creates a slightly abbreviated report, omitting the player's fortresses
	 * and units.
	 *
	 * @param map the map to base the report on
	 * @return the report, in HTML, as a string.
	 * @param player the player to report on
	 */
	public static String createAbbreviatedReport(final IMapNG map,
			final Player player) {
		// The full report for the world map, as of turn 11, is 8 megs. So we
		// make a 10 meg buffer.
		final StringBuilder builder = new StringBuilder(10485760)
				.append("<html>\n<head>");
		builder.append("<title>Strategic Primer map summary ").append(
				"abridged report</title></head>\n");
		builder.append("<body>");
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
				getFixtures(map);
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if ((pair.second() instanceof Unit || pair.second() instanceof Fortress)
					&& player.equals(((HasOwner) pair.second()).getOwner())) {
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		fixtures.coalesce();
		builder.append(new FortressReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new UnitReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new TextReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new TownReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new ExplorableReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new HarvestableReportGenerator().produce(fixtures,
				map, player));
		fixtures.coalesce();
		builder.append(new AnimalReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new VillageReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new ImmortalsReportGenerator().produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append("</body>\n</html>\n");
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param map the map to base the report on
	 * @return the report, in ReportIntermediateRepresentation
	 */
	public static AbstractReportNode createReportIR(final IMapNG map) {
		final AbstractReportNode retval = new RootReportNode(
				"Strategic Primer map summary report");
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
				getFixtures(map);
		final Player player = map.getCurrentPlayer();
		retval.add(new FortressReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new UnitReportGenerator()
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new TextReportGenerator()
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new TownReportGenerator()
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new ExplorableReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new HarvestableReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new AnimalReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new VillageReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new ImmortalsReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		return retval;
	}

	/**
	 * Creates a slightly abbreviated report, omitting the player's fortresses
	 * and units.
	 *
	 * @param map the map to base the report on
	 * @return the report, in HTML, as a string.
	 * @param player the player to report on
	 */
	public static AbstractReportNode createAbbreviatedReportIR(final IMapNG map,
			final Player player) {
		final AbstractReportNode retval = new RootReportNode(
				"Strategic Primer map summary abbreviated report");
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
				getFixtures(map);
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if ((pair.second() instanceof Unit || pair.second() instanceof Fortress)
					&& player.equals(((HasOwner) pair.second()).getOwner())) {
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		fixtures.coalesce();
		retval.add(new FortressReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new UnitReportGenerator()
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new TextReportGenerator()
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new TownReportGenerator()
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new ExplorableReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new HarvestableReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new AnimalReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new VillageReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new ImmortalsReportGenerator().produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		return retval;
	}

	/**
	 * @param map a map
	 * @return the fixtures in it, a mapping from their ID to a Pair of the
	 *         fixture's location and the fixture itself.
	 */
	private static DelayedRemovalMap<Integer, Pair<Point, IFixture>> getFixtures(
			final IMapNG map) {
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> retval =
				new IntMap<>();
		final IDFactory idf = IDFactoryFiller.createFactory(map);
		for (final Point point : map.locations()) {
			if (point == null) {
				continue;
			}
			// Because neither Forests, Mountains, nor Ground have positive IDs,
			// we can ignore everything but the "other" fixtures.
			for (final IFixture fix : getFixtures(map.getOtherFixtures(point))) {
				if (fix.getID() >= 0) {
					retval.put(NullCleaner.assertNotNull(Integer
							.valueOf(fix.getID())), Pair.of(point, fix));
				} else if (fix instanceof TextFixture) {
					retval.put(NullCleaner.assertNotNull(Integer.valueOf(idf.createID())),
							Pair.of(point, fix));
				}
			}
		}
		return retval;
	}

	/**
	 * @param iter a source of tile-fixtures
	 * @return all the tile-fixtures in it, recursively.
	 */
	private static List<IFixture> getFixtures(
			final Iterable<? extends IFixture> iter) {
		final List<IFixture> retval = new ArrayList<>();
		for (final IFixture fix : iter) {
			retval.add(fix);
			if (fix instanceof FixtureIterable) {
				retval.addAll(getFixtures((FixtureIterable<?>) fix));
			}
		}
		return retval;
	}
}

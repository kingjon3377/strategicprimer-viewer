package controller.map.report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import model.map.DistanceComparator;
import model.map.FixtureIterable;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
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
import util.PairComparator;

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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
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
	 * A simple comparator for fixtures.
	 */
	private static final Comparator<@NonNull IFixture> SIMPLE_COMPARATOR = (one, two) -> {
		if (one.equals(two)) {
			return 0;
		} else {
			return one.hashCode() - two.hashCode();
		}
	};
	/**
	 * @param map a map
	 * @param player a player
	 * @return the location of that player's HQ, or another of that player's fortresses if not found, (-1, -1) if none found
	 */
	private static Point findHQ(final IMapNG map, final Player player) {
		Point retval = PointFactory.point(-1, -1);
		for (Point location : map.locations()) {
			for (TileFixture fixture : map.getOtherFixtures(NullCleaner.assertNotNull(location))) {
				if (fixture instanceof Fortress && ((Fortress) fixture).getOwner().equals(player)) {
					if ("HQ".equals(((Fortress) fixture).getName())) {
						return location;
					} else if (location.row >= 0 && retval.row == -1) {
						retval = location;
					}
				}
			}
		}
		return retval;
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
		Point hq = findHQ(map, player);
		Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator = new PairComparator<>(new DistanceComparator(hq), SIMPLE_COMPARATOR);
		builder.append(new FortressReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new UnitReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new TextReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new TownReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new ExplorableReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new HarvestableReportGenerator(comparator).produce(fixtures,
				map, player));
		fixtures.coalesce();
		builder.append(new AnimalReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new VillageReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new ImmortalsReportGenerator(comparator).produce(fixtures, map,
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
		Point hq = findHQ(map, player);
		Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator = new PairComparator<>(new DistanceComparator(hq), SIMPLE_COMPARATOR);
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if ((pair.second() instanceof Unit || pair.second() instanceof Fortress)
					&& player.equals(((HasOwner) pair.second()).getOwner())) {
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		fixtures.coalesce();
		builder.append(new FortressReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new UnitReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new TextReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new TownReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new ExplorableReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new HarvestableReportGenerator(comparator).produce(fixtures,
				map, player));
		fixtures.coalesce();
		builder.append(new AnimalReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new VillageReportGenerator(comparator).produce(fixtures, map,
				player));
		fixtures.coalesce();
		builder.append(new ImmortalsReportGenerator(comparator).produce(fixtures, map,
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
		Point hq = findHQ(map, player);
		Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator = new PairComparator<>(new DistanceComparator(hq), SIMPLE_COMPARATOR);
		retval.add(new FortressReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new UnitReportGenerator(comparator)
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new TextReportGenerator(comparator)
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new TownReportGenerator(comparator)
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new ExplorableReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new HarvestableReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new AnimalReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new VillageReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new ImmortalsReportGenerator(comparator).produceRIR(fixtures, map,
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
		Point hq = findHQ(map, player);
		Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator = new PairComparator<>(new DistanceComparator(hq), SIMPLE_COMPARATOR);
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if ((pair.second() instanceof Unit || pair.second() instanceof Fortress)
					&& player.equals(((HasOwner) pair.second()).getOwner())) {
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		fixtures.coalesce();
		retval.add(new FortressReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new UnitReportGenerator(comparator)
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new TextReportGenerator(comparator)
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new TownReportGenerator(comparator)
				.produceRIR(fixtures, map, player));
		fixtures.coalesce();
		retval.add(new ExplorableReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new HarvestableReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new AnimalReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new VillageReportGenerator(comparator).produceRIR(fixtures, map,
				player));
		fixtures.coalesce();
		retval.add(new ImmortalsReportGenerator(comparator).produceRIR(fixtures, map,
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
				retval.addAll(getFixtures((FixtureIterable<@NonNull ?>) fix));
			}
		}
		return retval;
	}
}

package controller.map.report;

import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.DistanceComparator;
import model.map.FixtureIterable;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.Fortress;
import model.report.IReportNode;
import model.report.RootReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.DelayedRemovalMap;
import util.IntMap;
import util.Pair;
import util.PairComparator;
import view.util.SystemOut;

import static util.NullCleaner.assertNotNull;

/**
 * A class to produce a report based on a map for a player.
 *
 * TODO: Use an IR for lists, producing "" if empty, to simplify these methods!
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
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
	private static final Comparator<@NonNull IFixture> SIMPLE_COMPARATOR =
			(firstFixture, secondFixture) -> {
				if (firstFixture.equals(secondFixture)) {
					return 0;
				} else {
					if (firstFixture.hashCode() > secondFixture.hashCode()) {
						return 1;
					} else if (firstFixture.hashCode() == secondFixture.hashCode()) {
						return 0;
					} else {
						return -1;
					}
				}
			};

	/**
	 * @param map    a map
	 * @param player a player
	 * @return the location of that player's HQ, or another of that player's
	 * fortresses if
	 * not found, (-1, -1) if none found
	 */
	@SuppressWarnings("IfStatementWithIdenticalBranches")
	private static Point findHQ(final IMapNG map, final Player player) {
		Point retval = PointFactory.point(-1, -1);
		for (final Point location : map.locations()) {
			for (final TileFixture fixture : map.getOtherFixtures(
					assertNotNull(location))) {
				if ((fixture instanceof Fortress) &&
							((Fortress) fixture).getOwner().equals(player)) {
					if ("HQ".equals(((Fortress) fixture).getName())) {
						return location;
					} else if ((location.row >= 0) && (retval.row == -1)) {
						retval = location;
					}
				}
			}
		}
		return retval;
	}
	/**
	 * Produce sub-reports, appending them to the buffer and calling coalesce() on the
	 * fixtures collection after each.
	 * @param builder the buffer to write to
	 * @param fixtures the collection of fixtures to report on
	 * @param map the map being reported on
	 * @param player the current player
	 * @param generators report-generators to run
	 */
	private static void createSubReports(final StringBuilder builder,
										 final DelayedRemovalMap<Integer, Pair<Point,
																					  IFixture>> fixtures,
										 final IMapNG map, final Player player,
										 final IReportGenerator<?>... generators) {
		for (final IReportGenerator<?> generator : generators) {
			builder.append(generator.produce(fixtures, map, player));
			fixtures.coalesce();
		}
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
		final Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator =
				new PairComparator<>(new DistanceComparator(findHQ(map, player)),
											SIMPLE_COMPARATOR);
		createSubReports(builder, fixtures, map, player,
				new FortressReportGenerator(comparator),
				new UnitReportGenerator(comparator), new TextReportGenerator(comparator),
				new TownReportGenerator(comparator),
				new FortressMemberReportGenerator(comparator),
				new ExplorableReportGenerator(comparator),
				new HarvestableReportGenerator(comparator),
				new AnimalReportGenerator(comparator),
				new VillageReportGenerator(comparator),
				new ImmortalsReportGenerator(comparator));
		builder.append("</body>\n</html>\n");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			final IFixture fix = pair.second();
			if ((fix instanceof Hill) || (fix instanceof Sandbar)
						|| (fix instanceof Oasis)) {
				fixtures.remove(Integer.valueOf(fix.getID()));
				continue;
			}
			SystemOut.SYS_OUT.print("Unhandled fixture:\t");
			SystemOut.SYS_OUT.println(fix);
		}
		return assertNotNull(builder.toString());
	}

	/**
	 * Creates a slightly abbreviated report, omitting the player's fortresses and units.
	 *
	 * @param map    the map to base the report on
	 * @param player the player to report on
	 * @return the report, in HTML, as a string.
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
		final Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator =
				new PairComparator<>(new DistanceComparator(findHQ(map, player)),
											SIMPLE_COMPARATOR);

		fixtures.values().stream().filter(pair -> ((pair.second() instanceof Unit) ||
														   (pair.second() instanceof
																	Fortress))
														  && player.equals(
				((HasOwner) pair.second()).getOwner()))
				.forEach(pair -> fixtures.remove(Integer.valueOf(pair.second().getID()
				)));
		fixtures.coalesce();
		createSubReports(builder, fixtures, map, player,
				new FortressReportGenerator(comparator),
				new UnitReportGenerator(comparator), new TextReportGenerator(comparator),
				new TownReportGenerator(comparator),
				new ExplorableReportGenerator(comparator),
				new HarvestableReportGenerator(comparator),
				new FortressMemberReportGenerator(comparator),
				new AnimalReportGenerator(comparator),
				new VillageReportGenerator(comparator),
				new ImmortalsReportGenerator(comparator));
		builder.append("</body>\n</html>\n");
		return assertNotNull(builder.toString());
	}

	/**
	 * Produce sub-reports, appending them to the buffer and calling coalesce() on the
	 * fixtures collection after each.
	 * @param root the root node to add sub-reports as children of
	 * @param fixtures the collection of fixtures to report on
	 * @param map the map being reported on
	 * @param player the current player
	 * @param generators report-generators to run
	 */
	private static void createSubReportsIR(final IReportNode root,
										   final DelayedRemovalMap<Integer, Pair<Point,
																						IFixture>> fixtures,
										   final IMapNG map, final Player player,
										   final IReportGenerator<?>... generators) {
		for (final IReportGenerator<?> generator : generators) {
			root.add(generator.produceRIR(fixtures, map, player));
			fixtures.coalesce();
		}
	}
	/**
	 * @param map the map to base the report on
	 * @return the report, in ReportIntermediateRepresentation
	 */
	public static IReportNode createReportIR(final IMapNG map) {
		final IReportNode retval = new RootReportNode(
																	"Strategic Primer " +
																			"map summary" +
																			" report");
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
				getFixtures(map);
		final Player player = map.getCurrentPlayer();
		final Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator =
				new PairComparator<>(new DistanceComparator(findHQ(map, player)),
											SIMPLE_COMPARATOR);
		createSubReportsIR(retval, fixtures, map, player,
				new FortressReportGenerator(comparator),
				new UnitReportGenerator(comparator), new TextReportGenerator(comparator),
				new TownReportGenerator(comparator),
				new ExplorableReportGenerator(comparator),
				new HarvestableReportGenerator(comparator),
				new FortressMemberReportGenerator(comparator),
				new AnimalReportGenerator(comparator),
				new VillageReportGenerator(comparator),
				new ImmortalsReportGenerator(comparator));
		return retval;
	}

	/**
	 * Creates a slightly abbreviated report, omitting the player's fortresses and units.
	 *
	 * @param map    the map to base the report on
	 * @param player the player to report on
	 * @return the report, in HTML, as a string.
	 */
	public static IReportNode createAbbreviatedReportIR(final IMapNG map,
															   final Player player) {
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
				getFixtures(map);
		final Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator =
				new PairComparator<>(new DistanceComparator(findHQ(map, player)),
											SIMPLE_COMPARATOR);

		fixtures.values().stream().filter(pair -> ((pair.second() instanceof Unit) ||
														   (pair.second() instanceof
																	Fortress))
														  && player.equals(
				((HasOwner) pair.second()).getOwner()))
				.forEach(pair -> fixtures.remove(Integer.valueOf(pair.second().getID()
				)));
		fixtures.coalesce();
		final IReportNode retval =
				new RootReportNode("Strategic Primer map summary abbreviated report");
		createSubReportsIR(retval, fixtures, map, player,
				new FortressReportGenerator(comparator),
				new UnitReportGenerator(comparator), new TextReportGenerator(comparator),
				new TownReportGenerator(comparator),
				new ExplorableReportGenerator(comparator),
				new HarvestableReportGenerator(comparator),
				new FortressMemberReportGenerator(comparator),
				new AnimalReportGenerator(comparator),
				new VillageReportGenerator(comparator),
				new ImmortalsReportGenerator(comparator));
		return retval;
	}

	/**
	 * @param map a map
	 * @return the fixtures in it, a mapping from their ID to a Pair of the fixture's
	 * location and the fixture itself.
	 */
	private static DelayedRemovalMap<Integer, Pair<Point, IFixture>> getFixtures(
																						final IMapNG map) {
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> retval =
				new IntMap<>();
		final IDFactory idf = IDFactoryFiller.createFactory(map);
		for (final Point point : map.locations()) {
			// Because neither Forests, Mountains, nor Ground have positive IDs,
			// we can ignore everything but the "other" fixtures.
			retval.putAll(
					assertNotNull(getFixtures(map.streamOtherFixtures(point))
							.filter(fix -> (fix instanceof TileFixture)
									|| (fix.getID() > 0))
							.collect(Collectors.toMap(fix -> {
								if (fix instanceof TileFixture) {
									return Integer.valueOf(idf.createID());
								} else {
									return Integer.valueOf(fix.getID());
								}
							} , fix -> Pair.of(point, fix)))));
		}
		return retval;
	}

	/**
	 * @param stream a source of tile-fixtures
	 * @return all the tile-fixtures in it, recursively.
	 */
	private static Stream<IFixture> getFixtures(
														   final Stream<? extends IFixture> stream) {
		return assertNotNull(stream.flatMap(fix -> {
			if (fix instanceof FixtureIterable) {
				return Stream.concat(Stream.of(fix), getFixtures(assertNotNull(
						StreamSupport
								.stream(((FixtureIterable<@NonNull ?>) fix).spliterator(),
										false))));
			} else {
				return Stream.of(fix);
			}
		}));
	}
}

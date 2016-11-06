package controller.map.report.tabular;

import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.explorable.ExplorableFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.MobileFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.Village;
import org.eclipse.jdt.annotation.NonNull;
import util.IntMap;
import util.Pair;
import util.PatientMap;
import view.util.SystemOut;

import static util.NullCleaner.assertNotNull;

/**
 * A class to produce tabular reports based on a map for a player.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
public final class TableReportGenerator {
	/**
	 * No non-static members anymore.
	 */
	private TableReportGenerator() {
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
					} else if ((location.getRow() >= 0) && (retval.getRow() == -1)) {
						retval = location;
					}
				}
			}
		}
		return retval;
	}

	/**
	 * @param map the map to base the report on
	 * @param source a function returning an output stream for each new report.
	 * @throws IOException on I/O error while writing
	 */
	public static void createReports(final IMapNG map,
									  final Function<String, OutputStream> source) throws
			IOException {
		final PatientMap<Integer, Pair<Point, IFixture>> fixtures =
				getFixtures(map);
		final Player player = map.getCurrentPlayer();
		final Point hq = findHQ(map, player);
		try (final PrintStream out = new PrintStream(source.apply("fortresses"))) {
			new FortressTabularReportGenerator(player, hq)
					.produce(out, Fortress.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("units"))) {
			new UnitTabularReportGenerator(player, hq)
					.produce(out, IUnit.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("animals"))) {
			new AnimalTabularReportGenerator(hq).produce(out, Animal.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("workers"))) {
			new WorkerTabularReportGenerator(hq)
					.produce(out, IWorker.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("villages"))) {
			new VillageTabularReportGenerator(player, hq)
					.produce(out, Village.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("towns"))) {
			new TownTabularReportGenerator(player, hq)
					.produce(out, AbstractTown.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("crops"))) {
			new CropTabularReportGenerator(hq).produce(out, TileFixture.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("minerals"))) {
			new DiggableTabularReportGenerator(hq)
					.produce(out, TileFixture.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("resources"))) {
			new ResourceTabularReportGenerator().produce(out, IFixture.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("immortals"))) {
			new ImmortalsTabularReportGenerator(hq)
					.produce(out, MobileFixture.class, fixtures);
		}
		try (final PrintStream out = new PrintStream(source.apply("explorables"))) {
			new ExplorableTabularReportGenerator(player, hq)
					.produce(out, ExplorableFixture.class, fixtures);
		}
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
	}

	/**
	 * @param map a map
	 * @return the fixtures in it, a mapping from their ID (or a synthetic one, for
	 * fixtures without a unique ID) to a Pair of the fixture's location and the fixture
	 * itself.
	 */
	private static PatientMap<Integer, Pair<Point, IFixture>> getFixtures(final IMapNG
																				  map) {
		final PatientMap<Integer, Pair<Point, IFixture>> retval = new IntMap<>();
		final IDRegistrar idf = IDFactoryFiller.createFactory(map);
		for (final Point point : map.locations()) {
			retval.putAll(
					assertNotNull(getFixtures(map.streamOtherFixtures(point))
										  .filter(fix -> (fix instanceof TileFixture)
																 || (fix.getID() > 0))
										  .collect(Collectors.toMap(fix -> {
											  if ((fix instanceof TileFixture) &&
														  (fix.getID() < 0)) {
												  return Integer.valueOf(idf.createID());
											  } else {
												  return Integer.valueOf(fix.getID());
											  }
										  } , fix -> Pair.of(point, fix)))));
			final Ground ground = map.getGround(point);
			final Forest forest = map.getForest(point);
			if (ground != null) {
				retval.put(Integer.valueOf(idf.createID()), Pair.of(point, ground));
			}
			if (forest != null) {
				retval.put(Integer.valueOf(forest.getID()), Pair.of(point, forest));
			}
		}
		return retval;
	}

	/**
	 * @param stream a source of tile-fixtures
	 * @return all the tile-fixtures in it, recursively.
	 */
	private static Stream<IFixture> getFixtures(final Stream<? extends IFixture>
														stream) {
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


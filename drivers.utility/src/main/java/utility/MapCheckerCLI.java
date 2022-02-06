package utility;

import common.map.fixtures.towns.CommunityStats;
import common.map.fixtures.FixtureIterable;
import java.util.Arrays;
import common.map.TileFixture;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.javatuples.Pair;
import java.io.IOException;
import java.nio.file.Paths;

import lovelace.util.MissingFileException;
import lovelace.util.MalformedXMLException;

import java.nio.file.Path;

import drivers.common.UtilityDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import common.map.HasExtent;
import common.map.IFixture;
import common.map.Player;
import common.map.HasOwner;
import common.map.Point;
import common.map.TileType;
import common.map.IMapNG;

import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.AnimalTracks;

import common.map.fixtures.mobile.worker.IJob;
import static common.map.fixtures.mobile.worker.IJob.SUSPICIOUS_SKILLS;
import common.map.fixtures.mobile.worker.ISkill;

import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.StoneKind;
import common.map.fixtures.resources.Grove;

import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.ITownFixture;

import impl.xmlio.MapIOHelper;

import common.xmlio.Warning;
import common.xmlio.SPFormatException;

import common.map.fixtures.IResourcePile;

import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Oasis;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A driver to check every map file in a list for errors.
 */
public class MapCheckerCLI implements UtilityDriver {
	private static final Logger LOGGER = Logger.getLogger(MapCheckerCLI.class.getName());
	/**
	 * An interface for checks of a map's <em>contents</em> that we don't want the
	 * XML-<em>reading</em> code to do. Checkers should return true iff they
	 * report at least one warning.
	 */
	@FunctionalInterface
	private static interface Checker {
		/**
		 * @param terrain the terrain at a point
		 * @param location the location being checked
		 * @param fixture the fixture at that location being checked
		 * @param warner the Warning instance to report specific errors on
		 * @return true iff at least one warning was reported
		 */
		boolean check(TileType terrain, Point location, IFixture fixture, Warning warner);
	}

	private static class SPContentWarning extends Exception {
		public SPContentWarning(final Point context, final String message) {
			super(String.format("At %s: %s", context, message));
		}
	}

	private static class OwnerChecker {
		private final IMapNG map;
		public OwnerChecker(final IMapNG map) {
			this.map = map;
		}

		public boolean check(final TileType terrain, final Point context, final IFixture fixture, final Warning warner) {
			boolean retval = false;
			if (fixture instanceof HasOwner) {
				if (((HasOwner) fixture).getOwner().getName().trim().isEmpty()) {
					warner.handle(new SPContentWarning(context,
						String.format("Fixture owned by %s, who has no name",
							((HasOwner) fixture).getOwner())));
					retval = true;
				}
				if (StreamSupport.stream(map.getPlayers().spliterator(), true)
						.mapToInt(Player::getPlayerId)
						.noneMatch(n -> ((HasOwner) fixture)
							.getOwner().getPlayerId() == n)) {
					warner.handle(new SPContentWarning(context, String.format(
						"Fixture owned by %s, not known by the map",
						((HasOwner) fixture).getOwner())));
					retval = true;
				}
			}
			return retval;
		}
	}

	private static boolean lateriteChecker(final TileType terrain, final Point context, final IFixture fixture,
	                                       final Warning warner) {
		if (fixture instanceof StoneDeposit &&
				StoneKind.Laterite.equals(((StoneDeposit) fixture).getStone()) &&
				!TileType.Jungle.equals(terrain)) {
			warner.handle(new SPContentWarning(context, "Laterite stone in non-jungle"));
			return true;
		} else {
			return false;
		}
	}

	private static boolean oasisChecker(final TileType terrain, final Point context, final IFixture fixture,
	                                    final Warning warner) {
		if (fixture instanceof Oasis && !TileType.Desert.equals(terrain)) {
			warner.handle(new SPContentWarning(context, "Oasis in non-desert"));
			return true;
		} else {
			return false;
		}
	}

	private static boolean animalTracksChecker(final TileType terrain, final Point context, final IFixture fixture,
	                                           final Warning warner) {
		if (fixture instanceof AnimalTracks) {
			warner.handle(new SPContentWarning(context,
				"Animal tracks in map suspected to be main"));
			return true;
		} else {
			return false;
		}
	}

	private static boolean aquaticVillageChecker(final TileType terrain, final Point context,
	                                             final IFixture fixture, final Warning warner) {
		if (fixture instanceof Village &&
				LandRaces.LAND_RACES.contains(((Village) fixture).getRace()) &&
				TileType.Ocean.equals(terrain)) {
			warner.handle(new SPContentWarning(context, "Aquatic village has non-aquatic race"));
			return true;
		} else {
			return false;
		}
	}

	private static boolean suspiciousSkill(final IJob job) {
		if (StreamSupport.stream(job.spliterator(), false).count() > 1) {
			return false;
		} else {
			return StreamSupport.stream(job.spliterator(), true)
				.map(ISkill::getName).anyMatch(SUSPICIOUS_SKILLS::contains);
		}
	}

	private static boolean suspiciousSkillCheck(final TileType terrain, final Point context, final IFixture fixture,
	                                            final Warning warner) {
		boolean retval = false;
		if (fixture instanceof IWorker) {
			if (StreamSupport.stream(((IWorker) fixture).spliterator(), true)
					.anyMatch(MapCheckerCLI::suspiciousSkill)) {
				warner.handle(new SPContentWarning(context,
					((IWorker) fixture).getName() +
						" has a Job with one suspiciously-named Skill"));
				retval = true;
			}
			for (IJob job : (IWorker) fixture) {
				for (ISkill skill : job) {
					if ("miscellaneous".equals(skill.getName()) &&
							skill.getLevel() > 0) {
						warner.handle(new SPContentWarning(context,
							((IWorker) fixture).getName() +
								" has a level in 'miscellaneous'"));
						return true;
					}
				}
			}
		}
		return retval;
	}

	private static final List<String> PLACEHOLDER_KINDS = Collections.unmodifiableList(Arrays.asList(
		"various", "unknown"));

	private static final List<String> PLACEHOLDER_UNITS = Collections.unmodifiableList(Arrays.asList(
		"unit", "units"));

	private static boolean resourcePlaceholderChecker(final TileType terrain, final Point context,
	                                                  final IFixture fixture, final Warning warner) {
		if (fixture instanceof IResourcePile) {
			if (PLACEHOLDER_KINDS.contains(((IResourcePile) fixture).getKind())) {
				warner.handle(new SPContentWarning(context, String.format(
					"Resource pile, ID #%d, has placeholder kind: %s",
					fixture.getId(), ((IResourcePile) fixture).getKind())));
			} else if (PLACEHOLDER_KINDS.contains(((IResourcePile) fixture).getContents())) {
				warner.handle(new SPContentWarning(context, String.format(
					"Resource pile, ID #%d, has placeholder contents: %s",
					fixture.getId(), ((IResourcePile) fixture).getContents())));
			} else if (PLACEHOLDER_UNITS.contains(((IResourcePile) fixture)
					.getQuantity().getUnits())) {
				warner.handle(new SPContentWarning(context, String.format(
					"Resource pile, ID #%d, has placeholder units", fixture.getId())));
			} else if (((IResourcePile) fixture).getContents().contains("#")) {
				warner.handle(new SPContentWarning(context, String.format(
					"Resource pile, ID #%d, has suspicous contents: %s",
					fixture.getId(), ((IResourcePile) fixture).getContents())));
			} else {
				return false;
			}
			return true;
		} else if (fixture instanceof ITownFixture &&
				((ITownFixture) fixture).getPopulation() != null) {
			CommunityStats stats = ((ITownFixture) fixture).getPopulation();
			boolean retval = false;
			for (IResourcePile resource : stats.getYearlyConsumption()) {
				retval = resourcePlaceholderChecker(terrain, context, resource, warner)
					|| retval;
			}
			for (IResourcePile resource : stats.getYearlyProduction()) {
				retval = resourcePlaceholderChecker(terrain, context, resource, warner)
					|| retval;
			}
			return retval;
		} else {
			return false;
		}
	}

	private static boolean positiveAcres(final HasExtent<?> item) {
		return item.getAcres().doubleValue() > 0.0;
	}

	private static boolean acreageChecker(final Point context, final Warning warner,
	                                      final Collection<? extends IFixture> fixtures) {
		double total = 0.0;
		boolean retval = false;
		for (HasExtent<?> fixture : fixtures.stream()
				.filter(HasExtent.class::isInstance).map(HasExtent.class::cast)
				.filter(MapCheckerCLI::positiveAcres)
				.collect(Collectors.toList())) { // TODO: convert to forEach() to avoid collector?
			total += fixture.getAcres().doubleValue();
		}
		if (total > 160.0) {
			warner.handle(new SPContentWarning(context, String.format(
				"More explicit acres (%0.1f) than tile should allow", total)));
			return true;
		}
		for (ITownFixture fixture : fixtures.stream()
				.filter(ITownFixture.class::isInstance).map(ITownFixture.class::cast)
				.collect(Collectors.toList())) { // TODO: try to avoid collector
			switch (fixture.getTownSize()) {
			case Small:
				total += 15;
				break;
			case Medium:
				total += 40;
				break;
			case Large:
				total += 80;
				break;
			default:
				throw new IllegalStateException("Exhaustive switch wasn't");
			}
		}
		total += (fixtures.stream()
			.filter(Grove.class::isInstance).map(Grove.class::cast)
			.mapToInt(Grove::getPopulation).filter(p -> p > 0).sum() / 500.0);
		if (total > 160.0) {
			warner.handle(new SPContentWarning(context, String.format(
				"Counting towns and groves, more acres (%0.1f) used than tile should allow",
				total)));
			return true;
		} else {
			return retval;
		}
	}

	private static final List<Checker> EXTRA_CHECKS = Collections.unmodifiableList(Arrays.asList(
		MapCheckerCLI::lateriteChecker, MapCheckerCLI::aquaticVillageChecker,
		MapCheckerCLI::suspiciousSkillCheck,
		MapCheckerCLI::resourcePlaceholderChecker, MapCheckerCLI::oasisChecker));

	private static boolean contentCheck(final Checker checker, final TileType terrain, final Point context,
	                                    final Warning warner, final Iterable<? extends IFixture> list) {
		boolean retval = false;
		for (IFixture fixture : list) {
			if (fixture instanceof FixtureIterable) {
				retval = contentCheck(checker, terrain, context, warner,
					(FixtureIterable<?>) fixture) || retval;
			}
			retval = checker.check(terrain, context, fixture, warner) || retval;
		}
		return retval;
	}

	private final Consumer<String> stdout;
	private final Consumer<String> stderr;

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	public MapCheckerCLI(final Consumer<String> stdout, final Consumer<String> stderr) {
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public void check(final Path file) {
		check(file, new Warning(System.out::println, true));
	}

	public void check(final Path file, final Warning warner) {
		stdout.accept("Starting " + file);
		IMapNG map;
		try {
			map = MapIOHelper.readMap(file, warner);
		} catch (final MissingFileException except) {
			stderr.accept(file + " not found");
			LOGGER.severe(file + " not found");
			LOGGER.log(Level.FINE, "Full stack trace of file-not-found:", except);
			return;
		} catch (final IOException except) {
			stderr.accept("I/O error reading " + file);
			LOGGER.severe(String.format("I/O error reading %s: %s", file, except.getMessage()));
			LOGGER.log(Level.FINE, "Full stack trace of I/O error", except);
			return;
		} catch (final MalformedXMLException except) {
			stderr.accept("Malformed XML in " + file);
			LOGGER.severe(String.format("Malformed XML in %s: %s", file, except.getMessage()));
			LOGGER.log(Level.FINE, "Full stack trace of malformed-XML error", except);
			return;
		} catch (final SPFormatException except) {
			stderr.accept("SP map format error in " + file);
			LOGGER.severe(String.format("SP map format error in %s: %s", file,
				except.getMessage()));
			LOGGER.log(Level.FINE, "Full stack trace of SP map format error:", except);
			return;
		}

		boolean result = false;
		for (Checker checker : Stream.concat(Stream.of(new OwnerChecker(map)::check),
				EXTRA_CHECKS.stream()).collect(Collectors.toList())) {
			for (Point location : map.getLocations()) {
				if (map.getBaseTerrain(location) != null) {
					result = contentCheck(checker, map.getBaseTerrain(location),
						location, warner, map.getFixtures(location)) || result;
				}
			}
			LOGGER.fine("Finished a check for " + file);
		}

		for (Point location : map.getLocations()) {
			if (map.getBaseTerrain(location) != null) {
				// TODO: Make acreageChecker() take terrain so
				// this can go in the same loop as above
				result = acreageChecker(location, warner, map.getFixtures(location))
					|| result;
			}
			if (map.isMountainous(location) &&
					map.getFixtures(location).stream().anyMatch(Hill.class::isInstance)) {
				warner.handle(new SPContentWarning(location, "Hill in mountainous tile"));
				result = true;
			}
		}

		if (file.toString().contains("world_turn")) {
			for (Pair<Point, TileFixture> pair : map.streamLocations()
						.flatMap(l -> map.getFixtures(l).stream()
							.map(f -> Pair.with(l, f)))
						.collect(Collectors.toList())) {
				Point location = pair.getValue0();
				TileFixture fixture = pair.getValue1();
				if (map.getBaseTerrain(location) != null) {
					result = animalTracksChecker(map.getBaseTerrain(location),
						location, fixture, warner) || result;
				}
			}
		}

		LOGGER.fine("Finished with " + file);
		if (result) {
			stdout.accept("... done");
		} else {
			stdout.accept("No errors in " + file);
		}
	}

	@Override
	public void startDriver(final String... args) {
		// TODO: Convert to stream/functional form?
		for (String filename : args) {
			if (filename == null) {
				continue;
			}
			check(Paths.get(filename));
		}
	}
}

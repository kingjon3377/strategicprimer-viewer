package drivers.generators;

import java.io.IOException;

import drivers.exploration.old.MissingTableException;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.function.IntSupplier;

import drivers.common.cli.ICLIHelper;

import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.towns.ITownFixture;
import common.map.fixtures.towns.TownStatus;
import legacy.map.fixtures.towns.CommunityStats;
import legacy.map.fixtures.towns.AbstractTown;
import legacy.map.fixtures.towns.Village;
import common.map.fixtures.towns.TownSize;

import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.TileType;
import legacy.map.ILegacyMap;

import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.ResourcePileImpl;

import legacy.idreg.IDRegistrar;

import legacy.map.fixtures.resources.HarvestableFixture;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Shrub;

import exploration.common.SurroundingPointIterable;

import java.util.Random;

import drivers.exploration.old.ExplorationRunner;

import legacy.map.fixtures.terrain.Forest;

import java.util.LinkedList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import lovelace.util.FileContentsReader;
import lovelace.util.LovelaceLogger;
import lovelace.util.NumParsingHelper;
import org.javatuples.Triplet;
import org.javatuples.Pair;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * A command-line app to generate population details for villages.
 */
/* package */ class TownGenerator {
	public TownGenerator(final ICLIHelper cli) throws MissingTableException, IOException {
		this.cli = cli;
		runner = initProduction(); // TODO: pull its contents up?
		consumption = initConsumption(); // TODO: inline that?
	}

	private final ICLIHelper cli;

	/**
	 * Load consumption possibilities from file.
	 */
	private static Map<String, List<Triplet<LegacyQuantity, String, String>>> initConsumption() throws IOException {
		final Map<String, List<Triplet<LegacyQuantity, String, String>>> retval = new HashMap<>();
		for (final String terrain : Arrays.asList("mountain", "forest", "plains", "ocean")) {
			final String file = terrain + "_consumption";
			final Iterable<String> tableContents =
					FileContentsReader.readFileContents(TownGenerator.class, Paths.get("tables", file));
			final List<Triplet<LegacyQuantity, String, String>> inner = new ArrayList<>();
			for (final String line : tableContents) {
				if (line.isEmpty()) {
					continue;
				}
				final String[] split = line.split("\t");
				final int quantity = Integer.parseInt(split[0]);
				final String units = split[1];
				final String kind = split[2];
				final String resource = split[3];
				inner.add(Triplet.with(new LegacyQuantity(quantity, units), kind, resource));
			}
			retval.put(terrain, Collections.unmodifiableList(inner));
		}
		return Collections.unmodifiableMap(retval);
	}

	/**
	 * Load production possibilities from file.
	 */
	private static ExplorationRunner initProduction() throws MissingTableException, IOException {
		final ExplorationRunner retval = new ExplorationRunner();
		final Deque<String> firstTables = new LinkedList<>(Arrays.asList("mountain_skills",
				"forest_skills", "plains_skills", "ocean_skills"));
		final Deque<String> secondTables = new LinkedList<>();
		while (!firstTables.isEmpty()) {
			final String table = firstTables.removeFirst();
			retval.loadTableFromFile(TownGenerator.class, table);
			for (final String reference : retval.getTableContents(table)) {
				if (reference.contains("#")) {
					final String temp = reference.split("#", 2)[1];
					if (!retval.hasTable(temp)) {
						firstTables.addFirst(temp.strip());
					}
				} else if (!reference.isBlank()) {
					secondTables.addFirst(reference + "_production"); // TODO: should this be firstTables instead?
				}
			}
		}
		while (!secondTables.isEmpty()) {
			final String table = secondTables.removeFirst();
			retval.loadTableFromFile(TownGenerator.class, table);
			for (final String reference : retval.getTableContents(table)) {
				if (reference.contains("#")) {
					final String temp = reference.split("#", 2)[1];
					if (!retval.hasTable(temp)) {
						secondTables.addFirst(temp.strip());
					}
				}
			}
		}
		return retval;
	}

	private final Map<String, List<Triplet<LegacyQuantity, String, String>>> consumption;
	private final ExplorationRunner runner;

	/**
	 * The (for now active) towns in the given map that don't have 'stats'
	 * yet. In Ceylon the "town" type was the alias
	 * {@code ModifiableTown}, defined as the union of {@link
	 * AbstractTown} and {@link Village}, but we can neither define an
	 * alias nor use a union type in Java and so use the nearest supertype,
	 * {@link ITownFixture}.
	 */
	static List<Pair<Point, ITownFixture>> unstattedTowns(final ILegacyMap map) {
		return map.streamLocations()
				.flatMap(l -> map.getFixtures(l).stream().filter(ITownFixture.class::isInstance)
						.map(ITownFixture.class::cast)
						.filter(t -> TownStatus.Active == t.getStatus())
						.map(f -> Pair.with(l, f)))
				.collect(Collectors.toList());
	}

	/**
	 * Get the fixture in the given map identified by the given ID number.
	 *
	 * TODO: search inside fortresses and units
	 */
	private static @Nullable IFixture findByID(final ILegacyMap map, final int id) {
		return map.streamAllFixtures()
				.filter(f -> f.getId() == id)
				.findAny().orElse(null);
	}

	/**
	 * Find the location in the given map of the fixture identified
	 * by the given ID number.
	 *
	 * TODO: search inside fortresses and units
	 */
	private static @Nullable Point findLocById(final ILegacyMap map, final int id) {
		return map.streamLocations()
				.filter(l -> map.getFixtures(l).stream().anyMatch(f -> f.getId() == id))
				.findAny().orElse(null);
	}

	/**
	 * Whether, in the given map, any town claims a resource
	 * identified by the given ID number.
	 */
	private static boolean isClaimedField(final ILegacyMap map, final int id) {
		return map.streamAllFixtures()
				.filter(ITownFixture.class::isInstance).map(ITownFixture.class::cast)
				.map(ITownFixture::getPopulation).filter(Objects::nonNull)
				.flatMap(t -> t.getWorkedFields().stream())
				.anyMatch(n -> id == n);
	}

	/**
	 * Whether, in the given map, the given ID number
	 * refers to {@link HarvestableFixture a resource that can be worked}
	 * that {@link #isClaimedField is presently unclaimed}.
	 */
	private static boolean isUnclaimedField(final ILegacyMap map, final int id) {
		return !isClaimedField(map, id) && findByID(map, id) instanceof HarvestableFixture;
	}

	/**
	 * If both arguments exist and are ocean, return true; if one is ocean
	 * and the other is not, return false; otherwise, return true.
	 */
	private static boolean bothOrNeitherOcean(final @Nullable TileType one, final @Nullable TileType two) {
		if (TileType.Ocean == one) {
			return TileType.Ocean == two;
		} else {
			return TileType.Ocean != two;
		}
	}

	/**
	 * Whether the given fixture is actually claimable: an
	 * unexposed mineral vein, an uncultivated field or meadow, an
	 * uncultivated grove or orchard, an abandoned mine, or a cache is not claimable.
	 */
	private static boolean isReallyClaimable(final HarvestableFixture fix) {
		if (fix instanceof final MineralVein mv) {
			return mv.isExposed();
		} else if (fix instanceof final Meadow m) {
			return m.isCultivated();
		} else if (fix instanceof final Grove g) {
			return g.isCultivated();
		} else if (fix instanceof final Mine m) {
			return TownStatus.Active == m.getStatus();
		} else if (fix instanceof CacheFixture) {
			return false;
		} else if (fix instanceof Shrub || fix instanceof StoneDeposit) {
			return true;
		} else {
			LovelaceLogger.error("Unhandled harvestable type");
			return false;
		}
	}

	/**
	 * Find the nearest claimable resources to the given location.
	 */
	private static List<HarvestableFixture> findNearestFields(final ILegacyMap map, final Point location) {
		final TileType base = map.getBaseTerrain(location);
		if (Objects.isNull(base)) {
			return Collections.emptyList();
		} else {
			return new SurroundingPointIterable(location,
					map.getDimensions(), 10).stream().distinct()
					.filter(l -> bothOrNeitherOcean(base, map.getBaseTerrain(l)))
					.flatMap(l -> map.getFixtures(l).stream())
					.filter(HarvestableFixture.class::isInstance)
					.map(HarvestableFixture.class::cast).filter(TownGenerator::isReallyClaimable)
					.collect(Collectors.toList());
		}
	}

	/**
	 * Have the user enter expertise levels and claimed resources for a town.
	 */
	private static CommunityStats enterStats(final ICLIHelper cli, final IDRegistrar idf, final ILegacyMap map, final Point location,
			/*ModifiableTown*/ final ITownFixture town) {
		final CommunityStats retval = new CommunityStats(Optional.ofNullable(
				cli.inputNumber("Population: ")).orElse(0));
		cli.println("Now enter Skill levels, the highest in the community for each Job.");
		cli.println("(Empty to end.)");
		while (true) {
			final String job = cli.inputString("Job: ");
			if (Objects.isNull(job) || job.isEmpty()) {
				break;
			}
			final Integer level = cli.inputNumber("Level: ");
			if (Objects.isNull(level)) {
				break;
			}
			retval.setSkillLevel(job, level);
		}

		cli.println("Now enter ID numbers of worked fields (empty to skip).");
		final List<HarvestableFixture> nearestFields = new ArrayList<>(findNearestFields(map, location));
		while (true) {
			final String input = Objects.requireNonNull(cli.inputString("Field ID #: "));
			final int field;
			if (input.isEmpty()) {
				break;
			} else if (NumParsingHelper.isNumeric(input)) {
				field = NumParsingHelper.parseInt(input).orElseThrow(
						() -> new IllegalStateException(
								"Failed to parse after we determined input was numeric"));
			} else if ("nearest".equalsIgnoreCase(input) && !nearestFields.isEmpty()) {
				final HarvestableFixture nearest = nearestFields.removeFirst();
				cli.println("Nearest harvestable fixture is as follows:");
				cli.println(nearest.getShortDescription());
				field = nearest.getId();
			} else {
				cli.println("Invalid input");
				continue;
			}
			final Point fieldLoc = findLocById(map, field); // TODO: This wasn't initialized until isClaimedField() check in Ceylon, but a variable can't be declared in an if statement in Java
			if (isClaimedField(map, field)) {
				cli.println("That field is already worked by another town");
			} else if (!Objects.isNull(fieldLoc)) {
				if (!bothOrNeitherOcean(map.getBaseTerrain(location),
						map.getBaseTerrain(fieldLoc))) {
					if (TileType.Ocean == map.getBaseTerrain(location)) {
						cli.println(
								"That would be a land resource worked by an aquatic town.");
					} else {
						cli.println(
								"That would be an ocean resource worked by a town on land.");
					}
					// TODO: Handle EOF (here and elsewhere) more gracefully
					if (!cli.inputBooleanInSeries("Are you sure? ", "aquatic")) {
						continue;
					}
				}
				if (isUnclaimedField(map, field)) {
					retval.addWorkedField(field);
				} else {
					cli.println("That is not the ID of a resource a town can work.");
				}
			} else {
				cli.println("That is not the ID of a resource in the map.");
			}
		}

		cli.println("Now add resources produced each year. (Empty to end.)");
		while (true) {
			final String kind = cli.inputString("General kind of resource: ");
			// N.B. inputString() trims leading and trailing whitespace.
			if (Objects.isNull(kind) || kind.isEmpty()) {
				break;
			}
			final String contents = cli.inputString("Specific kind of resource: ");
			if (Objects.isNull(contents) || contents.isEmpty()) {
				break;
			}
			final BigDecimal quantity = cli.inputDecimal("Quantity of the resource produced: ");
			if (Objects.isNull(quantity)) {
				break;
			}
			final String units = cli.inputString("Units of that quantity: ");
			if (Objects.isNull(units)) { // TODO: What about empty units?
				break;
			}
			final IResourcePile pile = new ResourcePileImpl(idf.createID(), kind, contents,
					new LegacyQuantity(quantity, units));
			retval.getYearlyProduction().add(pile);
		}

		cli.println("Now add resources consumed each year. (Empty to end.)");
		while (true) {
			final String kind = cli.inputString("General kind of resource: ");
			// N.B. inputString() trims leading and trailing whitespace.
			if (Objects.isNull(kind) || kind.isEmpty()) {
				break;
			}
			final String contents = cli.inputString("Specific kind of resource: ");
			if (Objects.isNull(contents) || contents.isEmpty()) {
				break;
			}
			final BigDecimal quantity = cli.inputDecimal("Quantity of the resource produced: ");
			if (Objects.isNull(quantity)) {
				break;
			}
			final String units = cli.inputString("Units of that quantity: ");
			if (Objects.isNull(units)) { // TODO: What about empty units?
				break;
			}
			retval.getYearlyConsumption().add(new ResourcePileImpl(idf.createID(), kind,
					contents, new LegacyQuantity(quantity, units)));
		}

		return retval;
	}

	/**
	 * What general kind of thing the given harvestable fixture will produce each year.
	 *
	 * TODO: Provide and use lookup tables for specific crops to avoid miscategorizations
	 */
	private static String getHarvestableKind(final HarvestableFixture fixture) {
		return switch (fixture) {
			case final Grove g -> (g.isOrchard()) ? "food" : "wood";
			case final Meadow m -> (m.isField()) ? "food" : "fodder";
			case final MineralVein mineralVein -> "mineral";
			case final StoneDeposit stoneDeposit -> "stone";
			default -> "unknown"; // TODO: log this case?
		};
	}

	/**
	 * What specific resource the given harvestable fixture will produce.
	 */
	private static String getHarvestedProduct(final HarvestableFixture fixture) {
		return fixture.getKind();
	}

	@FunctionalInterface
	private interface IntToIntFunction {
		int apply(int num);
	}

	@FunctionalInterface
	private interface RepeatedRoller {
		default int repeatedlyRoll(final int count, final int die) {
			return repeatedlyRoll(count, die, 0);
		}

		int repeatedlyRoll(int count, int die, int addend);
	}

	/**
	 * Generate expertise and production and consumption data for the given town.
	 *
	 * Note that in Ceylon the type of "town" was {@code AbstractTown|Village},
	 * excluding fortresses.
	 *
	 * To ensure consistency between runs of this algorithm, we seed the
	 * random number generator with the town's ID.
	 */
	private CommunityStats generateStats(final IDRegistrar idf, final Point location, final ITownFixture town,
										 final ILegacyMap map) throws MissingTableException {
		final Random rng = new Random(town.getId());
		// A die roll using our pre-seeded RNG.
		final IntToIntFunction roll = (die) -> rng.nextInt(die) + 1;

		// Repeatedly roll our pre-seeded RNG-die, optionally adding a constant value.
		final RepeatedRoller repeatedlyRoll = (count, die, addend) -> {
			int sum = addend;
			for (int i = 0; i < count; i++) {
				sum += roll.apply(die);
			}
			return sum;
		};

		final int population;
		final int skillCount;
		final IntSupplier skillLevelSource;
		final int resourceCount;
		if (town instanceof Village) {
			if (TownSize.Small != town.getTownSize()) {
				throw new IllegalStateException(
						"Don't know how to handle non-small villages");
			}
			population = repeatedlyRoll.repeatedlyRoll(3, 8, 3);
			skillCount = repeatedlyRoll.repeatedlyRoll(2, 4);
			skillLevelSource = () -> repeatedlyRoll.repeatedlyRoll(4, 3, -3);
			resourceCount = repeatedlyRoll.repeatedlyRoll(2, 3);
		} else if (town instanceof AbstractTown) {
			switch (town.getTownSize()) {
				case Small -> {
					population = repeatedlyRoll.repeatedlyRoll(4, 10, 5);
					skillCount = repeatedlyRoll.repeatedlyRoll(3, 4);
					skillLevelSource = () -> repeatedlyRoll.repeatedlyRoll(2, 6);
					resourceCount = repeatedlyRoll.repeatedlyRoll(2, 3);
				}
				case Medium -> {
					population = repeatedlyRoll.repeatedlyRoll(20, 20, 50);
					skillCount = repeatedlyRoll.repeatedlyRoll(4, 6);
					skillLevelSource = () -> repeatedlyRoll.repeatedlyRoll(3, 6);
					resourceCount = repeatedlyRoll.repeatedlyRoll(2, 6);
				}
				case Large -> {
					population = repeatedlyRoll.repeatedlyRoll(23, 100, 200);
					skillCount = repeatedlyRoll.repeatedlyRoll(6, 8);
					skillLevelSource = () -> repeatedlyRoll.repeatedlyRoll(3, 8);
					resourceCount = repeatedlyRoll.repeatedlyRoll(4, 6);
				}
				default -> throw new IllegalStateException("Non-exhaustive switch");
			}
		} else {
			throw new IllegalStateException("Unhandled town type");
		}

		final CommunityStats retval = new CommunityStats(population);
		final String skillTable;
		final String consumptionTableName;
		final TileType terrain = map.getBaseTerrain(location);
		if (Objects.isNull(terrain)) {
			skillTable = "plains_skills";
			consumptionTableName = "plains";
		} else if (TileType.Ocean == terrain) {
			skillTable = "ocean_skills";
			consumptionTableName = "ocean";
		} else if (map.isMountainous(location)) {
			skillTable = "mountain_skills";
			consumptionTableName = "mountain";
		} else if (map.getFixtures(location).stream().anyMatch(Forest.class::isInstance)) {
			skillTable = "forest_skills";
			consumptionTableName = "forest";
		} else {
			skillTable = "plains_skills";
			consumptionTableName = "plains";
		}

		for (int i = 0; i < skillCount; i++) {
			final String skill = runner.recursiveConsultTable(skillTable, location,
					map.getBaseTerrain(location), map.isMountainous(location),
					map.getFixtures(location), map.getDimensions());
			final int level = skillLevelSource.getAsInt();
			if (Optional.ofNullable(retval.getHighestSkillLevels().get(skill)).orElse(0)
					< level) {
				retval.setSkillLevel(skill, level);
			}
		}

		final List<HarvestableFixture> workedFields = findNearestFields(map, location).stream()
				.limit(resourceCount).toList();
		for (final HarvestableFixture field : workedFields) {
			retval.addWorkedField(field.getId());
			retval.getYearlyProduction().add(new ResourcePileImpl(idf.createID(),
					getHarvestableKind(field), getHarvestedProduct(field),
					new LegacyQuantity(1, "unit")));
		}

		for (final Map.Entry<String, Integer> entry : retval.getHighestSkillLevels().entrySet()) {
			final String skill = entry.getKey();
			final int level = entry.getValue();
			final String tableName = skill + "_production";
			if (runner.hasTable(tableName)) {
				try {
					retval.getYearlyProduction().add(new ResourcePileImpl(
							idf.createID(), "unknown",
							runner.consultTable(tableName, location,
									map.getBaseTerrain(location), map.isMountainous(location),
									map.getFixtures(location), map.getDimensions()),
							new LegacyQuantity(Math.pow(2, level - 1),
									(level == 1) ? "unit" : "units")));
				} catch (final MissingTableException except) {
					LovelaceLogger.warning(except, "Missing table");
					retval.getYearlyProduction().add(new ResourcePileImpl(
							idf.createID(), "unknown", "product of " + skill,
							new LegacyQuantity(1, "unit")));
				}
			} else {
				retval.getYearlyProduction().add(new ResourcePileImpl(idf.createID(),
						"unknown", "product of " + skill, new LegacyQuantity(1, "unit")));
			}
		}

		if (!consumption.containsKey(consumptionTableName)) {
			throw new IllegalStateException("Appropriate consumption table missing");
		}
		final List<Triplet<LegacyQuantity, String, String>> consumptionTable =
				consumption.get(consumptionTableName);
		for (final Triplet<LegacyQuantity, String, String> triplet : consumptionTable) {
			retval.getYearlyConsumption().add(new ResourcePileImpl(idf.createID(),
					triplet.getValue1(), triplet.getValue2(), triplet.getValue0()));
		}

		retval.getYearlyConsumption().add(new ResourcePileImpl(idf.createID(), "food", "various",
				new LegacyQuantity(4 * 14 * population, "pounds")));
		return retval;
	}

	/**
	 * Allow the user to create population details for specific towns.
	 */
	public void generateSpecificTowns(final IDRegistrar idf, final PopulationGeneratingModel model) {
		while (true) {
			final String input = cli.inputString("ID or name of town to create stats for: ");
			if (Objects.isNull(input) || input.isBlank()) {
				break;
			}
			final @Nullable Point location;
			final @Nullable ITownFixture town; // ModifiableTown, i.e. AbstractTown|Village, in Ceylon
			if (NumParsingHelper.isNumeric(input)) {
				final int id = NumParsingHelper.parseInt(input).orElseThrow(
						() -> new IllegalStateException(
								"Failed to parse after input determined to be numeric"));
				final Optional<Pair<Point, ITownFixture>> temp =
						unstattedTowns(model.getMap()).stream()
								.filter(p -> p.getValue1().getId() == id).findAny();
				location = temp.map(Pair::getValue0).orElse(null);
				town = temp.map(Pair::getValue1).orElse(null);
			} else {
				final Optional<Pair<Point, ITownFixture>> temp =
						unstattedTowns(model.getMap()).stream()
								.filter(p -> input.equals(p.getValue1().getName()))
								.findAny();
				location = temp.map(Pair::getValue0).orElse(null);
				town = temp.map(Pair::getValue1).orElse(null);
			}
			try {
				if (Objects.isNull(town) || Objects.isNull(location)) {
					cli.println("No matching town found.");
				} else {
					final CommunityStats stats;
					if (cli.inputBooleanInSeries("Enter stats rather than generating them? ")) {
						stats = enterStats(cli, idf, model.getMap(), location, town);
					} else {
						stats = generateStats(idf, location, town, model.getMap());
					}
					model.assignTownStats(location, town.getId(), town.getName(), stats);
				}
			} catch (final MissingTableException except) {
				LovelaceLogger.error(except, "Missing table file");
				return;
			}
		}
	}

	/**
	 * Help the user generate population details for all the towns in the
	 * map that don't have such details already.
	 */
	public void generateAllTowns(final IDRegistrar idf, final PopulationGeneratingModel model) {
		final List<Pair<Point, ITownFixture>> list = unstattedTowns(model.getMap());
		Collections.shuffle(list);
		for (final Pair<Point, ITownFixture> pair : list) {
			final Point location = pair.getValue0();
			final ITownFixture town = pair.getValue1();
			cli.printf("Next town is %s, at %s.%n", town.getShortDescription(), location);
			final CommunityStats stats;
			final Boolean resp = cli.inputBooleanInSeries(
					"Enter stats rather than generating them?", "enter stats");
			try {
				if (Objects.isNull(resp)) {
					break;
				} else if (resp) {
					stats = enterStats(cli, idf, model.getMap(), location, town);
				} else {
					stats = generateStats(idf, location, town, model.getMap());
				}
				model.setMapModified(true);
				model.assignTownStats(location, town.getId(), town.getName(), stats);
			} catch (final MissingTableException except) {
				LovelaceLogger.error(except, "Missing table file");
				break;
			}
		}
	}
}

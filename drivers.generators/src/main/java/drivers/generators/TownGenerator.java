package drivers.generators;

import java.io.IOException;
import drivers.exploration.old.MissingTableException;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.logging.Logger;
import java.util.logging.Level;

import drivers.common.cli.ICLIHelper;

import common.map.fixtures.towns.ITownFixture;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.CommunityStats;
import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.TownSize;

import common.map.IFixture;
import common.map.TileFixture;
import common.map.Point;
import common.map.HasName;
import common.map.TileType;
import common.map.IMapNG;

import common.map.fixtures.IResourcePile;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Quantity;

import common.idreg.IDRegistrar;

import common.map.fixtures.resources.HarvestableFixture;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Shrub;

import exploration.common.SurroundingPointIterable;

import java.util.Random;

import drivers.exploration.old.ExplorationRunner;

import common.map.fixtures.terrain.Forest;

import java.util.LinkedList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import lovelace.util.FileContentsReader;
import lovelace.util.NumParsingHelper;
import org.javatuples.Triplet;
import org.javatuples.Pair;

import org.jetbrains.annotations.Nullable;
import java.math.BigDecimal;

/**
 * A command-line app to generate population details for villages.
 */
/* package */ class TownGenerator {
	private static final Logger LOGGER = Logger.getLogger(TownGenerator.class.getName());

	public TownGenerator(ICLIHelper cli) throws MissingTableException, IOException {
		this.cli = cli;
		runner = initProduction(); // TODO: pull its contents up?
		consumption = initConsumption(); // TODO: inline that?
	}

	private final ICLIHelper cli;

	/**
	 * Load consumption possibilities from file.
	 */
	private Map<String, List<Triplet<Quantity, String, String>>> initConsumption() throws IOException {
		Map<String, List<Triplet<Quantity, String, String>>> retval = new HashMap<>();
		for (String terrain : Arrays.asList("mountain", "forest", "plains", "ocean")) {
			String file = terrain + "_consumption";
			Iterable<String> tableContents =
				FileContentsReader.readFileContents(TownGenerator.class, "tables/" + file);
			List<Triplet<Quantity, String, String>> inner = new ArrayList<>();
			for (String line : tableContents) {
				if (line.isEmpty()) {
					continue;
				}
				String[] split = line.split("\t");
				int quantity = Integer.parseInt(split[0]);
				String units = split[1];
				String kind = split[2];
				String resource = split[3];
				inner.add(Triplet.with(new Quantity(quantity, units), kind, resource));
			}
			retval.put(terrain, Collections.unmodifiableList(inner));
		}
		return Collections.unmodifiableMap(retval);
	}

	/**
	 * Load production possibilities from file.
	 */
	private ExplorationRunner initProduction() throws MissingTableException, IOException {
		ExplorationRunner retval = new ExplorationRunner();
		Deque<String> firstTables = new LinkedList<>(Arrays.asList("mountain_skills",
			"forest_skills", "plains_skills", "ocean_skills"));
		Deque<String> secondTables = new LinkedList<>();
		while (!firstTables.isEmpty()) {
			String table = firstTables.removeFirst();
			retval.loadTableFromFile(TownGenerator.class, table);
			for (String reference : retval.getTableContents(table)) {
				if (reference.contains("#")) {
					String temp = reference.split("#", 2)[1];
					if (!retval.hasTable(temp)) {
						firstTables.addFirst(temp.trim());
					}
				} else if (!reference.trim().isEmpty()) {
					secondTables.addFirst(reference + "_production"); // TODO: should this be firstTables instead?
				}
			}
		}
		while (!secondTables.isEmpty()) {
			String table = secondTables.removeFirst();
			retval.loadTableFromFile(TownGenerator.class, table);
			for (String reference : retval.getTableContents(table)) {
				if (reference.contains("#")) {
					String temp = reference.split("#", 2)[1];
					if (!retval.hasTable(temp)) {
						secondTables.addFirst(temp.trim());
					}
				}
			}
		}
		return retval;
	}

	private final Map<String, List<Triplet<Quantity, String, String>>> consumption;
	private final ExplorationRunner runner;

	/**
	 * The (for now active) towns in the given map that don't have 'stats'
	 * yet. In Ceylon the "town" type was the alias
	 * {@code ModifiableTown}, defined as the union of {@link
	 * AbstractTown} and {@link Village}, but we can neither define an
	 * alias nor use a union type in Java and so use the nearest supertype,
	 * {@link ITownFixture}.
	 */
	List<Pair<Point, ITownFixture>> unstattedTowns(IMapNG map) {
		return map.streamLocations()
			.flatMap(l -> map.getFixtures(l).stream().filter(ITownFixture.class::isInstance)
				.map(ITownFixture.class::cast)
				.filter(t -> TownStatus.Active.equals(t.getStatus()))
				.map(f -> Pair.with(l, f)))
			.collect(Collectors.toList());
	}

	/**
	 * Get the fixture in the given {@link map} identified by the given {@link id ID number}.
	 *
	 * TODO: search inside fortresses and units
	 */
	@Nullable
	private IFixture findByID(IMapNG map, int id) {
		return map.streamLocations().flatMap(l -> map.getFixtures(l).stream())
			.filter(f -> f.getId() == id)
			.findAny().orElse(null);
	}

	/**
	 * Find the location in the given {@link map} of the fixture identified
	 * by the given {@link id ID number}.
	 *
	 * TODO: search inside fortresses and units
	 */
	@Nullable
	private Point findLocById(IMapNG map, int id) {
		return map.streamLocations()
			.filter(l -> map.getFixtures(l).stream().anyMatch(f -> f.getId() == id))
			.findAny().orElse(null);
	}

	/**
	 * Whether, in the given {@link map}, any town claims a resource
	 * identified by the given {@link id ID number}.
	 */
	private boolean isClaimedField(IMapNG map, int id) {
		return map.streamLocations().flatMap(l -> map.getFixtures(l).stream())
			.filter(ITownFixture.class::isInstance).map(ITownFixture.class::cast)
			.map(ITownFixture::getPopulation).filter(Objects::nonNull)
			.flatMap(t -> StreamSupport.stream(t.getWorkedFields().spliterator(), true))
			.anyMatch(n -> id == n);
	}

	/**
	 * Whether, in the given {@link map}, the given {@link id ID number}
	 * refers to {@link HarvestableFixture a resource that can be worked}
	 * that {@link isClaimedField is presently unclaimed}.
	 */
	private boolean isUnclaimedField(IMapNG map, int id) {
		return !isClaimedField(map, id) && findByID(map, id) instanceof HarvestableFixture;
	}

	/**
	 * If both arguments exist and are ocean, return true; if one is ocean
	 * and the other is not, return false; otherwise, return true.
	 */
	private boolean bothOrNeitherOcean(@Nullable TileType one, @Nullable TileType two) {
		if (TileType.Ocean.equals(one)) {
			return TileType.Ocean.equals(two);
		} else if (TileType.Ocean.equals(two)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Whether the given {@link fix fixture} is actually claimable: an
	 * unexposed mineral vein, an uncultivated field or meadow, an
	 * uncultivated grove or orchard, an abandoned mine, or a cache is not claimable.
	 */
	private boolean isReallyClaimable(HarvestableFixture fix) {
		if (fix instanceof MineralVein) {
			return ((MineralVein) fix).isExposed();
		} else if (fix instanceof Meadow) {
			return ((Meadow) fix).isCultivated();
		} else if (fix instanceof Grove) {
			return ((Grove) fix).isCultivated();
		} else if (fix instanceof Mine) {
			return TownStatus.Active.equals(((Mine) fix).getStatus());
		} else if (fix instanceof CacheFixture) {
			return false;
		} else if (fix instanceof Shrub || fix instanceof StoneDeposit) {
			return true;
		} else {
			LOGGER.severe("Unhandled harvestable type");
			return false;
		}
	}

	/**
	 * Find the nearest claimable resources to the given location.
	 */
	private List<HarvestableFixture> findNearestFields(IMapNG map, Point location) {
		TileType base = map.getBaseTerrain(location);
		if (base != null) { // TODO: invert
			return StreamSupport.stream(new SurroundingPointIterable(location,
					map.getDimensions(), 10).spliterator(), true).distinct()
				.filter(l -> bothOrNeitherOcean(base, map.getBaseTerrain(l)))
				.flatMap(l -> map.getFixtures(l).stream())
				.filter(HarvestableFixture.class::isInstance)
				.map(HarvestableFixture.class::cast).filter(this::isReallyClaimable)
				.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Have the user enter expertise levels and claimed resources for a town.
	 */
	private CommunityStats enterStats(ICLIHelper cli, IDRegistrar idf, IMapNG map, Point location,
			/*ModifiableTown*/ ITownFixture town) {
		CommunityStats retval = new CommunityStats(Optional.ofNullable(
			cli.inputNumber("Population: ")).orElse(0));
		cli.println("Now enter Skill levels, the highest in the community for each Job.");
		cli.println("(Empty to end.)");
		while (true) {
			String job = cli.inputString("Job: ");
			if (job == null || job.isEmpty()) {
				break;
			}
			Integer level = cli.inputNumber("Level: ");
			if (level == null) {
				break;
			}
			retval.setSkillLevel(job, level);
		}

		cli.println("Now enter ID numbers of worked fields (empty to skip).");
		List<HarvestableFixture> nearestFields = new ArrayList<>(findNearestFields(map, location));
		while (true) {
			String input = Objects.requireNonNull(cli.inputString("Field ID #: "));
			int field;
			if (input.isEmpty()) {
				break;
			} else if (NumParsingHelper.isNumeric(input)) {
				field = NumParsingHelper.parseInt(input).orElseThrow(
					() -> new IllegalStateException(
						"Failed to parse after we determined input was numeric"));
			} else if ("nearest".equalsIgnoreCase(input) && !nearestFields.isEmpty()) {
				HarvestableFixture nearest = nearestFields.remove(0);
				cli.println("Nearest harvestable fixture is as follows:");
				cli.println(nearest.getShortDescription());
				field = nearest.getId();
			} else {
				cli.println("Invalid input");
				continue;
			}
			Point fieldLoc = findLocById(map, field); // TODO: This wasn't initialized until isClaimedField() check in Ceylon, but a variable can't be declared in an if statement in Java
			if (isClaimedField(map, field)) {
				cli.println("That field is already worked by another town");
			} else if (fieldLoc != null) {
				if (!bothOrNeitherOcean(map.getBaseTerrain(location),
						map.getBaseTerrain(fieldLoc))) {
					if (TileType.Ocean.equals(map.getBaseTerrain(location))) {
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
			String kind = cli.inputString("General kind of resource: ");
			if (kind == null || kind.isEmpty()) { // TODO: here and elsewhere, trim before isEmpty(), unless inputString() does that for us
				break;
			}
			String contents = cli.inputString("Specific kind of resource: ");
			if (contents == null || contents.isEmpty()) {
				break;
			}
			BigDecimal quantity = cli.inputDecimal("Quantity of the resource produced: ");
			if (quantity == null) {
				break;
			}
			String units = cli.inputString("Units of that quantity: ");
			if (units == null) { // TODO: What about empty units?
				break;
			}
			IResourcePile pile = new ResourcePileImpl(idf.createID(), kind, contents,
				new Quantity(quantity, units));
			retval.getYearlyProduction().add(pile);
		}

		cli.println("Now add resources consumed each year. (Empty to end.)");
		while (true) {
			String kind = cli.inputString("General kind of resource: ");
			if (kind == null || kind.isEmpty()) { // TODO: here and elsewhere, trim before isEmpty(), unless inputString() does that for us
				break;
			}
			String contents = cli.inputString("Specific kind of resource: ");
			if (contents == null || contents.isEmpty()) {
				break;
			}
			BigDecimal quantity = cli.inputDecimal("Quantity of the resource produced: ");
			if (quantity == null) {
				break;
			}
			String units = cli.inputString("Units of that quantity: ");
			if (units == null) { // TODO: What about empty units?
				break;
			}
			retval.getYearlyConsumption().add(new ResourcePileImpl(idf.createID(), kind,
				contents, new Quantity(quantity, units)));
		}

		return retval;
	}

	/**
	 * What general kind of thing the given harvestable fixture will produce each year.
	 *
	 * TODO: Provide and use lookup tables for specific crops to avoid miscategorizations
	 */
	private String getHarvestableKind(HarvestableFixture fixture) {
		if (fixture instanceof Grove) {
			return (((Grove) fixture).isOrchard()) ? "food" : "wood";
		} else if (fixture instanceof Meadow) {
			return (((Meadow) fixture).isField()) ? "food" : "fodder";
		} else if (fixture instanceof MineralVein) {
			return "mineral";
		} else if (fixture instanceof StoneDeposit) {
			return "stone";
		} else {
			// TODO: log this case?
			return "unknown";
		}
	}

	/**
	 * What specific resource the given harvestable fixture will produce.
	 */
	private String getHarvestedProduct(HarvestableFixture fixture) {
		return fixture.getKind();
	}

	@FunctionalInterface
	private static interface IntToIntFunction {
		int apply(int num);
	}

	@FunctionalInterface
	private static interface RepeatedRoller {
		default int repeatedlyRoll(int count, int die) {
			return repeatedlyRoll(count, die, 0);
		}

		int repeatedlyRoll(int count, int die, int addend);
	}

	/**
	 * Generate expertise and production and consumption data for the given town.
	 *
	 * Note that in Ceylon the type of {@link town} was {@code AbstractTown|Village},
	 * excluding fortresses.
	 *
	 * To ensure consistency between runs of this algorithm, we seed the
	 * random number generator with the town's ID.
	 */
	private CommunityStats generateStats(IDRegistrar idf, Point location, ITownFixture town,
			IMapNG map) throws MissingTableException {
		final Random rng = new Random(town.getId());
		/**
		 * A die roll using our pre-seeded RNG.
		 */
		IntToIntFunction roll = (die) -> rng.nextInt(die) + 1;

		/**
		 * Repeatedly roll our pre-seeded RNG-die, optionally adding a constant value.
		 */
		RepeatedRoller repeatedlyRoll = (count, die, addend) -> {
			int sum = addend;
			for (int i = 0; i < count; i++) {
				sum +=roll.apply(die);
			}
			return sum;
		};

		int population;
		int skillCount;
		IntSupplier skillLevelSource;
		int resourceCount;
		if (town instanceof Village) {
			if (!TownSize.Small.equals(town.getTownSize())) {
				throw new IllegalStateException(
					"Don't know how to handle non-small villages");
			}
			population = repeatedlyRoll.repeatedlyRoll(3, 8, 3);
			skillCount = repeatedlyRoll.repeatedlyRoll(2, 4);
			skillLevelSource = () -> repeatedlyRoll.repeatedlyRoll(4, 3, -3);
			resourceCount = repeatedlyRoll.repeatedlyRoll(2, 3);
		} else if (town instanceof AbstractTown) {
			switch (town.getTownSize()) {
			case Small:
				population = repeatedlyRoll.repeatedlyRoll(4, 10, 5);
				skillCount = repeatedlyRoll.repeatedlyRoll(3, 4);
				skillLevelSource = () -> repeatedlyRoll.repeatedlyRoll(2, 6);
				resourceCount = repeatedlyRoll.repeatedlyRoll(2, 3);
			case Medium:
				population = repeatedlyRoll.repeatedlyRoll(20, 20, 50);
				skillCount = repeatedlyRoll.repeatedlyRoll(4, 6);
				skillLevelSource = () -> repeatedlyRoll.repeatedlyRoll(3, 6);
				resourceCount = repeatedlyRoll.repeatedlyRoll(2, 6);
			case Large:
				population = repeatedlyRoll.repeatedlyRoll(23, 100, 200);
				skillCount = repeatedlyRoll.repeatedlyRoll(6, 8);
				skillLevelSource = () -> repeatedlyRoll.repeatedlyRoll(3, 8);
				resourceCount = repeatedlyRoll.repeatedlyRoll(4, 6);
			default:
				throw new IllegalStateException("Non-exhaustive switch");
			}
		} else {
			throw new IllegalStateException("Unhandled town type");
		}

		CommunityStats retval = new CommunityStats(population);
		String skillTable;
		String consumptionTableName;
		TileType terrain = map.getBaseTerrain(location);
		if (terrain != null) { // TODO: invert
			if (TileType.Ocean.equals(terrain)) {
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
		} else {
			skillTable = "plains_skills";
			consumptionTableName = "plains";
		}

		for (int i = 0; i < skillCount; i++) {
			String skill = runner.recursiveConsultTable(skillTable, location,
				map.getBaseTerrain(location), map.isMountainous(location),
				map.getFixtures(location), map.getDimensions());
			int level = skillLevelSource.getAsInt();
			if (Optional.ofNullable(retval.getHighestSkillLevels().get(skill)).orElse(0)
					< level) {
				retval.setSkillLevel(skill, level);
			}
		}

		List<HarvestableFixture> workedFields = findNearestFields(map, location).stream()
			.limit(resourceCount).collect(Collectors.toList());
		for (HarvestableFixture field : workedFields) {
			retval.addWorkedField(field.getId());
			retval.getYearlyProduction().add(new ResourcePileImpl(idf.createID(),
				getHarvestableKind(field), getHarvestedProduct(field),
				new Quantity(1, "unit")));
		}

		for (Map.Entry<String, Integer> entry : retval.getHighestSkillLevels().entrySet()) {
			String skill = entry.getKey();
			int level = entry.getValue();
			String tableName = skill + "_production";
			if (runner.hasTable(tableName)) {
				try {
					retval.getYearlyProduction().add(new ResourcePileImpl(
						idf.createID(), "unknown",
						runner.consultTable(tableName, location,
							map.getBaseTerrain(location), map.isMountainous(location),
							map.getFixtures(location), map.getDimensions()),
						new Quantity(Math.pow(2, level - 1),
							(level == 1) ? "unit" : "units")));
				} catch (MissingTableException except) {
					LOGGER.log(Level.WARNING, "Missing table", except);
					retval.getYearlyProduction().add(new ResourcePileImpl(
						idf.createID(), "unknown", "product of " + skill,
						new Quantity(1, "unit")));
				}
			} else {
				retval.getYearlyProduction().add(new ResourcePileImpl(idf.createID(),
					"unknown", "product of " + skill, new Quantity(1, "unit")));
			}
		}

		if (!consumption.containsKey(consumptionTableName)) {
			throw new IllegalStateException("Appropriate consumption table missing");
		}
		List<Triplet<Quantity, String, String>> consumptionTable =
			consumption.get(consumptionTableName);
		for (Triplet<Quantity, String, String> triplet : consumptionTable) {
			retval.getYearlyConsumption().add(new ResourcePileImpl(idf.createID(),
				triplet.getValue1(), triplet.getValue2(), triplet.getValue0()));
		}

		retval.getYearlyConsumption().add(new ResourcePileImpl(idf.createID(), "food", "various",
			new Quantity(4 * 14 * population, "pounds")));
		return retval;
	}

	/**
	 * Allow the user to create population details for specific towns.
	 */
	public void generateSpecificTowns(IDRegistrar idf, PopulationGeneratingModel model) {
		while (true) {
			String input = cli.inputString("ID or name of town to create stats for: ");
			if (input == null || input.trim().isEmpty()) {
				break;
			}
			@Nullable Point location;
			@Nullable ITownFixture town; // ModifiableTown, i.e. AbstractTown|Village, in Ceylon
			if (NumParsingHelper.isNumeric(input)) {
				int id = NumParsingHelper.parseInt(input).orElseThrow(
					() -> new IllegalStateException(
						"Failed to parse after input determined to be numeric"));
				Optional<Pair<Point, ITownFixture>> temp =
					unstattedTowns(model.getMap()).stream()
						.filter(p -> p.getValue1().getId() == id).findAny();
				location = temp.map(Pair::getValue0).orElse(null);
				town = temp.map(Pair::getValue1).orElse(null);
			} else {
				Optional<Pair<Point, ITownFixture>> temp =
					unstattedTowns(model.getMap()).stream()
						.filter(p -> input.equals(p.getValue1().getName()))
						.findAny();
				location = temp.map(Pair::getValue0).orElse(null);
				town = temp.map(Pair::getValue1).orElse(null);
			}
			try {
				if (town != null && location != null) { // TODO: invert?
					CommunityStats stats;
					if (cli.inputBooleanInSeries("Enter stats rather than generating them? ")) {
						stats = enterStats(cli, idf, model.getMap(), location, town);
					} else {
						stats = generateStats(idf, location, town, model.getMap());
					}
					model.assignTownStats(location, town.getId(), town.getName(), stats);
				} else {
					cli.println("No matching town found.");
				}
			} catch (MissingTableException except) {
				LOGGER.log(Level.SEVERE, "Missing table file", except);
				return;
			}
		}
	}

	/**
	 * Help the user generate population details for all the towns in the
	 * map that don't have such details already.
	 */
	public void generateAllTowns(IDRegistrar idf, PopulationGeneratingModel model) {
		List<Pair<Point, ITownFixture>> list = unstattedTowns(model.getMap());
		Collections.shuffle(list);
		for (Pair<Point, ITownFixture> pair : list) {
			Point location = pair.getValue0();
			ITownFixture town = pair.getValue1();
			cli.println(String.format("Next town is %s, at %s. ",
				town.getShortDescription(), location));
			CommunityStats stats;
			Boolean resp = cli.inputBooleanInSeries(
				"Enter stats rather than generating them?", "enter stats");
			try {
				if (resp == null) {
					break;
				} else if (resp) {
					stats = enterStats(cli, idf, model.getMap(), location, town);
				} else {
					stats = generateStats(idf, location, town, model.getMap());
				}
				model.setMapModified(true);
				model.assignTownStats(location, town.getId(), town.getName(), stats);
			} catch (MissingTableException except) {
				LOGGER.log(Level.SEVERE, "Missing table file", except);
				break;
			}
		}
	}
}

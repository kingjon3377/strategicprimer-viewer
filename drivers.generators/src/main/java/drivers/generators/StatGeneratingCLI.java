package drivers.generators;

import common.map.fixtures.Implement;
import common.map.fixtures.mobile.AnimalImpl;
import java.util.Comparator;
import java.util.function.Predicate;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;
import lovelace.util.TriConsumer;

import java.util.function.BiConsumer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import common.idreg.IDRegistrar;
import common.idreg.IDFactoryFiller;
import drivers.common.DriverFailedException;

import common.map.IMapNG;
import common.map.Player;
import common.map.Point;

import common.map.fixtures.UnitMember;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.mobile.Worker;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.IMutableWorker;

import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.RaceFactory;
import common.map.fixtures.mobile.worker.Job;

import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import exploration.common.Pathfinder;
import exploration.common.PathfinderFactory;

import java.util.Optional;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.function.IntConsumer;
import java.util.function.Function;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;

import lovelace.util.FileContentsReader;
import lovelace.util.SingletonRandom;

import common.map.fixtures.towns.Village;

/**
 * A driver to generate new workers.
 *
 * FIXME: Write stat-generating GUI
 */
/* package */ class StatGeneratingCLI implements CLIDriver {
	/**
	 * Get the index of the lowest value in an array.
	 */
	private static int getMinIndex(final int... array) {
		int lowest = Integer.MAX_VALUE;
		int index = -1;
		int i = 0;
		for (final int num : array) {
			if (num < lowest) {
				index = i;
				lowest = num;
			}
			i++;
		}
		return index;
	}

	/**
	 * Simulate a die-roll.
	 */
	private static int die(final int max) {
		return SingletonRandom.SINGLETON_RANDOM.nextInt(max) + 1;
	}

	/**
	 * Simulate rolling 3d6.
	 */
	private static int threeDeeSix() {
		return die(6) + die(6) + die(6);
	}

	/**
	 * The chance that someone from a village located a "days"-day
	 * journey away will come as a volunteer.
	 */
	private static double villageChance(final int days) {
		return Math.pow(0.4, days);
	}

	private final ICLIHelper cli;
	private final PopulationGeneratingModel model;
	private int currentTurn;

	@Override
	public PopulationGeneratingModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	public StatGeneratingCLI(final ICLIHelper cli, final PopulationGeneratingModel model) {
		this.cli = cli;
		this.model = model;
	}

	/**
	 * Let the user enter which Jobs a worker's levels are in.
	 */
	private void enterWorkerJobs(final IUnit unit, final IWorker worker, final int levels) {
		for (int i = 0; i < levels; i++) {
			final String jobName = cli.inputString("Which Job does worker have a level in? ");
			if (jobName == null) {
				break;
			} else if (!model.addJobLevel(unit, worker, jobName)) {
				LovelaceLogger.warning("Adding or incrementing Job failed somehow ...");
			}
		}
	}

	/**
	 * Villages from which newcomers have arrived either recently or already this turn.
	 *
	 * TODO: Preinitialize size
	 */
	private final Map<Village, Boolean> excludedVillages = new HashMap<>();

	/**
	 * Get from the cache, or if not present there ask the user, if a
	 * newcomer has come from the given village recently.
	 */
	private boolean hasLeviedRecently(final Village village) {
		if (excludedVillages.containsKey(village)) {
			return excludedVillages.get(village);
		} else {
			final Boolean retval = cli.inputBoolean(String.format(
				"Has a newcomer come from %s in the last 7 turns?", village.getName()));
			if (retval == null) {
				return false;
			}
			excludedVillages.put(village, retval);
			return retval;
		}
	}

	/**
	 * Racial stat bonuses.
	 */
	private final Map<String, WorkerStats> racialBonuses = new HashMap<>();

	/**
	 * Load racial stat bonuses for the given race from the cache, or if not present there from file.
	 */
	WorkerStats loadRacialBonus(final String race) {
		if (racialBonuses.containsKey(race)) {
			return racialBonuses.get(race);
		}
		try {
			final Iterable<String> textContent = FileContentsReader
				.readFileContents(WorkerStats.class,
					String.format("racial_stat_adjustments/%s.txt", race));
			final List<Integer> parsed = new ArrayList<>(6);
			for (final String line : textContent) {
				parsed.add(Integer.parseInt(line.strip()));
			}
			final WorkerStats retval = WorkerStats.factory(parsed.get(0), parsed.get(1), parsed.get(2), parsed.get(3),
				parsed.get(4), parsed.get(5));
			racialBonuses.put(race, retval);
			return retval;
		} catch (final NoSuchFileException except) {
			LovelaceLogger.warning("No stat adjustments found for %s", race);
			return WorkerStats.factory(0, 0, 0, 0, 0, 0);
		} catch (final IOException except) {
			LovelaceLogger.warning("I/O error reading stat adjustments for %s", race);
			return WorkerStats.factory(0, 0, 0, 0, 0, 0);
		}
	}

	/**
	 * Whether the user has said to always give a human's racial stat bonus to the lowest stat.
	 */
	private boolean alwaysLowest = false;

	/**
	 * Create randomly-generated stats for a worker, with racial adjustments applied.
	 */
	private WorkerStats createWorkerStats(final String race, final int levels) {
		final WorkerStats base = WorkerStats.random(StatGeneratingCLI::threeDeeSix);
		final int lowestScore = getMinIndex(base.array());
		final WorkerStats racialBonus;
		if ("human".equals(race)) {
			final int bonusStat;
			if (alwaysLowest) {
				bonusStat = lowestScore;
			} else {
				final int chosenBonus = cli.chooseStringFromList(Arrays.asList("Strength",
					"Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma",
					"Lowest", "Always Choose Lowest"), String.format(
						"Character is a %s; which stat should get a +2 bonus?", race),
					"", "Stat for bonus:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue0();
				if (chosenBonus < 6) {
					bonusStat = chosenBonus;
				} else if (chosenBonus == 7) {
					bonusStat = lowestScore;
					alwaysLowest = true;
				} else {
					bonusStat = lowestScore;
				}
			}
			racialBonus = switch (bonusStat) {
				case 0 -> WorkerStats.factory(2, 0, 0, 0, 0, 0);
				case 1 -> WorkerStats.factory(0, 2, 0, 0, 0, 0);
				case 2 -> WorkerStats.factory(0, 0, 2, 0, 0, 0);
				case 3 -> WorkerStats.factory(0, 0, 0, 2, 0, 0);
				case 4 -> WorkerStats.factory(0, 0, 0, 0, 2, 0);
				default -> WorkerStats.factory(0, 0, 0, 0, 0, 2);
			};
		} else {
			racialBonus = loadRacialBonus(race);
		}
		final int conBonus = WorkerStats.getModifier(base.getConstitution() +
			racialBonus.getConstitution());
		int hp = 8 + conBonus;
		for (int level = 0; level < levels; level++) {
			hp += die(8) + conBonus;
		}
		return WorkerStats.adjusted(hp, base, racialBonus);
	}

	private void maybeAddEquipment(final IDRegistrar idf,
			final IMutableWorker worker, final String equipment,
			final double chance) {
		if (chance >= 1.0 || SingletonRandom.SINGLETON_RANDOM.nextDouble() < chance) {
			worker.addEquipment(new Implement(equipment,
				idf.createID()));
		}
	}

	/**
	 * Generate a worker with race and Job levels based on the population of the given village.
	 */
	private Worker generateWorkerFrom(final Village village, final String name, final IDRegistrar idf) {
		final Worker worker = new Worker(name, village.getRace(), idf.createID());
		worker.setNote(village.owner(), String.format("From %s.", village.getName()));
		if (village.getPopulation() == null) {
			cli.println("No population details, so no levels.");
			final WorkerStats stats = createWorkerStats(village.getRace(), 0);
			worker.setStats(stats);
			cli.println(String.format("%s is a %s from %s. Stats:", name, village.getRace(),
					village.getName()));
			cli.println(stats.getPrintable());
			final boolean woolen = cli.inputBooleanInSeries("Is the worker's tunic woolen rather than linen?");
			worker.addEquipment(new Implement(woolen ? "woolen tunic" : "linen tunic", idf.createID()));
			maybeAddEquipment(idf, worker, "woolen cloak", 0.9);
			maybeAddEquipment(idf, worker, "pair leather boots", 0.8);
			maybeAddEquipment(idf, worker, "leather satchel", 0.75);
			maybeAddEquipment(idf, worker, "leather waterskin", 0.75);
			return worker;
		} else {
			final List<IJob> candidates = new ArrayList<>();
			for (final Map.Entry<String, Integer> entry :
					village.getPopulation().getHighestSkillLevels().entrySet()) {
				final String job = entry.getKey();
				final int level = entry.getValue();
				final IntConsumer addCandidate = (lvl) -> candidates.add(new Job(job, lvl));
				if (level > 16) {
					addCandidate.accept(level - 3);
					addCandidate.accept(level - 4);
					addCandidate.accept(level - 6);
					addCandidate.accept(level - 7);
					// TODO: For counts above 16 just use a hard-coded distribution (e.g. 4 each of 5, 6, 7, 8)?
					SingletonRandom.SINGLETON_RANDOM.ints(16, 5, 9)
							.forEach(addCandidate);
					SingletonRandom.SINGLETON_RANDOM.ints(32, 1, 5)
							.forEach(addCandidate);
				} else if (level > 12) {
					addCandidate.accept(level - 3);
					SingletonRandom.SINGLETON_RANDOM.ints(8, 5, 9)
							.forEach(addCandidate);
					SingletonRandom.SINGLETON_RANDOM.ints(16, 1, 5)
							.forEach(addCandidate);
				} else if (level > 8) {
					SingletonRandom.SINGLETON_RANDOM.ints(3, 5, 9)
							.forEach(addCandidate);
					SingletonRandom.SINGLETON_RANDOM.ints(6, 1, 5)
							.forEach(addCandidate);
				} else if (level > 4) {
					SingletonRandom.SINGLETON_RANDOM.ints(2, 1, 5)
							.forEach(addCandidate);
				}
			}
			if (candidates.isEmpty()) {
				cli.println(String.format("No training available in %s.",
						village.getName()));
				final WorkerStats stats = createWorkerStats(village.getRace(), 0);
				worker.setStats(stats);
				cli.println(String.format("%s is a %s from %s. Stats:", name,
						village.getRace(), village.getName()));
				cli.println(stats.getPrintable());
				final boolean woolen = cli.inputBooleanInSeries("Is the worker's tunic woolen rather than linen?");
				worker.addEquipment(new Implement(woolen ? "woolen tunic" : "linen tunic", idf.createID()));
				maybeAddEquipment(idf, worker, "woolen cloak", 0.9);
				maybeAddEquipment(idf, worker, "pair leather boots", 0.8);
				maybeAddEquipment(idf, worker, "leather satchel", 0.75);
				maybeAddEquipment(idf, worker, "leather waterskin", 0.75);
				return worker;
			} else {
				final IJob training = candidates.get(SingletonRandom.SINGLETON_RANDOM.nextInt(candidates.size()));
				worker.addJob(training);
				final Predicate<WorkerStats> suitable = MinimumStats.suitableFor(training.getName(), training.getLevel());
				int iterations = 0;
				while (true) {
					iterations++;
					final WorkerStats stats = createWorkerStats(village.getRace(),
							training.getLevel());
					if (iterations > 100) {
						cli.println(String.format("Bypassing automated sanity check after %d iterations", iterations));
					} else if (!suitable.test(stats)) {
						LovelaceLogger.trace("Skipping stats deemed unsuitable: %s", stats.getPrintable());
						continue;
					}
					cli.println(String.format(
							"%s, a %s, is a level-%d %s from %s. Proposed stats:",
							name, village.getRace(), training.getLevel(),
							training.getName(), village.getName()));
					cli.println(stats.getPrintable());
					final boolean acceptance = cli.inputBoolean( // TODO: handle EOF
							"Do those stats fit that profile?");
					if (acceptance) {
						worker.setStats(stats);
						break;
					}
				}
				final boolean hasMount = cli.inputBooleanInSeries("Is the worker mounted?", "mounted-" + training.getName());
				if (hasMount) {
					final String mountKind = cli.inputString("Kind of mount: ");
					if (mountKind != null && !mountKind.isEmpty()) {
						worker.setMount(new AnimalImpl(mountKind, false, "tame", idf.createID()));
					}
				}
				final List<String> standardEquipment =
					StandardEquipment.standardEquipment(
						training.getName(), training.getLevel());
				for (final String item : standardEquipment) {
					LovelaceLogger.debug("Adding %s for %s, a level %d %s",
						item, worker.getName(),
						training.getLevel(), training.getName());
					worker.addEquipment(new Implement(item, idf.createID()));
				}
				String equipmentPrompt = "Does the worker have any equipment?";
				Predicate<String> equipmentQuery = cli::inputBooleanInSeries;
				final BiConsumer<String, String> addIfStdOmits = (key, arg) -> {
					if (standardEquipment.stream().map(String::toLowerCase).noneMatch(s -> s.contains(key))) {
						worker.addEquipment(new Implement(arg, idf.createID()));
					}
				};
				final Predicate<String> stdOmits = arg -> standardEquipment.stream().map(String::toLowerCase).noneMatch(s -> s.contains(arg));
				if (stdOmits.test("tunic")) {
					final boolean woolen = cli.inputBooleanInSeries("Is the worker's tunic woolen rather than linen?", "tunic-" + training.getName());
					addIfStdOmits.accept("tunic",
						woolen ? "woolen tunic" : "linen tunic");
				}
				final boolean hasMultipleLevels = training.getLevel() > 1;
				final TriConsumer<String, String, Double> maybeAdd =
					(key, item, chance) -> {
						if (hasMultipleLevels || SingletonRandom.SINGLETON_RANDOM.nextDouble() < chance) {
							addIfStdOmits.accept(key, item);
						} else {
							cli.println("Not adding " + key);
						}
				};
				maybeAdd.accept("cloak", "woolen cloak", 0.9);
				maybeAdd.accept("boots", "pair leather boots", 0.8);
				maybeAdd.accept("satchel", "leather satchel", 0.75);
				maybeAdd.accept("waterskin", "leather waterskin", 0.75);
				// TODO: Accept strings until empty line instead of boolean-prompting every time
                final Predicate<String> notInSeries = cli::inputBoolean;
				while (equipmentQuery.test(equipmentPrompt)) {
					final String equipment = cli.inputString("Kind of equipment: ");
					if (equipment == null || equipment.isEmpty()) {
						break;
					}
					worker.addEquipment(new Implement(equipment, idf.createID()));
					equipmentPrompt = "Does the worker have any more equipment?";
					equipmentQuery = notInSeries;
				}
				return worker;
			}
		}
	}

	/**
	 * Let the user create randomly-generated workers in a specific unit.
	 */
	private void createWorkersForUnit(final IDRegistrar idf, final IUnit unit) {
		final int count = Optional.ofNullable(cli.inputNumber("How many workers to generate? ")).orElse(0);
		for (int i = 0; i < count; i++) {
			final String race = RaceFactory.randomRace();
			final Worker worker;
			final String name = cli.inputString(String.format("Work is a %s. Worker name: ", race));
			if (name == null) {
				break;
			}
			worker = new Worker(name, race, idf.createID());
			final int levels = (int) SingletonRandom.SINGLETON_RANDOM.ints(3, 0, 20)
				.filter(n -> n == 0).count();
			if (levels == 1) {
				cli.println("Worker has 1 Job level.");
			} else if (levels > 1) {
				cli.println(String.format("Worker has %d Job levels.", levels));
			}
			final WorkerStats stats = createWorkerStats(race, levels);
			worker.setStats(stats);
			if (levels > 0) {
				cli.println("Generated stats:");
				cli.print(stats.toString());
			}
			model.addWorkerToUnit(unit, worker);
			enterWorkerJobs(unit, worker, levels);
		}
	}

	private static final Pattern VILLAGER_PATTERN = Pattern.compile(".*From \\([^.]*\\)\\. Newcomer in turn #\\([0-9]*\\)\\..*");

	/**
	 * Filter out villages that have had volunteers come to the player's fortress(es) recently.
	 */
	private Predicate<Village> filterRecentVillages(final Player player) {
		final Map<String, Integer> villageMap = new HashMap<>();
		for (final IUnit unit : model.getUnits(player)) {
			for (final UnitMember member : unit) {
				if (member instanceof IWorker worker) {
					final String note = worker.getNote(player);
					final Matcher match = VILLAGER_PATTERN.matcher(note);
					if (match.find()) {
						final String village = match.group(1);
						final String turnStr = match.group(2);
						final int turn = Integer.parseInt(turnStr);
						villageMap.put(village, Math.max(turn, villageMap.getOrDefault(village, -1)));
					}
				}
			}
		}
		return village -> villageMap.getOrDefault(village.getName(), -1) < currentTurn - 7;
	}

	/**
	 * Let the user create randomly-generated workers, with names read from file, in a unit.
	 */
	private void createWorkersFromFile(final IDRegistrar idf, final IUnit unit) throws IOException {
		final int count = Optional.ofNullable(cli.inputNumber("How many workers to generate? ")).orElse(0);
		final Deque<String> names;
		final String filename = cli.inputString("Filename to load names from: ");
		if (filename == null) {
			return;
		} else if (Files.exists(Paths.get(filename))) {
			names = new LinkedList<>(Files.readAllLines(Paths.get(filename)));
		} else {
			names = new LinkedList<>();
			cli.println("No such file.");
		}
		final Point hqLoc;
		final Optional<Point> found = model.getMap().streamLocations()
				.flatMap(l -> model.getMap().getFixtures(l).stream()
					.map(f -> Pair.with(l, f)))
				.filter(pair -> pair.getValue1().getId() == unit.getId()) // TODO: look in forts too
				.map(Pair::getValue0).findAny();
		if (found.isPresent()) {
			hqLoc = found.get();
		} else {
			cli.println("That unit's location not found in main map.");
			final Point point = cli.inputPoint("Location to use for village distances:");
			if (point == null) {
				return;
			} else {
				hqLoc = point;
			}
		}
		final Pathfinder pather = PathfinderFactory.pathfinder(model.getMap());
		final Function<Point, Pair<Integer, Double>> travelDistance =
			(dest) -> Pair.with(pather.getTravelDistance(hqLoc, dest).getValue0(),
				model.getMapDimensions().distance(hqLoc, dest));
		final List<Triplet<Integer, Double, Village>> villages = model.getMap().streamLocations()
				.flatMap(l -> model.getMap().getFixtures(l).stream()
					.filter(Village.class::isInstance).map(Village.class::cast)
					.filter(v -> v.owner().equals(unit.owner()))
					.filter(filterRecentVillages(unit.owner()))
					.map(v -> Pair.with(l, v)))
				.map(p -> travelDistance.apply(p.getValue0()).addAt2(p.getValue1()))
				.sorted(Comparator.comparingInt(Triplet::getValue0))
				.collect(Collectors.toList());
		final int mpPerDay = Optional.ofNullable(cli.inputNumber("MP per day for village volunteers:"))
			.orElse(-1);
		for (int i = 0; i < count; i++) {
			final String name;
			if (names.isEmpty()) {
				final String temp = cli.inputString("Next worker name: ");
				if (temp == null) {
					break;
				} else {
					name = temp;
				}
			} else {
				name = names.removeFirst().strip();
			}
			Village home = null;
			final List<Triplet<Integer, Double, Village>> villagesToRemove = new ArrayList<>();
			for (final Triplet<Integer, Double, Village> triplet : villages) {
				final int mpDistance = triplet.getValue0();
				final double tileDistance = triplet.getValue1();
				final Village village = triplet.getValue2();
				if (hasLeviedRecently(village)) {
					villagesToRemove.add(triplet);
					continue;
				} else if (SingletonRandom.SINGLETON_RANDOM.nextDouble() <
						villageChance((int) (Math.min((double) (mpDistance) / mpPerDay,
							tileDistance / 12.0)) + 1)) {
					excludedVillages.put(village, true);
					villagesToRemove.add(triplet);
					home = village;
					break;
				}
			}
			villages.removeAll(villagesToRemove);
			final Worker worker;
			if (home == null) {
				final String race = RaceFactory.randomRace();
				cli.println(String.format("Worker %s is a %s", name, race));
				worker = new Worker(name, race, idf.createID());
				final int levels = (int) SingletonRandom.SINGLETON_RANDOM.ints(3, 0, 20)
					.filter(n -> n == 0)
					.count();
				if (levels == 1) {
					cli.println("Worker has 1 Job level.");
				} else if (levels>1) {
					cli.println(String.format("Worker has %d Job levels.", levels));
				}
				final WorkerStats stats = createWorkerStats(race, levels);
				worker.setStats(stats);
				if (levels>0) {
					cli.println("Generated stats:");
					cli.print(stats.toString());
				}
				model.addWorkerToUnit(unit, worker);
				enterWorkerJobs(unit, worker, levels);
				cli.println(String.format("%s is a %s. Stats:", name, race));
				cli.println(stats.getPrintable());
			} else {
				worker = generateWorkerFrom(home, name, idf);
				model.addWorkerToUnit(unit, worker);
			}
		}
	}

	/**
	 * Allow the user to create randomly-generated workers belonging to a
	 * particular player.
	 */
	private void createWorkersForPlayer(final IDRegistrar idf, final Player player) throws IOException {
		final List<IUnit> units = StreamSupport.stream(
				model.getUnits(player).spliterator(), false).collect(Collectors.toList());
		while (true) {
			final Pair<Integer, @Nullable IUnit> chosen = cli.chooseFromList(units, "Which unit contains the worker in question? (Select -1 to create new.)", "There are no units owned by that player.", "Unit selection: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
			final IUnit item;
			if (chosen.getValue1() != null) {
				item = chosen.getValue1();
			} else if (chosen.getValue0() <= units.size()) {
				final Point point = cli.inputPoint("Where to put new unit? ");
				final String kind = point == null ? null : cli.inputString("Kind of unit: ");
				final String name = kind == null ? null : cli.inputString("Unit name: ");
				if (point == null || kind == null || name == null) {
					return;
				} else {
					final IUnit temp = new Unit(player, kind, name, idf.createID());
					model.addUnitAtLocation(temp, point);
					units.add(temp);
					item = temp;
				}
			} else {
				break;
			}
			final Boolean load = cli.inputBooleanInSeries(
				"Load names from file and use randomly generated stats?");
			if (load == null) {
				return;
			} else if (load) {
				createWorkersFromFile(idf, item);
			} else {
				createWorkersForUnit(idf, item);
			}
			if (!Optional.ofNullable(cli.inputBoolean("Choose another unit? "))
					.orElse(false)) {
				break;
			}
		}
	}

	@Override
	public void startDriver() throws DriverFailedException {
		final IDRegistrar idf = IDFactoryFiller.createIDFactory(
			model.streamAllMaps().toArray(IMapNG[]::new));
		currentTurn = model.getMap().getCurrentTurn();
		// TODO: Make getPlayerChoices() return Collection
		final List<Player> players = StreamSupport.stream(
				model.getPlayerChoices().spliterator(), false).collect(Collectors.toList());
		while (!players.isEmpty()) {
			final Player chosen = cli.chooseFromList((List<? extends Player>) players, "Which player owns the new worker(s)?", "There are no players shared by all the maps.", "Player selection: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (chosen == null) {
				break;
			}
			players.remove(chosen);
			while (true) {
				try {
					createWorkersForPlayer(idf, chosen);
				} catch (final IOException except) {
					throw new DriverFailedException(except, "I/O error");
				}
				final Boolean continuation = cli.inputBoolean("Add more workers to another unit?");
				if (continuation == null) {
					return;
				} else if (!continuation) {
					break;
				}
			}
			if (!Optional.ofNullable(cli.inputBoolean("Choose another player?"))
					.orElse(false)) {
				break;
			}
		}
	}
}

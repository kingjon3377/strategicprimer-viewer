package drivers.generators;

import java.util.Comparator;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import common.idreg.IDRegistrar;
import common.idreg.IDFactoryFiller;
import drivers.common.DriverFailedException;

import common.map.IMapNG;
import common.map.IFixture;
import common.map.Player;
import common.map.Point;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.mobile.Worker;
import common.map.fixtures.mobile.IWorker;

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
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.function.IntConsumer;
import java.util.function.Function;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;

import lovelace.util.FileContentsReader;
import lovelace.util.SingletonRandom;

import common.map.fixtures.FixtureIterable;
import common.map.fixtures.towns.Village;

/**
 * A driver to generate new workers.
 *
 * FIXME: Write stat-generating GUI
 */
/* package */ class StatGeneratingCLI implements CLIDriver {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(StatGeneratingCLI.class.getName());

	private static final List<String> statLabelArray = Collections.unmodifiableList(Arrays.asList("Str",
		"Dex", "Con", "Int", "Wis", "Cha"));

	/**
	 * Find a fixture in a given iterable with the given ID.
	 */
	@Nullable
	private static IFixture findInIterable(final Integer id, final IFixture... fixtures) {
		for (IFixture fixture : fixtures) {
			if (fixture.getId() == id) {
				return fixture;
			} else if (fixture instanceof FixtureIterable) {
				IFixture result = findInIterable(id,
						((FixtureIterable<?>) fixture).stream().toArray(IFixture[]::new));
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Get the index of the lowest value in an array.
	 */
	private static int getMinIndex(final int... array) {
		int lowest = Integer.MAX_VALUE;
		int index = -1;
		int i = 0;
		for (int num : array) {
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
	 * The chance that someone from a village located a {@link days}-day
	 * journey away will come as a volunteer.
	 */
	private static double villageChance(final int days) {
		return Math.pow(0.4, days);
	}

	private final ICLIHelper cli;
	private final PopulationGeneratingModel model;

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
			String jobName = cli.inputString("Which Job does worker have a level in? ");
			if (jobName == null) {
				break;
			} else if (!model.addJobLevel(unit, worker, jobName)) {
				LOGGER.warning("Adding or incrementing Job failed somehow ...");
			}
		}
	}

	/**
	 * Villages from which newcomers have arrived either recently or already this turn.
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
			Boolean retval = cli.inputBoolean(String.format(
				"Has a newcomer come from %s in the last 7 turns?", village.getName()));
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
			Iterable<String> textContent = FileContentsReader
				.readFileContents(WorkerStats.class,
					String.format("racial_stat_adjustments/%s.txt", race));
			List<Integer> parsed = new ArrayList<>();
			for (String line : textContent) {
				parsed.add(Integer.parseInt(line.trim()));
			}
			Integer[] temp = parsed.toArray(new Integer[0]);
			WorkerStats retval = WorkerStats.factory(temp[0], temp[1], temp[2], temp[3],
				temp[4], temp[5]);
			racialBonuses.put(race, retval);
			return retval;
		} catch (final NoSuchFileException except) {
			LOGGER.warning("No stat adjustments found for " + race);
			return WorkerStats.factory(0, 0, 0, 0, 0, 0);
		} catch (final IOException except) {
			LOGGER.warning("I/O error reading stat adjustments for " + race);
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
		WorkerStats base = WorkerStats.random(StatGeneratingCLI::threeDeeSix);
		int lowestScore = getMinIndex(base.array());
		WorkerStats racialBonus;
		if ("human".equals(race)) {
			int bonusStat;
			if (alwaysLowest) {
				bonusStat = lowestScore;
			} else {
				int chosenBonus = cli.chooseStringFromList(Arrays.asList("Strength",
					"Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma",
					"Lowest", "Always Choose Lowest"), String.format(
						"Character is a %s; which stat should get a +2 bonus?", race),
					"", "Stat for bonus:", false).getValue0();
				if (chosenBonus < 6) {
					bonusStat = chosenBonus;
				} else if (chosenBonus == 7) {
					bonusStat = lowestScore;
					alwaysLowest = true;
				} else {
					bonusStat = lowestScore;
				}
			}
			switch (bonusStat) {
			case 0:
				racialBonus = WorkerStats.factory(2, 0, 0, 0, 0, 0);
				break;
			case 1:
				racialBonus = WorkerStats.factory(0, 2, 0, 0, 0, 0);
				break;
			case 2:
				racialBonus = WorkerStats.factory(0, 0, 2, 0, 0, 0);
				break;
			case 3:
				racialBonus = WorkerStats.factory(0, 0, 0, 2, 0, 0);
				break;
			case 4:
				racialBonus = WorkerStats.factory(0, 0, 0, 0, 2, 0);
				break;
			default:
				racialBonus = WorkerStats.factory(0, 0, 0, 0, 0, 2);
				break;
			}
		} else {
			racialBonus = loadRacialBonus(race);
		}
		final int conBonus = WorkerStats.getModifier(base.getConstitution() +
			racialBonus.getConstitution());
		int hp = 8 + conBonus;
		for (int level = 0; level < levels; level++) {
			hp = hp + die(8) + conBonus;
		}
		return WorkerStats.adjusted(hp, base, racialBonus);
	}

	/**
	 * Generate a worker with race and Job levels based on the population of the given village.
	 */
	private Worker generateWorkerFrom(final Village village, final String name, final IDRegistrar idf) {
		Worker worker = new Worker(name, village.getRace(), idf.createID());
		worker.setNote(village.getOwner(), String.format("From %s.", village.getName()));
		if (village.getPopulation() != null) {
			List<IJob> candidates = new ArrayList<>();
			for (Map.Entry<String, Integer> entry :
					village.getPopulation().getHighestSkillLevels().entrySet()) {
				String job = entry.getKey();
				int level = entry.getValue();
				IntConsumer addCandidate = (lvl) -> candidates.add(new Job(job, lvl));
				if (level > 16) {
					addCandidate.accept(level - 3);
					addCandidate.accept(level - 4);
					addCandidate.accept(level - 6);
					addCandidate.accept(level - 7);
					SingletonRandom.SINGLETON_RANDOM.ints()
						.filter(n -> n < 4).map(n -> 5 + n).limit(16)
						.forEach(addCandidate);
					SingletonRandom.SINGLETON_RANDOM.ints()
						.filter(n -> n < 4).map(n -> 1 + n).limit(32)
						.forEach(addCandidate);
				} else if (level > 12) {
					addCandidate.accept(level - 3);
					SingletonRandom.SINGLETON_RANDOM.ints()
						.filter(n -> n < 4).map(n -> 5 + n).limit(8)
						.forEach(addCandidate);
					SingletonRandom.SINGLETON_RANDOM.ints()
						.filter(n -> n < 4).map(n -> 1 + n).limit(16)
						.forEach(addCandidate);
				} else if (level > 8) {
					SingletonRandom.SINGLETON_RANDOM.ints()
						.filter(n -> n < 4).map(n -> 5 + n).limit(3)
						.forEach(addCandidate);
					SingletonRandom.SINGLETON_RANDOM.ints()
						.filter(n -> n < 4).map(n -> 1 + n).limit(6)
						.forEach(addCandidate);
				} else if (level > 4) {
					SingletonRandom.SINGLETON_RANDOM.ints()
						.filter(n -> n < 4).map(n -> 1 + n).limit(2)
						.forEach(addCandidate);
				}
			}
			if (candidates.isEmpty()) {
				cli.println(String.format("No training available in %s.",
					village.getName()));
				WorkerStats stats = createWorkerStats(village.getRace(), 0);
				worker.setStats(stats);
				cli.println(String.format("%s is a %s from %s. Stats:", name,
					village.getRace(), village.getName()));
				// TODO: Extract helper method for printing stat array
				int[] statArray = stats.array();
				for (int i = 0; i < 6; i++) {
					if ((i == statLabelArray.size() - 1) ||
							(i == statArray.length - 1)) {
						cli.println(String.format("%s %s", statLabelArray.get(i),
							WorkerStats.getModifierString(statArray[i])));
					} else {
						cli.print(String.format("%s %s, ", statLabelArray.get(i),
							WorkerStats.getModifierString(statArray[i])));
					}
				}
				return worker;
			} else {
				Collections.shuffle(candidates);
				IJob training = candidates.get(0);
				while (true) {
					worker.addJob(training);
					WorkerStats stats = createWorkerStats(village.getRace(),
						training.getLevel());
					cli.println(String.format(
						"%s, a %s, is a level-%d %s from %s. Proposed stats:",
						name, village.getRace(), training.getLevel(),
						training.getName(), village.getName()));
					int[] statArray = stats.array();
					for (int i = 0; i < 6; i++) {
						if ((i == statLabelArray.size() - 1) ||
								(i == statArray.length - 1)) {
							cli.println(String.format("%s %s", statLabelArray.get(i),
								WorkerStats.getModifierString(statArray[i])));
						} else {
							cli.print(String.format("%s %s, ", statLabelArray.get(i),
								WorkerStats.getModifierString(statArray[i])));
						}
					}
					boolean acceptance = cli.inputBoolean( // TODO: handle EOF
						"Do those stats fit that profile?");
					if (acceptance) {
						worker.setStats(stats);
						return worker;
					}
				}
			}
		} else {
			cli.println("No population details, so no levels.");
			WorkerStats stats = createWorkerStats(village.getRace(), 0);
			worker.setStats(stats);
			cli.println(String.format("%s is a %s from %s. Stats:", name, village.getRace(),
				village.getName()));
			int[] statArray = stats.array();
			for (int i = 0; i < 6; i++) {
				if ((i == statLabelArray.size() - 1) ||
						(i == statArray.length - 1)) {
					cli.println(String.format("%s %s", statLabelArray.get(i),
						WorkerStats.getModifierString(statArray[i])));
				} else {
					cli.print(String.format("%s %s, ", statLabelArray.get(i),
						WorkerStats.getModifierString(statArray[i])));
				}
			}
			return worker;
		}
	}

	/**
	 * Let the user create randomly-generated workers in a specific unit.
	 */
	private void createWorkersForUnit(final IDRegistrar idf, final IUnit unit) {
		int count = Optional.ofNullable(cli.inputNumber("How many workers to generate? ")).orElse(0);
		for (int i = 0; i < count; i++) {
			String race = RaceFactory.randomRace();
			Worker worker;
			String name = cli.inputString(String.format("Work is a %s. Worker name: ", race));
			if (name == null) {
				break;
			}
			worker = new Worker(name, race, idf.createID());
			int levels = (int) SingletonRandom.SINGLETON_RANDOM.ints().filter(n -> n < 20)
				.limit(3).filter(n -> n == 0).count();
			if (levels == 1) {
				cli.println("Worker has 1 Job level.");
			} else if (levels > 1) {
				cli.println(String.format("Worker has %d Job levels.", levels));
			}
			WorkerStats stats = createWorkerStats(race, levels);
			worker.setStats(stats);
			if (levels > 0) {
				cli.println("Generated stats:");
				cli.print(stats.toString());
			}
			model.addWorkerToUnit(unit, worker);
			enterWorkerJobs(unit, worker, levels);
		}
	}

	/**
	 * Let the user create randomly-generated workers, with names read from file, in a unit.
	 */
	private void createWorkersFromFile(final IDRegistrar idf, final IUnit unit) throws IOException {
		int count = Optional.ofNullable(cli.inputNumber("How many workers to generate? ")).orElse(0);
		Deque<String> names;
		String filename = cli.inputString("Filename to load names from: ");
		if (filename == null) {
			return;
		} else if (Files.exists(Paths.get(filename))) {
			names = new LinkedList<>(Files.readAllLines(Paths.get(filename)));
		} else {
			names = new LinkedList<>();
			cli.println("No such file.");
		}
		Point hqLoc;
		Optional<Point> found = model.getMap().streamLocations()
				.flatMap(l -> model.getMap().getFixtures(l).stream()
					.map(f -> Pair.with(l, f)))
				.filter(pair -> pair.getValue1().getId() == unit.getId()) // TODO: look in forts too
				.map(Pair::getValue0).findAny();
		if (found.isPresent()) {
			hqLoc = found.get();
		} else {
			cli.println("That unit's location not found in main map.");
			Point point = cli.inputPoint("Location to use for village distances:");
			if (point == null) {
				return;
			} else {
				hqLoc = point;
			}
		}
		Pathfinder pather = PathfinderFactory.pathfinder(model.getMap());
		Function<Point, Pair<Integer, Double>> travelDistance =
			(dest) -> Pair.with(pather.getTravelDistance(hqLoc, dest).getValue0(),
				model.getMapDimensions().distance(hqLoc, dest));
		List<Triplet<Integer, Double, Village>> villages = model.getMap().streamLocations()
				.flatMap(l -> model.getMap().getFixtures(l).stream()
					.filter(Village.class::isInstance).map(Village.class::cast)
					.filter(v -> v.getOwner().equals(unit.getOwner()))
					.map(v -> Pair.with(l, v)))
				.map(p -> travelDistance.apply(p.getValue0()).addAt2(p.getValue1()))
				.sorted(Comparator.comparingInt(Triplet::getValue0))
				.collect(Collectors.toList());
		int mpPerDay = Optional.ofNullable(cli.inputNumber("MP per day for village volunteers:"))
			.orElse(-1);
		for (int i = 0; i < count; i++) {
			String name;
			if (!names.isEmpty()) {
				name = names.removeFirst().trim();
			} else {
				String temp = cli.inputString("Next worker name: ");
				if (temp == null) {
					break;
				} else {
					name = temp;
				}
			}
			Village home = null;
			for (Triplet<Integer, Double, Village> triplet : villages) {
				int mpDistance = triplet.getValue0();
				double tileDistance = triplet.getValue1();
				Village village = triplet.getValue2();
				if (hasLeviedRecently(village)) {
					continue;
				} else if (SingletonRandom.SINGLETON_RANDOM.nextDouble() <
						villageChance((int) (Math.min(mpDistance / mpPerDay,
							tileDistance / 12.0)) + 1)) {
					excludedVillages.put(village, true);
					home = village;
					break;
				}
			}
			Worker worker;
			if (home == null) {
				String race = RaceFactory.randomRace();
				cli.println(String.format("Worker %s is a %s", name, race));
				worker = new Worker(name, race, idf.createID());
				int levels = (int) SingletonRandom.SINGLETON_RANDOM.ints()
					.filter(n -> n < 20).limit(3).filter(n -> n == 0)
					.count();
				if (levels == 1) {
					cli.println("Worker has 1 Job level.");
				} else if (levels>1) {
					cli.println(String.format("Worker has %d Job levels.", levels));
				}
				WorkerStats stats = createWorkerStats(race, levels);
				worker.setStats(stats);
				if (levels>0) {
					cli.println("Generated stats:");
					cli.print(stats.toString());
				}
				model.addWorkerToUnit(unit, worker);
				enterWorkerJobs(unit, worker, levels);
				cli.println(String.format("%s is a %s. Stats:", name, race));
				int[] statArray = stats.array();
				for (int j = 0; j < statLabelArray.size() && j < statArray.length; j++) {
					if (j == statLabelArray.size() - 1 || j == statArray.length - 1) {
						cli.println(String.format("%s %s", statLabelArray.get(j),
							WorkerStats.getModifierString(statArray[j])));
					} else {
						cli.print(String.format("%s %s, ", statLabelArray.get(j),
							WorkerStats.getModifierString(statArray[j])));
					}
				}
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
		List<IUnit> units = StreamSupport.stream(
				model.getUnits(player).spliterator(), false).collect(Collectors.toList());
		while (true) {
			Pair<Integer, @Nullable IUnit> chosen = cli.chooseFromList(units,
				"Which unit contains the worker in question? (Select -1 to create new.)",
				"There are no units owned by that player.", "Unit selection: ", false);
			IUnit item;
			if (chosen.getValue1() != null) {
				item = chosen.getValue1();
			} else if (chosen.getValue0() <= units.size()) {
				Point point = cli.inputPoint("Where to put new unit? ");
				String kind = point == null ? null : cli.inputString("Kind of unit: ");
				String name = kind == null ? null : cli.inputString("Unit name: ");
				if (point == null || kind == null || name == null) {
					return;
				} else {
					IUnit temp = new Unit(player, kind, name, idf.createID());
					model.addUnitAtLocation(temp, point);
					units.add(temp);
					item = temp;
				}
			} else {
				break;
			}
			Boolean load = cli.inputBooleanInSeries(
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
		IDRegistrar idf = new IDFactoryFiller().createIDFactory(
			model.streamAllMaps().toArray(IMapNG[]::new));
		// TODO: Make getPlayerChoices() return Collection
		List<Player> players = StreamSupport.stream(
				model.getPlayerChoices().spliterator(), false).collect(Collectors.toList());
		while (!players.isEmpty()) {
			Player chosen = cli.chooseFromList(players, "Which player owns the new worker(s)?",
				"There are no players shared by all the maps.", "Player selection: ",
				false).getValue1();
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
				Boolean continuation = cli.inputBoolean("Add more workers to another unit?");
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

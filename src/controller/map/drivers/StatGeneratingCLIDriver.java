package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel;
import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.workermgmt.RaceFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import util.Pair;

import static util.SingletonRandom.RANDOM;

/**
 * A driver to let the user enter pre-generated stats for existing workers or generate new
 * workers.
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
public final class StatGeneratingCLIDriver implements SimpleCLIDriver {
	/**
	 * The prompt to use to ask the user if he or she wants to load names from file and
	 * generate new stats.
	 */
	private static final String LOAD_NAMES =
			"Load names from file and use randomly generated stats? ";
	/**
	 * The prompt to use to ask the user if he or she wants to enter pre-generated stats
	 * for workers that already exist.
	 */
	private static final String PREGEN_PROMPT =
			"Enter pre-generated stats for existing workers? ";
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-t", "--stats", ParamCount.AtLeastOne,
								   "Enter worker stats or generate new workers.",
								   "Enter stats for existing workers or generate new " +
										   "workers randomly.");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * Let the user enter stats for workers already in the maps.
	 *
	 * @param model the driver model.
	 * @param cli   the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void enterStats(final IExplorationModel model,
								   final ICLIHelper cli) throws IOException {
		final List<Player> players = model.getPlayerChoices();
		final String hdr = "Which player owns the worker in question?";
		final String none = "There are no players shared by all the maps.";
		final String prompt = "Player selection: ";
		final ICLIHelper.ChoiceOperation choice =
				() -> cli.chooseFromList(players, hdr, none, prompt, true);
		for (int playerNum = choice.choose();
				(playerNum >= 0) && (playerNum < players.size());
				playerNum = choice.choose()) {
			enterStats(model, NullCleaner.assertNotNull(players.get(playerNum)), cli);
		}
	}

	/**
	 * Let the user enter stats for workers already in the maps that belong to one
	 * particular player.
	 *
	 * @param model  the driver model
	 * @param player the player owning the worker
	 * @param cli    the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void enterStats(final IExplorationModel model,
								   final Player player, final ICLIHelper cli)
			throws IOException {
		final List<IUnit> units = removeStattedUnits(model.getUnits(player));
		final String hdr = "Which unit contains the worker in question?";
		final String none = "All that player's units already have stats.";
		final String prompt = "Unit selection: ";
		final ICLIHelper.ChoiceOperation choice =
				() -> cli.chooseFromList(units, hdr, none, prompt, false);
		for (int unitNum = choice.choose(); (unitNum >= 0) && (unitNum < units.size());
				unitNum = choice.choose()) {
			final IUnit unit = units.get(unitNum);
			enterStats(model, unit, cli);
			if (!hasUnstattedWorker(model, unit.getID())) {
				units.remove(unit);
			}
		}
	}

	/**
	 * @param model the exploration model
	 * @param idNum an ID number
	 * @return true if the number designates a unit containing a worker without stats,
	 * and
	 * false otherwise.
	 */
	private static boolean hasUnstattedWorker(final IDriverModel model,
											  final int idNum) {
		final IFixture fix = find(model.getMap(), idNum);
		return (fix instanceof IUnit) && hasUnstattedWorker((IUnit) fix);
	}

	/**
	 * @param unit a unit
	 * @return whether it contains any workers without stats
	 */
	private static boolean hasUnstattedWorker(final Iterable<UnitMember> unit) {
		return StreamSupport.stream(unit.spliterator(), false).anyMatch(
				member -> (member instanceof IWorker) &&
								  (((IWorker) member).getStats() == null));
	}

	/**
	 * @param units a list of units
	 * @return a list of the units in the list that have workers without stats
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static List<IUnit> removeStattedUnits(final Collection<IUnit> units) {
		return units.stream().filter(StatGeneratingCLIDriver::hasUnstattedWorker)
					   .collect(Collectors.toList());
	}

	/**
	 * Let the user enter stats for workers already in the maps that are part of one
	 * particular unit.
	 *
	 * @param model the driver model
	 * @param unit  the unit containing the worker
	 * @param cli   the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void enterStats(final IMultiMapModel model,
								   final Iterable<UnitMember> unit, final ICLIHelper cli)
			throws IOException {
		final List<Worker> workers = StreamSupport.stream(unit.spliterator(), false)
											 .filter(Worker.class::isInstance)
											 .map(Worker.class::cast)
											 .filter(wkr -> wkr.getStats() == null)
											 .collect(Collectors.toList());
		final String hdr = "Which worker do you want to enter stats for?";
		final String none = "There are no workers without stats in that unit.";
		final String prompt = "Worker to modify: ";
		final ICLIHelper.ChoiceOperation choice =
				() -> cli.chooseFromList(workers, hdr, none, prompt, false);
		for (int workerNum = choice.choose();
				(workerNum >= 0) && (workerNum < workers.size()) && !workers.isEmpty();
				workerNum = choice.choose()) {
			enterStats(model, workers.get(workerNum).getID(), cli);
			workers.remove(workerNum);
		}
	}

	/**
	 * Let the user enter stats for a worker.
	 *
	 * @param model the driver model
	 * @param idNum the worker's ID.
	 * @param cli   the interface to the user
	 * @throws IOException on I/O error interacting with user.
	 */
	private static void enterStats(final IMultiMapModel model, final int idNum,
								   final ICLIHelper cli) throws IOException {
		final WorkerStats stats = enterStats(cli);
		for (final Pair<IMutableMapNG, Optional<Path>> pair : model.getAllMaps()) {
			final IMapNG map = pair.first();
			final IFixture fix = find(map, idNum);
			if ((fix instanceof Worker) && (((Worker) fix).getStats() == null)) {
				((Worker) fix).setStats(stats);
			}
		}
	}

	/**
	 * Let the user enter stats for a worker.
	 *
	 * @param cli the interface to the user
	 * @return the stats the user entered
	 * @throws IOException on I/O error interacting with the user.
	 */
	private static WorkerStats enterStats(final ICLIHelper cli)
			throws IOException {
		final int maxHP = cli.inputNumber("Max HP: ");
		final int strength = cli.inputNumber("Str: ");
		final int dex = cli.inputNumber("Dex: ");
		final int con = cli.inputNumber("Con: ");
		final int intel = cli.inputNumber("Int: ");
		final int wis = cli.inputNumber("Wis: ");
		final int cha = cli.inputNumber("Cha: ");
		return new WorkerStats(maxHP, maxHP, strength, dex, con, intel, wis, cha);
	}

	/**
	 * @param map   a map
	 * @param idNum an ID number
	 * @return the fixture with that ID, or null if not found
	 */
	@Nullable
	private static IFixture find(final IMapNG map, final int idNum) {
		for (final Point point : map.locations()) {
			final IFixture forest = map.getForest(point);
			if (forest != null && forest.getID() == idNum) {
				return forest;
			}
			// TODO: If Ground ever gets ID, check it here.
			for (final IFixture fixture : map.getOtherFixtures(point)) {
				if (fixture.getID() == idNum) {
					return fixture;
				} else if (fixture instanceof FixtureIterable) {
					final IFixture result =
							find((FixtureIterable<@NonNull ?>) fixture, idNum);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param iter  something containing fixtures
	 * @param idNum an ID number
	 * @return the fixture with that ID, or null if not found
	 */
	@Nullable
	private static IFixture find(final Iterable<@NonNull ? extends IFixture> iter, final
	int idNum) {
		for (final IFixture fix : iter) {
			if (fix.getID() == idNum) {
				return fix;
			} else if (fix instanceof FixtureIterable) {
				final IFixture result = find((FixtureIterable<@NonNull ?>) fix, idNum);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Allow the user to create randomly-generated workers.
	 *
	 * @param model the driver model
	 * @param idf   the ID factory
	 * @param cli   the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void createWorkers(final IExplorationModel model,
									  final IDRegistrar idf, final ICLIHelper cli)
			throws IOException {
		final List<Player> players = model.getPlayerChoices();
		final String hdr = "Which player owns the new worker(s)?";
		final String none = "There are no players shared by all the maps.";
		final String prompt = "Player selection: ";
		final ICLIHelper.ChoiceOperation choice =
				() -> cli.chooseFromList(players, hdr, none, prompt, false);
		for (int playerNum = choice.choose();
				(playerNum >= 0) && (playerNum < players.size());
				playerNum = choice.choose()) {
			createWorkersForPlayer(model, idf,
					NullCleaner.assertNotNull(players.get(playerNum)), cli);
		}
	}

	/**
	 * Allow the user to create randomly-generated workers belonging to a particular
	 * player.
	 *
	 * @param model  the driver model
	 * @param idf    the ID factory
	 * @param player the player to own the workers
	 * @param cli    the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void createWorkersForPlayer(final IExplorationModel model,
											   final IDRegistrar idf, final Player
																			  player,
											   final ICLIHelper cli)
			throws IOException {
		boolean again = true;
		while (again) {
			final IUnit unit;
			if (cli.inputBooleanInSeries("Add worker(s) to an existing unit? ")) {
				final List<IUnit> units = model.getUnits(player);
				final int unitNum = cli.chooseFromList(units,
						"Which unit contains the worker in question?",
						"There are no units owned by that player",
						"Unit selection: ", false);
				if ((unitNum >= 0) && (unitNum < units.size())) {
					unit = units.get(unitNum);
				} else {
					break;
				}
			} else {
				final Point point = cli.inputPoint("Where to put new unit? ");
				//noinspection ObjectAllocationInLoop
				unit = new Unit(player, cli.inputString("Kind of unit: "),
									   cli.inputString("Unit name: "), idf.createID());
				for (final Pair<IMutableMapNG, Optional<Path>> pair : model.getAllMaps
																					()) {
					pair.first().addFixture(point, unit);
				}
			}
			if (cli.inputBooleanInSeries(LOAD_NAMES)) {
				createWorkersFromFile(model, idf, unit, cli);
			} else {
				createWorkersForUnit(model, idf, unit, cli);
			}
			again = cli.inputBoolean("Add more workers to another unit? ");
		}
	}

	/**
	 * Let the user create randomly-generated workers in a unit.
	 *
	 * @param model the driver model
	 * @param idf   the ID factory.
	 * @param unit  the unit to contain them.
	 * @param cli   the interface to the user
	 * @throws IOException on I/O error interacting with the user
	 */
	private static void createWorkersForUnit(final IMultiMapModel model,
											 final IDRegistrar idf, final IFixture unit,
											 final ICLIHelper cli)
			throws IOException {
		final int count = cli.inputNumber("How many workers to generate? ");
		for (int i = 0; i < count; i++) {
			final IWorker worker = createSingleWorker(idf, cli);
			addWorkerToUnit(model, unit, worker);
		}
	}

	/**
	 * Add a worker to a unit in all maps.
	 * @param model the map model
	 * @param unit the unit to use as our base for finding corresponding units
	 * @param worker the worker to add to them.
	 */
	private static void addWorkerToUnit(final IMultiMapModel model, final IFixture unit,
										final IWorker worker) {
		for (final Pair<IMutableMapNG, Optional<Path>> pair : model.getAllMaps()) {
			final IFixture fix = find(pair.first(), unit.getID());
			if (fix instanceof IUnit) {
				((IUnit) fix).addMember(worker.copy(false));
				final int turn = pair.first().getCurrentTurn();
				if (((IUnit) fix).getOrders(turn).isEmpty()) {
					((IUnit) fix).setOrders(turn, "TODO: assign");
				}
			}
		}
	}

	/**
	 * Let the user create randomly-generated workers, with names read from file, in a
	 * unit.
	 *
	 * @param model the driver model
	 * @param idf   the ID factory.
	 * @param unit  the unit to contain them.
	 * @param cli   the interface to the user
	 * @throws IOException on I/O error interacting with the user
	 */
	private static void createWorkersFromFile(final IMultiMapModel model,
											  final IDRegistrar idf, final IFixture unit,
											  final ICLIHelper cli)
			throws IOException {
		final int count = cli.inputNumber("How many workers to generate? ");
		final String filename = cli.inputString("Filename to load names from: ");
		final List<String> names = new ArrayList<>();
		try (final FileSystem fileSystem = FileSystems.getDefault()) {
			names.addAll(
					Files.readAllLines(fileSystem.getPath(filename),
							Charset.defaultCharset()));
		} catch (final UnsupportedOperationException ignored) {
			// Can't close a FileSystem, but IDEA screams if we don't use try-with-res
			// ...
		}
		for (int i = 0; i < count; i++) {
			final IWorker worker =
					createWorkerFromNameFile(
							NullCleaner.assertNotNull(names.get(i).trim()), idf, cli);
			addWorkerToUnit(model, unit, worker);
		}
	}

	/**
	 * Let the user create a randomly-generated worker.
	 *
	 * Each non-human race has a 1 in 20 chance of coming up; stats are all 3d6; there
	 * are
	 * five 1-in-20 chances of starting with a level in some Job, and the user will be
	 * prompted for what Job for each.
	 *
	 * @param idf the ID factory
	 * @param cli the interface to the user
	 * @return the generated worker
	 * @throws IOException on I/O error interacting with the user
	 */
	private static IWorker createSingleWorker(final IDRegistrar idf,
											  final ICLIHelper cli) throws IOException {
		final String race = RaceFactory.getRace();
		final String name = cli.inputString("Worker is a " + race
													+ ". Worker name: ");
		final Worker retval = new Worker(name, race, idf.createID());
		int levels = 0;
		for (int i = 0; i < 3; i++) {
			if (RANDOM.nextInt(20) == 0) {
				levels++;
			}
		}
		printLevelCount(cli, levels);

		final boolean pregenStats =
				cli.inputBooleanInSeries("Enter pre-generated stats? ");
		if (pregenStats) {
			retval.setStats(enterStats(cli));
		} else {
			final WorkerStats stats = createWorkerStats(race, levels, cli);
			retval.setStats(stats);
			if (levels > 0) {
				cli.println("Generated stats:");
				cli.print(stats.toString());
			}
		}
		enterWorkerJobs(cli, retval, levels);
		return retval;
	}

	/**
	 * Enter which Jobs a worker's levels are in.
	 * @param cli the interface for user I/O
	 * @param worker the worker in question
	 * @param levels how many Job levels he or she has
	 * @throws IOException on I/O error
	 */
	private static void enterWorkerJobs(final ICLIHelper cli, final Worker worker,
										final int levels) throws IOException {
		for (int i = 0; i < levels; i++) {
			final String jobName =
					cli.inputString("Which Job does worker have a level in? ");
			final Optional<IJob> existing = Optional.ofNullable(worker.getJob(jobName));
			if (existing.isPresent()) {
				final IJob job = existing.get();
				job.setLevel(job.getLevel() + 1);
			} else {
				//noinspection ObjectAllocationInLoop
				worker.addJob(new Job(jobName, 1));
			}
		}
	}

	/**
	 * Create randomly-generated stats for a worker, with racial adjustments applied.
	 *
	 * @param race   the worker's race
	 * @param levels how many Job levels the worker has
	 * @param cli    to ask any questions of the user
	 * @return the stats
	 * @throws IOException on I/O error interacting with user
	 */
	private static WorkerStats createWorkerStats(final String race, final int levels,
												 final ICLIHelper cli)
			throws IOException {
		final WorkerStats base =
				WorkerStats.factory(StatGeneratingCLIDriver::threeDeeSix);
		final int lowestScore = getMinIndex(base.toArray());
		final WorkerStats racialBonus;
		switch (race) {
		case "dwarf":
			racialBonus = WorkerStats.factory(2, 0, 2, 0, 0, -2);
			break;
		case "elf":
			racialBonus = WorkerStats.factory(-1, 2, 0, 1, 0, 0);
			break;
		case "gnome":
			racialBonus = WorkerStats.factory(-2, 1, -1, 2, 0, 0);
			break;
		case "half-elf":
			racialBonus = WorkerStats.factory(0, 1, 0, 1, 0, 0);
			break;
		case "Danan":
			racialBonus = WorkerStats.factory(-2, 1, 1, 1, -2, 1);
			break;
		case "human": // fall through; treat undefined as human
		default:
			final int chosenBonus = cli.chooseStringFromList(
					Arrays.asList("Strength", "Dexterity", "Constitution",
							"Intelligence",
							"Wisdom", "Charisma", "Lowest"),
					"Character is a " + race + "; which stat should get a +2 bonus?", "",
					"Stat for bonus: ", false);
			final int bonusStat;
			if (chosenBonus < 6) {
				bonusStat = chosenBonus;
			} else {
				bonusStat = lowestScore;
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
			case 5: // handle default to appease compiler
			default:
				racialBonus = WorkerStats.factory(0, 0, 0, 0, 0, 2);
				break;
			}
			break;
		}
		final int conBonus = WorkerStats.getModifier(
				base.getConstitution() + racialBonus.getConstitution());
		return new WorkerStats(8 + conBonus + rollDeeEight(levels, conBonus), base,
									  racialBonus);
	}
	/**
	 * @param array an array of ints
	 * @return the index of the lowest value
	 */
	private static final int getMinIndex(final int[] array) {
		int retval = 0;
		for (int i = 0; i < array.length; i++) {
			final int value = array[i];
			if (value > array[retval]) {
				retval = i;
			}
		}
		return retval;
	}

	/**
	 * Create a randomly-generated worker using a name from file, asking the user for
	 * Jobs
	 * and such but randomly generating stats.
	 *
	 * Each non-human race has a 1 in 20 chance of coming up; stats are all 3d6; there
	 * are
	 * five 1-in-20 chances of starting with a level in some Job, and the user will be
	 * prompted for what Job for each.
	 *
	 * @param name the name of the worker
	 * @param idf  the ID factory
	 * @param cli  the interface to the user
	 * @return the generated worker
	 * @throws IOException on I/O error interacting with the user
	 */
	private static IWorker createWorkerFromNameFile(final String name,
													final IDRegistrar idf,
													final ICLIHelper cli)
			throws IOException {
		final String race = RaceFactory.getRace();
		cli.printf("Worker %s is a %s%n", name, race);
		final Worker retval = new Worker(name, race, idf.createID());
		int levels = 0;
		for (int i = 0; i < 3; i++) {
			if (RANDOM.nextInt(20) == 0) {
				levels++;
			}
		}
		final WorkerStats stats = createWorkerStats(race, levels, cli);
		retval.setStats(stats);
		printLevelCount(cli, levels);
		cli.println("Worker stats:");
		enterWorkerJobs(cli, retval, levels);
		return retval;
	}

	/**
	 * @param times how many times to roll
	 * @param bonus the bonus to apply to each roll
	 * @return the result of rolling
	 */
	private static int rollDeeEight(final int times, final int bonus) {
		int total = 0;
		for (int i = 0; i < times; i++) {
			total += RANDOM.nextInt(8) + bonus + 1;
		}
		return total;
	}

	/**
	 * @return the result of simulating a 3d6 roll.
	 */
	private static int threeDeeSix() {
		final Random random = RANDOM;
		return random.nextInt(6) + random.nextInt(6) + random.nextInt(6) + 3;
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model)
			throws DriverFailedException {
		final IExplorationModel driverModel;
		if (model instanceof IExplorationModel) {
			driverModel = (IExplorationModel) model;
		} else {
			driverModel = new ExplorationModel(model);
		}
		try {
			if (cli.inputBooleanInSeries(PREGEN_PROMPT)) {
				enterStats(driverModel, cli);
			} else {
				createWorkers(driverModel, IDFactoryFiller.createFactory(driverModel),
						cli);
			}
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error interacting with user", except);
		}
	}
	/**
	 * Print a message saying how many levels the worker has, pluralizing or not as
	 * appropriate. If no levels, do nothing.
	 * @param cli the interface for user I/O
	 * @param levels how many levels the worker has
	 * @throws IOException on I/O error
	 */
	private static void printLevelCount(final ICLIHelper cli, final int levels) {
		if (levels == 1) {
			cli.println("Worker has 1 Job level.");
		} else if (levels > 1) {
			cli.printf("Worker has %d Job levels%n", Integer.valueOf(levels));
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "StatGeneratingCLIDriver";
	}
}

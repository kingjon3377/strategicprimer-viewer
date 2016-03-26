package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
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
 * A driver to let the user enter pregenerated stats for existing workers or generate new
 * workers.
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
public final class StatGeneratingCLIDriver implements SimpleCLIDriver {
	/**
	 * The prompt to use to ask the user if he or she wants to load names from file and
	 * generate new stats.
	 */
	private static final String LOAD_NAMES =
			"Load names from file and use randomly generated stats? ";
	/**
	 * The prompt to use to ask the user if he or she wants to enter pregenerated stats
	 * for workers that already exist.
	 */
	private static final String PREGEN_PROMPT =
			"Enter pregenerated stats for existing workers? ";
	/**
	 * The basis on which stat modifiers are calculated. Every two points above this
	 * gives
	 * +1, and every two points below this gives -1.
	 */
	private static final int STAT_BASIS = 10;
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-t", "--stats", ParamCount.AtLeastOne,
								   "Enter worker stats or generate new workers.",
								   "Enter stats for existing workers or generate new " +
										   "workers randomly.",
								   StatGeneratingCLIDriver.class);

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * Run the driver.
	 *
	 * @param model the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		final IExplorationModel emodel;
		if (model instanceof IExplorationModel) {
			emodel = (IExplorationModel) model;
		} else {
			emodel = new ExplorationModel(model);
		}
		try (final ICLIHelper cli = new CLIHelper()) {
			if (cli.inputBoolean(PREGEN_PROMPT)) {
				enterStats(emodel, cli);
			} else {
				createWorkers(emodel, IDFactoryFiller.createFactory(emodel), cli);
			}
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error interacting with user", except);
		}
	}

	/**
	 * Let the user enter stats for workers already in the maps.
	 *
	 * @param model the driver model.
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void enterStats(final IExplorationModel model,
			final ICLIHelper cli) throws IOException {
		final List<Player> players = model.getPlayerChoices();
		final String hdr = "Which player owns the worker in question?";
		final String none = "There are no players shared by all the maps.";
		final String prpt = "Player selection: ";
		for (int playerNum = cli.chooseFromList(players, hdr, none, prpt,
				true); (playerNum >= 0)
							   && (playerNum < players.size()); playerNum = cli
																				  .chooseFromList(
																						  players,
																						  hdr,
																						  none,
																						  prpt,
																						  true)) {
			enterStats(model, NullCleaner.assertNotNull(players.get(playerNum)), cli);
		}
	}

	/**
	 * Let the user enter stats for workers already in the maps that belong to one
	 * particular player.
	 *
	 * @param model  the driver model
	 * @param player the player owning the worker
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void enterStats(final IExplorationModel model,
			final Player player, final ICLIHelper cli) throws IOException {
		final List<IUnit> units = removeStattedUnits(model.getUnits(player));
		final String hdr = "Which unit contains the worker in question?";
		final String none = "All that player's units are already fully statted.";
		final String prpt = "Unit selection: ";
		for (int unitNum = cli.chooseFromList(units, hdr, none, prpt, false);
				(unitNum >= 0) && (unitNum < units.size());
				unitNum = cli.chooseFromList(units, hdr, none, prpt, false)) {
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
	 * @return true if the number designates a unit containing an unstatted worker, and
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
		return StreamSupport.stream(unit.spliterator(), false)
					   .anyMatch(member -> (member instanceof Worker) &&
												   (((Worker) member).getStats() == null));
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
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void enterStats(final IMultiMapModel model,
			final Iterable<UnitMember> unit, final ICLIHelper cli)
					throws IOException {
		final List<Worker> workers = NullCleaner.assertNotNull(StreamSupport
				.stream(unit.spliterator(), false)
				.filter(Worker.class::isInstance).map(Worker.class::cast)
				.filter(wkr -> wkr.getStats() == null)
				.collect(Collectors.toList()));
		final String hdr = "Which worker do you want to enter stats for?";
		final String none = "There are no owkers without stats in that unit.";
		final String prpt = "Worker to modify: ";
		for (int workerNum = cli.chooseFromList(workers, hdr, none, prpt,
				false); (workerNum >= 0) && (workerNum < workers.size())
								&& !workers.isEmpty();
				workerNum = cli.chooseFromList(workers,
						hdr, none, prpt, false)) {
			enterStats(model, workers.get(workerNum).getID(), cli);
			workers.remove(workerNum);
		}
	}

	/**
	 * Let the user enter stats for a worker.
	 *
	 * @param model the driver model
	 * @param idNum the worker's ID.
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with user.
	 */
	private static void enterStats(final IMultiMapModel model, final int idNum,
			final ICLIHelper cli) throws IOException {
		final WorkerStats stats = enterStats(cli);
		for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
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
			// TODO: If Ground or Forest ever gets ID, check it here.
			for (final IFixture fixture : map.getOtherFixtures(point)) {
				if (fixture.getID() == idNum) {
					return fixture;
				} else if (fixture instanceof FixtureIterable) {
					final IFixture result =
							find((FixtureIterable<@NonNull ?>) fixture, idNum);
					if (result != null) {
						return result; // NOPMD
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
				return fix; // NOPMD
			} else if (fix instanceof FixtureIterable) {
				final IFixture result = find((FixtureIterable<@NonNull ?>) fix, idNum);
				if (result != null) {
					return result; // NOPMD
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
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void createWorkers(final IExplorationModel model,
			final IDFactory idf, final ICLIHelper cli) throws IOException {
		final List<Player> players = model.getPlayerChoices();
		final String hdr = "Which player owns the new worker(s)?";
		final String none = "There are no players shared by all the maps.";
		final String prpt = "Player selection: ";
		for (int playerNum = cli.chooseFromList(players, hdr, none, prpt,
				false); (playerNum >= 0)
								&& (playerNum < players.size()); playerNum = cli
																				   .chooseFromList(
																						   players,
																						   hdr,
																						   none,
																						   prpt,
																						   false)) {
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
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with user
	 */
	private static void createWorkersForPlayer(final IExplorationModel model,
			final IDFactory idf, final Player player, final ICLIHelper cli)
					throws IOException {
		boolean again = true;
		while (again) {
			if (cli.inputBoolean("Add worker(s) to an existing unit? ")) {
				final List<IUnit> units = model.getUnits(player);
				final int unitNum = cli.chooseFromList(units,
						"Which unit contains the worker in question?",
						"There are no units owned by that player",
						"Unit selection: ", false);
				if ((unitNum >= 0) && (unitNum < units.size())) {
					final IUnit unit = units.get(unitNum);
					if (cli.inputBoolean(LOAD_NAMES)) {
						createWorkersFromFile(model, idf, unit, cli);
					} else {
						createWorkersForUnit(model, idf, unit, cli);
					}
				}
			} else {
				final Point point = PointFactory.point(
						cli.inputNumber("Row to put new unit: "),
						cli.inputNumber("Column to put new unit: "));
				//noinspection ObjectAllocationInLoop
				final TileFixture unit = new Unit(player, // NOPMD
														 cli.inputString(
																 "Kind of unit: "),
														 cli.inputString("Unit name: "),
														 idf.createID());
				for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
					pair.first().addFixture(point, unit);
				}
				if (cli.inputBoolean(LOAD_NAMES)) {
					createWorkersFromFile(model, idf, unit, cli);
				} else {
					createWorkersForUnit(model, idf, unit, cli);
				}
			}
			again = cli
							.inputBoolean("Add more workers to another unit? ");
		}
	}

	/**
	 * Let the user create randomly-generated workers in a unit.
	 *
	 * @param model the driver model
	 * @param idf   the ID factory.
	 * @param unit  the unit to contain them.
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with the user
	 */
	private static void createWorkersForUnit(final IMultiMapModel model,
			final IDFactory idf, final IFixture unit, final ICLIHelper cli)
					throws IOException {
		final int count = cli.inputNumber("How many workers to generate? ");
		for (int i = 0; i < count; i++) {
			final Worker worker = createSingleWorker(idf, cli);
			for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
				final IFixture fix = find(pair.first(), unit.getID());
				if (fix instanceof IUnit) {
					((IUnit) fix).addMember(worker);
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
	 * @param cli the interface to the user
	 * @throws IOException on I/O error interacting with the user
	 */
	private static void createWorkersFromFile(final IMultiMapModel model,
			final IDFactory idf, final IFixture unit, final ICLIHelper cli)
					throws IOException {
		final int count = cli.inputNumber("How many workers to generate? ");
		final String filename = cli.inputString("Filename to load names from: ");
		final List<String> names;
		try (final FileSystem fsys = FileSystems.getDefault()) {
			names = NullCleaner.assertNotNull(Files.readAllLines(
					fsys.getPath(filename), Charset.defaultCharset()));
		}
		for (int i = 0; i < count; i++) {
			final Worker worker =
					createWorkerFromNameFile(
							NullCleaner.assertNotNull(names.get(i).trim()), idf, cli);
			for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
				final IFixture fix = find(pair.first(), unit.getID());
				if (fix instanceof IUnit) {
					((IUnit) fix).addMember(worker);
				}
			}
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
	private static Worker createSingleWorker(final IDFactory idf,
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
		if (levels > 1) {
			cli.printf("Worker has %d job levels%n", Integer.valueOf(levels));
		} else if (levels == 1) {
			cli.println("Worker has 1 job level");
		}
		final boolean pregenStats = cli
											.inputBoolean("Enter pregenerated stats? ");
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
		for (int i = 0; i < levels; i++) {
			//noinspection ObjectAllocationInLoop
			retval.addJob(
					new Job(cli.inputString("Which Job does worker have a level in? "),
								   1));
		}
		return retval;
	}
	/**
	 * Create randomly-generated stats for a worker, with racial adjustments applied.
	 * @param race the worker's race
	 * @param levels how many Job levels the worker has
	 * @param cli to ask any questions of the user
	 * @throws IOException on I/O error interacting with user
	 * @return the stats
	 */
	private static WorkerStats createWorkerStats(final String race, final int levels,
	                                             final ICLIHelper cli)
			throws IOException {
		final int racialStrBonus;
		final int racialDexBonus;
		final int racialConBonus;
		final int racialIntBonus;
		final int racialWisBonus;
		final int racialChaBonus;
		switch (race) {
		case "dwarf":
			racialDexBonus = 0;
			racialStrBonus = 2;
			racialChaBonus = -2;
			racialConBonus = 2;
			racialIntBonus = 0;
			racialWisBonus = 0;
			break;
		case "elf":
			racialDexBonus = 2;
			racialStrBonus = -1;
			racialIntBonus = 1;
			racialConBonus = 0;
			racialWisBonus = 0;
			racialChaBonus = 0;
			break;
		case "gnome":
			racialIntBonus = 2;
			racialStrBonus = -2;
			racialConBonus = 1;
			racialDexBonus = 1;
			racialWisBonus = 0;
			racialChaBonus = 0;
			break;
		case "half-elf":
			racialDexBonus = 1;
			racialIntBonus = 1;
			racialStrBonus = 0;
			racialConBonus = 0;
			racialWisBonus = 0;
			racialChaBonus = 0;
			break;
		case "Danan":
			racialStrBonus = -2;
			racialDexBonus = 1;
			racialWisBonus = -2;
			racialConBonus = 1;
			racialIntBonus = 1;
			racialChaBonus = 1;
			break;
		case "human": // fall through; treat undefined as human
		default:
			switch (cli.chooseStringFromList(
					Arrays.asList("Strength", "Dexterity", "Constitution",
							"Intelligence", "Wisdom", "Charisma"),
					"Character is a " + race + "; which stat should get a +2 bonus?",
					"", "Stat for bonus: ", false)) {
			case 0:
				racialStrBonus = 2;
				racialDexBonus = 0;
				racialConBonus = 0;
				racialIntBonus = 0;
				racialWisBonus = 0;
				racialChaBonus = 0;
				break;
			case 1:
				racialStrBonus = 0;
				racialDexBonus = 2;
				racialConBonus = 0;
				racialIntBonus = 0;
				racialWisBonus = 0;
				racialChaBonus = 0;
				break;
			case 2:
				racialStrBonus = 0;
				racialDexBonus = 0;
				racialConBonus = 2;
				racialIntBonus = 0;
				racialWisBonus = 0;
				racialChaBonus = 0;
				break;
			case 3:
				racialStrBonus = 0;
				racialDexBonus = 0;
				racialConBonus = 0;
				racialIntBonus = 2;
				racialWisBonus = 0;
				racialChaBonus = 0;
				break;
			case 4:
				racialStrBonus = 0;
				racialDexBonus = 0;
				racialConBonus = 0;
				racialIntBonus = 0;
				racialWisBonus = 2;
				racialChaBonus = 0;
				break;
			case 5: // handle default to appease compiler
			default:
				racialStrBonus = 0;
				racialDexBonus = 0;
				racialConBonus = 0;
				racialIntBonus = 0;
				racialWisBonus = 0;
				racialChaBonus = 2;
				break;
			}
			break;
		}
		final int constitution = threeDeeSix() + racialConBonus;
		final int conBonus = (constitution - STAT_BASIS) / 2;
		final int hitp = 8 + conBonus + rollDeeEight(levels, conBonus);
		return new WorkerStats(hitp, hitp, threeDeeSix() + racialStrBonus,
				                      threeDeeSix() + racialDexBonus, constitution,
				                      threeDeeSix() + racialIntBonus,
				                      threeDeeSix() + racialWisBonus,
				                      threeDeeSix() + racialChaBonus);
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
	 * @param cli the interface to the user
	 * @return the generated worker
	 * @throws IOException on I/O error interacting with the user
	 */
	private static Worker createWorkerFromNameFile(final String name,
			final IDFactory idf, final ICLIHelper cli) throws IOException {
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
		if (levels > 1) {
			cli.printf("Worker has %d Job levels.%n", Integer.valueOf(levels));
			cli.println("Worker stats:");
			cli.print(stats.toString());
		} else if (levels == 1) {
			cli.println("Worker has 1 Job level.");
			cli.println("Worker stats:");
			cli.print(stats.toString());
		}
		for (int i = 0; i < levels; i++) {
			//noinspection ObjectAllocationInLoop
			retval.addJob(new Job(// NOPMD
										 cli.inputString(
												 "Which Job does worker have a level in? "),
										 1));
		}
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
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "StatGeneratingCLIDriver";
	}
}

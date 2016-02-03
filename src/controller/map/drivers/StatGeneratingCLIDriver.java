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
import view.util.SystemOut;

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
			new DriverUsage(false, "-t", "--stats", ParamCount.Many,
								   "Enter worker stats or generate new workers.",
								   "Enter stats for existing workers or generate new " +
										   "workers randomly.",
								   StatGeneratingCLIDriver.class);
	/**
	 * Helper to get numbers from the user, etc.
	 */
	private final ICLIHelper cli = new CLIHelper();

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
	 * @param dmodel the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final IDriverModel dmodel) throws DriverFailedException {
		final IExplorationModel model;
		if (dmodel instanceof IExplorationModel) {
			model = (IExplorationModel) dmodel;
		} else {
			model = new ExplorationModel(dmodel);
		}
		try {
			if (cli.inputBoolean(PREGEN_PROMPT)) {
				enterStats(model);
			} else {
				createWorkers(model, IDFactoryFiller.createFactory(model));
			}
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error interacting with user", except);
		}
	}

	/**
	 * Let the user enter stats for workers already in the maps.
	 *
	 * @param model the driver model.
	 * @throws IOException on I/O error interacting with user
	 */
	private void enterStats(final IExplorationModel model) throws IOException {
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
			enterStats(model, NullCleaner.assertNotNull(players.get(playerNum)));
		}
	}

	/**
	 * Let the user enter stats for workers already in the maps that belong to one
	 * particular player.
	 *
	 * @param model  the driver model
	 * @param player the player owning the worker
	 * @throws IOException on I/O error interacting with user
	 */
	private void enterStats(final IExplorationModel model, final Player player)
			throws IOException {
		final List<IUnit> units = removeStattedUnits(model.getUnits(player));
		final String hdr = "Which unit contains the worker in question?";
		final String none = "All that player's units are already fully statted.";
		final String prpt = "Unit selection: ";
		for (int unitNum = cli.chooseFromList(units, hdr, none, prpt, false);
				(unitNum >= 0) && (unitNum < units.size());
				unitNum = cli.chooseFromList(units, hdr, none, prpt, false)) {
			final IUnit unit = units.get(unitNum);
			enterStats(model, unit);
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
	 * @throws IOException on I/O error interacting with user
	 */
	private void enterStats(final IMultiMapModel model, final Iterable<UnitMember> unit)
			throws IOException {
		final List<Worker> workers = StreamSupport.stream(unit.spliterator(), false)
				                             .filter(Worker.class::isInstance)
				                             .map(Worker.class::cast)
				                             .filter(wkr -> wkr.getStats() == null)
				                             .collect(Collectors.toList());
		final String hdr = "Which worker do you want to enter stats for?";
		final String none = "There are no owkers without stats in that unit.";
		final String prpt = "Worker to modify: ";
		for (int workerNum = cli.chooseFromList(workers, hdr, none, prpt,
				false); (workerNum >= 0) && (workerNum < workers.size())
								&& !workers.isEmpty();
				workerNum = cli.chooseFromList(workers,
						hdr, none, prpt, false)) {
			enterStats(model, workers.get(workerNum).getID());
			workers.remove(workerNum);
		}
	}

	/**
	 * Let the user enter stats for a worker.
	 *
	 * @param model the driver model
	 * @param idNum the worker's ID.
	 * @throws IOException on I/O error interacting with user.
	 */
	private void enterStats(final IMultiMapModel model, final int idNum)
			throws IOException {
		final WorkerStats stats = enterStats();
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
	 * @return the stats the user entered
	 * @throws IOException on I/O error interacting with the user.
	 */
	private WorkerStats enterStats() throws IOException {
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
	 * @throws IOException on I/O error interacting with user
	 */
	private void createWorkers(final IExplorationModel model,
							   final IDFactory idf) throws IOException {
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
					NullCleaner.assertNotNull(players.get(playerNum)));
		}
	}

	/**
	 * Allow the user to create randomly-generated workers belonging to a particular
	 * player.
	 *
	 * @param model  the driver model
	 * @param idf    the ID factory
	 * @param player the player to own the workers
	 * @throws IOException on I/O error interacting with user
	 */
	private void createWorkersForPlayer(final IExplorationModel model,
										final IDFactory idf, final Player player)
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
						createWorkersFromFile(model, idf, unit);
					} else {
						createWorkersForUnit(model, idf, unit);
					}
				}
			} else {
				final Point point = PointFactory.point(
						cli.inputNumber("Row to put new unit: "),
						cli.inputNumber("Column to put new unit: "));
				final TileFixture unit = new Unit(player, // NOPMD
														 cli.inputString(
																 "Kind of unit: "),
														 cli.inputString("Unit name: "),
														 idf.createID());
				for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
					pair.first().addFixture(point, unit);
				}
				if (cli.inputBoolean(LOAD_NAMES)) {
					createWorkersFromFile(model, idf, unit);
				} else {
					createWorkersForUnit(model, idf, unit);
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
	 * @throws IOException on I/O error interacting with the user
	 */
	private void createWorkersForUnit(final IMultiMapModel model,
									  final IDFactory idf, final IFixture unit)
			throws IOException {
		final int count = cli.inputNumber("How many workers to generate? ");
		for (int i = 0; i < count; i++) {
			final Worker worker = createSingleWorker(idf);
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
	 * @throws IOException on I/O error interacting with the user
	 */
	private void createWorkersFromFile(final IMultiMapModel model,
									   final IDFactory idf, final IFixture unit)
			throws IOException {
		final int count = cli.inputNumber("How many workers to generate? ");
		final String filename = cli.inputString("Filename to load names from: ");
		final List<String> names;
		try (final FileSystem fsys = FileSystems.getDefault()) {
			names = Files.readAllLines(fsys.getPath(filename), Charset.defaultCharset());
		}
		for (int i = 0; i < count; i++) {
			final Worker worker =
					createWorkerFromNameFile(
							NullCleaner.assertNotNull(names.get(i).trim()), idf);
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
	 * TODO: racial adjustments to stats.
	 *
	 * @param idf the ID factory
	 * @return the generated worker
	 * @throws IOException on I/O error interacting with the user
	 */
	private Worker createSingleWorker(final IDFactory idf) throws IOException {
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
			SystemOut.SYS_OUT.print("Worker has ");
			SystemOut.SYS_OUT.print(levels);
			SystemOut.SYS_OUT.println(" job levels");
		} else if (levels == 1) {
			SystemOut.SYS_OUT.println("Worker has 1 job level");
		}
		final boolean pregenStats = cli
											.inputBoolean("Enter pregenerated stats? ");
		if (pregenStats) {
			retval.setStats(enterStats());
		} else {
			final int constitution = threeDeeSix();
			final int conBonus = (constitution - STAT_BASIS) / 2;
			final int hitp = 8 + conBonus + rollDeeEight(levels, conBonus);
			final WorkerStats stats = new WorkerStats(hitp, hitp, threeDeeSix(),
															 threeDeeSix(), constitution,
															 threeDeeSix(),
															 threeDeeSix(),
															 threeDeeSix());
			retval.setStats(stats);
			if (levels > 0) {
				SystemOut.SYS_OUT.println("Generated stats:");
				SystemOut.SYS_OUT.print(stats);
			}
		}
		for (int i = 0; i < levels; i++) {
			retval.addJob(
					new Job(cli.inputString("Which Job does worker have a level in? "),
								   1));
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
	 * TODO: racial adjustments to stats.
	 *
	 * @param name the name of the worker
	 * @param idf  the ID factory
	 * @return the generated worker
	 * @throws IOException on I/O error interacting with the user
	 */
	private Worker createWorkerFromNameFile(final String name, final IDFactory idf)
			throws IOException {
		final String race = RaceFactory.getRace();
		SystemOut.SYS_OUT.printf("Worker %s is a %s%n", name, race);
		final Worker retval = new Worker(name, race, idf.createID());
		int levels = 0;
		for (int i = 0; i < 3; i++) {
			if (RANDOM.nextInt(20) == 0) {
				levels++;
			}
		}
		final int constitution = threeDeeSix();
		final int conBonus = (constitution - STAT_BASIS) / 2;
		final int hitp = 8 + conBonus + rollDeeEight(levels, conBonus);
		final WorkerStats stats = new WorkerStats(hitp, hitp, threeDeeSix(),
														 threeDeeSix(), constitution,
														 threeDeeSix(), threeDeeSix(),
														 threeDeeSix());
		retval.setStats(stats);
		if (levels > 1) {
			SystemOut.SYS_OUT
					.printf("Worker has %d Job levels.%n", Integer.valueOf(levels));
			SystemOut.SYS_OUT.println("Worker stats:");
			SystemOut.SYS_OUT.print(stats);
		} else if (levels == 1) {
			SystemOut.SYS_OUT.println("Worker has 1 Job level.");
			SystemOut.SYS_OUT.println("Worker stats:");
			SystemOut.SYS_OUT.print(stats);
		}
		for (int i = 0; i < levels; i++) {
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

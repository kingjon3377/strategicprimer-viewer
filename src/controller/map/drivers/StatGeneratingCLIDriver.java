package controller.map.drivers;

import static util.SingletonRandom.RANDOM;
import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel;
import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMap;
import model.map.IMutableTile;
import model.map.IMutableTileCollection;
import model.map.ITile;
import model.map.ITileCollection;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.workermgmt.RaceFactory;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;
import util.Pair;
import util.SingletonRandom;
import util.Warning;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.CLIHelper;
import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to let the user enter pregenerated stats for existing workers or
 * generate new workers.
 *
 * @author Jonathan Lovelace
 *
 */
public class StatGeneratingCLIDriver implements ISPDriver {
	/**
	 * The prompt to use to ask the user if he or she wants to enter
	 * pregenerated stats for workers that already exist.
	 */
	private static final String PREGEN_PROMPT =
			"Enter pregenerated stats for existing workers? ";
	/**
	 * The basis on which stat modifiers are calculated. Every two points above
	 * this gives +1, and every two points below this gives -1.
	 */
	private static final int STAT_BASIS = 10;
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(
			false,
			"-t",
			"--stats",
			ParamCount.Many,
			"Enter worker stats or generate new workers.",
			"Enter stats for existing workers or generate new workers randomly.",
			StatGeneratingCLIDriver.class);
	/**
	 * Helper to get numbers from the user, etc.
	 */
	private final CLIHelper helper = new CLIHelper();

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE_OBJ.getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}

	/**
	 * Start the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if the driver failed
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		// ESCA-JAVA0177:
		final IExplorationModel model; // NOPMD
		try {
			model = readMaps(args);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error reading maps", except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in map file", except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP format error in map file",
					except);
		}
		try {
			if (helper.inputBoolean(PREGEN_PROMPT)) {
				enterStats(model);
			} else {
				createWorkers(model, IDFactoryFiller.createFactory(model));
			}
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error interacting with user",
					except);
		}
		try {
			writeMaps(model);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error writing to a map file",
					except);
		}
	}

	/**
	 * Write maps to disk.
	 *
	 * @param model the model containing all the maps
	 * @throws IOException on I/O error
	 */
	private static void writeMaps(final IExplorationModel model)
			throws IOException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		for (final Pair<IMap, File> pair : model.getAllMaps()) {
			reader.write(pair.second(), pair.first());
		}
	}

	/**
	 * Read maps.
	 *
	 * @param filenames the files to read from
	 * @return an exploration-model containing all of them
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on basic file I/O error
	 */
	private static ExplorationModel readMaps(final String[] filenames)
			throws IOException, XMLStreamException, SPFormatException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final File firstFile = new File(filenames[0]);
		final MapView master = reader.readMap(firstFile, Warning.INSTANCE);
		final ExplorationModel model = new ExplorationModel(master,
				firstFile);
		for (final String filename : filenames) {
			if (filename == null || filename.equals(filenames[0])) {
				continue;
			}
			final File file = new File(filename);
			final IMap map = reader.readMap(file, Warning.INSTANCE);
			if (!map.getDimensions().equals(master.getDimensions())) {
				throw new IllegalArgumentException("Size mismatch between "
						+ firstFile + " and " + filename);
			}
			model.addSubordinateMap(map, file);
		}
		return model;
	}

	/**
	 * Let the user enter stats for workers already in the maps.
	 *
	 * @param model the driver model.
	 * @throws IOException on I/O error interacting with user
	 */
	private void enterStats(final IExplorationModel model) throws IOException {
		final List<Player> players = model.getPlayerChoices();
		while (true) {
			final int playerNum = helper.chooseFromList(players,
					"Which player owns the worker in question?",
					"There are no players shared by all the maps",
					"Player selection: ", true);
			if (playerNum < 0 || playerNum >= players.size()) {
				break;
			} else {
				enterStats(model, NullCleaner.assertNotNull(players.get(playerNum)));
			}
		}
	}

	/**
	 * Let the user enter stats for workers already in the maps that belong to
	 * one particular player.
	 *
	 * @param model the driver model
	 * @param player the player owning the worker
	 * @throws IOException on I/O error interacting with user
	 */
	private void enterStats(final IExplorationModel model, final Player player)
			throws IOException {
		final List<IUnit> units = removeStattedUnits(model.getUnits(player));
		while (true) {
			final int unitNum = helper
					.chooseFromList(
							units,
							"Which unit contains the worker in question?",
							"All that player's units are already fully statted",
							"Unit selection: ", false);
			if (unitNum < 0 || unitNum >= units.size() || units.isEmpty()) {
				break;
			} else {
				final IUnit unit = units.get(unitNum);
				if (unit == null) {
					continue;
				}
				enterStats(model, unit);
				if (!hasUnstattedWorker(model, unit.getID())) {
					units.remove(unit);
				}
			}
		}
	}

	/**
	 * @param model the exploration model
	 * @param idNum an ID number
	 * @return true if the number designates a unit containing an unstatted
	 *         worker, and false otherwise.
	 */
	private static boolean hasUnstattedWorker(final IExplorationModel model,
			final int idNum) {
		final IFixture fix = find(model.getMap(), idNum);
		return fix instanceof IUnit && hasUnstattedWorker((IUnit) fix);
	}

	/**
	 * @param unit a unit
	 * @return whether it contains any workers without stats
	 */
	private static boolean hasUnstattedWorker(final IUnit unit) {
		for (final UnitMember member : unit) {
			if (member instanceof Worker
					&& ((Worker) member).getStats() == null) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param units a list of units
	 * @return a list of the units in the list that have workers without stats
	 */
	private static List<IUnit> removeStattedUnits(final List<IUnit> units) {
		final List<IUnit> retval = new ArrayList<>();
		for (final IUnit unit : units) {
			if (unit != null && hasUnstattedWorker(unit)) {
				retval.add(unit);
			}
		}
		return retval;
	}

	/**
	 * Let the user enter stats for workers already in the maps that are part of
	 * one particular unit.
	 *
	 * @param model the driver model
	 * @param unit the unit containing the worker
	 * @throws IOException on I/O error interacting with user
	 */
	private void enterStats(final IExplorationModel model, final IUnit unit)
			throws IOException {
		final List<Worker> workers = new ArrayList<>();
		for (final UnitMember member : unit) {
			if (member instanceof Worker
					&& ((Worker) member).getStats() == null) {
				workers.add((Worker) member);
			}
		}
		while (true) {
			final int workerNum = helper.chooseFromList(workers,
					"Which worker do you want to enter stats for?",
					"There are no workers without stats in that unit",
					"Worker to modify: ", false);
			if (workerNum < 0 || workerNum >= workers.size()
					|| workers.isEmpty()) {
				break;
			} else {
				enterStats(model, workers.get(workerNum).getID());
				workers.remove(workerNum);
			}
		}
	}

	/**
	 * Let the user enter stats for a worker.
	 *
	 * @param model the driver model
	 * @param idNum the worker's ID.
	 * @throws IOException on I/O error interacting with user.
	 */
	private void enterStats(final IExplorationModel model, final int idNum)
			throws IOException {
		final WorkerStats stats = enterStats();
		for (final Pair<IMap, File> pair : model.getAllMaps()) {
			final IMap map = pair.first();
			final IFixture fix = find(map, idNum);
			if (fix instanceof Worker && ((Worker) fix).getStats() == null) {
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
		final int maxHP = helper.inputNumber("Max HP: ");
		final int str = helper.inputNumber("Str: ");
		final int dex = helper.inputNumber("Dex: ");
		final int con = helper.inputNumber("Con: ");
		final int intel = helper.inputNumber("Int: ");
		final int wis = helper.inputNumber("Wis: ");
		final int cha = helper.inputNumber("Cha: ");
		return new WorkerStats(maxHP, maxHP, str, dex, con, intel, wis, cha);
	}

	/**
	 * @param map a map
	 * @param idNum an ID number
	 * @return the fixture with that ID, or null if not found
	 */
	@Nullable
	private static IFixture find(final IMap map, final int idNum) {
		final ITileCollection tiles = map.getTiles();
		for (final Point point : tiles) {
			if (point == null) {
				continue;
			}
			final ITile tile = tiles.getTile(point);
			final IFixture result = find(tile, idNum);
			if (result != null) {
				return result; // NOPMD
			}
		}
		return null;
	}

	/**
	 * @param iter something containing fixtures
	 * @param idNum an ID number
	 * @return the fixture with that ID, or null if not found
	 */
	@Nullable
	private static IFixture find(final FixtureIterable<?> iter, final int idNum) {
		for (final IFixture fix : iter) {
			if (fix.getID() == idNum) {
				return fix; // NOPMD
			} else if (fix instanceof FixtureIterable<?>) {
				final IFixture result = find((FixtureIterable<?>) fix, idNum);
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
	 * @param idf the ID factory
	 * @throws IOException on I/O error interacting with user
	 */
	private void createWorkers(final IExplorationModel model,
			final IDFactory idf) throws IOException {
		final List<Player> players = model.getPlayerChoices();
		while (true) {
			final int playerNum = helper.chooseFromList(players,
					"Which player owns the new worker(s)?",
					"There are no players shared by all the maps",
					"Player selection: ", true);
			if (playerNum < 0 || playerNum >= players.size()) {
				break;
			} else {
				createWorkers(model, idf,
						NullCleaner.assertNotNull(players.get(playerNum)));
			}
		}
	}

	/**
	 * Allow the user to create randomly-generated workers belonging to a
	 * particular player.
	 *
	 * @param model the driver model
	 * @param idf the ID factory
	 * @param player the player to own the workers
	 * @throws IOException on I/O error interacting with user
	 */
	private void createWorkers(final IExplorationModel model,
			final IDFactory idf, final Player player) throws IOException {
		boolean again = true;
		while (again) {
			if (helper.inputBoolean("Add worker(s) to an existing unit? ")) {
				final List<IUnit> units = model.getUnits(player);
				final int unitNum = helper.chooseFromList(units,
						"Which unit contains the worker in question?",
						"There are no units owned by that player",
						"Unit selection: ", false);
				if (unitNum >= 0 && unitNum < units.size()) {
					final IUnit unit = units.get(unitNum);
					if (unit != null) {
						createWorkers(model, idf, unit);
					}
				}
			} else {
				final Point point = PointFactory.point(
						helper.inputNumber("Row to put new unit: "),
						helper.inputNumber("Column to put new unit: "));
				final IUnit unit = new Unit(player, // NOPMD
						helper.inputString("Kind of unit: "),
						helper.inputString("Unit name: "), idf.createID());
				for (final Pair<IMap, File> pair : model.getAllMaps()) {
					final ITileCollection tiles = pair.first().getTiles();
					final ITile tile = tiles.getTile(point);
					if (!(tile instanceof IMutableTile)) {
						SYS_OUT.print("Couldn't add to ");
						SYS_OUT.print(pair.second().getPath());
						SYS_OUT.println(" because tile wasn't mutable");
					} else if (!(tiles instanceof IMutableTileCollection)) {
						SYS_OUT.print("Couldn't add to ");
						SYS_OUT.print(pair.second().getPath());
						SYS_OUT.println(" because the map's tiles aren't mutable");
					} else {
						final IMutableTile mtile = (IMutableTile) tile;
						mtile.addFixture(unit);
						((IMutableTileCollection) tiles).addTile(point,
								(IMutableTile) tile);
					}
				}
				if (helper.inputBoolean("Load names from file and use randomly generated stats?")) {
					createWorkersFromFile(model, idf, unit);
				} else {
					createWorkers(model, idf, unit);
				}
			}
			again = helper
					.inputBoolean("Add more workers to another unit? ");
		}
	}

	/**
	 * Let the user create randomly-generated workers in a unit.
	 *
	 * @param model the driver model
	 * @param idf the ID factory.
	 * @param unit the unit to contain them.
	 * @throws IOException on I/O error interacting with the user
	 */
	private void createWorkers(final IExplorationModel model,
			final IDFactory idf, final IUnit unit) throws IOException {
		final int count = helper.inputNumber("How many workers to generate? ");
		for (int i = 0; i < count; i++) {
			final Worker worker = createWorker(idf);
			for (final Pair<IMap, File> pair : model.getAllMaps()) {
				final IFixture fix = find(pair.first(), unit.getID());
				if (fix instanceof IUnit) {
					((IUnit) fix).addMember(worker);
				}
			}
		}
	}

	/**
	 * Let the user create randomly-generated workers, with names read from
	 * file, in a unit.
	 *
	 * @param model
	 *            the driver model
	 * @param idf
	 *            the ID factory.
	 * @param unit
	 *            the unit to contain them.
	 * @throws IOException
	 *             on I/O error interacting with the user
	 */
	private void createWorkersFromFile(final IExplorationModel model,
			final IDFactory idf, final IUnit unit) throws IOException {
		final int count = helper.inputNumber("How many workers to generate? ");
		final String filename = helper.inputString("Filename to load names from: ");
		final List<String> names =
				Files.readAllLines(FileSystems.getDefault().getPath(filename),
						Charset.defaultCharset());
		for (int i = 0; i < count; i++) {
			final Worker worker = createWorker(names.get(i).trim(), idf);
			for (final Pair<IMap, File> pair : model.getAllMaps()) {
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
	 * Each non-human race has a 1 in 20 chance of coming up; stats are all 3d6;
	 * there are five 1-in-20 chances of starting with a level in some Job, and
	 * the user will be prompted for what Job for each.
	 *
	 * TODO: racial adjustments to stats.
	 *
	 * @param idf the ID factory
	 * @throws IOException on I/O error interacting with the user
	 * @return the generated worker
	 */
	private Worker createWorker(final IDFactory idf) throws IOException {
		final String race = RaceFactory.getRace();
		final String name = helper.inputString("Worker is a " + race
				+ ". Worker name: ");
		final Worker retval = new Worker(name, race, idf.createID());
		int levels = 0;
		for (int i = 0; i < 3; i++) {
			// ESCA-JAVA0076:
			if (SingletonRandom.RANDOM.nextInt(20) == 0) {
				retval.addJob(new Job(// NOPMD
						helper.inputString("Which Job does worker have a level in? "),
						1));
				levels++;
			}
		}
		final boolean pregenStats = helper
				.inputBoolean("Enter pregenerated stats? ");
		if (pregenStats) {
			retval.setStats(enterStats());
		} else {
			final int constitution = threeDeeSix();
			final int conBonus = (constitution - STAT_BASIS) / 2;
			final int hitp = 8 + conBonus + rollDeeEight(levels, conBonus);
			retval.setStats(new WorkerStats(hitp, hitp, threeDeeSix(), threeDeeSix(),
					constitution, threeDeeSix(), threeDeeSix(), threeDeeSix()));
		}
		return retval;
	}

	/**
	 * Create a randomly-generated worker using a name from file, asking the
	 * user for Jobs and such but randomly generating stats.
	 *
	 * Each non-human race has a 1 in 20 chance of coming up; stats are all 3d6;
	 * there are five 1-in-20 chances of starting with a level in some Job, and
	 * the user will be prompted for what Job for each.
	 *
	 * TODO: racial adjustments to stats.
	 *
	 * @param idf
	 *            the ID factory
	 * @throws IOException
	 *             on I/O error interacting with the user
	 * @return the generated worker
	 */
	private Worker createWorker(final String name, final IDFactory idf)
			throws IOException {
		final String race = RaceFactory.getRace();
		System.out.print("Worker ");
		System.out.print(name);
		System.out.print(" is a ");
		System.out.println(race);
		final Worker retval = new Worker(name, race, idf.createID());
		int levels = 0;
		for (int i = 0; i < 3; i++) {
			// ESCA-JAVA0076:
			if (SingletonRandom.RANDOM.nextInt(20) == 0) {
				retval.addJob(new Job(// NOPMD
						helper.inputString("Which Job does worker have a level in? "),
						1));
				levels++;
			}
		}
		final int constitution = threeDeeSix();
		final int conBonus = (constitution - STAT_BASIS) / 2;
		final int hitp = 8 + conBonus + rollDeeEight(levels, conBonus);
		retval.setStats(new WorkerStats(hitp, hitp, threeDeeSix(),
				threeDeeSix(), constitution, threeDeeSix(), threeDeeSix(),
				threeDeeSix()));
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
	@Override
	public String toString() {
		return "StatGeneratingCLIDriver";
	}
}

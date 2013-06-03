package controller.map.drivers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel;
import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMap;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.misc.IMultiMapModel;
import util.Pair;
import util.SingletonRandom;
import util.Warning;
import controller.map.drivers.ISPDriver.DriverFailedException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.CLIHelper;
import controller.map.misc.DriverUsage;
import controller.map.misc.DriverUsage.ParamCount;
import controller.map.misc.IDFactory;
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
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-t",
			"--stats", ParamCount.Many, "Enter worker stats or generate new workers.",
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
	 * @param args command-line arguments
	 * @throws DriverFailedException if the driver failed
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		// ESCA-JAVA0177:
		final IExplorationModel model; // NOPMD
		try {
			model = readMaps(args);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error reading maps", except);
		} catch (XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in map file", except);
		} catch (SPFormatException except) {
			throw new DriverFailedException("SP format error in map file", except);
		}
		try {
			if (helper
					.inputBoolean("Enter pregenerated stats for existing workers? ")) {
				enterStats(model);
			} else {
				createWorkers(model, getIDFactory(model));
			}
		} catch (IOException except) {
			throw new DriverFailedException("I/O error interacting with user", except);
		}
		try {
			writeMaps(model);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error writing to a map file", except);
		}
	}
	/**
	 * Write maps to disk.
	 * @param model the model containing all the maps
	 * @throws IOException on I/O error
	 */
	private static void writeMaps(final IExplorationModel model) throws IOException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		for (Pair<IMap, String> pair : model.getAllMaps()) {
			reader.write(pair.second(), pair.first());
		}
	}
	/**
	 * @param model a driver model
	 * @return an IDFactory instance with all the IDs used in any of its maps marked as used.
	 */
	private static IDFactory getIDFactory(final IMultiMapModel model) {
		final IDFactory retval = new IDFactory();
		for (final Pair<IMap, String> pair : model.getAllMaps()) {
			final TileCollection tiles = pair.first().getTiles();
			for (final Point point : tiles) {
				recursiveRegister(retval, tiles.getTile(point));
			}
		}
		return retval;
	}
	/**
	 * @param idf an IDFactory instance
	 * @param iter a collection of fixtures, all of which (recursively) should have their IDs marked as used.
	 */
	private static void recursiveRegister(final IDFactory idf, final FixtureIterable<?> iter) {
		for (final IFixture fix : iter) {
			idf.register(fix.getID());
			if (fix instanceof FixtureIterable<?>) {
				recursiveRegister(idf, (FixtureIterable<?>) fix);
			}
		}
	}
	/**
	 * Read maps.
	 * @param filenames the files to read from
	 * @return an exploration-model containing all of them
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on basic file I/O error
	 */
	private static ExplorationModel readMaps(final String[] filenames)
			throws IOException, XMLStreamException, SPFormatException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final MapView master = reader.readMap(filenames[0], Warning.INSTANCE);
		final ExplorationModel model = new ExplorationModel(master, filenames[0]);
		for (final String filename : filenames) {
			if (filename.equals(filenames[0])) {
				continue;
			}
			final IMap map = reader.readMap(filename, Warning.INSTANCE);
			if (!map.getDimensions().equals(master.getDimensions())) {
				throw new IllegalArgumentException("Size mismatch between " + filenames[0] + " and " + filename);
			}
			model.addSubordinateMap(map, filename);
		}
		return model;
	}
	/**
	 * Let the user enter stats for workers already in the maps.
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
				enterStats(model, players.get(playerNum));
			}
		}
	}
	/**
	 * Let the user enter stats for workers already in the maps that belong to one particular player.
	 * @param model the driver model
	 * @param player the player owning the worker
	 * @throws IOException on I/O error interacting with user
	 */
	private void enterStats(final IExplorationModel model, final Player player) throws IOException {
		final List<Unit> units = model.getUnits(player);
		while (true) {
			final int unitNum = helper.chooseFromList(units,
					"Which unit contains the worker in question?",
					"There are no units owned by that player",
					"Unit selection: ", false);
			if (unitNum < 0 || unitNum >= units.size() || units.isEmpty()) {
				break;
			} else {
				enterStats(model, units.get(unitNum));
			}
		}
	}
	/**
	 * Let the user enter stats for workers already in the maps that are part of one particular unit.
	 * @param model the driver model
	 * @param unit the unit containing the worker
	 * @throws IOException on I/O error interacting with user
	 */
	private void enterStats(final IExplorationModel model, final Unit unit) throws IOException {
		final List<Worker> workers = new ArrayList<Worker>();
		for (final UnitMember member : unit) {
			if (member instanceof Worker && ((Worker) member).getStats() == null) {
				workers.add((Worker) member);
			}
		}
		while (true) {
			final int workerNum = helper.chooseFromList(workers,
					"Which worker do you want to enter stats for?",
					"There are no workers without stats in that unit",
					"Worker to modify: ", false);
			if (workerNum < 0 || workerNum >= workers.size() || workers.isEmpty()) {
				break;
			} else {
				enterStats(model, workers.get(workerNum).getID());
				workers.remove(workerNum);
			}
		}
	}
	/**
	 * Let the user enter stats for a worker.
	 * @param model the driver model
	 * @param idNum the worker's ID.
	 * @throws IOException on I/O error interacting with user.
	 */
	private void enterStats(final IExplorationModel model, final int idNum) throws IOException {
		final int maxHP = helper.inputNumber("Max HP: ");
		final int str = helper.inputNumber("Str: ");
		final int dex = helper.inputNumber("Dex: ");
		final int con = helper.inputNumber("Con: ");
		final int intel = helper.inputNumber("Int: ");
		final int wis = helper.inputNumber("Wis: ");
		final int cha = helper.inputNumber("Cha: ");
		final WorkerStats stats = new WorkerStats(maxHP, maxHP, str, dex, con, intel, wis, cha);
		for (final Pair<IMap, String> pair : model.getAllMaps()) {
			final IMap map = pair.first();
			final IFixture fix = find(map, idNum);
			if (fix instanceof Worker && ((Worker) fix).getStats() == null) {
				((Worker) fix).setStats(stats);
			}
		}
	}
	/**
	 * @param map a map
	 * @param idNum an ID number
	 * @return the fixture with that ID, or null if not found
	 */
	private static IFixture find(final IMap map, final int idNum) {
		final TileCollection tiles = map.getTiles();
		for (final Point point : tiles) {
			final Tile tile = tiles.getTile(point);
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
	 * @param model the driver model
	 * @param idf the ID factory
	 * @throws IOException on I/O error interacting with user
	 */
	private void createWorkers(final IExplorationModel model, final IDFactory idf) throws IOException {
		final List<Player> players = model.getPlayerChoices();
		while (true) {
			final int playerNum = helper.chooseFromList(players,
					"Which player owns the new worker(s)?",
					"There are no players shared by all the maps",
					"Player selection: ", true);
			if (playerNum < 0 || playerNum >= players.size()) {
				break;
			} else {
				createWorkers(model, idf, players.get(playerNum));
			}
		}
	}
	/**
	 * Allow the user to create randomly-generated workers belonging to a particular player.
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
				final List<Unit> units = model.getUnits(player);
				final int unitNum = helper.chooseFromList(units,
						"Which unit contains the worker in question?",
						"There are no units owned by that player",
						"Unit selection: ", false);
				if (unitNum >= 0 && unitNum < units.size()) {
					createWorkers(model, idf, units.get(unitNum));
				}
			} else {
				final Point point = PointFactory.point(
						helper.inputNumber("Row to put new unit: "),
						helper.inputNumber("Column to put new unit: "));
				final Unit unit = new Unit(player,
						helper.inputString("Kind of unit: "),
						helper.inputString("Unit name: "),
						idf.createID());
				for (final Pair<IMap, String> pair : model.getAllMaps()) {
					final IMap map = pair.first();
					final Tile tile = map.getTile(point);
					final boolean empty = tile.isEmpty();
					tile.addFixture(unit);
					if (empty) {
						map.getTiles().addTile(point, tile);
					}
				}
				createWorkers(model, idf, unit);
			}
		}
	}
	/**
	 * Let the user create randomly-generated workers in a unit.
	 * @param model the driver model
	 * @param idf the ID factory.
	 * @param unit the unit to contain them.
	 * @throws IOException on I/O error interacting with the user
	 */
	private void createWorkers(final IExplorationModel model,
			final IDFactory idf, final Unit unit) throws IOException {
		final int count = helper.inputNumber("How many workers to generate? ");
		for (int i = 0; i < count; i++) {
			final Worker worker = createWorker(idf);
			for (final Pair<IMap, String> pair : model.getAllMaps()) {
				final IFixture fix = find(pair.first(), unit.getID());
				if (fix instanceof Unit) {
					((Unit) fix).addMember(worker);
				}
			}
		}
	}
	/**
	 * A list of races.
	 */
	private static final List<String> RACES = new ArrayList<String>();
	static {
		RACES.add("dwarf");
		RACES.add("elf");
		RACES.add("gnome");
		RACES.add("half-elf");
		while (RACES.size() < 20) {
			RACES.add("human");
		}
	}
	/**
	 * Let the user create a randomly-generated worker.
	 *
	 * Each non-human race has a 1 in 20 chance of coming up; stats are all 3d6
	 * (TODO: racial adjustments); there are five 1-in-20 chances of starting
	 * with a level in some Job, and the user will be prompted for what Job for
	 * each.
	 *
	 * TODO: extra HP for Job levels.
	 *
	 * @param idf the ID factory
	 * @throws IOException on I/O error interacting with the user
	 * @return the generated worker
	 */
	private Worker createWorker(final IDFactory idf) throws IOException {
		final WorkerStats stats = new WorkerStats(8, 8, threeDeeSix(),
				threeDeeSix(), threeDeeSix(), threeDeeSix(), threeDeeSix(),
				threeDeeSix());
		final Worker retval = new Worker(helper.inputString("Worker name: "),
				RACES.get(SingletonRandom.RANDOM.nextInt(RACES.size())),
				idf.createID());
		retval.setStats(stats);
		for (int i = 0; i < 3; i++) {
			if (SingletonRandom.RANDOM.nextInt(20) == 0) {
				retval.addJob(new Job(helper.inputString("Worker has a level in a Job, which Job? "), 1));
			}
		}
		return retval;
	}
	/**
	 * @return the result of simulating a 3d6 roll.
	 */
	private static int threeDeeSix() {
		final Random random = SingletonRandom.RANDOM;
		return random.nextInt(6) + random.nextInt(6) + random.nextInt(6) + 3;
	}
}

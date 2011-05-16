package controller.map;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.exploration.LegacyTable;
import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.events.AbstractEvent;
import model.viewer.events.AbstractEvent.TownSize;
import model.viewer.events.AbstractEvent.TownStatus;
import model.viewer.events.BattlefieldEvent;
import model.viewer.events.CaveEvent;
import model.viewer.events.CityEvent;
import model.viewer.events.FortificationEvent;
import model.viewer.events.MineralEvent;
import model.viewer.events.MineralEvent.MineralKind;
import model.viewer.events.NothingEvent;
import model.viewer.events.StoneEvent;
import model.viewer.events.StoneEvent.StoneKind;
import model.viewer.events.TownEvent;
import controller.exploration.TableLoader;

/**
 * A class to convert old-style numeric events to Event objects without putting
 * the table in the source code.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class EventConverter {
	/**
	 * The table we read from.
	 */
	private final LegacyTable table;
	/**
	 * DC for possible but difficult events.
	 */
	private static final int POSSIBLE = 15;
	/**
	 * DC for essentially-impossible-now events.
	 */
	private static final int UNLIKELY = 40;

	/**
	 * Constructor.
	 * 
	 * @throws IOException
	 *             on I/O error loading the table
	 * @throws FileNotFoundException
	 *             if the table doesn't exist
	 */
	public EventConverter() throws FileNotFoundException, IOException {
		table = (LegacyTable) (new TableLoader().loadTable("tables/legacy"));
	}
	/**
	 * Constructor.
	 * @param ltable the table to use, instead of constructing our own.
	 */
	public EventConverter(final LegacyTable ltable) {
		table = ltable;
	}
	/**
	 * @param tile
	 *            a tile with a numeric event
	 * @return an equivalent event object for it
	 */
	public AbstractEvent convertEvent(final Tile tile) {
		final String result = table.generateEvent(tile);
//		System.out.print(tile.getEvent());
//		System.out.print(":\t");
//		System.out.println(result);
		if (result.contains("Nothing interesting")) {
			return new NothingEvent(); // NOPMD
		} else if (result.contains("vein")) {
//			System.out.print('m');
			return createMinerals(result, tile); // NOPMD
		} else if (result.contains("town")) {
//			System.out.print('t');
			return createTown(result, tile); // NOPMD
		} else if (result.contains("fortification")) {
//			System.out.print('f');
			return createFortification(result, tile); // NOPMD
		} else if (result.contains("long-ago battle")) {
//			System.out.print('b');
			return new BattlefieldEvent(POSSIBLE + getTerrainModifier(tile)); // NOPMD
		} else if (result.contains("city")) {
//			System.out.print('c');
			return createCity(result, tile); // NOPMD
		} else if (result.contains("deposit")) {
//			System.out.print('d');
			return createStone(result, tile); // NOPMD
		} else if (result.contains("caves")) {
//			System.out.print('v');
			return new CaveEvent(UNLIKELY + getTerrainModifier(tile));
		} else {
			throw new IllegalArgumentException("Unknown event type");
		}
	}
	
	/**
	 * Handle the StoneEvent case.
	 * @param result the string from the table
	 * @param tile the tile it's on.
	 * @return a suitable event object.
	 */
	private static StoneEvent createStone(final String result, final Tile tile) {
		// ESCA-JAVA0177:
		final StoneKind kind; // NOPMD
		if (result.contains("limestone")) {
			kind = StoneKind.Limestone;
		} else if (result.contains("marble")) {
			kind = StoneKind.Marble;
		} else {
			throw new IllegalArgumentException("Unknown kind of stone");
		}
		return new StoneEvent(kind, POSSIBLE + getTerrainModifier(tile));
	}

	/**
	 * Handle the CityEvent case.
	 * @param result the string from the table
	 * @param tile the tile it's on.
	 * @return a suitable event object.
	 */
	private static CityEvent createCity(final String result, final Tile tile) {
		final TownStatus status = getStatus(result);
		final TownSize size = getSize(result);
		final int dc = POSSIBLE + getSizeModifier(size) // NOPMD
				+ getStatusModifier(status) + getTerrainModifier(tile);
		return new CityEvent(status, size, dc);
	}

	/**
	 * Handle the FortificationEvent case.
	 * @param result the string from the table
	 * @param tile the tile it's on.
	 * @return a suitable event object.
	 */
	private static FortificationEvent createFortification(final String result, final Tile tile) {
		final TownStatus status = getStatus(result);
		final TownSize size = getSize(result);
		final int dc = POSSIBLE + getSizeModifier(size) // NOPMD
				+ getStatusModifier(status) + getTerrainModifier(tile);
		return new FortificationEvent(status, size, dc);
	}

	/**
	 * Handle the TownEvent case.
	 * 
	 * @param result
	 *            the string from the table
	 * @param tile
	 *            the tile it's on
	 * @return a suitable event object
	 */
	private static TownEvent createTown(final String result, final Tile tile) {
		final TownStatus status = getStatus(result);
		final TownSize size = getSize(result);
		final int dc = POSSIBLE + getSizeModifier(size) // NOPMD
				+ getStatusModifier(status) + getTerrainModifier(tile);
		return new TownEvent(status, size, dc);
	}

	/**
	 * @param result
	 *            the string from the table
	 * @return the town or whatever's size based on that string
	 */
	private static TownSize getSize(final String result) {
		if (result.contains("small")) {
			return TownSize.Small; // NOPMD
		} else if (result.contains("large")) {
			return TownSize.Large; // NOPMD
		} else {
			return TownSize.Medium;
		}
	}

	/**
	 * @param result
	 *            the string from the table
	 * @return the town or whatever status based on that string
	 */
	private static TownStatus getStatus(final String result) {
		if (result.contains("abandoned")) {
			return TownStatus.Abandoned; // NOPMD
		} else if (result.contains("burned")) {
			return TownStatus.Burned; // NOPMD
		} else if (result.contains("ruined")) {
			return TownStatus.Ruined; // NOPMD
		} else {
			return TownStatus.Active;
		}
	}

	/**
	 * @param status
	 *            the status of a town or whatever
	 * @return a suitable modifier based on the status
	 */
	private static int getStatusModifier(final TownStatus status) {
		switch (status) {
		case Abandoned:
			return 5; // NOPMD
		case Active:
			return 0; // NOPMD
		case Burned:
			return 5; // NOPMD
		case Ruined:
			// ESCA-JAVA0076:
			return 10;
		default:
			throw new IllegalArgumentException("Unknown status");
		}
	}

	/**
	 * @param size
	 *            the size of the town or whatever
	 * @return a suitable modifier based on the size.
	 */
	private static int getSizeModifier(final TownSize size) {
		switch (size) {
		case Large:
			return -5; // NOPMD
		case Medium:
			return 0; // NOPMD
		case Small:
			return 5;
		default:
			throw new IllegalArgumentException("Unknown size");
		}
	}

	/**
	 * Handle the MineralEvent case.
	 * 
	 * @param result
	 *            the string from the table
	 * @param tile
	 *            the tile it's on
	 * @return a suitable event object
	 */
	private static MineralEvent createMinerals(final String result,
			final Tile tile) {
		final boolean exposed = result.contains("exposed vein");
		// ESCA-JAVA0177:
		MineralKind kind;
		if (result.contains("iron")) {
			kind = MineralKind.Iron;
		} else if (result.contains("copper")) {
			kind = MineralKind.Copper;
		} else if (result.contains("gold")) {
			kind = MineralKind.Gold;
		} else if (result.contains("silver")) {
			kind = MineralKind.Silver;
		} else if (result.contains("coal")) {
			kind = MineralKind.Coal;
		} else {
			throw new IllegalArgumentException(
					"Doesn't contain any known mineral");
		}
		int dc = (exposed ? POSSIBLE : UNLIKELY) + getTerrainModifier(tile); // NOPMD
		return new MineralEvent(kind, exposed, dc);
	}

	/**
	 * @param tile
	 *            a tile
	 * @return a DC modifier based on its terrain type.
	 */
	// ESCA-JAVA0076:
	private static int getTerrainModifier(final Tile tile) { // NOPMD
		switch (tile.getType()) {
		case BorealForest:
			return 10; // NOPMD
		case Desert:
			return 15; // NOPMD
		case Jungle:
			return 20; // NOPMD
		case Mountain:
			return 15; // NOPMD
		case NotVisible:
			return 35; // NOPMD
		case Ocean:
			return 25; // NOPMD
		case Plains:
			return 5; // NOPMD
		case TemperateForest:
			return 10; // NOPMD
		case Tundra:
			return 15; // NOPMD
		default:
			throw new IllegalArgumentException(
					"Got a non-existent terrain type!");
		}
	}
	/**
	 * Driver.
	 * @param args the files to read from and write to
	 */
	public static void main(final String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: java controller.map.EventConverter in-file out-file");
			System.exit(1);
			return;
		}
		SPMap map;
		try {
			map = new MapReader().readMap(args[0]);
		} catch (XMLStreamException e) {
			System.err.println("XML stream error");
			System.exit(2);
			return;
		} catch (IOException e) {
			System.err.println("I/O error while reading");
			System.exit(3);
			return;
		}
		EventConverter converter;
		try {
			converter = new EventConverter();
		} catch (FileNotFoundException e) {
			System.err.println("Table file not found");
			System.exit(4);
			return;
		} catch (IOException e) {
			System.err.println("I/O error reading table");
			System.exit(5);
			return;
		}
		for (int i = 0; i < map.rows(); i++) {
			for (int j = 0; j < map.cols(); j++) {
				final Tile tile = map.getTile(i, j);
//				System.out.print(tile.getEvent());
//				System.out.print('\t');
				if (tile.getEvent() != -1) {
					tile.addFixture(converter.convertEvent(tile));
					tile.setEvent(-1);
				}
			}
//			System.out.println();
		}
		try {
			new XMLWriter(args[1]).write(map);
		} catch (IOException e) {
			System.err.println("I/O error writing new map");
			System.exit(6);
			return;
		}
	}
}

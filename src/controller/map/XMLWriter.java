package controller.map;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.EnumMap;
import java.util.Map;

import model.map.Fortress;
import model.map.Player;
import model.map.River;
import model.map.SPMap;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.Unit;
import model.map.events.AbstractEvent;
import model.map.events.CityEvent;
import model.map.events.EventKind;
import model.map.events.FortificationEvent;
import model.map.events.MineralEvent;
import model.map.events.StoneEvent;
import model.map.events.TownEvent;

/**
 * A class to write a map to file.
 * 
 * @author JOnathan Lovelace
 * 
 */
public class XMLWriter { // NOPMD
	/**
	 * The writer we'll write to.
	 */
	private final PrintWriter writer;

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            the file to write to
	 * 
	 * @throws IOException
	 *             on I/O error opening the file
	 */
	public XMLWriter(final String filename) throws IOException {
		this(new FileWriter(filename)); // $codepro.audit.disable
										// closeWhereCreated
	}

	/**
	 * Constructor. FIXME: The writer (or filename) should be a parameter to
	 * write(), not an instance variable.
	 * 
	 * @param out
	 *            the writer to write to
	 */
	public XMLWriter(final Writer out) {
		writer = new PrintWriter(new BufferedWriter(out)); // $codepro.audit.disable
															// closeWhereCreated
	}

	/**
	 * Write a map.
	 * 
	 * @param map
	 *            the map to write
	 */
	public void write(final SPMap map) {
		try {
			writer.print("<?xml version=");
			printQuoted("1.0");
			writer.println("?>");
			writer.print("<map version=");
			printQuoted(Integer.toString(SPMap.MAX_VERSION));
			writer.print(" rows=");
			printQuoted(map.rows());
			writer.print(" columns=");
			printQuoted(map.cols());
			final Player currentPlayer = map.getPlayers().getCurrentPlayer();
			if (!"".equals(currentPlayer.getName())) {
				writer.print(" current_player=");
				printQuoted(currentPlayer.getId());
			}
			writer.println('>');
			for (final Player player : map.getPlayers()) {
				indent(1);
				writer.print("<player number=");
				printQuoted(player.getId());
				writer.print(" code_name=");
				printQuoted(player.getName());
				writer.println(" />");
			}
			final int rows = map.rows();
			for (int i = 0; i < rows; i++) {
				printRow(map, i);
			}
			writer.println("</map>");
		} finally {
			writer.close();
		}
	}

	/**
	 * @param map
	 *            the map we're printing
	 * @param row
	 *            the row we're on
	 */
	private void printRow(final SPMap map, final int row) {
		boolean anyTiles = false;
		final int cols = map.cols();
		for (int j = 0; j < cols; j++) {
			final Tile tile = map.getTile(row, j);
			if (!anyTiles && !TileType.NotVisible.equals(tile.getType())) {
				indent(1);
				anyTiles = true;
				writer.print("<row index=");
				printQuoted(row);
				writer.println('>');
			}
			printTile(map.getTile(row, j));
		}
		if (anyTiles) {
			indent(1);
			writer.println("</row>");
		}
	}

	/**
	 * Prints a string in quotation marks.
	 * 
	 * @param text
	 *            the string to print
	 */
	private void printQuoted(final String text) {
		writer.print('"');
		writer.print(text);
		writer.print('"');
	}

	/**
	 * Prints a number in quotation marks.
	 * 
	 * @param num
	 *            the number to print
	 */
	private void printQuoted(final int num) {
		writer.print('"');
		writer.print(num);
		writer.print('"');
	}

	/**
	 * Prints the specified number of tabs.
	 * 
	 * @param tabs
	 *            how many tabs to print.
	 */
	private void indent(final int tabs) {
		for (int i = 0; i < tabs; i++) {
			writer.print('\t');
		}
	}

	/**
	 * Print a tile if its tile type isn't "unexplored" or it has a fortress or
	 * unit on it.
	 * 
	 * @param tile
	 *            the tile to print.
	 */
	private void printTile(final Tile tile) { // NOPMD
		if ((tile.getType() != TileType.NotVisible
				|| !tile.getContents().isEmpty() || !"".equals(tile
				.getTileText()))) {
			indent(2);
			writer.print("<tile row=");
			printQuoted(tile.getRow());
			writer.print(" column=");
			printQuoted(tile.getCol());
			if (tile.getType() != TileType.NotVisible) {
				writer.print(" type=");
				printQuoted(XML_TYPES.get(tile.getType()));
			}
			writer.print('>');
			if (!tile.getContents().isEmpty() || !tile.getRivers().isEmpty()
					|| !"".equals(tile.getTileText())) {
				writer.println(tile.getTileText());
				for (final TileFixture fix : tile.getContents()) {
					printFixture(fix);
				}
				for (final River river : tile.getRivers()) {
					printRiver(river);
				}
				indent(2);
			}
			writer.println("</tile>");
		}
	}

	/**
	 * @param fix
	 *            the fixture to print.
	 */
	private void printFixture(final TileFixture fix) {
		if (fix instanceof Fortress) {
			printFort((Fortress) fix);
		} else if (fix instanceof Unit) {
			printUnit((Unit) fix);
		} else if (fix instanceof AbstractEvent) {
			printEvent((AbstractEvent) fix);
		}
	}

	/**
	 * Print an Event.
	 * 
	 * @param fix
	 *            the event to print
	 */
	private void printEvent(final AbstractEvent fix) {
		if (!EventKind.Nothing.equals(fix.kind())) {
			indent(3);
			writer.print('<');
			switch (fix.kind()) {
			case Battlefield:
				writer.print("battlefield");
				break;
			case Caves:
				writer.print("cave");
				break;
			case City:
				writer.print("city status=");
				printQuoted(((CityEvent) fix).status().toString());
				writer.print(" size=");
				printQuoted(((CityEvent) fix).size().toString());
				break;
			case Fortification:
				writer.print("fortification status=");
				printQuoted(((FortificationEvent) fix).status().toString());
				writer.print(" size=");
				printQuoted(((FortificationEvent) fix).size().toString());
				break;
			case Town:
				writer.print("town status=");
				printQuoted(((TownEvent) fix).status().toString());
				writer.print(" size=");
				printQuoted(((TownEvent) fix).size().toString());
				break;
			case Mineral:
				writer.print("mineral mineral=");
				printQuoted(((MineralEvent) fix).mineral().toString());
				writer.print(" exposed=");
				printQuoted(Boolean.toString(((MineralEvent) fix).isExposed()));
				break;
			case Stone:
				writer.print("stone stone=");
				printQuoted(((StoneEvent) fix).stone().toString());
				break;
			default:
				throw new IllegalArgumentException("Unknown event type");
			}
			writer.print(" dc=");
			printQuoted(fix.getDC());
			writer.println(" />");
		}
	}

	/**
	 * Print the tag for a river.
	 * 
	 * @param river
	 *            the river to print
	 */
	private void printRiver(final River river) {
		indent(3);
		if (River.Lake.equals(river)) {
			writer.println("<lake />");
		} else {
			writer.print("<river direction=\"");
			writer.print(XML_RIVERS.get(river));
			writer.println("\" />");
		}
	}

	/**
	 * Prints a fortress.
	 * 
	 * @param fort
	 *            the fortress to print
	 */
	private void printFort(final Fortress fort) {
		indent(3);
		writer.print("<fortress owner=");
		printQuoted(fort.getOwner().getId());
		if (!"".equals(fort.getName())) {
			writer.print(" name=");
			printQuoted(fort.getName());
		}
		writer.print('>');
		if (!fort.getUnits().isEmpty()) {
			writer.println();
			for (final Unit unit : fort.getUnits()) {
				indent(1);
				printUnit(unit);
			}
			indent(3);
		}
		writer.println("</fortress>");
	}

	/**
	 * Print a unit.
	 * 
	 * @param unit
	 *            the unit to print
	 */
	private void printUnit(final Unit unit) {
		indent(3);
		writer.print("<unit owner=");
		printQuoted(unit.getOwner().getId());
		if (!"".equals(unit.getType())) {
			writer.print(" type=");
			printQuoted(unit.getType());
		}
		if (!"".equals(unit.getName())) {
			writer.print(" name=");
			printQuoted(unit.getName());
		}
		writer.println(" />");
	}

	/**
	 * A mapping from tile types to descriptive strings as used in the XML.
	 */
	private static final Map<TileType, String> XML_TYPES = new EnumMap<TileType, String>(
			TileType.class);
	/**
	 * Mapping from river directions to the strings used for them in the XML.
	 */
	private static final Map<River, String> XML_RIVERS = new EnumMap<River, String>(
			River.class);
	static {
		initializeTileTypeNames();
		XML_RIVERS.put(River.North, "north");
		XML_RIVERS.put(River.South, "south");
		XML_RIVERS.put(River.East, "east");
		XML_RIVERS.put(River.West, "west");
		XML_RIVERS.put(River.Lake, "lake");
	}

	/**
	 * Method extracted to let us localize the warning-suppression. Initialize
	 * the XML_TYPES tile-type-to-string mapping.
	 */
	@SuppressWarnings("deprecation")
	private static void initializeTileTypeNames() {
		XML_TYPES.put(TileType.Tundra, "tundra");
		XML_TYPES.put(TileType.TemperateForest, "temperate_forest");
		XML_TYPES.put(TileType.BorealForest, "boreal_forest");
		XML_TYPES.put(TileType.Ocean, "ocean");
		XML_TYPES.put(TileType.Desert, "desert");
		XML_TYPES.put(TileType.Plains, "plains");
		XML_TYPES.put(TileType.Jungle, "jungle");
		XML_TYPES.put(TileType.Mountain, "mountain");
		XML_TYPES.put(TileType.Steppe, "steppe");
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "XMLWriter";
	}
}

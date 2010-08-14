package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.EnumMap;

import model.Fortress;
import model.Player;
import model.SPMap;
import model.Tile;
import model.TileType;
import model.Unit;

/**
 * A class to write a map to file.
 * 
 * @author JOnathan Lovelace
 * 
 */
public class XMLWriter { // NOPMD
	/**
	 * The writer we'll write to
	 */
	private final PrintWriter writer;

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            the file to write to
	 * @throws IOException
	 *             on I/O error opening the file
	 */
	public XMLWriter(final String filename) throws IOException {
		this(new FileWriter(filename));
	}

	/**
	 * Constructor
	 * 
	 * @param out
	 *            the writer to write to
	 */
	public XMLWriter(final Writer out) {
		writer = new PrintWriter(new BufferedWriter(out));
	}

	/**
	 * Write a map.
	 * 
	 * @param map
	 *            the map to write
	 */
	public void write(final SPMap map) {
		writer.print("<map version=");
		printQuoted("0");
		writer.print(" rows=");
		printQuoted(map.rows());
		writer.print(" columns=");
		printQuoted(map.cols());
		writer.println('>');
		for (Player player : map.getPlayers()) {
			indent(1);
			writer.print("<player number=");
			printQuoted(player.getId());
			writer.print(" code_name=");
			printQuoted(player.getName());
			writer.println(" />");
		}
		for (int i = 0; i < map.rows(); i++) {
			printRow(map, i);
		}
		writer.println("</map>");
		writer.close();
	}

	/**
	 * @param map
	 *            the map we're printing
	 * @param row
	 *            the row we're on
	 */
	private void printRow(final SPMap map, final int row) {
		indent(1);
		writer.print("<row index=");
		printQuoted(row);
		writer.println('>');
		for (int j = 0; j < map.cols(); j++) {
			printTile(map.getTile(row, j));
		}
		indent(1);
		writer.println("</row>");
	}

	/**
	 * Prints a string in quotation marks
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
	 * Prints a number in quotation marks
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
		if (tile.getType() != TileType.NotVisible || !tile.getForts().isEmpty()
				|| !tile.getUnits().isEmpty()) {
			indent(2);
			writer.print("<tile row=");
			printQuoted(tile.getRow());
			writer.print(" column=");
			printQuoted(tile.getCol());
			if (tile.getType() != TileType.NotVisible) {
				writer.print(" type=");
				printQuoted(XML_TYPES.get(tile.getType()));
			}
			if (tile.getEvent() >= 0) {
				writer.print(" event=");
				printQuoted(tile.getEvent());
			}
			writer.print('>');
			if (!tile.getForts().isEmpty() || !tile.getUnits().isEmpty()) {
				writer.println();
				for (Fortress fort : tile.getForts()) {
					printFort(fort);
				}
				for (Unit unit : tile.getUnits()) {
					printUnit(unit);
				}
				indent(2);
			}
			writer.println("</tile>");
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
		printQuoted(fort.getOwner());
		if (fort.getName() != null) {
			writer.print(" name=");
			printQuoted(fort.getName());
		}
		writer.print('>');
		if (!fort.getUnits().isEmpty()) {
			writer.println();
			for (Unit unit : fort.getUnits()) {
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
		printQuoted(unit.getOwner());
		if (unit.getType() != null) {
			writer.print(" type=");
			printQuoted(unit.getType());
		}
		if (unit.getName() != null) {
			writer.print(" name=");
			printQuoted(unit.getName());
		}
		writer.println(" />");
	}

	/**
	 * A mapping from tile types to descriptive strings as used in the XML.
	 */
	private static final EnumMap<TileType, String> XML_TYPES = new EnumMap<TileType, String>(
			TileType.class);
	static {
		XML_TYPES.put(TileType.Tundra, "tundra");
		XML_TYPES.put(TileType.TemperateForest, "temperate_forest");
		XML_TYPES.put(TileType.BorealForest, "boreal_forest");
		XML_TYPES.put(TileType.Ocean, "ocean");
		XML_TYPES.put(TileType.Desert, "desert");
		XML_TYPES.put(TileType.Plains, "plains");
		XML_TYPES.put(TileType.Jungle, "jungle");
		XML_TYPES.put(TileType.Mountain, "mountain");
	}
}

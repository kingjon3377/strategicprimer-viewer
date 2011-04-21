package model.viewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A map, consisting of tiles, units, and fortresses. Each fortress is on a
 * tile; each unit is either in a fortress or on a tile directly.
 * 
 * @author kingjon
 * 
 */
public class SPMap implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7490067634803753892L;

	/**
	 * Constructor.
	 */
	public SPMap() {
		tiles = new HashMap<Point, Tile>();
		forts = new ArrayList<Fortress>();
		units = new ArrayList<Unit>();
		players = new ArrayList<Player>();
	}

	/**
	 * Constructor that takes the size.
	 * 
	 * @param rows
	 *            the number of rows
	 * @param cols
	 *            the number of columns
	 */
	public SPMap(final int rows, final int cols) {
		this();
		myRows = rows;
		myCols = cols;
	}

	/**
	 * The number of rows on the map.
	 */
	private int myRows;
	/**
	 * The number of columns on the map.
	 */
	private int myCols;

	/**
	 * The tiles on the map.
	 */
	private HashMap<Point, Tile> tiles; // NOPMD
	/**
	 * The fortresses on the map.
	 */
	private final ArrayList<Fortress> forts; // NOPMD
	/**
	 * The units on the map.
	 */
	private final ArrayList<Unit> units; // NOPMD
	/**
	 * The players in the game.
	 */
	private final ArrayList<Player> players; // NOPMD

	/**
	 * @return how many rows the map has.
	 */
	public final int rows() {
		return myRows;
	}

	/**
	 * @return how many columns the map has
	 */
	public final int cols() {
		return myCols;
	}

	/**
	 * Adds a fortress to the map. FIXME: Should require its coordinates and add
	 * it to the relevant tile.
	 * 
	 * @param fort
	 *            the fortress to add.
	 */
	public final void addFortress(final Fortress fort) {
		forts.add(fort);
	}

	/**
	 * Adds a unit to the map. FIXME: Should require its coordinates and add it
	 * to the relevant tile, and maybe to the relevant fortress if any.
	 * 
	 * @param unit
	 *            the unit to add.
	 */
	public final void addUnit(final Unit unit) {
		units.add(unit);
	}

	/**
	 * Add a tile to the map.
	 * 
	 * @param tile
	 *            the tile to add
	 */
	public final void addTile(final Tile tile) {
		tiles.put(new Point(tile.getRow(), tile.getCol()), tile);
	}

	/**
	 * Add a player to the game.
	 * 
	 * @param player
	 *            the player to add
	 */
	public final void addPlayer(final Player player) {
		players.add(player);
	}

	/**
	 * A structure encapsulating two coordinates.
	 * 
	 * @author Jonathan Lovelace
	 * 
	 */
	public static class Point {
		/**
		 * The first coordinate.
		 */
		private final int myRow;
		/**
		 * The second coordinate.
		 */
		private final int myCol;

		/**
		 * @return the first coordinate.
		 */
		public final int row() {
			return myRow;
		}

		/**
		 * @return the second coordinate.
		 */
		public final int col() {
			return myCol;
		}

		/**
		 * Constructor.
		 * 
		 * @param _row
		 *            The first coordinate
		 * @param _col
		 *            The second coordinate
		 */
		public Point(final int _row, final int _col) {
			myRow = _row;
			myCol = _col;
		}

		/**
		 * @return whether this object equals another.
		 * @param obj
		 *            the other object
		 */
		@Override
		public final boolean equals(final Object obj) {
			return (obj instanceof Point && (((Point) obj).myRow == myRow && ((Point) obj).myCol == myCol));
		}

		/**
		 * @return a hash code.
		 */
		@Override
		public final int hashCode() {
			return myRow | myCol;
		}
	}

	/**
	 * @param row
	 *            the row
	 * @param col
	 *            the column
	 * @return the tile at those coordinates
	 */
	public final Tile getTile(final int row, final int col) {
		return tiles.get(new Point(row, col));
	}

	/**
	 * @return the players in the map
	 */
	public List<Player> getPlayers() {
		return new ArrayList<Player>(players);
	}

	/**
	 * @param obj
	 *            another object
	 * @return whether it is an identical map.
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof SPMap && myCols == ((SPMap) obj).cols()
				&& myRows == ((SPMap) obj).rows()
				&& sort(players).equals(sort(((SPMap) obj).getPlayers()))
				&& sort(forts).equals(sort(((SPMap) obj).forts))
				&& sort(units).equals(sort(((SPMap) obj).units))
				&& tiles.equals(((SPMap) obj).tiles);
	}

	/**
	 * @return a hash value for the map
	 */
	@Override
	public int hashCode() {
		return myRows + myCols << 2 + players.hashCode() << 4 + forts
				.hashCode() << 6 + units.hashCode() << 8 + tiles.hashCode() << 10;
	}

	/**
	 * @return a String representation of the map
	 */
	@Override
	public String toString() {
		final StringBuilder sbuild = new StringBuilder("SP Map with ");
		sbuild.append(myRows);
		sbuild.append(" rows and ");
		sbuild.append(myCols);
		sbuild.append(" columns. Players:");
		for (Player player : players) {
			sbuild.append("\n\t");
			sbuild.append(player);
		}
		sbuild.append("\nForts:");
		for (Fortress fort : forts) {
			sbuild.append("\n\t");
			sbuild.append(fort);
		}
		sbuild.append("\nUnits:");
		for (Unit unit : units) {
			sbuild.append("\n\t");
			sbuild.append(unit);
		}
		sbuild.append("\nTiles:");
		for (Point point : tiles.keySet()) {
			sbuild.append("\n\t(");
			sbuild.append(point.row());
			sbuild.append(", ");
			sbuild.append(point.col());
			sbuild.append("): ");
			sbuild.append(tiles.get(point));
		}
		return sbuild.toString();
	}

	private static <T extends Comparable<T>> List<T> sort(
			final List<? extends T> list) {
		final List<T> newList = new ArrayList<T>(list);
		Collections.sort(newList);
		return newList;
	}
}

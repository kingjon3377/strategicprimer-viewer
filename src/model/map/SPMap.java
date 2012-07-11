package model.map;

import view.util.SystemOut;

/**
 * A map, consisting of tiles, units, and fortresses. Each fortress is on a
 * tile; each unit is either in a fortress or on a tile directly.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SPMap implements IMap {
	/**
	 * Map max version.
	 */
	public static final int MAX_VERSION = 1;
	/**
	 * Map version.
	 */
	private final int version;
	/**
	 * @return the map version
	 */
	@Override
	public int getVersion() {
		return version;
	}
	
	/**
	 * Constructor that takes the size.
	 * 
	 * @param ver
	 *            the map version
	 * @param rows
	 *            the number of rows
	 * @param cols
	 *            the number of columns
	 * @param fileName the file this was loaded from
	 */
	public SPMap(final int ver, final int rows, final int cols, final String fileName) {
		tiles = new TileCollection(fileName);
		players = new PlayerCollection();
		myRows = rows;
		myCols = cols;
		version = ver;
		file = fileName;
	}

	/**
	 * The number of rows on the map.
	 */
	private final int myRows;
	/**
	 * The number of columns on the map.
	 */
	private final int myCols;

	/**
	 * The tiles on the map.
	 */
	private TileCollection tiles; // NOPMD
	/**
	 * The players in the game.
	 */
	private final PlayerCollection players; // NOPMD

	/**
	 * 
	 * @return how many rows the map has.
	 */
	@Override
	public final int rows() {
		return myRows;
	}

	/**
	 * 
	 * @return how many columns the map has
	 */
	@Override
	public final int cols() {
		return myCols;
	}

	/**
	 * Add a tile to the map.
	 * 
	 * @param tile
	 *            the tile to add
	 */
	public final void addTile(final Tile tile) {
		tiles.addTile(tile);
	}

	/**
	 * Add a player to the game.
	 * 
	 * @param player
	 *            the player to add
	 */
	@Override
	public final void addPlayer(final Player player) {
		players.addPlayer(player);
	}
	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	@Override
	public final Tile getTile(final Point point) {
		return tiles.getTile(point);
	}
	/**
	 * 
	 * @return the players in the map
	 */
	@Override
	public PlayerCollection getPlayers() {
		return players;
	}

	/**
	 * @param obj
	 *            another object
	 * 
	 * @return whether it is an identical map.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof SPMap && myCols == ((IMap) obj).cols()
						&& myRows == ((IMap) obj).rows()
						&& players.equals(((IMap) obj).getPlayers()) && tiles
							.equals(((SPMap) obj).tiles));
	}

	/**
	 * 
	 * @return a hash value for the map
	 */
	@Override
	public int hashCode() {
		return myRows + myCols << 2 + players.hashCode() << 4 + tiles
				.hashCode() << 10;
	}

	/**
	 * 
	 * @return a String representation of the map
	 */
	@Override
	public String toString() {
		final StringBuilder sbuild = new StringBuilder("SP Map with ");
		sbuild.append(myRows);
		sbuild.append(" rows and ");
		sbuild.append(myCols);
		sbuild.append(" columns. Players:");
		for (final Player player : players) {
			sbuild.append("\n\t");
			sbuild.append(player);
		}
		sbuild.append("\nTiles:");
		for (final Point point : tiles) {
			sbuild.append("\n\t(");
			sbuild.append(point.row());
			sbuild.append(", ");
			sbuild.append(point.col());
			sbuild.append("): ");
			sbuild.append(tiles.getTile(point));
		}
		return sbuild.toString();
	}
	/**
	 * Write the map to XML.
	 * @return an XML representation of the map.
	 */
	@Override
	@Deprecated
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<map version=\"");
		sbuild.append(getVersion());
		sbuild.append("\" rows=\"");
		sbuild.append(rows());
		sbuild.append("\" columns=\"");
		sbuild.append(cols());
		if (!players.getCurrentPlayer().getName().isEmpty()) {
			sbuild.append("\" current_player=\"");
			sbuild.append(players.getCurrentPlayer().getPlayerId());
		}
		sbuild.append("\">\n");
		for (Player player : players) {
			sbuild.append('\t');
			sbuild.append(player.toXML());
			sbuild.append('\n');
		}
		for (int i = 0; i < myRows; i++) {
			boolean anyTiles = false;
			for (int j = 0; j < myCols; j++) {
				final String tileXML = getTile(PointFactory.point(i, j)).toXML();
				if (!anyTiles && !tileXML.isEmpty()) {
					anyTiles = true;
					sbuild.append("\t<row index=\"");
					sbuild.append(i);
					sbuild.append("\">\n");
				}
				if (!tileXML.isEmpty()) {
					sbuild.append("\t\t");
					sbuild.append(tileXML);
					sbuild.append('\n');
				}
			}
			if (anyTiles) {
				sbuild.append("\t</row>\n");
			}
		}
		sbuild.append("</map>");
		return sbuild.toString();
	}
	/**
	 * @param obj another map
	 * @return whether it's a strict subset of this one
	 */
	@Override
	public boolean isSubset(final IMap obj) {
		if (cols() == obj.cols() && rows() == obj.rows()) {
			return players.isSubset(obj.getPlayers()) && tiles.isSubset(obj.getTiles()); // NOPMD
		} else {
			SystemOut.SYS_OUT.println("Sizes differ");
			return false;
		}
	}
	/**
	 * Compare to another map.
	 * @param other the other map
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final IMap other) {
		return equals(other) ? 0 : hashCode() - other.hashCode();
	}
	/**
	 * @return The name of the file this is to be written to.
	 */
	@Override
	public String getFile() {
		return file;
	}
	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		file = fileName;
		players.setFileOnChildren(file);
		tiles.setFileOnChildren(file);
	}
	/**
	 * The name of the file this is to be written to.
	 */
	private String file;
	/**
	 * @return the collection of tiles
	 */
	@Override
	public TileCollection getTiles() {
		return tiles;
	}
	/**
	 * @return a clone of this object.
	 */
	@Override
	public IMap deepCopy() {
		final SPMap retval = new SPMap(version, myRows, myCols, file);
		for (Player player : players) {
			retval.players.addPlayer(player.deepCopy());
		}
		retval.tiles = tiles.deepCopy();
		return retval;
	}
}

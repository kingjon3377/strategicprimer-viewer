package model.map;

import view.util.SystemOut;

/**
 * A map, consisting of tiles, units, and fortresses. Each fortress is on a
 * tile; each unit is either in a fortress or on a tile directly.
 *
 * @author Jonathan Lovelace
 *
 */
public class SPMap extends XMLWritableImpl implements IMap {
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
	 * @param ver the map version
	 * @param rows the number of rows
	 * @param cols the number of columns
	 * @param fileName the file this was loaded from
	 */
	public SPMap(final int ver, final int rows, final int cols,
			final String fileName) {
		super(fileName);
		tiles = new TileCollection(fileName);
		players = new PlayerCollection();
		myRows = rows;
		myCols = cols;
		version = ver;
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
	 * @param tile the tile to add
	 */
	public final void addTile(final Tile tile) {
		tiles.addTile(tile);
	}

	/**
	 * Add a player to the game.
	 *
	 * @param player the player to add
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
	 * @param obj another object
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
			sbuild.append(point.row);
			sbuild.append(", ");
			sbuild.append(point.col);
			sbuild.append("): ");
			sbuild.append(tiles.getTile(point));
		}
		return sbuild.toString();
	}

	/**
	 * @param obj another map
	 * @return whether it's a strict subset of this one
	 */
	@Override
	public boolean isSubset(final IMap obj) {
		if (cols() == obj.cols() && rows() == obj.rows()) {
			return players.isSubset(obj.getPlayers()) // NOPMD
					&& tiles.isSubset(obj.getTiles());
		} else {
			SystemOut.SYS_OUT.println("Sizes differ");
			return false;
		}
	}

	/**
	 * Compare to another map.
	 *
	 * @param other the other map
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final IMap other) {
		return equals(other) ? 0 : hashCode() - other.hashCode();
	}

	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		super.setFile(fileName);
		players.setFileOnChildren(fileName);
		tiles.setFileOnChildren(fileName);
	}
	/**
	 * @return the collection of tiles
	 */
	@Override
	public TileCollection getTiles() {
		return tiles;
	}
}

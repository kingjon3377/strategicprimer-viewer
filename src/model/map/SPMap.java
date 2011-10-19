package model.map;

/**
 * A map, consisting of tiles, units, and fortresses. Each fortress is on a
 * tile; each unit is either in a fortress or on a tile directly.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SPMap {
	/**
	 * Map version.
	 */
	public static final int VERSION = 1;

	/**
	 * Constructor.
	 */
	public SPMap() {
		tiles = new TileCollection();
		players = new PlayerCollection();
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
	private TileCollection tiles; // NOPMD
	/**
	 * The players in the game.
	 */
	private final PlayerCollection players; // NOPMD

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
	public final void addPlayer(final Player player) {
		players.addPlayer(player);
	}

	/**
	 * @param row
	 *            the row
	 * @param col
	 *            the column
	 * @return the tile at those coordinates
	 */
	public final Tile getTile(final int row, final int col) {
		return tiles.getTile(PointFactory.point(row, col));
	}

	/**
	 * @return the players in the map
	 */
	public PlayerCollection getPlayers() {
		return players;
	}

	/**
	 * @param obj
	 *            another object
	 * @return whether it is an identical map.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof SPMap && myCols == ((SPMap) obj).cols()
						&& myRows == ((SPMap) obj).rows()
						&& players.equals(((SPMap) obj).getPlayers()) && tiles
							.equals(((SPMap) obj).tiles));
	}

	/**
	 * @return a hash value for the map
	 */
	@Override
	public int hashCode() {
		return myRows + myCols << 2 + players.hashCode() << 4 + tiles
				.hashCode() << 10;
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
}

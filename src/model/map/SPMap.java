package model.map;

import java.io.PrintWriter;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A map, consisting of tiles, units, and fortresses. Each fortress is on a
 * tile; each unit is either in a fortress or on a tile directly.
 *
 * @author Jonathan Lovelace
 *
 */
public class SPMap implements IMap {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Map max version.
	 */
	public static final int MAX_VERSION = 1;
	/**
	 * The map's version and dimensions.
	 */
	private final MapDimensions dimensions;
	/**
	 * Constructor taking the size and version as an encapsulated object.
	 * @param dim the dimensions
	 */
	public SPMap(final MapDimensions dim) {
		super();
		tiles = new TileCollection();
		players = new PlayerCollection();
		dimensions = dim;
	}
	/**
	 * The tiles on the map.
	 */
	private TileCollection tiles; // NOPMD
	/**
	 * The players in the game.
	 */
	private final PlayerCollection players; // NOPMD

	/**
	 * Add a tile to the map.
	 *
	 * @param tile the tile to add
	 * @param point the point at which to add the tile
	 */
	public final void addTile(final Point point, final Tile tile) {
		tiles.addTile(point, tile);
	}

	/**
	 * Add a player to the game.
	 *
	 * @param player the player to add
	 */
	@Override
	public final void addPlayer(final Player player) {
		players.add(player);
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
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof SPMap && getDimensions().equals(((IMap) obj).getDimensions())
						&& players.equals(((IMap) obj).getPlayers()) && tiles
							.equals(((SPMap) obj).tiles));
	}

	/**
	 *
	 * @return a hash value for the map
	 */
	@Override
	public int hashCode() {
		return getDimensions().hashCode() + players.hashCode() << 4 + tiles
				.hashCode() << 10;
	}

	/**
	 *
	 * @return a String representation of the map
	 */
	@Override
	public String toString() {
		// This will be big; assume at least half a meg. Fortunately it is rarely called.
		final StringBuilder sbuild = new StringBuilder(524288).append("SP Map with ");
		sbuild.append(dimensions.rows);
		sbuild.append(" rows and ");
		sbuild.append(dimensions.cols);
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
	 * TODO: Write tests for this.
	 *
	 * @param obj another map
	 * @return whether it's a strict subset of this one
	 * @param out the stream to write details of the difference to
	 */
	@Override
	public boolean isSubset(final IMap obj, final PrintWriter out) {
		if (getDimensions().equals(obj.getDimensions())) {
			return players.isSubset(obj.getPlayers(), out) // NOPMD
					&& tiles.isSubset(obj.getTiles(), out);
		} else {
			out.println("Sizes differ");
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
	 * @return the collection of tiles
	 */
	@Override
	public TileCollection getTiles() {
		return tiles;
	}
	/**
	 * @return The map's dimensions and version.
	 */
	@Override
	public MapDimensions getDimensions() {
		return dimensions;
	}
}

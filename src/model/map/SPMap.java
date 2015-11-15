package model.map;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A map, consisting of tiles, units, and fortresses. Each fortress is on a
 * tile; each unit is either in a fortress or on a tile directly.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated the old map API is deprecated in this branch
 */
@Deprecated
public class SPMap implements IMutableMap {
	/**
	 * The map's version and dimensions.
	 */
	private final MapDimensions dimensions;

	/**
	 * What to shift the hash-code of the players collection by in calculating
	 * ours.
	 */
	private static final int PLAYERS_HASH_SHFT = 4;
	/**
	 * What to shift the hash-code of the tiles collection by in calculating
	 * ours.
	 */
	private static final int TILES_HASH_SHIFT = 10;
	/**
	 * Constructor taking the size and version as an encapsulated object.
	 *
	 * @param dim the dimensions
	 */
	public SPMap(final MapDimensions dim) {
		tiles = new TileCollection();
		players = new PlayerCollection();
		dimensions = dim;
	}

	/**
	 * The tiles on the map.
	 */
	private final TileCollection tiles; // NOPMD
	/**
	 * The players in the game.
	 */
	private final PlayerCollection players; // NOPMD

	/**
	 * Add a tile to the map. Because we use a mutable collection of tiles, the
	 * tile must be mutable.
	 *
	 * FIXME: There should be an immutable map implementation ... unless we can
	 * finish MapNG and switch to it quickly enough.
	 *
	 * @param tile the tile to add
	 * @param point the point at which to add the tile
	 */
	public final void addTile(final Point point, final IMutableTile tile) {
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
	public final IMutableTile getTile(final Point point) {
		return tiles.getTile(point);
	}

	/**
	 *
	 * @return the players in the map
	 */
	@Override
	public IMutablePlayerCollection getPlayers() {
		return players;
	}

	/**
	 * @param obj another object
	 *
	 * @return whether it is an identical map.
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof IMap
				&& getDimensions().equals(((IMap) obj).getDimensions())
				&& players.equals(((IMap) obj).getPlayers())
				&& tiles.equals(((IMap) obj).getTiles());
	}

	/**
	 *
	 * @return a hash value for the map
	 */
	@Override
	public int hashCode() {
		return getDimensions().hashCode() + (players.hashCode() << PLAYERS_HASH_SHFT)
				+ (tiles.hashCode() << TILES_HASH_SHIFT);
	}

	/**
	 *
	 * @return a String representation of the map
	 */
	@Override
	public String toString() {
		// This will be big; assume at least half a meg. Fortunately it is
		// rarely called.
		final StringBuilder sbuild = new StringBuilder(524288)
				.append("SP Map with ");
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
		return NullCleaner.assertNotNull(sbuild.toString());
	}

	/**
	 * TODO: Write tests for this.
	 *
	 * @param obj another map
	 * @return whether it's a strict subset of this one
	 * @param ostream the stream to write details of the difference to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IMap obj, final Appendable ostream,
			final String context) throws IOException {
		if (getDimensions().equals(obj.getDimensions())) {
			return players.isSubset(obj.getPlayers(), ostream, context) // NOPMD
					&& tiles.isSubset(obj.getTiles(), ostream, context);
		} else {
			ostream.append(context);
			ostream.append("\tSizes differ\n");
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
	public int compareTo(@Nullable final IMap other) {
		if (equals(other)) {
			return 0; // NOPMD
		} else {
			return hashCode() - Objects.hashCode(other);
		}
	}

	/**
	 * @return the collection of tiles
	 */
	@Override
	public IMutableTileCollection getTiles() {
		return tiles;
	}

	/**
	 * @return The map's dimensions and version.
	 */
	@Override
	public MapDimensions getDimensions() {
		return dimensions;
	}
	/**
	 * @return a copy of this map
	 * @param zero whether to "zero" sensitive data (probably just DCs)
	 */
	@Override
	public IMap copy(final boolean zero) {
		SPMap retval = new SPMap(dimensions);
		for (Player player : players) {
			if (player != null) {
				retval.addPlayer(player);
			}
		}
		for (Point point : tiles) {
			if (point != null && tiles.hasTile(point)) {
				ITile tile = NullCleaner.assertNotNull(tiles.getTile(point));
				Tile newTile = new Tile(tile.getTerrain());
				for (TileFixture fixture : tile) {
					if (fixture instanceof IEvent) {
						newTile.addFixture(fixture.copy(zero));
					} else {
						// TODO: Should we zero other fixtures?
						newTile.addFixture(fixture.copy(false));
					}
				}
				retval.addTile(point, newTile);
			}
		}
		return retval;
	}
}

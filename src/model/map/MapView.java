package model.map;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A view of a map. This is in effect an extension of SPMap that adds the
 * current turn, the current player, and eventually changesets.
 *
 * FIXME: What makes a "MapView" more than a "Map" should be extracted to an
 * interface, so we can have immutable vs mutable implementations, and thus not
 * have to return the mutable player collection or tile collection from an
 * immutable map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 * @deprecated the old API is deprecated in this branch
 */
@Deprecated
public class MapView implements IMutableMapView {
	/**
	 * The map we wrap.
	 */
	private final IMap map;
	/**
	 * The current turn.
	 */
	private int turn;

	/**
	 * Constructor. We get the current-player *object* from the wrapped map.
	 *
	 * @param wrapped the map this wraps
	 * @param curPlayer the current player's number
	 * @param curTurn the current turn
	 */
	public MapView(final IMap wrapped, final int curPlayer, final int curTurn) {
		map = wrapped;
		map.getPlayers().getPlayer(curPlayer).setCurrent(true);
		turn = curTurn;
	}

	/**
	 * Test whether another map or map view is a subset of this one.
	 *
	 * TODO: Check changesets.
	 *
	 * TODO: Test this.
	 *
	 * @param obj the map to check
	 * @return whether it's a strict subset of this one
	 * @param ostream the stream to write details to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IMap obj, final Appendable ostream,
			final String context) throws IOException {
		return map.isSubset(obj, ostream, context);
	}

	/**
	 * @param obj another map.
	 * @return the result of a comparison with it.
	 */
	@Override
	public int compareTo(final IMap obj) {
		return map.compareTo(obj);
	}

	/**
	 * Add a player to the wrapped map.
	 *
	 * @param newPlayer the player to add
	 */
	@Override
	public void addPlayer(final Player newPlayer) {
		final IPlayerCollection pColl = map.getPlayers();
		if (map instanceof IMutableMap) {
			((IMutableMap) map).addPlayer(newPlayer);
		} else if (pColl instanceof IMutablePlayerCollection) {
			((IMutablePlayerCollection) pColl).add(newPlayer);
		} else {
			throw new IllegalStateException(
					"MapView#addPlayer() with immutable IMap and IPlayerCollection");
		}
	}

	/**
	 * @param point a pair of coordinates
	 * @return the tile at those coordinates
	 */
	@Override
	public IMutableTile getTile(final Point point) {
		if (map instanceof IMutableMap) {
			return ((IMutableMap) map).getTile(point);
		} else {
			throw new IllegalStateException(
					"Mutable view of immutable map can't be queried");
		}
	}

	/**
	 * @return the collection of players in the map
	 */
	@Override
	public IMutablePlayerCollection getPlayers() {
		if (map instanceof IMutableMap) {
			return ((IMutableMap) map).getPlayers();
		} else {
			throw new IllegalStateException(
					"Mutable view of immutable map can't be queried");
		}
	}

	/**
	 * TODO: changesets affect this.
	 *
	 * @return the collection of tiles that make up the map.
	 */
	@Override
	public IMutableTileCollection getTiles() {
		if (map instanceof IMutableMap) {
			return ((IMutableMap) map).getTiles();
		} else {
			throw new IllegalStateException(
					"Mutable view of immutable map can't be queried");
		}
	}

	/**
	 * Set the current player.
	 *
	 * @param current the new current player (number)
	 */
	@Override
	public void setCurrentPlayer(final int current) {
		map.getPlayers().getCurrentPlayer().setCurrent(false);
		map.getPlayers().getPlayer(current).setCurrent(true);
	}

	/**
	 * Set the current turn.
	 *
	 * @param current the new current turn
	 */
	@Override
	public void setCurrentTurn(final int current) {
		turn = current;
	}

	/**
	 * TODO: sub-maps, changesets.
	 *
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof IMapView
				&& equalsImpl((IMapView) obj);
	}
	/**
	 * @param obj another map-view
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final IMapView obj) {
		return map.equals(obj.getMap()) && turn == obj.getCurrentTurn();
	}
	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return map.hashCode();
	}

	/**
	 * @return the current turn
	 */
	@Override
	public int getCurrentTurn() {
		return turn;
	}

	/**
	 * TODO: How does this interact with changesets? This is primarily used
	 * (should probably *only* be used) in serialization.
	 *
	 * @return the map this wraps
	 */
	@Override
	public IMap getMap() {
		return map;
	}

	/**
	 * @return a String representation of the view.
	 */
	@Override
	public String toString() {
		// This will be big ... assume at least half a meg. Fortunately this is
		// rarely called.
		final StringBuilder builder = new StringBuilder(524288)
				.append("Map view at turn ");
		builder.append(turn);
		builder.append(":\nCurrent player:");
		builder.append(map.getPlayers().getCurrentPlayer());
		builder.append("\nMap:\n");
		builder.append(map);
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @return The map's dimensions and version.
	 */
	@Override
	public MapDimensions getDimensions() {
		return map.getDimensions();
	}
}

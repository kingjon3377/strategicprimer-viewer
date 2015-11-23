package model.map;

import static util.NullStream.DEV_NULL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A collection of players. Using a simple List doesn't work when -1 is the
 * default index if one isn't given in the XML.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
 *
 */
public final class PlayerCollection implements IMutablePlayerCollection {
	/**
	 * The collection this class wraps.
	 */
	private final Map<Integer, Player> players = new HashMap<>();
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner.assertNotNull(Logger.getLogger(PlayerCollection.class.getName()));
	/**
	 * @param player a player-id
	 *
	 * @return the player with that ID, or a new Player with that number if we
	 *         don't have it.
	 */
	@Override
	public Player getPlayer(final int player) {
		final Integer pValue = Integer.valueOf(player);
		if (players.containsKey(pValue)) {
			return NullCleaner.assertNotNull(players.get(pValue)); // NOPMD
		} else {
			return new Player(player, "");
		}
	}

	/**
	 * @return an iterator over the players we contain.
	 */
	@Override
	public Iterator<Player> iterator() {
		return NullCleaner.assertNotNull(players.values().iterator());
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it is another identical PlayerCollection or not
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		try {
			return this == obj || obj instanceof IPlayerCollection
					&& isSubset((IPlayerCollection) obj, DEV_NULL, "")
					&& ((IPlayerCollection) obj).isSubset(this, DEV_NULL, "");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error from NullStream", e);
			return false;
		}
	}

	/**
	 *
	 * @return a hash value for this collection.
	 */
	@Override
	public int hashCode() {
		return players.hashCode();
	}

	/**
	 * Note that this method currently iterates through all the players to find
	 * the one marked current.
	 *
	 *
	 * @return the current player, or a new player with a negative number and
	 *         the empty string for a name.
	 */
	@Override
	public Player getCurrentPlayer() {
		for (final Player player : this) {
			if (player.isCurrent()) {
				return player; // NOPMD
			}
		}
		return new Player(-1, "");
	}

	/**
	 *
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "PlayerCollection";
	}

	/**
	 * @param obj another PlayerCollection
	 * @return whether it's a strict subset of this one
	 * @param ostream the stream to write details of the differences to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IPlayerCollection obj,
			final Appendable ostream, final String context) throws IOException {
		for (final Player player : obj) {
			if (!players.containsValue(player)) {
				ostream.append(context);
				ostream.append("\tExtra player ");
				ostream.append(player.getName());
				ostream.append('\n');
				return false; // NOPMD
			}
		}
		return true;
	}

	/**
	 * @param obj an object
	 * @return whether we contain it
	 */
	@Override
	public boolean contains(final Player obj) {
		return players.containsValue(obj);
	}

	/**
	 * Add a player to the collection.
	 *
	 * @param player the player to add
	 * @return whether the collection was changed by the operation.
	 */
	@Override
	public boolean add(final Player player) {
		if (player.isIndependent()) {
			independent = player;
		}
		final boolean retval = !players.containsValue(player);
		players.put(NullCleaner
				.assertNotNull(Integer.valueOf(player.getPlayerId())), player);
		return retval;
	}

	/**
	 * Remove an object from the collection.
	 *
	 * @param obj an object
	 * @return true if it was removed as a result of this call
	 */
	@Override
	public boolean remove(final Object obj) {
		if (obj instanceof Integer) {
			final boolean retval = players.containsKey(obj);
			players.remove(obj);
			return retval; // NOPMD
		} else if (obj instanceof Player) {
			final boolean retval = players.containsValue(obj);
			players.remove(Integer.valueOf(((Player) obj).getPlayerId()));
			return retval; // NOPMD
		} else {
			return false;
		}
	}

	/**
	 * The player for "independent" fixtures.
	 */
	private Player independent = new Player(-1, "Independent");

	/**
	 * @return a player for "independent" fixtures.
	 */
	@Override
	public Player getIndependent() {
		return independent;
	}

	/**
	 * @return an array of the players
	 */
	public Player[] asArray() {
		return NullCleaner.assertNotNull(players.values().toArray(
				new Player[players.size()]));
	}

	/**
	 * @param zero ignored
	 * @return a copy of this collection
	 */
	@Override
	public IMutablePlayerCollection copy(final boolean zero) {
		final PlayerCollection retval = new PlayerCollection();
		for (Player player : this) {
			if (player != null) {
				retval.add(player);
			}
		}
		return retval;
	}
}

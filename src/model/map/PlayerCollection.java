package model.map;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.eclipse.jdt.annotation.Nullable;

import static util.NullCleaner.assertNotNull;
import static util.NullStream.DEV_NULL;

/**
 * A collection of players. Using a simple List doesn't work when -1 is the default index
 * if one isn't given in the XML.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class PlayerCollection implements IMutablePlayerCollection {
	/**
	 * The collection this class wraps.
	 */
	private final Map<Integer, Player> players = new HashMap<>();
	/**
	 * The player for "independent" fixtures.
	 */
	private Player independent = new Player(-1, "Independent");

	/**
	 * @param player a player-id
	 * @return the player with that ID, or a new Player with that number if we don't have
	 * it.
	 */
	@Override
	public Player getPlayer(final int player) {
		final Integer pValue = Integer.valueOf(player);
		if (players.containsKey(pValue)) {
			return assertNotNull(players.get(pValue));
		} else {
			return new Player(player, "");
		}
	}

	/**
	 * @return an iterator over the players we contain.
	 */
	@Override
	public Iterator<Player> iterator() {
		return assertNotNull(players.values().iterator());
	}

	/**
	 * @param obj an object
	 * @return whether it is another identical PlayerCollection or not
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof IPlayerCollection) &&
										 isSubset((IPlayerCollection) obj, DEV_NULL,
												 "") && ((IPlayerCollection) obj)
																.isSubset(this, DEV_NULL,
																		""));
	}

	/**
	 * @return a hash value for this collection.
	 */
	@Override
	public int hashCode() {
		return players.hashCode();
	}

	/**
	 * Note that this method currently iterates through all the players to find the one
	 * marked current.
	 *
	 * @return the current player, or a new player with a negative number and the empty
	 * string for a name.
	 */
	@Override
	public Player getCurrentPlayer() {
		return StreamSupport.stream(spliterator(), false).filter(Player::isCurrent)
					   .findFirst().orElse(new Player(-1, ""));
	}

	/**
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "PlayerCollection with " + players.size() + " players";
	}

	/**
	 * @param obj     another PlayerCollection
	 * @param ostream the stream to write details of the differences to
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether it's a strict subset of this one
	 */
	@Override
	public boolean isSubset(final IPlayerCollection obj,
							final Formatter ostream, final String context) {
		return StreamSupport.stream(obj.spliterator(), false).allMatch(
				player -> isConditionTrue(ostream, players.containsValue(player),
						"%s\tExtra player %s%n", context, player.getName()));
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
		players.put(assertNotNull(Integer.valueOf(player.getPlayerId())), player);
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
			return retval;
		} else if (obj instanceof Player) {
			final boolean retval = players.containsValue(obj);
			players.remove(Integer.valueOf(((Player) obj).getPlayerId()));
			return retval;
		} else {
			return false;
		}
	}

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
		return assertNotNull(players.values().toArray(new Player[players.size()]));
	}

	/**
	 * @return a copy of this collection
	 */
	@Override
	public IMutablePlayerCollection copy() {
		final IMutablePlayerCollection retval = new PlayerCollection();
		forEach(retval::add);
		return retval;
	}
}

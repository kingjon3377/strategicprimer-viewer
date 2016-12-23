package model.map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A representation of a player in the game.
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
public final class Player implements Comparable<Player>, HasName {
	/**
	 * The player's number.
	 */
	private final int playerID;
	/**
	 * The player's code name.
	 */
	private final String playerName;

	/**
	 * Whether this is the current player or not.
	 */
	private boolean current;

	/**
	 * Constructor.
	 *
	 * @param idNum the player's number
	 * @param name  the player's code name
	 */
	public Player(final int idNum, final String name) {
		playerID = idNum;
		playerName = name;
		current = false;
	}

	/**
	 * @return the player's number
	 */
	public int getPlayerId() {
		return playerID;
	}

	/**
	 * @return the player's code name
	 */
	@Override
	public String getName() {
		return playerName;
	}

	/**
	 * @param obj an object
	 * @return whether it's an identical Player
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Player) &&
										 (playerID == ((Player) obj).playerID) &&
										 playerName.equals(((Player) obj).playerName));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return playerID;
	}

	/**
	 * Compare to another Player.
	 *
	 * @param player the Player to compare to
	 * @return the result of the comparison
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compareTo(final Player player) {
		final int theirs = player.hashCode();
		final int ours = hashCode();
		if (ours > theirs) {
			return 1;
		} else if (ours == theirs) {
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * @return a String representation of the Player
	 */
	@Override
	public String toString() {
		if (playerName.isEmpty()) {
			return "player #" + playerID;
		} else {
			return playerName;
		}
	}

	/**
	 * @return true iff this is the current player
	 */
	public boolean isCurrent() {
		return current;
	}

	/**
	 * @param curr whether this is the current player or not
	 */
	public void setCurrent(final boolean curr) {
		current = curr;
	}

	/**
	 * @return whether this is the (or an) "independent" player---the "owner" of unowned
	 * fixtures.
	 */
	public boolean isIndependent() {
		return "independent".equalsIgnoreCase(playerName);
	}
}

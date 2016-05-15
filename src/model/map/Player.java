package model.map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A representation of a player in the game.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author jsl7
 */
public final class Player implements Comparable<Player>, HasMutableName {
	/**
	 * The player's number.
	 */
	private final int playerID;
	/**
	 * The player's code name.
	 */
	private String playerName;

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
	 * @param curr whether this is the current player or not
	 */
	public void setCurrent(final boolean curr) {
		current = curr;
	}

	/**
	 * @return true iff this is the current player
	 */
	public boolean isCurrent() {
		return current;
	}

	/**
	 * @return whether this is the (or an) "independent" player---the "owner" of unowned
	 * fixtures.
	 */
	public boolean isIndependent() {
		return "independent".equalsIgnoreCase(playerName);
	}

	/**
	 * @param newName the player's new name
	 */
	@Override
	public void setName(final String newName) {
		playerName = newName;
	}
}

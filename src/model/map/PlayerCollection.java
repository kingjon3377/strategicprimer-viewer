package model.map;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of players. Using a simple List doesn't work when -1 is the
 * default index if one isn't given in the XML.
 *
 * @author Jonathan Lovelace
 *
 */
public class PlayerCollection implements Collection<Player>,
		Subsettable<PlayerCollection> {
	/**
	 * The collection this class wraps.
	 */
	private final Map<Integer, Player> players = new HashMap<Integer, Player>();

	/**
	 * @param player a player-id
	 *
	 * @return the player with that ID, or a new Player with that number if we
	 *         don't have it.
	 */
	public Player getPlayer(final int player) {
		return players.containsKey(Integer.valueOf(player)) ? players
				.get(Integer.valueOf(player)) : new Player(player, "");
	}

	/**
	 * @return an iterator over the players we contain.
	 */
	@Override
	public Iterator<Player> iterator() {
		return players.values().iterator();
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it is another identical PlayerCollection or not
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof PlayerCollection && ((PlayerCollection) obj).players
						.equals(players));
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
	 * @param out the stream to write details of the differences to
	 */
	@Override
	public boolean isSubset(final PlayerCollection obj, final PrintStream out) {
		for (final Player player : obj) {
			if (!players.containsValue(player)) {
				out.print("Extra player");
				return false; // NOPMD
			}
		}
		return true;
	}
	/**
	 * @return the number of players
	 */
	@Override
	public int size() {
		return players.size();
	}
	/**
	 * @return whether the collection is empty.
	 */
	@Override
	public boolean isEmpty() {
		return players.isEmpty();
	}
	/**
	 * @param obj an object
	 * @return whether we contain it
	 */
	@Override
	public boolean contains(final Object obj) {
		return players.containsValue(obj);
	}
	/**
	 * @return a view of the collection as an array
	 */
	@Override
	public Object[] toArray() {
		return players.values().toArray();
	}
	/**
	 * @param <T> a type
	 * @param array an array of that type
	 * @return a view of the collection in that array
	 */
	@Override
	public <T> T[] toArray(final T[] array) {
		return players.values().toArray(array);
	}
	/**
	 * Add a player to the collection.
	 * @param player the player to add
	 * @return whether the collection was changed by the operation.
	 */
	@Override
	public boolean add(final Player player) {
		final boolean retval = !players.containsValue(player);
		players.put(Integer.valueOf(player.getPlayerId()), player);
		return retval;
	}
	/**
	 * Remove an object from the collection.
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
	 * @param coll a collection of objects of an unknown type
	 * @return whether we contain all of them
	 */
	@Override
	public boolean containsAll(final Collection<?> coll) {
		return players.keySet().containsAll(coll)
				|| players.values().containsAll(coll);
	}
	/**
	 * @param coll a collection of Players
	 * @return true if the collection changed as the result of the call
	 *
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends Player> coll) {
		boolean retval = false;
		for (Player player : coll) {
			if (add(player)) {
				retval = true;
			}
		}
		return retval;
	}
	/**
	 * @param coll a collection of objects of unknown type
	 * @return true if the collection changedd as the result of trying to remove them
	 */
	@Override
	public boolean removeAll(final Collection<?> coll) {
		boolean retval = false;
		for (Object obj : coll) {
			if (remove(obj)) {
				retval = true;
			}
		}
		return retval;
	}
	/**
	 *
	 * @param coll A collection
	 * @return whether the collection changed as a result of this operation.
	 */
	@Override
	public boolean retainAll(final Collection<?> coll) {
		boolean retval = false;
		for (Integer num : players.keySet()) {
			if (!coll.contains(num) && !coll.contains(players.get(num))
					&& remove(num)) {
				retval = true;
			}
		}
		return retval;
	}
	/**
	 * Empty the collection.
	 */
	@Override
	public void clear() {
		players.clear();
	}
}

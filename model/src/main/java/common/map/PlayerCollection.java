package common.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * A collection of players. Using a simple List doesn't work when -1 is the
 * default index if one isn't given in the XML.
 */
public final class PlayerCollection implements IMutablePlayerCollection {
	public PlayerCollection() {
	}

	/**
	 * The collection this class wraps.
	 */
	private final Map<Integer, Player> players = new TreeMap<>();

	/**
	 * The player for "independent" fixtures.
	 */
	private Player independentPlayer = new PlayerImpl(-1, "Independent", "", false, "");

	/**
	 * Get a player by ID number.
	 */
	@Override
	public Player getPlayer(final int player) {
		if (players.containsKey(player)) {
			return players.get(player);
		} else if (player < 0) {
			return new PlayerImpl(player, "", "", false, "");
		} else {
			final Player retval = new PlayerImpl(player, "", "", false, "");
			players.put(player, retval);
			return retval;
		}
	}

	/**
	 * An iterator over the players in the collection.
	 */
	@Override
	public Iterator<Player> iterator() {
		return players.values().iterator();
	}

	@Override
	public int hashCode() {
		return players.hashCode();
	}

	@Override
	public String toString() {
		return String.format("Player collection with %d players", players.size());
	}

	private Player currentPlayer = new PlayerImpl(-1, "", "", true, "");

	/**
	 * Add a player to the collection.
	 */
	@Override
	public void add(final Player player) {
		if (players.containsKey(player.playerId())) {
			throw new IllegalArgumentException("Player must not already exist in players");
		}
		if (player.isIndependent()) {
			independentPlayer = player;
		}
		if (player.current() && (currentPlayer.playerId() < 0 || !currentPlayer.current())) {
			currentPlayer = player;
		}
		players.put(player.playerId(), player);
	}

	/**
	 * Remove a player from the collection.
	 */
	@Override
	public void remove(final Player obj) {
		if (!Objects.equals(obj, players.get(obj.playerId()))) {
			throw new IllegalArgumentException("Player must be in the collection to be removed");
		}
		remove(obj.playerId());
	}

	/**
	 * Remove a player from the collection.
	 */
	@Override
	public void remove(final int obj) {
		if (players.containsKey(obj)) {
			final Player removed = players.remove(obj);
			if (independentPlayer.equals(removed)) {
				independentPlayer = players.values().stream().filter(Player::isIndependent)
					.findAny().orElseGet(() -> new PlayerImpl(-1, "Independent", "", false, ""));
			}
			if (currentPlayer.equals(removed)) {
				currentPlayer = new PlayerImpl(-1, "", "", true, "");
			}
		}
	}

	/**
	 * The player for "independent" fixtures.
	 */
	@Override
	public Player getIndependent() {
		return independentPlayer;
	}

	/**
	 * Clone the collection.
	 */
	@Override
	public IMutablePlayerCollection copy() {
		final IMutablePlayerCollection retval = new PlayerCollection();
		players.values().forEach(retval::add);
		return retval;
	}

	/**
	 * The current player, or a new player with an empty name and number -1.
	 */
	@Override
	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * An object is equal iff it is a player collection with exactly the
	 * players we have.
	 */
	@Override
	public boolean equals(final Object obj) {
		final Predicate<String> unknownName = str -> str.isEmpty() || "unknown".equalsIgnoreCase(str);
		if (obj == this) {
			return true;
		} else if (obj instanceof final IPlayerCollection pc) {
			// TODO: What about "current player" flag?
			for (final Player player : pc) {
				if (players.containsValue(player)) {
					continue;
				}
				final Player ours = players.get(player.playerId());
				if (ours == null || !unknownName.test(ours.getName())) {
					return false;
				}
			}
			for (final Player player : this) {
				final Player theirs = pc.getPlayer(player.playerId());
				if (!theirs.getName().equals(player.getName()) && !unknownName.test(theirs.getName())) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean containsAll(final IPlayerCollection other) {
		final Collection<Player> collection = players.values();
		for (final Player player : other) {
			if (!collection.contains(player)) {
				return false;
			}
		}
		return true;
	}
}

package legacy.map;

import lovelace.util.LovelaceLogger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * A collection of players. Using a simple List doesn't work when -1 is the
 * default index if one isn't given in the XML.
 */
public class LegacyPlayerCollection implements IMutableLegacyPlayerCollection {
	public LegacyPlayerCollection() {
	}

	/**
	 * The collection this class wraps.
	 *
	 * TODO: Make private again once legacy version is dropped.
	 */
	protected final Map<Integer, Player> players = new TreeMap<>();

	/**
	 * The player for "independent" fixtures.
	 */
	private Player independentPlayer = new PlayerImpl(-1, "Independent");

	/**
	 * Get a player by ID number.
	 */
	@Override
	public Player getPlayer(final int player) {
		if (players.containsKey(player)) {
			return players.get(player);
		} else if (player < 0) {
			return new PlayerImpl(player, "");
		} else {
			final Player retval = new PlayerImpl(player, "");
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
		return "Player collection with %d players".formatted(players.size());
	}

	private Player current = new PlayerImpl(-1, "");

	/**
	 * Add a player to the collection.
	 */
	@Override
	public void add(final Player player) {
		if (player.isIndependent()) {
			independentPlayer = player;
		}
		if (player.isCurrent() && (current.getPlayerId() < 0 || !current.isCurrent())) {
			current = player;
		}
		players.put(player.getPlayerId(), player);
	}

	/**
	 * Remove a player from the collection.
	 */
	@Override
	public void remove(final Player obj) {
		remove(obj.getPlayerId());
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
						.findAny().orElseGet(() -> new PlayerImpl(-1, "Independent"));
			}
			if (current.equals(removed)) {
				current = new PlayerImpl(-1, "");
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
	 * The current player, or a new player with an empty name and number -1.
	 */
	@Override
	public Player getCurrentPlayer() {
		return current;
	}

	/**
	 * Set the current player.
	 */
	@Override
	public void setCurrentPlayer(final Player currentPlayer) {
		final Player oldCurrent = current;
		if (oldCurrent instanceof final MutablePlayer mp) {
			mp.setCurrent(false);
		} else {
			LovelaceLogger.warning("Previous current player wasn't mutable");
		}
		if (players.containsValue(currentPlayer)) {
			current = currentPlayer;
		} else { // TODO: Why not add()?
			current = players.values().stream().filter((p) -> p.getPlayerId() == currentPlayer.getPlayerId()).findAny()
					.orElse(currentPlayer);
		}
		if (current instanceof final MutablePlayer mp) {
			mp.setCurrent(true);
		} else {
			LovelaceLogger.warning("Newly current player wasn't mutable");
		}
	}

	@Override
	public boolean containsAll(final ILegacyPlayerCollection other) {
		final Collection<Player> collection = players.values();
		for (final Player player : other) {
			if (!collection.contains(player)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A player collection is a subset if it has no players we don't.
	 */
	@Override
	public boolean isSubset(final Iterable<Player> obj, final Consumer<String> report) {
		boolean retval = true;
		for (final Player player : obj) {
			if (!players.containsValue(player)) {
				if (players.containsKey(player.getPlayerId())) {
					final Player match = players.get(player.getPlayerId());
					if (player.getName().isEmpty() || "unknown".equalsIgnoreCase(player.getName())) {
						continue;
					} else {
						report.accept("Matching players differ: our %s, their %s".formatted(
								match.toString(), player));
					}
				} else {
					report.accept("Extra player " + player.getName());
				}
				retval = false;
			}
		}
		return retval;
	}

	/**
	 * Clone the collection.
	 */
	@Override
	public IMutableLegacyPlayerCollection copy() {
		final IMutableLegacyPlayerCollection retval = new LegacyPlayerCollection();
		players.values().forEach(retval::add);
		return retval;
	}

	/**
	 * An object is equal iff it is a player collection with exactly the
	 * players we have.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof final ILegacyPlayerCollection pc) {
			return isSubset(pc, (ignored) -> {
			}) &&
					pc.isSubset(this, (ignored) -> {
					});
		} else {
			return false;
		}
	}
}

package common.map;

import java.util.function.Consumer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * A collection of players. Using a simple List doesn't work when -1 is the
 * default index if one isn't given in the XML.
 */
public class PlayerCollection implements IMutablePlayerCollection {
	private static final Logger LOGGER = Logger.getLogger(PlayerCollection.class.getName());
	public PlayerCollection() {
	}
	/**
	 * The collection this class wraps.
	 */
	private final Map<Integer, Player> players = new TreeMap<>();

	/**
	 * The player for "independent" fixtures.
	 */
	private Player independentPlayer = new PlayerImpl(-1, "Independent");

	/**
	 * Get a player by ID number.
	 */
	@Override
	public Player getPlayer(int player) {
		if (players.containsKey(player)) {
			return players.get(player);
		} else if (player < 0) {
			return new PlayerImpl(player, "");
		} else {
			Player retval = new PlayerImpl(player, "");
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

	/**
	 * A player collection is a subset if it has no players we don't.
	 */
	@Override
	public boolean isSubset(Iterable<Player> obj, Consumer<String> report) {
		boolean retval = true;
		for (Player player : obj) {
			if (!players.containsValue(player)) {
				if (players.containsKey(player.getPlayerId())) {
					Player match = players.get(player.getPlayerId());
					if (player.getName().isEmpty() || "unknown".equals(player.getName().toLowerCase())) {
						continue;
					} else {
						report.accept(String.format(
							"Matching players differ: our %s, their %s",
							match.toString(), player.toString()));
					}
				} else {
					report.accept("Extra player " + player.getName());
				}
				retval = false;
			}
		}
		return retval;
	}

	@Override
	public int hashCode() {
		return players.hashCode();
	}

	@Override
	public String toString() {
		return String.format("Player collection with %d players", players.size());
	}

	private Player current = new PlayerImpl(-1, "");

	/**
	 * Add a player to the collection.
	 */
	@Override
	public void add(Player player) {
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
	public void remove(Player obj) {
		remove(obj.getPlayerId());
	}

	/**
	 * Remove a player from the collection.
	 */
	@Override
	public void remove(int obj) {
		if (players.containsKey(obj)) {
			Player removed = players.remove(obj);
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
	 * Clone the collection.
	 */
	@Override
	public IMutablePlayerCollection copy() {
		IMutablePlayerCollection retval = new PlayerCollection();
		players.values().forEach(retval::add);
		return retval;
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
	public void setCurrentPlayer(Player currentPlayer) {
		Player oldCurrent = current;
		if (oldCurrent instanceof MutablePlayer) {
			((MutablePlayer) oldCurrent).setCurrent(false);
		} else {
			LOGGER.warning("Previous current player wasn't mutable");
		}
		if (players.values().contains(currentPlayer)) {
			current = currentPlayer;
		} else { // TODO: Why not add()?
			current = players.values().stream().filter((p) -> p.getPlayerId() == currentPlayer.getPlayerId()).findAny().orElse(currentPlayer);
		}
		if (current instanceof MutablePlayer) {
			((MutablePlayer) current).setCurrent(true);
		} else {
			LOGGER.warning("Newly current player wasn't mutable");
		}
	}

	/**
	 * An object is equal iff it is a player collection with exactly the
	 * players we have.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof IPlayerCollection) {
			return isSubset((IPlayerCollection) obj, (ignored) -> {}) &&
				((IPlayerCollection) obj).isSubset(this, (ignored) -> {});
		} else {
			return false;
		}
	}
}

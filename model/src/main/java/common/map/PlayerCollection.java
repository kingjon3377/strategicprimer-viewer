package common.map;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import lovelace.util.LovelaceLogger;

/**
 * A collection of players. Using a simple List doesn't work when -1 is the
 * default index if one isn't given in the XML.
 */
public class PlayerCollection implements IMutablePlayerCollection {
    public PlayerCollection() {
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
        return String.format("Player collection with %d players", players.size());
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
            current = players.values().stream().filter((p) -> p.getPlayerId() == currentPlayer.getPlayerId()).findAny().orElse(currentPlayer);
        }
        if (current instanceof final MutablePlayer mp) {
            mp.setCurrent(true);
        } else {
            LovelaceLogger.warning("Newly current player wasn't mutable");
        }
    }

    /**
     * An object is equal iff it is a player collection with exactly the
     * players we have.
     */
    @Override
    public boolean equals(final Object obj) {
		Predicate<String> unknownName = str -> str.isEmpty() || "unknown".equalsIgnoreCase(str);
		if (obj == this) {
            return true;
        } else if (obj instanceof final IPlayerCollection pc) {
			// TODO: What about "current player" flag?
			for (final Player player : pc) {
				if (players.containsValue(player)) {
					continue;
				}
				Player ours = players.get(player.getPlayerId());
				if (ours == null || !unknownName.test(ours.getName())) {
					return false;
				}
			}
			for (final Player player : this) {
				Player theirs = pc.getPlayer(player.getPlayerId());
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

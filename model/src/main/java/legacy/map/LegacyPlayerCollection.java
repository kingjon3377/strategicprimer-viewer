package legacy.map;

import common.map.IPlayerCollection;
import common.map.Player;
import common.map.PlayerCollection;

import java.util.function.Consumer;

/**
 * A collection of players. Using a simple List doesn't work when -1 is the
 * default index if one isn't given in the XML.
 */
public class LegacyPlayerCollection extends PlayerCollection implements IMutableLegacyPlayerCollection {
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
                        report.accept(String.format(
                                "Matching players differ: our %s, their %s",
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

package changesets.operations.player;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import org.jetbrains.annotations.NotNull;

public final class RemovePlayerChangeset implements Changeset {
	private final Player player;

	public RemovePlayerChangeset(final @NotNull Player player) {
		this.player = player;
	}

	public @NotNull Changeset invert() {
		return new AddPlayerChangeset(player);
	}

	private void checkPrecondition(final @NotNull IMap map) throws PreconditionFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (final Player item : players) {
			if (item.equals(player)) {
				return;
			}
		}
		throw new PreconditionFailureException("Cannot remove player if not present in the map");
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.removePlayer(player);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.removePlayer(player);
		return retval;
	}

	@Override
	public String toString() {
		return "RemovePlayerChangeset{player=%s}".formatted(player);
	}
}

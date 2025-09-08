package changesets.operations.player;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import org.jspecify.annotations.NonNull;

/**
 * A changeset to add a player to the game.
 */
public final class AddPlayerChangeset implements Changeset {
	private final Player player;

	public AddPlayerChangeset(final @NonNull Player player) {
		this.player = player;
	}

	public @NonNull Changeset invert() {
		return new RemovePlayerChangeset(player);
	}

	private void checkPrecondition(final @NonNull IMap map) throws PreconditionFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (final Player item : players) {
			if (item.playerId() == player.playerId()) {
				throw new PreconditionFailureException("Cannot add player if another exists with same ID");
			}
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.addPlayer(player);
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.addPlayer(player);
		return retval;
	}

	@Override
	public String toString() {
		return "AddPlayerChangeset{player=%s}".formatted(player);
	}
}

package changesets.operations.player;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A changeset to add a player to the game.
 */
public final class AddPlayerChangeset implements Changeset {
	private final Player player;

	public AddPlayerChangeset(final @NotNull Player player) {
		this.player = player;
	}

	public @NotNull Changeset invert() {
		return new RemovePlayerChangeset(player);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (final Player item : players) {
			if (item.playerId() == player.playerId()) {
				throw new PreconditionFailureException("Cannot add player if another exists with same ID");
			}
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.addPlayer(player);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.addPlayer(player);
		return retval;
	}
}

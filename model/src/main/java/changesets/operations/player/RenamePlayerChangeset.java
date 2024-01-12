package changesets.operations.player;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import common.map.PlayerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * A changeeset for changing the name of a player in the map.
 */
public final class RenamePlayerChangeset implements Changeset {
	private final int playerId;
	private final @NotNull String oldName;
	private final @NotNull String newName;

	public RenamePlayerChangeset(final int playerId, final @NotNull String oldName, final @NotNull String newName) {
		this.playerId = playerId;
		this.oldName = oldName;
		this.newName = newName;
	}

	public @NotNull Changeset invert() {
		return new RenamePlayerChangeset(playerId, newName, oldName);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (final Player item : players) {
			if (item.playerId() == playerId) {
				if (oldName.equals(item.getName())) {
					return;
				} else {
					throw new PreconditionFailureException("Cannot rename player if old name doesn't match");
				}
			}
		}
		throw new PreconditionFailureException("Cannot rename player if not present in the map");
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		map.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
	}

	private @NotNull Player alteredCopy(final Player oldPlayer) {
		return new PlayerImpl(playerId, newName, oldPlayer.country(), oldPlayer.current(), oldPlayer.portrait());
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		retval.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
		return retval;
	}
}

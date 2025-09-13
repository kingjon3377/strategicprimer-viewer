package changesets.operations.player;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import common.map.PlayerImpl;

/**
 * A changeset for changing the name of a player in the map.
 */
public final class RenamePlayerChangeset implements Changeset {
	private final int playerId;
	private final String oldName;
	private final String newName;

	public RenamePlayerChangeset(final int playerId, final String oldName, final String newName) {
		this.playerId = playerId;
		this.oldName = oldName;
		this.newName = newName;
	}

	public Changeset invert() {
		return new RenamePlayerChangeset(playerId, newName, oldName);
	}

	private void checkPrecondition(final IMap map) throws PreconditionFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (final Player item : players) {
			if (item.playerId() == playerId) {
				if (oldName.equals(item.name())) {
					return;
				} else {
					throw new PreconditionFailureException("Cannot rename player if old name doesn't match");
				}
			}
		}
		throw new PreconditionFailureException("Cannot rename player if not present in the map");
	}

	@Override
	public void applyInPlace(final IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		map.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
	}

	private Player alteredCopy(final Player oldPlayer) {
		return new PlayerImpl(playerId, newName, oldPlayer.country(), oldPlayer.portrait());
	}

	@Override
	public IMap apply(final IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		retval.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
		return retval;
	}

	@Override
	public String toString() {
		return "RenamePlayerChangeset{playerId=%d, oldName='%s', newName='%s'}".formatted(playerId, oldName, newName);
	}
}

package changesets.operations.player;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import org.jetbrains.annotations.NotNull;

public class RemovePlayerChangeset implements Changeset {
	private final Player player;
	public RemovePlayerChangeset(final @NotNull Player player) {
		this.player = player;
	}
	public Changeset invert() {
		return new AddPlayerChangeset(player);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (final Player item : players) {
			if (item.equals(player)) {
				return;
			}
		}
		throw new PreconditionFailureException("Cannot remove player if not present in the map");
	}
	@Override
	public void applyInPlace(final IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.removePlayer(player);
	}

	@Override
	public IMap apply(final IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.removePlayer(player);
		return retval;
	}
}

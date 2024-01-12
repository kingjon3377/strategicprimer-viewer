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
 * A changeset operation to replace one player with another.
 */
public class ReplacePlayerChangeset implements Changeset {
	private final @NotNull Player toRemove;
	private final @NotNull Player toAdd;

	public ReplacePlayerChangeset(final @NotNull Player toRemove, final @NotNull Player toAdd) {
		this.toRemove = toRemove;
		this.toAdd = toAdd;
	}

	@Override
	public Changeset invert() {
		return new ReplacePlayerChangeset(toAdd, toRemove);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		final IPlayerCollection players = map.getPlayers();
		boolean met = false;
		for (Player item : players) {
			if (item.equals(toRemove)) {
				met = true;
			} else if (item.playerId() == toAdd.playerId()) {
				throw new PreconditionFailureException("Cannot add player with non-unique ID");
			}
		}
		if (!met) {
			throw new PreconditionFailureException("Cannot remove player if not present in the map");
		}
	}

	@Override
	public void applyInPlace(IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.replacePlayer(toRemove, toAdd);
	}

	@Override
	public IMap apply(IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.replacePlayer(toRemove, toAdd);
		return retval;
	}
}

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
public final class ReplacePlayerChangeset implements Changeset {
	private final @NotNull Player toRemove;
	private final @NotNull Player toAdd;

	public ReplacePlayerChangeset(final @NotNull Player toRemove, final @NotNull Player toAdd) {
		this.toRemove = toRemove;
		this.toAdd = toAdd;
	}

	@Override
	public @NotNull Changeset invert() {
		return new ReplacePlayerChangeset(toAdd, toRemove);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		final IPlayerCollection players = map.getPlayers();
		boolean met = false;
		for (final Player item : players) {
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
	public void applyInPlace(final @NotNull IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.replacePlayer(toRemove, toAdd);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.replacePlayer(toRemove, toAdd);
		return retval;
	}
}

package changesets.operations.player;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import org.jspecify.annotations.NonNull;

/**
 * A changeset operation to replace one player with another.
 */
public final class ReplacePlayerChangeset implements Changeset {
	private final @NonNull Player toRemove;
	private final @NonNull Player toAdd;

	public ReplacePlayerChangeset(final @NonNull Player toRemove, final @NonNull Player toAdd) {
		this.toRemove = toRemove;
		this.toAdd = toAdd;
	}

	@Override
	public @NonNull Changeset invert() {
		return new ReplacePlayerChangeset(toAdd, toRemove);
	}

	private void checkPrecondition(final @NonNull IMap map) throws PreconditionFailureException {
		final IPlayerCollection players = map.getPlayers();
		boolean neverMet = true;
		for (final Player item : players) {
			if (item.equals(toRemove)) {
				neverMet = false;
			} else if (item.playerId() == toAdd.playerId()) {
				throw new PreconditionFailureException("Cannot add player with non-unique ID");
			}
		}
		if (neverMet) {
			throw new PreconditionFailureException("Cannot remove player if not present in the map");
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.replacePlayer(toRemove, toAdd);
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.replacePlayer(toRemove, toAdd);
		return retval;
	}

	@Override
	public String toString() {
		return "ReplacePlayerChangeset{toRemove=%s, toAdd=%s}".formatted(toRemove, toAdd);
	}
}

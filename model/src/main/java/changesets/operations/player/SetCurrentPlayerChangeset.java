package changesets.operations.player;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IMutablePlayerCollection;
import common.map.Player;
import org.jspecify.annotations.NonNull;

import java.util.stream.StreamSupport;

public final class SetCurrentPlayerChangeset implements Changeset {
	private final Player oldCurrent;
	private final Player newCurrent;

	public SetCurrentPlayerChangeset(final @NonNull Player oldCurrent, final @NonNull Player newCurrent) {
		if (oldCurrent.playerId() == newCurrent.playerId()) {
			throw new IllegalArgumentException("Changing current player to the same player doesn't make sense");
		}
		this.oldCurrent = oldCurrent;
		this.newCurrent = newCurrent;
	}

	@Override
	public @NonNull Changeset invert() {
		return new SetCurrentPlayerChangeset(newCurrent, oldCurrent);
	}

	private void checkPreconditions(final IMap map) throws PreconditionFailureException {
		if (map.getPlayers().getCurrentPlayer().playerId() != oldCurrent.playerId()) {
			throw new PreconditionFailureException(
					"Can't change current player when 'old current' player isn't current");
		} else if (StreamSupport.stream(map.getPlayers().spliterator(), false)
				.noneMatch(p -> p.playerId() == newCurrent.playerId())) {
			throw new PreconditionFailureException("Can't change current player to not-found player");
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		if (map.getPlayers() instanceof final IMutablePlayerCollection mpc) {
			mpc.setCurrentPlayer(newCurrent);
		} else {
			throw new IllegalArgumentException("Map with immutable player collection");
		}
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		final IMutableMap retval = (IMutableMap) map.copy();
		// TODO: Make a way to construct an IMap with modification to its player collection
		applyInPlace(retval);
		return retval;
	}

	@Override
	public String toString() {
		return "SetCurrentPlayerChangeset{oldCurrent=%s, newCurrent=%s}".formatted(oldCurrent, newCurrent);
	}
}

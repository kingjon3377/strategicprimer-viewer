package changesets.operations.player;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IMutablePlayerCollection;
import common.map.Player;
import common.map.PlayerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.StreamSupport;

public final class SetCurrentPlayerChangeset implements Changeset {
	private final Player oldCurrent;
	private final Player newCurrent;

	public SetCurrentPlayerChangeset(final @NotNull Player oldCurrent, final @NotNull Player newCurrent) {
		if (oldCurrent.playerId() == newCurrent.playerId()) {
			throw new IllegalArgumentException("Changing current player to the same player doesn't make sense");
		}
		this.oldCurrent = oldCurrent;
		this.newCurrent = newCurrent;
	}

	@Override
	public @NotNull Changeset invert() {
		return new SetCurrentPlayerChangeset(newCurrent, oldCurrent);
	}

	private void checkPreconditions(final IMap map) throws PreconditionFailureException {
		if (map.getPlayers().getCurrentPlayer().playerId() != oldCurrent.playerId()) {
			throw new PreconditionFailureException(
					"Can't change current player when 'old current' player isn't current");
		} else if (StreamSupport.stream(map.getPlayers().spliterator(), false).noneMatch(p -> p.playerId() == newCurrent.playerId())) {
			throw new PreconditionFailureException("Can't change current player to not-found player");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		if (map.getPlayers() instanceof IMutablePlayerCollection mpc) {
			mpc.setCurrentPlayer(newCurrent);
		} else {
			throw new IllegalArgumentException("Map with immutable player collection");
		}
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
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

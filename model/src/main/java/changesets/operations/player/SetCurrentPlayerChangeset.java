package changesets.operations.player;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.Player;
import common.map.PlayerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
		Player matchingOld = null;
		Player matchingNew = null;
		for (final Player player : map.getPlayers()) {
			if (player.playerId() == oldCurrent.playerId()) {
				if (player.current()) {
					matchingOld = player;
				} else {
					throw new PreconditionFailureException(
							"Can't change current player when 'old current' player isn't current");
				}
			} else if (player.current() && player.playerId() != oldCurrent.playerId()) {
				throw new PreconditionFailureException("Can't change current player when unexpected player is current");
			} else if (player.playerId() == newCurrent.playerId()) {
				matchingNew = player;
			}
		}
		if (Objects.isNull(matchingOld)) {
			throw new PreconditionFailureException("Can't change current player from not-found player");
		} else if (Objects.isNull(matchingNew)) {
			throw new PreconditionFailureException("Can't change current player to not-found player");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final Player matchingOld = map.getPlayers().getPlayer(oldCurrent.playerId());
		final Player matchingNew = map.getPlayers().getPlayer(newCurrent.playerId());
		final int playerId1 = matchingOld.playerId();
		final @NotNull String name1 = matchingOld.getName();
		final @Nullable String country1 = matchingOld.country();
		final @NotNull String portrait1 = matchingOld.portrait();
		final Player matchingOldCopy = new PlayerImpl(playerId1, name1, country1, false, portrait1);
		final int playerId = matchingNew.playerId();
		final @NotNull String name = matchingNew.getName();
		final @Nullable String country = matchingNew.country();
		final @NotNull String portrait = matchingNew.portrait();
		final Player matchingNewCopy = new PlayerImpl(playerId, name, country, true, portrait);
		// TODO: try-catch here, in case the first operation fails?
		map.replacePlayer(matchingOld, matchingOldCopy);
		map.replacePlayer(matchingNew, matchingNewCopy);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
		final IMutableMap retval = (IMutableMap) map.copy();
		applyInPlace(retval);
		return retval;
	}

	@Override
	public String toString() {
		return "SetCurrentPlayerChangeset{oldCurrent=%s, newCurrent=%s}".formatted(oldCurrent, newCurrent);
	}
}

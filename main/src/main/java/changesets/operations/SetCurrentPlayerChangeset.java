package changesets.operations;

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

public class SetCurrentPlayerChangeset implements Changeset {
	private final Player oldCurrent;
	private final Player newCurrent;
	public SetCurrentPlayerChangeset(final @NotNull Player oldCurrent, final @NotNull Player newCurrent) {
		if (oldCurrent.getPlayerId() == newCurrent.getPlayerId()) {
			throw new IllegalArgumentException("Changing current player to the same player doesn't make sense");
		}
		this.oldCurrent = oldCurrent;
		this.newCurrent = newCurrent;
	}

	@Override
	public Changeset invert() {
		return new SetCurrentPlayerChangeset(newCurrent, oldCurrent);
	}

	private void checkPreconditions(final IMap map) throws ChangesetFailureException {
		Player matchingOld = null;
		Player matchingNew = null;
		for (final Player player : map.getPlayers()) {
            if (player.getPlayerId() == oldCurrent.getPlayerId()) {
                if (!player.isCurrent()) {
                    throw new PreconditionFailureException("Can't change current player when 'old current' player isn't current");
                } else {
					matchingOld = player;
                }
            } else if (player.isCurrent() && player.getPlayerId() != oldCurrent.getPlayerId()) {
                throw new PreconditionFailureException("Can't change current player when unexpected player is current");
            } else if (player.getPlayerId() == newCurrent.getPlayerId()) {
				matchingNew = player;
			}
		}
		if (matchingOld == null) {
			throw new PreconditionFailureException("Can't change current player from not-found player");
		} else if (matchingNew == null) {
			throw new PreconditionFailureException("Can't change current player to not-found player");
		}
	}

	// TODO: Inline once PlayerImpl (-> Player) constructor covers all fields

	@Override
	public void applyInPlace(IMutableMap map) throws ChangesetFailureException {
		checkPreconditions(map);
		// FIXME: Make 'current' an immutable field and constructor parameter
		final Player matchingOld = map.getPlayers().getPlayer(oldCurrent.getPlayerId());
		final Player matchingNew = map.getPlayers().getPlayer(newCurrent.getPlayerId());
		final int playerId1 = matchingOld.getPlayerId();
		final @NotNull String name1 = matchingOld.getName();
		final @Nullable String country1 = matchingOld.getCountry();
		final @NotNull String portrait1 = matchingOld.getPortrait();
		final Player matchingOldCopy = new PlayerImpl(playerId1, name1, Objects.requireNonNullElse(country1, ""), false, portrait1);
		final int playerId = matchingNew.getPlayerId();
		final @NotNull String name = matchingNew.getName();
		final @Nullable String country = matchingNew.getCountry();
		final @NotNull String portrait = matchingNew.getPortrait();
		final Player matchingNewCopy = new PlayerImpl(playerId, name, Objects.requireNonNullElse(country, ""), true, portrait);
		// TODO: try-catch here, in case the first operation fails?
		map.replacePlayer(matchingOld, matchingOldCopy);
		map.replacePlayer(matchingNew, matchingNewCopy);
	}

	@Override
	public IMap apply(IMap map) throws ChangesetFailureException {
		final IMutableMap retval = (IMutableMap) map.copy();
		applyInPlace(retval);
		return retval;
	}
}

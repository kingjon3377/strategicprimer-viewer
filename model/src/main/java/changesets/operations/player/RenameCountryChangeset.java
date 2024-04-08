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

import java.util.Objects;

public final class RenameCountryChangeset implements Changeset {
	private final int playerId;
	private final @NotNull String oldCountry;
	private final @NotNull String newCountry;

	public RenameCountryChangeset(final int playerId, final @NotNull String oldCountry,
	                              final @NotNull String newCountry) {
		this.playerId = playerId;
		this.oldCountry = oldCountry;
		this.newCountry = newCountry;
	}

	public @NotNull Changeset invert() {
		return new RenameCountryChangeset(playerId, newCountry, oldCountry);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (final Player item : players) {
			if (item.playerId() == playerId) {
				if (Objects.requireNonNullElse(item.country(), "").equals(oldCountry)) {
					return;
				} else {
					throw new PreconditionFailureException("Cannot rename player's country if old name doesn't match");
				}
			}
		}
		throw new PreconditionFailureException("Cannot rename player's country if not present in the map");
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		map.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
	}

	private @NotNull Player alteredCopy(final Player oldPlayer) {
		return new PlayerImpl(playerId, oldPlayer.getName(), newCountry, oldPlayer.current(), oldPlayer.portrait());
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

package changesets.operations.player;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import common.map.PlayerImpl;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class RenameCountryChangeset implements Changeset {
	private final int playerId;
	private final @NonNull String oldCountry;
	private final @NonNull String newCountry;

	public RenameCountryChangeset(final int playerId, final @NonNull String oldCountry,
	                              final @NonNull String newCountry) {
		this.playerId = playerId;
		this.oldCountry = oldCountry;
		this.newCountry = newCountry;
	}

	public @NonNull Changeset invert() {
		return new RenameCountryChangeset(playerId, newCountry, oldCountry);
	}

	private void checkPrecondition(final @NonNull IMap map) throws PreconditionFailureException {
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
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		map.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
	}

	private @NonNull Player alteredCopy(final Player oldPlayer) {
		return new PlayerImpl(playerId, oldPlayer.name(), newCountry, oldPlayer.portrait());
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		retval.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
		return retval;
	}

	@Override
	public String toString() {
		return "RenameCountryChangeset{playerId=%d, oldCountry='%s', newCountry='%s'}".formatted(playerId, oldCountry,
				newCountry);
	}
}

package changesets.operations.player;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.IPlayerCollection;
import common.map.Player;
import common.map.PlayerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ChangePlayerPortraitChangeset implements Changeset {
	private final int playerId;
	private final @NotNull String oldPortrait;
	private final @NotNull String newPortrait;

	public ChangePlayerPortraitChangeset(final int playerId, final @NotNull String oldPortrait,
										 final @NotNull String newPortrait) {
		this.playerId = playerId;
		this.oldPortrait = oldPortrait;
		this.newPortrait = newPortrait;
	}

	public @NotNull Changeset invert() {
		return new RenameCountryChangeset(playerId, newPortrait, oldPortrait);
	}

	private void checkPrecondition(final @NotNull IMap map) throws PreconditionFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (final Player item : players) {
			if (item.playerId() == playerId) {
				if (Objects.requireNonNullElse(item.portrait(), "").equals(oldPortrait)) {
					return;
				} else {
					throw new PreconditionFailureException(
							"Cannot change player's portrait if old portrait doesn't match");
				}
			}
		}
		throw new PreconditionFailureException("Cannot change player's portrait if not present in the map");
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		map.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
	}

	private @NotNull Player alteredCopy(final Player oldPlayer) {
		return new PlayerImpl(playerId, oldPlayer.name(), oldPlayer.country(), newPortrait);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		retval.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
		return retval;
	}

	@Override
	public String toString() {
		return "ChangePlayerPortraitChangeset{playerId=%d, oldPortrait='%s', newPortrait='%s'}".formatted(playerId,
				oldPortrait, newPortrait);
	}
}

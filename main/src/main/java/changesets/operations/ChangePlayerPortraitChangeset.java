package changesets.operations;

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

public class ChangePlayerPortraitChangeset implements Changeset {
	private final int playerId;
	private final @NotNull String oldPortrait;
	private final @NotNull String newPortrait;
	public ChangePlayerPortraitChangeset(final int playerId, final @NotNull String oldPortrait,
										 final @NotNull String newPortrait) {
		this.playerId = playerId;
		this.oldPortrait = oldPortrait;
		this.newPortrait = newPortrait;
	}
	public Changeset invert() {
		return new RenameCountryChangeset(playerId, newPortrait, oldPortrait);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		final IPlayerCollection players = map.getPlayers();
		for (Player item : players) {
			if (item.getPlayerId() == playerId) {
				if (Objects.requireNonNullElse(item.getPortrait(), "").equals(oldPortrait)) {
					return;
				} else {
					throw new PreconditionFailureException("Cannot change player's portrait if old portrait doesn't match");
				}
			}
		}
		throw new PreconditionFailureException("Cannot change player's portrait if not present in the map");
	}
	@Override
	public void applyInPlace(IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		map.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
	}

	@NotNull
	private Player alteredCopy(Player oldPlayer) {
		final Player newPlayer;
		if (oldPlayer.getCountry().isEmpty()) {
			newPlayer = new PlayerImpl(playerId, oldPlayer.getName());
		} else {
			newPlayer = new PlayerImpl(playerId, oldPlayer.getName(), oldPlayer.getCountry());
		}
		newPlayer.setPortrait(newPortrait); // FIXME: Make this immutable and a constructor parameter---or else make a builder class
		return newPlayer;
	}

	@Override
	public IMap apply(IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		final Player oldPlayer = map.getPlayers().getPlayer(playerId);
		retval.replacePlayer(oldPlayer, alteredCopy(oldPlayer));
		return retval;
	}
}

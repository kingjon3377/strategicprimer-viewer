package view.exploration;

import java.io.IOException;
import java.util.List;

import model.exploration.IExplorationModel;
import model.map.Player;
import model.map.fixtures.mobile.Unit;
import controller.map.misc.MapHelper;

/**
 * A CLI to help running exploration. Now separated from the "driver" bits, to
 * simplify things.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationCLI {
	/**
	 * The exploration model we use.
	 */
	private final IExplorationModel model;
	/**
	 * The helper to handle user I/O.
	 */
	private final MapHelper helper;
	/**
	 * @param emodel the exploration model to use
	 * @param mhelper the helper to handle user I/O
	 */
	public ExplorationCLI(final IExplorationModel emodel, final MapHelper mhelper) {
		model = emodel;
		helper = mhelper;
	}
	/**
	 * Have the user choose a player.
	 * @return the chosen player, or a player with a negative number if no choice made.
	 * @throws IOException on I/O error
	 */
	public Player choosePlayer() throws IOException {
		final List<Player> players = model.getPlayerChoices();
		final int playerNum = helper.chooseFromList(players,
				"The players shared by all the maps:",
				"No players shared by all the maps.",
				"Please make a selection: ", true);
		return playerNum < 0 ? new Player(-1, "abort") : players.get(playerNum);
	}
	/**
	 * Have the player choose a unit.
	 * @param player the player to whom the unit must belong
	 * @return the chosen unit, or a unit with a negative ID number if none selected.
	 * @throws IOException on I/O error
	 */
	public Unit chooseUnit(final Player player) throws IOException {
		final List<Unit> units = model.getUnits(player);
		final int unitNum = helper.chooseFromList(units, "Player's units:",
				"That player has no units in the master map.",
				"Please make a selection: ", true);
		return unitNum < 0 ? new Unit(new Player(-1, "abort"), "", "", -1) : units.get(unitNum);
	}
}

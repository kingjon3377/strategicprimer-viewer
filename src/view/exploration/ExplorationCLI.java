package view.exploration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.map.IMap;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import util.Pair;
import view.util.SystemOut;
import controller.map.misc.CLIHelper;

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
	private final CLIHelper helper;
	/**
	 * @param emodel the exploration model to use
	 * @param mhelper the helper to handle user I/O
	 */
	public ExplorationCLI(final IExplorationModel emodel, final CLIHelper mhelper) {
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
	/**
	 * Have the player move the selected unit.
	 *
	 * @return the cost of the specified movement, 1 if not possible (in which
	 *         case we update subordinate maps with that tile's tile type but no
	 *         fixtures), or MAX_INT if "exit".
	 * @throws IOException on I/O error
	 */
	public int move() throws IOException {
		final List<TileFixture> allFixtures = new ArrayList<TileFixture>();
		final List<TileFixture> constants = new ArrayList<TileFixture>();
		final int directionNum = helper.inputNumber("Direction to move: ");
		if (directionNum > 7) {
			return Integer.MAX_VALUE; // NOPMD
		}
		final Direction direction = Direction.values()[directionNum];
		final Point point = model.getSelectedUnitLocation();
		// ESCA-JAVA0177:
		final int cost; // NOPMD
		try {
			cost = model.move(direction);
		} catch (TraversalImpossibleException except) {
			SystemOut.SYS_OUT.printC(
					"That direction is impassable; we've made sure ").println(
					"all maps show that at a cost of 1 MP");
			return 1; // NOPMD
		}
		final Point dPoint = model.getDestination(point, direction);
		for (TileFixture fix : model.getMap().getTile(dPoint)) {
			if (SimpleMovement.shouldAlwaysNotice(model.getSelectedUnit(), fix)) {
				constants.add(fix);
			} else if (SimpleMovement.mightNotice(model.getSelectedUnit(), fix)) {
				allFixtures.add(fix);
			}
		}
		SystemOut.SYS_OUT.printC("The explorer comes to ")
				.printC(dPoint.toString()).printC(", a tile with terrain ")
				.println(model.getMap().getTile(dPoint).getTerrain());
		if (allFixtures.isEmpty()) {
			SystemOut.SYS_OUT
					.println("The following fixtures were automatically noticed:");
		} else {
			SystemOut.SYS_OUT.printC(
					"The following fixtures were noticed, all but the ")
					.println("last automtically:");
			Collections.shuffle(allFixtures);
			constants.add(allFixtures.get(0));
		}
		for (TileFixture fix : constants) {
			SystemOut.SYS_OUT.println(fix);
			for (Pair<IMap, String> pair : model.getSubordinateMaps()) {
				final IMap map = pair.first();
				map.getTile(dPoint).addFixture(fix);
			}
		}
		return cost;
	}
	/**
	 * Ask the user for directions the unit should move until it runs out of MP or the user decides to quit.
	 * @throws IOException on I/O error.
	 */
	public void moveUntilDone() throws IOException {
		SystemOut.SYS_OUT.println("Details of the unit:");
		SystemOut.SYS_OUT.println(model.getSelectedUnit().verbose());
		final int totalMP = helper.inputNumber("MP the unit has: ");
		int movement = totalMP;
		final String prompt = new StringBuilder(
				"0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, ").append(
				"6 = W, 7 = NW, 8 = Quit.").toString();
		while (movement > 0) {
			SystemOut.SYS_OUT.printC(movement).printC(" MP of ")
					.printC(totalMP).println(" remaining.");
			SystemOut.SYS_OUT.println(prompt);
			movement -= move();
		}
	}
}

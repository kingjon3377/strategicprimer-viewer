package view.exploration;

import static view.util.SystemOut.SYS_OUT;

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
import model.map.fixtures.towns.Village;
import util.Pair;
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
	 * The prompt to use when the user tells the unit to go nowhere.
	 */
	private static final String FEALTY_PROMPT = "Should any village here swear to the player?  ";
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
	public ExplorationCLI(final IExplorationModel emodel,
			final CLIHelper mhelper) {
		model = emodel;
		helper = mhelper;
	}

	/**
	 * Have the user choose a player.
	 *
	 * @return the chosen player, or a player with a negative number if no
	 *         choice made.
	 * @throws IOException on I/O error
	 */
	public Player choosePlayer() throws IOException {
		final List<Player> players = model.getPlayerChoices();
		final int playerNum = helper.chooseFromList(players,
				"The players shared by all the maps:",
				"No players shared by all the maps.",
				"Please make a selection: ", true);
		if (playerNum < 0 || playerNum >= players.size()) {
			return new Player(-1, "abort");
		} else {
			final Player player = players.get(playerNum);
			assert player != null;
			return player;
		}
	}

	/**
	 * Have the player choose a unit.
	 *
	 * @param player the player to whom the unit must belong
	 * @return the chosen unit, or a unit with a negative ID number if none
	 *         selected.
	 * @throws IOException on I/O error
	 */
	public Unit chooseUnit(final Player player) throws IOException {
		final List<Unit> units = model.getUnits(player);
		final int unitNum = helper.chooseFromList(units, "Player's units:",
				"That player has no units in the master map.",
				"Please make a selection: ", true);
		if (unitNum < 0 || unitNum >= units.size()) {
			return new Unit(new Player(-1, "abort"), "", "", -1);
		} else {
			final Unit unit = units.get(unitNum);
			assert unit != null;
			return unit;
		}
	}

	/**
	 * Change the owner of all the villages on the specified tile in all the
	 * maps to the owner of the currently selected unit.
	 *
	 * @param point the location of the tile in question
	 */
	private void swearVillages(final Point point) {
		final Unit visitor = model.getSelectedUnit();
		if (visitor != null) {
			for (final Pair<IMap, String> mapPair : model.getAllMaps()) {
				final IMap map = mapPair.first();
				for (final TileFixture fix : map.getTile(point)) {
					if (fix instanceof Village) {
						((Village) fix).setOwner(visitor.getOwner());
					}
				}
			}
		}
	}

	/**
	 * Have the player move the selected unit. Throws an exception if no unit is
	 * selected.
	 *
	 * @return the cost of the specified movement, 1 if not possible (in which
	 *         case we update subordinate maps with that tile's tile type but no
	 *         fixtures), or MAX_INT if "exit".
	 * @throws IOException on I/O error
	 */
	public int move() throws IOException {
		final Unit mover = model.getSelectedUnit();
		if (mover == null) {
			throw new IllegalStateException("No unit selected");
		}
		final List<TileFixture> allFixtures = new ArrayList<>();
		final List<TileFixture> constants = new ArrayList<>();
		final int directionNum = helper.inputNumber("Direction to move: ");
		if (directionNum > 8) {
			return Integer.MAX_VALUE; // NOPMD
		}
		final Direction direction = Direction.values()[directionNum];
		assert direction != null;
		final Point point = model.getSelectedUnitLocation();
		// ESCA-JAVA0177:
		int cost; // NOPMD
		try {
			cost = model.move(direction);
		} catch (final TraversalImpossibleException except) { // $codepro.audit.disable logExceptions
			SYS_OUT.printC("That direction is impassable; we've made sure ")
					.println("all maps show that at a cost of 1 MP");
			return 1; // NOPMD
		}
		final Point dPoint = model.getDestination(point, direction);
		for (final TileFixture fix : model.getMap().getTile(dPoint)) {
			if (SimpleMovement.shouldAlwaysNotice(mover, fix)) {
				constants.add(fix);
			} else if (SimpleMovement.mightNotice(mover, fix)) {
				allFixtures.add(fix);
			}
		}
		if (Direction.Nowhere.equals(direction)
				&& helper.inputBoolean(FEALTY_PROMPT)) {
			swearVillages(dPoint);
			cost += 5;
		}
		SYS_OUT.printC("The explorer comes to ").printC(dPoint.toString())
				.printC(", a tile with terrain ")
				.println(model.getMap().getTile(dPoint).getTerrain());
		if (allFixtures.isEmpty()) {
			SYS_OUT.println("The following fixtures were automatically noticed:");
		} else {
			SYS_OUT.printC("The following fixtures were noticed, all but the ")
					.println("last automtically:");
			Collections.shuffle(allFixtures);
			constants.add(allFixtures.get(0));
		}
		for (final TileFixture fix : constants) {
			if (fix != null) {
				SYS_OUT.println(fix);
				for (final Pair<IMap, String> pair : model.getSubordinateMaps()) {
					final IMap map = pair.first();
					map.getTile(dPoint).addFixture(fix);
				}
			}
		}
		return cost;
	}

	/**
	 * Ask the user for directions the unit should move until it runs out of MP
	 * or the user decides to quit.
	 *
	 * @throws IOException on I/O error.
	 */
	public void moveUntilDone() throws IOException {
		final Unit selUnit = model.getSelectedUnit();
		if (selUnit == null) {
			SYS_OUT.println("No unit is selected");
		} else {
			SYS_OUT.println("Details of the unit:");
			SYS_OUT.println(selUnit.verbose());
			final int totalMP = helper.inputNumber("MP the unit has: ");
			int movement = totalMP;
			final String prompt = new StringBuilder(90)
					.append("0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, ")
					.append("6 = W, 7 = NW, 8 = Stay Here, 9 = Quit.")
					.toString();
			while (movement > 0) {
				SYS_OUT.printC(movement).printC(" MP of ")
						.printC(totalMP).println(" remaining.");
				SYS_OUT.println(prompt);
				movement -= move();
			}
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ExplorationCLI";
	}
}

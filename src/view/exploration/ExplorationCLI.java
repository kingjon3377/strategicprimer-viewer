package view.exploration;

import static util.NullCleaner.assertNotNull;
import static view.util.SystemOut.SYS_OUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.map.IMap;
import model.map.IMutableTile;
import model.map.ITile;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Village;

import org.eclipse.jdt.annotation.Nullable;

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
	private static final String FEALTY_PROMPT =
			"Should any village here swear to the player?  ";
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
			return new Player(-1, "abort"); // NOPMD
		} else {
			return assertNotNull(players.get(playerNum));
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
			return new Unit(new Player(-1, "abort"), "", "", -1); // NOPMD
		} else {
			return assertNotNull(units.get(unitNum));
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
	 * @param mover the selected unit
	 * @return the cost of the specified movement, 1 if not possible (in which
	 *         case we update subordinate maps with that tile's tile type but no
	 *         fixtures), or MAX_INT if "exit".
	 * @throws IOException on I/O error
	 */
	public int move(final Unit mover) throws IOException {
		final int directionNum = helper.inputNumber("Direction to move: ");
		if (directionNum > 8) {
			return Integer.MAX_VALUE; // NOPMD
		}
		final Direction direction =
				assertNotNull(Direction.values()[directionNum]);
		// ESCA-JAVA0177:
		int cost; // NOPMD
		try {
			cost = model.move(direction);
		} catch (final TraversalImpossibleException except) {
			SYS_OUT.printC("That direction is impassable; we've made sure ")
					.println("all maps show that at a cost of 1 MP");
			return 1; // NOPMD
		}
		final Point point = model.getSelectedUnitLocation();
		final Point dPoint = model.getDestination(point, direction);
		final List<TileFixture> allFixtures = new ArrayList<>();
		final List<TileFixture> constants = new ArrayList<>();
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
			printAndTransferFixture(dPoint, fix);
		}
		return cost;
	}

	/**
	 * @param dPoint
	 *            the current location
	 * @param fix
	 *            the fixture to copy to subordinate maps. May be null, to
	 *            simplify the caller.
	 */
	private void printAndTransferFixture(final Point dPoint,
			@Nullable final TileFixture fix) {
		if (fix != null) {
			SYS_OUT.println(fix);
			for (final Pair<IMap, String> pair : model.getSubordinateMaps()) {
				final IMap map = pair.first();
				final ITile tile = map.getTile(dPoint);
				if (tile instanceof IMutableTile) {
					((IMutableTile) tile).addFixture(fix);
				} else {
					SYS_OUT.print("Failed to copy fixture to ");
					SYS_OUT.print(pair.second());
					SYS_OUT.println(" because the tile there was not mutable.");
				}
			}
		}
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
			// FIXME: Should be a class-level constant.
			final String prompt =
					assertNotNull(new StringBuilder(90)
							.append("0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, ")
							.append("6 = W, 7 = NW, 8 = Stay Here, 9 = Quit.")
							.toString());
			while (movement > 0) {
				SYS_OUT.printC(movement).printC(" MP of ")
						.printC(totalMP).println(" remaining.");
				SYS_OUT.println(prompt);
				movement -= move(selUnit);
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

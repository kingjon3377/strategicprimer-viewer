package view.exploration;

import controller.map.misc.ICLIHelper;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IDRegistrar;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.exploration.HuntingModel;
import model.exploration.IExplorationModel;
import model.listeners.MovementCostListener;
import model.listeners.MovementCostSource;
import model.map.HasOwner;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import org.eclipse.jdt.annotation.Nullable;
import util.Accumulator;
import util.IntHolder;
import util.Pair;
import util.SingletonRandom;

import static model.map.TileType.Ocean;
import static util.NullCleaner.assertNotNull;

/**
 * A CLI to help running exploration. Now separated from the "driver" bits, to simplify
 * things.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExplorationCLI implements MovementCostSource {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			assertNotNull(Logger.getLogger(ExplorationCLI.class.getName()));
	/**
	 * The direction prompt.
	 */
	private static final String PROMPT =
			"0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, 6 = W, 7 = NW, 8 = Stay Here, " +
					"9 = Quit.";
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
	private final ICLIHelper helper;

	/**
	 * @param explorationModel  the exploration model to use
	 * @param cli the helper to handle user I/O
	 */
	public ExplorationCLI(final IExplorationModel explorationModel, final ICLIHelper cli) {
		model = explorationModel;
		helper = cli;
		huntingModel = new HuntingModel(model.getMap());
		idf = IDFactoryFiller.createFactory(model);
	}

	/**
	 * Have the user choose a player.
	 *
	 * @return the chosen player, or a player with a negative number if no choice made.
	 * @throws IOException on I/O error
	 */
	@Nullable
	public Player choosePlayer() throws IOException {
		final List<Player> players = model.getPlayerChoices();
		final int playerNum = helper.chooseFromList(players,
				"The players shared by all the maps:",
				"No players shared by all the maps.",
				"Please make a selection: ", true);
		if ((playerNum < 0) || (playerNum >= players.size())) {
			return null;
		} else {
			return players.get(playerNum);
		}
	}

	/**
	 * Have the player choose a unit.
	 *
	 * @param player the player to whom the unit must belong
	 * @return the chosen unit, or a unit with a negative ID number if none selected.
	 * @throws IOException on I/O error
	 */
	@Nullable
	public IUnit chooseUnit(final Player player) throws IOException {
		final List<IUnit> units = model.getUnits(player);
		final int unitNum = helper.chooseFromList(units, "Player's units:",
				"That player has no units in the master map.",
				"Please make a selection: ", true);
		if ((unitNum < 0) || (unitNum >= units.size())) {
			return null;
		} else {
			return units.get(unitNum);
		}
	}

	/**
	 * A "hunting model," to get the animals to have traces of.
	 */
	private final HuntingModel huntingModel;
	/**
	 * An ID number factory for the animal tracks.
	 */
	private final IDRegistrar idf;
	/**
	 * Have the player move the selected unit. Throws an exception if no unit is
	 * selected. Movement cost is reported by the driver model to all registered
	 * MovementCostListeners, while any additional costs for non-movement
	 * actions are reported by this class, so a listener should be attached to both.
	 *
	 * @throws IOException on I/O error
	 */
	public void move() throws IOException {
		final IUnit mover = model.getSelectedUnit();
		final int directionNum = helper.inputNumber("Direction to move: ");
		if (directionNum > 8) {
			fireMovementCost(Integer.MAX_VALUE);
		}
		final IExplorationModel.Direction direction =
				assertNotNull(IExplorationModel.Direction.values()[directionNum]);
		final Point point = model.getSelectedUnitLocation();
		final Point dPoint = model.getDestination(point, direction);
		try {
			model.move(direction);
		} catch (final SimpleMovement.TraversalImpossibleException except) {
			LOGGER.log(Level.FINEST, "Attempted movement to impassable destination",
					except);
			helper.print("That direction is impassable; we've made sure ");
			helper.println("all maps show that at a cost of 1 MP");
			return;
		}
		final Collection<TileFixture> constants = new ArrayList<>();
		final IMutableMapNG map = model.getMap();
		if (map.isMountainous(dPoint)) {
			constants.add(new Mountain());
		}
		final Ground ground = map.getGround(dPoint);
		final List<TileFixture> allFixtures = new ArrayList<>();
		if (ground != null) {
			if (SimpleMovement.shouldAlwaysNotice(mover, ground)) {
				constants.add(ground);
			} else if (SimpleMovement.shouldSometimesNotice(mover, ground)) {
				allFixtures.add(ground);
			}
		}
		final Forest forest = map.getForest(dPoint);
		if (forest != null) {
			if (SimpleMovement.shouldAlwaysNotice(mover, forest)) {
				constants.add(forest);
			} else if (SimpleMovement.shouldSometimesNotice(mover, forest)) {
				allFixtures.add(forest);
			}
		}
		for (final TileFixture fix : map.getOtherFixtures(dPoint)) {
			if (SimpleMovement.shouldAlwaysNotice(mover, fix)) {
				constants.add(fix);
			} else if (SimpleMovement.shouldSometimesNotice(mover, fix)) {
				allFixtures.add(fix);
			}
		}
		final String possibleTracks;
		if (Ocean == model.getMap().getBaseTerrain(model.getSelectedUnitLocation())) {
			possibleTracks = huntingModel.fish(model.getSelectedUnitLocation(), 1).get(0);
		} else {
			possibleTracks = huntingModel.hunt(model.getSelectedUnitLocation(), 1).get(0);
		}
		if (!HuntingModel.NOTHING.equals(possibleTracks)) {
			allFixtures
					.add(new Animal(possibleTracks, true, false, "wild", idf.createID()));
		}
		if ((IExplorationModel.Direction.Nowhere == direction) &&
					helper.inputBoolean(FEALTY_PROMPT)) {
			model.swearVillages();
			fireMovementCost(5);
		} else if ((IExplorationModel.Direction.Nowhere == direction) &&
						helper.inputBoolean("Dig to expose some ground here?")) {
			model.dig();
			fireMovementCost(4);
		}
		helper.printf("The explorer comes to %s, a tile with terrain %s%n",
				dPoint.toString(), map.getBaseTerrain(dPoint).toString());
		if (allFixtures.isEmpty()) {
			helper.println("The following fixtures were automatically noticed:");
		} else if ((allFixtures.size() > 1) &&
						(SingletonRandom.RANDOM.nextDouble() < 0.1)) {
			helper.print("The following fixtures were noticed, all but the last ");
			helper.println("two automatically:");
			Collections.shuffle(allFixtures);
			constants.add(allFixtures.get(0));
			constants.add(allFixtures.get(1));
		} else {
			helper.print("The following fixtures were noticed, all but the ");
			helper.println("last automatically:");
			Collections.shuffle(allFixtures);
			constants.add(allFixtures.get(0));
		}
		for (final TileFixture fix : constants) {
			printAndTransferFixture(dPoint, fix, mover);
		}
	}

	/**
	 * @param dPoint the current location
	 * @param fix    the fixture to copy to subordinate maps. May be null, to simplify
	 *                  the
	 *               caller.
	 * @param mover  the current unit (needed for its owner)
	 */
	private void printAndTransferFixture(final Point dPoint,
										@Nullable final TileFixture fix,
										final HasOwner mover) {
		if (fix != null) {
			helper.println(assertNotNull(fix.toString()));
			final boolean zero = (fix instanceof HasOwner) &&
										!((HasOwner) fix).getOwner()
												.equals(mover.getOwner());
			for (final Pair<IMutableMapNG, File> pair : model.getSubordinateMaps()) {
				final IMutableMapNG map = pair.first();
				if ((fix instanceof Ground) && (map.getGround(dPoint) == null)) {
					map.setGround(dPoint, ((Ground) fix).copy(false));
				} else if ((fix instanceof Forest) && (map.getForest(dPoint) == null)) {
					map.setForest(dPoint, ((Forest) fix).copy(false));
				} else if (fix instanceof Mountain) {
					map.setMountainous(dPoint, true);
				} else {
					map.addFixture(dPoint, fix.copy(zero));
				}
			}
		}
		if (fix instanceof CacheFixture) {
			model.getMap().removeFixture(dPoint, fix);
		}
	}

	/**
	 * Ask the user for directions the unit should move until it runs out of MP or the
	 * user decides to quit.
	 *
	 * @throws IOException on I/O error.
	 */
	public void moveUntilDone() throws IOException {
		final IUnit selUnit = model.getSelectedUnit();
		if (selUnit == null) {
			helper.println("No unit is selected");
		} else {
			helper.println("Details of the unit:");
			helper.println(selUnit.verbose());
			final int totalMP = helper.inputNumber("MP the unit has: ");
			final Accumulator movement = new IntHolder(totalMP);
			model.addMovementCostListener(cost -> movement.add(0 - cost));
			addMovementCostListener(cost -> movement.add(0 - cost));
			while (movement.getValue() > 0) {
				helper.printf("%d MP of %d remaining.%n%s%n",
						Integer.valueOf(movement.getValue()), Integer.valueOf(totalMP),
						PROMPT);
				move();
			}
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorationCLI";
	}
	/**
	 * The list of movement-cost listeners.
	 */
	private final Collection<MovementCostListener> mcListeners = new ArrayList<>();
	/**
	 * @param listener the listener to add
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void addMovementCostListener(final MovementCostListener listener) {
		mcListeners.add(listener);
	}

	/**
	 * @param listener the listener to remove
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void removeMovementCostListener(final MovementCostListener listener) {
		mcListeners.remove(listener);
	}
	/**
	 * Tell listeners of a movement cost.
	 *
	 * @param cost how much the move cost
	 */
	private void fireMovementCost(final int cost) {
		for (final MovementCostListener list : mcListeners) {
			list.deduct(cost);
		}
	}
}

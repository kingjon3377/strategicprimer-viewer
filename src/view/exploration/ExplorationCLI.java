package view.exploration;

import static util.NullCleaner.assertNotNull;
import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.misc.ICLIHelper;
import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.map.HasOwner;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.towns.Village;
import util.NullCleaner;
import util.Pair;
import view.util.SystemOut;

/**
 * A CLI to help running exploration. Now separated from the "driver" bits, to
 * simplify things.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ExplorationCLI {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = assertNotNull(Logger.getLogger(ExplorationCLI.class.getName()));
	/**
	 * The direction prompt.
	 */
	private static final String PROMPT = assertNotNull(new StringBuilder(90)
			.append("0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, ")
			.append("6 = W, 7 = NW, 8 = Stay Here, 9 = Quit.")
			.toString());
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
	 * @param emodel the exploration model to use
	 * @param mhelper the helper to handle user I/O
	 */
	public ExplorationCLI(final IExplorationModel emodel,
			final ICLIHelper mhelper) {
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
	@Nullable
	public Player choosePlayer() throws IOException {
		final List<Player> players = model.getPlayerChoices();
		final int playerNum = helper.chooseFromList(players,
				"The players shared by all the maps:",
				"No players shared by all the maps.",
				"Please make a selection: ", true);
		if (playerNum < 0 || playerNum >= players.size()) {
			return null;
		} else {
			return players.get(playerNum);
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
	@Nullable
	public IUnit chooseUnit(final Player player) throws IOException {
		final List<IUnit> units = model.getUnits(player);
		final int unitNum = helper.chooseFromList(units, "Player's units:",
				"That player has no units in the master map.",
				"Please make a selection: ", true);
		if (unitNum < 0 || unitNum >= units.size()) {
			return null;
		} else {
			return units.get(unitNum);
		}
	}

	/**
	 * Change the owner of all the villages on the specified tile in all the
	 * maps to the owner of the currently selected unit.
	 *
	 * @param point the location of the tile in question
	 */
	private void swearVillages(final Point point) {
		final IUnit visitor = model.getSelectedUnit();
		if (visitor != null) {
			for (final Pair<IMutableMapNG, File> mapPair : model.getAllMaps()) {
				final IMutableMapNG map = mapPair.first();
				for (final TileFixture fix : map.getOtherFixtures(point)) {
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
	public int move(final IUnit mover) throws IOException {
		final int directionNum = helper.inputNumber("Direction to move: ");
		if (directionNum > 8) {
			return Integer.MAX_VALUE; // NOPMD
		}
		final Direction direction =
				assertNotNull(Direction.values()[directionNum]);
		final Point point = model.getSelectedUnitLocation();
		final Point dPoint = model.getDestination(point, direction);
		int cost; // NOPMD
		try {
			cost = model.move(direction);
		} catch (final TraversalImpossibleException except) {
			LOGGER.log(Level.FINEST, "Attempted movement to impassable destination", except);
			System.out.print("That direction is impassable; we've made sure ");
			System.out.println("all maps show that at a cost of 1 MP");
			return 1; // NOPMD
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
			} else if (SimpleMovement.mightNotice(mover, ground)) {
				allFixtures.add(ground);
			}
		}
		final Forest forest = map.getForest(dPoint);
		if (forest != null) {
			if (SimpleMovement.shouldAlwaysNotice(mover, forest)) {
				constants.add(forest);
			} else if (SimpleMovement.mightNotice(mover, forest)) {
				allFixtures.add(forest);
			}
		}
		for (final TileFixture fix : map.getOtherFixtures(dPoint)) {
			if (SimpleMovement.shouldAlwaysNotice(mover, fix)) {
				constants.add(fix);
			} else if (SimpleMovement.mightNotice(mover, fix)) {
				allFixtures.add(fix);
			}
		}
		if (Direction.Nowhere == direction
				&& helper.inputBoolean(FEALTY_PROMPT)) {
			swearVillages(dPoint);
			cost += 5;
		}
		SYS_OUT.printf("The explorer comes to %s, a tile with terrain %s%n",
				dPoint.toString(), map.getBaseTerrain(dPoint).toString());
		if (allFixtures.isEmpty()) {
			SYS_OUT.println("The following fixtures were automatically noticed:");
		} else {
			System.out.print("The following fixtures were noticed, all but the ");
			System.out.println("last automtically:");
			Collections.shuffle(allFixtures);
			constants.add(allFixtures.get(0));
		}
		for (final TileFixture fix : constants) {
			printAndTransferFixture(dPoint, fix, mover);
		}
		return cost;
	}

	/**
	 * TODO: Remove caches from master map.
	 * @param dPoint
	 *            the current location
	 * @param fix
	 *            the fixture to copy to subordinate maps. May be null, to
	 *            simplify the caller.
	 * @param mover
	 *            the current unit (needed for its owner)
	 */
	private void printAndTransferFixture(final Point dPoint,
			@Nullable final TileFixture fix, final HasOwner mover) {
		if (fix != null) {
			SYS_OUT.println(fix);
			final boolean zero = fix instanceof HasOwner && !((HasOwner) fix)
					.getOwner().equals(mover.getOwner());
			for (final Pair<IMutableMapNG, File> pair : model.getSubordinateMaps()) {
				final IMutableMapNG map = pair.first();
				if (fix instanceof Ground && map.getGround(dPoint) == null) {
					map.setGround(dPoint, ((Ground) fix).copy(false));
				} else if (fix instanceof Forest && map.getForest(dPoint) == null) {
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
	 * Ask the user for directions the unit should move until it runs out of MP
	 * or the user decides to quit.
	 *
	 * @throws IOException on I/O error.
	 */
	public void moveUntilDone() throws IOException {
		final IUnit selUnit = model.getSelectedUnit();
		if (selUnit == null) {
			SYS_OUT.println("No unit is selected");
		} else {
			SYS_OUT.println("Details of the unit:");
			SYS_OUT.println(selUnit.verbose());
			final int totalMP = helper.inputNumber("MP the unit has: ");
			int movement = totalMP;
			while (movement > 0) {
				SYS_OUT.printf("%d MP of %d remaining.%n%s%n", Integer.valueOf(movement),
						Integer.valueOf(totalMP), PROMPT);
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

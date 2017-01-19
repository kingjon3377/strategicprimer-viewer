package model.exploration;

import java.util.List;
import model.listeners.MovementCostSource;
import model.listeners.SelectionChangeSource;
import model.map.HasName;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement;
import model.misc.IMultiMapModel;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A model for exploration drivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IExplorationModel
		extends IMultiMapModel, SelectionChangeSource, MovementCostSource {
	/**
	 * Players that are shared by all the maps.
	 * @return all the players that are shared by all the maps
	 */
	List<Player> getPlayerChoices();

	/**
	 * The given player's units in the master map.
	 * @param player a player
	 * @return all that player's units in the master map
	 */
	List<IUnit> getUnits(Player player);

	/**
	 * Move the currently selected unit from its current tile one tile in the specified
	 * direction. Moves the unit in all maps where the unit *was* in the specified tile,
	 * copying terrain information if the tile didn't exist in a subordinate map. If
	 * movement in the specified direction is impossible, we update all subordinate maps
	 * with the terrain information showing that, then re-throw the exception; callers
	 * should deduct a minimal MP cost.
	 *
	 * @param direction the direction to move
	 * @param speed how quickly or slowly the explorer is taking things
	 * @return the movement cost
	 * @throws SimpleMovement.TraversalImpossibleException if movement in that direction
	 *                                                     is impossible
	 */
	int move(Direction direction, Speed speed)
			throws SimpleMovement.TraversalImpossibleException;

	/** Given a starting point and a direction, get the next point in that direction.
	 * @param point     a point
	 * @param direction a direction
	 * @return the point bordering the specified one in the specified direction
	 */
	Point getDestination(Point point, Direction direction);

	/**
	 * Get the location of the first fixture that can be found that is "equal to" the
	 * given fixture.
	 * @param fix a fixture
	 * @return the first location found (search order is not defined) containing a
	 * fixture "equal to" the specified one. (Using it on mountains, e.g., will *not* do
	 * what you want ...)
	 */
	Point find(TileFixture fix);

	/**
	 * Get the currently selected unit, if any.
	 * @return the currently selected unit---may be null!
	 */
	@Nullable IUnit getSelectedUnit();

	/**
	 * Change the currently selected unit.
	 * @param unit the new selected unit
	 */
	void selectUnit(@Nullable IUnit unit);

	/**
	 * Get the location of the currently selected unit.
	 * @return its location. This will *not* be null.
	 */
	Point getSelectedUnitLocation();

	/**
	 * If there is a currently selected unit, make any independent villages at its
	 * location change to be owned by the owner of the currently selected unit. This
	 * costs MP.
	 */
	void swearVillages();

	/**
	 * If there is a currently selected unit, change one Ground, StoneDeposit, or
	 * MineralVein at the location of that unit from unexposed to exposed (and discover
	 * it). This costs MP.
	 */
	void dig();

	/**
	 * An enumeration of directions.
	 */
	enum Direction {
		/**
		 * North.
		 */
		North,
		/**
		 * Northeast.
		 */
		Northeast,
		/**
		 * East.
		 */
		East,
		/**
		 * Southeast.
		 */
		Southeast,
		/**
		 * South.
		 */
		South,
		/**
		 * Southwest.
		 */
		Southwest,
		/**
		 * West.
		 */
		West,
		/**
		 * Northwest.
		 */
		Northwest,
		/**
		 * Stay still.
		 */
		Nowhere
	}
	/**
	 * An enumeration of possible movement speeds, joining their effects on MP costs and
	 * Perception. Traveling to "Nowhere" should give an additional bonus (+2?) to
	 * Perception.
	 */
	enum Speed implements HasName {
		/**
		 * Traveling as quickly as possible.
		 */
		Hurried(0.66, -6),
		/**
		 * Normal speed.
		 */
		Normal(1.0, -4),
		/**
		 * Moving slowly enough to notice one's surroundings.
		 */
		Observant(1.5, -2),
		/**
		 * Looking carefully at one's surroundings to try not to miss anything important.
		 */
		Careful(2.0, 0),
		/**
		 * Painstaking searches.
		 */
		Meticulous(2.5, 2);
		/**
		 * This is applied to the normal MP cost of a movement by multiplication, and
		 * the cost is then rounded up to the nearest integer.
		 */
		private final double mpMultiplier;
		/**
		 * This is a bonus or penalty applied to the explorer's Perception, as usual by
		 * adding it.
		 */
		private final int perceptionModifier;
		/**
		 * A String description.
		 */
		private final String desc;
		/**
		 * The modifier by which to multiply movement costs.
		 * @return the multiplicative modifier to apply to movement costs
		 */
		public double getMpMultiplier() {
			return mpMultiplier;
		}
		/**
		 * The modifier to add to Perception checks.
		 * @return the additive modifier to apply to Perception checks
		 */
		public int getPerceptionModifier() {
			return perceptionModifier;
		}
		/**
		 * A description to use in menus.
		 * @return a String description for use in CLI menus.
		 */
		@Override
		public String getName() {
			return desc;
		}
		/**
		 * Constructor.
		 * @param mpMod the multiplicative modifier to movement costs
		 * @param perceptionMod the additive modifier to Perception checks
		 */
		private Speed(final double mpMod, final int perceptionMod) {
			mpMultiplier = mpMod;
			perceptionModifier = perceptionMod;
			final String perceptionString;
			if (perceptionModifier >= 0) {
				perceptionString =
						String.format("+%d", Integer.valueOf(perceptionModifier));
			} else {
				perceptionString = Integer.toString(perceptionModifier);
			}
			desc = String.format("%s: x%,.1f MP costs, %s Perception", name(),
					Double.valueOf(mpMultiplier), perceptionString);

		}
	}
}

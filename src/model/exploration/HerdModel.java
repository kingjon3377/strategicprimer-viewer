package model.exploration;

import util.Quantity;

/**
 * Models of (game statistics for) herding, including dairy cattle, small mammals,
 * large mammals, small poultry, large poultry, etc.
 *
 * TODO: Turn into interface implemented by one for mammals and one for poultry.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface HerdModel {
	/**
	 * How much is produced per head per turn.
	 * @return The amount, in some model-specified unit, produced per head per turn.
	 */
	Quantity getProductionPerHead();

	/**
	 * The coefficient to turn production into pounds.
	 * @return The coefficient by which the unit of in which production is measured must
	 * be multiplied to get pounds.
	 */
	double getPoundsCoefficient();

	/**
	 * How much time per head is spent to collect the food, in minutes.
	 * @return How much time, per head, in minutes, must be spent to milk, gather
	 * eggs, or otherwise collect the food produced by the animals. Callers may adjust
	 * this downward somewhat if the herders in question are experts.
	 */
	int getDailyTimePerHead();
}

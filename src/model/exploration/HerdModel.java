package model.exploration;

import util.Quantity;

/**
 * Models of (game statistics for) herding, including dairy cattle, small mammals,
 * large mammals, small poultry, large poultry, etc.
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
public enum HerdModel {
	/**
	 * The model for dairy cattle.
	 */
	DairyCattle(new Quantity(Integer.valueOf(4), "gallons"), 8.6, 40, 60, 0, 0),
	/**
	 * The model for other roughly-cattle-sized mammals. (Not anything as large as
	 * elephants.)
	 */
	LargeMammals(new Quantity(Integer.valueOf(3), "gallons"), 8.6, 40, 60, 0, 0),
	/**
	 * The model for roughly-goat-sized mammals.
	 */
	SmallMammals(new Quantity(Double.valueOf(1.5), "gallons"), 8.6, 30, 60, 0, 0),
	/**
	 * The model for chickens.
	 */
	Chickens(new Quantity(Double.valueOf(0.75), "eggs"), 0.125, 2, 0, 30, 2),
	/**
	 * The model for turkeys.
	 */
	Turkeys(new Quantity(Double.valueOf(0.75), "eggs"), 0.25, 2, 0, 30, 2);

	/**
	 * The amount produced per head per turn.
	 */
	private final Quantity productionPerHead;
	/**
	 * The coefficient by which the unit of in which production is measured must be
	 * multiplied to get pounds.
	 */
	private final double poundsCoefficient;
	/**
	 * How much time, per head, in minutes, must be spent to milk, gather eggs, or
	 * otherwise collect the food produced by the animals.
	 */
	private final int dailyTimePerHead;
	/**
	 * How much time, in minutes, must be spent on the entire herd or flock each turn,
	 * regardless of its size. This should be *added* to the quotient of the total time
	 * for the entire herd divided by the number of herders.
	 */
	private final int dailyTimeFloor;
	/**
	 * How much time, in minutes, must be spent per head on "extra chores" days.
	 */
	private final int extraTimePerHead;
	/**
	 * How many turns at most should elapse between "extra chores" days.
	 */
	private final int extraChoresInterval;
	/**
	 * Constructor.
	 *
	 * @param production    The amount produced per head per turn.
	 * @param coefficient   The coefficient by which the unit of in which production is
	 *                      measured must be multiplied to get pounds
	 * @param unitCost      How much time, per head, in minutes, must be spent to milk,
	 *                      gather eggs, or otherwise collect the food produced by the
	 *                      animals.
	 * @param constantCost  How much time, in minutes, must be spent on the entire
	 *                         herd or
	 *                      flock each turn, regardless of its size.
	 * @param extraCost     How much time, in minutes, must be spent per head on "extra
	 *                      chores" days.
	 * @param extraInterval How many turns at most should elapse between "extra chores"
	 *                      days.
	 */
	HerdModel(final Quantity production, final double coefficient, final int unitCost,
			  final int constantCost, final int extraCost, final int extraInterval) {
		productionPerHead = production;
		poundsCoefficient = coefficient;
		dailyTimePerHead = unitCost;
		dailyTimeFloor = constantCost;
		extraTimePerHead = extraCost;
		extraChoresInterval = extraInterval;
	}
	/**
	 * Whether this is poultry.
	 * @return true if this is poultry, false otherwise
	 */
	public boolean isPoultry() {
		return this == Chickens || this == Turkeys;
	}
	/**
	 * How much is produced per head per turn.
	 * @return The amount, in some model-specified unit, produced per head per turn.
	 */
	public Quantity getProductionPerHead() {
		return productionPerHead;
	}

	/**
	 * The coefficient to turn production into pounds.
	 * @return The coefficient by which the unit of in which production is measured must
	 * be multiplied to get pounds.
	 */
	public double getPoundsCoefficient() {
		return poundsCoefficient;
	}

	/**
	 * How much time per head is spent to collect the food, in minutes.
	 * @return How much time, per head, in minutes, must be spent to milk, gather
	 * eggs, or
	 * otherwise collect the food produced by the animals. Callers may adjust this
	 * downward somewhat if the herders in question are experts.
	 */
	public int getDailyTimePerHead() {
		return dailyTimePerHead;
	}

	/**
	 * How much time must be spent on the herd regardless of its size per turn.
	 * @return How much time, in minutes, must be spent on the entire herd or flock each
	 * turn, regardless of its size. This should be *added* to the quotient of the total
	 * time for the entire herd divided by the number of herders.
	 */
	public int getDailyTimeFloor() {
		return dailyTimeFloor;
	}

	/**
	 * How many minutes must be spent per head on "extra chores" days.
	 * @return How much time, in minutes, must be spent per head on "extra chores" days.
	 */
	public int getExtraTimePerHead() {
		return extraTimePerHead;
	}

	/**
	 * How often "extra chores" days should be.
	 * @return How many turns at most should elapse between "extra chores" days. A return
	 * value less than or equal to zero indicates that there are no extra chores that
	 * should be regularly, but not daily, scheduled.
	 */
	public int getExtraChoresInterval() {
		return extraChoresInterval;
	}
}

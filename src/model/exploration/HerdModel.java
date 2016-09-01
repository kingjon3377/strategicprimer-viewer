package model.exploration;

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
	DairyCattle(4, "gallons", 8.6, 40, 60, 0, 0),
	/**
	 * The model for other roughly-cattle-sized mammals. (Not anything as large as
	 * elephants.)
	 */
	LargeMammals(3, "gallons", 8.6, 40, 60, 0, 0),
	/**
	 * The model for roughly-goat-sized mammals.
	 */
	SmallMammals(1.5, "gallons", 8.6, 30, 60, 0, 0),
	/**
	 * The model for chickens.
	 */
	Chickens(0.75, "eggs", 0.125, 2, 0, 30, 2),
	/**
	 * The model for turkeys.
	 */
	Turkeys(0.75, "eggs", 0.25, 2, 0, 30, 2);

	/**
	 * Constructor.
	 * @param production The amount, in some model-specified unit, produced per head per
	 *                      turn.
	 * @param unit The unit in which production is measured.
	 * @param coefficient The coefficient by which the unit of in which production is
	 *                       measured must be multiplied to get pounds
	 * @param unitCost How much time, per head, in minutes, must be spent to milk,
	 *                    gather eggs, or otherwise collect the food produced by the
	 *                    animals.
	 * @param constantCost How much time, in minutes, must be spent on the entire herd
	 *                        or flock each turn, regardless of its size.
	 * @param extraCost How much time, in minutes, must be spent per head on "extra
	 *                     chores" days.
	 * @param extraInterval How many turns at most should elapse between "extra chores"
	 *                         days.
	 */
	HerdModel(final double production, final String unit, final double coefficient,
			  final int unitCost, final int constantCost, final int extraCost,
			  final int extraInterval) {
		productionPerHead = production;
		productionUnit = unit;
		poundsCoefficient = coefficient;
		dailyTimePerHead = unitCost;
		dailyTimeFloor = constantCost;
		extraTimePerHead = extraCost;
		extraChoresInterval = extraInterval;
	}
	/**
	 * The amount, in some model-specified unit, produced per head per turn.
	 */
	private final double productionPerHead;
	/**
	 * The unit in which production is measured.
	 */
	private final String productionUnit;
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
	 * @return The amount, in some model-specified unit, produced per head per turn.
	 */
	public double getProductionPerHead() {
		return productionPerHead;
	}
	/**
	 * @return The unit in which production is measured.
	 */
	public String getProductionUnit() {
		return productionUnit;
	}
	/**
	 * @return The coefficient by which the unit of in which production is measured must be
	 * multiplied to get pounds.
	 */
	public double getPoundsCoefficient() {
		return poundsCoefficient;
	}
	/**
	 * @return How much time, per head, in minutes, must be spent to milk, gather eggs, or
	 * otherwise collect the food produced by the animals. Callers may adjust this
	 * downward somewhat if the herders in question are experts.
	 */
	public int getDailyTimePerHead() {
		return dailyTimePerHead;
	}
	/**
	 * @return How much time, in minutes, must be spent on the entire herd or flock each turn,
	 * regardless of its size. This should be *added* to the quotient of the total time
	 * for the entire herd divided by the number of herders.
	 */
	public int getDailyTimeFloor() {
		return dailyTimeFloor;
	}
	/**
	 * @return How much time, in minutes, must be spent per head on "extra chores" days.
	 */
	public int getExtraTimePerHead() {
		return extraTimePerHead;
	}
	/**
	 * @return How many turns at most should elapse between "extra chores" days. A return value
	 * less than or equal to zero indicates that there are no extra chores that should
	 * be regularly, but not daily, scheduled.
	 */
	public int getExtraChoresInterval() {
		return extraChoresInterval;
	}
}

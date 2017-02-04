package model.exploration;

import util.Quantity;

/**
 * Models of (game statistics for) herding poultry.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public enum PoultryModel implements HerdModel {
	/**
	 * The model for chickens.
	 */
	Chickens(new Quantity(Double.valueOf(0.75), "eggs"), 0.125, 2, 30, 2),
	/**
	 * The model for turkeys.
	 */
	Turkeys(new Quantity(Double.valueOf(0.75), "eggs"), 0.25, 2, 30, 2),
	/**
	 * The model for pigeons.
	 */
	Pigeons(new Quantity(Double.valueOf(0.5), "eggs"), 0.035, 1, 30, 4);
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
	 * @param extraCost     How much time, in minutes, must be spent per head on "extra
	 *                      chores" days.
	 * @param extraInterval How many turns at most should elapse between "extra chores"
	 *                      days.
	 */
	PoultryModel(final Quantity production, final double coefficient, final int unitCost,
			  final int extraCost, final int extraInterval) {
		productionPerHead = production;
		poundsCoefficient = coefficient;
		dailyTimePerHead = unitCost;
		extraTimePerHead = extraCost;
		extraChoresInterval = extraInterval;
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

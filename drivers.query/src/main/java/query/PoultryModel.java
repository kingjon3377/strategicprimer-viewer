package query;

import common.map.fixtures.Quantity;

/**
 * Models of (game statistics for) herding poultry.
 */
public enum PoultryModel implements HerdModel {
	/**
	 * The model for chickens.
	 */
	Chickens(0.75, 0.125, 2, 2, "Chickens"),

	/**
	 * The model for turkeys.
	 */
	Turkeys(0.75, 0.25, 2, 2, "Turkeys"),

	/**
	 * The model for pigeons.
	 */
	Pigeons(0.5, 0.035, 1, 4, "Pigeons");

	/**
	 * The amount produced per head per turn.
	 */
	private final Quantity productionPerHead;

	/**
	 * The amount produced per head per turn.
	 */
	@Override
	public Quantity getProductionPerHead() {
		return productionPerHead;
	}

	/**
	 * The coefficient to turn production into pounds.
	 */
	private final double poundsCoefficient;

	/**
	 * The coefficient to turn production into pounds.
	 */
	@Override
	public double getPoundsCoefficient() {
		return poundsCoefficient;
	}

	/**
	 * How much time, per head, in minutes, must be spent to gather eggs.
	 */
	private final int dailyTimePerHead;

	/**
	 * How much time, per head, in minutes, must be spent to gather eggs.
	 */
	@Override
	public int getDailyTimePerHead() {
		return dailyTimePerHead;
	}

	/**
	 * How many turns, at most, should elapse between "extra chores" days.
	 */
	private final int extraChoresInterval;

	/**
	 * How many turns, at most, should elapse between "extra chores" days.
	 */
	public int getExtraChoresInterval() {
		return extraChoresInterval;
	}

	/**
	 * How much time, in minutes, must be spent per head on "extra chores" days.
	 */
	@SuppressWarnings("MagicNumber")
	public static int getExtraTimePerHead() {
		return 30;
	}

	/**
	 * A description of the model to show the user.
	 */
	private final String name;

	/**
	 * A description of the model to show the user.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param production The number of eggs produced per head per turn.
	 */
	PoultryModel(final double production, final double poundsCoefficient, final int dailyTimePerHead,
	             final int extraChoresInterval, final String nomen) {
		this.poundsCoefficient = poundsCoefficient;
		this.dailyTimePerHead = dailyTimePerHead;
		this.extraChoresInterval = extraChoresInterval;
		productionPerHead = new Quantity(production, "eggs");
		name = nomen;
	}

	/**
	 * How much time, in minutes, herders must spend on a flock with this many animals per herder.
	 */
	@Override
	public int dailyTime(final int heads) {
		return heads * dailyTimePerHead;
	}

	/**
	 * How much time, in minutes, herders must spend on a flock with this
	 * many animals per head on "extra chores" days.
	 */
	public static int dailyExtraTime(final int heads) {
		return heads * getExtraTimePerHead();
	}
}

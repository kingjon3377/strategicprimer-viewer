package query;

import common.map.fixtures.Quantity;

/**
 * Models of (game statistics for) herding mammals.
 */
public enum MammalModel implements HerdModel {
	/**
	 * The model for dairy cattle.
	 */
	DairyCattle(4.0, 40, "Dairy Cattle"),

	/**
	 * The model for other roughly-cattle-sized mammals. (Not for anything as large as elephants.)
	 */
	LargeMammals(3.0, 40, "Large Mammals"),

	/**
	 * The model for roughly-goat-sized mammals.
	 */
	SmallMammals(1.5, 30, "Medium-Size Mammals");

	/**
	 * How much time, per head, in minutes, must be spent to milk, or
	 * otherwise collect the food produced by the animals.
	 */
	private final int dailyTimePerHead;

	/**
	 * How much time, per head, in minutes, must be spent to milk, or
	 * otherwise collect the food produced by the animals.
	 */
	@Override
	public int getDailyTimePerHead() {
		return dailyTimePerHead;
	}

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
	 * The number of pounds per gallon.
	 */
	@Override
	public double getPoundsCoefficient() {
		return 8.6;
	}

	/**
	 * How much time, in minutes, must be spent on the entire herd or flock
	 * each turn, regardless of its size, in addition to each herder's time
	 * with individual animals.
	 */
	public int getDailyTimeFloor() {
		return 60;
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
	 * @param production The amount produced per head per turn, in gallons
	 */
	private MammalModel(double production, int dailyTimePerHead, String nomen) {
		this.dailyTimePerHead = dailyTimePerHead;
		productionPerHead = new Quantity(production, "gallons");
		name = nomen;
	}

	/**
	 * How much time, in minutes, herders must spend on a flock with this
	 * many animals per herder.
	 */
	@Override
	public int dailyTime(int heads) {
		return heads * dailyTimePerHead + getDailyTimeFloor();
	}

	/**
	 * How much time, in minutes, an expert herder must spend on a flock
	 * with this many animals per herder.
	 */
	public int dailyExpertTime(int heads) {
		return heads * (dailyTimePerHead - 10) + getDailyTimeFloor();
	}
}

package query;

import common.map.fixtures.Quantity;

import legacy.map.HasName;
import legacy.map.fixtures.LegacyQuantity;

/**
 * Models of (game statistics for) herding.
 *
 * TODO: investigated "sealed" possibilities in Java
 */
public /* sealed */ interface HerdModel /* of PoultryModel | MammalModel | SmallAnimalModel */
		extends HasName {
	/**
	 * How much is produced per head per turn, in some model-specified unit.
	 */
	Quantity getProductionPerHead();

	/**
	 * The coefficient to turn production into pounds.
	 */
	double getPoundsCoefficient();

	/**
	 * How much time, per head, in minutes, must be spent to milk, gather
	 * eggs, or otherwise collect the food produced by the animals. Callers
	 * may adjust this downward somewhat if the herders in question are
	 * experts.
	 */
	int getDailyTimePerHead();

	/**
	 * How much time is spent for a flock (per herder) of the given size,
	 * possibly including any time "floor".
	 */
	int dailyTime(int heads);

	/**
	 * How much is produced by a flock of the given size.
	 */
	default LegacyQuantity scaledProduction(final int heads) {
		return new LegacyQuantity(getProductionPerHead().number().doubleValue() * heads,
				getProductionPerHead().units());
	}

	/**
	 * How many pounds are produced by a flock of the given size.
	 */
	default double scaledPoundsProduction(final int heads) {
		return getProductionPerHead().number().doubleValue() * heads * getPoundsCoefficient();
	}
}

package exploration.common;

import common.map.Direction;
import common.map.HasName;

/**
 * An enumeration of possible movement speeds, joining their effects on MP
 * costs and Perception. Traveling to {@link Direction#Nowhere} should give an
 * additional bonus (+2?) to Perception.
 */
public enum Speed implements HasName {
	/**
	 * Traveling as quickly as possible.
	 */
	Hurried(0.66, -6, "Hurried"),

	/**
	 * Normal speed.
	 */
	Normal(1.0, -4, "Normal"),

	/**
	 * Moving slowly enough to notice one's surroundings.
	 */
	Observant(1.5, -2, "Observant"),

	/**
	 * Looking carefully at one's surroundings to try not to miss anything important.
	 */
	Careful(2.0, 0, "Careful"),

	/**
	 * Painstaking searches.
	 */
	Meticulous(2.5, 2, "Meticulous");

	/**
	 * The multiplicative modifier to apply to movement costs.
	 */
	private final double mpMultiplier;

	/**
	 * The multiplicative modifier to apply to movement costs.
	 */
	public double getMpMultiplier() {
		return mpMultiplier;
	}

	/**
	 * The modifier to add to Perception checks.
	 */
	private final int perceptionModifier;

	/**
	 * The modifier to add to Perception checks.
	 */
	public int getPerceptionModifier() {
		return perceptionModifier;
	}

	/**
	 * A description to use in menus.
	 */
	private final String name;

	/**
	 * A description to use in menus.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * A description to use in prose text.
	 */
	private final String shortName;

	/**
	 * A description to use in prose text.
	 */
	public String getShortName() {
		return shortName;
	}

	Speed(final double multMod, final int addMod, final String desc) {
		mpMultiplier = multMod;
		perceptionModifier = addMod;
		final String perceptionString = (addMod >= 0) ? String.format("+%d", addMod) :
			Integer.toString(addMod);
		name = String.format("%s: x%.1f MP costs, %s Perception", desc, multMod, perceptionString);
		shortName = desc;
	}

	/**
	 * A description to use in GUI menus.
	 */
	@Override
	public String toString() {
		return name;
	}

	// FIXME: Make sure we use this wherever comparisons had been implicit
	public static int comparePerception(final Speed one, final Speed two) {
		return Integer.compare(one.perceptionModifier, two.perceptionModifier);
	}
}

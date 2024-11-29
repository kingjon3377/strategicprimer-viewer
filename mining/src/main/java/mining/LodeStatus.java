package mining;

import java.util.Random;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleSupplier;

/**
 * The status of a vein of ore or deposit of stone at any given point.
 */
enum LodeStatus {
	/**
	 * There is very little ore: one part ore per 16,384 parts other rock.
	 */
	Minimal(16384, 0.4),
	/**
	 * There is quite little ore: one part ore per 4096 parts other rock.
	 */
	VeryPoor(4096),
	/**
	 * There is little ore: one part ore per 1096 parts other rock.
	 */
	Poor(1096),
	/**
	 * There is a fair amount of ore: one part ore per 256 parts other rock.
	 */
	Fair(256),
	/**
	 * There is quite a bit of ore: one part ore per 16 parts other rock.
	 */
	Good(16),
	/**
	 * There is a relatively high ratio of ore: one part ore per 4 parts other rock.
	 */
	VeryGood(4),
	/**
	 * The mother-lode: one part ore per one part other rock.
	 */
	MotherLode(1, 0.7, 1.0);

	/**
	 * Given a String, if it is the name of a {@link LodeStatus}, return
	 * that value; otherwise, return null.
	 */
	@SuppressWarnings({"DuplicateBranchesInSwitch", "SwitchStatementWithTooManyBranches"})
	public static @Nullable LodeStatus parse(final String str) {
		return switch (str.toLowerCase()) {
			case "none" -> null;
			case "minimal" -> Minimal;
			case "verypoor", "very-poor" -> VeryPoor;
			case "poor" -> Poor;
			case "fair" -> Fair;
			case "good" -> Good;
			case "verygood", "very-good" -> VeryGood;
			case "motherlode", "mother-lode" -> MotherLode;
			default -> {
				LovelaceLogger.warning("Unsupported lode status '%s'", str);
				yield null;
			}
		};
	}

	/**
	 * The number of parts of other rock per part of ore.
	 */
	private final int ratio;

	/**
	 * The number of parts of other rock per part of ore.
	 */
	public int getRatio() {
		return ratio;
	}

	/**
	 * The probability that an adjacent area will be the next state lower.
	 */
	private final double lowerProbability;

	/**
	 * The probability that an adjacent area will be the next state lower.
	 */
	public double getLowerProbability() {
		return lowerProbability;
	}

	/**
	 * The probability that an adjacent area will be either the next state
	 * lower or the same state, not the next state higher.
	 */
	private final double notHigherProbability;

	/**
	 * The probability that an adjacent area will be either the next state
	 * lower or the same state, not the next state higher.
	 */
	public double getNotHigherProbability() {
		return notHigherProbability;
	}

	@SuppressWarnings("MagicNumber")
	LodeStatus(final int qty) {
		this(qty, 0.5);
	}

	@SuppressWarnings("MagicNumber")
	LodeStatus(final int qty, final double lowerChance) {
		this(qty, lowerChance, 0.8);
	}

	LodeStatus(final int qty, final double lowerChance, final double notHigherChance) {
		ratio = qty;
		lowerProbability = lowerChance;
		notHigherProbability = notHigherChance;
	}

	/**
	 * The next lower status.
	 */
	private @Nullable LodeStatus getLower() {
		return switch (this) {
			case Minimal -> null;
			case VeryPoor -> Minimal;
			case Poor -> VeryPoor;
			case Fair -> Poor;
			case Good -> Fair;
			case VeryGood -> Good;
			case MotherLode -> VeryGood;
		};
	}

	/**
	 * The next higher status.
	 */
	private LodeStatus getHigher() {
		return switch (this) {
			case Minimal -> VeryPoor;
			case VeryPoor -> Poor;
			case Poor -> Fair;
			case Fair -> Good;
			case Good -> VeryGood;
			case VeryGood, MotherLode -> MotherLode;
		};
	}

	/**
	 * Randomly choose a status of a location adjacent to one with this status.
	 *
	 * @param rng The random-number-generating function to use
	 */
	public @Nullable LodeStatus adjacent(final DoubleSupplier rng) {
		final double rand = rng.getAsDouble();
		if (rand < lowerProbability) {
			return getLower();
		} else if (rand < notHigherProbability) {
			return this;
		} else {
			return getHigher();
		}
	}

	/**
	 * Randomly choose the status of a location horizontally adjacent in a
	 * "banded" (for example sand) mine to one with this status.
	 */
	public @Nullable LodeStatus bandedAdjacent(final Random rng) {
		return adjacent(rng::nextGaussian);
	}
}

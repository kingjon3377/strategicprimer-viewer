package mining;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleSupplier;
import java.util.Comparator;

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
	 *
	 * TODO: Throw instead of returning null?
	 *
	 * TODO: support hyphenated "very" statuses
	 */
	@Nullable
	public static LodeStatus parse(String str) {
		switch (str.toLowerCase()) {
		case "none":
			return null;
		case "minimal":
			return LodeStatus.Minimal;
		case "verypoor":
			return LodeStatus.VeryPoor;
		case "poor":
			return LodeStatus.Poor;
		case "fair":
			return LodeStatus.Fair;
		case "good":
			return LodeStatus.Good;
		case "verygood":
			return LodeStatus.VeryGood;
		case "motherlode":
			return LodeStatus.MotherLode;
		default:
			return null;
		}
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

	private LodeStatus(int qty) {
		this(qty, 0.5);
	}

	private LodeStatus(int qty, double lowerChance) {
		this(qty, lowerChance, 0.8);
	}

	private LodeStatus(int qty, double lowerChance, double notHigherChance) {
		ratio = qty;
		lowerProbability = lowerChance;
		notHigherProbability = notHigherChance;
	}

	/**
	 * The next lower status.
	 */
	@Nullable
	private LodeStatus getLower() {
		switch (this) {
		case Minimal:
			return null;
		case VeryPoor:
			return Minimal;
		case Poor:
			return VeryPoor;
		case Fair:
			return Poor;
		case Good:
			return Fair;
		case VeryGood:
			return Good;
		case MotherLode:
			return VeryGood;
		default:
			throw new IllegalStateException("Non-exhaustive switch");
		}
	}

	/**
	 * The next higher status.
	 */
	@Nullable
	private LodeStatus getHigher() {
		switch (this) {
		case Minimal:
			return VeryPoor;
		case VeryPoor:
			return Poor;
		case Poor:
			return Fair;
		case Fair:
			return Good;
		case Good:
			return VeryGood;
		case VeryGood:
			return MotherLode;
		case MotherLode:
			return MotherLode;
		default:
			throw new IllegalStateException("Non-exhaustive switch");
		}
	}

	/**
	 * Randomly choose a status of a location adjacent to one with this status.
	 *
	 * @param rng The random-number-generating function to use
	 */
	@Nullable
	public LodeStatus adjacent(DoubleSupplier rng) {
		double rand = rng.getAsDouble();
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
	@Nullable
	public LodeStatus bandedAdjacent(Random rng) {
		return adjacent(rng::nextGaussian);
	}

	// FIXME: Why not just a method that can be passed by method reference?
	public static class LodeStatusComparator implements Comparator<LodeStatus> {
		/**
		* One status is *greater* than another iff the former's {@link
		* LodeStatus#ratio} is <em>less</em> than the latter's.
		*/
		@Override
		public int compare(LodeStatus one, LodeStatus two) {
			return Integer.compare(two.getRatio(), one.getRatio());
		}
	}
}

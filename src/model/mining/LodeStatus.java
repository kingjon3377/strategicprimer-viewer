package model.mining;

import java.util.Random;

/**
 * The status of a vein of ore or deposit of stone at any given point.
 * @author Jonathan Lovelace
 */
public enum LodeStatus {
	/**
	 * The vein is not present.
	 */
	None(-1),
	/**
	 * There is very little ore: one part ore per 16,384 parts other rock.
	 */
	Minimal(16384),
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
	MotherLode(1);
	/**
	 * The number of parts of other rock per part of ore.
	 */
	private final int ratio;
	/**
	 * @param qty the number of parts of other rock per part of ore
	 */
	private LodeStatus(final int qty) {
		ratio = qty;
	}
	/**
	 * @return the number of parts of other rock per part of ore
	 */
	public int getRatio() {
		return ratio;
	}
	/**
	 * @param state the status of one location
	 * @param rng a random-number generator
	 * @return the status of an adjacent location
	 */
	public static LodeStatus adjacent(final LodeStatus state, final Random rng) {
		final double rand = rng.nextDouble();
		switch (state) {
		case None:
			return None;
		case Minimal:
			if (rand < .25) {
				return None;
			} else if (rand < .75) {
				return Minimal;
			} else {
				return VeryPoor;
			}
		case VeryPoor:
			if (rand < .1) {
				return None;
			} else if (rand < .4) {
				return Minimal;
			} else if (rand < .7) {
				return VeryPoor;
			} else {
				return Poor;
			}
		case Poor:
			if (rand < .1) {
				return None;
			} else if (rand < .4) {
				return VeryPoor;
			} else if (rand < .7) {
				return Poor;
			} else {
				return Fair;
			}
		case Fair:
			if (rand < .1) {
				return None;
			} else if (rand < .4) {
				return Poor;
			} else if (rand < .7) {
				return Fair;
			} else {
				return Good;
			}
		case Good:
			if (rand < .1) {
				return None;
			} else if (rand < .4) {
				return Fair;
			} else if (rand < .7) {
				return Good;
			} else {
				return VeryGood;
			}
		case VeryGood:
			if (rand < .1) {
				return None;
			} else if (rand < .4) {
				return Good;
			} else if (rand < .7) {
				return VeryGood;
			} else {
				return MotherLode;
			}
		case MotherLode:
			if (rand < .1) {
				return None;
			} else if (rand < .6) {
				return VeryGood;
			} else {
				return MotherLode;
			}
		default:
			throw new IllegalArgumentException("Unhandled LodeStatus");
		}
	}
}

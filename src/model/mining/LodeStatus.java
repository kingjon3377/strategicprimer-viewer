package model.mining;

import java.util.Random;

/**
 * The status of a vein of ore or deposit of stone at any given point.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
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
	Minimal(16384, 0.4, 0.8),
	/**
	 * There is quite little ore: one part ore per 4096 parts other rock.
	 */
	VeryPoor(4096, 0.5, 0.8),
	/**
	 * There is little ore: one part ore per 1096 parts other rock.
	 */
	Poor(1096, 0.5, 0.8),
	/**
	 * There is a fair amount of ore: one part ore per 256 parts other rock.
	 */
	Fair(256, 0.5, 0.8),
	/**
	 * There is quite a bit of ore: one part ore per 16 parts other rock.
	 */
	Good(16, 0.5, 0.8),
	/**
	 * There is a relatively high ratio of ore: one part ore per 4 parts other rock.
	 */
	VeryGood(4, 0.5, 0.8),
	/**
	 * The mother-lode: one part ore per one part other rock.
	 */
	MotherLode(1, 0.7, 1.0);
	/**
	 * The number of parts of other rock per part of ore.
	 */
	private final int ratio;
	/**
	 * The probability that an adjacent area will be the next state lower.
	 */
	private final double lowerProbability;
	/**
	 * The probability that an adjacent area will be either the next state lower or the
	 * same state.
	 */
	private final double notHigherProbability;
	/**
	 * @param qty the number of parts of other rock per part of ore
	 */
	LodeStatus(final int qty) {
		this(qty, 1.0, 1.0);
	}

	/**
	 * @param qty the number of parts of other rock per part of ore
	 * @param lowerChance the probability that an adjacent area will be the next state
	 *                       lower
	 * @param notHigherChance the probability that an adjacent area will be either the
	 *                           next state lower or the same state
	 */
	LodeStatus(final int qty, final double lowerChance, final double notHigherChance) {
		ratio = qty;
		lowerProbability = lowerChance;
		notHigherProbability = notHigherChance;
	}

	/**
	 * @return the number of parts of other rock per part of ore
	 */
	public int getRatio() {
		return ratio;
	}
	/**
	 * @param state a LodeStatus
	 * @return the next status lower
	 */
	private static LodeStatus lower(final LodeStatus state) {
		switch (state) {
		case None:
			return None;
		case Minimal:
			return None;
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
			throw new IllegalStateException("Unhandled case in switch");
		}
	}

	/**
	 * @param state a LodeStatus
	 * @return the next status higher
	 */
	private static LodeStatus higher(final LodeStatus state) {
		switch (state) {
		case None:
			return None;
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
			throw new IllegalStateException("Unhandled case in switch");
		}
	}
	/**
	 * @param state the status of one location
	 * @param rng   a random-number generator
	 * @return the status of an adjacent location
	 */
	public static LodeStatus adjacent(final LodeStatus state, final Random rng) {
		final double rand = rng.nextDouble();
		if (state == None) {
			return None;
		} else if (rand < state.lowerProbability) {
			return lower(state);
		} else if (rand < state.notHigherProbability) {
			return state;
		} else {
			return higher(state);
		}
	}
}

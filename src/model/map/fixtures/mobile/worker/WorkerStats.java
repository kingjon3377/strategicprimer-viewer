package model.map.fixtures.mobile.worker;

import java.util.Formatter;
import java.util.function.IntSupplier;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

/**
 * A class representing a worker's core statistical attributes.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class WorkerStats {
	/**
	 * The basis of stats: every two points more than this is worth +1, and every two
	 * points less is worth -1.
	 */
	private static final int STAT_BASIS = 10;
	/**
	 * The worker's health.
	 */
	private final int hp;
	/**
	 * The worker's max health.
	 */
	private final int maxHP;
	/**
	 * The worker's strength.
	 */
	private final int strength;
	/**
	 * The worker's dexterity.
	 */
	private final int dex;
	/**
	 * The worker's constitution.
	 */
	private final int con;
	/**
	 * The worker's intelligence.
	 */
	private final int intel;
	/**
	 * The worker's wisdom.
	 */
	private final int wis;
	/**
	 * The worker's charisma.
	 */
	private final int cha;

	/**
	 * Constructor.
	 *
	 * @param hitPoints    the worker's health
	 * @param maxHitPoints the worker's max health
	 * @param strengthStat the worker's strength
	 * @param dexterity    the worker's dexterity
	 * @param constitution the worker's constitution
	 * @param intelligence the worker's intelligence
	 * @param wisdom       the worker's wisdom
	 * @param charisma     th worker's charisma
	 */
	public WorkerStats(final int hitPoints, final int maxHitPoints,
					   final int strengthStat, final int dexterity,
					   final int constitution,
					   final int intelligence, final int wisdom, final int charisma) {
		hp = hitPoints;
		maxHP = maxHitPoints;
		strength = strengthStat;
		dex = dexterity;
		con = constitution;
		intel = intelligence;
		wis = wisdom;
		cha = charisma;
	}
	/**
	 * @param hitPoints the number to use for HP and maxHP
	 * @param base a set of base stats
	 * @param adjustment a set of adjustments to those stats
	 */
	public WorkerStats(final int hitPoints, final WorkerStats base, final WorkerStats adjustment) {
		hp = hitPoints;
		maxHP = hitPoints;
		strength = base.strength + adjustment.strength;
		dex = base.dex + adjustment.dex;
		con = base.con + adjustment.con;
		intel = base.intel + adjustment.intel;
		wis = base.wis + adjustment.wis;
		cha = base.cha + adjustment.cha;
	}
	/**
	 * @param stat a stat
	 * @return a String representing the modifier it conveys.
	 */
	public static String getModifierString(final int stat) {
		final int modifier = getModifier(stat);
		final String modStr = NullCleaner.assertNotNull(Integer.toString(modifier));
		if (modifier >= 0) {
			return '+' + modStr;
		} else {
			return modStr;
		}
	}

	/**
	 * @param stat a stat
	 * @return the modifier it conveys
	 */
	public static int getModifier(final int stat) {
		// Officially it's "( stat - STAT_BASIS) / 2", but "*always round down*"
		return stat / 2 - STAT_BASIS / 2;
	}

	/**
	 * @return a copy of this
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	public WorkerStats copy() {
		return new WorkerStats(hp, maxHP, strength, dex, con, intel, wis, cha);
	}

	/**
	 * @return the worker's health
	 */
	public int getHitPoints() {
		return hp;
	}

	/**
	 * @return the worker's max health
	 */
	public int getMaxHitPoints() {
		return maxHP;
	}

	/**
	 * @return the worker's strength
	 */
	public int getStrength() {
		return strength;
	}

	/**
	 * @return the worker's dexterity
	 */
	public int getDexterity() {
		return dex;
	}

	/**
	 * @return the worker's constitution
	 */
	public int getConstitution() {
		return con;
	}

	/**
	 * @return the worker's intelligence
	 */
	public int getIntelligence() {
		return intel;
	}

	/**
	 * @return the worker's wisdom
	 */
	public int getWisdom() {
		return wis;
	}

	/**
	 * @return the worker's charisma
	 */
	public int getCharisma() {
		return cha;
	}

	/**
	 * @param obj another object
	 * @return whether it's a WorkerStats equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (obj == this) ||
					   ((obj instanceof WorkerStats) && equalsImpl((WorkerStats) obj));
	}

	/**
	 * @param obj another stats object
	 * @return whether it's equal to this.
	 */
	private boolean equalsImpl(final WorkerStats obj) {
		return (hp == obj.hp) && (maxHP == obj.maxHP) && (strength == obj.strength) &&
					   (dex == obj.dex) && (con == obj.con) && (intel == obj.intel) &&
					   (wis == obj.wis) && (cha == obj.cha);
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return strength + (dex << 3) + (con << 6) + (intel << 9) + (wis << 12) +
					   (cha << 15) + (hp << 18) + (maxHP << 22);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("StringBufferReplaceableByString")
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(70);
        try (final Formatter formatter = new Formatter(builder)) {
			formatter.format("HP: %d / %d%nStr: %d%nDex: %d%n", hp, maxHP, strength, dex);
			formatter.format("Con: %d%nInt: %d%nWis: %d%nCha: %d%n", con, intel, wis, cha);
        }
		return NullCleaner.assertNotNull(builder.toString());
	}
	/**
	 * A factory method to produce a set of stats for use as bonuses to be applied to yet
	 * another set. This is so that you don't have to specify HP if it's not applicable.
	 * @param strengthStat the worker's strength (bonus)
	 * @param dexterity    the worker's dexterity (bonus)
	 * @param constitution the worker's constitution (bonus)
	 * @param intelligence the worker's intelligence (bonus)
	 * @param wisdom       the worker's wisdom (bonus)
	 * @param charisma     th worker's charisma (bonus)
	 * @return the constructed object
	 */
	public static WorkerStats factory(final int strengthStat, final int dexterity,
									  final int constitution, final int intelligence,
									  final int wisdom, final int charisma) {
		return new WorkerStats(0, 0, strengthStat, dexterity, constitution, intelligence,
									  wisdom, charisma);
	}
	/**
	 * A factory method taking a RNG.
	 * @param rng a method of "rolling 3d6"
	 * @return a WorkerStats with 0 HP and a random number for each stat.
	 */
	public static WorkerStats factory(final IntSupplier rng) {
		return factory(rng.getAsInt(), rng.getAsInt(), rng.getAsInt(), rng.getAsInt(),
				rng.getAsInt(), rng.getAsInt());
	}
	/**
	 * @return the stats as an array
	 */
	public int[] toArray() {
		return new int[] { strength, dex, con, intel, wis, cha };
	}
}

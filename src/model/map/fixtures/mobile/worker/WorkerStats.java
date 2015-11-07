package model.map.fixtures.mobile.worker;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A class representing a worker's core statistical attributes.
 *
 * TODO: Should this really be mutable?
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerStats {
	/**
	 * The basis of stats: every two points more than this is worth +1, and
	 * every two points less is worth -1.
	 */
	private static final int STAT_BASIS = 10;
	/**
	 * The worker's health.
	 */
	private int hp; // NOPMD
	/**
	 * The worker's max health.
	 */
	private int maxHP;
	/**
	 * The worker's strength.
	 */
	private int str;
	/**
	 * The worker's dexterity.
	 */
	private int dex;
	/**
	 * The worker's constitution.
	 */
	private int con;
	/**
	 * The worker's intelligence.
	 */
	private int intel;
	/**
	 * The worker's wisdom.
	 */
	private int wis;
	/**
	 * The worker's charisma.
	 */
	private int cha;

	// ESCA-JAVA0138:
	/**
	 * Constructor.
	 *
	 * @param hitPoints the worker's health
	 * @param maxHitPoints the worker's max health
	 * @param strength the worker's strength
	 * @param dexterity the worker's dexterity
	 * @param constitution the worker's constitution
	 * @param intelligence the worker's intelligence
	 * @param wisdom the worker's wisdom
	 * @param charisma th worker's charisma
	 */
	public WorkerStats(final int hitPoints, final int maxHitPoints,
			final int strength, final int dexterity, final int constitution,
			final int intelligence, final int wisdom, final int charisma) {
		hp = hitPoints;
		maxHP = maxHitPoints;
		str = strength;
		dex = dexterity;
		con = constitution;
		intel = intelligence;
		wis = wisdom;
		cha = charisma;
	}
	/**
	 * @return a copy of this
	 * @param zero ignored, as all our information is "essential"
	 */
	public WorkerStats copy(final boolean zero) {
		return new WorkerStats(hp, maxHP, str, dex, con, intel, wis, cha);
	}
	/**
	 * @return the worker's health
	 */
	public int getHitPoints() {
		return hp;
	}

	/**
	 * @param hitPoints the worker's health
	 */
	public void setHitPoints(final int hitPoints) {
		hp = hitPoints;
	}

	/**
	 * @return the worker's max health
	 */
	public int getMaxHitPoints() {
		return maxHP;
	}

	/**
	 * @param maxHitPoints the worker's max health
	 */
	public void setMaxHitPoints(final int maxHitPoints) {
		maxHP = maxHitPoints;
	}

	/**
	 * @return the worker's strength
	 */
	public int getStrength() {
		return str;
	}

	/**
	 * @param strength the worker's strength
	 */
	public void setStrength(final int strength) {
		str = strength;
	}

	/**
	 * @return the worker's dexterity
	 */
	public int getDexterity() {
		return dex;
	}

	/**
	 * @param dexterity the worker's dexterity
	 */
	public void setDexterity(final int dexterity) {
		dex = dexterity;
	}

	/**
	 * @return the worker's constitution
	 */
	public int getConstitution() {
		return con;
	}

	/**
	 * @param constitution the worker's constitution
	 */
	public void setConstitution(final int constitution) {
		con = constitution;
	}

	/**
	 * @return the worker's intelligence
	 */
	public int getIntelligence() {
		return intel;
	}

	/**
	 * @param intelligence the worker's intelligence
	 */
	public void setIntelligence(final int intelligence) {
		intel = intelligence;
	}

	/**
	 * @return the worker's wisdom
	 */
	public int getWisdom() {
		return wis;
	}

	/**
	 * @param wisdom the worker's wisdom
	 */
	public void setWisdom(final int wisdom) {
		wis = wisdom;
	}

	/**
	 * @return the worker's charisma
	 */
	public int getCharisma() {
		return cha;
	}

	/**
	 * @param charisma the worker's charisma
	 */
	public void setCharisma(final int charisma) {
		cha = charisma;
	}

	/**
	 * @param obj another object
	 * @return whether it's a WorkerStats equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return obj == this || obj instanceof WorkerStats
				&& equalsImpl((WorkerStats) obj);
	}
	/**
	 * @param obj another stats object
	 * @return whether it's equal to this.
	 */
	private boolean equalsImpl(final WorkerStats obj) {
		return hp == obj.hp && maxHP == obj.maxHP && str == obj.str
				&& dex == obj.dex && con == obj.con && intel == obj.intel
				&& wis == obj.wis && cha == obj.cha;
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return (str + (dex << 3) + (con << 6) + (intel << 9))
				+ ((wis << 12) + (cha << 15) + (hp << 18) + (maxHP << 22));
	}

	/**
	 * @param stat a stat
	 * @return a String representing the modifier it conveys.
	 */
	public static String getModifierString(final int stat) {
		final int modifier = (stat - STAT_BASIS) / 2;
		final String modStr = Integer.toString(modifier);
		assert modStr != null;
		if (modifier >= 0) {
			return '+' + modStr; // NOPMD
		} else {
			return modStr;
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(70);
		builder.append("HP: ");
		builder.append(hp);
		builder.append(" / ");
		builder.append(maxHP);
		builder.append("\nStr: ");
		builder.append(str);
		builder.append("\nDex: ");
		builder.append(dex);
		builder.append("\nCon: ");
		builder.append(con);
		builder.append("\nInt: ");
		builder.append(intel);
		builder.append("\nWis: ");
		builder.append(wis);
		builder.append("\nCha: ");
		builder.append(cha);
		builder.append('\n');
		return NullCleaner.assertNotNull(builder.toString());
	}
}

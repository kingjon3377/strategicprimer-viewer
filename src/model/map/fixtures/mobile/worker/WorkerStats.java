package model.map.fixtures.mobile.worker;

import java.io.Serializable;

import model.map.XMLWritable;

/**
 * A class representing a worker's core statistical attributes.
 *
 * TODO: Should this really be mutable?
 * @author Jonathan Lovelace
 *
 */
public class WorkerStats implements Serializable, XMLWritable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 * @param strength the worker's strength
	 * @param dexterity the worker's dexterity
	 * @param constitution the worker's constitution
	 * @param intelligence the worker's intelligence
	 * @param wisdom the worker's wisdom
	 * @param charisma th worker's charisma
	 */
	public WorkerStats(final int strength, final int dexterity,
			final int constitution, final int intelligence, final int wisdom,
			final int charisma) {
		str = strength;
		dex = dexterity;
		con = constitution;
		intel = intelligence;
		wis = wisdom;
		cha = charisma;
	}
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
	public boolean equals(final Object obj) {
		return obj == this
				|| (obj instanceof WorkerStats
						&& str == ((WorkerStats) obj).str
						&& dex == ((WorkerStats) obj).dex
						&& con == ((WorkerStats) obj).con
						&& intel == ((WorkerStats) obj).intel
						&& wis == ((WorkerStats) obj).wis && cha == ((WorkerStats) obj).cha);
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return str + dex << 4 + con << 8 + intel << 12 + wis << 16 + cha << 20;
	}
}

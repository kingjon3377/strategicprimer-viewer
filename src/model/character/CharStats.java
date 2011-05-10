package model.character;

import java.io.Serializable;

/**
 * An interface to represent a character's stats. One implementing class will
 * know the actual values, while the other will just know the summaries.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface CharStats extends Serializable {

	/**
	 * Possible values, as visible to players, of an attribute.
	 */
	enum Attribute {
		ExtremelyLow, VeryLow, QuiteLow, RatherLow, SomewhatLow, Average, SomewhatHigh, RatherHigh, QuiteHigh, VeryHigh
	}

	/**
	 * The stats.
	 */
	enum Stat {
		Strength, Dexterity, Constitution, Intelligence, Wisdom, Charisma
	}

	/**
	 * @param stat
	 *            a stat
	 * @return an approximate value for that stat
	 */
	Attribute getStat(final Stat stat);

	/**
	 * @param stat
	 *            a stat
	 * @param value
	 *            a new approximate value for that stat
	 */
	void setStat(final Stat stat, final Attribute value);
}

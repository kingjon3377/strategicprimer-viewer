package model.character;


/**
 * An interface to represent a character's stats. One implementing class will
 * know the actual values, while the other will just know the summaries.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface CharStats {

	/**
	 * Possible values, as visible to players, of an attribute.
	 */
	enum Attribute {
		/**
		 * The lowest possible values.
		 */
		ExtremelyLow,
		/**
		 * Better than "extremely low," but still very low.
		 */
		VeryLow,
		/**
		 * Better than "very low," but still quite low.
		 */
		QuiteLow,
		/**
		 * Better than "quite low," but still rather low.
		 */
		RatherLow,
		/**
		 * Getting better, but still somewhat low.
		 */
		SomewhatLow,
		/**
		 * About average: neither low nor high.
		 */
		Average,
		/**
		 * Somewhat high.
		 */
		SomewhatHigh,
		/**
		 * Higher than "somewhat high": rather high.
		 */
		RatherHigh,
		/**
		 * Even higher: quite high.
		 */
		QuiteHigh,
		/**
		 * The highest we can model: very high.
		 */
		VeryHigh;
	}

	/**
	 * The stats.
	 */
	enum Stat {
		/**
		 * Physical strength.
		 */
		Strength,
		/**
		 * Flexibility and fine control.
		 */
		Dexterity,
		/**
		 * Constitution.
		 */
		Constitution,
		/**
		 * Intelligence: factual knowledge, logic, etc.
		 */
		Intelligence,
		/**
		 * Wisdom: reasoning ability, common sense, etc.
		 */
		Wisdom,
		/**
		 * Charisma.
		 */
		Charisma;
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

package model.character;

import java.util.EnumMap;

/**
 * An implementation of CharStats that knows about the actual stats.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class AdminStatsImpl implements CharStats { // NOPMD
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 3610744454711188925L;
	/**
	 * A map containing the stats.
	 */
	private final EnumMap<Stat, Integer> stats = new EnumMap<Stat, Integer>(
			Stat.class);

	/**
	 * Constructor.
	 */
	public AdminStatsImpl() {
		for (final Stat stat : Stat.values()) {
			// ESCA-JAVA0076:
			stats.put(stat, 10);
		}
	}

	/**
	 * @param stat
	 *            a stat
	 * @return that stat
	 */
	public int getStatValue(final Stat stat) {
		return stats.get(stat);
	}

	/**
	 * @param stat
	 *            a stat
	 * @return a summary of that stat
	 */
	@Override
	public Attribute getStat(final Stat stat) {
		return convertStat(stats.get(stat));
	}

	/**
	 * @param value
	 *            a stat value
	 * @return a summary of it
	 */
	public static Attribute convertStat(final int value) { // NOPMD
		if (value < 2) {
			return Attribute.ExtremelyLow; // NOPMD
		}
		switch (value) { // NOPMD
		case 2:
		case 3:
			return Attribute.VeryLow; // NOPMD
		case 4:
		case 5:
			return Attribute.QuiteLow; // NOPMD
		case 6:
		case 7:
			return Attribute.RatherLow; // NOPMD
		case 8:
		case 9:
			return Attribute.SomewhatLow; // NOPMD
			// ESCA-JAVA0076:
		case 10:
		case 11:
			return Attribute.Average; // NOPMD
		case 12:
		case 13:
			return Attribute.SomewhatHigh; // NOPMD
		case 14:
		case 15:
			return Attribute.RatherHigh; // NOPMD
		case 16:
		case 17:
			return Attribute.QuiteHigh; // NOPMD
		default:
			return Attribute.VeryHigh;
		}
	}

	/**
	 * @param stat
	 *            a stat
	 * @param value
	 *            the new summary value of that stat.
	 */
	@Override
	public void setStat(final Stat stat, final Attribute value) {
		stats.put(stat, value.ordinal() * 2);
	}

	/**
	 * @param stat
	 *            a stat
	 * @param value
	 *            the new exact value of that stat.
	 */
	public void setStat(final Stat stat, final int value) {
		stats.put(stat, value);
	}

}

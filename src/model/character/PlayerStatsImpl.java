package model.character;

import java.util.EnumMap;
import java.util.Map;

/**
 * An implementation of CharStats for players, who shouldn't know the precise
 * value of their workers' stats.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class PlayerStatsImpl implements CharStats {
	/**
	 * A map containing the stats.
	 */
	private final Map<Stat, Attribute> stats = new EnumMap<Stat, Attribute>(
			Stat.class);

	/**
	 * @param stat
	 *            a stat
	 * @return a summary of that stat
	 */
	@Override
	public Attribute getStat(final Stat stat) {
		return stats.get(stat);
	}

	/**
	 * @param stat
	 *            a stat
	 * @param value
	 *            the new summary value of that stat.
	 */
	@Override
	public void setStat(final Stat stat, final Attribute value) {
		stats.put(stat, value);
	}

	/**
	 * Constructor.
	 */
	public PlayerStatsImpl() {
		for (final Stat stat : Stat.values()) {
			stats.put(stat, Attribute.Average);
		}
	}
}

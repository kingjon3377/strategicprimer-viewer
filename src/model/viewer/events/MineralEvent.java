package model.viewer.events;

import java.util.HashMap;
import java.util.Map;

/**
 * A vein of a mineral.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class MineralEvent extends AbstractEvent {
	/**
	 * Constructor.
	 * 
	 * @param minkind
	 *            what kind of mineral this is
	 * @param exp
	 *            whether the vein is exposed
	 * @param discdc
	 *            the dc to discover the vein
	 */
	public MineralEvent(final MineralKind minkind, final boolean exp,
			final int discdc) {
		super(EventKind.Mineral);
		mineral = minkind;
		exposed = exp;
		dc = discdc;
	}

	/**
	 * Kinds of minerals whose events the program knows about.
	 */
	public enum MineralKind {
		/**
		 * Iron.
		 */
		Iron("iron"),
		/**
		 * Copper.
		 */
		Copper("copper"),
		/**
		 * Gold.
		 */
		Gold("gold"),
		/**
		 * Silver.
		 */
		Silver("silver"),
		/**
		 * Coal.
		 */
		Coal("coal");
		/**
		 * A mapping from string to MineralKind.
		 */
		private static final Map<String, MineralKind> M_MAP = new HashMap<String, MineralKind>();
		/**
		 * A string representing the MineralKind.
		 */
		private final String str;
		/**
		 * Constructor.
		 * @param string A string representing the MineralKind.
		 */
		private MineralKind(final String string) {
			str = string;
		}
		static {
			for (MineralKind mk : values()) {
				M_MAP.put(mk.str, mk);
			}
		}
		/**
		 * @param string a string representing a MineralKind
		 * @return the MineralKind it represents
		 */
		public static MineralKind parseMineralKind(final String string) {
			return M_MAP.get(string);
		}
		/**
		 * @return a string representation of the mineral
		 */
		@Override
		public String toString() {
			return str;
		}
	}

	/**
	 * What kind of mineral this is.
	 */
	public final MineralKind mineral;
	/**
	 * Whether the vein is exposed. TODO: Perhaps this should be mutable and
	 * protected by accessor methods?
	 */
	public final boolean exposed;
	/**
	 * The DC to discover the vein. TODO: Should perhaps be mutable.
	 */
	public final int dc; // NOPMD

	/**
	 * 
	 * @return the DC to discover the event.
	 * 
	 * @see model.viewer.events.AbstractEvent#getDC()
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final StringBuilder build = new StringBuilder("There is a");
		if (exposed) {
			build.append("n exposed");
		} 
		build.append(" vein of ");
		build.append(mineral.toString());
		build.append(" here");
		if (exposed) {
			build.append('.');
		} else {
			build.append(", but it's not exposed.");
		}
		return build.toString();
	}

}

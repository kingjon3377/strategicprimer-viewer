package model.viewer.events;

import java.util.HashMap;
import java.util.Map;

import model.viewer.TileFixture;

/**
 * An "event" --- something, other than a fortress, unit, or river, that can be
 * on a tile. Intended to replace legacy/numeric "events."
 * 
 * @author Jonathan Lovelace
 * 
 */
public abstract class AbstractEvent implements TileFixture {
	/**
	 * What kind of event this is.
	 */
	private final EventKind kind;
	/**
	 * @return what kind of event this is
	 */
	public EventKind kind() {
		return kind;
	}
	/**
	 * Sizes of towns, fortifications, and cities.
	 */
	public enum TownSize {
		/**
		 * Small.
		 */
		Small("small"),
		/**
		 * Medium.
		 */
		Medium("medium"),
		/**
		 * Large.
		 */
		Large("large");
		/**
		 * A mapping from string to TownSize.
		 */
		private static final Map<String, TownSize> TSZ_MAP = new HashMap<String, TownSize>();
		/**
		 * A string representing the size.
		 */
		private final String sizeStr;
		/**
		 * Constructor.
		 * @param str a string representing the size.
		 */
		private TownSize(final String str) {
			sizeStr = str;
		}
		static {
			for (TownSize tsz : values()) {
				TSZ_MAP.put(tsz.sizeStr, tsz);
			}
		}
		/**
		 * @param string a string representing a TownSize
		 * @return the TownSize it represents
		 */
		public static TownSize parseTownSize(final String string) {
			return TSZ_MAP.get(string);
		}
		/**
		 * @return a string representation of the size
		 */
		@Override
		public String toString() {
			return sizeStr;
		}
	}

	/**
	 * This is just a superclass. Actual events must be a subclass.
	 * 
	 * @param eKind
	 *            What kind of event this is.
	 */
	protected AbstractEvent(final EventKind eKind) {
		kind = eKind;
	}

	/**
	 * @return the DC to discover the event.
	 */
	public abstract int getDC();
	
	/**
	 * @return exploration-result text for the event.
	 */
	public abstract String getText();
}

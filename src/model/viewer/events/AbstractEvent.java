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
	 * The kinds of events we know about.
	 */
	public enum EventKind {
		/**
		 * "Nothing of interest here ...". Useful primarily as an alternative to null.
		 */
		Nothing("nothing"),
		/**
		 * A mineral vein.
		 */
		Mineral("mineral"),
		/**
		 * A fortification.
		 */
		Fortification("fortification"),
		/**
		 * A town.
		 */
		Town("town"),
		/**
		 * Signs of a long-ago battle.
		 */
		Battlefield("battlefield"),
		/**
		 * A city.
		 */
		City("city"),
		/**
		 * A stone deposit.
		 */
		Stone("stone"),
		/**
		 * Caves beneath the tile.
		 */
		Caves("cave");
		/**
		 * A mapping from strings to EventKinds.
		 */
		private static final Map<String, EventKind> EK_MAP = new HashMap<String, EventKind>();
		/**
		 * A string representing the event.
		 */
		private final String str;
		/**
		 * Constructor.
		 * @param string a string representing the event, to be used in non-specific event tags.
		 */
		private EventKind(final String string) {
			str = string;
		}
		static {
			for (EventKind ek : values()) {
				EK_MAP.put(ek.str, ek);
			}
		}
		/**
		 * @param string a string representing an EventKind
		 * @return the EventKind it represents
		 */
		public static EventKind parseEventKind(final String string) {
			return EK_MAP.get(string);
		}
		/**
		 * @return a string representing the kind of event.
		 */
		@Override
		public String toString() {
			return str;
		}
	}

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
	 * Possible status of towns, fortifications, and cities.
	 */
	public enum TownStatus {
		/**
		 * The town is inhabited.
		 */
		Active("active"),
		/**
		 * The town is abandoned.
		 */
		Abandoned("abandoned"),
		/**
		 * The town is burned-out.
		 */
		Burned("burned"),
		/**
		 * The town is in ruins.
		 */
		Ruined("ruined");
		/**
		 * A mapping from strings to TownStatus.
		 */
		private static final Map<String, TownStatus> TST_MAP = new HashMap<String, TownStatus>();
		/**
		 * A string representing the TownStatus.
		 */
		private final String str;
		/**
		 * Constructor.
		 * @param string a string representing the status.
		 */
		private TownStatus(final String string) {
			str = string;
		}
		static {
			for (TownStatus ts : values()) {
				TST_MAP.put(ts.str, ts);
			}
		}
		/**
		 * @param string a string representing a TownStatus
		 * @return the TownStatus it represents
		 */
		public static TownStatus parseTownStatus(final String string) {
			return TST_MAP.get(string);
		}
		/**
		 * @return a string representation of the status
		 */
		@Override
		public String toString() {
			return str;
		}
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

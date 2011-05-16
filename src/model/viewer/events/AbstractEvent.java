package model.viewer.events;

import java.util.HashMap;
import java.util.Map;

import model.viewer.Tile;
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
	public final EventKind kind;

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
		 * @param string
		 * @return
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
		private final String tsz_str;
		/**
		 * Constructor.
		 * @param str a string representing the size.
		 */
		private TownSize(final String str) {
			tsz_str = str;
		}
		static {
			for (TownSize tsz : values()) {
				TSZ_MAP.put(tsz.tsz_str, tsz);
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
			return tsz_str;
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

	/**
	 * The location of the event.
	 */
	private Tile location;
	/**
	 * @return the location of the event
	 */
	@Override
	public Tile getLocation() {
		return location;
	}
	/**
	 * @param tile the new location of the event 
	 */
	public void setLocation(final Tile tile) {
		location = tile;
	}
	/**
	 * An abstract superclass for towns etc.
	 * @author Jonathan Lovelace
	 */
	// ESCA-JAVA0011:
	protected abstract static class AbstractTownEvent extends AbstractEvent {
		/**
		 * Constructor.
		 * 
		 * @param eKind
		 *            what kind of event this is
		 * @param tSize
		 *            the size of the town, fortress, or city
		 * @param tStatus
		 *            the status of the town, fortress, or city
		 */
		protected AbstractTownEvent(final EventKind eKind,
				final TownStatus tStatus, final TownSize tSize) {
			super(eKind);
			status = tStatus;
			size = tSize;
			if (status == null || size == null) {
				throw new IllegalArgumentException("An enumerated argument was somehow null!");
			}
		}

		/**
		 * The status of the town, fortress, or city.
		 */
		public final TownStatus status;
		/**
		 * The size of the town, fortress, or city.
		 */
		public final TownSize size;
		/**
		 * FIXME: What about towns we've already rolled up?
		 * @return exploration-result text for the event.
		 */
		@Override
		public String getText() {
			final StringBuilder builder = new StringBuilder("There is a ");
			builder.append(TownSize.Medium.equals(size) ? "medium-size" : size.toString());
			if (!TownStatus.Active.equals(status)) {
				builder.append(' ');
				builder.append(TownStatus.Burned.equals(status) ? "burned-out" : status.toString());
			}
			builder.append(' ');
			builder.append(kind.toString());
			builder.append(" here");
			builder.append(TownStatus.Active.equals(status) ? " (roll it up)" : '.');
			return builder.toString();
		}
	}
}

package model.viewer.events;

import java.util.HashMap;
import java.util.Map;

/**
 * A deposit (always exposed for now) of stone.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class StoneEvent extends AbstractEvent {
	/**
	 * Constructor.
	 * 
	 * @param skind
	 *            the kind of stone
	 * @param discdc
	 *            the dc to discover the stone.
	 */
	public StoneEvent(final StoneKind skind, final int discdc) {
		super(EventKind.Stone);
		stone = skind;
		dc = discdc;
	}

	/**
	 * The kinds of stone we know about (for purposes of this event).
	 */
	public enum StoneKind {
		/**
		 * Limestone.
		 */
		Limestone("limestone"),
		/**
		 * Marble.
		 */
		Marble("marble");
		/**
		 * A string representing the StoneKind.
		 */
		private final String str;
		/**
		 * A mapping from string representation to StoneKind.
		 */
		private static final Map<String, StoneKind> SK_MAP = new HashMap<String, StoneKind>();
		/**
		 * @param string a string representing a StoneKind
		 * @return the StoneKind it represents
		 */
		public static StoneKind parseStoneKind(final String string) {
			return SK_MAP.get(string);
		}
		static {
			for (StoneKind sk : values()) {
				SK_MAP.put(sk.str, sk);
			}
		}
		/**
		 * Constructor.
		 * @param string A string representing the StoneKind.
		 */
		private StoneKind(final String string) {
			str = string;
		}
		/**
		 * @return a string representation of the kind of stone
		 */
		@Override
		public String toString() {
			return str;
		}
	}

	/**
	 * What kind of stone this deposit is.
	 */
	private final StoneKind stone;
	/**
	 * @return what kind of stone this deposit is.
	 */
	public StoneKind stone() {
		return stone;
	}
	/**
	 * The DC to discover the stone. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD
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
		final StringBuilder build = new StringBuilder("There is an exposed ");
		build.append(stone.toString());
		build.append(" deposit here.");
		return build.toString();
	}
	/**
	 * @param obj an object
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof StoneEvent
				&& ((StoneEvent) obj).stone.equals(stone)
				&& ((StoneEvent) obj).dc == dc);
	}
	/**
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return stone.hashCode() + dc << 3;
	}
	/**
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "A " + stone.toString() + " deposit, of DC " + dc;
	}
}

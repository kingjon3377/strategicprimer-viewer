package model.viewer.events;

/**
 * An "event" --- something, other than a fortress, unit, or river, that can be
 * on a tile. Intended to replace legacy/numeric "events."
 * 
 * @author Jonathan Lovelace
 * 
 */
public abstract class AbstractEvent {
	/**
	 * The kinds of events we know about.
	 */
	public enum EventKind {
		Nothing, Mineral, Fortification, Town, Battlefield, City, Stone, Caves;
	}

	/**
	 * What kind of event this is.
	 */
	public final EventKind kind;

	/**
	 * Possible status of towns, fortifications, and cities
	 */
	public enum TownStatus {
		Active, Abandoned, Burned, Ruined;
	}

	/**
	 * Sizes of towns, fortifications, and cities
	 */
	public enum TownSize {
		Small, Medium, Large;
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
		}

		/**
		 * The status of the town, fortress, or city
		 */
		public final TownStatus status;
		/**
		 * The size of the town, fortress, or city
		 */
		public final TownSize size;
	}
}

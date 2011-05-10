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
		/**
		 * "Nothing of interest here ...". Useful primarily as an alternative to null.
		 */
		Nothing,
		/**
		 * A mineral vein.
		 */
		Mineral,
		/**
		 * A fortification.
		 */
		Fortification,
		/**
		 * A town.
		 */
		Town,
		/**
		 * Signs of a long-ago battle.
		 */
		Battlefield,
		/**
		 * A city.
		 */
		City,
		/**
		 * A stone deposit.
		 */
		Stone,
		/**
		 * Caves beneath the tile.
		 */
		Caves;
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
		Active,
		/**
		 * The town is abandoned.
		 */
		Abandoned,
		/**
		 * The town is burned-out.
		 */
		Burned,
		/**
		 * The town is in ruins.
		 */
		Ruined;
	}

	/**
	 * Sizes of towns, fortifications, and cities.
	 */
	public enum TownSize {
		/**
		 * Small.
		 */
		Small,
		/**
		 * Medium.
		 */
		Medium,
		/**
		 * Large.
		 */
		Large;
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
		 * The status of the town, fortress, or city.
		 */
		public final TownStatus status;
		/**
		 * The size of the town, fortress, or city.
		 */
		public final TownSize size;
	}
}

package model.viewer.events;

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
		Iron,
		/**
		 * Copper.
		 */
		Copper,
		/**
		 * Gold.
		 */
		Gold,
		/**
		 * Silver.
		 */
		Silver,
		/**
		 * Coal.
		 */
		Coal;
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

}

package model.map.events;


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
	 * What kind of mineral this is.
	 */
	private final MineralKind mineral;
	/**
	 * @return what kind of mineral this is
	 */
	public MineralKind mineral() {
		return mineral;
	}
	/**
	 * Whether the vein is exposed. TODO: Perhaps this should be mutable and
	 * protected by accessor methods?
	 */
	private final boolean exposed;
	/**
	 * @return whether the vein is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}
	/**
	 * The DC to discover the vein. TODO: Should perhaps be mutable.
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
	/**
	 * @param obj an object
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof MineralEvent
				&& ((MineralEvent) obj).mineral.equals(mineral)
				&& ((MineralEvent) obj).exposed == exposed
				&& ((MineralEvent) obj).dc == dc);
	}
	/**
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return mineral.hashCode() + Boolean.valueOf(exposed).hashCode() << 3 + dc << 5;
	}
	/**
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "A " + mineral.toString() + " deposit, "
				+ (exposed ? "exposed" : "not exposed") + ", DC " + dc;
	}
}

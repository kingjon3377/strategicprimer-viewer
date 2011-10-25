package model.map.events;


/**
 * An abandoned, ruined, or burned-out fortification.
 * 
 * FIXME: We want this to share a tag, and thus probably model code as well,
 * with Fortress.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class FortificationEvent extends AbstractTownEvent {
	/**
	 * Constructor.
	 * 
	 * @param tStatus
	 *            The status of the fortification
	 * @param tSize
	 *            The size of the fortification
	 * @param discdc
	 *            The DC to discover it.
	 */
	public FortificationEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc) {
		super(EventKind.Fortification, tStatus, tSize);
		dc = discdc;
	}

	/**
	 * The DC to discover the fortification. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD

	/**
	 * 
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}
	/**
	 * @return an XML representation of the event.
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<fortification status=\"")
				.append(status().toString()).append("\" size=\"")
				.append(size().toString()).append("\" dc=\"").append(dc)
				.append("\" />").toString();
	}

}

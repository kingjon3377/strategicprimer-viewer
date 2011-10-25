package model.map.events;

/**
 * An abstract superclass for towns etc.
 * 
 * @author Jonathan Lovelace
 */
// ESCA-JAVA0011:
public abstract class AbstractTownEvent implements IEvent {
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
		super();
		kind = eKind;
		status = tStatus;
		size = tSize;
	}

	/**
	 * The status of the town, fortress, or city.
	 */
	private final TownStatus status;

	/**
	 * 
	 * @return the status of the town, fortress, or city
	 */
	public TownStatus status() {
		return status;
	}

	/**
	 * The size of the town, fortress, or city.
	 */
	private final TownSize size;
	/**
	 * What kind of event this is.
	 */
	private final EventKind kind;

	/**
	 * 
	 * @return the size of the town, fortress, or city
	 */
	public TownSize size() {
		return size;
	}

	/**
	 * FIXME: What about towns we've already rolled up?
	 * 
	 * 
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final StringBuilder builder = new StringBuilder("There is a ");
		builder.append(TownSize.Medium.equals(size) ? "medium-size" : size
				.toString());
		if (!TownStatus.Active.equals(status)) {
			builder.append(' ');
			builder.append(TownStatus.Burned.equals(status) ? "burned-out"
					: status.toString());
		}
		builder.append(' ');
		builder.append(kind().toString());
		builder.append(" here");
		builder.append(TownStatus.Active.equals(status) ? " (roll it up)" : '.');
		return builder.toString();
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof AbstractTownEvent
						&& ((AbstractTownEvent) obj).kind().equals(kind())
						&& ((AbstractTownEvent) obj).size.equals(size)
						&& ((AbstractTownEvent) obj).status.equals(status) && ((AbstractTownEvent) obj)
						.getDC() == getDC());
	}

	/**
	 * 
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return kind().hashCode() + size.hashCode() << 4 + status.hashCode() << 8 + getDC() << 12;
	}

	/**
	 * 
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "A " + size.toString() + status.toString() + kind().toString()
				+ " of DC " + getDC();
	}

	/**
	 * 
	 * @return what kind of event this is
	 */
	public EventKind kind() {
		return kind;
	}
}

package model.map.events;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * An abstract superclass for towns etc.
 * 
 * @author Jonathan Lovelace
 */
// ESCA-JAVA0011:
public abstract class AbstractTownEvent implements IEvent, HasImage {
	/**
	 * Constructor.
	 * 
	 * @param eKind
	 *            what kind of event this is
	 * @param tSize
	 *            the size of the town, fortress, or city
	 * @param tStatus
	 *            the status of the town, fortress, or city
	 * @param tName
	 *            the name of the town, fortress, or city
	 */
	protected AbstractTownEvent(final EventKind eKind,
			final TownStatus tStatus, final TownSize tSize, final String tName) {
		super();
		kind = eKind;
		status = tStatus;
		size = tSize;
		name = tName;
	}
	/**
	 * The name of this town, fortress, or city.
	 */
	private final String name;
	/**
	 * @return the name of the town, fortress, or city.
	 */
	public String name() {
		return name;
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
		if (!"".equals(name)) {
			builder.append(", ");
			builder.append(name);
			builder.append(',');
		}
		builder.append(" here");
		if (TownStatus.Active.equals(status) && "".equals(name)) {
			builder.append(" (roll it up)");
		} else {
			builder.append('.');
		}
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
						&& ((AbstractTownEvent) obj).name.equals(name)
						&& ((AbstractTownEvent) obj).status.equals(status));
	}

	/**
	 * 
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return kind().ordinal() + size.ordinal() << 4 + status.ordinal() << 8 + name.hashCode() << 16;
	}

	/**
	 * 
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "A " + size.toString() + status.toString() + kind().toString()
				+ " of DC " + getDC() + ("".equals(name) ? " with no name" : " with name " + name);
	}

	/**
	 * 
	 * @return what kind of event this is
	 */
	public EventKind kind() {
		return kind;
	}
	/**
	 * TODO: Should be more granular.
	 * @return the name of an image to represent the event.
	 */
	@Override
	public String getImage() {
		return "town.png";
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() { // NOPMD: It claims this is "empty" and should be abstract instead.
		return 50;
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}
}

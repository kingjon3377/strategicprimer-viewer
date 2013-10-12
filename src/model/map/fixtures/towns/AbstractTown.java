package model.map.fixtures.towns;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasImage;
import model.map.IEvent;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;

/**
 * An abstract superclass for towns etc.
 *
 * @author Jonathan Lovelace
 */
// ESCA-JAVA0011:
public abstract class AbstractTown implements
		IEvent, HasImage, ITownFixture {
	/**
	 * Constructor.
	 *
	 * @param eKind what kind of event this is
	 * @param tSize the size of the town, fortress, or city
	 * @param tStatus the status of the town, fortress, or city
	 * @param tName the name of the town, fortress, or city
	 * @param player the owner of the town, fortress, or city
	 */
	protected AbstractTown(final TownKind eKind, final TownStatus tStatus,
			final TownSize tSize, final String tName, final Player player) {
		super();
		kind = eKind;
		status = tStatus;
		size = tSize;
		name = tName;
		owner = player;
	}
	/**
	 * The owner of this town, fortress, or city.
	 */
	private Player owner;
	/**
	 * The name of this town, fortress, or city.
	 */
	private String name;

	/**
	 * @return the name of the town, fortress, or city.
	 */
	@Override
	public String getName() {
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
	@Override
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
	private final TownKind kind;

	/**
	 *
	 * @return the size of the town, fortress, or city
	 */
	@Override
	public TownSize size() {
		return size;
	}

	/**
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final StringBuilder builder = new StringBuilder(56)
				.append("There is a ");
		builder.append(TownSize.Medium.equals(size) ? "medium-size" : size
				.toString());
		if (!TownStatus.Active.equals(status)) {
			builder.append(' ');
			builder.append(TownStatus.Burned.equals(status) ? "burned-out"
					: status.toString());
		}
		builder.append(' ');
		builder.append(kind().toString());
		if (!name.isEmpty()) {
			builder.append(", ");
			builder.append(name);
			builder.append(',');
		}
		builder.append(" here.");
		return builder.toString();
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof AbstractTown
						&& ((AbstractTown) obj).kind().equals(kind())
						&& ((AbstractTown) obj).size.equals(size)
						&& ((AbstractTown) obj).name.equals(name)
						&& ((AbstractTown) obj).status.equals(status)
						&& ((AbstractTown) obj).owner.equals(owner) && ((TileFixture) obj)
						.getID() == getID());
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return this == fix
				|| (fix instanceof AbstractTown && equalsContents((AbstractTown) fix));
	}
	/**
	 * @param fix a town-event
	 * @return whether it's equal to this one ignoring ID.
	 */
	private boolean equalsContents(final AbstractTown fix) {
		return fix.kind().equals(kind) && fix.size().equals(size)
				&& fix.getName().equals(name) && fix.status().equals(status) && fix.owner.equals(owner);
	}
	/**
	 *
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return getID();
	}

	/**
	 *
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		final String middle = size.toString() + ' ' + status.toString() + ' '
				+ kind().toString() + " of DC " + getDC()
				+ (name.isEmpty() ? " with no name" : " with name " + name);
		return owner.isIndependent() ? "An independent " + middle : "A "
				+ middle + ", owned by "
				+ (owner.isCurrent() ? "you" : owner.getName());
	}

	/**
	 *
	 * @return what kind of event this is
	 */
	public TownKind kind() {
		return kind;
	}

	/**
	 * TODO: Should be more granular.
	 *
	 * @return the name of an image to represent the event.
	 */
	@Override
	public String getDefaultImage() {
		return "town.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() { // NOPMD: It claims this is "empty" and should be
								// abstract instead.
		return 50;
	}

	/**
	 * @param fix A TileFixture to compare to
	 *
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}
	/**
	 * @return the player that owns the town
	 */
	@Override
	public final Player getOwner() {
		return owner;
	}
	/**
	 * @param player the town's new owner
	 */
	@Override
	public final void setOwner(final Player player) {
		owner = player;
	}
	/**
	 * @param nomen the town's new name
	 */
	@Override
	public final void setName(final String nomen) {
		name = nomen;
	}
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}
	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}
}

package model.map.fixtures.towns;

import model.map.HasImage;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;

/**
 * A village on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Village implements ITownFixture, HasImage {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The status of the village.
	 */
	private final TownStatus status;
	/**
	 * The name of the village.
	 */
	private String name;

	/**
	 * Constructor.
	 *
	 * @param vstatus the status of the village.
	 * @param vName the name of the village
	 * @param idNum the ID number.
	 * @param player the owner of the village
	 * @param vRace the dominant race of the village
	 */
	public Village(final TownStatus vstatus, final String vName,
			final int idNum, final Player player, final String vRace) {
		super();
		status = vstatus;
		name = vName;
		id = idNum;
		owner = player;
		race = vRace;
	}
	/**
	 * The "owner" of the village---the player it's pledged to serve.
	 */
	private Player owner;
	/**
	 * @return a String representation of the village
	 */
	@Override
	public String toString() {
		final String middle = status.toString() + " village"
				+ (name.isEmpty() ? name : " named " + name);
		return owner.isIndependent() ? "Independent " + middle : middle
				+ ", owned by " + (owner.isCurrent() ? "you" : owner.getName());
	}

	/**
	 * @return the name of an image to represent the village
	 */
	@Override
	public String getDefaultImage() {
		return "village.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 45;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Village
						&& status.equals(((Village) obj).status)
						&& name.equals(((Village) obj).name) && id == ((TileFixture) obj)
						.getID()) && owner.equals(((Village) obj).owner)
				&& race.equals(((Village) obj).race);
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
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
	 * ID number.
	 */
	private final int id; // NOPMD

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 *
	 * @param fix a fixture
	 * @return whether it's an identical-but-for-ID village.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Village && status.equals(((Village) fix).status)
				&& name.equals(((Village) fix).name)
				&& owner.equals(((Village) fix).owner)
				&& race.equals(((Village) fix).race);
	}
	/**
	 * @return the name of the village
	 */
	@Override
	public String getName() {
		return name;
	}
	/**
	 * @return the status of the village
	 */
	@Override
	public TownStatus status() {
		return status;
	}
	/**
	 * All villages are small.
	 * @return the size of the village.
	 */
	@Override
	public TownSize size() {
		return TownSize.Small;
	}
	/**
	 * @return the "owner" of the village---the player it's pledged to serve and support
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
	/**
	 * The dominant race of the village.
	 */
	private String race;
	/**
	 * @return the dominant race of the village.
	 */
	public String getRace() {
		return race;
	}
	/**
	 * @param vRace the new dominant race of the village.
	 */
	public void setRace(final String vRace) {
		race = vRace;
	}
	/**
	 * @return a phrase describing all villages as a class.
	 */
	@Override
	public String plural() {
		return "Villages";
	}
}

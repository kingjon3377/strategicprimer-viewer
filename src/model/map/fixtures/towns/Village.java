package model.map.fixtures.towns;

import java.io.IOException;

import model.map.HasImage;
import model.map.IFixture;
import model.map.Player;
import model.map.SubsettableFixture;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A village on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Village implements ITownFixture, HasImage, SubsettableFixture {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * The dominant race of the village.
	 */
	private String race;

	/**
	 * The "owner" of the village---the player it's pledged to serve.
	 */
	private Player owner;

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
	 * @return a String representation of the village
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(52 + name.length()
				+ owner.getName().length());
		if (owner.isIndependent()) {
			builder.append("Independent ");
		}
		builder.append(status);
		builder.append(" village");
		if (!name.isEmpty()) {
			builder.append(" named ");
			builder.append(name);
		}
		if (owner.isCurrent()) {
			builder.append(", owned by you");
		} else if (!owner.isIndependent()) {
			builder.append(", owned by ");
			builder.append(owner.getName());
		}
		return NullCleaner.assertNotNull(builder.toString());
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
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Village
				&& status.equals(((Village) obj).status)
				&& name.equals(((Village) obj).name)
				&& id == ((Village) obj).id
				&& owner.equals(((Village) obj).owner)
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
	 *
	 * @return the size of the village.
	 */
	@Override
	public TownSize size() {
		return TownSize.Small;
	}

	/**
	 * @return the "owner" of the village---the player it's pledged to serve and
	 *         support
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
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}

	/**
	 * A village is a "subset" of another if they are identical, or if the only
	 * difference is that the "subset" is independent and the "superset" owes
	 * allegiance to some player.
	 *
	 * @param obj
	 *            a fixture
	 * @param ostream
	 *            a stream to write explanation to
	 * @return whether the fixture is a "subset" of this
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream)
			throws IOException {
		if (obj instanceof Village) {
			final Village village = (Village) obj;
			if (village.id != id) {
				ostream.append("IDs differ\n");
				return false;
			} else if (!status.equals(village.status)) {
				ostream.append("Village status differs\n");
				return false;
			} else if (!name.equals(village.name)) {
				ostream.append("Village names differ\n");
				return false;
			} else if (!race.equals(village.race)) {
				ostream.append("Dominant race differs\n");
				return false;
			} else if (owner.getPlayerId() == village.owner.getPlayerId()) {
				return true;
			} else {
				return village.owner.isIndependent();
			}
		} else {
			ostream.append("Incompatible types\n");
			return false;
		}
	}
}

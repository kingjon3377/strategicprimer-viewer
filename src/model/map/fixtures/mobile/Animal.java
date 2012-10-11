package model.map.fixtures.mobile;

import model.map.HasImage;
import model.map.HasKind;
import model.map.TileFixture;
import model.map.XMLWritableImpl;
import model.map.fixtures.UnitMember;

/**
 * An animal or group of animals. TODO: Add more features (population, to start
 * with).
 *
 * @author Jonathan Lovelace
 *
 */
public class Animal extends XMLWritableImpl implements MobileFixture, HasImage, HasKind, UnitMember {
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
	 * Whether this is really the animal, or only traces.
	 */
	private final boolean traces;
	/**
	 * Kind of animal.
	 */
	private final String kind;
	/**
	 * Whether this is a talking animal.
	 */
	private final boolean talking;
	/**
	 * The domestication status of the animal. TODO: Should this be an
	 * enumerated type?
	 */
	private final String status;
	/**
	 * Constructor.
	 *
	 * @param animal what kind of animal
	 * @param tracks whether this is really the animal, or only tracks
	 * @param talks whether this is a talking animal.
	 * @param dStatus domestication status
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Animal(final String animal, final boolean tracks,
			final boolean talks, final String dStatus, final int idNum, final String fileName) {
		super(fileName);
		kind = animal;
		traces = tracks;
		talking = talks;
		status = dStatus;
		id = idNum;
	}

	/**
	 * @return true if this is only traces or tracks, false if this is really
	 *         the animal
	 */
	public boolean isTraces() {
		return traces;
	}

	/**
	 * @return whether the animal is a talking animal
	 */
	public boolean isTalking() {
		return talking;
	}

	/**
	 * @return what kind of animal this is
	 */
	@Override
	public String getKind() {
		return kind;
	}
	/**
	 * @return the domestication status of the animal
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return a String representation of the animal
	 */
	@Override
	public String toString() {
		return (isTraces() ? "traces of " : "") + status
				+ (isTalking() ? " talking " : " ") + getKind();
	}

	/**
	 * TODO: Should depend on the kind of animal.
	 *
	 * @return the name of an image to represent the animal
	 */
	@Override
	public String getImage() {
		return "animal.png";
	}

	/**
	 * TODO: Should depend on the kind of animal ...
	 *
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 40;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof Animal && ((Animal) obj).kind.equals(kind)
				&& ((Animal) obj).traces == traces
				&& ((Animal) obj).talking == talking
				&& ((Animal) obj).status.equals(status)
				&& ((TileFixture) obj).getID() == id);
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
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final TileFixture fix) {
		return fix instanceof Animal && ((Animal) fix).kind.equals(kind)
				&& ((Animal) fix).traces == traces
				&& ((Animal) fix).status.equals(status)
				&& ((Animal) fix).talking == talking;
	}
}

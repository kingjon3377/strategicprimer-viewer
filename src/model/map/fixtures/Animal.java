package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * An animal or group of animals. TODO: Add more features (population, to start with).
 * @author Jonathan Lovelace
 *
 */
public class Animal implements TileFixture, HasImage {
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
	 * Constructor.
	 * @param animal what kind of animal
	 * @param tracks whether this is really the animal, or only tracks
	 * @param talks whether this is a talking animal.
	 */
	public Animal(final String animal, final boolean tracks, final boolean talks) {
		kind = animal;
		traces = tracks;
		talking = talks;
	}
	/**
	 * @return true if this is only traces or tracks, false if this is really the animal
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
	public String getAnimal() {
		return kind;
	}
	/**
	 * @return an XML representation of the Fixture.
	 */
	@Override
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<animal kind=\"");
		sbuild.append(kind);
		if (isTraces()) {
			sbuild.append("\" traces=\"");
		}
		if (isTalking()) {
			sbuild.append("\" talking=\"true");
		}
		sbuild.append("\" />");
		return sbuild.toString();
	}
	/**
	 * @return a String representation of the animal
	 */
	@Override
	public String toString() {
		return (isTraces() ? "traces of " : "") + (isTalking() ? "talking " : "") + getAnimal();
	}
	/**
	 * TODO: Should depend on the kind of animal.
	 * @return the name of an image to represent the animal
	 */
	@Override
	public String getImage() {
		return "animal.png";
	}
	/**
	 * TODO: Should depend on the kind of animal ...
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
		return obj instanceof Animal && ((Animal) obj).kind.equals(kind)
				&& ((Animal) obj).traces == traces && ((Animal) obj).talking == talking;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return kind.hashCode() << (traces ? 1 : 0) << (talking ? 2 : 0);
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.getZValue() - getZValue();
	}
}

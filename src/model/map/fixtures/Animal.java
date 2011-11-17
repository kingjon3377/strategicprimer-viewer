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
	 * Constructor.
	 * @param animal what kind of animal
	 * @param tracks whether this is really the animal, or only tracks
	 */
	public Animal(final String animal, final boolean tracks) {
		kind = animal;
		traces = tracks;
	}
	/**
	 * @return true if this is only traces or tracks, false if this is really the animal
	 */
	public boolean isTraces() {
		return traces;
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
			sbuild.append("traces=\"");
		}
		sbuild.append("\" />");
		return sbuild.toString();
	}
	/**
	 * @return a String representation of the animal
	 */
	@Override
	public String toString() {
		return (isTraces() ? "traces of " : "") + getAnimal();
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
				&& ((Animal) obj).traces == traces;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return kind.hashCode() << (traces ? 1 : 0);
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return Integer.valueOf(getZValue()).compareTo(fix.getZValue());
	}
}

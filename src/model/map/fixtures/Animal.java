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
	 * Kind of animal.
	 */
	private final String kind;
	/**
	 * Constructor.
	 * @param animal what kind of animal
	 */
	public Animal(final String animal) {
		kind = animal;
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
		return new StringBuilder("<animal kind=\"").append(kind)
				.append("\" />").toString();
	}
	/**
	 * @return a String representation of the animal
	 */
	@Override
	public String toString() {
		return getAnimal();
	}
	/**
	 * TODO: Should depend on the kind of animal.
	 * @return the name of an image to represent the animal
	 */
	@Override
	public String getImage() {
		return "animal.png";
	}
}

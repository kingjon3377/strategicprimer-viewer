package model.map.fixtures;

import model.map.HasImage;
import model.map.IFixture;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A Fixture to encapsulate arbitrary text associated with a tile, so we can
 * improve the interface, have more than one set of text per tile, and be clear
 * on <em>which turn</em> encounters happened.
 *
 * @author Jonathan Lovelace
 *
 */
public class TextFixture implements TileFixture, HasImage {
	/**
	 * The text.
	 */
	private String text;
	/**
	 * The turn it's associated with.
	 */
	private final int turn;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param theText the text
	 * @param turnNum the turn number it's associated with
	 */
	public TextFixture(final String theText, final int turnNum) {
		text = theText;
		turn = turnNum;
	}

	/**
	 * @return a String representation of the fixture
	 */
	@Override
	public String toString() {
		if (turn == -1) {
			return text; // NOPMD
		} else {
			return text + "(turn " + turn + ')';
		}
	}

	/**
	 * @param newText the new text for the fixture
	 */
	public void setText(final String newText) {
		text = newText;
	}

	/**
	 * @return the turn this is associated with
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * @return the name of an image to represent the fixture
	 */
	@Override
	public String getDefaultImage() {
		return "text.png";
	}

	/**
	 * @return a z-value for use in ordering tile icons on a tile
	 */
	@Override
	public int getZValue() {
		return 25;
	}

	/**
	 * @return the text this fixture encapsulates
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof TextFixture
				&& equalsImpl((TextFixture) obj);
	}
	/**
	 * @param obj a text-fixture
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final TextFixture obj) {
		return text.equals(obj.text) && turn == obj.turn;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return text.hashCode() << turn;
	}

	/**
	 * @param fix A TileFixture to compare to
	 *
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(@Nullable final TileFixture fix) {
		if (fix == null) {
			throw new IllegalArgumentException("Compared to null fixture");
		}
		return fix.hashCode() - hashCode();
	}

	/**
	 * TextFixtures deliberately don't have a UID---unlike Forests, Mountains,
	 * or Ground, which lack one because there are so many in the world map.
	 *
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return -1;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return equals(fix);
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
	 * @return a string describing all text fixtures as a class
	 */
	@Override
	public String plural() {
		return "Arbitrary-text notes";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}

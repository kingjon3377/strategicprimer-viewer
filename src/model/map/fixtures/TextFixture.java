package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

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
		return (turn == -1 ? text : text + "(turn " + turn + ')');
	}

	/**
	 * @param newText the new text for the fixture
	 */
	public void setText(final String newText) {
		text = newText;
	}

	/**
	 * The text.
	 */
	private String text;
	/**
	 * The turn it's associated with.
	 */
	private final int turn;

	/**
	 * @return the turn this is associated with
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * @return an XML representation of the fixture
	 */
	@Override
	@Deprecated
	public String toXML() {
		return (turn == -1 ? "<text>" : "<text turn=\"" + turn + "\">")
				+ text.trim() + "</text>";
	}

	/**
	 * @return the name of an image to represent the fixture
	 */
	@Override
	public String getImage() {
		return "text.png";
	}

	/**
	 * @return a z-value for use in ordering tile icons on a tile
	 */
	@Override
	public int getZValue() {
		return 0;
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
	public boolean equals(final Object obj) {
		return obj instanceof TextFixture
				&& text.equals(((TextFixture) obj).text)
				&& turn == ((TextFixture) obj).turn;
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
	public int compareTo(final TileFixture fix) {
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
	public boolean equalsIgnoringID(final TileFixture fix) {
		return equals(fix);
	}

	/**
	 * @return The name of the file this is to be written to.
	 */
	@Override
	public String getFile() {
		return file;
	}

	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		file = fileName;
	}

	/**
	 * The name of the file this is to be written to.
	 */
	private String file;

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		final TextFixture retval = new TextFixture(getText(), getTurn());
		retval.setFile(getFile());
		return retval;
	}
}

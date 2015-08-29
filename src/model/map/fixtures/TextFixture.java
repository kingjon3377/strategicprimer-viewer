package model.map.fixtures;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasImage;
import model.map.IFixture;
import model.map.TileFixture;

/**
 * A Fixture to encapsulate arbitrary text associated with a tile, so we can
 * improve the interface, have more than one set of text per tile, and be clear
 * on <em>which turn</em> encounters happened.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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
	 * @return a copy of this fixture
	 * @param zero ignored, as a text fixture without its sensitive information is meaningless
	 */
	@Override
	public TextFixture copy(final boolean zero) {
		TextFixture retval = new TextFixture(text, turn);
		retval.setImage(image);
		return retval;
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

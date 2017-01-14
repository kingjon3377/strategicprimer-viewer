package model.map.fixtures;

import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A Fixture to encapsulate arbitrary text associated with a tile, so we can improve the
 * interface, have more than one set of text per tile, and be clear on <em>which turn</em>
 * encounters happened.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class TextFixture implements TileFixture, HasMutableImage {
	/**
	 * The turn it's associated with.
	 */
	private final int turn;
	/**
	 * The text.
	 */
	private String text;
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
	 * Clone the object.
	 * @param zero ignored, as a text fixture without its sensitive information is
	 *             meaningless
	 * @return a copy of this fixture
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public TextFixture copy(final boolean zero) {
		final TextFixture retval = new TextFixture(text, turn);
		retval.image = image;
		return retval;
	}

	/**
	 * A String representation of the fixture: its text, plus a note of the turn it was
	 * created.
	 * @return a String representation of the fixture
	 */
	@Override
	public String toString() {
		if (turn == -1) {
			return text;
		} else {
			return text + "(turn " + turn + ')';
		}
	}

	/**
	 * The turn the note is associated with.
	 * @return the turn this is associated with
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the fixture
	 */
	@Override
	public String getDefaultImage() {
		return "text.png";
	}

	/**
	 * The text of the note.
	 * @return the text this fixture encapsulates
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set new text for the note. TODO: is this really necessary?
	 * @param newText the new text for the fixture
	 */
	public void setText(final String newText) {
		text = newText;
	}

	/**
	 * An object is equal iff it is a TextFixture from the same turn with the same text.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof TextFixture) && equalsImpl((TextFixture) obj));
	}

	/**
	 * A TextFixture is equal iff it has the same text and the same turn.
	 * @param obj a text-fixture
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final TextFixture obj) {
		return text.equals(obj.text) && (turn == obj.turn);
	}

	/**
	 * A hash value for the object, since we don't have an ID.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return text.hashCode() << turn;
	}

	/**
	 * TextFixtures deliberately don't have a UID---unlike Forests, Mountains, or Ground,
	 * which lack one because there are so many in the world map.
	 *
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return -1;
	}

	/**
	 * Since text fixtures don't have an ID, equality calculations already "ignore" ID,
	 * so we delegate to equals() here.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return equals(fix);
	}

	/**
	 * The per-instance icon filename.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the per-instance icon filename.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of "Arbitrary-text note" is "Arbitrary-text notes".
	 * @return a string describing all text fixtures as a class
	 */
	@Override
	public String plural() {
		return "Arbitrary-text notes";
	}

	/**
	 * Delegates to toString(). TODO: reverse that
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 5;
	}
}

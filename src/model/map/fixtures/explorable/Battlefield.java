package model.map.fixtures.explorable;

import model.map.IEvent;
import model.map.IFixture;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * "There are the signs of a long-ago battle here".
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class Battlefield implements IEvent, ExplorableFixture {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * A (U)ID.
	 */
	private final int id; // NOPMD

	/**
	 * Constructor.
	 *
	 * @param discdc the DC to discover the battlefield.
	 * @param idNum  the ID number for the event.
	 */
	public Battlefield(final int discdc, final int idNum) {
		dc = discdc;
		id = idNum;
	}

	/**
	 * @param zero whether to zero out the DC
	 * @return a copy of this battlefield
	 */
	@Override
	public Battlefield copy(final boolean zero) {
		final Battlefield retval;
		if (zero) {
			retval = new Battlefield(0, id);
		} else {
			retval = new Battlefield(dc, id);
		}
		retval.setImage(image);
		return retval;
	}

	/**
	 * The DC to discover the battlefield. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD

	/**
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		return "There are the signs of a long-ago battle here.";
	}

	/**
	 * @param obj an object
	 * @return whether it's an identical BattlefieldEvent.
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Battlefield)
				                         && (((TileFixture) obj).getID() == id));
	}

	/**
	 * @return a hash value for the event.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "An ancient battlefield with DC " + dc;
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 40;
	}

	/**
	 * @param fix A TileFixture to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}

	/**
	 * @return the ID number for the event.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Battlefield;
	}

	/**
	 * @return the name of the image to represent the event
	 */
	@Override
	public String getDefaultImage() {
		return "battlefield.png";
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
	 * @return a string describing all battlefields as a class
	 */
	@Override
	public String plural() {
		return "Battlefields";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "signs of a long-ago battle";
	}

}

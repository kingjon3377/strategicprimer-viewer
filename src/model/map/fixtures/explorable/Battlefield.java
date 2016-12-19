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
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class Battlefield implements IEvent, ExplorableFixture {
	/**
	 * A (U)ID.
	 */
	private final int id;
	/**
	 * The DC to discover the battlefield.
	 */
	private final int dc;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param discoverDC the DC to discover the battlefield.
	 * @param idNum      the ID number for the event.
	 */
	public Battlefield(final int discoverDC, final int idNum) {
		dc = discoverDC;
		id = idNum;
	}

	/**
	 * Clone a Battlefield.
	 * @param zero whether to zero out the DC
	 * @return a copy of this battlefield
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Battlefield copy(final boolean zero) {
		final Battlefield retval;
		if (zero) {
			retval = new Battlefield(0, id);
		} else {
			retval = new Battlefield(dc, id);
		}
		retval.image = image;
		return retval;
	}

	/**
	 * The Perception check result required to find the Battlefield.
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * "There are the signs of a long-ago battle here.".
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		return "There are the signs of a long-ago battle here.";
	}

	/**
	 * Test an object for equality with us.
	 * @param obj an object
	 * @return whether it's an identical BattlefieldEvent.
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Battlefield)
										 && (((TileFixture) obj).getID() == id));
	}

	/**
	 * Our ID number is our hash value.
	 * @return a hash value for the event.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * The DC is the only state represented in toString().
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "An ancient battlefield with DC " + dc;
	}

	/**
	 * The ID number for the battlefield.
	 * @return the ID number for the event.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Test for equality with us ignoring ID and DC, which is all of our per-instance
	 * state.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Battlefield;
	}

	/**
	 * The default icon filename.
	 * @return the name of the image to represent the event
	 */
	@Override
	public String getDefaultImage() {
		return "battlefield.png";
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
	 * The plural of Battlefield is Battlefields.
	 * @return a string describing all battlefields as a class
	 */
	@Override
	public String plural() {
		return "Battlefields";
	}

	/**
	 * "signs of a long-ago battle" is the short description of all Battlefields.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "signs of a long-ago battle";
	}

}

package model.map.fixtures.explorable;

import model.map.IEvent;
import model.map.IFixture;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * "There are extensive caves beneath this tile".
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
public final class Cave implements IEvent, ExplorableFixture {
	/**
	 * The DC to discover the caves.
	 */
	private final int dc;
	/**
	 * The event's ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param discoverDC the DC to discover the caves
	 * @param idNum      the ID number for the event.
	 */
	public Cave(final int discoverDC, final int idNum) {
		dc = discoverDC;
		id = idNum;
	}

	/**
	 * Clone the Cave.
	 * @param zero whether to zero out the DC
	 * @return a copy of this cave
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Cave copy(final boolean zero) {
		final Cave retval;
		if (zero) {
			retval = new Cave(0, id);
		} else {
			retval = new Cave(dc, id);
		}
		retval.image = image;
		return retval;
	}

	/**
	 * The required Perception check result to discover the caves.
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * "There are extensive caves beneath this tile.".
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		return "There are extensive caves beneath this tile.";
	}

	/**
	 * Test for equality.
	 * @param obj an object
	 * @return whether it's an identical CaveEvent.
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Cave)
										 && (((TileFixture) obj).getID() == id));
	}

	/**
	 * We use the ID number for the hash value.
	 * @return a hash value for the event. Constant, as our only state is DC, and that's
	 * zeroed in players' maps.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * DC is the only state represented in toString().
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "Caves with DC " + dc;
	}

	/**
	 * The Cave's ID number.
	 * @return the event's ID number.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Test for equality ignoring ID and DC, which are our only mutable state.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("InstanceofInterfaces")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Cave;
	}

	/**
	 * Image from OpenGameArt.org, by user MrBeast, from
	 * <a href="http://opengameart.org/content/cave-tileset-0">this set</a>.
	 * .
	 * @return the name of the image representing a cave
	 */
	@Override
	public String getDefaultImage() {
		return "cave.png";
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
	 * The plural of Cave is Caves.
	 * @return a string describing all caves as a class
	 */
	@Override
	public String plural() {
		return "Caves";
	}

	/**
	 * "caves underground" is the short description.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "caves underground";
	}
}

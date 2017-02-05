package model.map.fixtures.resources;

import model.map.IEvent;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.fixtures.MineralFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A deposit (always exposed for now) of stone.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class StoneDeposit implements IEvent, HarvestableFixture, MineralFixture {
	/**
	 * The DC to discover the stone. TODO: reasonable defaults
	 */
	private final int dc;
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * What kind of stone this deposit is.
	 */
	private final StoneKind stone;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param kind       the kind of stone
	 * @param discoverDC the dc to discover the stone.
	 * @param idNum      the ID number.
	 */
	public StoneDeposit(final StoneKind kind, final int discoverDC, final int idNum) {
		stone = kind;
		dc = discoverDC;
		id = idNum;
	}

	/**
	 * Clone the object.
	 * @param zero whether to zero out the DC
	 * @return a copy of this deposit
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public StoneDeposit copy(final boolean zero) {
		final StoneDeposit retval;
		if (zero) {
			retval = new StoneDeposit(stone, 0, id);
		} else {
			retval = new StoneDeposit(stone, dc, id);
		}
		retval.image = image;
		return retval;
	}

	/**
	 * What kind of stone this deposit is.
	 * @return what kind of stone this deposit is.
	 */
	public StoneKind stone() {
		return stone;
	}

	/**
	 * The Perception check result required to discover the deposit.
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * The exploration-result text for this event.
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		return String.format("There is an exposed %s deposit here.", stone.toString());
	}

	/**
	 * An object is equal iff it is a StoneDeposit with the same kind of stone and the
	 * same ID.
	 * @param obj an object
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof StoneDeposit) &&
										 (((StoneDeposit) obj).stone == stone) &&
										 (((TileFixture) obj).getID() == id));
	}

	/**
	 * Use the ID for hashing.
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * A simple toString().
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "A " + stone + " deposit, of DC " + dc;
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the event
	 */
	@Override
	public String getDefaultImage() {
		return "stone.png";
	}

	/**
	 * The ID number.
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * A fixture is equal, ignoring ID, if it is a StoneDeposit of the same kind.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"ObjectEquality", "CastToConcreteClass", "InstanceofInterfaces"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof StoneDeposit) &&
										 (((StoneDeposit) fix).stone == stone));
	}

	/**
	 * The kind of stone in the deposit, as a String.
	 * @return a string representation of the kind of stone in the deposit
	 */
	@Override
	public String getKind() {
		return stone.toString();
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
	 * The plural of "Stone deposit" is "Stone deposits".
	 * @return a string describing all stone deposits as a class
	 */
	@Override
	public String plural() {
		return "Stone deposits";
	}

	/**
	 * A short description.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "an exposed " + stone() + " deposit";
	}

}

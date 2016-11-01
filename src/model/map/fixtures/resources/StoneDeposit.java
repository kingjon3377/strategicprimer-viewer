package model.map.fixtures.resources;

import model.map.HasMutableKind;
import model.map.IEvent;
import model.map.IFixture;
import model.map.TileFixture;
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
public final class StoneDeposit implements IEvent, HarvestableFixture, HasMutableKind {
	/**
	 * Constructor.
	 *
	 * @param kind  the kind of stone
	 * @param discoverDC the dc to discover the stone.
	 * @param idNum  the ID number.
	 */
	public StoneDeposit(final StoneKind kind, final int discoverDC, final int idNum) {
		stone = kind;
		dc = discoverDC;
		id = idNum;
	}

	/**
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
	 */
	private StoneKind stone;

	/**
	 * @return what kind of stone this deposit is.
	 */
	public StoneKind stone() {
		return stone;
	}

	/**
	 * The DC to discover the stone. TODO: Should perhaps be mutable.
	 */
	private final int dc;

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
		final String stoneStr = stone.toString();
		return String.format("There is an exposed %s deposit here.", stoneStr);
	}

	/**
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
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "A " + stone + " deposit, of DC " + dc;
	}

	/**
	 * @return the name of an image to represent the event
	 */
	@Override
	public String getDefaultImage() {
		return "stone.png";
	}

	/**
	 * ID number.
	 */
	private final int id;

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"ObjectEquality", "CastToConcreteClass"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof StoneDeposit) &&
										(((StoneDeposit) fix).stone == stone));
	}

	/**
	 * @return a string representation of the kind of stone in the deposit
	 */
	@Override
	public String getKind() {
		return stone.toString();
	}

	/**
	 * TODO: Allow arbitrary-text.
	 *
	 * @param nKind the new kind
	 */
	@Override
	public void setKind(final String nKind) {
		stone = StoneKind.parseStoneKind(nKind);
	}

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

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
	 * @return a string describing all stone deposits as a class
	 */
	@Override
	public String plural() {
		return "Stone deposits";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "an exposed " + stone() + " deposit";
	}

}

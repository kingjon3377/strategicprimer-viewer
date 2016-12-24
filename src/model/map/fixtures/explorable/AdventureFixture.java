package model.map.fixtures.explorable;

import model.map.HasMutableOwner;
import model.map.IFixture;
import model.map.Player;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A Fixture representing an adventure hook.
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
public class AdventureFixture implements ExplorableFixture, HasMutableOwner {
	/**
	 * A brief description of the adventure.
	 */
	private final String briefDesc;
	/**
	 * A longer description of the adventure.
	 */
	private final String fullDesc;
	/**
	 * A unique ID # for the fixture.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * The player that has undertaken the adventure.
	 */
	private Player owner;

	/**
	 * Constructor.
	 *
	 * @param player the player who has undertaken the adventure, or the independent
	 *               player if none
	 * @param brief  a brief description of the adventure
	 * @param full   a fuller description of the adventure
	 * @param idNum  an ID number for the fixture
	 */
	public AdventureFixture(final Player player, final String brief,
							final String full, final int idNum) {
		owner = player;
		briefDesc = brief;
		fullDesc = full;
		id = idNum;
	}

	/**
	 * Clone the fixture.
	 * @param zero ignored, as there is no sensitive information that is not essential
	 * @return a copy of this fixture
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public AdventureFixture copy(final boolean zero) {
		final AdventureFixture retval =
				new AdventureFixture(owner, briefDesc, fullDesc, id);
		retval.image = image;
		return retval;
	}

	/**
	 * A brief description of the adventure.
	 * @return a brief description of the adventure
	 */
	public String getBriefDescription() {
		return briefDesc;
	}

	/**
	 * A longer description of the adventure.
	 * @return a fuller description of the adventure
	 */
	public String getFullDescription() {
		return fullDesc;
	}

	/**
	 * If there is a full description, returns it; if not, but there is a brief
	 * description, returns it; otherwise, returns "Adventure hook".
	 * @return a String representation of the fixture
	 */
	@Override
	public String toString() {
		if (fullDesc.isEmpty()) {
			if (briefDesc.isEmpty()) {
				return "Adventure hook";
			} else {
				return briefDesc;
			}
		} else {
			return fullDesc;
		}
	}

	/**
	 * The default icon to use.
	 * @return the name of an image to represent the fixture
	 */
	@Override
	public String getDefaultImage() {
		return "adventure.png";
	}

	/**
	 * Test equality.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof AdventureFixture)
										 && (id == ((AdventureFixture) obj).id)
										 && equalsImpl((AdventureFixture) obj));
	}

	/**
	 * Test for equality, once we know that the object is the right class.
	 * @param obj an adventure fixture
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final AdventureFixture obj) {
		return isOwnerEqual(obj.owner) && briefDesc.equals(obj.briefDesc)
					   && fullDesc.equals(obj.fullDesc);
	}

	/**
	 * Test Players for equality-in-essentials.
	 * @param player a player
	 * @return whether it's the same as the adventure's owner
	 */
	private boolean isOwnerEqual(final Player player) {
		if (owner.isIndependent()) {
			return player.isIndependent();
		} else {
			return owner.getPlayerId() == player.getPlayerId();
		}
	}

	/**
	 * Hash value.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return briefDesc.hashCode() | (fullDesc.hashCode() << owner.getPlayerId());
	}

	/**
	 * The player that has taken on this adventure.
	 * @return the player that has taken on this adventure
	 */
	@Override
	public Player getOwner() {
		return owner;
	}

	/**
	 * Set a new "owner" for the adventure.
	 * @param player the player who has now taken on the adventure
	 */
	@Override
	public void setOwner(final Player player) {
		owner = player;
	}

	/**
	 * The icon to use for this instance.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set a new icon for this instance.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of "adventure" is "adventures".
	 * @return a string describing all text fixtures as a class
	 */
	@Override
	public String plural() {
		return "Adventures";
	}

	/**
	 * A brief description of the fixture.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return briefDesc;
	}

	/**
	 * The adventure's ID number.
	 * @return the ID number of the adventure.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Test equality ignoring ID number.
	 * @param fix a fixture
	 * @return whether it's equal to this one except for its ID number
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof AdventureFixture) && equalsImpl((AdventureFixture) fix);
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * TODO: This should probably be variable (i.e. read from XML)
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 30;
	}
}

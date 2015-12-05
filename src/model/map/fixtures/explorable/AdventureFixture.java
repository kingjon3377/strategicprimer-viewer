package model.map.fixtures.explorable;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasOwner;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;

/**
 * A Fixture representing an adventure hook.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public class AdventureFixture implements ExplorableFixture, HasOwner {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * The player that has undertaken the adventure.
	 */
	private Player owner;
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
	 * Constructor.
	 *
	 * @param player
	 *            the player who has undertaken the adventure, or the
	 *            independent player if none
	 * @param brief
	 *            a brief description of the adventure
	 * @param full
	 *            a fuller description of the adventure
	 * @param idNum an ID number for the fixture
	 */
	public AdventureFixture(final Player player, final String brief,
			final String full, final int idNum) {
		owner = player;
		briefDesc = brief;
		fullDesc = full;
		id = idNum;
	}
	/**
	 * @return a copy of this fixture
	 * @param zero ignored, as there is no sensitive information that is not essential
	 */
	@Override
	public AdventureFixture copy(final boolean zero) {
		AdventureFixture retval = new AdventureFixture(owner, briefDesc, fullDesc, id);
		retval.setImage(image);
		return retval;
	}
	/**
	 * @return a brief description of the adventure
	 */
	public String getBriefDescription() {
		return briefDesc;
	}
	/**
	 * @return a fuller description of the adventure
	 */
	public String getFullDescription() {
		return fullDesc;
	}
	/**
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
	 * @return the name of an image to represent the fixture
	 */
	@Override
	public String getDefaultImage() {
		return "adventure.png";
	}
	/**
	 * @return a z-value for use in ordering tile icons on a tile
	 */
	@Override
	public int getZValue() {
		return 25;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof AdventureFixture
				&& id == ((AdventureFixture) obj).id
				&& equalsImpl((AdventureFixture) obj);
	}
	/**
	 * @param obj an adventure fixture
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final AdventureFixture obj) {
		return equalOwner(obj.owner) && briefDesc.equals(obj.briefDesc)
				&& fullDesc.equals(obj.fullDesc);
	}
	/**
	 * @param player a player
	 * @return whether it's the same as the adventure's owner
	 */
	private boolean equalOwner(final Player player) {
		if (owner.isIndependent()) {
			return player.isIndependent();
		} else {
			return owner.getPlayerId() == player.getPlayerId();
		}
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return briefDesc.hashCode() | (fullDesc.hashCode() << owner.getPlayerId());
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
	 * @return the player that has taken on this adventure
	 */
	@Override
	public Player getOwner() {
		return owner;
	}
	/**
	 * @param player the player who has now taken on the adventure
	 */
	@Override
	public void setOwner(final Player player) {
		owner = player;
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
		return "Adventures";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return briefDesc;
	}
	/**
	 * @return the ID number of the adventure.
	 */
	@Override
	public int getID() {
		return id;
	}
	/**
	 * @param fix a fixture
	 * @return whether it's equal to this one except for its ID number
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof AdventureFixture && equalsImpl((AdventureFixture) fix);
	}
}

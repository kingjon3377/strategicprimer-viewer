package model.map.fixtures.towns;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasImage;
import model.map.IEvent;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import util.NullCleaner;

/**
 * An abstract superclass for towns etc.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
 */
public abstract class AbstractTown implements IEvent, HasImage, ITownFixture {
	/**
	 * The size of the town, fortress, or city.
	 */
	private final TownSize size;
	/**
	 * The status of the town, fortress, or city.
	 */
	private final TownStatus status;
	/**
	 * The owner of this town, fortress, or city.
	 */
	private Player owner;
	/**
	 * The name of this town, fortress, or city.
	 */
	private String name;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param tSize the size of the town, fortress, or city
	 * @param tStatus the status of the town, fortress, or city
	 * @param tName the name of the town, fortress, or city
	 * @param player the owner of the town, fortress, or city
	 */
	protected AbstractTown(final TownStatus tStatus, final TownSize tSize,
			final String tName, final Player player) {
		status = tStatus;
		size = tSize;
		name = tName;
		owner = player;
	}

	/**
	 * @return the name of the town, fortress, or city.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 *
	 * @return the status of the town, fortress, or city
	 */
	@Override
	public TownStatus status() {
		return status;
	}

	/**
	 *
	 * @return the size of the town, fortress, or city
	 */
	@Override
	public TownSize size() {
		return size;
	}

	/**
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final StringBuilder builder = new StringBuilder(56)
				.append("There is a ");
		if (TownSize.Medium == size) {
			builder.append("medium-size");
		} else {
			builder.append(size.toString());
		}
		if (TownStatus.Burned == status) {
			builder.append(" burned-out");
		} else if (TownStatus.Active != status) {
			builder.append(' ');
			builder.append(status.toString());
		}
		builder.append(' ');
		builder.append(kind());
		if (!name.isEmpty()) {
			builder.append(", ");
			builder.append(name);
			builder.append(',');
		}
		builder.append(" here.");
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof AbstractTown
				&& getID() == ((TileFixture) obj).getID()
				&& equalsContents((AbstractTown) obj);
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return this == fix
				|| fix instanceof AbstractTown && equalsContents((AbstractTown) fix);
	}

	/**
	 * This should be used in subclasses' equals() and equalsIgnoringID(), where
	 * all that is needed is a check of the type of the object in question.
	 *
	 * @param fix
	 *            a town-event
	 * @return whether it's equal to this one ignoring ID.
	 */
	protected final boolean equalsContents(final AbstractTown fix) {
		return fix.size() == size && fix.getName().equals(name)
				&& fix.status() == status && fix.owner.equals(owner);
	}

	/**
	 *
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return getID();
	}

	/**
	 *
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(80 + name.length()
				+ owner.getName().length());
		if (owner.isIndependent()) {
			builder.append("An independent ");
		} else {
			builder.append("A ");
		}
		builder.append(size);
		builder.append(' ');
		builder.append(status);
		builder.append(' ');
		builder.append(kind());
		builder.append(" of DC ");
		builder.append(getDC());
		if (name.isEmpty()) {
			builder.append(" with no name");
		} else {
			builder.append(" with name ");
			builder.append(name);
		}
		builder.append(", owned by ");
		if (owner.isCurrent()) {
			builder.append("you");
		} else {
			builder.append(owner.getName());
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 *
	 * @return a description of what kind of 'town' this is.
	 */
	public abstract String kind();

	/**
	 * TODO: Should be more granular.
	 *
	 * @return the name of an image to represent the event.
	 */
	@Override
	public String getDefaultImage() {
		return "town.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() { // NOPMD: Claims this is "empty" and should be abstract
		return 50;
	}

	/**
	 * @return the player that owns the town
	 */
	@Override
	public final Player getOwner() {
		return owner;
	}

	/**
	 * @param player the town's new owner
	 */
	@Override
	public final void setOwner(final Player player) {
		owner = player;
	}

	/**
	 * @param nomen the town's new name
	 */
	@Override
	public final void setName(final String nomen) {
		name = nomen;
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
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		final StringBuilder builder = new StringBuilder(78 + name.length()
				+ owner.getName().length());
		if (owner.isIndependent()) {
			builder.append("An independent ");
		} else {
			builder.append("A ");
		}
		builder.append(size);
		builder.append(' ');
		builder.append(status);
		builder.append(' ');
		builder.append(kind());
		if (name.isEmpty()) {
			builder.append(" with no name");
		} else {
			builder.append(" named ");
			builder.append(name);
		}
		builder.append(", owned by ");
		if (owner.isCurrent()) {
			builder.append("you");
		} else {
			builder.append(owner.getName());
		}
		return NullCleaner.assertNotNull(builder.toString());
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

	}

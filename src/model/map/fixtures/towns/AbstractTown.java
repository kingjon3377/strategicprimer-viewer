package model.map.fixtures.towns;

import java.util.Objects;
import model.map.HasMutableImage;
import model.map.IEvent;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An abstract superclass for towns etc.
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
public abstract class AbstractTown implements IEvent, HasMutableImage, ITownFixture {
	/**
	 * The DC to discover the city.
	 */
	protected final int dc;
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
	private final String name;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * The filename of an image to use as a portrait for the town.
	 */
	private String portraitName = "";

	/**
	 * Constructor.
	 *
	 * @param tSize      the size of the town, fortress, or city
	 * @param tStatus    the status of the town, fortress, or city
	 * @param tName      the name of the town, fortress, or city
	 * @param player     the owner of the town, fortress, or city
	 * @param discoverDC the DC to discover the town
	 */
	protected AbstractTown(final TownStatus tStatus, final TownSize tSize,
						   final String tName, final Player player,
						   final int discoverDC) {
		status = tStatus;
		size = tSize;
		name = tName;
		owner = player;
		dc = discoverDC;
	}

	/**
	 * Get the town's name.
	 * @return the name of the town, fortress, or city.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Get the town's status.
	 * @return the status of the town, fortress, or city
	 */
	@Override
	public TownStatus status() {
		return status;
	}

	/**
	 * Get the town's size.
	 * @return the size of the town, fortress, or city
	 */
	@Override
	public TownSize size() {
		return size;
	}

	/**
	 * Get exploration-result text for the town.
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final String localSize;
		if (TownSize.Medium == size) {
			localSize = "medium-size";
		} else {
			localSize = size.toString();
		}
		final String localStatus;
		if (TownStatus.Burned == status) {
			localStatus = "burned-out";
		} else {
			localStatus = status.toString();
		}
		if (name.isEmpty()) {
			return String.format("There is a %s %s %s here.", localSize, localStatus,
					kind());
		} else {
			return String.format("There is a %s %s %s, %s, here.", localSize,
					localStatus, kind(), name);
		}
	}

	/**
	 * Test equality.
	 * @param obj an object
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof AbstractTown)
										 && (getID() == ((TileFixture) obj).getID())
										 && equalsContents((AbstractTown) obj));
	}

	/**
	 * Test equality ignoring ID number.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"ObjectEquality", "CastToConcreteClass"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix)
					   || ((fix instanceof AbstractTown) &&
								   equalsContents((AbstractTown) fix));
	}

	/**
	 * This should be used in subclasses' equals() and equalsIgnoringID(), where all that
	 * is needed is a check of the type of the object in question.
	 *
	 * @param fix a town-event
	 * @return whether it's equal to this one ignoring ID.
	 */
	protected final boolean equalsContents(final AbstractTown fix) {
		return (fix.size() == size) && Objects.equals(fix.name, name)
					   && (fix.status() == status) && Objects.equals(fix.owner, owner);
	}

	/**
	 * Uses ID number as hash value.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return getID();
	}

	/**
	 * The format of the String representation varies slightly depending on its owner
	 * and whether it has a name.
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		final String nameString;
		if (name.isEmpty()) {
			nameString = "with no name";
		} else {
			nameString = "named " + name;
		}
		final String sizeString = size.toString();
		final String statusString = status.toString();
		if (owner.isIndependent()) {
			return String.format("An independent %s %s %s of DC %d %s", sizeString,
					statusString, kind(), Integer.valueOf(dc), nameString);
		} else if (owner.isCurrent()) {
			return String.format("A %s %s %s of DC %d %s, owned by you", sizeString,
					statusString, kind(), Integer.valueOf(dc), nameString);
		} else {
			return String.format("A %s %s %s of DC %d %s, owned by %s", sizeString,
					statusString, kind(), Integer.valueOf(dc), nameString,
					owner.getName());
		}
	}

	/**
	 * The default icon for towns.
	 * TODO: Should be more granular.
	 *
	 * @return the name of an image to represent the event.
	 */
	@Override
	public String getDefaultImage() {
		return "town.png";
	}

	/**
	 * The owner of the town.
	 * @return the player that owns the town
	 */
	@Override
	public final Player getOwner() {
		return owner;
	}

	/**
	 * Set the owner of the town.
	 * @param player the town's new owner
	 */
	@Override
	public final void setOwner(final Player player) {
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
	 * Set an icon to use for this instance.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * A short description of the town, which differs from {@link #toString()} in that
	 * it doesn't include DC.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		final String nameString;
		if (name.isEmpty()) {
			nameString = "with no name";
		} else {
			nameString = "named " + name;
		}
		if (owner.isIndependent()) {
			return String.format("An independent %s %s %s %s", size.toString(),
					status.toString(), kind(), nameString);
		} else if (owner.isCurrent()) {
			return String.format("A %s %s %s %s, owned by you", size.toString(),
					status.toString(), kind(), nameString);
		} else {
			return String.format("A %s %s %s %s, owned by %s", size.toString(),
					status.toString(), kind(), nameString, owner.getName());
		}
	}

	/**
	 * The filename of a portrait image to use.
	 * @return The filename of an image to use as a portrait for the town.
	 */
	@Override
	public String getPortrait() {
		return portraitName;
	}

	/**
	 * Set the portrait image filename.
	 * @param portrait The filename of an image to use as a portrait for the town.
	 */
	@Override
	public void setPortrait(final String portrait) {
		portraitName = portrait;
	}

	/**
	 * The DC to discover the town.
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}
}

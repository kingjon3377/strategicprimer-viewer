package model.map.fixtures.towns;

import java.util.Formatter;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.Player;
import model.map.SubsettableFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A village on the map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class Village implements ITownFixture, HasMutableImage, SubsettableFixture {
	/**
	 * The status of the village.
	 */
	private final TownStatus status;
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * The dominant race of the village.
	 */
	private String race;
	/**
	 * The "owner" of the village---the player it's pledged to serve.
	 */
	private Player owner;
	/**
	 * The name of the village.
	 */
	private final String name;
	/**
	 * The filename of an image to use as a portrait for the village.
	 */
	private String portraitName = "";

	/**
	 * Constructor.
	 *
	 * @param villageStatus the status of the village.
	 * @param vName         the name of the village
	 * @param idNum         the ID number.
	 * @param player        the owner of the village
	 * @param vRace         the dominant race of the village
	 */
	public Village(final TownStatus villageStatus, final String vName,
				   final int idNum, final Player player, final String vRace) {
		status = villageStatus;
		name = vName;
		id = idNum;
		owner = player;
		race = vRace;
	}

	/**
	 * @param zero ignored, as a village has no sensitive information that is not
	 *             essential
	 * @return a copy of this village
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Village copy(final boolean zero) {
		final Village retval = new Village(status, name, id, owner, race);
		retval.image = image;
		return retval;
	}

	/**
	 * @return a String representation of the village
	 */
	@Override
	public String toString() {
		final StringBuilder builder =
				new StringBuilder(52 + name.length() + owner.getName().length());
		if (owner.isIndependent()) {
			builder.append("Independent ");
		}
		builder.append(status);
		builder.append(" village");
		if (!name.isEmpty()) {
			builder.append(" named ");
			builder.append(name);
		}
		if (owner.isCurrent()) {
			builder.append(", owned by you");
		} else if (!owner.isIndependent()) {
			builder.append(", owned by ");
			builder.append(owner.getName());
		}
		return builder.toString();
	}

	/**
	 * @return the name of an image to represent the village
	 */
	@Override
	public String getDefaultImage() {
		return "village.png";
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Village) && (status == ((Village) obj).status) &&
								name.equals(((Village) obj).name) &&
								(id == ((Village) obj).id) &&
								owner.equals(((Village) obj).owner) &&
								race.equals(((Village) obj).race));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's an identical-but-for-ID village.
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Village) && (status == ((Village) fix).status) &&
					   name.equals(((Village) fix).name) &&
					   owner.equals(((Village) fix).owner) &&
					   race.equals(((Village) fix).race);
	}

	/**
	 * @return the name of the village
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the status of the village
	 */
	@Override
	public TownStatus status() {
		return status;
	}

	/**
	 * All villages are small.
	 *
	 * @return the size of the village.
	 */
	@Override
	public TownSize size() {
		return TownSize.Small;
	}

	/**
	 * @return the "owner" of the village---the player it's pledged to serve and support
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
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return the dominant race of the village.
	 */
	public String getRace() {
		return race;
	}

	/**
	 * @param vRace the new dominant race of the village.
	 */
	public void setRace(final String vRace) {
		race = vRace;
	}

	/**
	 * @return a phrase describing all villages as a class.
	 */
	@Override
	public String plural() {
		return "Villages";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}

	/**
	 * A village is a "subset" of another if they are identical, or if the only
	 * difference
	 * is that the "subset" is independent and the "superset" owes allegiance to some
	 * player.
	 *
	 * @param obj     a fixture
	 * @param ostream a stream to write explanation to
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether the fixture is a "subset" of this
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj instanceof Village) {
			final Village village = (Village) obj;
			return !(!areIntItemsEqual(ostream, id, village.id, "%s\tIDs differ%n",
					context) || !areObjectsEqual(ostream, status, village.status,
					"%s In village (ID #%d):\tVillage status differs%n", context,
					Integer.valueOf(id)) || !areObjectsEqual(ostream, name, village.name,
					"%s In village (ID #%d):\tVillage name differs%n", context,
					Integer.valueOf(id)) || !areObjectsEqual(ostream, race, village.race,
					"%s In village %s (ID #%d):\tDominant race differs%n", context, name,
					Integer.valueOf(id)) || !isConditionTrue(ostream,
					(owner.getPlayerId() == village.owner.getPlayerId()) ||
							village.owner.isIndependent(),
					"%s In village %s (ID #%d):\tOwners differ%n", context, name,
					Integer.valueOf(id)));
		} else {
			ostream.format("%sIncompatible type to Village%n", context);
			return false;
		}
	}

	/**
	 * @return what kind of town this is
	 */
	@Override
	public final String kind() {
		return "village";
	}

	/**
	 * @return The filename of an image to use as a portrait for the village.
	 */
	@Override
	public String getPortrait() {
		return portraitName;
	}

	/**
	 * @param portrait The filename of an image to use as a portrait for the village.
	 */
	@Override
	public void setPortrait(final String portrait) {
		portraitName = portrait;
	}
}

package model.map.fixtures.resources;

import java.util.Formatter;
import model.map.IEvent;
import model.map.IFixture;
import model.map.fixtures.MineralFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A vein of a mineral.
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
public final class MineralVein implements IEvent, HarvestableFixture, MineralFixture {
	/**
	 * The DC to discover the vein. TODO: Should have good defaults
	 */
	private final int dc;
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * Whether the vein is exposed.
	 */
	private boolean exposed;
	/**
	 * What kind of mineral this is.
	 */
	private final String mineral;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param minKind    what kind of mineral this is
	 * @param exp        whether the vein is exposed
	 * @param discoverDC the dc to discover the vein
	 * @param idNum      the ID number.
	 */
	public MineralVein(final String minKind, final boolean exp,
					   final int discoverDC, final int idNum) {
		mineral = minKind;
		exposed = exp;
		dc = discoverDC;
		id = idNum;
	}

	/**
	 * Clone the object.
	 * @param zero whether to zero out the DC
	 * @return a copy of this vein
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public MineralVein copy(final boolean zero) {
		final MineralVein retval;
		if (zero) {
			retval = new MineralVein(mineral, exposed, 0, id);
		} else {
			retval = new MineralVein(mineral, exposed, dc, id);
		}
		retval.image = image;
		return retval;
	}

	/**
	 * Whether the vein is exposed.
	 * @return whether the vein is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * Set whether the vein is exposed.
	 * @param exp whether the vein is exposed
	 */
	public void setExposed(final boolean exp) {
		exposed = exp;
	}

	/**
	 * The DC to discover the mineral vein.
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * Text to explain having discovered the vein.
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final StringBuilder build = new StringBuilder(48 + mineral.length());
		try (final Formatter formatter = new Formatter(build)) {
			if (exposed) {
				formatter.format("There is an exposed vein of %s here.", mineral);
			} else {
				formatter.format("There is a vein of %s here, but it's not exposed.",
						mineral);
			}
		}
		return build.toString();
	}

	/**
	 * An object is equal iff it is a MineralVein with the same mineral and ID and
	 * either both or neither are exposed.
	 * @param obj an object
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof MineralVein)
										 && (id == ((MineralVein) obj).id) &&
										 equalsContents((MineralVein) obj));
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
	 * A simple string representation.
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		if (exposed) {
			return "A " + mineral + " deposit, exposed, DC " + dc;
		} else {
			return "A " + mineral + " deposit, not exposed, DC " + dc;
		}
	}

	/**
	 * The kind of mineral.
	 * @return the kind of mineral
	 */
	@Override
	public String getKind() {
		return mineral;
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the event
	 */
	@Override
	public String getDefaultImage() {
		return "mineral.png";
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
	 * If we ignore ID and DC, a fixture is equal if it is a MineralVein with the same
	 * kind of mineral and either both or neither are exposed.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"ObjectEquality", "CastToConcreteClass", "InstanceofInterfaces"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof MineralVein)
										 && equalsContents((MineralVein) fix));
	}

	/**
	 * If we ignore ID and DC, a fixture is equal if it is a MineralVein with the same
	 * kind of mineral and either both or neither are exposed.
	 * @param fix another MineralEvent
	 * @return whether its contents equal ours
	 */
	private boolean equalsContents(final MineralVein fix) {
		return fix.mineral.equals(mineral) && (fix.exposed == exposed);
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
	 * The plural of "Mineral vein" is "Mineral veins".
	 * @return a string describing all mineral veins as a class
	 */
	@Override
	public String plural() {
		return "Mineral veins";
	}

	/**
	 * A short description of the vein.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (exposed) {
			return "exposed " + mineral;
		} else {
			return "unexposed " + mineral;
		}
	}
}

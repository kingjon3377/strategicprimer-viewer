package model.map.fixtures.resources;

import java.util.Formatter;
import model.map.HasKind;
import model.map.IEvent;
import model.map.IFixture;
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
public final class MineralVein implements IEvent, HarvestableFixture, HasKind {
	/**
	 * The DC to discover the vein. TODO: Should perhaps be mutable.
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
	 * @return what kind of mineral this is
	 */
	public String mineral() {
		return mineral;
	}

	/**
	 * @return whether the vein is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * @param exp whether the vein is exposed
	 */
	public void setExposed(final boolean exp) {
		exposed = exp;
	}

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
	 * @param obj an object
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof MineralVein)
										 && mineral.equals(((MineralVein) obj).mineral)
										 && (exposed == ((MineralVein) obj).exposed)
										 && (id == ((MineralVein) obj).id));
	}

	/**
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
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
	 * @return the kind of mineral
	 */
	@Override
	public String getKind() {
		return mineral;
	}

	/**
	 * @return the name of an image to represent the event
	 */
	@Override
	public String getDefaultImage() {
		return "mineral.png";
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
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"ObjectEquality", "CastToConcreteClass"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof MineralVein)
										 && equalsContents((MineralVein) fix));
	}

	/**
	 * @param fix another MineralEvent
	 * @return whether its contents equal ours
	 */
	private boolean equalsContents(final MineralVein fix) {
		return fix.mineral.equals(mineral) && (fix.exposed == exposed);
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
	 * @return a string describing all mineral veins as a class
	 */
	@Override
	public String plural() {
		return "Mineral veins";
	}

	/**
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

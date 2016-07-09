package model.map.fixtures.resources;

import model.map.HasMutableKind;
import model.map.IEvent;
import model.map.IFixture;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

/**
 * A vein of a mineral.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MineralVein implements IEvent, HarvestableFixture, HasMutableKind {
	/**
	 * Whether the vein is exposed.
	 */
	private boolean exposed;

	/**
	 * What kind of mineral this is.
	 */
	private String mineral;

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param minKind what kind of mineral this is
	 * @param exp     whether the vein is exposed
	 * @param discoverDC  the dc to discover the vein
	 * @param idNum   the ID number.
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
	 * The DC to discover the vein. TODO: Should perhaps be mutable.
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
		final StringBuilder build =
				new StringBuilder(48 + mineral.length()).append("There is a");
		if (exposed) {
			build.append("n exposed");
		}
		build.append(" vein of ");
		build.append(mineral);
		build.append(" here");
		if (exposed) {
			build.append('.');
		} else {
			build.append(", but it's not exposed.");
		}
		return NullCleaner.assertNotNull(build.toString());
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
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@SuppressWarnings("MagicNumber")
	@Override
	public int getZValue() {
		return 40;
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
	 * @param nKind the new kind
	 */
	@Override
	public void setKind(final String nKind) {
		mineral = nKind;
	}

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}
	/**
	 * @param exp whether the vein is exposed
	 */
	public void setExposed(final boolean exp) {
		exposed = exp;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
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
		return toString();
	}
}

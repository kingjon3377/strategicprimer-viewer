package model.map.fixtures.explorable;

import java.util.Formatter;
import model.map.IFixture;
import model.map.Point;
import model.map.PointFactory;
import model.map.SubsettableFixture;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;

/**
 * A fixture representing a portal to another world.
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
public class Portal implements ExplorableFixture, SubsettableFixture {
	/**
	 * A string identifying the world the portal connects to.
	 *
	 * TODO: Should this be mutable?
	 */
	private final String destinationWorld;
	/**
	 * A unique ID # for the fixture.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * The coordinates in that world that the portal connects to. A negative coordinate
	 * indicates that the coordinate needs to be generated, presumably randomly.
	 */
	private Point destinationCoordinates;

	/**
	 * Constructor.
	 *
	 * @param dest            a string identifying the world the portal connects to
	 * @param destCoordinates the coordinates in that world that the portal connects to
	 * @param idNum           the ID # for the portal
	 */
	public Portal(final String dest, final Point destCoordinates, final int idNum) {
		destinationWorld = dest;
		destinationCoordinates = destCoordinates;
		id = idNum;
	}

	/**
	 * @param zero whether to "zero out" the destination coordinates
	 * @return a copy of this portal
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Portal copy(final boolean zero) {
		final Portal retval;
		if (zero) {
			retval = new Portal("unknown", PointFactory.INVALID_POINT, id);
		} else {
			retval = new Portal(destinationWorld, destinationCoordinates, id);
		}
		retval.image = image;
		return retval;
	}

	/**
	 * @return a string identifying the world the portal connects to
	 */
	public String getDestinationWorld() {
		return destinationWorld;
	}

	/**
	 * @return the location in that world the portal connects to
	 */
	public Point getDestinationCoordinates() {
		return destinationCoordinates;
	}

	/**
	 * @param destination the new destination coordinates
	 */
	public void setDestinationCoordinates(final Point destination) {
		destinationCoordinates = destination;
	}

	/**
	 * @return a String representation of the fixture
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "A portal to another world";
	}

	/**
	 * @return the name of an image to represent the fixture if no instance-specific
	 * image
	 * has been specified
	 */
	@Override
	public String getDefaultImage() {
		return "portal.png";
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Portal) && (id == ((Portal) obj).id)
										 && equalsIgnoringID((Portal) obj));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
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
	 * @return a string describing all portals as a class
	 */
	@Override
	public String plural() {
		return "Portals";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "A portal to another world";
	}

	/**
	 * @return an ID # for the fixture
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it would be equal to this one if its ID # were not considered
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Portal) &&
					   destinationWorld.equals(((Portal) fix).destinationWorld) &&
					   destinationCoordinates
							   .equals(((Portal) fix).destinationCoordinates);
	}

	/**
	 * Test whether an object is a subset of this one.
	 *
	 * TODO: test this
	 *
	 * @param obj     an object
	 * @param ostream the stream to write details to
	 * @param context a string to print before every line of output, describing the
	 *                context; it should be passed through and appended to. Whenever
	 *                it is
	 *                put onto ostream, it should probably be followed by a tab.
	 * @return whether that object equals, or is a zeroed-out equivalent of, this one
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			if (obj instanceof Portal) {
				final Portal other = (Portal) obj;
				if (!EqualsAny.equalsAny(other.destinationWorld, "unknown",
						destinationWorld)) {
					ostream.format(
							"%s\tIn portal with ID #%d: Different destination world%n",
							context, Integer.valueOf(id));
					return false;
				} else if (other.destinationCoordinates.getRow() > 0 &&
								   other.destinationCoordinates.getCol() > 0 &&
								   !destinationCoordinates
											.equals(other.destinationCoordinates)) {
					ostream.format(
							"%s\tIn portal with ID #%d: Different destination " +
									"coordinates%n",
							context, Integer.valueOf(id));
					return false;
				} else {
					return true;
				}
			} else {
				ostream.format("%s\tDifferent kinds of fixtures for ID #%d%n", context,
						Integer.valueOf(id));
				return false;
			}
		} else {
			ostream.format("%s\tCalled with different-ID-# argument%n", context);
			return false;
		}
	}
}

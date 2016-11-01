package model.map.fixtures.explorable;

import java.io.IOException;
import model.map.IFixture;
import model.map.Point;
import model.map.PointFactory;
import model.map.SubsettableFixture;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;
import util.LineEnd;

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
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * A string identifying the world the portal connects to.
	 *
	 * TODO: Should this be mutable?
	 */
	private final String destinationWorld;
	/**
	 * The coordinates in that world that the portal connects to. A negative coordinate
	 * indicates that the coordinate needs to be generated, presumably randomly.
	 */
	private Point destinationCoordinates;
	/**
	 * A unique ID # for the fixture.
	 */
	private final int id;

	/**
	 * Constructor.
	 *
	 * @param dest       a string identifying the world the portal connects to
	 * @param destCoordinates the coordinates in that world that the portal connects to
	 * @param idNum      the ID # for the portal
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
			retval = new Portal("unknown", PointFactory.point(-1, -1), id);
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
	 * @param destination the new destination coordinates
	 */
	public void setDestinationCoordinates(final Point destination) {
		destinationCoordinates = destination;
	}

	/**
	 * TODO: test this
	 *
	 * @param obj     an object
	 * @param ostream the stream to write details to
	 * @param context a string to print before every line of output, describing the
	 *                context; it should be passed through and appended to. Whenever
	 *                it is
	 *                put onto ostream, it should probably be followed by a tab.
	 * @return whether that object equals, or is a zeroed-out equivalent of, this one
	 * @throws IOException
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
							final String context) throws IOException {
		if (obj.getID() == id) {
			if (obj instanceof Portal) {
				final Portal other = (Portal) obj;
				if (!EqualsAny.equalsAny(other.getDestinationWorld(), "unknown",
						destinationWorld)) {
					ostream.append(context);
					ostream.append("\tIn portal with ID #");
					ostream.append(Integer.toString(getID()));
					ostream.append(": Different destination world");
					ostream.append(LineEnd.LINE_SEP);
					return false;
				} else if (other.getDestinationCoordinates().getRow() > 0 &&
								   other.getDestinationCoordinates().getCol() > 0 &&
								   !destinationCoordinates
											.equals(other.getDestinationCoordinates())) {
					ostream.append(context);
					ostream.append("\tIn portal with ID #");
					ostream.append(Integer.toString(getID()));
					ostream.append(": Different destination coordinates");
					ostream.append(LineEnd.LINE_SEP);
					return false;
				} else {
					return true;
				}
			} else {
				ostream.append(context);
				ostream.append("\tDifferent kinds of fixtures for ID #");
				ostream.append(Integer.toString(getID()));
				ostream.append(LineEnd.LINE_SEP);
				return false;
			}
		} else {
			ostream.append(context);
			ostream.append("\tCalled with different-ID-# argument");
			ostream.append(LineEnd.LINE_SEP);
			return false;
		}
	}
}

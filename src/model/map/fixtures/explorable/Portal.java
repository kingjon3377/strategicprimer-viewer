package model.map.fixtures.explorable;

import model.map.IFixture;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A fixture representing a portal to another world.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
public class Portal implements ExplorableFixture {
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
	 * @param destCoords the coordinates in that world that the portal connects to
	 * @param idNum      the ID # for the portal
	 */
	public Portal(final String dest, final Point destCoords, final int idNum) {
		destinationWorld = dest;
		destinationCoordinates = destCoords;
		id = idNum;
	}

	/**
	 * TODO: Should we "zero out" (to "unknown") the destination world?
	 *
	 * @param zero whether to "zero out" the destination coordinates
	 * @return a copy of this portal
	 */
	@Override
	public Portal copy(final boolean zero) {
		final Portal retval;
		if (zero) {
			retval = new Portal(destinationWorld, PointFactory.point(-1, -1), id);
		} else {
			retval = new Portal(destinationWorld, destinationCoordinates, id);
		}
		retval.setImage(image);
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
	 * @return a String represntation of the fixture
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
	 * @return a Z-value for use in ordering tile icons on a tile
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
	 * @param obj a fixture
	 * @return whether it would be equal to this one if its ID # were not considered
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture obj) {
		return (obj instanceof Portal)
				       && destinationWorld.equals(((Portal) obj).destinationWorld)
				       && destinationCoordinates
						          .equals(((Portal) obj).destinationCoordinates);
	}

	/**
	 * @param destination the new destination coordinates
	 */
	public void setDestinationCoordinates(final Point destination) {
		destinationCoordinates = destination;
	}
}

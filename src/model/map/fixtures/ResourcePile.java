package model.map.fixtures;

import java.util.Formatter;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IFixture;
import org.eclipse.jdt.annotation.Nullable;
import util.Quantity;

/**
 * A quantity of some kind of resource.
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
 *
 *         TODO: more members
 */
public class ResourcePile
		implements UnitMember, FortressMember, HasKind, HasMutableImage {
	/**
	 * The ID # of the resource pile.
	 */
	private final int id;
	/**
	 * What general kind of thing is in the resource pile.
	 */
	private final String kind;
	/**
	 * What specific kind of thing is in the resource pile.
	 */
	private String contents;
	/**
	 * How much of that thing is in the pile, including units.
	 */
	private Quantity quantity;
	/**
	 * The image to use for the resource.
	 */
	private String image = "";
	/**
	 * The turn on which the resource was created.
	 */
	private int created = -1;

	/**
	 * Constructor.
	 * @param idNum       an ID number for the fixture
	 * @param resKind     the general kind of resource
	 * @param resContents the specific kind of resource
	 * @param qty         how much of the resource is in the pile
	 */
	public ResourcePile(final int idNum, final String resKind,
						final String resContents, final Quantity qty) {
		id = idNum;
		kind = resKind;
		contents = resContents;
		quantity = qty;
	}

	/**
	 * The ID number.
	 * @return the ID # of the resource pile
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * What general kind of resource this is. (E.g. "food")
	 * @return the general kind of resource this is
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * What specific kind of resource this is. (E.g. "meat")
	 * @return the specific kind of resource this is
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * Set the specific kind of resource.
	 * @param nContents the new value of the specific kind of resource this is
	 */
	public void setContents(final String nContents) {
		contents = nContents;
	}

	/**
	 * The default icon filename.
	 * @return the filename of an image to use for resource piles by default
	 */
	@Override
	public String getDefaultImage() {
		return "resource.png";
	}

	/**
	 * The per-instance icon filename.
	 * @return the filename of an image to use for this resource pile
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the per-instance icon filename.
	 * @param img the filename of an image to use for this resource pile.
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * If we ignore ID, a fixture is equal iff it is a ResourcePile with the same kind and
	 * contents, of the same age, and with equal quantity.
	 * @param fix a fixture
	 * @return whether it equals this one except for ID
	 */
	@SuppressWarnings({"CastToConcreteClass", "InstanceofInterfaces"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof ResourcePile) && kind.equals(((ResourcePile) fix).kind) &&
					   contents.equals(((ResourcePile) fix).contents) &&
					   quantity.equals(((ResourcePile) fix).quantity) &&
					   created == ((ResourcePile) fix).created;
	}

	/**
	 * A fixture is a subset iff it is a ResourcePile of the same kind, contents, and age,
	 * with the same ID, and its quantity is a subset of ours.
	 * @param obj     a fixture
	 * @param ostream the stream to report errors to
	 * @param context the context to report before errors
	 * @return whether it's a subset of (i.e. equal to, except perhaps with different
	 * quantity from) this one
	 */
	@SuppressWarnings({"CastToConcreteClass", "InstanceofInterfaces"})
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			final Integer idNum = Integer.valueOf(id);
			if (obj instanceof ResourcePile) {
				boolean retval = areObjectsEqual(ostream, kind, ((ResourcePile) obj).kind,
						"%s\tIn Resource Pile, ID #%d: Kinds differ%n", context,
						idNum);
				retval &= areObjectsEqual(ostream, contents,
						((ResourcePile) obj).contents,
						"%s\tIn Resource Pile, ID #%d, Contents differ%n", context,
						idNum);
				retval &= quantity.isSubset(((ResourcePile) obj).quantity, ostream,
						String.format("%s\tIn Resource Pile, ID #%d", context,
								idNum));
				retval &= areIntItemsEqual(ostream, created, ((ResourcePile) obj)
																	 .created,
						"%s\tIn Resource Pile, ID #%d, Age differs", context,
						Integer.valueOf(id));
				return retval;
			} else {
				ostream.format("%s\tDifferent fixture types given for ID #%d", context,
						idNum);
				return false;
			}
		} else {
			ostream.format("%s\tIDs differ%n", context);
			return false;
		}
	}

	/**
	 * Clone the object.
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this Implement
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public ResourcePile copy(final boolean zero) {
		final ResourcePile retval = new ResourcePile(id, kind, contents, quantity);
		if (!zero) {
			retval.setCreated(created);
		}
		return retval;
	}

	/**
	 * The quantity of resource in the pile.
	 * @return the quantity of resource in the pile
	 */
	public Quantity getQuantity() {
		return quantity;
	}

	/**
	 * Set the quantity of resource in the pile.
	 * @param qty the new quantity of resource in the pile.
	 */
	public void setQuantity(final Quantity qty) {
		quantity = qty;
	}

	/**
	 * Use ID for hashing.
	 * @return a hash code for the object.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * An object is equal iff it is a ResourcePile with equal ID and quantity and the
	 * same kind, contents, and created date.
	 * @param obj an object
	 * @return whether it equals this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof ResourcePile) &&
										 (id == ((ResourcePile) obj).id) &&
										 quantity.equals(((ResourcePile) obj)
																 .quantity) &&
										 contents.equals(((ResourcePile) obj)
																 .contents) &&
										 kind.equals(((ResourcePile) obj).kind) &&
										 (created == ((ResourcePile) obj).created));
	}

	/**
	 * A String representaiton of the pile.
	 * @return a String representation of the resource pile
	 */
	@Override
	public String toString() {
		final String age;
		if (created < 0) {
			age = "";
		} else {
			age = " from turn " + created;
		}
		if (quantity.getUnits().isEmpty()) {
			return String.format("A pile of %s %s (%s)%s", quantity.toString(),
					contents, kind, age);
		} else {
			return String.format("A pile of %s of %s (%s)%s", quantity.toString(),
					contents, kind, age);
		}
	}

	/**
	 * The turn on which the resource was created. A negative return value means N/A.
	 * @return the turn on which the resource was created
	 */
	public int getCreated() {
		return created;
	}

	/**
	 * Set the turn on which the resource was created.
	 * @param createdTurn the turn on which the resource was created
	 */
	public void setCreated(final int createdTurn) {
		if (createdTurn < 0) {
			created = -1;
		} else {
			created = createdTurn;
		}
	}
}

package model.map.fixtures;

import java.io.IOException;
import model.map.HasImage;
import model.map.HasMutableKind;
import model.map.IFixture;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

/**
 * A quantity of some kind of resource.
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
 *
 *         TODO: more members
 */
public class ResourcePile
		implements UnitMember, FortressMember, HasMutableKind, HasImage {
	/**
	 * The ID # of the resource pile.
	 */
	private final int id;
	/**
	 * What general kind of thing is in the resource pile.
	 */
	private String kind;
	/**
	 * What specific kind of thing is in the resource pile.
	 */
	private String contents;
	/**
	 * How much of that thing is in the pile.
	 */
	private int quantity;
	/**
	 * The units the quantity is measured in.
	 */
	private String unit;
	/**
	 * The image to use for the resource.
	 */
	private String image = "";
	/**
	 * The turn on which the resource was created.
	 */
	private int created = -1;

	/**
	 * @param idNum       an ID number for the fixture
	 * @param resKind     the general kind of resource
	 * @param resContents the specific kind of resource
	 * @param qty         how much of the resource is in the pile
	 * @param qtyUnit     what units the quantity is measured in
	 */
	public ResourcePile(final int idNum, final String resKind,
						final String resContents, final int qty, final String qtyUnit) {
		id = idNum;
		kind = resKind;
		contents = resContents;
		quantity = qty;
		unit = qtyUnit;
	}

	/**
	 * @return the ID # of the resource pile
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @return the general kind of resource this is
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @param nKind the new value for the general kind of resource this is
	 */
	@Override
	public void setKind(final String nKind) {
		kind = nKind;
	}

	/**
	 * @return the specific kind of resource this is
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * @param nContents the new value of the specific kind of resource this is
	 */
	public void setContents(final String nContents) {
		contents = nContents;
	}

	/**
	 * @return the filename of an image to use for implements by default
	 */
	@Override
	public String getDefaultImage() {
		return "resource.png";
	}

	/**
	 * @param img the filename of an image to use for this implement
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return the filename of an image to use for this implement
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @param fix a fixture
	 * @return whether it equals this one except for ID
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof ResourcePile)
					   && kind.equals(((ResourcePile) fix).kind)
					   && contents.equals(((ResourcePile) fix).contents)
					   && (quantity == ((ResourcePile) fix).quantity)
					   && unit.equals(((ResourcePile) fix).unit);
	}

	/**
	 * @param ostream the stream to report errors to
	 * @param context the context to report before errors
	 * @param obj     a fixture
	 * @return whether it's a subset of (i.e. equal to, except perhaps with different
	 * quantity from) this one
	 * @throws IOException on I/O error writing to ostream
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
							final String context) throws IOException {
		if (obj.getID() != id) {
			ostream.append(context);
			ostream.append("\tIDs differ");
			return false;
		} else if (obj instanceof ResourcePile) {
			boolean retval = true;
			final String ctxt = String.format("%s\tIn Resource Pile, ID #%d: ",
					context, Integer.valueOf(id));
			if (!kind.equals(((ResourcePile) obj).kind)) {
				ostream.append(ctxt);
				ostream.append("Kinds differ\n");
				retval = false;
			}
			if (!contents.equals(((ResourcePile) obj).contents)) {
				ostream.append(ctxt);
				ostream.append("Contents differ\n");
				retval = false;
			}
			if (!unit.equals(((ResourcePile) obj).unit)) {
				ostream.append(ctxt);
				ostream.append("Units differ\n");
				retval = false;
			}
			if ((quantity != ((ResourcePile) obj).quantity)
						&& (0 != ((ResourcePile) obj).quantity)) {
				ostream.append(ctxt);
				ostream.append("Quantities differ\n");
				retval = false;
			}
			return retval;
		} else {
			ostream.append(context);
			ostream.append("\tDifferent fixture types given for ID #");
			ostream.append(Integer.toString(id));
			return false;
		}
	}

	/**
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this Implement
	 */
	@Override
	public ResourcePile copy(final boolean zero) {
		final ResourcePile retval = new ResourcePile(id, kind, contents, quantity, unit);
		if (!zero) {
			retval.setCreated(created);
		}
		return retval;
	}

	/**
	 * @return the quantity of resource in the pile
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @param qty the new quantity of resource in the pile
	 */
	public void setQuantity(final int qty) {
		quantity = qty;
	}

	/**
	 * @return the unit in which the quantity is measured
	 */
	public String getUnits() {
		return unit;
	}

	/**
	 * @param newUnit the unit in which the quantity is measured
	 */
	public void setUnits(final String newUnit) {
		unit = newUnit;
	}

	/**
	 * This hash code function is modified, I think without changing the algorithm, from
	 * the auto-generated one.
	 *
	 * @return a hash code for the object.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		return (prime * ((prime
								  * ((prime * ((prime * (prime + contents.hashCode())) +
													   id))
											 + kind.hashCode()))
								 + quantity)) + unit.hashCode();
	}

	/**
	 * @param obj an object
	 * @return whether it equals this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj)
					   ||
					   ((obj instanceof ResourcePile) && (id == ((ResourcePile) obj).id)
								&& (quantity == ((ResourcePile) obj).quantity)
								&& contents.equals(((ResourcePile) obj).contents)
								&& kind.equals(((ResourcePile) obj).kind)
								&& unit.equals(((ResourcePile) obj).unit) &&
								(created == ((ResourcePile) obj).created));
	}

	/**
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
		if (unit.isEmpty()) {
			return NullCleaner
						   .assertNotNull(String.format("A pile of %d %s (%s)%s",
								   Integer.valueOf(quantity), contents, kind, age));
		} else {
			return NullCleaner
						   .assertNotNull(String.format("A pile of %d %s of %s (%s)%s",
								   Integer.valueOf(quantity), unit, contents, kind,
								   age));
		}
	}

	/**
	 * @param createdTurn the turn on which the resource was created
	 */
	public void setCreated(final int createdTurn) {
		if (createdTurn < 0) {
			created = -1;
		} else {
			created = createdTurn;
		}
	}

	/**
	 * @return the turn on which the resource was created
	 */
	public int getCreated() {
		return created;
	}
}

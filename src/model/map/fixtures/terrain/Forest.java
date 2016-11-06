package model.map.fixtures.terrain;

import model.map.HasMutableImage;
import model.map.HasMutableKind;
import model.map.IFixture;
import model.map.TerrainFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A forest on a tile.
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
public class Forest implements TerrainFixture, HasMutableImage, HasMutableKind {
	/**
	 * What kind of trees dominate the forest.
	 */
	private String trees;

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * @return what kind of trees
	 */
	@Override
	public String getKind() {
		return trees;
	}
	/**
	 * Unique identifying number for this instance.
	 */
	private int id;

	/**
	 * Whether this is "rows of" trees.
	 */
	private final boolean rows;

	/**
	 * Constructor.
	 *
	 * @param kind  what kind of trees dominate.
	 * @param rowed whether the trees are in rows
	 * @param idNum a number to uniquely identify this instance
	 */
	public Forest(final String kind, final boolean rowed, final int idNum) {
		trees = kind;
		rows = rowed;
		id = idNum;
	}

	/**
	 * @param zero ignored, as there's no sensitive data
	 * @return a copy of this forest
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Forest copy(final boolean zero) {
		final Forest retval = new Forest(trees, rows, id);
		retval.image = image;
		return retval;
	}

	/**
	 * @return a String representation of the forest.
	 */
	@Override
	public String toString() {
		if (rows) {
			return "Rows of " + trees + " trees.";
		} else {
			return "A " + trees + " forest.";
		}
	}

	/**
	 * TODO: Should differ based on what kind of tree.
	 *
	 * @return the name of an image to represent the forest.
	 */
	@Override
	public String getDefaultImage() {
		return "trees.png";
	}

	/**
	 * @return whether this is "rows of" trees.
	 */
	public boolean isRows() {
		return rows;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Forest) && equalsImpl((Forest) obj));
	}

	/**
	 * @param obj a forest
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final Forest obj) {
		return id == obj.id && trees.equals(obj.trees) && (rows == obj.rows);
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @return an ID for the object
	 */
	@Override
	public int getID() {
		return id;
	}
	/**
	 * Set the ID for the object.
	 */
	public void setID(final int idNum) {
		id = idNum;
	}
	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) ||
					   ((fix instanceof Forest) && trees.equals(((Forest) fix).trees) &&
								(rows == ((Forest) fix).rows));
	}

	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		trees = nKind;
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
	 * @return a string describing all forests as a class
	 */
	@Override
	public String plural() {
		return "Forests";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}

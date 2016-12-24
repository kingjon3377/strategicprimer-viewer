package model.map.fixtures.terrain;

import model.map.HasKind;
import model.map.HasMutableImage;
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
public class Forest implements TerrainFixture, HasMutableImage, HasKind {
	/**
	 * Whether this is "rows of" trees.
	 */
	private final boolean rows;
	/**
	 * What kind of trees dominate the forest.
	 */
	private final String trees;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * Unique identifying number for this instance.
	 */
	private int id;

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
	 * What kind of trees are in the forest.
	 * @return what kind of trees
	 */
	@Override
	public String getKind() {
		return trees;
	}

	/**
	 * Clone the forest.
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
	 * Delegates to shortDesc().
	 * @return a String representation of the forest.
	 */
	@Override
	public String toString() {
		return shortDesc();
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
	 * Whether this is "rows of" trees.
	 * @return whether this is "rows of" trees.
	 */
	public boolean isRows() {
		return rows;
	}

	/**
	 * An object is equal iff it is a Forest of the same kind with the same ID and
	 * either both or neither is rows.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Forest) && equalsImpl((Forest) obj));
	}

	/**
	 * A forest is equal iff it has the same ID and kind and either both or neither are
	 * rows.
	 * @param obj a forest
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final Forest obj) {
		return id == obj.id && trees.equals(obj.trees) && (rows == obj.rows);
	}

	/**
	 * Use the ID for hashing.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * The ID number.
	 * @return an ID for the object
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Set the ID for the object.
	 * @param idNum the new ID number
	 */
	public void setID(final int idNum) {
		id = idNum;
	}

	/**
	 * If we ignore ID, a fixture is equal iff it is a forest of the same kind and
	 * either both or neither is rows.
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
	 * The plural of Forest is Forests.
	 * @return a string describing all forests as a class
	 */
	@Override
	public String plural() {
		return "Forests";
	}

	/**
	 * Either "Rows of such-and-such trees." or "A such-and-such forest.".
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (rows) {
			return "Rows of " + trees + " trees.";
		} else {
			return "A " + trees + " forest.";
		}
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 5;
	}
}

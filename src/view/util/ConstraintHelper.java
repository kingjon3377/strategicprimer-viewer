package view.util;

import java.awt.GridBagConstraints;

/**
 * A helper class so we can specify arguments inline rather than having to build
 * each object manually.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2010-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public class ConstraintHelper extends GridBagConstraints {
	/**
	 * Constructor taking just position.
	 *
	 * @param col the column
	 * @param row the row
	 */
	public ConstraintHelper(final int col, final int row) {
		gridx = col;
		gridy = row;
	}

	/**
	 * Constructor taking position and extent.
	 *
	 * @param col the column
	 * @param row the row
	 * @param width how many columns
	 * @param height how many rows
	 */
	public ConstraintHelper(final int col, final int row, final int width,
			final int height) {
		this(col, row);
		gridwidth = width;
		gridheight = height;
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ConstraintHelper";
	}

}

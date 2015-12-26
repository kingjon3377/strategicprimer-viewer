package view.util;

/**
 * A class to hold numeric constants useful for drawing.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
public enum DrawingNumericConstants {
	/**
	 * The part of a tile's width or height a river's short dimension should occupy.
	 */
	RiverShortDimension(1.0 / 8.0),
	/**
	 * Where the short side of a river starts, along the edge of the tile.
	 */
	RiverShortStart(7.0 / 16.0),
	/**
	 * The part of a tile's width or height its long dimension should occupy.
	 *
	 * FIXME: This gets used for drawing things other than rivers; it should be split.
	 */
	RiverLongDimension(1.0 / 2.0),
	/**
	 * How far along a tile's dimension a lake should start.
	 *
	 * FIXME: This gets used for drawing things other than lakes; it should be split.
	 */
	LakeStart(1.0 / 4.0),
	/**
	 * How wide and tall a fort should be.
	 */
	FortSize(1.0 / 3.0),
	/**
	 * Where a fort should start.
	 */
	FortStart(2.0 / 3.0),
	/**
	 * Where an 'event' should start.
	 */
	EventStart(3.0 / 4.0);
	/**
	 * @param numConst the constant this instance encapsulates.
	 */
	DrawingNumericConstants(final double numConst) {
		constant = numConst;
	}
	/**
	 * The constant this instance encapsulates.
	 */
	public final double constant;
}

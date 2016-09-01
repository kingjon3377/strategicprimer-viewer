package model.map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A structure encapsulating two coordinates.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public interface Point extends Comparable<@NonNull Point> {
	/**
	 * @return the first coordinate.
	 */
	int getRow();

	/**
	 * @return the second coordinate.
	 */
	int getCol();
	/**
	 * @param point another point
	 * @return the result of a comparison with that point
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	default int compareTo(final Point point) {
		if (getRow() > point.getRow()) {
			return 1;
		} else if (getRow() < point.getRow()) {
			return -1;
		} else if (getCol() > point.getCol()) {
			return 1;
		} else if (getCol() < point.getCol()) {
			return -1;
		} else {
			return 0;
		}
	}

}

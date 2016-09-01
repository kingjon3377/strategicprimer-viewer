package model.viewer;

import model.map.TileFixture;

/**
 * An interface for a filter to tell whether a given fixture should be displayed.
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
 */
@FunctionalInterface
public interface ZOrderFilter {
	/**
	 * @param fix a fixture
	 * @return whether it should be displayed or not
	 */
	boolean shouldDisplay(TileFixture fix);
}

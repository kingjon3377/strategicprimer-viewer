package model.map.fixtures.mobile;

import model.map.fixtures.UnitMember;

/**
 * A (marker) interface for centaurs, trolls, ogres, fairies, and the like.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface Immortal extends MobileFixture, UnitMember {
	/**
	 * Clone the object.
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of the fixture
	 */
	@Override
	Immortal copy(final boolean zero);
}

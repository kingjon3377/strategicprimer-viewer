package model.map.fixtures;

import model.map.SubsettableFixture;

/**
 * A (marker) interface for things that can be part of a unit.
 *
 * We extend Subsettable to make Unit's subset calculation show differences in
 * workers, but without hard-coding "Worker" in the Unit implementation. Most
 * implementations of this will essentially delegate to equals().
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public interface UnitMember extends SubsettableFixture {
	// Just a marker interface for now. TODO: members?
	/**
	 * A specialization of the method from IFixture.
	 * @return a copy of the member
	 * @param zero whether to "zero out" or omit sensitive information
	 */
	@Override
	UnitMember copy(boolean zero);
}

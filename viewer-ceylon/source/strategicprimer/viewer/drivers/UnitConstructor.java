package strategicprimer.viewer.drivers;

import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;

/**
 * A wrapper around the Unit class's constructor, since the Ceylon compiler in the
 * IntelliJ plugin says it doesn't have a default constructor.
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
 * @deprecated this is a hack
 */
@Deprecated
public class UnitConstructor {
	/**
	 * Instantiate a unit.
	 * @param owner the unit's owner
	 * @param kind the unit's kind
	 * @param name the unit's name
	 * @param id the unit's ID.
	 */
	public static IUnit unit(final Player owner, final String kind, final String name,
							 final int id) {
		return new Unit(owner, kind, name, id);
	}
}

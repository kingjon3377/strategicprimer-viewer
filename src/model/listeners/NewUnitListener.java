package model.listeners;

import java.util.EventListener;
import model.map.fixtures.mobile.IUnit;

/**
 * An interface for things that want to accept a new user-created unit.
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
public interface NewUnitListener extends EventListener {
	/**
	 * Add the new unit.
	 *
	 * @param unit the unit to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addNewUnit(IUnit unit);
}

package model.listeners;

import java.util.EventListener;

/**
 * An interface for objects listening to AddRemovePanels.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
 */
public interface AddRemoveListener extends EventListener {
	/**
	 * @param category what kind of thing is being added
	 * @param addendum A String description of the thing to be added
	 */
	void add(String category, String addendum);

	/**
	 * This method need not actually be implemented.
	 *
	 * @param category what kind of thing is being removed
	 */
	void remove(String category);
}

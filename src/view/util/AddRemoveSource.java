package view.util;

import model.listeners.AddRemoveListener;

/**
 * An interface for UIs (etc.) for adding and removing items in lists.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface AddRemoveSource {
	/**
	 * Add a listener.
	 * @param list a listener to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addAddRemoveListener(final AddRemoveListener list);

	/**
	 * Remove a listener.
	 * @param list a listener to remove
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeAddRemoveListener(final AddRemoveListener list);

}

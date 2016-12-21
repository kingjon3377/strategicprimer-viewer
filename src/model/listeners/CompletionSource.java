package model.listeners;

/**
 * An interface for objects that can tell others when they've finished something.
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
public interface CompletionSource {
	/**
	 * Add a listener to the list of listeners we notify.
	 * @param list a listener to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addCompletionListener(CompletionListener list);

	/**
	 * Remove a listener from the list of listeners we notify.
	 * @param list a listener to remove
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeCompletionListener(CompletionListener list);
}

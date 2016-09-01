package model.listeners;

import java.util.EventListener;

/**
 * An interface for things that want to be called when a new map is loaded.
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
public interface MapChangeListener extends EventListener {
	/**
	 * Called when a new map is loaded. Since everything that listens for the "map"
	 * property (which this replaces) goes to its reference to a map model rather than
	 * taking the map from the event, this has no parameters.
	 */
	void mapChanged();
}

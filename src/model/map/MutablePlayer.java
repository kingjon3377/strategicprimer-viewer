package model.map;

/**
 * TODO: explain this class
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
public interface MutablePlayer extends Player {
	/**
	 * Set whether this is the current player.
	 * @param curr whether this is the current player or not
	 */
	void setCurrent(boolean curr);
}

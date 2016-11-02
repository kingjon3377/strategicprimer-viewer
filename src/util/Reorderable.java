package util;

/**
 * An interface for list-like things that can be reordered.
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
 * @author Aaron Davidson, in http://stackoverflow.com/a/4769575
 * @author Jonathan Lovelace
 */
public interface Reorderable {
	/**
	 * Move a row of a list or table from one position to another.
	 * @param fromIndex the index to remove from
	 * @param toIndex the index to move to (its index *before* removing the old!)
	 */
	void reorder(int fromIndex, int toIndex);
}

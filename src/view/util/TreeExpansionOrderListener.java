package view.util;

/**
 * An interface for classes listening for directives to expand or collapse trees.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface TreeExpansionOrderListener {
	/**
	 * Expand a tree entirely.
	 */
	void expandAll();
	/**
	 * Collapse a tree entirely.
	 */
	void collapseAll();
	/**
	 * Expand a tree to a certain level.
	 * @param levels how many levels from the root, inclusive, to expand
	 */
	void expandSome(int levels);
}

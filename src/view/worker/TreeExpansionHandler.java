package view.worker;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import view.util.TreeExpansionOrderListener;

/**
 * A class to handle "expand all", "collapse all", etc. This is part of the Strategic
 * Primer assistive programs suite developed by Jonathan Lovelace.
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
public final class TreeExpansionHandler implements TreeExpansionOrderListener {
	/**
	 * The tree we're handling
	 */
	private final JTree tree;

	/**
	 * @param theTree The tree to handle.
	 */
	public TreeExpansionHandler(final JTree theTree) {
		tree = theTree;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TreeExpansionHandler";
	}

	@Override
	public void expandAll() {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	@Override
	public void collapseAll() {
		int i = tree.getRowCount() - 1;
		while (i >= 0) {
			if (i < tree.getRowCount()) {
				tree.collapseRow(i);
			}
			i--;
		}
	}

	@Override
	public void expandSome(final int levels) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			final TreePath path = tree.getPathForRow(i);
			if ((path != null) && (path.getPathCount() <= levels)) {
				tree.expandRow(i);
			}
		}
	}
}

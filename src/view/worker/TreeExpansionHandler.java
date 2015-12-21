package view.worker;

import model.workermgmt.WorkerTreeModelAlt.KindNode;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public final class TreeExpansionHandler implements ActionListener {
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
	 * Handle menu items.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		} else if ("Expand All".equals(evt.getActionCommand())) {
			for (int i = 0; i < tree.getRowCount(); i++) {
				tree.expandRow(i);
			}
		} else if ("Collapse All".equals(evt.getActionCommand())) {
			int i = tree.getRowCount() - 1;
			while (i >= 0) {
				if (i < tree.getRowCount()) {
					tree.collapseRow(i);
				}
				i--;
			}
		} else if ("Expand Unit Kinds".equals(evt.getActionCommand()) &&
				           (tree instanceof WorkerTree)) {
			for (int i = 0; i < tree.getRowCount(); i++) {
				final TreePath path = tree.getPathForRow(i);
				if (path == null) {
					continue;
				} else if ((path.getLastPathComponent() instanceof String) ||
						           (path.getLastPathComponent() instanceof KindNode)) {
					tree.expandRow(i);
				}
			}
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TreeExpansionHandler";
	}
}

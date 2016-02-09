package view.worker;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import model.listeners.SkillSelectionListener;
import model.listeners.SkillSelectionSource;
import model.map.fixtures.mobile.worker.ISkill;
import model.workermgmt.JobTreeModel;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

/**
 * A tree representing a worker's Jobs and Skills.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
public final class JobsTree extends JTree implements SkillSelectionSource {
	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<SkillSelectionListener> ssListeners = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param jtmodel the tree model underlying this tree
	 */
	@SuppressWarnings("AssignmentToNull")
	public JobsTree(final JobTreeModel jtmodel) {
		jtmodel.setSelectionModel(NullCleaner.assertNotNull(getSelectionModel()));
		setModel(jtmodel);
		final JTree tree = this;
		jtmodel.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeStructureChanged(@Nullable final TreeModelEvent e) {
				if (e == null) {
					return;
				}
				tree.expandPath(e.getTreePath().getParentPath());
				for (int i = 0; i < getRowCount(); i++) {
					expandRow(i);
				}
			}

			@Override
			public void treeNodesRemoved(@Nullable final TreeModelEvent e) {
				// Ignored
			}

			@Override
			public void treeNodesInserted(@Nullable final TreeModelEvent e) {
				if (e == null) {
					return;
				}
				tree.expandPath(e.getTreePath());
				tree.expandPath(e.getTreePath().getParentPath());
			}

			@Override
			public void treeNodesChanged(@Nullable final TreeModelEvent e) {
				if (e == null) {
					return;
				}
				tree.expandPath(e.getTreePath().getParentPath());
			}
		});
		setRootVisible(false);
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
		setShowsRootHandles(true);
		getSelectionModel().addTreeSelectionListener(evt -> {
			if (evt != null) {
				final TreePath selPath = evt.getNewLeadSelectionPath();
				@Nullable
				final ISkill retval;
				if (selPath == null) {
					retval = null;
				} else {
					final Object component = selPath.getLastPathComponent();
					if (component instanceof ISkill) {
						retval = (ISkill) component;
					} else {
						retval = null;
					}
				}
				for (final SkillSelectionListener list : ssListeners) {
					list.selectSkill(retval);
				}
			}
		});
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addSkillSelectionListener(final SkillSelectionListener list) {
		ssListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeSkillSelectionListener(final SkillSelectionListener list) {
		ssListeners.remove(list);
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

}

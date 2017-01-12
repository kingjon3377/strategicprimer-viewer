package model.workermgmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import model.listeners.AddRemoveListener;
import model.listeners.UnitMemberListener;
import model.map.HasName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;

/**
 * A model for a tree of a worker's Jobs and Skills.
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
public final class JobTreeModel
		implements TreeModel, UnitMemberListener, AddRemoveListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(WorkerTreeModel.class);
	/**
	 * The listeners registered to listen for model changes.
	 */
	private final Collection<TreeModelListener> listeners = new ArrayList<>();
	/**
	 * The worker who the Jobs and Skills describe.
	 */
	@Nullable
	private IWorker root = null;
	/**
	 * The tree's selection model.
	 */
	private TreeSelectionModel tsm = new DefaultTreeSelectionModel();

	/**
	 * Get the Nth item in an iterable.
	 * @param <T>      the type of thing we want to get
	 * @param iterable an iterable
	 * @param index    the index of the item we want to return
	 * @return that item
	 */
	@SuppressWarnings("ProhibitedExceptionThrown")
	private static <T> T getFromIter(final Iterable<T> iterable, final int index) {
		final Iterator<T> iter = iterable.iterator();
		for (int i = 0; i < index; i++) {
			if (iter.hasNext()) {
				iter.next();
			} else {
				throw new ArrayIndexOutOfBoundsException("Not enough children");
			}
		}
		if (iter.hasNext()) {
			final T retval = iter.next();
			if (retval == null) {
				throw new IllegalStateException("Iterable contained null");
			}
			return retval;
		} else {
			throw new ArrayIndexOutOfBoundsException("Not enough children");
		}
	}

	/**
	 * Wrap an int in an array.
	 * @param integer an int
	 * @return an array containing it
	 */
	private static int[] arrayOfInt(final int integer) {
		return new int[]{integer};
	}

	/**
	 * Wrap an object in an array.
	 * @param obj an object
	 * @return an array containing it
	 */
	private static Object[] arrayOfObj(final Object obj) {
		return new Object[]{obj};
	}

	/**
	 * Set the selection model for the tree we're the model for.
	 *
	 * @param selectionModel the selection model
	 */
	public void setSelectionModel(final TreeSelectionModel selectionModel) {
		tsm = selectionModel;
	}

	/**
	 * The root of the tree, namely the worker.
	 * @return the root of the tree, the worker.
	 */
	@Override
	@Nullable
	public IWorker getRoot() {
		return root;
	}

	/**
	 * Get the Nth child of the given parent.
	 * @param parent an object in the tree
	 * @param index  the index of the child we want
	 * @return the specified child
	 */
	@SuppressWarnings("ProhibitedExceptionThrown")
	@Override
	public HasName getChild(@Nullable final Object parent, final int index) {
		final IWorker currRoot = root;
		if ((index >= 0) && (currRoot != null) && (parent instanceof IWorker)
					&& parent.equals(currRoot)) {
			return getFromIter(currRoot, index);
		} else if ((index >= 0) && (parent instanceof IJob)) {
			return getFromIter((IJob) parent, index);
		} else {
			throw new ArrayIndexOutOfBoundsException("Parent does not have that" +
																	" child.");
		}
	}

	/**
	 * How many children a given object has in the tree.
	 * @param parent an object in the tree
	 * @return how many children it has
	 */
	@Override
	public int getChildCount(@Nullable final Object parent) {
		if ((parent instanceof IWorker) || (parent instanceof IJob)) {
			//noinspection ConstantConditions
			assert parent != null;
			return (int) StreamSupport.stream(((Iterable<?>) parent).spliterator(),
					false).count();
		} else if (parent instanceof ISkill) {
			return 0;
		} else {
			throw new IllegalArgumentException("Not a possible member of the tree");
		}
	}

	/**
	 * Whether a given object is a leaf node.
	 * @param node a node in the tree
	 * @return whether it's a leaf node
	 */
	@Override
	public boolean isLeaf(@Nullable final Object node) {
		return !(node instanceof IWorker) && !(node instanceof IJob);
	}

	/**
	 * TODO: implement if necessary
	 * @param path     a path indicating a node
	 * @param newValue the new value for that place
	 */
	@Override
	public void valueForPathChanged(@Nullable final TreePath path,
									@Nullable final Object newValue) {
		LOGGER.severe("valueForPathChanged needs to be implemented");
	}

	/**
	 * Get the index of the given child in the given parent
	 * @param parent an object presumably in the tree
	 * @param child  something that's presumably one of its children
	 * @return which child it is, or -1 if preconditions broken
	 */
	@Override
	public int getIndexOfChild(@Nullable final Object parent,
							   @Nullable final Object child) {
		if ((parent instanceof IWorker) || (parent instanceof IJob)) {
			//noinspection ConstantConditions
			assert parent != null;
			int index = 0;
			for (final Object item : (Iterable<?>) parent) {
				if (item.equals(child)) {
					return index;
				}
				index++;
			}
			return -1;
		} else {
			return -1;
		}
	}

	/**
	 * Add a listener to the list of listeners we notify
	 * @param list something to listen for tree model changes
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void addTreeModelListener(@Nullable final TreeModelListener list) {
		if (list != null) {
			listeners.add(list);
		}
	}

	/**
	 * Stop notifying a listener.
	 * @param list something that doesn't want to listen for tree model changes anymore
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void removeTreeModelListener(@Nullable final TreeModelListener list) {
		listeners.remove(list);
	}

	/**
	 * Add a new Job.
	 * @param category what kind of thing is being added; if not a Job we ignore it
	 * @param addendum a description of what to add
	 */
	@Override
	public void add(final String category, final String addendum) {
		final IWorker currRoot = root;
		if ("job".equals(category) && (currRoot != null)) {
			final IJob job = new Job(addendum, 0);
			final int childCount = getChildCount(currRoot);
			currRoot.addJob(job);
			fireTreeNodesInserted(new TreeModelEvent(this, new TreePath(currRoot),
															arrayOfInt(childCount),
															arrayOfObj(job)));
		} else if ("skill".equals(category)) {
			final TreePath selPath = tsm.getSelectionPath();
			if ((selPath != null)
						&& (selPath.getLastPathComponent() instanceof IJob)) {
				final IJob job = (IJob) selPath.getLastPathComponent();
				final ISkill skill = new Skill(addendum, 0, 0);
				final int childCount = getChildCount(job);
				job.addSkill(skill);
				fireTreeNodesInserted(
						new TreeModelEvent(this, new TreePath(new Object[]{root, job}),
												  arrayOfInt(childCount),
												  arrayOfObj(skill)));
			}
		}
	}

	/**
	 * Change what unit member is currently selected.
	 * @param old      the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
							   @Nullable final UnitMember selected) {
		if (selected instanceof IWorker) {
			root = (IWorker) selected;
			fireTreeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
		} else {
			root = null;
			fireTreeStructureChanged(new TreeModelEvent(this, (TreePath) null));
		}
	}

	/**
	 * Send an event to all listeners.
	 *
	 * @param event the event to send
	 */
	private void fireTreeNodesInserted(final TreeModelEvent event) {
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
	}

	/**
	 * Send an event to all listeners.
	 *
	 * @param event the event to send
	 */
	private void fireTreeStructureChanged(final TreeModelEvent event) {
		for (final TreeModelListener listener : listeners) {
			listener.treeStructureChanged(event);
		}
	}

	/**
	 * A simple toString().
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final String workerString = String.valueOf(root);
		return String.format("JobTreeModel showing worker %s", workerString);
	}
}

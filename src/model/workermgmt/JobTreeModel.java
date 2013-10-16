package model.workermgmt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import model.listeners.AddRemoveListener;
import model.listeners.UnitMemberListener;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A model for a tree of a worker's Jobs and Skills.
 *
 * @author Jonathan Lovelace
 *
 */
public class JobTreeModel implements TreeModel, UnitMemberListener,
		AddRemoveListener {
	/**
	 * Constructor.
	 *
	 * @param selModel the tree's selection model.
	 */
	public JobTreeModel(final TreeSelectionModel selModel) {
		tsm = selModel;
	}

	/**
	 * The worker who the Jobs and Skills describe.
	 */
	@Nullable
	private Worker root; // NOPMD: Claims only initialized in constructor, which
							// is Not True.
	/**
	 * The tree's selection model.
	 */
	private final TreeSelectionModel tsm;

	/**
	 * @return the root of the tree, the worker.
	 */
	@Override
	@Nullable
	public Object getRoot() {
		return root;
	}

	/**
	 * @param parent an object in the tree
	 * @param index the index of the child we want
	 * @return the specified child
	 */
	@Override
	public Object getChild(@Nullable final Object parent, final int index) {
		final Worker currRoot = root;
		if (index >= 0 && currRoot != null && parent instanceof Worker
				&& parent.equals(currRoot)) {
			return getFromIter(currRoot, index);
		} else if (index >= 0 && parent instanceof Job) {
			return getFromIter((Job) parent, index);
		} else {
			throw new ArrayIndexOutOfBoundsException(
					"Parent does not have that child.");
		}
	}

	/**
	 * @param <T> the type of thing we want to get
	 * @param iterable an iterable
	 * @param index the index of the item we want to return
	 * @return that item, or null if there aren't enough items
	 */
	private static <T> T getFromIter(final Iterable<T> iterable, final int index) {
		final Iterator<T> iter = iterable.iterator();
		for (int i = 0; i < index; i++) {
			if (iter.hasNext()) {
				// ESCA-JAVA0282:
				iter.next();
			} else {
				throw new ArrayIndexOutOfBoundsException(
						"Parent does not have that many children");
			}
		}
		if (iter.hasNext()) {
			return iter.next(); // NOPMD
		} else {
			throw new ArrayIndexOutOfBoundsException(
					"Parent does not have that many children");
		}
	}

	/**
	 * @param parent an object in the tree
	 * @return how many children it has
	 */
	@Override
	public int getChildCount(@Nullable final Object parent) {
		if (parent instanceof Worker || parent instanceof Job) {
			assert parent != null;
			final Iterator<?> iter = ((Iterable<?>) parent).iterator();
			int count = 0;
			// ESCA-JAVA0254:
			while (iter.hasNext()) {
				count++;
				iter.next();
			}
			return count; // NOPMD
		} else if (parent instanceof Skill) {
			return 0; // NOPMD
		} else {
			throw new IllegalArgumentException(
					"Not a possible member of the tree");
		}
	}

	/**
	 * @param node a node in the tree
	 * @return whether it's a leaf node
	 */
	@Override
	public boolean isLeaf(@Nullable final Object node) {
		return !(node instanceof Worker) && !(node instanceof Job);
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(WorkerTreeModel.class
			.getName());

	/**
	 *
	 * @param path a path indicating a node
	 * @param newValue the new value for that place
	 *
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 *      java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(@Nullable final TreePath path,
			@Nullable final Object newValue) {
		LOGGER.severe("valueForPathChanged needs to be implemented");
	}

	/**
	 * @param parent an object presumably in the tree
	 * @param child something that's presumably one of its children
	 * @return which child it is, or -1 if preconditions broken
	 *
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(@Nullable final Object parent,
			@Nullable final Object child) {
		if (parent instanceof Worker || parent instanceof Job) {
			int index = 0;
			assert parent != null;
			for (Object item : (Iterable<?>) parent) {
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
	 * @param list something to listen for tree model changes
	 */
	@Override
	public void addTreeModelListener(@Nullable final TreeModelListener list) {
		listeners.add(list);
	}

	/**
	 * @param list something that doesn't want to listen for tree model changes
	 *        anymore
	 */
	@Override
	public void removeTreeModelListener(@Nullable final TreeModelListener list) {
		listeners.remove(list);
	}

	/**
	 * The listeners registered to listen for model changes.
	 */
	private final List<TreeModelListener> listeners = new ArrayList<>();

	/**
	 * @param category what kind of thing is being added; if not a Job we ignore
	 *        it
	 * @param addendum a description of what to add
	 */
	@Override
	public void add(final String category, final String addendum) {
		final Worker currRoot = root;
		if ("job".equals(category) && currRoot != null) {
			final Job job = new Job(addendum, 0);
			final int childCount = getChildCount(currRoot);
			currRoot.addJob(job);
			fireTreeNodesInserted(new TreeModelEvent(this, new TreePath(
					currRoot), arrayOfInt(childCount), arrayOfObj(job)));
		} else if ("skill".equals(category)) {
			final TreePath selPath = tsm.getSelectionPath();
			if (selPath != null
					&& selPath.getLastPathComponent() instanceof Job) {
				final Job job = (Job) selPath.getLastPathComponent();
				final Skill skill = new Skill(addendum, 0, 0);
				final int childCount = getChildCount(job);
				job.addSkill(skill);
				fireTreeNodesInserted(new TreeModelEvent(this, new TreePath(
						new Object[] { root, job }), arrayOfInt(childCount),
						arrayOfObj(skill)));
			}
		}
	}

	/**
	 * @param old the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
			@Nullable final UnitMember selected) {
		if (selected instanceof Worker) {
			root = (Worker) selected;
		} else {
			root = null;
		}
		fireTreeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
	}

	/**
	 * @param integer an int
	 * @return an array containing it
	 */
	private static int[] arrayOfInt(final int integer) {
		return new int[] { integer };
	}

	/**
	 * @param obj an object
	 * @return an array containing it
	 */
	private static Object[] arrayOfObj(final Object obj) {
		return new Object[] { obj };
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
	 * @param category ignored
	 */
	@Override
	public void remove(final String category) {
		// Not implemented
	}
}

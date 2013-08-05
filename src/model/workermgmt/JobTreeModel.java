package model.workermgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
/**
 * A model for a tree of a worker's Jobs and Skills.
 * @author Jonathan Lovelace
 *
 */
public class JobTreeModel implements TreeModel, PropertyChangeListener {
	/**
	 * Constructor.
	 * @param selModel the tree's selection model.
	 */
	public JobTreeModel(final TreeSelectionModel selModel) {
		tsm = selModel;
	}
	/**
	 * The worker who the Jobs and Skills describe.
	 */
	private Worker root = null;
	/**
	 * The tree's selection model.
	 */
	private final TreeSelectionModel tsm;
	/**
	 * @return the root of the tree, the worker.
	 */
	@Override
	public Object getRoot() {
		return root;
	}
	/**
	 * @param parent an object in the tree
	 * @param index the index of the child we want
	 * @return the specified child
	 */
	@Override
	public Object getChild(final Object parent, final int index) {
		if (index < 0 || parent == null) {
			return null; // NOPMD
		} else if (parent instanceof Worker && parent.equals(root)) {
			final Iterator<Job> iter = root.iterator();
			for (int i = 0; i < index; i++) {
				if (iter.hasNext()) {
					iter.next();
				} else {
					return null; // NOPMD
				}
			}
			if (iter.hasNext()) {
				return iter.next(); // NOPMD
			} else {
				return null; // NOPMD
			}
		} else if (parent instanceof Job) {
			final Iterator<Skill> iter = ((Job) parent).iterator();
			for (int i = 0; i < index; i++) {
				if (iter.hasNext()) {
					iter.next(); // NOPMD
				} else {
					return null; // NOPMD
				}
			}
			if (iter.hasNext()) {
				return iter.next(); // NOPMD
			} else {
				return null; // NOPMD
			}
		} else {
			return null;
		}
	}
	/**
	 * @param parent an object in the tree
	 * @return how many children it has
	 */
	@Override
	public int getChildCount(final Object parent) {
		if (parent instanceof Worker || parent instanceof Job) {
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
			throw new IllegalArgumentException("Not a possible member of the tree");
		}
	}
	/**
	 * @param node a node in the tree
	 * @return whether it's a leaf node
	 */
	@Override
	public boolean isLeaf(final Object node) {
		return !(node instanceof Worker) && !(node instanceof Job);
	}
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(WorkerTreeModel.class.getName());
	/**
	 *
	 * @param path a path indicating a node
	 * @param newValue the new value for that place
	 *
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		LOGGER.severe("valueForPathChanged needs to be implemented");
	}
	/**
	 * @param parent an object presumably in the tree
	 * @param child something that's presumably one of its children
	 * @return which child it is, or -1 if preconditions broken
	 *
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		if (parent instanceof Worker ||  parent instanceof Job) {
			int index = 0;
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
	public void addTreeModelListener(final TreeModelListener list) {
		listeners.add(list);
	}
	/**
	 * @param list something that doesn't want to listen for tree model changes anymore
	 */
	@Override
	public void removeTreeModelListener(final TreeModelListener list) {
		listeners.remove(list);
	}
	/**
	 * The listeners registered to listen for model changes.
	 */
	private final List<TreeModelListener> listeners = new ArrayList<>();
	/**
	 * Handle a property change.
	 * @param evt the even to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("member".equalsIgnoreCase(evt.getPropertyName())) {
			if (evt.getNewValue() instanceof Worker) {
				root = (Worker) evt.getNewValue();
				fireTreeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
			} else if (evt.getNewValue() instanceof UnitMember || evt.getNewValue() == null) {
				root = null;
				fireTreeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
			}
		} else if ("add_job".equalsIgnoreCase(evt.getPropertyName()) && root != null) {
			final Job job = new Job(evt.getNewValue().toString(), 0);
			root.addJob(job);
			fireTreeNodesInserted(new TreeModelEvent(this, new TreePath(root), arrayOfInt(getChildCount(root)), arrayOfObj(job)));
		} else if ("add_skill".equalsIgnoreCase(evt.getPropertyName())) {
			final TreePath selPath = tsm.getSelectionPath();
			if (selPath != null && selPath.getLastPathComponent() instanceof Job) {
				final Job job = (Job) selPath.getLastPathComponent();
				final Skill skill = new Skill(evt.getNewValue().toString(), 0, 0);
				job.addSkill(skill);
				fireTreeNodesInserted(new TreeModelEvent(this, new TreePath(
						new Object[] { root, job }),
						arrayOfInt(getChildCount(job)), arrayOfObj(skill)));
			} // Need to handle added skills ... at least firing
				// notification of changes to the tree, and on level-up as well.
		}
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
	 * @param event the event to send
	 */
	private void fireTreeNodesInserted(final TreeModelEvent event) {
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
	}
	/**
	 * Send an event to all listeners.
	 * @param event the event to send
	 */
	private void fireTreeStructureChanged(final TreeModelEvent event) {
		for (final TreeModelListener listener : listeners) {
			listener.treeStructureChanged(event);
		}
	}
}

package model.workermgmt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
/**
 * A TreeModel implementation for a player's units and workers.
 * @author Jonathan Lovelace
 *
 */
public class WorkerTreeModel implements IWorkerTreeModel {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(WorkerTreeModel.class.getName());
	/**
	 * Constructor.
	 * @param player the player whose units and workers will be shown in the tree
	 * @param wmodel the driver model
	 */
	public WorkerTreeModel(final Player player, final IWorkerModel wmodel) {
		root = player;
		model = wmodel;
	}
	/**
	 * The player to whom the units and workers belong, the root of the tree.
	 */
	private final Player root;
	/**
	 * The driver model.
	 */
	private final IWorkerModel model;
	/**
	 * @return the root of the tree, the player.
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
		if (index < 0) {
			return null; // NOPMD
		} else if (parent instanceof Player && parent.equals(root)
				&& index < model.getUnits(root).size()) {
			return model.getUnits(root).get(index); // NOPMD
		} else if (parent instanceof Unit) {
			final Iterator<UnitMember> iter = ((Unit) parent).iterator();
			for (int i = 0; i < index; i++) {
				if (iter.hasNext()) {
					// ESCA-JAVA0282:
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
		if (parent instanceof Player) {
			return model.getUnits((Player) parent).size(); // NOPMD
		} else if (parent instanceof Unit) {
			final Iterator<UnitMember> iter = ((Unit) parent).iterator();
			int count = 0;
			// ESCA-JAVA0254:
			while (iter.hasNext()) {
				count++;
				iter.next();
			}
			return count;
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
		return !(node instanceof Player) && !(node instanceof Unit);
	}
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
		if (parent instanceof Player && parent.equals(root)) {
			return model.getUnits(root).contains(child) ? model.getUnits(root)//NOPMD
					.indexOf(child) : -1;
		} else if (parent instanceof Unit) {
			int index = 0;
			for (final UnitMember member : (Unit) parent) {
				if (member.equals(child)) {
					return index; // NOPMD
				}
				index++;
			}
			return -1; // NOPMD
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
	private final List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	/**
	 * Move a member between units.
	 * @param member a unit member
	 * @param old the prior owner
	 * @param newOwner the new owner
	 */
	@Override
	public void moveMember(final UnitMember member, final Unit old, final Unit newOwner) {
		final int oldIndex = getIndexOfChild(old, member);
		old.removeMember(member);
		final TreeModelEvent removedEvent = new TreeModelEvent(this,
				new TreePath(new Object[] { root, old }), new int[] { oldIndex },
				new Object[] { member });
		final TreeModelEvent removedChangedEvent = new TreeModelEvent(this, new TreePath(new Object[] { root, old }));
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesRemoved(removedEvent); // FIXME: Somehow removed nodes are still visible!
			listener.treeStructureChanged(removedChangedEvent);
		}
		newOwner.addMember(member);
//		final int newIndex = getIndexOfChild(member, newOwner);
//		final TreeModelEvent insertedEvent = new TreeModelEvent(this,
//				new TreePath(new Object[] { root, newOwner }),
//				new int[] { newIndex }, new Object[] { member });
		final TreeModelEvent insertedChangedEvent = new TreeModelEvent(this, new TreePath(new Object[] { root, newOwner }));
		for (final TreeModelListener listener : listeners) {
//			listener.treeNodesInserted(insertedEvent);
			listener.treeStructureChanged(insertedChangedEvent);
		}
	}
}

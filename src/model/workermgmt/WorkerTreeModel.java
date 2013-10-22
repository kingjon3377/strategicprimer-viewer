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

import org.eclipse.jdt.annotation.Nullable;

import util.TypesafeLogger;

/**
 * A TreeModel implementation for a player's units and workers.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerTreeModel implements IWorkerTreeModel {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(WorkerTreeModel.class);

	/**
	 * Constructor.
	 *
	 * @param player the player whose units and workers will be shown in the
	 *        tree
	 * @param wmodel the driver model
	 */
	public WorkerTreeModel(final Player player, final IWorkerModel wmodel) {
		root = player;
		model = wmodel;
	}

	/**
	 * The player to whom the units and workers belong, the root of the tree.
	 */
	private Player root;
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
	public Object getChild(@Nullable final Object parent, final int index) {
		if (index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		} else if (parent instanceof Player && parent.equals(root)
				&& index < model.getUnits(root).size()) {
			final Unit unit = model.getUnits(root).get(index);
			assert unit != null;
			return unit; // NOPMD
		} else if (parent instanceof Unit) {
			final Iterator<UnitMember> iter = ((Unit) parent).iterator();
			for (int i = 0; i < index; i++) {
				if (iter.hasNext()) {
					// ESCA-JAVA0282:
					iter.next();
				} else {
					throw new ArrayIndexOutOfBoundsException(index);
				}
			}
			if (iter.hasNext()) {
				final UnitMember next = iter.next();
				assert next != null;
				return next; // NOPMD
			} else {
				throw new ArrayIndexOutOfBoundsException(index);
			}
		} else {
			throw new ArrayIndexOutOfBoundsException("Unrecognized parent");
		}
	}

	/**
	 * @param parent an object in the tree
	 * @return how many children it has
	 */
	@Override
	public int getChildCount(@Nullable final Object parent) {
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
		return !(node instanceof Player) && !(node instanceof Unit);
	}

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
		if (parent instanceof Player && parent.equals(root)) {
			return model.getUnits(root).contains(child) ? model.getUnits(root)// NOPMD
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
	 * Move a member between units.
	 *
	 * @param member a unit member
	 * @param old the prior owner
	 * @param newOwner the new owner
	 */
	@Override
	public void moveMember(final UnitMember member, final Unit old,
			final Unit newOwner) {
		final int oldIndex = getIndexOfChild(old, member);
		old.removeMember(member);
		final TreeModelEvent removedEvent = new TreeModelEvent(this,
				new TreePath(new Object[] { root, old }),
				new int[] { oldIndex }, new Object[] { member });
		final TreeModelEvent removedChEvent = new TreeModelEvent(this,
				new TreePath(new Object[] { root, old }));
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesRemoved(removedEvent);
			// FIXME: Somehow removed nodes are still visible!
			listener.treeStructureChanged(removedChEvent);
		}
		newOwner.addMember(member);
		// final int newIndex = getIndexOfChild(member, newOwner);
		// final TreeModelEvent insertedEvent = new TreeModelEvent(this,
		// new TreePath(new Object[] { root, newOwner }),
		// new int[] { newIndex }, new Object[] { member });
		final TreeModelEvent insertedChEvent = new TreeModelEvent(this,
				new TreePath(new Object[] { root, newOwner }));
		for (final TreeModelListener listener : listeners) {
			// listener.treeNodesInserted(insertedEvent);
			listener.treeStructureChanged(insertedChEvent);
		}
	}

	/**
	 * Add a unit.
	 *
	 * @param unit the unit to add
	 */
	@Override
	public void addUnit(final Unit unit) {
		model.addUnit(unit);
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesInserted(new TreeModelEvent(this, new TreePath(// NOPMD
					root), singletonInt(model.getUnits(root).size()),
					singletonObj(unit)));
		}
	}

	/**
	 * Create a singleton array.
	 *
	 * @param obj the object it should contain
	 * @return the array
	 */
	private static Object[] singletonObj(final Object obj) {
		return new Object[] { obj };
	}

	/**
	 * Create a singleton array.
	 *
	 * @param num the integer it should contain
	 * @return the array
	 */
	private static int[] singletonInt(final int num) {
		return new int[] { num };
	}

	/**
	 * Handle the user's request to add a unit.
	 *
	 * @param unit the unit to add.
	 */
	@Override
	public void addNewUnit(final Unit unit) {
		addUnit(unit);
	}

	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public void mapChanged() {
		root = model.getMap().getPlayers().getCurrentPlayer();
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesChanged(new TreeModelEvent(this, new TreePath(
					root)));
		}
	}

	/**
	 * @param old the old current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		root = newPlayer;
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesChanged(new TreeModelEvent(this, new TreePath(
					root)));
		}
	}

	/**
	 * @param obj an object
	 * @return it
	 */
	@Override
	public Object getModelObject(final Object obj) {
		return obj;
	}

	/**
	 * Add a member to a unit.
	 *
	 * @param unit the unit to contain the member
	 * @param member the member to add to it
	 */
	@Override
	public void addUnitMember(final Unit unit, final UnitMember member) {
		unit.addMember(member);
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesInserted(new TreeModelEvent(this, new TreePath(
					new Object[] { root, unit }), new int[] { getIndexOfChild(
					unit, member) }, new Object[] { member }));
		}
	}
}

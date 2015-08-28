package model.workermgmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasKind;
import model.map.HasName;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import util.EnumerationWrapper;
import util.NullCleaner;

/**
 * An alternative implementation of the worker tree model.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerTreeModelAlt extends DefaultTreeModel implements
		IWorkerTreeModel {
	/**
	 * The driver model.
	 */
	protected final IWorkerModel model;

	/**
	 * A list of unit members that have been dismissed.
	 */
	private final List<UnitMember> dismissedMembers = new ArrayList<>();
	/**
	 * Constructor.
	 *
	 * @param player the player whose units and workers will be shown in the
	 *        tree
	 * @param wmodel the driver model
	 */
	public WorkerTreeModelAlt(final Player player, final IWorkerModel wmodel) {
		super(new PlayerNode(player, wmodel), true);
		model = wmodel;
	}

	/**
	 * Move a member between units.
	 *
	 * @param member a unit member
	 * @param old the prior owner
	 * @param newOwner the new owner
	 */
	@Override
	public final void moveMember(final UnitMember member, final IUnit old,
			final IUnit newOwner) {
		final PlayerNode pnode = (PlayerNode) root;
		assert pnode != null;
		final UnitNode oldNode =
				NullCleaner.assertNotNull((UnitNode) getNode(pnode, old));
		final UnitNode newNode =
				NullCleaner.assertNotNull((UnitNode) getNode(pnode, newOwner));
		final UnitMemberNode node = (UnitMemberNode) getNode(pnode, member);
		fireTreeNodesRemoved(this, new Object[] { pnode,
				getNode(old.getKind()), oldNode },
				new int[] { oldNode.getIndex(node) }, new Object[] { node });
		oldNode.remove(node);
		old.removeMember(member);
		newNode.add(node);
		fireTreeNodesInserted(this,
				new Object[] { pnode, getNode(newOwner.getKind()), newNode },
				new int[] { newNode.getIndex(node) }, new Object[] { node });
		newOwner.addMember(member);
	}
	/**
	 * A base class for our nodes.
	 */
	public static class WorkerTreeNode extends
			DefaultMutableTreeNode implements Iterable<TreeNode> {
		/**
		 * @param userObj the user object the node wraps
		 * @param permitsChildren whether to allow children
		 */
		protected WorkerTreeNode(final Object userObj,
				final boolean permitsChildren) {
			super(userObj, permitsChildren);
		}
		/**
		 * Allows children without having to pass that to us.
		 * @param userObj the user object the node wraps.
		 */
		protected WorkerTreeNode(final Object userObj) {
			super(userObj, true);
		}
		/**
		 * @return an iterator over the immediate children of this node
		 */
		@Override
		public Iterator<TreeNode> iterator() {
			return new EnumerationWrapper<>(children());
		}
	}
	/**
	 * A node representing the player.
	 * @author Jonathan Lovelace
	 */
	public static class PlayerNode extends WorkerTreeNode {
		/**
		 * Constructor.
		 *
		 * @param player the player the node represents
		 * @param model the worker model we're drawing from
		 */
		public PlayerNode(final Player player, final IWorkerModel model) {
			super(player);
			final List<String> kinds = model.getUnitKinds(player);
			int index = 0;
			for (final String kind : kinds) {
				insert(new KindNode(kind, model.getUnits(player, kind)), index);// NOPMD
				index++;
			}
		}
	}
	/**
	 * A node representing a kind of unit.
	 * @author Jonathan Lovelace
	 */
	public static class KindNode extends WorkerTreeNode {
		/**
		 * Constructor.
		 * @param kind what kind of unit
		 * @param units the units of this kind
		 */
		public KindNode(final String kind, final List<IUnit> units) {
			super(kind);
			int index = 0;
			for (final IUnit unit : units) {
				insert(new UnitNode(unit), index); // NOPMD
				index++;
			}
		}
	}
	/**
	 * A node representing a unit.
	 * @author Jonathan Lovelace
	 */
	public static class UnitNode extends WorkerTreeNode {
		/**
		 * Constructor.
		 *
		 * @param unit the unit we represent.
		 */
		public UnitNode(final IUnit unit) {
			super(unit);
			int index = 0;
			for (final UnitMember member : unit) {
				insert(new UnitMemberNode(member), index); // NOPMD
				index++;
			}
		}
	}

	/**
	 * A node representing a unit member.
	 * @author Jonathan Lovelace
	 */
	public static class UnitMemberNode extends WorkerTreeNode {
		/**
		 * Constructor.
		 *
		 * @param member the unit member we represent.
		 */
		public UnitMemberNode(final UnitMember member) {
			super(member, false);
		}
	}

	/**
	 * Add a unit.
	 *
	 * @param unit the unit to add
	 */
	@Override
	public final void addUnit(final IUnit unit) {
		model.addUnit(unit);
		final UnitNode node = new UnitNode(unit);
		final String kind = unit.getKind();
		for (final TreeNode child : (PlayerNode) root) {
			if (child instanceof KindNode
					&& kind.equals(((KindNode) child).getUserObject())) {
				((KindNode) child).add(node);
				fireTreeNodesInserted(this, getPathToRoot(child),
						new int[] { child.getChildCount() - 1 },
						new Object[] { node });
				break;
			}
		}
	}

	/**
	 * Handle the user's desire to add a new unit.
	 *
	 * @param unit the unit to add
	 */
	@Override
	public final void addNewUnit(final IUnit unit) {
		addUnit(unit);
	}

	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public final void mapChanged() {
		setRoot(new PlayerNode(model.getMap().getCurrentPlayer(),
				model));
	}

	/**
	 * @param old the old current player
	 * @param newPlayer the new current player
	 */
	@Override
	public final void playerChanged(@Nullable final Player old,
			final Player newPlayer) {
		setRoot(new PlayerNode(newPlayer, model));
	}

	/**
	 * @param obj an object
	 * @return the model object it contains if it's a node, otherwise the object
	 *         itself
	 */
	@Override
	public final Object getModelObject(final Object obj) {
		if (obj instanceof DefaultMutableTreeNode) {
			return NullCleaner.assertNotNull(((DefaultMutableTreeNode) obj) // NOPMD
					.getUserObject());
		} else {
			return obj;
		}
	}

	/**
	 * Add a member to a unit.
	 *
	 * @param unit the unit to contain the member
	 * @param member the member to add to it
	 */
	@Override
	public final void addUnitMember(final IUnit unit, final UnitMember member) {
		final PlayerNode pnode = (PlayerNode) root;
		UnitNode unode = null;
		for (final TreeNode item : pnode) {
			if (item instanceof UnitNode
					&& ((UnitNode) item).getUserObject().equals(unit)) {
				unode = (UnitNode) item;
				break;
			}
		}
		if (unode == null) {
			return;
		}
		unit.addMember(member);
		final UnitMemberNode newNode = new UnitMemberNode(member);
		unode.add(newNode);
		fireTreeNodesInserted(this, new Object[] { root, unode },
				new int[] { unode.getChildCount() - 1 },
				new Object[] { newNode });
	}
	/**
	 * TODO: Specify which player.
	 * @return a String representation of the object
	 */
	@Override
	public final String toString() {
		return "WorkerTreeModelAlt";
	}
	/**
	 * @param obj a model object
	 * @return the node representing it, or null if it isn't in the tree
	 */
	@Nullable
	public MutableTreeNode getNode(final Object obj) {
		final TreeNode localRoot = root;
		if (localRoot != null) {
			return getNode(localRoot, obj);
		} else {
			return null;
		}
	}

	/**
	 * @param node
	 *            a node
	 * @param obj
	 *            an object
	 * @return the node in the subtree under the node representing the object,
	 *         or null if it isn't in this subtree
	 */
	@Nullable
	private static MutableTreeNode getNode(final TreeNode node, final Object obj) {
		if (node instanceof MutableTreeNode && objectEquals(node, obj)) {
			return (MutableTreeNode) node;
		} else if (node instanceof WorkerTreeNode && node.getAllowsChildren()) {
			for (final TreeNode child : (WorkerTreeNode) node) {
				@Nullable final MutableTreeNode result = getNode(child, obj);
				if (result != null) {
					return result;
				}
			}
			return null;
		} else {
			return null;
		}
	}
	/**
	 * @param node a node
	 * @param obj an object
	 * @return whether the object is or equals the node's user-object
	 */
	private static boolean objectEquals(final TreeNode node, final Object obj) {
		return node instanceof DefaultMutableTreeNode
				&& (obj == ((DefaultMutableTreeNode) node).getUserObject() || obj
						.equals(((DefaultMutableTreeNode) node).getUserObject()));
	}
	/**
	 * @param item the newly renamed item
	 */
	@Override
	public void renameItem(final HasName item) {
		final TreeNode node = getNode(item);
		if (node == null) {
			return;
		}
		final TreeNode[] path = getPathToRoot(node);
		final int index = getIndexOfChild(path[path.length - 2], node);
		fireTreeNodesChanged(this, path, new int[] { index }, new Object[] { node });
	}
	/**
	 * @param item the item to move to this point in the tree
	 */
	@Override
	public void moveItem(final HasKind item) {
		if (item instanceof UnitMember) {
			final TreeNode node = getNode(item);
			if (node == null) {
				return;
			}
			final TreeNode[] path = getPathToRoot(node);
			final int index = getIndexOfChild(path[path.length - 1], node);
			fireTreeNodesChanged(this, path, new int[] { index },
					new Object[] { node });
			// FIXME: We don't actually move unit members here!
		} else if (item instanceof IUnit) {
			final TreeNode node = getNode(item);
			if (node == null) {
				return;
			}
			final TreeNode[] pathOne = getPathToRoot(node);
			final int indexOne = getIndexOfChild(pathOne[pathOne.length - 2], node);
			TreeNode nodeTwo = null;
			for (final TreeNode child : (PlayerNode) root) {
				if (child instanceof KindNode
						&& item.getKind().equals(
								((KindNode) child).getUserObject())) {
					nodeTwo = child;
					break;
				}
			}
			((MutableTreeNode) pathOne[pathOne.length - 1]).removeFromParent();
			final Object[] pathSubset;
			if (pathOne[pathOne.length - 2].getChildCount() == 0) {
				final int parentIndex =
						pathOne[pathOne.length - 3]
								.getIndex(pathOne[pathOne.length - 2]);
				pathSubset = Arrays.copyOf(pathOne, pathOne.length - 2);
				((MutableTreeNode) pathOne[pathOne.length - 2])
						.removeFromParent();
				fireTreeNodesRemoved(this, pathSubset,
						new int[] { parentIndex },
						new Object[] { pathOne[pathOne.length - 2] });
			} else {
				pathSubset = Arrays.copyOf(pathOne, pathOne.length - 1);
				fireTreeNodesRemoved(this, pathSubset, new int[] { indexOne },
					new Object[] { node });
			}
			if (nodeTwo == null) {
				nodeTwo =
						new KindNode(item.getKind(), new ArrayList<>(
								Collections.singletonList((IUnit) item)));
				((PlayerNode) root).add((MutableTreeNode) nodeTwo);
				fireTreeNodesInserted(this, new TreeNode[] { root },
						new int[] { getIndexOfChild(root, nodeTwo) },
						new Object[] { nodeTwo });
			} else {
				final int indexTwo = nodeTwo.getChildCount();
				((MutableTreeNode) nodeTwo).insert((MutableTreeNode) node,
						indexTwo);
				fireTreeNodesInserted(this, new Object[] { root, nodeTwo },
						new int[] { indexTwo }, new Object[] { node });
			}

		}
	}
	/**
	 * @param member the member to dismiss
	 */
	@Override
	public void dismissUnitMember(final UnitMember member) {
		final TreeNode node = getNode(member);
		if (node == null) {
			return;
		}
		final TreeNode parentNode = node.getParent();
		if (!(parentNode instanceof UnitNode)) {
			throw new IllegalStateException("Unexpected tree state");
		}
		final TreeNode[] path = getPathToRoot(node);
		final int index = getIndexOfChild(path[path.length - 1], node);
		((UnitNode) parentNode).remove((MutableTreeNode) node);
		fireTreeNodesRemoved(this, path, new int[] { index },
				new Object[] { node });
		dismissedMembers.add(member);
		((Unit) ((UnitNode) parentNode).getUserObject()).removeMember(member);
	}
	/**
	 * @return an iteration over the unit-members the user has dismissed.
	 */
	@Override
	public Iterable<UnitMember> dismissed() {
		return dismissedMembers;
	}
}

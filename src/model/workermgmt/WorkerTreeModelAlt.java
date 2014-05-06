package model.workermgmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import model.map.HasKind;
import model.map.HasName;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;

import org.eclipse.jdt.annotation.Nullable;

import util.IteratorWrapper;
import util.NullCleaner;

/**
 * An alternative implementation of the worker tree model.
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
	public final void moveMember(final UnitMember member, final Unit old,
			final Unit newOwner) {
		final PlayerNode pnode = (PlayerNode) root;
		final List<Unit> units =
				model.getUnits(NullCleaner.assertNotNull((Player) pnode
						.getUserObject()));
		final UnitNode oldNode = (UnitNode) pnode
				.getChildAt(units.indexOf(old));
		final UnitNode newNode = (UnitNode) pnode.getChildAt(units
				.indexOf(newOwner));
		final Iterable<TreeNode> iter = new IteratorWrapper<>(
				new EnumerationWrapper<TreeNode>(oldNode.children()));
		int index = -1;
		for (final TreeNode node : iter) {
			if (node instanceof UnitMemberNode
					&& ((UnitMemberNode) node).getUserObject().equals(member)) {
				index = oldNode.getIndex(node);
			}
		}
		final UnitMemberNode node = (UnitMemberNode) oldNode.getChildAt(index);
		oldNode.remove(node);
		fireTreeNodesRemoved(this, new Object[] { pnode, oldNode },
				new int[] { index }, new Object[] { node });
		old.removeMember(member);
		newNode.add(node);
		fireTreeNodesInserted(this, new Object[] { pnode, newNode },
				new int[] { newNode.getIndex(node) }, new Object[] { node });
		newOwner.addMember(member);
	}

	/**
	 * A node representing the player.
	 * @author Jonathan Lovelace
	 */
	public static class PlayerNode extends DefaultMutableTreeNode {
		/**
		 * Constructor.
		 *
		 * @param player the player the node represents
		 * @param model the worker model we're drawing from
		 */
		public PlayerNode(final Player player, final IWorkerModel model) {
			super(player, true);
			final List<String> kinds = model.getUnitKinds(player);
			int index = 0;
			for (final String kind : kinds) {
				if (kind != null) {
					insert(new KindNode(kind, model.getUnits(player, kind)), // NOPMD
							index);
					index++;
				}
			}
		}
	}
	/**
	 * A node representing a kind of unit.
	 * @author Jonathan Lovelace
	 */
	public static class KindNode extends DefaultMutableTreeNode {
		/**
		 * Constructor.
		 * @param kind what kind of unit
		 * @param units the units of this kind
		 */
		public KindNode(final String kind, final List<Unit> units) {
			super(kind, true);
			int index = 0;
			for (final Unit unit : units) {
				if (unit != null) {
					insert(new UnitNode(unit), index); // NOPMD
					index++;
				}
			}
		}
	}
	/**
	 * A node representing a unit.
	 * @author Jonathan Lovelace
	 */
	public static class UnitNode extends DefaultMutableTreeNode {
		/**
		 * Constructor.
		 *
		 * @param unit the unit we represent.
		 */
		public UnitNode(final Unit unit) {
			super(unit, true);
			int index = 0;
			for (final UnitMember member : unit) {
				if (member != null) {
					insert(new UnitMemberNode(member), index); // NOPMD
					index++;
				}
			}
		}
	}

	/**
	 * A node representing a unit member.
	 * @author Jonathan Lovelace
	 */
	public static class UnitMemberNode extends DefaultMutableTreeNode {
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
	 * A wrapper around an Enumeration to make it fit the Iterable interface.
	 *
	 * @param <T> the type parameter
	 * @author Jonathan Lovelace
	 */
	public static final class EnumerationWrapper<T> implements Iterator<T> {
		/**
		 * The object we're wrapping.
		 */
		private final Enumeration<T> wrapped;

		/**
		 * @param enumer the object we're wrapping.
		 */
		public EnumerationWrapper(@Nullable final Enumeration<T> enumer) {
			if (enumer == null) {
				wrapped = new Enumeration<T>() {
					@Override
					public boolean hasMoreElements() {
						return false;
					}

					@Override
					public T nextElement() {
						throw new NoSuchElementException(
								"No elements in empty enumeration (replacing null)");
					}
				};
			} else {
				wrapped = enumer;
			}
		}

		/**
		 * @return whether there are more elements
		 */
		@Override
		public boolean hasNext() {
			return wrapped.hasMoreElements();
		}

		/**
		 * @return the next element
		 * @throws NoSuchElementException if no more elements
		 */
		// ESCA-JAVA0126:
		// ESCA-JAVA0277:
		@Nullable
		@Override
		public T next() throws NoSuchElementException { // NOPMD: throws clause
														// required by
														// superclass
			return wrapped.nextElement();
		}

		/**
		 * Not supported.
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Not supported by Enumeration");
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "EnumerationWrapper";
		}
	}

	/**
	 * Add a unit.
	 *
	 * @param unit the unit to add
	 */
	@Override
	public final void addUnit(final Unit unit) {
		model.addUnit(unit);
		final UnitNode node = new UnitNode(unit);
		final String kind = unit.getKind();
		for (final TreeNode child : new IteratorWrapper<>(
				new EnumerationWrapper<TreeNode>(root.children()))) {
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
	public final void addNewUnit(final Unit unit) {
		addUnit(unit);
	}

	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public final void mapChanged() {
		setRoot(new PlayerNode(model.getMap().getPlayers().getCurrentPlayer(),
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
	public final void addUnitMember(final Unit unit, final UnitMember member) {
		final PlayerNode pnode = (PlayerNode) root;
		final IteratorWrapper<UnitNode> units = new IteratorWrapper<>(
				new EnumerationWrapper<UnitNode>(pnode.children()));
		UnitNode unode = null;
		for (final UnitNode item : units) {
			if (item.getUserObject().equals(unit)) {
				unode = item;
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
		} else if (node.getAllowsChildren()) {
			for (final TreeNode child : new IteratorWrapper<>(
					new EnumerationWrapper<TreeNode>(node.children()))) {
				if (child == null) {
					continue;
				}
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
		TreeNode[] path = getPathToRoot(node);
		int index = getIndexOfChild(path[path.length - 1], node);
		fireTreeNodesChanged(this, path, new int[] { index }, new Object[] { node });
	}

	@Override
	public void moveItem(final HasKind item) {
		if (item instanceof UnitMember) {
			final TreeNode node = getNode(item);
			if (node == null) {
				return;
			}
			TreeNode[] path = getPathToRoot(node);
			int index = getIndexOfChild(path[path.length - 1], node);
			fireTreeNodesChanged(this, path, new int[] { index },
					new Object[] { node });
		} else if (item instanceof Unit) {
			final TreeNode node = getNode(item);
			if (node == null) {
				return;
			}
			TreeNode[] pathOne = getPathToRoot(node);
			int indexOne = getIndexOfChild(pathOne[pathOne.length - 2], node);
			TreeNode nodeTwo = null;
			for (final TreeNode child : new IteratorWrapper<>(
					new EnumerationWrapper<TreeNode>(root.children()))) {
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
								Collections.singletonList((Unit) item)));
				((PlayerNode) root).add((MutableTreeNode) nodeTwo);
				fireTreeNodesInserted(this, new TreeNode[] { root },
						new int[] { getIndexOfChild(root, nodeTwo) },
						new Object[] { nodeTwo });
			} else {
				int indexTwo = nodeTwo.getChildCount();
				((MutableTreeNode) nodeTwo).insert((MutableTreeNode) node,
						indexTwo);
				fireTreeNodesInserted(this, new Object[] { root, nodeTwo },
						new int[] { indexTwo }, new Object[] { node });
			}

		}
	}
}

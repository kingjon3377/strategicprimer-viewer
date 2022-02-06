package drivers.worker_mgmt;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.Iterator;
import common.map.fixtures.UnitMember;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import common.map.fixtures.mobile.IUnit;
import common.map.Player;
import common.map.HasMutableName;
import common.map.HasMutableOwner;
import common.map.HasKind;
import worker.common.IWorkerTreeModel;
import lovelace.util.IteratorWrapper;
import lovelace.util.EnumerationWrapper;
import drivers.common.IWorkerModel;
import java.util.logging.Logger;

/**
 * An alternative implementation of the worker tree model.
 */
public class WorkerTreeModelAlt extends DefaultTreeModel implements IWorkerTreeModel {
	protected static final Logger LOGGER = Logger.getLogger(WorkerTreeModelAlt.class.getName());
	/**
	 * A base class for all nodes in the tree in this implementation of the tree model.
	 *
	 * TODO: Does this really need to be public, or just protected?
	 */
	public static class WorkerTreeNode<NodeObject> extends DefaultMutableTreeNode
			implements Iterable<TreeNode> {
		public WorkerTreeNode(final Class<NodeObject> cls, final NodeObject userObj) {
			this(cls, userObj, true);
		}

		public WorkerTreeNode(final Class<NodeObject> cls, final NodeObject userObj, final boolean permitsChildren) {
			super(userObj, permitsChildren);
			this.cls = cls;
		}

		private final Class<NodeObject> cls;

		/**
		 * An iterator over the node's child-nodes.
		 */
		@Override
		public final Iterator<TreeNode> iterator() {
			return new EnumerationWrapper<>(children());
		}

		/**
		 * Stream the node's child nodes.
		 */
		public Stream<TreeNode> stream() {
			return StreamSupport.stream(spliterator(), false);
		}

		/**
		 * The object the node represents, with its type cast
		 * to {@link NodeObject}.
		 */
		@Override
		public final NodeObject getUserObject() {
			return (NodeObject) super.getUserObject();
		}

		/**
		 * Change what object the node represents. While the compiler
		 * will not complain if you pass an object in that is not a
		 * {@link NodeObject}, that will cause an exception to be thrown.
		 *
		 * @throws IllegalArgumentException if {@link obj} is not a {@link NodeObject}.
		 */
		@Override
		public final void setUserObject(final Object obj) {
			if (!cls.isInstance(obj)) {
				throw new IllegalArgumentException("Node can only contain specified type");
			}
			super.setUserObject(obj);
		}

		/**
		 * Explicitly delegate to the {@link DefaultMutableTreeNode}
		 * default {@link toString} implementation. (This, with a cast
		 * specifying which supertype's implementation to use, was
		 * required in Ceylon for the code to compile.)
		 */
		@Override
		public final String toString() {
			return super.toString();
		}

		/**
		 * Add a child. If it is not a {@link WorkerTreeNode} of some sort,
		 * we log this (at the info level) but otherwise ignore the
		 * request instead of adding it.
		 */
		@Override
		public void add(final MutableTreeNode child) {
			if (child instanceof WorkerTreeNode) {
				super.add(child);
			} else {
				LOGGER.info("Asked to add a non-WorkerTreeNode to a WorkerTreeNode");
			}
		}
	}

	/**
	 * A class for tree-nodes representing members of units.
	 */
	private static class UnitMemberNode extends WorkerTreeNode<UnitMember> {
		public UnitMemberNode(final UnitMember member) {
			super(UnitMember.class, member, false);
		}
	}

	/**
	 * A class for tree-nodes representing units.
	 *
	 * TODO: Does this really need to be public?
	 */
	public static class UnitNode extends WorkerTreeNode<IUnit> {
		private final IUnit unit;
		public UnitNode(final IUnit unit) {
			super(IUnit.class, unit);
			this.unit = unit;
			int index = 0;
			for (UnitMember member : unit) {
				insert(new UnitMemberNode(member), index);
				index++;
			}
		}

		/**
		 * Add a child. If it is a {@link UnitMemberNode}, also check
		 * that the unit member it represents is already in the unit
		 * this node represents.
		 */
		@Override
		public void add(final MutableTreeNode child) {
			if (child instanceof UnitMemberNode) {
				if (unit.stream().noneMatch(
							((UnitMemberNode) child).getUserObject()::equals)) {
					LOGGER.warning(
						"Adding UnitMemberNode when its object is not in the unit");
				}
			} else {
				LOGGER.info("Added a non-UnitMemberNode to a UnitNode");
			}
			super.add(child);
		}

		/**
		 * Remove a child. If it is a {@link UnitMemberNode}, also
		 * <del>remove the unit-member it represents from the unit this node
		 * represents</del> check whether it has already been removed,
		 * and warn if it is still in the unit.
		 */
		@Override
		public void remove(final MutableTreeNode child) {
			if (child instanceof UnitMemberNode) {
				if (unit.stream().anyMatch(
							((UnitMemberNode) child).getUserObject()::equals)) {
					LOGGER.warning(
						"Removing UnitMemberNode when member is still in the unit");
				}
			} else {
				LOGGER.warning("Asked to remove non-UnitMember child from UnitNode");
			}
			super.remove(child);
		}

		public void refreshChildren() {
			removeAllChildren();
			for (UnitMember member : unit) {
				super.add(new UnitMemberNode(member));
			}
		}
	}

	/**
	 * A class for tree-nodes representing unit kinds, grouping units
	 * sharing a "kind" (in practice an administrative classification) in
	 * the tree.
	 *
	 * TODO: Does this really need to be public?
	 */
	public static class KindNode extends WorkerTreeNode<String> {
		public KindNode(final String kind, final IUnit... units) {
			super(String.class, kind);
			for (int index = 0; index < units.length; index++) {
				insert(new UnitNode(units[index]), index);
			}
		}
	}

	/**
	 * A class for the tree node representing the player, which serves as
	 * the root of the tree (and is hidden from the user, so it looks like
	 * there are multiple roots, each of which is a {@link KindNode}).
	 *
	 * TODO: We want to add a "Fortress" level to the tree, ideally when
	 * and only when some but not all units are in a fortress, or when
	 * units are in (divided between) multiple fortresses.
	 */
	private static class PlayerNode extends WorkerTreeNode<Player> {
		public PlayerNode(final Player player, final IWorkerModel model) {
			super(Player.class, player);
			int index = 0;
			for (String kind : model.getUnitKinds(player)) {
				insert(new KindNode(kind,
					model.getUnits(player, kind).stream()
						.filter(IUnit.class::isInstance)
						.map(IUnit.class::cast)
						.toArray(IUnit[]::new)), index);
			}
			if (getChildCount() == 0) {
				LOGGER.warning("No unit kinds in player node for player " + player);
			}
		}
	}

	/**
	 * A helper method to test whether a node has the given object as the object it represents.
	 */
	private static boolean areTreeObjectsEqual(final TreeNode node, final Object obj) {
		return node instanceof DefaultMutableTreeNode &&
			Objects.equals(((DefaultMutableTreeNode) node).getUserObject(), obj);
	}

	/**
	 * Get the node in the subtree under the given node that represents the given object.
	 */
	@Nullable
	private static MutableTreeNode getNode(final TreeNode node, final Object obj) {
		if (node instanceof MutableTreeNode && areTreeObjectsEqual(node, obj)) {
			return (MutableTreeNode) node;
		} else if (node instanceof WorkerTreeNode && node.getAllowsChildren()) {
			for (TreeNode child : (WorkerTreeNode<?>) node) {
				MutableTreeNode result = getNode(child, obj);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private final IWorkerModel model;
	public WorkerTreeModelAlt(final IWorkerModel driverModel) {
		super(new PlayerNode(driverModel.getCurrentPlayer(), driverModel), true);
		model = driverModel;
	}

	/**
	 * Move a unit-member from one unit to another, notifying listeners of changes to the tree.
	 */
	@Override
	public void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
		TreeNode playerNode = (TreeNode) getRoot();
		MutableTreeNode oldNode = getNode(playerNode, old);
		MutableTreeNode newNode = getNode(playerNode, newOwner);
		MutableTreeNode node = getNode(playerNode, member);
		if (newNode != null && node != null) {
			int oldIndex = (oldNode == null) ? -1 : oldNode.getIndex(node);
			fireTreeNodesRemoved(this,
				new Object[] { playerNode, getNode(playerNode, old.getKind()), oldNode },
				new int[] { oldIndex }, new Object[] { node });
			if (oldNode != null) {
				oldNode.remove(node);
			}
			model.moveMember(member, old, newOwner);
			newNode.insert(node, newNode.getChildCount());
			fireTreeNodesInserted(this,
				new Object[] { playerNode, getNode(playerNode, newOwner.getKind()), newNode },
				new int[] { newNode.getIndex(node) },
				new Object[] { node });
		}
	}

	/**
	 * Add a unit to the driver-model (that is, the map) and to the tree,
	 * notifying listeners of the change.
	 */
	@Override
	public void addUnit(final IUnit unit) {
		model.addUnit(unit);
		PlayerNode temp = (PlayerNode) getRoot();
		IUnit matchingUnit = model.getUnitByID(temp.getUserObject(), unit.getId());
		if (matchingUnit != null) {
			MutableTreeNode node = new UnitNode(matchingUnit);
			String kind = unit.getKind();
			boolean any = false;
			for (TreeNode child : temp) {
				if (child instanceof KindNode &&
						kind.equals(((KindNode) child).getUserObject())) {
					((KindNode) child).add(node);
					fireTreeNodesInserted(this, getPathToRoot(node),
						new int[] { child.getChildCount() - 1 },
						new Object[] { node });
					any = true;
					break;
				}
			}
			if (!any) {
				KindNode kindNode = new KindNode(kind, matchingUnit);
				temp.add(kindNode);
				fireTreeNodesInserted(this, getPathToRoot(kindNode),
					new int[] { temp.getChildCount() - 1 }, new Object[] { kindNode });
			}
		}
	}

	/**
	 * Add a unit to the driver-model and the map. Delegates to {@link
	 * addUnit}; the two have the same functionality, but are required by
	 * different interfaces.
	 */
	@Override
	public void addNewUnit(final IUnit unit) {
		addUnit(unit);
	}

	/**
	 * When we are notified that the map has changed, regenerate the tree
	 * by replacing the root node with a newly initialized {@link PlayerNode}.
	 */
	@Override
	public void mapChanged() {
		setRoot(new PlayerNode(model.getCurrentPlayer(), model));
	}

	/**
	 * When we are notified that the current player has changed, regenerate
	 * the tree for that player.
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		setRoot(new PlayerNode(newPlayer, model));
	}

	/**
	 * For any node, the model object is its {@link
	 * DefaultMutableTreeNode#getUserObject user object}; for anything that
	 * is not a node descending from that superclass, its model object is
	 * (presumed to be) itself.
	 */
	@Override
	public Object getModelObject(final Object obj) {
		if (obj instanceof DefaultMutableTreeNode) {
			return ((DefaultMutableTreeNode) obj).getUserObject();
		} else {
			return obj;
		}
	}

	/**
	 * Add a member to a unit, and to the corresponding node in the tree.
	 */
	@Override
	public void addUnitMember(final IUnit unit, final UnitMember member) {
		KindNode kindNode = ((PlayerNode) getRoot()).stream()
			.filter(KindNode.class::isInstance).map(KindNode.class::cast)
			.filter(n -> unit.getKind().equals(n.getUserObject()))
			.findAny().orElse(null);
		UnitNode unitNode = Optional.ofNullable(kindNode).map(WorkerTreeNode::stream)
			.orElse(Stream.empty()).filter(UnitNode.class::isInstance).map(UnitNode.class::cast)
			.filter(n -> unit.getKind().equals(n.getUserObject())).findAny().orElse(null);
		if (kindNode != null && unitNode != null) {
			model.addUnitMember(unit, member);
			MutableTreeNode newNode = new UnitMemberNode(member);
			unitNode.add(newNode);
			fireTreeNodesInserted(this, new Object[] { root, unitNode },
				new int[] { unitNode.getChildCount() - 1 }, new Object[] { newNode });
		} else {
			LOGGER.severe(
				"Asked to add a unit member but couldn't find corresponding unit node");
		}
	}

	/**
	 * Update the tree in response to something changing its name.
	 */
	@Override
	public void renameItem(final HasMutableName item, final String newName) {
		PlayerNode temp = (PlayerNode) getRoot();
		MutableTreeNode node = getNode(temp, item);
		if (node != null && model.renameItem(item, newName)) {
			TreeNode[] path = getPathToRoot(node);
			int index = getIndexOfChild(path[path.length - 2], node);
			fireTreeNodesChanged(this,
				Arrays.copyOf(path, path.length - 1, Object[].class),
				new int[] { index }, new Object[] { node });
		}
	}

	/**
	 * Update the tree in response to something's "kind" changing. If a
	 * unit-member, just tell listeners to update its appearance; if a
	 * unit, move it from its old parent node to the one for its new
	 * "kind," creating a new one if necessary, and removing the old one if empty.
	 *
	 * TODO: Do we actually create a new one if necessary?
	 *
	 * TODO: Do we actually remove the old one if now empty?
	 *
	 * FIXME: Make sure not to change the tree if newKind is the same as the old kind
	 */
	@Override
	public void changeKind(final HasKind item, final String newKind) {
		PlayerNode temp = (PlayerNode) getRoot();
		if (item instanceof UnitMember) {
			MutableTreeNode node = getNode(temp, item);
			if (node != null) {
				TreeNode[] path = getPathToRoot(node);
				int index = getIndexOfChild(path[path.length - 1], node);
				if (model.changeKind(item, newKind)) {
					// fireNodesChanged() is *correct*: a
					// change in a unit member's kind does
					// *not* mean any node should move.
					fireTreeNodesChanged(this, path, new int[] { index },
						new Object[] { node });
				}
			}
		} else if (item instanceof IUnit) {
			MutableTreeNode node = getNode(temp, item);
			TreeNode[] pathOne = getPathToRoot(node);
			int indexOne = getIndexOfChild(pathOne[pathOne.length - 2], node);
			KindNode nodeTwo = temp.stream().filter(KindNode.class::isInstance)
				.map(KindNode.class::cast)
				.filter(n -> item.getKind().equals(n.getUserObject()))
				.findAny().orElse(null);
			MutableTreeNode end = (MutableTreeNode) pathOne[pathOne.length - 1];
			end.removeFromParent();
			Object[] pathSubset;
			TreeNode lastParent = pathOne[pathOne.length - 2];
			if (lastParent instanceof MutableTreeNode &&
					((MutableTreeNode) lastParent).getChildCount() == 0) {
				TreeNode lastParentParent = pathOne[pathOne.length - 3];
				int parentIndex = lastParentParent.getIndex(lastParent);
				pathSubset = Arrays.copyOf(pathOne, pathOne.length - 2,
					Object[].class);
				((MutableTreeNode) lastParent).removeFromParent();
				fireTreeNodesRemoved(this, pathSubset, new int[] { parentIndex },
					new Object[] { lastParent });
			} else {
				pathSubset = Arrays.copyOf(pathOne, pathOne.length - 1,
					Object[].class);
				fireTreeNodesRemoved(this, pathSubset, new int[] { indexOne },
					new Object[] { node });
			}
			model.changeKind(item, newKind);
			if (nodeTwo == null) {
				MutableTreeNode kindNode = new KindNode(newKind, (IUnit) item);
				temp.add(kindNode);
				fireTreeNodesInserted(this, new Object[] { temp },
					new int[] { getIndexOfChild(temp, kindNode) },
					new Object[] { kindNode });
			} else {
				int indexTwo = nodeTwo.getChildCount();
				nodeTwo.insert(node, indexTwo);
				fireTreeNodesInserted(this, new Object[] { root, nodeTwo },
					new int[] { indexTwo }, new Object[] { node });
			}
		}
	}

	/**
	 * Remove a unit-member from its parent unit.
	 */
	@Override
	public void dismissUnitMember(final UnitMember member) {
		TreeNode temp = (TreeNode) getRoot();
		MutableTreeNode node = getNode(temp, member);
		if (node instanceof MutableTreeNode) {
			UnitNode parentNode = (UnitNode) node.getParent();
			// Note that getPathToRoot() returns a path that does
			// *not* include the node itself
			TreeNode[] path = getPathToRoot(node);
			int index = getIndexOfChild(path[path.length - 1], node);
			model.dismissUnitMember(member);
			parentNode.remove(node);
			fireTreeNodesRemoved(this, path, new int[] { index },
				new Object[] { node });
		}
	}

	/**
	 * Add {@link sibling a unit-member} to the unit containing {@link base
	 * the given unit-member}. This is primarily used when the user asks to
	 * split an animal population.
	 */
	@Override
	public void addSibling(final UnitMember base, final UnitMember sibling) {
		UnitMemberNode childNode = new UnitMemberNode(sibling);
		// FIXME: Log and/or give UI feedback on failure of some of these conditions
		TreeNode temp = (TreeNode) getRoot();
		TreeNode node = getNode(temp, base);
		TreeNode parentNode = Optional.ofNullable(node).map(TreeNode::getParent).orElse(null);
		if (node != null && parentNode instanceof UnitNode && model.addSibling(base, sibling)) {
			boolean found = false;
			int index = 0;
			for (UnitMember child : ((UnitNode) parentNode).getUserObject()) {
				if (sibling.equals(child)) {
					found = true;
					break;
				}
			}
			if (found) {
				((UnitNode) parentNode).insert(childNode, index);
				fireTreeNodesInserted(this, getPathToRoot(parentNode),
					new int[] { index },
					new Object[] { childNode });
			}
		}
	}

	/**
	 * Get the path to the "next" unit whose orders for the given turn
	 * either contain "TODO", contain "FIXME", contain "XXX", or are empty.
	 * Skips units with no members.  Returns null if no unit matches those criteria.
	 */
	@Override
	@Nullable
	public TreePath nextProblem(@Nullable final TreePath starting, final int turn) {
		PlayerNode rootNode = (PlayerNode) getRoot();
		Enumeration<?> enumeration = rootNode.preorderEnumeration();
		Iterable<WorkerTreeNode<?>> wrapped =
			new IteratorWrapper<>(new EnumerationWrapper<WorkerTreeNode<?>>(enumeration));
		Iterable<WorkerTreeNode<?>> sequence;
		boolean leading;
		WorkerTreeNode<?> toTrim;
		if (starting == null) {
			sequence = wrapped;
			leading = false;
			toTrim = null;
		} else {
			toTrim = (WorkerTreeNode<?>) starting.getLastPathComponent();
			sequence = Stream.concat(StreamSupport.stream(wrapped.spliterator(), false),
					StreamSupport.stream(wrapped.spliterator(), false))
				.collect(Collectors.toList());
			leading = true;
		}
		// if starting non-null, skip any leading instances of 'last'.
		// Thereafter, in any case, only look at UnitNode instances.
		for (WorkerTreeNode<?> node : sequence) {
			if (leading) {
				if (node.equals(toTrim)) {
					continue;
				} else {
					leading = false;
				}
			}
			if (!(node instanceof UnitNode)) {
				continue;
			} else if (!((UnitNode) node).getUserObject().iterator().hasNext()) {
				continue;
			}
			// TODO: add .trim() (in a separate commit!)
			String orders = ((UnitNode) node).getUserObject().getOrders(turn).toLowerCase();
			if (orders.isEmpty() || orders.contains("todo") || orders.contains("fixme") ||
					orders.contains("xxx")) {
				if (orders.isEmpty()) {
					LOGGER.fine("Orders are empty");
				} else if (orders.contains("todo")) {
					LOGGER.fine("Orders contain 'todo'");
				} else if (orders.contains("fixme")) {
					LOGGER.fine("Orders contain 'fixme'");
				} else if (orders.contains("xxx")) {
					LOGGER.fine("Orders contain 'xxx'");
				} else {
					LOGGER.warning(
						"Orders are not problematic, but called a problem anyway");
				}
				return new TreePath(node.getPath());
			}
		}
		return null;
	}

	/**
	 * Ignore notification of changes to map filename or "modified" flag.
	 */
	@Override
	public void mapMetadataChanged() {}

	@Override
	public Iterable<Object> childrenOf(final Object obj) {
		PlayerNode temp = (PlayerNode) getRoot();
		if (obj instanceof WorkerTreeNode) {
			return (Iterable<Object>) ((Iterable<?>) ((WorkerTreeNode<?>) obj));
		}
		TreeNode node = getNode(temp, obj);
		if (node == null) {
			return Collections.emptyList();
		} else {
			return childrenOf(node); // FIXME: This looks like possible infinite recursion ...
		}
	}

	@Override
	public void refreshChildren(final IUnit parent) {
		PlayerNode playerNode = (PlayerNode) getRoot();
		TreeNode parentNode = getNode(playerNode, parent);
		if (parentNode instanceof UnitNode) {
			((UnitNode) parentNode).refreshChildren();
			fireTreeStructureChanged(this, new Object[] { root, parentNode }, // FIXME: kind node?
				null, null);
		} else {
			LOGGER.severe("refreshChildren() called on unit not in the tree");
		}
	}

	@Override
	public void removeUnit(final IUnit unit) {
		LOGGER.finer("In WorkerTreeModelAlt.removeUnit");
		PlayerNode playerNode = (PlayerNode) getRoot();
		TreeNode kindNode = getNode(playerNode, unit.getKind());
		TreeNode node = Optional.ofNullable(kindNode).map(n -> getNode(n, unit)).orElse(null);
		if (kindNode instanceof KindNode && node instanceof UnitNode) {
			int index = getIndexOfChild(kindNode, node);
			LOGGER.finer(String.format("Unit is %dth child of unit-kind", index));
			if (model.removeUnit(unit)) {
				LOGGER.finer("Removed from the map, about to remove from the tree");
				((KindNode) kindNode).remove((UnitNode) node);
				fireTreeNodesRemoved(this, new Object[] { playerNode, kindNode},
					new int[] { index }, new Object[] { node });
				LOGGER.finer("Finished updating the tree");
			} else {
				LOGGER.warning("Failed to remove from the map for some reason");
				// FIXME: Some user feedback---beep, visual beep, etc.
			}
		} else {
			LOGGER.severe("Tree root isn't a tree node, or tree doesn't contain that unit");
		}
	}

	@Override
	public void changeOwner(final HasMutableOwner item, final Player newOwner) {
		PlayerNode playerNode = (PlayerNode) getRoot();
		if (item instanceof IUnit && item.getOwner().equals(model.getCurrentPlayer())) {
			TreeNode kindNode = getNode(playerNode, ((IUnit) item).getKind());
			TreeNode node = Optional.ofNullable(kindNode).map(n -> getNode(n, item))
				.orElse(null);
			if (kindNode instanceof KindNode && node instanceof UnitNode) {
				int index = getIndexOfChild(kindNode, null);
				if (model.changeOwner(item, newOwner)) {
					((KindNode) kindNode).remove((UnitNode) node);
					fireTreeNodesRemoved(this, new Object[] { playerNode, kindNode },
						new int[] { index }, new Object[] { node });
					return;
				}
			}
			LOGGER.warning("Failed to change unit's owner");
			// FIXME: Some user feedback---beep, visual beep, etc.
		} else { // FIXME: Also check the case where newOwner is the current player
			// TODO: Log when preconditions other than 'is a unit'
			// and 'the current player is involved' aren't met
			model.changeOwner(item, newOwner);
		}
	}

	@Override
	public void sortMembers(final IUnit fixture) {
		if (model.sortFixtureContents(fixture)) {
			refreshChildren(fixture);
		}
	}
}

package drivers.worker_mgmt;

import common.map.HasName;
import legacy.map.HasOwner;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.Iterator;

import legacy.map.fixtures.UnitMember;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import legacy.map.fixtures.mobile.IUnit;
import legacy.map.Player;
import legacy.map.HasKind;
import worker.common.IWorkerTreeModel;
import lovelace.util.IteratorWrapper;
import lovelace.util.EnumerationWrapper;
import drivers.common.IWorkerModel;

/**
 * An alternative implementation of the worker tree model.
 *
 * TODO: Nodes should store 'keys', i.e. [id, owner, name, kind] for units, rather than model objects
 * directly, to ease the elimination of proxies.  We may need to alter the driver model to make that
 * work. Once that's in place we'll get rid of {@link WorkerTreeModel}.
 *
 * TODO: We want to add Fortress and Unit-Group levels to the tree,
 *
 * TODO: We want a way for the user to manage 'personal equipment'
 */
public final class WorkerTreeModelAlt extends DefaultTreeModel implements IWorkerTreeModel {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * A base class for all nodes in the tree in this implementation of the tree model.
	 *
	 * TODO: should be abstract
	 */
	protected static class WorkerTreeNode<NodeObject> extends DefaultMutableTreeNode
			implements Iterable<TreeNode> {
		@Serial
		private static final long serialVersionUID = 1L;

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
		public final Stream<TreeNode> stream() {
			return StreamSupport.stream(spliterator(), false);
		}

		/**
		 * The object the node represents, with its type cast
		 * to {@link NodeObject}.
		 */
		@Override
		public final NodeObject getUserObject() {
			return cls.cast(super.getUserObject());
		}

		/**
		 * Change what object the node represents. While the compiler
		 * will not complain if you pass an object in that is not a
		 * {@link NodeObject}, that will cause an exception to be thrown.
		 *
		 * @throws IllegalArgumentException if "obj" is not a {@link NodeObject}.
		 */
		@Override
		public final void setUserObject(final Object obj) {
			if (!cls.isInstance(obj)) {
				throw new IllegalArgumentException("Node can only contain specified type");
			}
			super.setUserObject(obj);
		}

		/**
		 * Explicitly delegate to the
		 * {@link DefaultMutableTreeNode#toString default superclass implementation}. (This, with a cast
		 * specifying which supertype's implementation to use, was
		 * required in Ceylon for the code to compile.)
		 */
		@Override
		public final String toString() {
			return super.toString();
		}

		/**
		 * Add a child. If it is not a WorkerTreeNode of some sort,
		 * we log this (at the info level) but otherwise ignore the
		 * request instead of adding it.
		 */
		@Override
		public void add(final MutableTreeNode child) {
			if (child instanceof WorkerTreeNode) {
				super.add(child);
			} else {
				LovelaceLogger.info("Asked to add a non-WorkerTreeNode to a WorkerTreeNode");
			}
		}
	}

	/**
	 * A class for tree-nodes representing members of units.
	 */
	private static class UnitMemberNode extends WorkerTreeNode<UnitMember> {
		@Serial
		private static final long serialVersionUID = 1L;

		public UnitMemberNode(final UnitMember member) {
			super(UnitMember.class, member, false);
		}
	}

	/**
	 * A class for tree-nodes representing units.
	 */
	private static final class UnitNode extends WorkerTreeNode<IUnit> {
		@Serial
		private static final long serialVersionUID = 1L;
		private final IUnit unit;

		public UnitNode(final IUnit unit) {
			super(IUnit.class, unit);
			this.unit = unit;
			int index = 0;
			for (final UnitMember member : unit) {
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
			if (child instanceof final UnitMemberNode umn) {
				if (unit.stream().noneMatch(umn.getUserObject()::equals)) {
					LovelaceLogger.warning(
							"Adding UnitMemberNode when its object is not in the unit");
				}
			} else {
				LovelaceLogger.info("Added a non-UnitMemberNode to a UnitNode");
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
			if (child instanceof final UnitMemberNode umn) {
				if (unit.stream().anyMatch(umn.getUserObject()::equals)) {
					LovelaceLogger.warning(
							"Removing UnitMemberNode when member is still in the unit");
				}
			} else {
				LovelaceLogger.warning("Asked to remove non-UnitMember child from UnitNode");
			}
			super.remove(child);
		}

		public void refreshChildren() {
			removeAllChildren();
			for (final UnitMember member : unit) {
				super.add(new UnitMemberNode(member));
			}
		}
	}

	/**
	 * A class for tree-nodes representing unit kinds, grouping units
	 * sharing a "kind" (in practice an administrative classification) in
	 * the tree.
	 */
	private static final class KindNode extends WorkerTreeNode<String> {
		@Serial
		private static final long serialVersionUID = 1L;

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
	private static final class PlayerNode extends WorkerTreeNode<Player> {
		@Serial
		private static final long serialVersionUID = 1L;

		public PlayerNode(final Player player, final IWorkerModel model) {
			super(Player.class, player);
			final int index = 0;
			for (final String kind : model.getUnitKinds(player)) {
				insert(new KindNode(kind,
						model.getUnits(player, kind).toArray(IUnit[]::new)), index);
			}
			if (getChildCount() == 0) {
				LovelaceLogger.warning("No unit kinds in player node for player %s", player);
			}
		}
	}

	/**
	 * A helper method to test whether a node has the given object as the object it represents.
	 */
	private static boolean areTreeObjectsEqual(final TreeNode node, final Object obj) {
		return node instanceof final DefaultMutableTreeNode mtn && Objects.equals(mtn.getUserObject(), obj);
	}

	/**
	 * Get the node in the subtree under the given node that represents the given object.
	 */
	private static @Nullable MutableTreeNode getNode(final TreeNode node, final Object obj) {
		switch (node) {
			case final MutableTreeNode mutableTreeNode when areTreeObjectsEqual(node, obj) -> {
				return mutableTreeNode;
			}
			case final WorkerTreeNode<?> workerTreeNode when node.getAllowsChildren() -> {
				for (final TreeNode child : workerTreeNode) {
					final MutableTreeNode result = getNode(child, obj);
					if (!Objects.isNull(result)) {
						return result;
					}
				}
			}
			default -> {
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
		final TreeNode playerNode = (TreeNode) getRoot();
		final MutableTreeNode oldNode = getNode(playerNode, old);
		final MutableTreeNode newNode = getNode(playerNode, newOwner);
		final MutableTreeNode node = getNode(playerNode, member);
		if (!Objects.isNull(newNode) && !Objects.isNull(node)) {
			final int oldIndex = (Objects.isNull(oldNode)) ? -1 : oldNode.getIndex(node);
			fireTreeNodesRemoved(this,
					Stream.of(playerNode, getNode(playerNode, old.getKind()), oldNode)
							.filter(Objects::nonNull).toArray(),
					new int[]{oldIndex}, new Object[]{node});
			if (!Objects.isNull(oldNode)) {
				oldNode.remove(node);
			}
			model.moveMember(member, old, newOwner);
			newNode.insert(node, newNode.getChildCount());
			fireTreeNodesInserted(this,
					new Object[]{playerNode, Objects.requireNonNull(getNode(playerNode, newOwner.getKind())), newNode},
					new int[]{newNode.getIndex(node)},
					new Object[]{node});
		}
	}

	/**
	 * Add a unit to the driver-model (that is, the map) and to the tree,
	 * notifying listeners of the change.
	 */
	@Override
	public void addUnit(final IUnit unit) {
		model.addUnit(unit);
		final PlayerNode temp = (PlayerNode) getRoot();
		final IUnit matchingUnit = model.getUnitByID(temp.getUserObject(), unit.getId());
		if (!Objects.isNull(matchingUnit)) {
			final MutableTreeNode node = new UnitNode(matchingUnit);
			final String kind = unit.getKind();
			boolean any = false;
			for (final TreeNode child : temp) {
				if (child instanceof final KindNode kn && kind.equals(kn.getUserObject())) {
					kn.add(node);
					fireTreeNodesInserted(this, getPathToRoot(node),
							new int[]{child.getChildCount() - 1},
							new Object[]{node});
					any = true;
					break;
				}
			}
			if (!any) {
				final MutableTreeNode kindNode = new KindNode(kind, matchingUnit);
				temp.add(kindNode);
				fireTreeNodesInserted(this, getPathToRoot(kindNode),
						new int[]{temp.getChildCount() - 1}, new Object[]{kindNode});
			}
		}
	}

	/**
	 * Add a unit to the driver-model and the map. Delegates to {@link
	 * #addUnit}; the two have the same functionality, but are required by
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
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
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
		if (obj instanceof final DefaultMutableTreeNode dmtn) {
			return dmtn.getUserObject();
		} else {
			return obj;
		}
	}

	/**
	 * Add a member to a unit, and to the corresponding node in the tree.
	 */
	@Override
	public void addUnitMember(final IUnit unit, final UnitMember member) {
		// Our choices are an "overly strong" cast or an "unchecked" (because of generics) cast
		@SuppressWarnings("OverlyStrongTypeCast") final KindNode kindNode = ((PlayerNode) getRoot()).stream()
				.filter(KindNode.class::isInstance).map(KindNode.class::cast)
				.filter(n -> unit.getKind().equals(n.getUserObject()))
				.findAny().orElse(null);
		final UnitNode unitNode = Optional.ofNullable(kindNode).map(WorkerTreeNode::stream)
				.orElse(Stream.empty()).filter(UnitNode.class::isInstance).map(UnitNode.class::cast)
				.filter(n -> unit.equals(n.getUserObject())).findAny().orElse(null);
		if (!Objects.isNull(kindNode) && !Objects.isNull(unitNode)) {
			model.addUnitMember(unit, member);
			final MutableTreeNode newNode = new UnitMemberNode(member);
			unitNode.add(newNode);
			fireTreeNodesInserted(this, new Object[]{root, unitNode},
					new int[]{unitNode.getChildCount() - 1}, new Object[]{newNode});
		} else {
			LovelaceLogger.error(
					"Asked to add a unit member but couldn't find corresponding unit node");
		}
	}

	/**
	 * Update the tree in response to something changing its name.
	 */
	@Override
	public void renameItem(final HasName item, final String newName) {
		final TreeNode temp = (TreeNode) getRoot();
		final MutableTreeNode node = getNode(temp, item);
		if (!Objects.isNull(node) && model.renameItem(item, newName)) {
			final TreeNode[] path = getPathToRoot(node);
			final int index = getIndexOfChild(path[path.length - 2], node);
			fireTreeNodesChanged(this,
					Arrays.copyOf(path, path.length - 1, Object[].class),
					new int[]{index}, new Object[]{node});
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
		final PlayerNode temp = (PlayerNode) getRoot();
		switch (item) {
			case final UnitMember unitMember -> {
				final MutableTreeNode node = getNode(temp, item);
				if (!Objects.isNull(node)) {
					final TreeNode[] path = getPathToRoot(node);
					final int index = getIndexOfChild(path[path.length - 1], node);
					if (model.changeKind(item, newKind)) {
						// fireNodesChanged() is *correct*: a
						// change in a unit member's kind does
						// *not* mean any node should move.
						fireTreeNodesChanged(this, path, new int[]{index},
								new Object[]{node});
					}
				}
			}
			case final IUnit unitMembers -> {
				final MutableTreeNode node = getNode(temp, item);
				if (Objects.isNull(node)) {
					LovelaceLogger.debug("changeKind() called for unit not in the tree");
					return;
				}
				final TreeNode[] pathOne = getPathToRoot(node);
				final int indexOne = getIndexOfChild(pathOne[pathOne.length - 2], node);
				final KindNode nodeTwo = temp.stream().filter(KindNode.class::isInstance)
						.map(KindNode.class::cast)
						.filter(n -> item.getKind().equals(n.getUserObject()))
						.findAny().orElse(null);
				final MutableTreeNode end = (MutableTreeNode) pathOne[pathOne.length - 1];
				end.removeFromParent();
				final Object[] pathSubset;
				final TreeNode lastParent = pathOne[pathOne.length - 2];
				if (lastParent instanceof final MutableTreeNode mtn &&
						0 == lastParent.getChildCount()) {
					final TreeNode lastParentParent = pathOne[pathOne.length - 3];
					final int parentIndex = lastParentParent.getIndex(lastParent);
					pathSubset = Arrays.copyOf(pathOne, pathOne.length - 2,
							Object[].class);
					mtn.removeFromParent();
					fireTreeNodesRemoved(this, pathSubset, new int[]{parentIndex},
							new Object[]{lastParent});
				} else {
					pathSubset = Arrays.copyOf(pathOne, pathOne.length - 1,
							Object[].class);
					fireTreeNodesRemoved(this, pathSubset, new int[]{indexOne},
							new Object[]{node});
				}
				model.changeKind(item, newKind);
				if (Objects.isNull(nodeTwo)) {
					final MutableTreeNode kindNode = new KindNode(newKind, (IUnit) item);
					temp.add(kindNode);
					fireTreeNodesInserted(this, new Object[]{temp},
							new int[]{getIndexOfChild(temp, kindNode)},
							new Object[]{kindNode});
				} else {
					final int indexTwo = nodeTwo.getChildCount();
					nodeTwo.insert(node, indexTwo);
					fireTreeNodesInserted(this, new Object[]{root, nodeTwo},
							new int[]{indexTwo}, new Object[]{node});
				}
			}
			default -> {
			}
		}
	}

	/**
	 * Remove a unit-member from its parent unit.
	 */
	@Override
	public void dismissUnitMember(final UnitMember member) {
		final TreeNode temp = (TreeNode) getRoot();
		final MutableTreeNode node = getNode(temp, member);
		if (!Objects.isNull(node)) {
			final UnitNode parentNode = (UnitNode) node.getParent();
			// Note that getPathToRoot() returns a path that does
			// *not* include the node itself
			final TreeNode[] path = getPathToRoot(node);
			final int index = getIndexOfChild(path[path.length - 1], node);
			model.dismissUnitMember(member);
			parentNode.remove(node);
			fireTreeNodesRemoved(this, path, new int[]{index},
					new Object[]{node});
		}
	}

	/**
	 * Add a unit-member, "sibling" to the unit containing
	 * the given unit-member, "base". This is primarily used when the user asks to
	 * split an animal population.
	 */
	@Override
	public void addSibling(final UnitMember base, final UnitMember sibling) {
		final MutableTreeNode childNode = new UnitMemberNode(sibling);
		// FIXME: Log and/or give UI feedback on failure of some of these conditions
		final TreeNode temp = (TreeNode) getRoot();
		final TreeNode node = getNode(temp, base);
		final TreeNode parentNode = Optional.ofNullable(node).map(TreeNode::getParent).orElse(null);
		if (!Objects.isNull(node) && parentNode instanceof final UnitNode un && model.addSibling(base, sibling)) {
			boolean found = false;
			final int index = 0;
			for (final UnitMember child : un.getUserObject()) {
				if (sibling.equals(child)) {
					found = true;
					break;
				}
			}
			if (found) {
				un.insert(childNode, index);
				fireTreeNodesInserted(this, getPathToRoot(parentNode),
						new int[]{index},
						new Object[]{childNode});
			}
		}
	}

	/**
	 * Get the path to the "next" unit whose orders for the given turn
	 * either contain "TODO", contain "FIXME", contain "XXX", or are empty.
	 * Skips units with no members.  Returns null if no unit matches those criteria.
	 */
	@Override
	public @Nullable TreePath nextProblem(final @Nullable TreePath starting, final int turn) {
		final PlayerNode rootNode = (PlayerNode) getRoot();
		final Enumeration<?> enumeration = rootNode.preorderEnumeration();
		final Iterable<WorkerTreeNode<?>> wrapped =
				new IteratorWrapper<>(new EnumerationWrapper<>(enumeration));
		final Iterable<WorkerTreeNode<?>> sequence;
		boolean leading;
		final WorkerTreeNode<?> toTrim;
		if (Objects.isNull(starting)) {
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
		for (final WorkerTreeNode<?> node : sequence) {
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
			// Our choices are an "overly strong" cast or an "unchecked" (because of generics) one
			@SuppressWarnings("OverlyStrongTypeCast") final String orders = ((UnitNode) node).getUserObject()
					.getOrders(turn).toLowerCase().strip();
			if (orders.isEmpty() || orders.contains("todo") || orders.contains("fixme") ||
					orders.contains("xxx")) {
				if (orders.isEmpty()) {
					LovelaceLogger.debug("Orders are empty");
				} else if (orders.contains("todo")) {
					LovelaceLogger.debug("Orders contain 'todo'");
				} else if (orders.contains("fixme")) {
					LovelaceLogger.debug("Orders contain 'fixme'");
				} else if (orders.contains("xxx")) {
					LovelaceLogger.debug("Orders contain 'xxx'");
				} else {
					LovelaceLogger.warning(
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
	public void mapMetadataChanged() {
	}

	@Override
	public Iterable<Object> childrenOf(final Object obj) {
		final TreeNode temp = (TreeNode) getRoot();
		if (obj instanceof WorkerTreeNode) {
			// uncheecked-cast warning is unavoidable without reified generics
			//noinspection unchecked
			return (Iterable<Object>) obj;
		}
		final TreeNode node = getNode(temp, obj);
		if (Objects.isNull(node)) {
			return Collections.emptyList();
		} else {
			return childrenOf(node); // FIXME: This looks like possible infinite recursion ...
		}
	}

	@Override
	public void refreshChildren(final IUnit parent) {
		final TreeNode playerNode = (TreeNode) getRoot();
		final TreeNode kindNode = getNode(playerNode, parent.getKind());
		final TreeNode parentNode = getNode(playerNode, parent);
		if (kindNode instanceof KindNode && parentNode instanceof final UnitNode un) {
			un.refreshChildren();
			fireTreeStructureChanged(this, new Object[]{root, kindNode, parentNode},
					null, null);
		} else {
			LovelaceLogger.error("refreshChildren() called on unit not in the tree");
		}
	}

	@Override
	public void removeUnit(final IUnit unit) {
		LovelaceLogger.trace("In WorkerTreeModelAlt.removeUnit");
		final TreeNode playerNode = (TreeNode) getRoot();
		final TreeNode kindNode = getNode(playerNode, unit.getKind());
		final TreeNode node = Optional.ofNullable(kindNode).map(n -> getNode(n, unit)).orElse(null);
		if (kindNode instanceof final KindNode kn && node instanceof UnitNode) {
			final int index = getIndexOfChild(kindNode, node);
			LovelaceLogger.trace("Unit is %dth child of unit-kind", index);
			if (model.removeUnit(unit)) {
				LovelaceLogger.trace("Removed from the map, about to remove from the tree");
				kn.remove((UnitNode) node);
				fireTreeNodesRemoved(this, new Object[]{playerNode, kindNode},
						new int[]{index}, new Object[]{node});
				LovelaceLogger.trace("Finished updating the tree");
			} else {
				LovelaceLogger.warning("Failed to remove from the map for some reason");
				// FIXME: Some user feedback---beep, visual beep, etc.
			}
		} else {
			LovelaceLogger.error("Tree root isn't a tree node, or tree doesn't contain that unit");
		}
	}

	@Override
	public void changeOwner(final HasOwner item, final Player newOwner) {
		final TreeNode playerNode = (TreeNode) getRoot();
		if (item instanceof final IUnit unit && item.owner().equals(model.getCurrentPlayer())) {
			final TreeNode kindNode = getNode(playerNode, unit.getKind());
			final TreeNode node = Optional.ofNullable(kindNode).map(n -> getNode(n, item))
					.orElse(null);
			if (kindNode instanceof final KindNode kn && node instanceof final UnitNode un) {
				final int index = getIndexOfChild(kindNode, null);
				if (model.changeOwner(item, newOwner)) {
					kn.remove(un);
					fireTreeNodesRemoved(this, new Object[]{playerNode, kindNode},
							new int[]{index}, new Object[]{node});
					return;
				}
			}
			LovelaceLogger.warning("Failed to change unit's owner");
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

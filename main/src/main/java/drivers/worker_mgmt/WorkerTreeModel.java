package drivers.worker_mgmt;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import common.map.fixtures.UnitMember;
import javax.swing.tree.TreePath;

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import common.map.fixtures.mobile.IUnit;
import common.map.Player;
import common.map.HasMutableName;
import common.map.HasMutableOwner;
import common.map.HasKind;
import worker.common.IWorkerTreeModel;
import drivers.common.IWorkerModel;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * A TreeModel implementation for a player's units and workers.
 */
/* package */ class WorkerTreeModel implements IWorkerTreeModel {
	private static final Logger LOGGER = Logger.getLogger(WorkerTreeModel.class.getName());
	private static Predicate<IUnit> containingItem(final UnitMember item) {
		return unit -> unit.stream().anyMatch(item::equals);
	}

	private Player player;
	private final IWorkerModel model;

	public WorkerTreeModel(final Player player, final IWorkerModel model) {
		this.player = player;
		this.model = model;
	}

	private final List<TreeModelListener> listeners = new ArrayList<>();

	@Override
	public Player getRoot() {
		return player;
	}

	// TODO: We want to add a 'Fortress' level to the tree

	@Override
	public Object getChild(final Object parent, final int index) {
		// TODO: Make IWorkerModel methods return List to simplify this?
		if (parent instanceof Player) {
			return StreamSupport.stream(model.getUnitKinds((Player) parent).spliterator(), false)
				.collect(Collectors.toList()).get(index);
		} else if (parent instanceof String) {
			return new ArrayList<>(model.getUnits(player, (String) parent)).get(index);
		} else if (parent instanceof IUnit) {
			return ((IUnit) parent).stream().collect(Collectors.toList()).get(index);
		} else {
			throw new ArrayIndexOutOfBoundsException("Unrecognized parent");
		}
	}

	@Override
	public int getChildCount(final Object parent) {
		if (parent instanceof Player) {
			return (int) StreamSupport.stream(model.getUnitKinds((Player) parent).spliterator(),
				false).count();
		} else if (parent instanceof String && StreamSupport.stream(
				model.getUnitKinds(player).spliterator(), false).anyMatch(parent::equals)) {
			return model.getUnits(player, (String) parent).size();
		} else if (parent instanceof IUnit) {
			return (int) ((IUnit) parent).stream().count();
		} else {
			throw new IllegalArgumentException("Not a possible member of the tree");
		}
	}

	@Override
	public boolean isLeaf(final Object node) {
		// TODO: condense further
		if (node instanceof Player || node instanceof IUnit || node instanceof String) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		LOGGER.severe("valueForPathChanged needs to be implemented");
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		if (parent instanceof Player && child instanceof IUnit) {
			// FIXME: This case shouldn't be allowed, right?
			return new ArrayList<>(model.getUnits((Player) parent)).indexOf(child);
		} else if (parent instanceof Player && child instanceof String) {
			return StreamSupport.stream(model.getUnitKinds((Player) parent).spliterator(), false)
				.collect(Collectors.toList()).indexOf(child);
		} else if (parent instanceof String && child instanceof IUnit) {
			return new ArrayList<>(model.getUnits(player, (String) parent)).indexOf(child);
		} else if (parent instanceof IUnit) {
			return ((IUnit) parent).stream().collect(Collectors.toList()).indexOf(child);
		} else {
			return -1;
		}
	}

	@Override
	public void addTreeModelListener(final TreeModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTreeModelListener(final TreeModelListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
		int oldIndex = getIndexOfChild(old, member);
		TreeModelEvent removedEvent = new TreeModelEvent(this,
			new TreePath(new Object[] { player, old.getKind(), old }),
				new int[] { oldIndex }, new Object[] { member });
		TreeModelEvent removedChangeEvent = new TreeModelEvent(this,
			new TreePath(new Object[] { player, old.getKind(), old }));
		for (TreeModelListener listener : listeners) {
			listener.treeNodesRemoved(removedEvent);
			listener.treeStructureChanged(removedChangeEvent);
		}
		model.moveMember(member, old, newOwner);
		TreeModelEvent insertedEvent = new TreeModelEvent(this,
			new TreePath(new Object[] { player, newOwner.getKind(), newOwner }),
				new int[] { getIndexOfChild(newOwner, member) }, new Object[] { member });
		TreeModelEvent insertedChangeEvent = new TreeModelEvent(this,
			new TreePath(new Object[] { player, newOwner.getKind(), newOwner }));
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(insertedEvent);
			listener.treeStructureChanged(insertedChangeEvent);
		}
	}

	@Override
	public void addUnit(final IUnit unit) {
		model.addUnit(unit);
		TreePath path = new TreePath(new Object[] { player, unit.getKind() });
		int[] indices = { model.getUnits(player, unit.getKind()).size()};
		Object[] children = { unit };
		TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
	}

	@Override
	public void addNewUnit(final IUnit unit) {
		addUnit(unit);
	}

	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		player = newPlayer;
		TreeModelEvent event = new TreeModelEvent(this, new TreePath(getRoot()));
		for (TreeModelListener listener : listeners) {
			listener.treeNodesChanged(event);
		}
	}

	@Override
	public void mapChanged() {
		playerChanged(player, model.getCurrentPlayer());
	}

	@Override
	public Object getModelObject(final Object obj) {
		return obj;
	}

	@Override
	public void addUnitMember(final IUnit unit, final UnitMember member) {
		LOGGER.finer("In WorkerTreeModel.addUnitMember");
		model.addUnitMember(unit, member);
		LOGGER.finer("Added member to unit");
		TreePath path = new TreePath(new Object[] { player, unit.getKind(), unit });
		int[] indices = { getIndexOfChild(unit, member) };
		Object[] children = { member };
		TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
		LOGGER.finer("Notified listeners of inserted nodes");
	}

	@Override
	public void renameItem(final HasMutableName item, final String newName) {
		TreePath path;
		int[] indices;
		Object[] children;
		if (item instanceof IUnit) {
			path = new TreePath(new Object[] { player, ((IUnit) item).getKind() });
			indices = new int[] { getIndexOfChild(((IUnit) item).getKind(), item) };
			children = new Object[] { item };
		} else if (item instanceof UnitMember) {
			IUnit parent = model.getUnits(player).stream()
				.filter(containingItem((UnitMember) item)).findAny().orElse(null);
			if (parent == null) {
				LOGGER.warning(
					"In WorkerTreeModel.renameItem(), unit member belonged to no unit");
				return;
			}
			path = new TreePath(new Object[] { player, parent.getKind(), parent });
			indices = new int[] { getIndexOfChild(parent, item) };
			children = new Object[] { item };
		} else if (item instanceof Player) {
			// ignore
			return;
		} else {
			LOGGER.warning(
				"In WorkerTreeModel.renameItem(), item was neither unit nor unit member");
			// Ignore, as it's something we don't know how to handle.
			// If we see log messages, revisit.
			return;
		}
		if (model.renameItem(item, newName)) {
			TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
			for (TreeModelListener listener : listeners) {
				listener.treeNodesChanged(event);
			}
		}
	}

	@Override
	public void changeKind(final HasKind item, final String newKind) {
		TreePath path;
		int[] indices;
		Object[] children;
		if (item instanceof IUnit) {
			// TODO: should probably fire removal and addition events instead
			path = new TreePath(new Object[] { player });
			indices = new int[] { getIndexOfChild(player, item.getKind()),
				getIndexOfChild(player, newKind) };
			children = new Object[] { item.getKind(), newKind };
		} else if (item instanceof UnitMember) {
			IUnit parent = model.getUnits(player).stream()
				.filter(containingItem((UnitMember) item)).findAny().orElse(null);
			if (parent == null) {
				LOGGER.warning(
					"In WorkerTreeModel.changeKind(), unit member belonged to no unit");
				return;
			}
			path = new TreePath(new Object[] { player, parent.getKind(), parent });
			indices = new int[] { getIndexOfChild(parent, item) };
			children = new Object[] { item };
		} else {
			// Impossible at present, so ignore. TODO: log
			return;
		}
		model.changeKind(item, newKind);
		TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
		for (TreeModelListener listener : listeners) {
			listener.treeNodesChanged(event);
		}
	}

	@Override
	public void dismissUnitMember(final UnitMember member) {
		for (IUnit unit : model.getUnits(player)) {
			int index = 0;
			for (UnitMember item : unit) {
				if (member.equals(item)) {
					model.dismissUnitMember(member);
					TreeModelEvent event = new TreeModelEvent(this,
						new TreePath(new Object[] { player, unit }),
							new int[] { index }, new Object[] { member });
					for (TreeModelListener listener : listeners) {
						listener.treeNodesRemoved(event);
					}
					return;
				}
				index++;
			}
		}
	}

	@Override
	public void addSibling(final UnitMember base, final UnitMember sibling) {
		for (IUnit unit : model.getUnits(player)) {
			if (unit.stream().anyMatch(base::equals)) {
				int existingMembersCount = (int) unit.stream().count();
				model.addSibling(base, sibling);
				int countAfterAdding = (int) unit.stream().count();
				TreeModelEvent event;
				if (countAfterAdding > existingMembersCount) {
					event = new TreeModelEvent(this,
						new TreePath(new Object[] { player, unit.getKind(), unit }),
						new int[] { existingMembersCount },
						new Object[] { sibling });
				} else {
					event = new TreeModelEvent(this,
						new TreePath(new Object[] { player, unit.getKind() }),
						new int[] { getIndexOfChild(unit.getKind(), unit) },
						new Object[] { unit });
				}
				for (TreeModelListener listener : listeners) {
					listener.treeNodesInserted(event);
				}
				// TODO: return, surely?
			}
		}
	}

	/**
	 * Get the path to the "next" unit whose orders for the given turn
	 * either contain "TODO", contain "FIXME", contain "XXX", or are empty.
	 * Skips units with no members.  Returns null if no unit matches those
	 * criteria.
	 */
	@Override
	@Nullable
	public TreePath nextProblem(@Nullable final TreePath starting, final int turn) {
		Iterable<IUnit> sequence;
		boolean leading;
		Predicate<IUnit> leadingFilter;
		if (starting != null) {
			IUnit startingUnit = Stream.of(starting.getPath()).filter(IUnit.class::isInstance)
				.map(IUnit.class::cast).findFirst().orElse(null);
			String startingKind = Stream.of(starting.getPath()).filter(String.class::isInstance)
				.map(String.class::cast).findFirst().orElse(null);
			Collection<IUnit> temp = model.getUnits(player);
			sequence = Stream.concat(temp.stream(), temp.stream()).collect(Collectors.toList());
			if (startingUnit != null) {
				leading = true;
				leadingFilter = startingUnit::equals;
			} else if (startingKind != null) {
				leading = true;
				leadingFilter = unit -> startingKind.equals(unit.getKind());
			} else {
				leading = false;
				leadingFilter = null;
				sequence = model.getUnits(player);
			}
		} else {
			leading = false;
			leadingFilter = null;
			sequence = model.getUnits(player);
		}
		for (IUnit unit : sequence) {
			if (leading) {
				if (leadingFilter.test(unit)) {
					continue;
				} else {
					leading = false;
				}
			} else if (!unit.iterator().hasNext()) {
				continue;
			}
			String orders = unit.getOrders(turn).toLowerCase(); // TODO: add trim(), in separate commit
			if (orders.isEmpty() || orders.contains("todo") || orders.contains("fixme") ||
					orders.contains("xxx")) {
				if (orders.isEmpty()) {
					LOGGER.finer("Orders are empty");
				} else if (orders.contains("todo")) {
					LOGGER.finer("Orders contain 'todo'");
				} else if (orders.contains("fixme")) {
					LOGGER.finer("Orders contain 'fixme'");
				} else if (orders.contains("xxx")) {
					LOGGER.finer("Orders contain 'xxx'");
				} else {
					LOGGER.warning(
						"Orders are not problematic, but called a problem anyway");
				}
				return new TreePath(new Object[] { player, unit.getKind(), unit });
			}
		}
		return null;
	}

	@Override
	public void mapMetadataChanged() {}

	@Override
	public Iterable<Object> childrenOf(final Object obj) {
		if (obj instanceof Player) {
			return (Iterable<Object>) ((Iterable<?>) model.getUnitKinds((Player) obj));
		} else if (obj instanceof String) {
			return (Iterable<Object>) ((Iterable<?>) model.getUnits(player, (String) obj));
		} else if (obj instanceof IUnit) {
			return (Iterable<Object>) ((Iterable<?>) ((IUnit) obj));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void refreshChildren(final IUnit parent) {
		TreeModelEvent event = new TreeModelEvent(this, new TreePath(
			new Object[] { player, parent.getKind(), parent }));
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(event);
		}
	}

	@Override
	public void removeUnit(final IUnit unit) {
		LOGGER.finer("In WorkerTreeModel.removeUnit()");
		// FIXME: What if it's the only unit with this kind?
		TreeModelEvent event = new TreeModelEvent(this,
			new TreePath(new Object[] { player, unit.getKind() }),
				new int[] { getIndexOfChild(unit.getKind(), unit) },
				new Object[] { unit });
		if (model.removeUnit(unit)) {
			LOGGER.finer("Removed unit from the map, about to notify tree listeners");
			for (TreeModelListener listener : listeners) {
				listener.treeNodesRemoved(event);
			}
			LOGGER.finer("Finished notifying tree listeners");
		} else {
			LOGGER.warning("Failed to remove from the map for some reason");
		}
	}

	@Override
	public void changeOwner(final HasMutableOwner item, final Player newOwner) {
		if (item instanceof IUnit && item.getOwner().equals(player)) {
			// TODO: What if it's the only unit with this kind?
			TreeModelEvent event = new TreeModelEvent(this,
				new TreePath(new Object[] { player, ((IUnit) item).getKind() }),
				new int[] { getIndexOfChild(((IUnit) item).getKind(), item) },
				new Object[] { item });
			if (model.changeOwner(item, newOwner)) {
				for (TreeModelListener listener : listeners) {
					listener.treeNodesRemoved(event);
				}
			}
		} else { // FIXME: Also check the case where newOwner is the current player
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

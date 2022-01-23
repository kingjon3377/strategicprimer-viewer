package drivers.worker_mgmt;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import common.map.fixtures.UnitMember;
import javax.swing.tree.TreePath;

import java.util.logging.Logger;
import java.util.logging.Level;
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
	private static Predicate<IUnit> containingItem(UnitMember item) {
		return unit -> StreamSupport.stream(unit.spliterator(), true).anyMatch(item::equals);
	}

	private Player player;
	private final IWorkerModel model;

	public WorkerTreeModel(Player player, IWorkerModel model) {
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
	public Object getChild(Object parent, int index) {
		// TODO: Make IWorkerModel methods return List to simplify this?
		if (parent instanceof Player) {
			return StreamSupport.stream(model.getUnitKinds((Player) parent).spliterator(), false)
				.collect(Collectors.toList()).get(index);
		} else if (parent instanceof String) {
			return StreamSupport.stream(
					model.getUnits(player, (String) parent).spliterator(), false)
				.collect(Collectors.toList()).get(index);
		} else if (parent instanceof IUnit) {
			return StreamSupport.stream(((IUnit) parent).spliterator(), false)
				.collect(Collectors.toList()).get(index);
		} else {
			throw new ArrayIndexOutOfBoundsException("Unrecognized parent");
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof Player) {
			return (int) StreamSupport.stream(model.getUnitKinds((Player) parent).spliterator(),
				false).count();
		} else if (parent instanceof String && StreamSupport.stream(
				model.getUnitKinds(player).spliterator(), false).anyMatch(parent::equals)) {
			return (int) StreamSupport.stream(model.getUnits(player,
				(String) parent).spliterator(), false).count();
		} else if (parent instanceof IUnit) {
			return (int) StreamSupport.stream(((IUnit) parent).spliterator(), false).count();
		} else {
			throw new IllegalArgumentException("Not a possible member of the tree");
		}
	}

	@Override
	public boolean isLeaf(Object node) {
		// TODO: condense further
		if (node instanceof Player || node instanceof IUnit || node instanceof String) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		LOGGER.severe("valueForPathChanged needs to be implemented");
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof Player && child instanceof IUnit) {
			// FIXME: This case shouldn't be allowed, right?
			return StreamSupport.stream(model.getUnits((Player) parent).spliterator(), false)
				.collect(Collectors.toList()).indexOf(child);
		} else if (parent instanceof Player && child instanceof String) {
			return StreamSupport.stream(model.getUnitKinds((Player) parent).spliterator(), false)
				.collect(Collectors.toList()).indexOf(child);
		} else if (parent instanceof String && child instanceof IUnit) {
			return StreamSupport.stream(
					model.getUnits(player, (String) parent).spliterator(), false)
				.collect(Collectors.toList()).indexOf(child);
		} else if (parent instanceof IUnit) {
			return StreamSupport.stream(((IUnit) parent).spliterator(), false)
				.collect(Collectors.toList()).indexOf(child);
		} else {
			return -1;
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
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
	public void addUnit(IUnit unit) {
		model.addUnit(unit);
		TreePath path = new TreePath(new Object[] { player, unit.getKind() });
		int[] indices = new int[] {
			(int) StreamSupport.stream(model.getUnits(player, unit.getKind()).spliterator(),
				false).count() };
		Object[] children = new Object[] { unit };
		TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
	}

	@Override
	public void addNewUnit(IUnit unit) {
		addUnit(unit);
	}

	@Override
	public void playerChanged(@Nullable Player old, Player newPlayer) {
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
	public Object getModelObject(Object obj) {
		return obj;
	}

	@Override
	public void addUnitMember(IUnit unit, UnitMember member) {
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
	public void renameItem(HasMutableName item, String newName) {
		TreePath path;
		int[] indices;
		Object[] children;
		if (item instanceof IUnit) {
			path = new TreePath(new Object[] { player, ((IUnit) item).getKind() });
			indices = new int[] { getIndexOfChild(((IUnit) item).getKind(), item) };
			children = new Object[] { item };
		} else if (item instanceof UnitMember) {
			IUnit parent = StreamSupport.stream(model.getUnits(player).spliterator(), false)
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
	public void changeKind(HasKind item, String newKind) {
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
			IUnit parent = StreamSupport.stream(model.getUnits(player).spliterator(), false)
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
	public void dismissUnitMember(UnitMember member) {
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
	public void addSibling(UnitMember base, UnitMember sibling) {
		for (IUnit unit : model.getUnits(player)) {
			if (StreamSupport.stream(unit.spliterator(), false).anyMatch(base::equals)) {
				int existingMembersCount = (int) StreamSupport.stream(unit.spliterator(),
					false).count();
				model.addSibling(base, sibling);
				int countAfterAdding = (int) StreamSupport.stream(unit.spliterator(),
					false).count();
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
	public TreePath nextProblem(@Nullable TreePath starting, int turn) {
		Iterable<IUnit> sequence;
		boolean leading;
		Predicate<IUnit> leadingFilter;
		if (starting != null) {
			IUnit startingUnit = Stream.of(starting.getPath()).filter(IUnit.class::isInstance)
				.map(IUnit.class::cast).findFirst().orElse(null);
			String startingKind = Stream.of(starting.getPath()).filter(String.class::isInstance)
				.map(String.class::cast).findFirst().orElse(null);
			Iterable<IUnit> temp = model.getUnits(player);
			sequence = Stream.concat(StreamSupport.stream(temp.spliterator(), false),
					StreamSupport.stream(temp.spliterator(), false))
				.collect(Collectors.toList());
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
	public Iterable<Object> childrenOf(Object obj) {
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
	public void refreshChildren(IUnit parent) {
		TreeModelEvent event = new TreeModelEvent(this, new TreePath(
			new Object[] { player, parent.getKind(), parent }));
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(event);
		}
	}

	@Override
	public void removeUnit(IUnit unit) {
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
	public void changeOwner(HasMutableOwner item, Player newOwner) {
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
	public void sortMembers(IUnit fixture) {
		if (model.sortFixtureContents(fixture)) {
			refreshChildren(fixture);
		}
	}
}
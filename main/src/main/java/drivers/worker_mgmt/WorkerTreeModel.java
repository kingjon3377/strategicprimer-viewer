package drivers.worker_mgmt;

import common.map.HasName;
import common.map.HasOwner;
import java.util.Collection;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import common.map.fixtures.UnitMember;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.List;
import common.map.fixtures.mobile.IUnit;
import common.map.Player;
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
		if (parent instanceof Player p) {
			return StreamSupport.stream(model.getUnitKinds(p).spliterator(), false).toList().get(index);
		} else if (parent instanceof String kind) {
			return new ArrayList<>(model.getUnits(player, kind)).get(index);
		} else if (parent instanceof IUnit unit) {
			return (unit).stream().toList().get(index);
		} else {
			throw new ArrayIndexOutOfBoundsException("Unrecognized parent");
		}
	}

	@Override
	public int getChildCount(final Object parent) {
		if (parent instanceof Player p) {
			return (int) StreamSupport.stream(model.getUnitKinds(p).spliterator(),
				false).count();
		} else if (parent instanceof String kind && StreamSupport.stream(
				model.getUnitKinds(player).spliterator(), false).anyMatch(parent::equals)) {
			return model.getUnits(player, kind).size();
		} else if (parent instanceof IUnit unit) {
			return (int) unit.stream().count();
		} else {
			throw new IllegalArgumentException("Not a possible member of the tree");
		}
	}

	@Override
	public boolean isLeaf(final Object node) {
		return !(node instanceof Player) && !(node instanceof IUnit) && !(node instanceof String);
	}

	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		LovelaceLogger.error("valueForPathChanged needs to be implemented");
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		if (parent instanceof Player p && child instanceof IUnit) {
			// FIXME: This case shouldn't be allowed, right?
			return new ArrayList<>(model.getUnits(p)).indexOf(child);
		} else if (parent instanceof Player p && child instanceof String) {
			return StreamSupport.stream(model.getUnitKinds(p).spliterator(), false).toList().indexOf(child);
		} else if (parent instanceof String kind && child instanceof IUnit) {
			return new ArrayList<>(model.getUnits(player, kind)).indexOf(child);
		} else if (parent instanceof IUnit unit) {
			return unit.stream().toList().indexOf(child);
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
		final int oldIndex = getIndexOfChild(old, member);
		final TreeModelEvent removedEvent = new TreeModelEvent(this,
			new TreePath(new Object[] { player, old.getKind(), old }),
				new int[] { oldIndex }, new Object[] { member });
		final TreeModelEvent removedChangeEvent = new TreeModelEvent(this,
			new TreePath(new Object[] { player, old.getKind(), old }));
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesRemoved(removedEvent);
			listener.treeStructureChanged(removedChangeEvent);
		}
		model.moveMember(member, old, newOwner);
		final TreeModelEvent insertedEvent = new TreeModelEvent(this,
			new TreePath(new Object[] { player, newOwner.getKind(), newOwner }),
				new int[] { getIndexOfChild(newOwner, member) }, new Object[] { member });
		final TreeModelEvent insertedChangeEvent = new TreeModelEvent(this,
			new TreePath(new Object[] { player, newOwner.getKind(), newOwner }));
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesInserted(insertedEvent);
			listener.treeStructureChanged(insertedChangeEvent);
		}
	}

	@Override
	public void addUnit(final IUnit unit) {
		model.addUnit(unit);
		final TreePath path = new TreePath(new Object[] { player, unit.getKind() });
		final int[] indices = { model.getUnits(player, unit.getKind()).size()};
		final Object[] children = { unit };
		final TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
	}

	@Override
	public void addNewUnit(final IUnit unit) {
		addUnit(unit);
	}

	@Override
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
		player = newPlayer;
		final TreeModelEvent event = new TreeModelEvent(this, new TreePath(getRoot()));
		for (final TreeModelListener listener : listeners) {
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
		LovelaceLogger.trace("In WorkerTreeModel.addUnitMember");
		model.addUnitMember(unit, member);
		LovelaceLogger.trace("Added member to unit");
		final TreePath path = new TreePath(new Object[] { player, unit.getKind(), unit });
		final int[] indices = { getIndexOfChild(unit, member) };
		final Object[] children = { member };
		final TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
		LovelaceLogger.trace("Notified listeners of inserted nodes");
	}

	@Override
	public void renameItem(final HasName item, final String newName) {
		final TreePath path;
		final int[] indices;
		final Object[] children;
		if (item instanceof IUnit unit) {
			path = new TreePath(new Object[] { player, unit.getKind() });
			indices = new int[] { getIndexOfChild(unit.getKind(), item) };
			children = new Object[] { item };
		} else if (item instanceof UnitMember member) {
			final IUnit parent = model.getUnits(player).stream()
				.filter(containingItem(member)).findAny().orElse(null);
			if (parent == null) {
				LovelaceLogger.warning(
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
			LovelaceLogger.warning(
				"In WorkerTreeModel.renameItem(), item was neither unit nor unit member");
			// Ignore, as it's something we don't know how to handle.
			// If we see log messages, revisit.
			return;
		}
		if (model.renameItem(item, newName)) {
			final TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
			for (final TreeModelListener listener : listeners) {
				listener.treeNodesChanged(event);
			}
		}
	}

	@Override
	public void changeKind(final HasKind item, final String newKind) {
		final TreePath path;
		final int[] indices;
		final Object[] children;
		if (item instanceof IUnit) {
			// TODO: should probably fire removal and addition events instead
			path = new TreePath(new Object[] { player });
			indices = new int[] { getIndexOfChild(player, item.getKind()),
				getIndexOfChild(player, newKind) };
			children = new Object[] { item.getKind(), newKind };
		} else if (item instanceof UnitMember um) {
			final IUnit parent = model.getUnits(player).stream()
				.filter(containingItem(um)).findAny().orElse(null);
			if (parent == null) {
				LovelaceLogger.warning(
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
		final TreeModelEvent event = new TreeModelEvent(this, path, indices, children);
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesChanged(event);
		}
	}

	@Override
	public void dismissUnitMember(final UnitMember member) {
		for (final IUnit unit : model.getUnits(player)) {
			int index = 0;
			for (final UnitMember item : unit) {
				if (member.equals(item)) {
					model.dismissUnitMember(member);
					final TreeModelEvent event = new TreeModelEvent(this,
						new TreePath(new Object[] { player, unit }),
							new int[] { index }, new Object[] { member });
					for (final TreeModelListener listener : listeners) {
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
		for (final IUnit unit : model.getUnits(player)) {
			if (unit.stream().anyMatch(Predicate.isEqual(base))) {
				final int existingMembersCount = (int) unit.stream().count();
				model.addSibling(base, sibling);
				final int countAfterAdding = (int) unit.stream().count();
				final TreeModelEvent event;
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
				for (final TreeModelListener listener : listeners) {
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
	public @Nullable TreePath nextProblem(final @Nullable TreePath starting, final int turn) {
		Iterable<IUnit> sequence;
		boolean leading;
		final Predicate<IUnit> leadingFilter;
		if (starting == null) {
			leading = false;
			leadingFilter = null;
			sequence = model.getUnits(player);
		} else {
			final IUnit startingUnit = Stream.of(starting.getPath()).filter(IUnit.class::isInstance)
					.map(IUnit.class::cast).findFirst().orElse(null);
			final String startingKind = Stream.of(starting.getPath()).filter(String.class::isInstance)
					.map(String.class::cast).findFirst().orElse(null);
			final Collection<IUnit> temp = model.getUnits(player);
			sequence = Stream.concat(temp.stream(), temp.stream()).collect(Collectors.toList());
			if (startingUnit != null) {
				leading = true;
				leadingFilter = startingUnit::equals;
			} else if (startingKind == null) {
				leading = false;
				leadingFilter = null;
				sequence = model.getUnits(player);
			} else {
				leading = true;
				leadingFilter = unit -> startingKind.equals(unit.getKind());
			}
		}
		for (final IUnit unit : sequence) {
			if (leading) {
				if (leadingFilter.test(unit)) {
					continue;
				} else {
					leading = false;
				}
			} else if (!unit.iterator().hasNext()) {
				continue;
			}
			final String orders = unit.getOrders(turn).toLowerCase().strip();
			if (orders.isEmpty() || orders.contains("todo") || orders.contains("fixme") ||
					orders.contains("xxx")) {
				if (orders.isEmpty()) {
					LovelaceLogger.trace("Orders are empty");
				} else if (orders.contains("todo")) {
					LovelaceLogger.trace("Orders contain 'todo'");
				} else if (orders.contains("fixme")) {
					LovelaceLogger.trace("Orders contain 'fixme'");
				} else if (orders.contains("xxx")) {
					LovelaceLogger.trace("Orders contain 'xxx'");
				} else {
					LovelaceLogger.warning(
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
		if (obj instanceof Player p) {
			return (Iterable<Object>) ((Iterable<?>) model.getUnitKinds(p));
		} else if (obj instanceof String kind) {
			return (Iterable<Object>) ((Iterable<?>) model.getUnits(player, kind));
		} else if (obj instanceof IUnit) {
			return (Iterable<Object>) obj;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void refreshChildren(final IUnit parent) {
		final TreeModelEvent event = new TreeModelEvent(this, new TreePath(
			new Object[] { player, parent.getKind(), parent }));
		for (final TreeModelListener listener : listeners) {
			listener.treeStructureChanged(event);
		}
	}

	@Override
	public void removeUnit(final IUnit unit) {
		LovelaceLogger.trace("In WorkerTreeModel.removeUnit()");
		// FIXME: What if it's the only unit with this kind?
		final TreeModelEvent event = new TreeModelEvent(this,
			new TreePath(new Object[] { player, unit.getKind() }),
				new int[] { getIndexOfChild(unit.getKind(), unit) },
				new Object[] { unit });
		if (model.removeUnit(unit)) {
			LovelaceLogger.trace("Removed unit from the map, about to notify tree listeners");
			for (final TreeModelListener listener : listeners) {
				listener.treeNodesRemoved(event);
			}
			LovelaceLogger.trace("Finished notifying tree listeners");
		} else {
			LovelaceLogger.warning("Failed to remove from the map for some reason");
		}
	}

	@Override
	public void changeOwner(final HasOwner item, final Player newOwner) {
		if (item instanceof IUnit unit && item.owner().equals(player)) {
			// TODO: What if it's the only unit with this kind?
			final TreeModelEvent event = new TreeModelEvent(this,
				new TreePath(new Object[] { player, unit.getKind() }),
				new int[] { getIndexOfChild(unit.getKind(), item) },
				new Object[] { item });
			if (model.changeOwner(item, newOwner)) {
				for (final TreeModelListener listener : listeners) {
					listener.treeNodesRemoved(event);
				}
			}
		} else if (item instanceof IUnit unit && newOwner.equals(player)) {
			final TreeModelEvent event;
			final String kind = unit.getKind();
			// TODO: Make getUnitKinds() return Collection
			final boolean existingKind = StreamSupport.stream(model.getUnitKinds(player).spliterator(), false)
					.anyMatch(kind::equals);
			if (!model.changeOwner(item, newOwner)) {
				return;
			}
			// TODO: double-check I passed the parameters a nodes-inserted listener expects
			if (existingKind) {
				event = new TreeModelEvent(this, new TreePath(new Object[] { player }),
						new int[] { getIndexOfChild(player, kind)},
						new Object[] { kind });
			} else {
				event = new TreeModelEvent(this, new TreePath(new Object[] { player, kind }),
						new int[] { getIndexOfChild(kind, item) }, new Object[] { item });
			}
			if (model.changeOwner(item, newOwner)) {
				for (final TreeModelListener listener : listeners) {
					listener.treeNodesInserted(event);
				}
			}
		}
	}

	@Override
	public void sortMembers(final IUnit fixture) {
		if (model.sortFixtureContents(fixture)) {
			refreshChildren(fixture);
		}
	}
}

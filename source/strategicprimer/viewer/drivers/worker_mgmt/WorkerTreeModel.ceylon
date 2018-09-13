import javax.swing.event {
    TreeModelListener,
    TreeModelEvent
}
import strategicprimer.model.map.fixtures {
    UnitMember
}
import javax.swing.tree {
    TreePath
}
import java.lang {
    ArrayIndexOutOfBoundsException,
    ObjectArray,
    IntArray,
    JString=String
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.map {
    HasMutableName,
    Player,
    HasKind
}
import strategicprimer.drivers.worker.common {
    IWorkerModel,
    IWorkerTreeModel
}
import lovelace.util.common {
    matchingPredicate
}
"A TreeModel implementation for a player's units and workers."
class WorkerTreeModel satisfies IWorkerTreeModel {
	static Boolean(IUnit) containingItem(UnitMember item) =>
                    shuffle(IUnit.contains)(item);
	variable Player player;
	IWorkerModel model;
	shared new (Player player, IWorkerModel model) {
		this.player = player;
		this.model = model;
	}

    MutableList<UnitMember> dismissedMembers = ArrayList<UnitMember>();
    MutableList<TreeModelListener> listeners = ArrayList<TreeModelListener>();
    shared actual Player root => player;

    // TODO: We want to add a 'Fortress' level to the tree

    shared actual Object getChild(Object parent, Integer index) {
        if (is Player parent) {
            if (exists child = model.getUnitKinds(parent).getFromFirst(index)) {
                return child;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else if (is String parent, model.getUnitKinds(player).contains(parent)) {
            if (exists child = model.getUnits(player, parent).getFromFirst(index)) {
                return child;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else if (is IUnit parent) {
            if (exists child = parent.getFromFirst(index)) {
                return child;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else {
            throw ArrayIndexOutOfBoundsException("Unrecognized parent");
        }
    }

    shared actual Integer getChildCount(Object parent) {
        if (is Player parent) {
            return model.getUnitKinds(parent).size;
        } else if (is String parent, model.getUnitKinds(player).contains(parent)) {
            return model.getUnits(player, parent).size;
        } else if (is IUnit parent) {
            return parent.size;
        } else {
            throw AssertionError("Not a possible member of the tree");
        }
    }

    shared actual Boolean isLeaf(Object node) => !node is Player|IUnit|String;
    shared actual void valueForPathChanged(TreePath path, Object newValue) =>
            log.error("valueForPathChanged needs to be implemented");

    shared actual Integer getIndexOfChild(Object parent, Object child) {
        if (is JString parent) {
            return getIndexOfChild(parent.string, child);
        } else if (is JString child) {
            return getIndexOfChild(parent, child.string);
        } else if (is Player parent, is IUnit child) {
            return model.getUnits(parent).locate(child.equals)?.key else -1;
        } else if (is Player parent, is String child) {
            return model.getUnitKinds(parent).locate(child.equals)?.key else -1;
        } else if (is String parent, is IUnit child) {
            return model.getUnits(root, parent).locate(child.equals)?.key else -1;
        } else if (is IUnit parent,
            exists index->ignored = parent.locate(child.equals)) {
            return index;
        } else {
            return -1;
        }
    }

    shared actual void addTreeModelListener(TreeModelListener listener) =>
            listeners.add(listener);
    shared actual void removeTreeModelListener(TreeModelListener listener) =>
            listeners.remove(listener);

	void markModified() {
		for (map->[file, modified] in model.allMaps) {
			if (!modified) {
				model.setModifiedFlag(map, true);
			}
		}
	}
    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        Integer oldIndex = getIndexOfChild(old, member);
        TreeModelEvent removedEvent = TreeModelEvent(this,
            TreePath(ObjectArray<Object>.with([ root, old.kind, old ])),
            IntArray.with(Singleton(oldIndex)),
            ObjectArray.with(Singleton(member)));
        TreeModelEvent removedChangeEvent = TreeModelEvent(this,
            TreePath(ObjectArray<Object>.with([ root, old.kind, old ])));
        for (listener in listeners) {
            listener.treeNodesRemoved(removedEvent);
            listener.treeStructureChanged(removedChangeEvent);
        }
        newOwner.addMember(member);
        TreeModelEvent insertedEvent = TreeModelEvent(this,
            TreePath(ObjectArray<Object>.with([ root, newOwner.kind, newOwner ])),
            IntArray.with(Singleton(getIndexOfChild(newOwner, member))),
            ObjectArray<Object>.with(Singleton(member)));
        TreeModelEvent insertedChangeEvent = TreeModelEvent(this,
            TreePath(ObjectArray<Object>.with([ root, newOwner.kind, newOwner ])));
        for (listener in listeners) {
            listener.treeNodesInserted(insertedEvent);
            listener.treeStructureChanged(insertedChangeEvent);
        }
		markModified();
    }
    shared actual void addUnit(IUnit unit) {
        model.addUnit(unit);
        TreePath path = TreePath(ObjectArray<Object>.with([root, unit.kind]));
        value indices = IntArray.with(Singleton(model.getUnits(player, unit.kind).size));
        value children = ObjectArray.with(Singleton(unit));
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesInserted(event);
        }
		markModified();
    }
    shared actual void addNewUnit(IUnit unit) => addUnit(unit);
    shared actual void mapChanged() {
        player = model.currentPlayer;
        TreePath path = TreePath(root);
        TreeModelEvent event = TreeModelEvent(this, path);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }
    shared actual void playerChanged(Player? old, Player newPlayer) {
        player = newPlayer;
        TreeModelEvent event = TreeModelEvent(this, TreePath(root));
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }
    shared actual Object getModelObject(Object obj) => obj;
    shared actual void addUnitMember(IUnit unit, UnitMember member) {
        log.trace("In WorkerTreeModel.addUnitMember");
        unit.addMember(member);
        log.trace("Added member to unit");
        TreePath path = TreePath(ObjectArray<Object>.with([root, unit.kind, unit]));
        IntArray indices = IntArray.with(Singleton(getIndexOfChild(unit, member)));
        ObjectArray<Object> children = ObjectArray<Object>.with(Singleton(member));
        TreeModelEvent event = TreeModelEvent(this, path, indices,
            children);
        for (listener in listeners) {
            listener.treeNodesInserted(event);
        }
        log.trace("Notified listeners of inserted nodes");
		markModified();
    }
    shared actual void renameItem(HasMutableName item) {
        TreePath path;
        IntArray indices;
        ObjectArray<Object> children;
        if (is IUnit item) {
            path = TreePath(ObjectArray<Object>.with([root, item.kind]));
            indices = IntArray.with(Singleton(getIndexOfChild(item.kind, item)));
            children = ObjectArray<Object>.with(Singleton(item));
        } else if (is UnitMember item,
            exists parent = model.getUnits(player).find(containingItem(item))) {
            path = TreePath(ObjectArray<Object>.with([root, parent.kind, parent]));
            indices = IntArray.with(Singleton(getIndexOfChild(parent, item)));
            children = ObjectArray<Object>.with(Singleton(item));
        } else if (is Player item) {
            // ignore
            return;
        } else {
            log.warn(
                "In WorkerTreeModel.renameItem(), item was neither unit nor unit member");
            // Ignore, as it's something we don't know how to handle.
            // If we see log messages, revisit.
            return;
        }
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
		markModified();
    }
    shared actual void moveItem(HasKind item, String priorKind) {
        TreePath path;
        IntArray indices;
        ObjectArray<Object> children;
        if (is IUnit item) {
            path = TreePath(ObjectArray.with(Singleton(root)));
            indices = IntArray.with([getIndexOfChild(root, priorKind),
                getIndexOfChild(root, item.kind)]);
            children = ObjectArray<Object>.with([priorKind, item.kind]);
        } else if (is UnitMember item,
            exists parent = model.getUnits(player).find(containingItem(item))) {
            path = TreePath(ObjectArray<Object>.with([root, parent.kind, parent]));
            indices = IntArray.with(Singleton(getIndexOfChild(parent, item)));
            children = ObjectArray<Object>.with(Singleton(item));
        } else {
            // Impossible at present, so ignore
            return;
        }
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
		markModified();
    }
    shared actual void dismissUnitMember(UnitMember member) {
        for (unit in model.getUnits(root)) {
            if (exists index->item = unit.locate(member.equals)) {
                dismissedMembers.add(member);
                unit.removeMember(member);
                TreeModelEvent event = TreeModelEvent(this,
                    TreePath(ObjectArray<Object>.with([root, unit])),
                    IntArray.with(Singleton(index)), ObjectArray.with(Singleton(member)));
                for (listener in listeners) {
                    listener.treeNodesRemoved(event);
                }
            }
        }
		markModified();
    }
    shared actual {UnitMember*} dismissed => dismissedMembers;
    shared actual void addSibling(UnitMember base, UnitMember sibling) {
        for (unit in model.getUnits(root)) {
            if (exists index->item = unit.locate(base.equals)) {
                Integer existingMembersCount = unit.size;
                unit.addMember(sibling);
                Integer countAfterAdding = unit.size;
                if (countAfterAdding > existingMembersCount) {
                    TreeModelEvent event = TreeModelEvent(this,
                            TreePath(ObjectArray<Object>.with([root, unit.kind, unit])),
                            IntArray.with(Singleton(existingMembersCount)),
                            ObjectArray.with(Singleton(sibling)));
                    for (listener in listeners) {
                        listener.treeNodesInserted(event);
                    }
                } else {
                    TreeModelEvent event = TreeModelEvent(this,
                            TreePath(ObjectArray<Object>.with([root, unit.kind])),
                            IntArray.with(Singleton(getIndexOfChild(unit.kind, unit))),
                            ObjectArray.with(Singleton(unit)));
                    for (listener in listeners) {
                        listener.treeStructureChanged(event);
                    }
                }
            }
        }
		markModified();
    }
    """Get the path to the "next" unit whose orders for the given turn either contain
       "TODO", contain "FIXME", or are empty. Returns null if no unit matches those
       criteria."""
    shared actual TreePath? nextProblem(TreePath? starting, Integer turn) {
        {IUnit*} sequence;
        if (exists starting, exists startingUnit = starting.path.array.narrow<IUnit>().first) {
            sequence = model.getUnits(root).repeat(2).sequence().trimLeading(not(startingUnit.equals)).rest;
        } else if (exists starting, exists startingKind = starting.path.array.narrow<String>().first) {
            sequence = model.getUnits(root).repeat(2).sequence().trimLeading(not(matchingPredicate(startingKind.equals, IUnit.kind)));
        } else {
            sequence = model.getUnits(root);
        }
        for (unit in sequence) {
            String orders = unit.getOrders(turn).lowercased;
            if (orders.empty || orders.contains("todo") || orders.contains("fixme")) {
                return TreePath(ObjectArray<Object>.with([root, unit.kind, unit]));
            }
        }
        return null;
    }
}

import javax.swing.event {
    TreeModelListener,
    TreeModelEvent
}
import strategicprimer.model.common.map.fixtures {
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
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map {
    Player,
    HasMutableName,
    HasKind
}
import strategicprimer.drivers.worker.common {
    IWorkerTreeModel
}
import strategicprimer.drivers.common {
    IWorkerModel
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

    deprecated("Move logic that needed this into the driver model")
    void markModified() {
        for (map in model.allMaps) {
            if (!map.modified) {
                model.setMapModified(map, true);
            }
        }
    }
    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        Integer oldIndex = getIndexOfChild(old, member);
        TreeModelEvent removedEvent = TreeModelEvent(this,
            TreePath(ObjectArray<Object>.with([ root, old.kind, old ])),
            IntArray.with(Singleton(oldIndex)), ObjectArray.with(Singleton(member)));
        TreeModelEvent removedChangeEvent = TreeModelEvent(this,
            TreePath(ObjectArray<Object>.with([ root, old.kind, old ])));
        for (listener in listeners) {
            listener.treeNodesRemoved(removedEvent);
            listener.treeStructureChanged(removedChangeEvent);
        }
        model.moveMember(member, old, newOwner);
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
    }

    shared actual void addNewUnit(IUnit unit) => addUnit(unit);

    shared actual void playerChanged(Player? old, Player newPlayer) {
        player = newPlayer;
        TreeModelEvent event = TreeModelEvent(this, TreePath(root));
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }

    shared actual void mapChanged() => playerChanged(player, model.currentPlayer);

    shared actual Object getModelObject(Object obj) => obj;

    shared actual void addUnitMember(IUnit unit, UnitMember member) {
        log.trace("In WorkerTreeModel.addUnitMember");
        model.addUnitMember(unit, member);
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
    }

    shared actual void renameItem(HasMutableName item, String newName) {
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
        if (model.renameItem(item, newName)) {
            TreeModelEvent event = TreeModelEvent(this, path, indices, children);
            for (listener in listeners) {
                listener.treeNodesChanged(event);
            }
        }
    }

    shared actual void changeKind(HasKind item, String newKind) {
        TreePath path;
        IntArray indices;
        ObjectArray<Object> children;
        if (is IUnit item) {
            path = TreePath(ObjectArray.with(Singleton(root)));
            indices = IntArray.with([getIndexOfChild(root, item.kind),
                getIndexOfChild(root, newKind)]);
            children = ObjectArray<Object>.with([item.kind, newKind]);
        } else if (is UnitMember item,
                exists parent = model.getUnits(player).find(containingItem(item))) {
            path = TreePath(ObjectArray<Object>.with([root, parent.kind, parent]));
            indices = IntArray.with(Singleton(getIndexOfChild(parent, item)));
            children = ObjectArray<Object>.with(Singleton(item));
        } else {
            // Impossible at present, so ignore
            return;
        }
        model.changeKind(item, newKind);
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }

    shared actual void dismissUnitMember(UnitMember member) {
        for (unit in model.getUnits(root)) {
            if (exists index->item = unit.locate(member.equals)) {
                model.dismissUnitMember(member);
                TreeModelEvent event = TreeModelEvent(this,
                    TreePath(ObjectArray<Object>.with([root, unit])),
                    IntArray.with(Singleton(index)), ObjectArray.with(Singleton(member)));
                for (listener in listeners) {
                    listener.treeNodesRemoved(event);
                }
            }
        }
    }

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
       "TODO", contain "FIXME", contain "XXX", or are empty. Skips units with no members.
       Returns null if no unit matches those criteria."""
    shared actual TreePath? nextProblem(TreePath? starting, Integer turn) {
        {IUnit*} sequence;
        if (exists starting, exists startingUnit =
                starting.path.array.narrow<IUnit>().first) {
            sequence = model.getUnits(root).repeat(2).sequence()
                .trimLeading(not(startingUnit.equals)).rest;
        } else if (exists starting, exists startingKind =
                starting.path.array.narrow<String>().first) {
            sequence = model.getUnits(root).repeat(2).sequence()
                .trimLeading(not(compose(startingKind.equals, IUnit.kind)));
        } else if (exists starting, exists startingKind =
                starting.path.array.narrow<JString>().first) {
            return nextProblem(TreePath(ObjectArray<Object>
                .with([root, startingKind.string])), turn);
        } else {
            sequence = model.getUnits(root);
        }
        for (unit in sequence) {
            if (unit.empty) {
                continue;
            }
            String orders = unit.getOrders(turn).lowercased;
            if (orders.empty || orders.contains("todo") || orders.contains("fixme") ||
                    orders.contains("xxx")) {
                if (orders.empty) {
                    log.trace("Orders are empty");
                } else if (orders.contains("todo")) {
                    log.trace("Orders contain 'todo'");
                } else if (orders.contains("fixme")) {
                    log.trace("Orders contain 'fixme'");
                } else if (orders.contains("xxx")) {
                    log.trace("Orders contain 'xxx'");
                } else {
                    log.warn("Orders are not problematic, but called a problem anyway");
                }
                return TreePath(ObjectArray<Object>.with([root, unit.kind, unit]));
            }
        }
        return null;
    }

    shared actual void mapMetadataChanged() {}
    shared actual {Object*} childrenOf(Object obj) {
        if (is Player obj) {
            return model.getUnitKinds(obj);
        } else if (is String obj) {
            return model.getUnits(root, obj);
        } else if (is JString obj) {
            return model.getUnits(root, obj.string);
        } else if (is IUnit obj) {
            return obj;
        } else {
            return [];
        }
    }

    shared actual void refreshChildren(IUnit parent) {
        TreeModelEvent event = TreeModelEvent(this, TreePath(ObjectArray<Object>.with([root, parent.kind, parent])));
        for (listener in listeners) {
            listener.treeStructureChanged(event);
        }
    }

    shared actual void removeUnit(IUnit unit) {
        log.trace("In WorkerTreeModel.removeUnit()");
        TreeModelEvent event = TreeModelEvent(this, TreePath(ObjectArray<Object>.with([root, unit.kind])),
            IntArray.with(Singleton(getIndexOfChild(unit.kind, unit))), ObjectArray<Object>.with(Singleton(unit)));
        if (model.removeUnit(unit)) {
            log.trace("Removed unit from the map, about to notify tree listeners");
            for (listener in listeners) {
                listener.treeNodesRemoved(event);
            }
            log.trace("Finished notifying tree listeners");
        } else {
            log.warn("Failed to remove from the map for some reason");
        }
    }
}

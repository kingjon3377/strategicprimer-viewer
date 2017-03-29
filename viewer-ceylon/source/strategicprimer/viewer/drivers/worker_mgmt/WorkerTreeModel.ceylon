import javax.swing.event {
    TreeModelListener,
    TreeModelEvent
}
import model.map.fixtures {
    UnitMember
}
import javax.swing.tree {
    TreePath
}
import java.lang {
    ArrayIndexOutOfBoundsException,
    ObjectArray,
    IntArray,
    IllegalArgumentException
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit
}
import strategicprimer.viewer.model.map {
    HasMutableName
}
import model.map {
    HasKind,
    Player
}
import ceylon.interop.java {
    createJavaIntArray,
    CeylonIterable,
    createJavaObjectArray
}
"A TreeModel implementation for a player's units and workers."
class WorkerTreeModel(variable Player player, IWorkerModel model)
        satisfies IWorkerTreeModel {
    MutableList<UnitMember> dismissedMembers = ArrayList<UnitMember>();
    MutableList<TreeModelListener> listeners = ArrayList<TreeModelListener>();
    shared actual Player root => player;

    shared actual Object getChild(Object parent, Integer index) {
        if (is Player parent) {
            if (exists child = model.getUnitKinds(parent).getFromFirst(index)) {
                return child;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else if (is String parent, model.getUnitKinds(player).contains(parent)) {
            if (exists child = model.getUnits(player, parent)
                .getFromFirst(index)) {
                return child;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else if (is IUnit parent) {
            if (exists child = parent.getFromFirst(index)) {
                return index;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else {
            throw ArrayIndexOutOfBoundsException("Unrecognized parent");
        }
    }

    shared actual Integer getChildCount(Object parent) {
        if (is Player parent) {
            return model.getUnits(parent).size;
        } else if (is String parent, model.getUnitKinds(player).contains(parent)) {
            return model.getUnits(player, parent).size;
        } else if (is IUnit parent) {
            return parent.size;
        } else {
            throw IllegalArgumentException("Not a possible member of the tree");
        }
    }

    shared actual Boolean isLeaf(Object node) => !node is Player|IUnit|String;
    shared actual void valueForPathChanged(TreePath path, Object newValue) =>
            log.error("valueForPathChanged needs to be implemented");

    shared actual Integer getIndexOfChild(Object parent, Object child) {
        if (is Player parent, is IUnit child) {
            if (exists key->item = model.getUnits(parent).locate(child.equals)) {
                return key;
            } else {
                return -1;
            }
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

    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        Integer oldIndex = getIndexOfChild(old, member);
        TreeModelEvent removedEvent = TreeModelEvent(this,
            TreePath(createJavaObjectArray({ root, old })), createJavaIntArray({ oldIndex }),
            createJavaObjectArray({ member }));
        TreeModelEvent removedChangeEvent = TreeModelEvent(this,
            TreePath(createJavaObjectArray({ root, old })));
        for (listener in listeners) {
            listener.treeNodesRemoved(removedEvent);
            listener.treeStructureChanged(removedChangeEvent);
        }
        newOwner.addMember(member);
        TreeModelEvent insertedEvent = TreeModelEvent(this,
            TreePath(createJavaObjectArray<Object>({ root, newOwner })),
            createJavaIntArray({ getIndexOfChild(newOwner, member) }),
            createJavaObjectArray<Object>({ member }));
        TreeModelEvent insertedChangeEvent = TreeModelEvent(this,
            TreePath(createJavaObjectArray<Object>({ root, newOwner })));
        for (listener in listeners) {
            listener.treeNodesInserted(insertedEvent);
            listener.treeStructureChanged(insertedChangeEvent);
        }
    }
    shared actual void addUnit(IUnit unit) {
        model.addUnit(unit);
        TreePath path = TreePath(root);
        value indices = createJavaIntArray({model.getUnits(player).size});
        value children = createJavaObjectArray({unit});
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesInserted(event);
        }
    }
    shared actual void addNewUnit(IUnit unit) => addUnit(unit);
    shared actual void mapChanged() {
        player = model.map.currentPlayer;
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
        unit.addMember(member);
        TreePath path = TreePath(createJavaObjectArray({root, unit}));
        IntArray indices = createJavaIntArray({getIndexOfChild(unit, member)});
        ObjectArray<Object> children = createJavaObjectArray<Object>({member});
        TreeModelEvent event = TreeModelEvent(this, path, indices,
            children);
        for (listener in listeners) {
            listener.treeNodesInserted(event);
        }
    }
    shared actual void renameItem(HasMutableName item) {
        TreePath path;
        IntArray indices;
        ObjectArray<Object> children;
        if (is IUnit item) {
            path = TreePath(createJavaObjectArray({root}));
            indices = createJavaIntArray({getIndexOfChild(root, item)});
            children = createJavaObjectArray<Object>({item});
        } else if (is UnitMember item,
            exists parent = model.getUnits(player)
                .find((unit) => unit.contains(item))) {
            path = TreePath(createJavaObjectArray({root, parent}));
            indices = createJavaIntArray({getIndexOfChild(parent, item)});
            children = createJavaObjectArray<Object>({item});
        } else {
            // Probably the player; in any case, ignore.
            return;
        }
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }
    shared actual void moveItem(HasKind item) {
        TreePath path;
        IntArray indices;
        ObjectArray<Object> children;
        if (is IUnit item) {
            path = TreePath(createJavaObjectArray({root}));
            indices = createJavaIntArray({getIndexOfChild(root, item)});
            children = createJavaObjectArray<Object>({item});
        } else if (is UnitMember item,
            exists parent = model.getUnits(player)
                .find((unit) => unit.contains(item))) {
            path = TreePath(createJavaObjectArray({root, parent}));
            indices = createJavaIntArray({getIndexOfChild(parent, item)});
            children = createJavaObjectArray<Object>({item});
        } else {
            // Impossible at present, so ignore
            return;
        }
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }
    shared actual void dismissUnitMember(UnitMember member) {
        for (unit in model.getUnits(root)) {
            if (exists index->item = unit.locate(member.equals)) {
                dismissedMembers.add(member);
                unit.removeMember(member);
                TreeModelEvent event = TreeModelEvent(this,
                    TreePath(createJavaObjectArray({root, unit})),
                    createJavaIntArray({index}), createJavaObjectArray({member}));
                for (listener in listeners) {
                    listener.treeNodesRemoved(event);
                }
            }
        }
    }
    shared actual {UnitMember*} dismissed => dismissedMembers;
}

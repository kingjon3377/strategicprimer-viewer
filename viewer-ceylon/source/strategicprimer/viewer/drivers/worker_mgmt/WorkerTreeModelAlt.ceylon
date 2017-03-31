import java.lang {
    ObjectArray,
    IllegalArgumentException
}
import strategicprimer.viewer.model.map.fixtures {
    UnitMember
}
import javax.swing.tree {
    DefaultTreeModel,
    MutableTreeNode,
    DefaultMutableTreeNode,
    TreeNode
}
import ceylon.collection {
    LinkedList,
    ArrayList,
    Queue,
    MutableList
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    ProxyFor,
    IUnit,
    ProxyUnit
}
import strategicprimer.viewer.model.map {
    HasMutableName,
    Player,
    HasKind
}
import ceylon.interop.java {
    createJavaIntArray,
    createJavaObjectArray
}
"An alternative implementation of the worker tree model."
shared class WorkerTreeModelAlt extends DefaultTreeModel satisfies IWorkerTreeModel {
    shared static class WorkerTreeNode<T>(T userObj, Boolean permitsChildren = true)
            extends DefaultMutableTreeNode(userObj, permitsChildren)
            satisfies Iterable<TreeNode> given T satisfies Object {
        shared actual Iterator<TreeNode> iterator() {
            value wrapped = children();
            object retval satisfies Iterator<TreeNode> {
                shared actual TreeNode|Finished next() {
                    if (wrapped.hasMoreElements()) {
                        assert (is TreeNode item = wrapped.nextElement());
                        return item;
                    } else {
                        return finished;
                    }
                }
            }
            return retval;
        }
        // Can't refine userObject to narrow its type because that would narrow the type
        // of the setter as well, which Ceylon's type system doesn't allow.
        shared T userObjectNarrowed {
            assert (is T narrowed = userObject);
            return narrowed;
        }
        shared actual void setUserObject(Object obj) {
            if (is T obj) {
                (super of DefaultMutableTreeNode).userObject =obj;
            } else {
                throw IllegalArgumentException("PlayerNode can only contain Player");
            }
        }
        shared actual String string => (super of DefaultMutableTreeNode).string;
    }
    static class UnitMemberNode(UnitMember member)
            extends WorkerTreeNode<UnitMember>(member, false) { }
    shared static class UnitNode(IUnit unit) extends WorkerTreeNode<IUnit>(unit) {
        for (index->member in unit.indexed) {
            insert(UnitMemberNode(member), index);
        }
    }
    shared static class KindNode(String kind, IUnit* units)
            extends WorkerTreeNode<String>(kind) {
        for (index->unit in units.indexed) {
            insert(UnitNode(unit), index);
        }
    }
    static class PlayerNode(Player player, IWorkerModel model)
            extends WorkerTreeNode<Player>(player) {
        for (index->kind in model.getUnitKinds(player).indexed) {
            insert(KindNode(kind, *model.getUnits(player, kind)), index);
        }
    }
    static Boolean areTreeObjectsEqual(TreeNode node, Object obj) {
        if (is DefaultMutableTreeNode node, obj == node.userObject) {
            return true;
        } else {
            return false;
        }
    }
    "Get the node in the subtree under the given node that represents the given object."
    static MutableTreeNode? getNode(TreeNode node, Object obj) {
        if (is MutableTreeNode node, areTreeObjectsEqual(node, obj)) {
            return node;
        } else if (is WorkerTreeNode<out Anything> node, node.allowsChildren) {
            for (child in node) {
                if (exists result = getNode(child, obj)) {
                    return result;
                }
            }
        }
        return null;
    }
    IWorkerModel model;
    MutableList<UnitMember> dismissedMembers = ArrayList<UnitMember>();
    shared new (Player player, IWorkerModel driverModel)
            extends DefaultTreeModel(PlayerNode(player, driverModel), true) {
        model = driverModel;
    }
    void moveProxied(UnitMember member, ProxyUnit old, ProxyUnit newOwner,
            UnitNode newNode, MutableTreeNode node) {
        assert (is PlayerNode playerNode = root);
        // TODO: encode this second assertion into the type of the argument
        assert (is ProxyFor<out UnitMember> member);
        if (old.proxied.size == newOwner.proxied.size,
            old.proxied.size == member.proxied.size) {
            Queue<UnitMember>&Iterable<UnitMember> members = LinkedList<UnitMember>();
            Queue<IUnit>&Iterable<IUnit> newList = LinkedList<IUnit>();
            for ([item, innerOld, innerNew] in
                    zip(member.proxied, zipPairs(old.proxied, newOwner.proxied))) {
                innerOld.removeMember(item);
                members.offer(item);
                newList.offer(innerNew);
            }
            newNode.add(node);
            fireTreeNodesInserted(this,
                createJavaObjectArray<Object>({playerNode,
                    getNode(playerNode, newOwner.kind), newNode}),
                createJavaIntArray({newNode.getIndex(node)}),
                createJavaObjectArray<Object>({node}));
            for ([unit, innerMember] in zipPairs(newList, members)) {
                unit.addMember(innerMember);
            }
        } else {
            old.removeMember(member);
            newNode.add(node);
            fireTreeNodesInserted(this,
                createJavaObjectArray<Object>({playerNode,
                    getNode(playerNode, newOwner.kind), newNode}),
                createJavaIntArray({newNode.getIndex(node)}),
                createJavaObjectArray<Object>({node}));
            Iterable<UnitMember> iter = member.proxied;
            if (iter.empty) {
                newOwner.addMember(member);
            } else {
                for (item in iter) {
                    for (unit in newOwner.proxied) {
                        unit.addMember(item.copy(false));
                    }
                }
            }
        }
    }
    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        assert (is TreeNode playerNode = root);
        MutableTreeNode? oldNode = getNode(playerNode, old);
        if (is UnitNode newNode = getNode(playerNode, newOwner),
            exists node = getNode(playerNode, member)) {
            Integer oldIndex;
            if (exists oldNode) {
                oldIndex = oldNode.getIndex(node);
            } else {
                oldIndex = -1;
            }
            fireTreeNodesRemoved(this,
                createJavaObjectArray<Object>({playerNode, getNode(playerNode, old.kind),
                    oldNode}), createJavaIntArray({oldIndex}),
                createJavaObjectArray<Object>({node}));
            if (exists oldNode) {
                oldNode.remove(node);
            }
            if (is ProxyFor<out Anything> member, is ProxyUnit old, is ProxyUnit newOwner) {
                moveProxied(member, old, newOwner, newNode, node);
            } else {
                old.removeMember(member);
                newNode.add(node);
                fireTreeNodesInserted(this,
                    createJavaObjectArray<Object>({playerNode, getNode(playerNode,
                        newOwner.kind), newNode}),
                    createJavaIntArray({newNode.getIndex(node)}),
                    createJavaObjectArray<Object>({node}));
                // TODO: make UniitNode.add() call IUnit.addMember()?
                newOwner.addMember(member);
            }
        }
    }
    shared actual void addUnit(IUnit unit) {
        model.addUnit(unit);
        assert (is PlayerNode temp = root);
        if (exists matchingUnit = model.getUnitByID(temp.userObjectNarrowed, unit.id)) {
            MutableTreeNode node = UnitNode(matchingUnit);
            String kind = unit.kind;
            for (child in temp) {
                if (is KindNode child, kind == child.userObjectNarrowed) {
                    child.add(node);
                    fireTreeNodesInserted(this, getPathToRoot(node),
                        createJavaIntArray({child.childCount - 1}),
                        createJavaObjectArray<Object>({node}));
                    break;
                }
            } else {
                KindNode kindNode = KindNode(kind, matchingUnit);
                temp.add(kindNode);
                fireTreeNodesInserted(this, getPathToRoot(kindNode),
                    createJavaIntArray({temp.childCount - 1}),
                    createJavaObjectArray<Object>({kindNode}));
            }
        }
    }
    shared actual void addNewUnit(IUnit unit) => addUnit(unit);
    shared actual void mapChanged() =>
            setRoot(PlayerNode(model.map.currentPlayer, model));
    shared actual void playerChanged(Player? old, Player newPlayer) =>
            setRoot(PlayerNode(newPlayer, model));
    shared actual Object getModelObject(Object obj) {
        if (is DefaultMutableTreeNode obj) {
            return obj.userObject;
        } else {
            return obj;
        }
    }
    shared actual void addUnitMember(IUnit unit, UnitMember member) {
        assert (is Iterable<TreeNode> temp = root);
        UnitNode? findUnitNode() {
            for (node in temp) {
                if (is UnitNode node, node.userObjectNarrowed == unit) {
                    return node;
                }
            }
            return null;
        }
        UnitNode? unitNode = findUnitNode();
        if (exists unitNode) {
            unit.addMember(member);
            MutableTreeNode newNode = UnitMemberNode(member);
            unitNode.add(newNode);
            fireTreeNodesInserted(this, createJavaObjectArray<Object>({root, unitNode}),
                createJavaIntArray({unitNode.childCount - 1}),
                createJavaObjectArray<Object>({newNode}));
        }
    }
    shared actual void renameItem(HasMutableName item) {
        if (is TreeNode temp = root, exists node = getNode(temp, item)) {
            value path = getPathToRoot(node);
            Integer index = getIndexOfChild(path[path.size - 2], node);
            fireTreeNodesChanged(this,
                createJavaObjectArray<Object>(path.array.exceptLast),
                createJavaIntArray({index}),
                createJavaObjectArray({node}));
        }
    }
    shared actual void moveItem(HasKind item) {
        assert (is PlayerNode temp = root);
        if (is UnitMember item) {
            if (is TreeNode node = getNode(temp, item)) {
                value path = getPathToRoot(node);
                Integer index = getIndexOfChild(path.array.last, node);
                // FIXME: We don't actually move unit members (nodes) here!
                fireTreeNodesChanged(this, path, createJavaIntArray({ index }),
                    createJavaObjectArray({ node }));
            }
        } else if (is IUnit item) {
            if (is TreeNode node = getNode(temp, item)) {
                value pathOne = getPathToRoot(node);
                Integer indexOne = getIndexOfChild(pathOne.array.exceptLast.last, node);
                value nodeTwo = temp.find((child) {
                    if (is KindNode child, item.kind == child.userObjectNarrowed) {
                        return true;
                    } else {
                        return false;
                    }
                });
                assert (is MutableTreeNode end = pathOne.array.last);
                end.removeFromParent();
                ObjectArray<Object> pathSubset;
                if (is MutableTreeNode lastParent = pathOne.array.exceptLast.last,
                    lastParent.childCount == 0) {
                    assert (exists lastParentParent = pathOne.array.exceptLast.exceptLast.last);
                    Integer parentIndex = lastParentParent.getIndex(lastParent);
                    pathSubset = createJavaObjectArray<Object>(pathOne.array.exceptLast);
                    lastParent.removeFromParent();
                    fireTreeNodesRemoved(this, pathSubset,
                        createJavaIntArray({parentIndex}),
                        createJavaObjectArray({lastParent}));
                } else {
                    pathSubset = createJavaObjectArray<Object>(pathOne.array.exceptLast);
                    fireTreeNodesRemoved(this, pathSubset, createJavaIntArray({indexOne}),
                        createJavaObjectArray({node}));
                }
                if (is MutableTreeNode nodeTwo) {
                    Integer indexTwo = nodeTwo.childCount;
                    nodeTwo.insert(node, indexTwo);
                    fireTreeNodesInserted(this,
                        createJavaObjectArray<Object>({root, nodeTwo}),
                        createJavaIntArray({indexTwo}),
                        createJavaObjectArray({node}));
                } else {
                    MutableTreeNode kindNode = KindNode(item.kind, item);
                    temp.add(kindNode);
                    fireTreeNodesInserted(this, createJavaObjectArray<TreeNode>({temp}),
                        createJavaIntArray({getIndexOfChild(temp, kindNode)}),
                        createJavaObjectArray<Object>({kindNode}));
                }
            }
        }
    }
    shared actual void dismissUnitMember(UnitMember member) {
        if (is TreeNode temp = root, exists node = getNode(temp, member)) {
            assert (is UnitNode parentNode = node.parent);
            value path = getPathToRoot(node);
            Integer index = getIndexOfChild(path.array.last, node);
            parentNode.remove(node);
            fireTreeNodesRemoved(this, path, createJavaIntArray({index}),
                createJavaObjectArray<Object>({node}));
            dismissedMembers.add(member);
            // TODO: Extend UnitNode.remove to remove from the user object?
            parentNode.userObjectNarrowed.removeMember(member);
        }
    }
    shared actual {UnitMember*} dismissed => dismissedMembers;
}

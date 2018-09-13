import java.lang {
    ObjectArray,
	IntArray
}
import strategicprimer.model.map.fixtures {
    UnitMember
}
import javax.swing.tree {
    DefaultTreeModel,
    MutableTreeNode,
    DefaultMutableTreeNode,
    TreeNode,
    TreePath
}
import ceylon.collection {
    LinkedList,
    ArrayList,
    Queue,
    MutableList
}
import strategicprimer.model.map.fixtures.mobile {
    ProxyFor,
    IUnit,
    ProxyUnit
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
	matchingValue,
	as,
    IteratorWrapper
}
import lovelace.util.jvm {
	EnumerationWrapper
}
"An alternative implementation of the worker tree model."
shared class WorkerTreeModelAlt extends DefaultTreeModel satisfies IWorkerTreeModel {
    shared static class WorkerTreeNode<T>(T userObj, Boolean permitsChildren = true)
            extends DefaultMutableTreeNode(userObj, permitsChildren)
            satisfies {TreeNode*} given T satisfies Object {
        shared actual Iterator<TreeNode> iterator() =>
                EnumerationWrapper<TreeNode>(children());
        // Can't refine userObject to narrow its type because that would narrow the type
        // of the setter as well, which Ceylon's type system doesn't allow.
        shared T userObjectNarrowed {
            assert (is T narrowed = userObject);
            return narrowed;
        }
        shared actual void setUserObject(Object obj) {
            "PlayerNode can only contain Player"
            assert (is T obj);
            (super of DefaultMutableTreeNode).userObject = obj;
        }
        shared actual String string => (super of DefaultMutableTreeNode).string;
        shared actual default void add(MutableTreeNode child) {
            if (is WorkerTreeNode<out Anything> child) {
                super.add(child);
            } else {
                log.info("Asked to add a non-WorkerTreeNode to a WorkerTreeNode");
            }
        }
    }
    static class UnitMemberNode(UnitMember member)
            extends WorkerTreeNode<UnitMember>(member, false) { }
    shared static class UnitNode(IUnit unit) extends WorkerTreeNode<IUnit>(unit) {
        for (index->member in unit.indexed) {
            insert(UnitMemberNode(member), index);
        }
        shared actual void add(MutableTreeNode child) {
            if (is UnitMemberNode child) {
                unit.addMember(child.userObjectNarrowed);
            } else {
                log.info("Added a non-UnitMemberNode to a UnitNode");
            }
            super.add(child);
        }
        shared actual void remove(MutableTreeNode child) {
            if (is UnitMemberNode child) {
                unit.removeMember(child.userObjectNarrowed);
            } else {
                log.warn("Asked to remove non-UnitMember child from UnitNode");
            }
            super.remove(child);
        }
    }
    shared static class KindNode(String kind, IUnit* units)
            extends WorkerTreeNode<String>(kind) {
        for (index->unit in units.indexed) {
            insert(UnitNode(unit), index);
        }
    }
    // TODO: We want to add a 'Fortress' level to the tree
    static class PlayerNode(Player player, IWorkerModel model)
            extends WorkerTreeNode<Player>(player) {
        for (index->kind in model.getUnitKinds(player).indexed) {
            insert(KindNode(kind, *model.getUnits(player, kind)), index);
        }
        if (childCount == 0) {
            log.warn("No unit kinds in player node for player ``player``");
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
    shared new (IWorkerModel driverModel)
            extends DefaultTreeModel(PlayerNode(driverModel.currentPlayer, driverModel),
                true) {
        model = driverModel;
    }
    void moveProxied(UnitMember&ProxyFor<out UnitMember> member, ProxyUnit old,
            ProxyUnit newOwner, UnitNode newNode, MutableTreeNode node) {
        assert (is PlayerNode playerNode = root);
        if (old.proxied.size == newOwner.proxied.size,
	            old.proxied.size == member.proxied.size) {
            Queue<UnitMember>&{UnitMember*} members = LinkedList<UnitMember>();
            Queue<IUnit>&{IUnit*} newList = LinkedList<IUnit>();
            for ([item, innerOld, innerNew] in
                    zip(member.proxied, zipPairs(old.proxied, newOwner.proxied))) {
                innerOld.removeMember(item);
                members.offer(item);
                newList.offer(innerNew);
            }
            newNode.insert(node, newNode.childCount);
            fireTreeNodesInserted(this,
                ObjectArray<Object>.with([playerNode,
                    getNode(playerNode, newOwner.kind), newNode]),
                IntArray.with(Singleton(newNode.getIndex(node))),
                ObjectArray<Object>.with(Singleton(node)));
            for ([unit, innerMember] in zipPairs(newList, members)) {
                unit.addMember(innerMember);
            }
        } else {
            old.removeMember(member);
            newNode.add(node);
            fireTreeNodesInserted(this,
                ObjectArray<Object>.with([playerNode,
                    getNode(playerNode, newOwner.kind), newNode]),
                IntArray.with(Singleton(newNode.getIndex(node))),
                ObjectArray<Object>.with(Singleton(node)));
            {UnitMember*} iter = member.proxied;
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
    void markModified() {
        for (map->[file, modified] in model.allMaps) {
            if (!modified) {
                model.setModifiedFlag(map, true);
            }
        }
    }
    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        assert (is TreeNode playerNode = root);
        MutableTreeNode? oldNode = getNode(playerNode, old);
        if (is UnitNode newNode = getNode(playerNode, newOwner),
	            exists node = getNode(playerNode, member)) {
            Integer oldIndex = oldNode?.getIndex(node) else -1;
            fireTreeNodesRemoved(this,
                ObjectArray<Object>.with([playerNode, getNode(playerNode, old.kind),
                    oldNode]), IntArray.with(Singleton(oldIndex)),
                ObjectArray<Object>.with(Singleton(node)));
            if (exists oldNode) {
                oldNode.remove(node);
            }
            if (is ProxyFor<out UnitMember> member, is ProxyUnit old,
                    is ProxyUnit newOwner) {
                moveProxied(member, old, newOwner, newNode, node);
            } else {
                old.removeMember(member);
                newNode.add(node);
                fireTreeNodesInserted(this,
                    ObjectArray<Object>.with([playerNode, getNode(playerNode,
                        newOwner.kind), newNode]),
                    IntArray.with(Singleton(newNode.getIndex(node))),
                    ObjectArray<Object>.with(Singleton(node)));
            }
            markModified();
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
                        IntArray.with(Singleton(child.childCount - 1)),
                        ObjectArray<Object>.with(Singleton(node)));
                    break;
                }
            } else {
                KindNode kindNode = KindNode(kind, matchingUnit);
                temp.add(kindNode);
                fireTreeNodesInserted(this, getPathToRoot(kindNode),
                    IntArray.with(Singleton(temp.childCount - 1)),
                    ObjectArray<Object>.with(Singleton(kindNode)));
            }
            markModified();
        }
    }
    shared actual void addNewUnit(IUnit unit) => addUnit(unit);
    shared actual void mapChanged() =>
            setRoot(PlayerNode(model.currentPlayer, model));
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
        if (exists kindNode = as<PlayerNode>(root)?.narrow<KindNode>()
                    ?.find(matchingValue(unit.kind, KindNode.userObjectNarrowed)),
                exists unitNode = kindNode.narrow<UnitNode>().find(matchingValue(unit,
                    UnitNode.userObjectNarrowed))) {
            unit.addMember(member);
            MutableTreeNode newNode = UnitMemberNode(member);
            unitNode.add(newNode);
            fireTreeNodesInserted(this, ObjectArray<Object>.with([root, unitNode]),
                IntArray.with(Singleton(unitNode.childCount - 1)),
                ObjectArray<Object>.with(Singleton(newNode)));
            markModified();
        } else {
            log.error("Asked to add a unit member but couldn't find corresponding unit node");
        }
    }
    shared actual void renameItem(HasMutableName item) {
        if (is TreeNode temp = root, exists node = getNode(temp, item)) {
            value path = getPathToRoot(node);
            Integer index = getIndexOfChild(path[path.size - 2], node);
            fireTreeNodesChanged(this,
                ObjectArray<Object>.with(path.array.exceptLast),
                IntArray.with(Singleton(index)),
                ObjectArray.with(Singleton(node)));
            markModified();
        }
    }
    shared actual void moveItem(HasKind item, String priorKind) {
        assert (is PlayerNode temp = root);
        if (is UnitMember item) {
            if (is TreeNode node = getNode(temp, item)) {
                value path = getPathToRoot(node);
                Integer index = getIndexOfChild(path.array.last, node);
                // fireNodesChanged() is *correct*: a change in a unit member's kind
                // does *not* mean any node should move.
                fireTreeNodesChanged(this, path, IntArray.with(Singleton(index)),
                    ObjectArray.with(Singleton(node)));
                markModified();
            }
        } else if (is IUnit item) {
            if (is TreeNode node = getNode(temp, item)) {
                value pathOne = getPathToRoot(node);
                Integer indexOne = getIndexOfChild(pathOne.array.exceptLast.last, node);
                value nodeTwo = temp.narrow<KindNode>()
                        .find(matchingValue(item.kind, KindNode.userObjectNarrowed));
                assert (is MutableTreeNode end = pathOne.array.last);
                end.removeFromParent();
                ObjectArray<Object> pathSubset;
                if (is MutableTreeNode lastParent = pathOne.array.exceptLast.last,
                    lastParent.childCount == 0) {
                    assert (exists lastParentParent =
                            pathOne.array.exceptLast.exceptLast.last);
                    Integer parentIndex = lastParentParent.getIndex(lastParent);
                    pathSubset = ObjectArray<Object>
                        .with(pathOne.array.exceptLast.exceptLast);
                    lastParent.removeFromParent();
                    fireTreeNodesRemoved(this, pathSubset,
                        IntArray.with(Singleton(parentIndex)),
                        ObjectArray.with(Singleton(lastParent)));
                } else {
                    pathSubset = ObjectArray<Object>.with(pathOne.array.exceptLast);
                    fireTreeNodesRemoved(this, pathSubset,
                        IntArray.with(Singleton(indexOne)),
                        ObjectArray.with(Singleton(node)));
                }
                if (is MutableTreeNode nodeTwo) {
                    Integer indexTwo = nodeTwo.childCount;
                    nodeTwo.insert(node, indexTwo);
                    fireTreeNodesInserted(this,
                        ObjectArray<Object>.with([root, nodeTwo]),
                        IntArray.with(Singleton(indexTwo)),
                        ObjectArray.with(Singleton(node)));
                } else {
                    MutableTreeNode kindNode = KindNode(item.kind, item);
                    temp.add(kindNode);
                    fireTreeNodesInserted(this,
                        ObjectArray<TreeNode>.with(Singleton(temp)),
                        IntArray.with(Singleton(getIndexOfChild(temp, kindNode))),
                        ObjectArray<Object>.with(Singleton(kindNode)));
                }
                markModified();
            }
        }
    }
    shared actual void dismissUnitMember(UnitMember member) {
        if (is TreeNode temp = root, exists node = getNode(temp, member)) {
            assert (is UnitNode parentNode = node.parent);
            // Note that getPathToRoot() returns a path that does *not* include the node
            // itself
            value path = getPathToRoot(node);
            Integer index = getIndexOfChild(path.array.last, node);
            parentNode.remove(node);
            fireTreeNodesRemoved(this, path, IntArray.with(Singleton(index)),
                ObjectArray<Object>.with(Singleton(node)));
            dismissedMembers.add(member);
            markModified();
        }
    }
    shared actual {UnitMember*} dismissed => dismissedMembers;
    shared actual void addSibling(UnitMember base, UnitMember sibling) {
        if (is TreeNode temp = root, exists node = getNode(temp, base)) {
            assert (is UnitNode parentNode = node.parent);
            UnitMemberNode childNode = UnitMemberNode(sibling);
            parentNode.add(childNode);
            Integer index = getIndexOfChild(parentNode, childNode);
            fireTreeNodesInserted(this, getPathToRoot(parentNode),
                IntArray.with(Singleton(index)),
                ObjectArray<Object>.with(Singleton(childNode)));
            markModified();
        }
    }
    """Get the path to the "next" unit whose orders for the given turn either contain
       "TODO", contain "FIXME", or are empty. Returns null if no unit matches those
       criteria."""
    shared actual TreePath? nextProblem(TreePath? starting, Integer turn) {
        assert (is PlayerNode rootNode = root);
        value enumeration = rootNode.preorderEnumeration();
        value wrapped = IteratorWrapper(EnumerationWrapper<WorkerTreeNode<out Anything>>(
            enumeration)).sequence();
        {UnitNode*} sequence;
        if (exists starting) {
            assert (is WorkerTreeNode<out Anything> last = starting.lastPathComponent);
            sequence = wrapped.repeat(2).sequence().trimLeading(not(last.equals))
                .rest.narrow<UnitNode>();
        } else {
            sequence = wrapped.narrow<UnitNode>().sequence();
        }
        for (node in sequence) {
            String orders = node.userObjectNarrowed.getOrders(turn).lowercased;
            if (orders.empty || orders.contains("todo") || orders.contains("fixme")) {
                return TreePath(node.path);
            }
        }
        return null;
    }
}

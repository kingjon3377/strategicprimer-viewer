import java.lang {
    ObjectArray,
    IntArray
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import javax.swing.tree {
    DefaultTreeModel,
    MutableTreeNode,
    DefaultMutableTreeNode,
    TreeNode,
    TreePath
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
import lovelace.util.common {
    matchingValue,
    as,
    IteratorWrapper,
    todo
}
import lovelace.util.jvm {
    EnumerationWrapper
}
import strategicprimer.drivers.common {
    IWorkerModel
}

"An alternative implementation of the worker tree model."
shared class WorkerTreeModelAlt extends DefaultTreeModel satisfies IWorkerTreeModel {
    "A base class for all nodes in the tree in this implementation of the tree model."
    shared static class WorkerTreeNode<NodeObject>(NodeObject userObj,
                Boolean permitsChildren = true)
            extends DefaultMutableTreeNode(userObj, permitsChildren)
            satisfies {TreeNode*} given NodeObject satisfies Object {
        "An iterator over the node's child-nodes."
        shared actual Iterator<TreeNode> iterator() =>
                EnumerationWrapper<TreeNode>(children());

        "The object the node represents, with its type statically guaranteed to
         be [[NodeObject]]. This returns the same *value* as the [[userObject]]
         attribute inherited from [[DefaultMutableTreeNode]], but we can't
         narrow its type without narrowing the type in its setter as well, and
         the Ceylon type system won't allow that."
        shared NodeObject userObjectNarrowed {
            assert (is NodeObject narrowed = userObject);
            return narrowed;
        }

        "Change what object the node represents. While the compiler will not
         complain if you pass an object in that is not a [[NodeObject]], that
         will cause a runtime assertion failure."
        throws(`class AssertionError`, "if [[obj]] is not a [[NodeObject]].")
        shared actual void setUserObject(Object obj) {
            "PlayerNode can only contain Player"
            assert (is NodeObject obj);
            (super of DefaultMutableTreeNode).userObject = obj;
        }

        "Delegate to the [[DefaultMutableTreeNode]] default [[string]] implementation."
        shared actual String string => (super of DefaultMutableTreeNode).string;

        "Add a child. If it is not a [[WorkerTreeNode]] of some sort, we log
         this (at the info level) but otherwise ignore the request instead of
         adding it."
        shared actual default void add(MutableTreeNode child) {
            if (is WorkerTreeNode<out Anything> child) {
                super.add(child);
            } else {
                log.info("Asked to add a non-WorkerTreeNode to a WorkerTreeNode");
            }
        }
    }

    "A class for tree-nodes representing members of units."
    static class UnitMemberNode(UnitMember member)
            extends WorkerTreeNode<UnitMember>(member, false) { }

    "A class for tree-nodes representing units."
    shared static class UnitNode(IUnit unit) extends WorkerTreeNode<IUnit>(unit) {
        for (index->member in unit.indexed) {
            insert(UnitMemberNode(member), index);
        }

        "Add a child. If it is a [[UnitMemberNode]], also add the unit-member
         it represents to the unit this node represents."
        shared actual void add(MutableTreeNode child) {
            if (is UnitMemberNode child) {
                unit.addMember(child.userObjectNarrowed);
            } else {
                log.info("Added a non-UnitMemberNode to a UnitNode");
            }
            super.add(child);
        }

        "Remove a child. If it is a [[UnitMemberNode]], also remove the
         unit-member it represents from the unit this node represents."
        shared actual void remove(MutableTreeNode child) {
            if (is UnitMemberNode child) {
                unit.removeMember(child.userObjectNarrowed);
            } else {
                log.warn("Asked to remove non-UnitMember child from UnitNode");
            }
            super.remove(child);
        }

        shared void refreshChildren() {
            removeAllChildren();
            for (member in unit) {
                super.add(UnitMemberNode(member));
            }
        }
    }

    """A class for tree-nodes representing unit kinds, grouping units sharing a
       "kind" (in practice an administrative classification) in the tree."""
    shared static class KindNode(String kind, IUnit* units)
            extends WorkerTreeNode<String>(kind) {
        for (index->unit in units.indexed) {
            insert(UnitNode(unit), index);
        }
    }

    "A class for the tree node representing the player, which serves as the
     root of the tree (and is hidden from the user, so it looks like there are
     multiple roots, each of which is a [[KindNode]])."
    todo("""We want to add a "Fortress" level to the tree, ideally when and
            only when some but not all units are in a fortress, or when units
            are in (divided between) multiple fortresses.""")
    static class PlayerNode(Player player, IWorkerModel model)
            extends WorkerTreeNode<Player>(player) {
        for (index->kind in model.getUnitKinds(player).indexed) {
            insert(KindNode(kind, *model.getUnits(player, kind)), index);
        }
        if (childCount == 0) {
            log.warn("No unit kinds in player node for player ``player``");
        }
    }

    "A helper method to test whether a node has the given object as the object
     it represents."
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
    shared new (IWorkerModel driverModel)
            extends DefaultTreeModel(PlayerNode(driverModel.currentPlayer, driverModel),
                true) {
        model = driverModel;
    }

    "Move a unit-member from one unit to another, notifying listeners of
     changes to the tree."
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
	    model.moveMember(member, old, newOwner);
	    newNode.insert(node, newNode.childCount);
            fireTreeNodesInserted(this,
                ObjectArray<Object>.with([playerNode, getNode(playerNode,
                    newOwner.kind), newNode]),
                IntArray.with(Singleton(newNode.getIndex(node))),
                ObjectArray<Object>.with(Singleton(node)));
        }
    }

    "Add a unit to the driver-model (i.e. the map) and to the tree, notifying
     listeners of the change."
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
        }
    }

    "Add a unit to the driver-model and the map. Delegates to [[addUnit]]; the two
     have the same functionality, but are required by different interfaces."
    shared actual void addNewUnit(IUnit unit) => addUnit(unit);

    "When we are notified that the map has changed, regenerate the tree by
     replacing the root node with a newly initialized [[PlayerNode]]."
    shared actual void mapChanged() =>
            setRoot(PlayerNode(model.currentPlayer, model));

    "When we are notified that the current player has changed, regenerate the
     tree for that player."
    shared actual void playerChanged(Player? old, Player newPlayer) =>
            setRoot(PlayerNode(newPlayer, model));

    "For any node, the model object is its [[user
     object|DefaultMutableTreeNode.userObject]]; for anything that is not a
     node descending from that superclass, its model object is (presumed to be)
     itself."
    shared actual Object getModelObject(Object obj) {
        if (is DefaultMutableTreeNode obj) {
            return obj.userObject;
        } else {
            return obj;
        }
    }

    "Add a member to a unit, and to the corresponding node in the tree."
    shared actual void addUnitMember(IUnit unit, UnitMember member) {
        if (exists kindNode = as<PlayerNode>(root)?.narrow<KindNode>()
                    ?.find(matchingValue(unit.kind, KindNode.userObjectNarrowed)),
                exists unitNode = kindNode.narrow<UnitNode>().find(matchingValue(unit,
                    UnitNode.userObjectNarrowed))) {
            model.addUnitMember(unit, member);
            MutableTreeNode newNode = UnitMemberNode(member);
            unitNode.add(newNode); // FIXME: Check this doesn't also add the unit member again
            fireTreeNodesInserted(this, ObjectArray<Object>.with([root, unitNode]),
                IntArray.with(Singleton(unitNode.childCount - 1)),
                ObjectArray<Object>.with(Singleton(newNode)));
        } else {
            log.error(
                "Asked to add a unit member but couldn't find corresponding unit node");
        }
    }

    "Update the tree in response to something changing its name."
    shared actual void renameItem(HasMutableName item, String newName) {
        if (is TreeNode temp = root, exists node = getNode(temp, item),
                model.renameItem(item, newName)) {
            value path = getPathToRoot(node);
            Integer index = getIndexOfChild(path[path.size - 2], node);
            fireTreeNodesChanged(this,
                ObjectArray<Object>.with(path.array.exceptLast),
                IntArray.with(Singleton(index)),
                ObjectArray.with(Singleton(node)));
        }
    }

    """Update the tree in response to something's "kind" changing. If a
       unit-member, just tell listeners to update its appearance; if a unit,
       move it from its old parent node to the one for its new "kind," creating
       a new one if necessary, and removing the old one if empty."""
    todo("Do we actually create a new one if necessary?",
         "Do we actually remove the old one if now empty?")
    shared actual void changeKind(HasKind item, String newKind) {
        assert (is PlayerNode temp = root);
        if (is UnitMember item) {
            if (is TreeNode node = getNode(temp, item)) {
                value path = getPathToRoot(node);
                Integer index = getIndexOfChild(path.array.last, node);
                if (model.changeKind(item, newKind)) {
                    // fireNodesChanged() is *correct*: a change in a unit member's kind
                    // does *not* mean any node should move.
                    fireTreeNodesChanged(this, path, IntArray.with(Singleton(index)),
                        ObjectArray.with(Singleton(node)));
                }
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
                model.changeKind(item, newKind);
                if (is MutableTreeNode nodeTwo) {
                    Integer indexTwo = nodeTwo.childCount;
                    nodeTwo.insert(node, indexTwo);
                    fireTreeNodesInserted(this,
                        ObjectArray<Object>.with([root, nodeTwo]),
                        IntArray.with(Singleton(indexTwo)),
                        ObjectArray.with(Singleton(node)));
                } else {
                    MutableTreeNode kindNode = KindNode(newKind, item);
                    temp.add(kindNode);
                    fireTreeNodesInserted(this,
                        ObjectArray<TreeNode>.with(Singleton(temp)),
                        IntArray.with(Singleton(getIndexOfChild(temp, kindNode))),
                        ObjectArray<Object>.with(Singleton(kindNode)));
                }
            }
        }
    }

    "Remove a unit-member from its parent unit."
    shared actual void dismissUnitMember(UnitMember member) {
        if (is TreeNode temp = root, exists node = getNode(temp, member)) {
            assert (is UnitNode parentNode = node.parent);
            // Note that getPathToRoot() returns a path that does *not* include the node
            // itself
            value path = getPathToRoot(node);
            Integer index = getIndexOfChild(path.array.last, node);
	    model.dismissUnitMember(member);
            parentNode.remove(node);
            fireTreeNodesRemoved(this, path, IntArray.with(Singleton(index)),
                ObjectArray<Object>.with(Singleton(node)));
        }
    }

    "Add [[a unit-member|sibling]] to the unit containing [[the given
     unit-member]]. This is primarily used when the user asks to split an
     animal population."
    shared actual void addSibling(UnitMember base, UnitMember sibling) {
        UnitMemberNode childNode = UnitMemberNode(sibling);
        // FIXME: Log and/or give UI feedback on failure of some of these conditions
        if (is TreeNode temp = root, exists node = getNode(temp, base), is UnitNode parentNode = node.parent,
                model.addSibling(base, sibling),
                exists index->siblingNode = parentNode.userObjectNarrowed.locate(sibling.equals)) {
            parentNode.insert(childNode, index);
            fireTreeNodesInserted(this, getPathToRoot(parentNode),
                IntArray.with(Singleton(index)),
                ObjectArray<Object>.with(Singleton(childNode)));
        }
    }

    """Get the path to the "next" unit whose orders for the given turn either contain
       "TODO", contain "FIXME", contain "XXX", or are empty. Skips units with no members.
       Returns null if no unit matches those criteria."""
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
            if (node.userObjectNarrowed.empty) {
                continue;
            }
            String orders = node.userObjectNarrowed.getOrders(turn).lowercased;
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
                return TreePath(node.path);
            }
        }
        return null;
    }

    """Ignore notification of changes to map filename or "modified" flag."""
    shared actual void mapMetadataChanged() {}

    shared actual {TreeNode*} childrenOf(Object obj) {
        assert (is PlayerNode temp = root);
        if (is WorkerTreeNode<out Object> obj) {
            return obj;
        } else if (exists node = getNode(temp, obj)) {
            return childrenOf(node);
        } else {
            return [];
        }
    }

    shared actual void refreshChildren(IUnit parent) {
        assert (is TreeNode playerNode = root);
        if (is UnitNode parentNode = getNode(playerNode, parent)) {
            parentNode.refreshChildren();
            fireTreeStructureChanged(this, ObjectArray<Object>.with([root, parentNode]), null, null);
        } else {
            process.writeErrorLine("refreshChildren() called on unit not in the tree");
        }
    }

    shared actual void removeUnit(IUnit unit) {
        log.trace("In WorkerTreeModelAlt.removeUnit");
        if (is TreeNode playerNode = root, is KindNode kindNode = getNode(playerNode, unit.kind),
                exists node = getNode(kindNode, unit)) {
            Integer index = getIndexOfChild(kindNode, node);
            log.trace("Unit is ``index``th child of unit-kind");
            if (model.removeUnit(unit)) {
                log.trace("Removed from the map, about to remove from the tree");
                kindNode.remove(node);
                fireTreeNodesRemoved(this,
                    ObjectArray<Object>.with([playerNode, kindNode]), IntArray.with(Singleton(index)),
                    ObjectArray<Object>.with(Singleton(node)));
                log.trace("Finished updating the tree");
            } else {
                log.warn("Failed to remove from the map for some reason");
                // FIXME: Some user feedback---beep, visual beep, etc.
            }
        } else {
            log.error("Tree root isn't a tree node, or tree doesn't contain that unit");
        }
    }
}

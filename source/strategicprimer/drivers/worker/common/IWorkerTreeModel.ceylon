import javax.swing.tree {
    TreeModel,
    TreePath
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import strategicprimer.drivers.common {
    PlayerChangeListener,
    MapChangeListener
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map {
    HasMutableName,
    HasKind
}

"An interface for worker tree-models, adding methods to the [[TreeModel]] interface."
shared interface IWorkerTreeModel
        satisfies TreeModel&NewUnitListener&PlayerChangeListener&MapChangeListener {
    "Move a member between units."
    shared formal void moveMember(
            "The member to move."
            UnitMember member,
            "Its prior owner"
            IUnit old,
            "Its new owner"
            IUnit newOwner);

    "Add a new unit, and also handle adding it to the map (via the driver model)."
    shared formal void addUnit(
            "The unit to add"
            IUnit unit);

    "If the parameter is a node in the tree (and this implementation is one using nodes
     rather than model objects directly), return the model object it represents;
     otherwise, returns the parameter."
    shared formal Object getModelObject(Object obj);

    "Add a new member to a unit."
    shared formal void addUnitMember(
            "The unit that should own the member"
            IUnit unit,
            "The member to add to the unit"
            UnitMember member);

    "Update the tree to reflect the fact that something's name has changed."
    shared formal void renameItem(HasMutableName item);

    "Update the tree to reflect a change in something's kind. If a unit, this means it
     has moved in the tree, since units' kinds are their parent nodes now."
    shared formal void moveItem(HasKind kind, String priorKind);

    "Dismiss a unit member from a unit and from the player's service."
    shared formal void dismissUnitMember(UnitMember member);

    "The unit members that have been dismissed during this session."
    shared formal {UnitMember*} dismissed;

    "Add a unit member to the unit that contains the given member. If the base is not in
     the tree, the model is likely to simply ignore the call, but the behavior is
     undefined."
    shared formal void addSibling(
        "The member that is already in the tree."
        UnitMember base,
        "The member to add as its sibling."
        UnitMember sibling);

    """Get the path to the "next" unit whose orders for the given turn either contain
       "TODO", contain "FIXME", or are empty. Returns null if no unit matches those
       criteria."""
    shared formal TreePath? nextProblem(TreePath? starting, Integer turn);
}

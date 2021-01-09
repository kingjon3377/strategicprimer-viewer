import javax.swing.tree {
    TreeModel,
    TreePath
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

"An interface for worker tree-models, adding methods to the [[TreeModel]] interface."
shared interface IWorkerTreeModel
        satisfies TreeModel&IFixtureEditHelper {
    "If the parameter is a node in the tree (and this implementation is one using nodes
     rather than model objects directly), return the model object it represents;
     otherwise, returns the parameter."
    shared formal Object getModelObject(Object obj);

    """Get the path to the "next" unit whose orders for the given turn either contain
       "TODO", contain "FIXME", contain "XXX", or are empty. Skips units with no members.
       Returns null if no unit matches those criteria."""
    shared formal TreePath? nextProblem(TreePath? starting, Integer turn);

    "If [[arg]] is a node in the tree, return its children, if any; otherwise, return
     the empty sequence."
    shared formal {Object*} childrenOf(Object arg);

    "Refresh the children of the given tree-member, usually because it has been sorted."
    shared formal void refreshChildren(IUnit parent);
}

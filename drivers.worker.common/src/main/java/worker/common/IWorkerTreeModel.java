package worker.common;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import legacy.map.fixtures.mobile.IUnit;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for worker tree-models, adding methods to the {@link TreeModel} interface.
 */
public interface IWorkerTreeModel extends TreeModel, IFixtureEditHelper {
	/**
	 * If the parameter is a node in the tree (and this implementation is
	 * one using nodes rather than model objects directly), return the
	 * model object it represents; otherwise, returns the parameter.
	 */
	Object getModelObject(Object obj);

	/**
	 * Get the path to the "next" unit whose orders for the given turn
	 * either contain "TODO", contain "FIXME", contain "XXX", or are empty.
	 * Skips units with no members.  Returns null if no unit matches those
	 * criteria.
	 */
	@Nullable
	TreePath nextProblem(@Nullable TreePath starting, int turn);

	/**
	 * If "arg" is a node in the tree, return its children, if any;
	 * otherwise, return an empty collection.
	 */
	Iterable<Object> childrenOf(Object arg);

	/**
	 * Refresh the children of the given tree-member, usually because it has been sorted.
	 */
	void refreshChildren(IUnit parent);
}

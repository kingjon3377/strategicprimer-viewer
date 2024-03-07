package drivers.worker_mgmt;

/**
 * An interface for classes listening for directives to expand or collapse trees.
 */
public interface TreeExpansionOrderListener {
	/**
	 * Expand a tree entirely.
	 */
	void expandAll();

	/**
	 * Collapse a tree entirely.
	 */
	void collapseAll();

	/*
	 * Expand a tree to a certain level.
	 *
	 * @param levels How many levels from the root, counting the root, to expand.
	 */
	void expandSome(int levels);
}

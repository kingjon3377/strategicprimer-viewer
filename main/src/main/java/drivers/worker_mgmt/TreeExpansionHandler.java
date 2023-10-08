package drivers.worker_mgmt;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * A class to handle "expand all," "collapse all," etc.
 */
public class TreeExpansionHandler implements TreeExpansionOrderListener {
    public TreeExpansionHandler(final JTree tree) {
        this.tree = tree;
    }

    private final JTree tree;

    /**
     * Expand all rows of the tree.
     */
    @Override
    public void expandAll() {
        int i = 0;
        while (i < tree.getRowCount()) {
            tree.expandRow(i);
            i++;
        }
    }

    /**
     * Collapse all rows of the tree.
     */
    @Override
    public void collapseAll() {
        int i = tree.getRowCount() - 1;
        while (i >= 0) {
            if (i < tree.getRowCount()) {
                tree.collapseRow(i);
            }
            i--;
        }
    }

    /**
     * Expand some rows of the tree.
     *
     * @param levels How many levels from the root, inclusive, to expand.
     */
    @Override
    public void expandSome(final int levels) {
        int i = 0;
        while (i < tree.getRowCount()) {
            final TreePath path = tree.getPathForRow(i);
            if (path != null && path.getPathCount() <= levels) {
                tree.expandRow(i);
            }
            i++;
        }
    }
}

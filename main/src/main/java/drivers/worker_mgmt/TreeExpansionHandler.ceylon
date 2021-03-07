import javax.swing {
    JTree
}

"An interface for classes listening for directives to expand or collapse trees."
shared interface TreeExpansionOrderListener {
    "Expand a tree entirely."
    shared formal void expandAll();

    "Collapse a tree entirely."
    shared formal void collapseAll();

    "Expand a tree to a certain level."
    shared formal void expandSome(
            "How many levels from the root, counting the root, to expand."
            Integer levels);
}

"""A class to handle "expand all," "collapse all," etc."""
shared class TreeExpansionHandler(JTree tree) satisfies TreeExpansionOrderListener {
    "Expand all rows of the tree."
    shared actual void expandAll() {
        variable Integer i = 0;
        while (i < tree.rowCount) {
            tree.expandRow(i);
            i++;
        }
    }

    "Collapse all rows of the tree."
    shared actual void collapseAll() {
        variable Integer i = tree.rowCount - 1;
        while (i >= 0) {
            if (i < tree.rowCount) {
                tree.collapseRow(i);
            }
            i--;
        }
    }

    "Expand some rows of the tree."
    shared actual void expandSome(
            "How many levels from the root, inclusive, to expand."
            Integer levels) {
        variable Integer i = 0;
        while (i < tree.rowCount) {
            if (exists path = tree.getPathForRow(i), path.pathCount <= levels) {
                tree.expandRow(i);
            }
            i++;
        }
    }
}

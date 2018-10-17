import strategicprimer.report {
    IReportNode
}
import javax.swing.tree {
    MutableTreeNode
}

"""A node is empty if it is an IReportNode and "the empty node"."""
shared Boolean isNonEmptyNode(MutableTreeNode node) {
    if (is IReportNode node) {
        return !node.emptyNode;
    } else {
        return true;
    }
}

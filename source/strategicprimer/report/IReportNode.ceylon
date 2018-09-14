import javax.swing.tree {
    MutableTreeNode
}

import lovelace.util.common {
    todo,
    matchingPredicate
}

import lovelace.util.jvm {
    EnumerationWrapper
}

import strategicprimer.model.map {
    Point
}
import strategicprimer.report.nodes {
    SimpleReportNode,
    isNonEmptyNode
}
"""An interface for "report nodes" that serve as "report intermediate representation":
   [[TreeNodes|javax.swing.tree::TreeNode]] that can be turned into HTML."""
todo("The interface has been ported directly over from Java; with String interpolation,
      only replacing Formatter with Anything(String); reconsider the interface!")
shared interface IReportNode satisfies Comparable<IReportNode>&MutableTreeNode&
        {IReportNode*} {
    "Write the HTML representation to a stream."
    shared formal void produce(Anything(String) stream);
    "How large the HTML representation will be, approximately."
    shared formal Integer htmlSize;
    "The text of this node (itself, ignoring any children), usually the header."
    shared formal variable String text;
    """Whether this is "the empty node," which should always be ignored."""
    shared default Boolean emptyNode => false;
    """Compare to another node."""
    shared actual default Comparison compare(IReportNode node) =>
            text.compare(node.text);
    "Add children iff they have children of their own."
    shared default void addIfNonEmpty(MutableTreeNode* children) =>
            children.filter(matchingPredicate(Integer.positive,
                MutableTreeNode.childCount)).each(appendNode);
    "Add a node as a child."
    todo("Allow null instead of having
          [[strategicprimer.report.nodes::emptyReportNode]]?")
    shared formal void appendNode(MutableTreeNode node);
    "Add a node as our first child."
    shared default void addAsFirst(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            insert(node, 0);
        }
    }
    "The point, if any, in the map that this node (as opposed to any of its children)
     represents anything on."
    shared formal variable Point? localPoint;
    "The point, if any, that this and its children represent something on."
    shared default Point? point {
        if (exists retval = localPoint) {
            return retval;
        } else {
            variable Point? retval = null;
            for (child in this) {
                if (exists temp = retval) {
                    if (exists childPoint = child.point) {
                        if (temp != childPoint) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    retval = child.point;
                }
            }
            return retval;
        }
    }
    "The HTML representation of the subtree based on this node."
    shared default String htmlProduct {
        StringBuilder builder = StringBuilder();
        produce(builder.append);
        return builder.string;
    }
    "An iterator over the node's children."
    shared default actual Iterator<IReportNode> iterator() =>
            EnumerationWrapper<IReportNode>(children());
}
"Create a report node holding just a single string, such as for a placeholder while the
 report generator generates the report."
shared IReportNode&MutableTreeNode simpleReportNode(String string) =>
        SimpleReportNode(string);

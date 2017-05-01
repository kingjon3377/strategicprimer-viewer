import java.util {
    Enumeration
}

import javax.swing.tree {
    MutableTreeNode
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    Point,
    invalidPoint
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
        Iterable<IReportNode> {
    "Write the HTML representation to a stream."
    shared formal void produce(Anything(String) stream);
    "How large the HTML representation will be, approximately."
    shared formal Integer htmlSize;
    "The text of this node (itself, ignoring any children), usually the header."
    shared formal variable String text;
    """Whether this is "the empty node," which should always be ignored."""
    shared default Boolean emptyNode => false;
    """Compare to another node. Note that this is an expensive implementation, producing
       and delegating to the HTML representation."""
    todo("Implement more efficiently")
    shared actual default Comparison compare(IReportNode node) =>
            htmlProduct.compare(node.htmlProduct);
    "Add children iff they have children of their own."
    shared default void addIfNonEmpty(MutableTreeNode* children) {
        for (child in children) {
            if (child.childCount != 0) {
                appendNode(child);
            }
        }
    }
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
    todo("Return null rather than [[strategicprimer.model.map::invalidPoint]]?")
    shared default Point point {
        if (exists retval = localPoint) {
            return retval;
        } else {
            variable Point? retval = null;
            for (child in this) {
                if (exists temp = retval) {
                    if (temp != child.point) {
                        retval = invalidPoint;
                    }
                } else {
                    retval = child.point;
                }
            }
            if (exists temp = retval) {
                return temp;
            } else {
                return invalidPoint;
            }
        }
    }
    "The HTML representation of the subtree based on this node."
    shared default String htmlProduct {
        StringBuilder builder = StringBuilder();
        produce(builder.append);
        return builder.string;
    }
    "An iterator over the node's children."
    shared default actual Iterator<IReportNode> iterator() {
        Enumeration<out Object> wrapped = children();
        object retval satisfies Iterator<IReportNode> {
            shared actual IReportNode|Finished next() {
                if (wrapped.hasMoreElements()) {
                    assert (is IReportNode node = wrapped.nextElement());
                    return node;
                } else {
                    return finished;
                }
            }
        }
        return retval;
    }
}
"Create a report node holding just a single string, such as for a placeholder while the
 report generator generates the report."
shared IReportNode&MutableTreeNode simpleReportNode(String string) =>
        SimpleReportNode(string);
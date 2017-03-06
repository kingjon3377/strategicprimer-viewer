import javax.swing.tree {
    MutableTreeNode,
    DefaultMutableTreeNode
}
import model.map {
    Point,
    PointFactory
}
import lovelace.util.common {
    todo
}
import java.util {
    Enumeration, JCollections=Collections,
    Vector
}
import ceylon.interop.java {
    JavaComparator
}
"""A node is empty if it is an IReportNode and "the empty node"."""
Boolean isNonEmptyNode(MutableTreeNode node) {
    if (is IReportNode node) {
        return !node.emptyNode;
    } else {
        return true;
    }
}
"""An interface for "report nodes" that serve as "report intermediate representation":
   [[TreeNode]]s that can be turned into HTML."""
todo("The interface has been ported directly over from Java; with String interpolation,
      only replacing Formatter with Anything(String); reconsider the interface!")
shared interface IReportNode satisfies Comparable<IReportNode>&MutableTreeNode&Iterable<IReportNode> {
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
    todo("Allow null instead of having [[emptyReportNode]]?")
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
    todo("Return null rather than [[PointFactory.invalidPoint]]?")
    shared default Point point {
        if (exists retval = localPoint) {
            return retval;
        } else {
            variable Point? retval = null;
            for (child in this) {
                if (exists temp = retval) {
                    if (temp != child.point) {
                        retval = PointFactory.invalidPoint;
                    }
                } else {
                    retval = child.point;
                }
            }
            if (exists temp = retval) {
                return temp;
            } else {
                return PointFactory.invalidPoint;
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
"A node for a section consisting only of a list. This is a common case, which would
 otherwise be represented by a section node containing only a list."
class SectionListReportNode(Integer level, variable String header,
        shared actual variable Point? localPoint = null)
        extends DefaultMutableTreeNode(header) satisfies IReportNode {
    Integer minimumBoilerPlate = "<h1></h1><p></p><ul></ul>".size + 4;
    Integer perChildBoilerPlate = "<li></li>".size + 1;
    shared actual Integer htmlSize =>
            minimumBoilerPlate + header.size + Integer.sum(map(
                        (node) => node.htmlSize + perChildBoilerPlate));
    shared actual default Boolean equals(Object that) {
        if (is SectionListReportNode that, that.level == level, that.header == header,
            that.children() ==children()) {
            if (exists ours = localPoint) {
                if (exists theirs = that.localPoint) {
                    return ours == theirs;
                } else {
                    return false;
                }
            } else if (that.localPoint exists) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    shared actual default Integer hash => level + text.hash /*.or(children().hash)*/;
    shared actual default void appendNode(MutableTreeNode newChild) {
        if (isNonEmptyNode(newChild)) {
            add(newChild);
        }
    }
    shared actual void add(MutableTreeNode node) => appendNode(node);
    shared actual String text => header;
    assign text {
        header = text;
        super.userObject = text;
    }
    "We use the header text as the String representation."
    todo("Should we use the children here as well?")
    shared actual String string => text;
    shared actual void produce(Anything(String) stream) {
        stream("<h``level``>``header``</h``level``>
                ");
        if (childCount != 0) {
            stream("""<ul>
                      """);
            for (child in this) {
                stream("<li>");
                child.produce(stream);
                stream("""</li>
                          """);
            }
            stream("""</ul>
                      """);
        }
    }
    shared actual void setUserObject(Object obj) => super.userObject = obj;
}
"A [[SectionListReportNode]] that sorts itself after every addition."
class SortedSectionListReportNode(Integer level, String text)
        extends SectionListReportNode(level, text) {
    shared actual void appendNode(MutableTreeNode newChild) {
        super.appendNode(newChild);
        assert (is Vector<IReportNode> temp = children);
        JCollections.sort(temp,
            JavaComparator((IReportNode x, IReportNode y) => x <=> y));
    }
}
"A simple node representing plain text. Any children are ignored!"
shared class SimpleReportNode(variable String nodeText, shared actual variable Point? localPoint = null)
        extends DefaultMutableTreeNode(nodeText) satisfies IReportNode {
    shared actual String text => nodeText;
    assign text {
        nodeText = text;
        super.userObject = text;
    }
    shared actual default void produce(Anything(String) stream) => stream(text);
    shared actual default Integer htmlSize => text.size;
    shared actual default Boolean equals(Object that) {
        if (is SimpleReportNode that, text == that.text) {
            if (exists ours = localPoint) {
                if (exists theirs = that.localPoint) {
                    return ours == theirs;
                } else {
                    return false;
                }
            } else if (that.localPoint exists) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    shared actual Integer hash => text.hash;
    shared actual default void appendNode(MutableTreeNode node) {}
    shared actual default void add(MutableTreeNode node) {}
    shared actual default void addAsFirst(MutableTreeNode node) {}
    shared actual String string => text;
    shared actual void setUserObject(Object obj) => super.userObject = obj;
}
"A node for cases where a [[SimpleReportNode]] is not enough, because we *can* have
 children, but there isn't a *header*, and children shouldn't be wrapped in a list."
class ComplexReportNode(String textArg = "", Point? pointArg = null) extends SimpleReportNode(textArg, pointArg) {
    shared actual default void produce(Anything(String) stream) {
        stream(text);
        for (node in this) {
            node.produce(stream);
        }
    }
    shared actual default Integer htmlSize =>
            text.size + Integer.sum(map(IReportNode.size));
    shared actual Boolean equals(Object that) {
        if (is ComplexReportNode that, super.equals(that),
                children() == that.children()) {
            return true;
        } else {
            return false;
        }
    }
    shared actual void appendNode(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            super.add(node);
        }
    }
    shared actual void add(MutableTreeNode node) => appendNode(node);
    shared actual void addAsFirst(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            insert(node, 0);
        }
    }
    // TODO: refine `string` to reflect children?
}
"A node representing a section, with a header."
class SectionReportNode(Integer level, variable String header,
            shared actual variable Point? localPoint = null)
        extends DefaultMutableTreeNode(header) satisfies IReportNode {
    shared actual void appendNode(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            add(node);
        }
    }
    shared actual void add(MutableTreeNode node) => appendNode(node);
    shared actual String text => header;
    assign text {
        super.userObject = text;
        header = text;
    }
    shared actual Integer htmlSize =>
            16 + header.size + Integer.sum(map(IReportNode.size));
    shared actual void produce(Anything(String) stream) {
        stream("<h``level``>``header``</h``level``>
                ");
        for (child in this) {
            child.produce(stream);
        }
    }
    shared actual void setUserObject(Object obj) => super.userObject = obj;
    shared actual Boolean equals(Object that) {
        if (is SectionReportNode that, level == that.level, header == that.header,
                children() == that.children()) {
            if (exists ours = localPoint) {
                if (exists theirs = that.localPoint) {
                    return ours == theirs;
                } else {
                    return false;
                }
            } else if (that.localPoint exists) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    shared actual Integer hash => level + header.hash;
    todo("Use level and/or children?")
    shared actual String string => text;
}
"A node representing a list."
class ListReportNode(variable String initialText,
            shared actual variable Point? localPoint = null)
        extends DefaultMutableTreeNode(initialText) satisfies IReportNode {
    Integer boilerPlateLength = "<ul></ul>".size + 3;
    Integer perChildBoilerPlate = "<li></li>".size + 1;
    shared actual void appendNode(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            add(node);
        }
    }
    shared actual void add(MutableTreeNode node) => appendNode(node);
    shared actual String text => initialText;
    assign text {
        super.userObject = text;
        initialText = text;
    }
    shared actual Integer htmlSize =>
            boilerPlateLength + text.size + Integer.sum(
                map((node) => node.htmlSize + perChildBoilerPlate));
    shared actual void produce(Anything(String) stream) {
        stream(text);
        stream("""
                  <ul>
                  """);
        for (node in this) {
            stream("<li>");
            node.produce(stream);
            stream("""</li>
                      """);
        }
        stream("""</ul>
                  """);
    }
    shared actual Boolean equals(Object that) {
        if (is ListReportNode that, that.initialText == initialText,
                children() == that.children()) {
            if (exists ours = localPoint) {
                if (exists theirs = that.localPoint) {
                    return ours == theirs;
                } else {
                    return false;
                }
            } else if (that.localPoint exists) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    shared actual Integer hash => initialText.hash;
    todo("Reflect the children")
    shared actual String string => text;
    shared actual void setUserObject(Object obj) => super.userObject = obj;
}
"The root of a node hierarchy."
class RootReportNode(variable String title)
        extends DefaultMutableTreeNode(title) satisfies IReportNode {
    shared actual variable Point? localPoint = null;
    shared actual void appendNode(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            super.add(node);
        }
    }
    shared actual void add(MutableTreeNode node) => appendNode(node);
    shared actual Integer htmlSize => 72 + title.size + Integer.sum(map(IReportNode.htmlSize));
    shared actual void produce(Anything(String) stream) {
        stream("<html>
                <head><title>```title``</tile></head>
                <body>
                ");
        for (child in this) {
            child.produce(stream);
        }
        stream("""</body>
                  </html>""");
    }
    shared actual void setUserObject(Object obj) => super.userObject = obj;
    shared actual String text => title;
    assign text {
        title = text;
        super.userObject = text;
    }
    shared actual Boolean equals(Object that) {
        if (is RootReportNode that, title == that.title, children() == that.children()) {
            return true;
        } else {
            return false;
        }
    }
    shared actual Integer hash => title.hash;
    todo("Reflect children?")
    shared actual String string => title;
}
todo("Is this missing any methods that could be used to modify it?",
    "Consider dropping this in favor of using [[IReportNode?]] everywhere")
object emptyReportNode extends DefaultMutableTreeNode() satisfies IReportNode {
    // TODO: log if mutator methods called
    shared actual void appendNode(MutableTreeNode node) {}
    shared actual void add(MutableTreeNode node) { }
    shared actual void addAsFirst(MutableTreeNode node) {}
    shared actual Integer htmlSize => 0;
    shared actual Point? localPoint => null;
    assign localPoint { }
    shared actual void produce(Anything(String) stream) {}
    shared actual void setUserObject(Object ignored) {}
    shared actual String text => "";
    assign text { }
    shared actual String string => "";
    shared actual Boolean emptyNode = true;
    object iter satisfies Iterator<IReportNode> {
        shared actual IReportNode|Finished next() => finished;
    }
    shared actual Iterator<IReportNode> iterator() => iter;
}
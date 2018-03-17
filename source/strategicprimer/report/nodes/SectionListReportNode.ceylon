import javax.swing.tree {
    DefaultMutableTreeNode,
    MutableTreeNode
}

import lovelace.util.common {
    todo,
	anythingEqual
}

import strategicprimer.model.map {
    Point
}
import strategicprimer.report {
    IReportNode
}
"A node for a section consisting only of a list. This is a common case, which would
 otherwise be represented by a section node containing only a list."
shared class SectionListReportNode(Integer level, variable String header,
        shared actual variable Point? localPoint = null)
        extends DefaultMutableTreeNode(header) satisfies IReportNode {
    Integer minimumBoilerPlate = "<h1></h1><p></p><ul></ul>".size + 4;
    Integer perChildBoilerPlate = "<li></li>".size + 1;
    shared actual Integer htmlSize =>
            minimumBoilerPlate + header.size + Integer.sum(map(
                        (node) => node.htmlSize + perChildBoilerPlate));
    shared actual default Boolean equals(Object that) {
        if (is SectionListReportNode that, that.level == level, that.header == header,
	            that.children() == children()) {
            return anythingEqual(localPoint, that.localPoint);
        } else {
            return false;
        }
    }
    shared actual default Integer hash => level + text.hash /*.or(children().hash)*/;
    shared actual default void appendNode(MutableTreeNode newChild) {
        if (isNonEmptyNode(newChild)) {
            super.add(newChild);
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

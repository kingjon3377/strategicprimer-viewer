import javax.swing.tree {
    DefaultMutableTreeNode,
    MutableTreeNode
}

import strategicprimer.model.map {
    Point
}
import strategicprimer.report {
    IReportNode
}
import lovelace.util.common {
	anythingEqual
}
"A simple node representing plain text. Any children are ignored!"
shared class SimpleReportNode(variable String nodeText,
        shared actual variable Point? localPoint = null)
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
            return anythingEqual(localPoint, that.localPoint);
        } else {
            return false;
        }
    }
    shared actual Integer hash => text.hash;
    // TODO: We should probably print a warning, or worse, if a child-adding method is called
    shared actual default void appendNode(MutableTreeNode node) {}
    shared actual default void add(MutableTreeNode node) {}
    shared actual default void addAsFirst(MutableTreeNode node) {}
    shared actual default String string => text;
    shared actual void setUserObject(Object obj) => super.userObject = obj;
}

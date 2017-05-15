import javax.swing.tree {
    DefaultMutableTreeNode,
    MutableTreeNode
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    Point
}
import strategicprimer.report {
    IReportNode
}
"A node representing a list."
shared class ListReportNode(variable String initialText,
        shared actual variable Point? localPoint = null)
        extends DefaultMutableTreeNode(initialText) satisfies IReportNode {
    Integer boilerPlateLength = "<ul></ul>".size + 3;
    Integer perChildBoilerPlate = "<li></li>".size + 1;
    shared actual void appendNode(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            super.add(node);
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

import javax.swing.tree {
    DefaultMutableTreeNode,
    MutableTreeNode
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    Point
}
import strategicprimer.report {
    IReportNode
}

"The root of a node hierarchy."
shared class RootReportNode(variable String title)
        extends DefaultMutableTreeNode(title) satisfies IReportNode {
    shared actual variable Point? localPoint = null;

    shared actual void appendNode(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            super.add(node);
        }
    }

    shared actual void add(MutableTreeNode node) => appendNode(node);

    shared actual Integer htmlSize =>
            72 + title.size + Integer.sum(map(IReportNode.htmlSize));

    shared actual void produce(Anything(String) stream) {
        stream("<!DOCTYPE html>
                <html>
                <head><title>``title``</tile></head>
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

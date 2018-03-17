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
"A node representing a section, with a header."
shared class SectionReportNode(Integer level, variable String header,
        shared actual variable Point? localPoint = null)
        extends DefaultMutableTreeNode(header) satisfies IReportNode {
    shared actual void appendNode(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            super.add(node);
        }
    }
    shared actual void add(MutableTreeNode node) => appendNode(node);
    shared actual String text => header;
    assign text {
        super.userObject = text;
        header = text;
    }
    shared actual Integer htmlSize =>
            16 + header.size + Integer.sum(map(IReportNode.htmlSize));
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
            return anythingEqual(localPoint, that.localPoint);
        } else {
            return false;
        }
    }
    shared actual Integer hash => level + header.hash;
    todo("Use level and/or children?")
    shared actual String string => text;
}

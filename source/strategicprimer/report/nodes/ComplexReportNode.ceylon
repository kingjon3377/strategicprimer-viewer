import javax.swing.tree {
    MutableTreeNode
}

import strategicprimer.model.common.map {
    Point
}
import strategicprimer.report {
    IReportNode
}

"A node for cases where a [[SimpleReportNode]] is not enough, because we *can* have
 children, but there isn't a *header*, and children shouldn't be wrapped in a list."
shared class ComplexReportNode(String textArg = "", Point? pointArg = null)
        extends SimpleReportNode(textArg, pointArg) {
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

    shared actual String string {
        StringBuilder builder = StringBuilder();
        builder.append(text);
        for (child in this) {
            builder.appendNewline();
            builder.append("- ");
            builder.append(child.string);
        }
        return builder.string;
    }
}

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
todo("Is this missing any methods that could be used to modify it?",
    "Consider dropping this in favor of using [[IReportNode?]] everywhere")
shared object emptyReportNode extends DefaultMutableTreeNode() satisfies IReportNode {
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
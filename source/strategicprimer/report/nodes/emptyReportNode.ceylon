import javax.swing.tree {
    DefaultMutableTreeNode,
    MutableTreeNode
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.impl.map {
    Point
}
import strategicprimer.report {
    IReportNode
}
import ceylon.logging {
    Logger,
    logger
}
Logger log = logger(`package strategicprimer.report.nodes`);
todo("Is this missing any methods that could be used to modify it?",
    "Consider dropping this in favor of using [[IReportNode?]] everywhere")
shared object emptyReportNode extends DefaultMutableTreeNode() satisfies IReportNode {
    shared actual void appendNode(MutableTreeNode node) =>
            log.warn("emptyReportNode.appendNode called");
    shared actual void add(MutableTreeNode node) =>
            log.warn("emptyReportNode.add called");
    shared actual void addAsFirst(MutableTreeNode node) =>
            log.warn("emptyReportNode.addAsFirst called");
    shared actual Integer htmlSize => 0;
    shared actual Point? localPoint => null;
    assign localPoint {
        log.warn("emptyReportNode localPoint mutator called");
    }
    shared actual void produce(Anything(String) stream) =>
            log.warn("emptyReportNode.produce called");
    shared actual void setUserObject(Object ignored) =>
            log.warn("emptyReportNode.setUserObject called");
    shared actual String text => "";
    assign text {
        log.warn("emptyReportNode text mutator called");
    }
    shared actual String string => "";
    shared actual Boolean emptyNode = true;
    shared actual Iterator<IReportNode> iterator() => emptyIterator;
}

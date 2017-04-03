import ceylon.interop.java {
    JavaComparator
}

import java.util {
    Vector,
    JCollections=Collections
}

import javax.swing.tree {
    MutableTreeNode
}

import strategicprimer.report {
    IReportNode
}
"A [[SectionListReportNode]] that sorts itself after every addition."
shared class SortedSectionListReportNode(Integer level, String text)
        extends SectionListReportNode(level, text) {
    shared actual void appendNode(MutableTreeNode newChild) {
        super.appendNode(newChild);
        assert (is Vector<IReportNode> temp = children);
        JCollections.sort(temp,
            JavaComparator((IReportNode x, IReportNode y) => x <=> y));
    }
}

import ceylon.interop.java {
    JavaComparator
}

import java.util {
    Vector,
    JCollections=Collections,
    JComparator=Comparator
}

import javax.swing.tree {
    MutableTreeNode
}

import strategicprimer.report {
    IReportNode
}
import lovelace.util.common {
    todo
}
"A [[SectionListReportNode]] that sorts itself after every addition. (Callers can turn
 that feature off temporarily with [[suspend]], and turn it back on again with
 [[resume]].)"
shared class SortedSectionListReportNode
        extends SectionListReportNode {
    static JComparator<IReportNode> sorter =
            JavaComparator((IReportNode x, IReportNode y) => x <=> y);
    shared new (Integer level, String text)
            extends SectionListReportNode(level, text) {}
    variable Boolean sorting = true;
    shared actual void appendNode(MutableTreeNode newChild) {
        super.appendNode(newChild);
        if (sorting) {
            assert (is Vector<IReportNode> temp = children);
            JCollections.sort(temp, sorter);
        }
    }
    "Stop sorting on every addition."
    shared void suspend() => sorting = false;
    "Sort, and resume sorting on every addition."
    shared void resume() {
        sorting = true;
        assert (is Vector<IReportNode> temp = children);
        JCollections.sort(temp, sorter);
    }
}

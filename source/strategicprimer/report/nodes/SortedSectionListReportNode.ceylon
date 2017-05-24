import ceylon.interop.java {
    JavaComparator
}

import java.util {
    JCollections=Collections,
    JComparator=Comparator,
    JList=List
}

import javax.swing.tree {
    MutableTreeNode
}

import strategicprimer.report {
    IReportNode
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
            // We use List<> rather than Vector<> because the latter fails at runtime
            // in the metamodel with "Class has more than one overloaded constructor"
            // TODO: report this bug (if it isn't already known)
            assert (is JList<IReportNode> temp = children);
            JCollections.sort(temp, sorter);
        }
    }
    "Stop sorting on every addition."
    shared void suspend() => sorting = false;
    "Sort, and resume sorting on every addition."
    shared void resume() {
        sorting = true;
        if (exists temp = children) {
            // We use List<> rather than Vector<> because the latter fails at runtime
            // in the metamodel with "Class has more than one overloaded constructor"
            // TODO: report this bug (if it isn't already known)
            assert (is JList<IReportNode> temp);
            JCollections.sort(temp, sorter);
        }
    }
}

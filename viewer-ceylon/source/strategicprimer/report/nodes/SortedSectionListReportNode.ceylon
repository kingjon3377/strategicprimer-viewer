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
"A [[SectionListReportNode]] that sorts itself after every addition."
todo("Sorting after *every* addition is probably part of why we see such poor performance; we
      really only need to sort after the *last* addition.")
shared class SortedSectionListReportNode
        extends SectionListReportNode {
    static JComparator<IReportNode> sorter =
            JavaComparator((IReportNode x, IReportNode y) => x <=> y);
    shared new (Integer level, String text)
            extends SectionListReportNode(level, text) {}

    shared actual void appendNode(MutableTreeNode newChild) {
        super.appendNode(newChild);
        assert (is Vector<IReportNode> temp = children);
        JCollections.sort(temp, sorter);
    }
}

package model.report;

import java.util.Collections;

import javax.swing.tree.MutableTreeNode;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A node that sorts itself after every addition.
 * @author Jonathan Lovelace
 *
 */
public class SortedSectionListReportNode extends SectionListReportNode {
	/**
	 * Constructor.
	 * @param level the header level
	 * @param text the header text
	 */
	public SortedSectionListReportNode(final int level, final String text) {
		super(level, text);
	}
	/**
	 * Add a node, then sort.
	 * @param node the node to add
	 */
	@Override
	public void add(@Nullable final MutableTreeNode node) {
		super.add(node);
		Collections.sort(children);
	}
}

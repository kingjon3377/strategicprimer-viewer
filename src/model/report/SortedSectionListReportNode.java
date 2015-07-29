package model.report;

import java.util.Collections;

import javax.swing.tree.MutableTreeNode;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Point;

/**
 * A node that sorts itself after every addition.
 *
 * @author Jonathan Lovelace
 *
 */
public class SortedSectionListReportNode extends SectionListReportNode {
	/**
	 * Constructor.
	 *
	 * @param point the point, if any, in the map that this represents something on
	 * @param level the header level
	 * @param text the header text
	 */
	public SortedSectionListReportNode(@Nullable final Point point, final int level, final String text) {
		super(point, level, text);
	}

	/**
	 * Add a node, then sort.
	 *
	 * @param node the node to add
	 */
	@SuppressWarnings("unchecked") // Nothing we can do about it ...
	@Override
	public void add(@Nullable final MutableTreeNode node) {
		super.add(node);
		Collections.sort(children);
	}
}

package model.report;

import javax.swing.tree.TreeNode;

import model.map.Point;
import util.NullCleaner;

/**
 * A node for a section consisting only of a list. This is a common case, and
 * we'd otherwise end up with a section node containing only a list.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class SectionListReportNode extends AbstractReportNode {
	/**
	 * The header level.
	 */
	private int level;

	/**
	 * An optional sub-header. Since this only comes up once at present, we only
	 * expose it in the constructor.
	 */
	private final String subheader;

	/**
	 * The size of the boilerplate text we have even before we add the size of
	 * the children and the header.
	 */
	private static final int MIN_BOILERPLATE = "<h1></h1>\n<p></p>\n<ul>\n</ul>\n"
			.length();
	/**
	 * The size of the boilerplate text we have to add for each child.
	 */
	private static final int PER_CHILD_BPLATE = "<li></li>\n".length();

	/**
	 * Constructor.
	 *
	 * @param point the point, if any, in the map that this represents something on
	 * @param lvl the header level
	 * @param header the header text
	 */
	public SectionListReportNode(final Point point, final int lvl,
			final String header) {
		super(point, header);
		setLevel(lvl);
		subheader = "";
	}

	/**
	 * Constructor.
	 *
	 * @param lvl the header level
	 * @param header the header text
	 */
	public SectionListReportNode(final int lvl, final String header) {
		super(header);
		setLevel(lvl);
		subheader = "";
	}
	/**
	 * @return the HTML representation of the node
	 */
	@Override
	public String produce() {
		return NullCleaner.assertNotNull(produce(new StringBuilder(size()))
				.toString());
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		builder.append("<h").append(level).append('>').append(getText())
				.append("</h").append(level).append(">\n");
		if (!subheader.isEmpty()) {
			builder.append("<p>").append(subheader).append("</p>\n");
		}
		if (getChildCount() != 0) {
			builder.append("<ul>\n");
			for (int i = 0; i < getChildCount(); i++) {
				final TreeNode child = getChildAt(i);
				if (child instanceof AbstractReportNode) {
					builder.append("<li>");
					builder.append(((AbstractReportNode) child).produce());
					builder.append("</li>\n");
				}
			}
			builder.append("</ul>\n");
		}
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will
	 *         be.
	 */
	@Override
	public int size() {
		int retval = MIN_BOILERPLATE + getText().length() + subheader.length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				retval += ((AbstractReportNode) child).size()
						+ PER_CHILD_BPLATE;
			}
		}
		return retval;
	}

	/**
	 * @param lvl the new header level
	 */
	public final void setLevel(final int lvl) {
		level = lvl;
	}

	/**
	 * @return the header level
	 */
	public final int getHeaderLevel() {
		return level;
	}

	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final IReportNode obj) {
		return obj instanceof SectionListReportNode
				&& ((SectionListReportNode) obj).getHeaderLevel() == level
				&& getText().equals(obj.getText())
				&& children().equals(obj.children());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return level + getText().hashCode() /* | children().hashCode() */;
	}
}

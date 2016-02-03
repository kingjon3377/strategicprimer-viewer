package model.report;

import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import model.map.Point;
import org.eclipse.jdt.annotation.Nullable;
import util.EnumerationWrapper;
import util.NullCleaner;

/**
 * A node for a section consisting only of a list. This is a common case, and we'd
 * otherwise end up with a section node containing only a list.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class SectionListReportNode extends DefaultMutableTreeNode
		implements IReportNode {
	/**
	 * The header level.
	 */
	private int level;

	/**
	 * An optional sub-header. Since this only comes up once at present, we only
	 * expose it
	 * in the constructor.
	 */
	private final String subheader;

	/**
	 * The size of the boilerplate text we have even before we add the size of the
	 * children and the header.
	 */
	private static final int MIN_BOILERPLATE = "<h1></h1>\n<p></p>\n<ul>\n</ul>\n"
			                                           .length();
	/**
	 * The size of the boilerplate text we have to add for each child.
	 */
	private static final int PER_CHILD_BPLATE = "<li></li>\n".length();
	/**
	 * The point, if any, in the map that this node represents something on.
	 */
	@SuppressWarnings("FieldHasSetterButNoGetter")
	@Nullable
	private Point point;
	/**
	 * The (usually header) text. May be empty, but not null.
	 */
	private String text;

	/**
	 * Constructor.
	 *
	 * @param pt  the point, if any, in the map that this represents something on
	 * @param lvl    the header level
	 * @param header the header text
	 */
	public SectionListReportNode(final Point pt, final int lvl,
	                             final String header) {
		super(header);
		setText(header);
		point = pt;
		level = lvl;
		subheader = "";
	}

	/**
	 * Constructor.
	 *
	 * @param lvl    the header level
	 * @param header the header text
	 */
	public SectionListReportNode(final int lvl, final String header) {
		super(header);
		setText(header);
		point = null;
		level = lvl;
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
		builder.append("<h").append(level).append('>').append(text)
				.append("</h").append(level).append(">\n");
		if (!subheader.isEmpty()) {
			builder.append("<p>").append(subheader).append("</p>\n");
		}
		if (getChildCount() != 0) {
			builder.append("<ul>\n");
			for (int i = 0; i < getChildCount(); i++) {
				final TreeNode child = getChildAt(i);
				if (child instanceof IReportNode) {
					builder.append("<li>");
					builder.append(((IReportNode) child).produce());
					builder.append("</li>\n");
				}
			}
			builder.append("</ul>\n");
		}
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		int retval = MIN_BOILERPLATE + text.length() + subheader.length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				retval += ((IReportNode) child).size()
						          + PER_CHILD_BPLATE;
			}
		}
		return retval;
	}

	/**
	 * @param lvl the new header level
	 */
	public final void setHeaderLevel(final int lvl) {
		level = lvl;
	}

	/**
	 * @return the header level
	 */
	public final int getHeaderLevel() {
		return level;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof SectionListReportNode) &&
				                         (((SectionListReportNode) obj).level == level) &&
				                         text
								                                                         .equals(((IReportNode) obj)
										                                                                 .getText()) &&
				                         children()
						                         .equals(((IReportNode) obj).children()));
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return level + text.hashCode() /* | children().hashCode() */;
	}

	/**
	 * Add a node. Do nothing if null, rather than crashing.
	 *
	 * @param node the node to add
	 */
	@Override
	public void add(final @Nullable MutableTreeNode node) {
		if (node != null) {
			if (node instanceof IReportNode) {
				if (!((IReportNode) node).isEmptyNode()) {
					super.add(node);
				}
			} else {
				super.add(node);
			}
		}
	}

	/**
	 * @return an iterator over the children
	 */
	@Override
	public Iterator<IReportNode> iterator() {
		return new EnumerationWrapper<>(children());
	}

	/**
	 * Add generic-type information for the compiler.
	 */
	@SuppressWarnings("EmptyMethod")
	@Override
	public Enumeration<IReportNode> children() {
		//noinspection unchecked
		return super.children();
	}

	/**
	 * @return the text of the node, usually the header.
	 */
	@Override
	public final String getText() {
		return text;
	}

	/**
	 * @param txt the new text for the node
	 */
	@Override
	public final void setText(final String txt) {
		text = txt;
		setUserObject(text);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return text;
	}

	/**
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public final void setPoint(final Point pt) {
		point = pt;
	}

	/**
	 * @return the point, if any, in the map that this node in particular represents something on
	 */
	@Override
	@Nullable
	public final Point getLocalPoint() {
		return point;
	}
}

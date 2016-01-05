package model.report;

import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.eclipse.jdt.annotation.Nullable;
import util.EnumerationWrapper;
import util.NullCleaner;

/**
 * A node representing a section, with a header.
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
public final class SectionReportNode extends AbstractReportNode implements IReportNode {
	/**
	 * The header level.
	 */
	private int level;

	/**
	 * Constructor.
	 *
	 * @param lvl    the header level
	 * @param header the header text
	 */
	public SectionReportNode(final int lvl, final String header) {
		super(header);
		setHeaderLevel(lvl);
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
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				((IReportNode) child).produce(builder);
			}
		}
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		int retval = 16 + getText().length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				retval += ((IReportNode) child).size();
			}
		}
		return retval;
	}

	/**
	 * @param lvl the new header level
	 */
	public void setHeaderLevel(final int lvl) {
		level = lvl;
	}

	/**
	 * @return the header level
	 */
	public int getHeaderLevel() {
		return level;
	}

	/**
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	protected boolean equalsNode(final IReportNode obj) {
		return (obj instanceof SectionReportNode)
				       && (level == ((SectionReportNode) obj).getLevel())
				       && getText().equals(obj.getText())
				       && children().equals(obj.children());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return level + getText().hashCode();
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof IReportNode)
				                         && equalsNode((IReportNode) obj));
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return hashCodeImpl();
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
	@Override
	public Enumeration<IReportNode> children() {
		return super.children();
	}
}

package model.report;

import util.NullCleaner;

import javax.swing.tree.TreeNode;

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
public final class SectionReportNode extends AbstractReportNode {
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
		setLevel(lvl);
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
			if (child instanceof AbstractReportNode) {
				((AbstractReportNode) child).produce(builder);
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
			if (child instanceof AbstractReportNode) {
				retval += ((AbstractReportNode) child).size();
			}
		}
		return retval;
	}

	/**
	 * @param lvl the new header level
	 */
	public void setLevel(final int lvl) {
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
	protected boolean equalsImpl(final IReportNode obj) {
		return obj instanceof SectionReportNode
				       && level == ((SectionReportNode) obj).getLevel()
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
}

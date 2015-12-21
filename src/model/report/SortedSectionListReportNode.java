package model.report;

import java.util.Collections;
import javax.swing.tree.MutableTreeNode;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A node that sorts itself after every addition.
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
public final class SortedSectionListReportNode extends SectionListReportNode {
	/**
	 * Constructor.
	 *
	 * @param level the header level
	 * @param text  the header text
	 */
	public SortedSectionListReportNode(final int level, final String text) {
		super(level, text);
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

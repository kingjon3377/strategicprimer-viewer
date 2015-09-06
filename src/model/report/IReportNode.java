package model.report;

import javax.swing.tree.MutableTreeNode;

/**
 * An interface for report nodes.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
 */
public interface IReportNode extends Comparable<IReportNode>, MutableTreeNode {
	/**
	 * @return the HTML representation of the node.
	 */
	String produce();
	/**
	 * @param builder a string builder
	 * @return that builder, with an HTML representation of the node added.
	 */
	StringBuilder produce(StringBuilder builder);
	/**
	 * @return an approximation of how large the HTML produced by this node will
	 *         be.
	 */
	int size();
	/**
	 * @return the text of the node, usually the header.
	 */
	String getText();
	/**
	 * @param txt the new text for the node
	 */
	void setText(String txt);
}

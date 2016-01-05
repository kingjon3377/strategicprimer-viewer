package model.report;

import javax.swing.tree.DefaultMutableTreeNode;
import model.map.Point;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A superclass for report-nodes.
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
public abstract class AbstractReportNode extends DefaultMutableTreeNode
		implements IReportNode, Iterable<@NonNull IReportNode> {

	/**
	 * Constructor.
	 *
	 * @param pt  the point in the map that this node represents something on
	 * @param txt the (header) text.
	 */
	protected AbstractReportNode(final Point pt, final String txt) {
		super(txt);
		setText(txt);
		setPoint(pt);
	}

	/**
	 * Constructor.
	 *
	 * @param txt the (header) text.
	 */
	protected AbstractReportNode(final String txt) {
		super(txt);
		setText(txt);
		setPoint(null);
	}

	/**
	 * @param obj a node
	 * @return whether it's equal to this one.
	 */
	protected abstract boolean equalsNode(final IReportNode obj);

	/**
	 * @return a hash code for the object
	 */
	protected abstract int hashCodeImpl();

}

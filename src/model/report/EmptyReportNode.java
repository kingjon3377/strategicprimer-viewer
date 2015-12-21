package model.report;

/**
 * A node to replace usages of null.
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
public final class EmptyReportNode extends AbstractReportNode {
	/**
	 * Let's make this singleton, to reduce object allocations further.
	 */
	public static final EmptyReportNode NULL_NODE = new EmptyReportNode();

	/**
	 * Constructor.
	 */
	private EmptyReportNode() {
		super("");
	}

	/**
	 * @return the empty string
	 */
	@Override
	public String produce() {
		return "";
	}

	/**
	 * @param builder the string-builder used to build the report
	 * @return it, unmodified
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		return builder;
	}

	/**
	 * @return the number of characters we'll add to the report, namely zero.
	 */
	@Override
	public int size() {
		return 0;
	}

	/**
	 * @param obj an object
	 * @return whether it equals this; all EmptyReportNodes are equal.
	 */
	@Override
	protected boolean equalsImpl(final IReportNode obj) {
		return (this == obj) || (obj instanceof EmptyReportNode);
	}

	/**
	 * @return a constant hash code
	 */
	@Override
	protected int hashCodeImpl() {
		return 0;
	}

	/**
	 * @return true: this is "the empty node."
	 */
	@Override
	protected boolean isEmptyNode() {
		return true;
	}
}

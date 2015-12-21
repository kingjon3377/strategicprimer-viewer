package model.report;

import model.map.Point;
import util.NullCleaner;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple node representing plain text. Any children are ignored!
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
public final class SimpleReportNode extends AbstractReportNode {
	/**
	 * @param point the point, if any, in the map that this represents something on
	 * @param texts a number of strings to concatenate and make the text of the node.
	 */
	public SimpleReportNode(final Point point, final String... texts) {
		super(point, concat(texts));
	}

	/**
	 * @param texts a number of strings to concatenate and make the text of the node.
	 */
	public SimpleReportNode(final String... texts) {
		super(concat(texts));
	}

	/**
	 * @param strings a number of strings
	 * @return them all concatenated together
	 */
	private static String concat(final String... strings) {
		final StringBuilder builder =
				new StringBuilder(2 + Stream.of(strings).collect(
						Collectors.summingInt(String::length)).intValue());
		Stream.of(strings).forEach(builder::append);
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @return the HTML representation of the node, its text.
	 */
	@Override
	public String produce() {
		return getText();
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		return NullCleaner.assertNotNull(builder.append(getText()));
	}

	/**
	 * @return the size of the HTML representation of the node.
	 */
	@Override
	public int size() {
		return getText().length();
	}

	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final IReportNode obj) {
		return (obj instanceof SimpleReportNode)
				       && getText().equals(obj.getText());
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode();
	}

}

package model.report;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import model.map.Point;
import org.eclipse.jdt.annotation.Nullable;
import util.EnumerationWrapper;
import util.LineEnd;
import util.NullCleaner;

/**
 * A node representing a list.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("CloneableClassInSecureContext")
public final class ListReportNode extends DefaultMutableTreeNode
		implements IReportNode {
	/**
	 * The length of the boilerplate even if we have no text and no children.
	 */
	private static final int BOILERPLATE_LEN = "<ul></ul>".length() + 3;

	/**
	 * The estimated size of a child: half a kilobyte, which is absurdly high, but we
	 * *really* don't want to resize the buffer!
	 */
	private static final int CHILD_BUF_SIZE = 512;
	/**
	 * The length of the boilerplate per child.
	 */
	private static final int PER_CHILD_BOILERPLATE = "<li></li>".length() + 1;
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
	 * @param pt  the point in the map that this represents something on
	 * @param txt the header text
	 */
	public ListReportNode(final Point pt, final String txt) {
		super(txt);
		text = txt; // required by Eclipse
		setText(txt);
		point = pt;
	}

	/**
	 * @param txt the header text
	 */
	public ListReportNode(final String txt) {
		super(txt);
		text = txt; // required by Eclipse
		setText(txt);
		point = null;
	}

	/**
	 * @return the HTML representation of the node.
	 */
	@Override
	public String produce() {
		// Assume each child is half a K.
		final StringBuilder builder = new StringBuilder(text.length() + BOILERPLATE_LEN +
																(getChildCount() *
																		 CHILD_BUF_SIZE))
											  .append(text).append(LineEnd.LINE_SEP);
		builder.append("<ul>").append(LineEnd.LINE_SEP);
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				builder.append("<li>");
				builder.append(((IReportNode) child).produce());
				builder.append("</li>");
				builder.append(LineEnd.LINE_SEP);
			}
		}
		builder.append("</ul>");
		builder.append(LineEnd.LINE_SEP);
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		builder.append(text);
		builder.append(LineEnd.LINE_SEP);
		builder.append("<ul>");
		builder.append(LineEnd.LINE_SEP);
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof IReportNode) {
				builder.append("<li>");
				((IReportNode) child).produce(builder);
				builder.append("</li>");
				builder.append(LineEnd.LINE_SEP);
			}
		}
		builder.append("</ul>");
		builder.append(LineEnd.LINE_SEP);
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		return BOILERPLATE_LEN + text.length() +
					   stream().mapToInt(child -> child.size() + PER_CHILD_BOILERPLATE)
							   .sum();
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof ListReportNode) &&
										 text.equals(((IReportNode) obj).getText()) &&
										 children()
												 .equals(((IReportNode) obj).children
																					 ()));
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return text.hashCode();
	}

	/**
	 * Add a node. Do nothing if null, rather than crashing.
	 *
	 * @param newChild the node to add
	 */
	@Override
	public void add(final @Nullable MutableTreeNode newChild) {
		if (IReportNode.isNonEmptyNode(newChild)) {
			super.add(newChild);
		}
	}

	/**
	 * Add generic-type information for the compiler.
	 */
	@SuppressWarnings({"unchecked", "EmptyMethod"})
	@Override
	public Enumeration<IReportNode> children() {
		return super.children();
	}

	/**
	 * @return the text of the node, usually the header.
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * @param txt the new text for the node
	 */
	@Override
	public void setText(@Nullable final String txt) {
		if (txt != null) {
			text = txt;
			setUserObject(text);
		}
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
	public void setPoint(final Point pt) {
		point = pt;
	}

	/**
	 * @return the point, if any, in the map that this node in particular represents
	 * something on
	 */
	@Override
	@Nullable
	public Point getLocalPoint() {
		return point;
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Superclass removes CloneNotSupportedException from method signature, but we still
	 * want to throw it, so we wrap it in a RuntimeException
	 *
	 * @return never
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public ListReportNode clone() {
		throw new IllegalStateException("cloning prohibited",
											   new CloneNotSupportedException("cloning " +
																					  "prohibited"));
	}
}

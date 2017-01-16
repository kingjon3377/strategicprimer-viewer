package model.report;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Formatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import model.map.Point;
import org.eclipse.jdt.annotation.Nullable;
import util.NoCloneException;

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
public final class ListReportNode extends DefaultMutableTreeNode
		implements IReportNode {
	/**
	 * The length of the boilerplate even if we have no text and no children.
	 */
	private static final int BOILERPLATE_LEN = "<ul></ul>".length() + 3;

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
	 * Constructor.
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
	 * Constructor taking header text but no point.
	 * @param txt the header text
	 */
	public ListReportNode(final String txt) {
		super(txt);
		text = txt; // required by Eclipse
		setText(txt);
		point = null;
	}

	/**
	 * Write the HTML representation of the node to a Formatter.
	 * @param formatter a Formatter to write the HTML representation to
	 */
	@Override
	public void produce(final Formatter formatter) {
		formatter.format("%s%n<ul>%n", text);
		for (final IReportNode node : this) {
			formatter.format("<li>");
			node.produce(formatter);
			formatter.format("</li>%n");
		}
		formatter.format("</ul>%n");
	}

	/**
	 * The approximate size of the HTML representation of the node.
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		return BOILERPLATE_LEN + text.length() +
					   stream().mapToInt(child -> child.size() + PER_CHILD_BOILERPLATE)
							   .sum();
	}

	/**
	 * An object is equal iff it is a ListReportNode with equal text and children.
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
	 * We delegate to the header string.
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
	 * The header text.
	 * @return the text of the node, usually the header.
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * Set the header text.
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
	 * This simply returns the header text.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return text;
	}

	/**
	 * Set the point this represents.
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public void setPoint(final Point pt) {
		point = pt;
	}

	/**
	 * Get the point, if any, that this node (as opposed to its children) refers to.
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
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Superclass removes CloneNotSupportedException from method signature, but we still
	 * want to throw it, so we wrap it in a RuntimeException.
	 *
	 * @return never
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public ListReportNode clone() {
		throw new NoCloneException("cloning prohibited");
	}
}

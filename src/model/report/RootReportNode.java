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
 * The root of a node hierarchy.
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
public final class RootReportNode extends DefaultMutableTreeNode
		implements IReportNode {
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
	 * @param title the title text
	 */
	public RootReportNode(final String title) {
		super(title);
		text = title; // required by Eclipse
		setText(title);
		point = null;
	}

	/**
	 * Write the HTML to a formatter.
	 * @param formatter a Formatter to write the HTML representation to
	 */
	@Override
	public void produce(final Formatter formatter) {
		formatter.format("<html>%n<head><title>%s</title></head>%n<body>", text);
		for (final IReportNode child : this) {
			child.produce(formatter);
		}
		formatter.format("</body>%n</html>%n");
	}

	/**
	 * How long the HTML representation of this node (recursively) will be.
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		return 72 + text.length() + stream().mapToInt(IReportNode::size).sum();
	}

	/**
	 * An object is equal iff it is a RootReportNode with the same text and equal
	 * children in the same order.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof RootReportNode) &&
										 text.equals(((IReportNode) obj).getText()) &&
										 children()
												 .equals(((IReportNode) obj).children
																					 ()));
	}

	/**
	 * Use the header text's hash value, for stability.
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
	@SuppressWarnings("EmptyMethod")
	@Override
	public Enumeration<IReportNode> children() {
		//noinspection unchecked
		return super.children();
	}

	/**
	 * The text of the node.
	 * @return the text of the node, usually the header.
	 */
	@Override
	public String getText() {
		return text;
	}

	/** Set the text of the node.
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
	 * Returns the text of the node.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return text;
	}

	/**
	 * Set the point, if any, that this represents something at.
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public void setPoint(final Point pt) {
		point = pt;
	}

	/**
	 * Get the point, if any, that this point in particular represents something at.
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
	 * Prevent serialization.
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
	 * want to throw it, so we wrap it in a RuntimeException.
	 *
	 * @return never
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public RootReportNode clone() {
		throw new NoCloneException("cloning prohibited");
	}
}

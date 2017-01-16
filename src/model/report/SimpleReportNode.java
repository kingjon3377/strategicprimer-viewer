package model.report;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.stream.Stream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import model.map.Point;
import org.eclipse.jdt.annotation.Nullable;
import util.NoCloneException;

/**
 * A simple node representing plain text. Any children are ignored!
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
public final class SimpleReportNode extends DefaultMutableTreeNode
		implements IReportNode {
	/**
	 * The point, if any, in the map that this node represents something on.
	 */
	@SuppressWarnings(
			{"FieldHasSetterButNoGetter", "InstanceVariableMayNotBeInitialized"})
	@Nullable
	private Point point;
	/**
	 * The (usually header) text. May be empty, but not null.
	 */
	private String text;

	/**
	 * Constructor that the public constructors delegate to.
	 * @param concatenated a concatenated string to make the text of the node
	 */
	private SimpleReportNode(final String concatenated) {
		super(concatenated);
		text = concatenated; // required by Eclipse
		setText(concatenated);
	}

	/**
	 * Constructor.
	 * @param pt    the point, if any, in the map that this represents something on
	 * @param texts a number of strings to concatenate and make the text of the node.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	public SimpleReportNode(final Point pt, final String... texts) {
		this(concat(texts));
		point = pt;
	}

	/**
	 * Constructor not taking a Point.
	 * @param texts a number of strings to concatenate and make the text of the node.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	public SimpleReportNode(final String... texts) {
		this(concat(texts));
		point = null;
	}

	/**
	 * Concatenate a number of Strings.
	 * @param strings a number of strings
	 * @return them all concatenated together
	 */
	private static String concat(final String... strings) {
		final StringBuilder builder = new StringBuilder(2 + Stream.of(strings).mapToInt(
				String::length).sum());
		Stream.of(strings).forEach(builder::append);
		return builder.toString();
	}

	/**
	 * The HTML representation of the node is its text.
	 * @return the HTML representation of the node, its text.
	 */
	@Override
	public String produce() {
		return text;
	}

	/**
	 * Write the HTML representation of the node to a Formatter.
	 * @param formatter a Formatter to write the HTML representation to
	 */
	@Override
	public void produce(final Formatter formatter) {
		formatter.format("%s", text);
	}

	/**
	 * The size of the HTML representation of the node.
	 * @return the size of the HTML representation of the node.
	 */
	@Override
	public int size() {
		return text.length();
	}

	/**
	 * An object is equal iff it is a SimpleReportNode with the same text.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof SimpleReportNode) &&
										 text.equals(((IReportNode) obj).getText()));
	}

	/**
	 * Use the text's hash value for ours.
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
		// Do nothing
	}

	/**
	 * This method is ignored; a simple node cannot have children.
	 */
	@Override
	public void addAsFirst(final @Nullable MutableTreeNode node) {
		// Do nothing
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
	 * The text of the node.
	 * @return the text of the node, usually the header.
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * Set the text of the node.
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
	 * Use the text of the node as its String representation.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return text;
	}

	/**
	 * Set the location, if any, of what this represents.
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public void setPoint(final Point pt) {
		point = pt;
	}

	/**
	 * The location, if any, that this represents something at.
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
	public SimpleReportNode clone() {
		throw new NoCloneException("cloning prohibited");
	}
}

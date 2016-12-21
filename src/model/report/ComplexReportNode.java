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
 * A node for cases slightly more complex than a {@link SimpleReportNode} covers: the text
 * here isn't really a header, and no wrapping children as a list, but we can *have*
 * children. For example, when a report needs to have multiple lists, each with its own
 * header.
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
public final class ComplexReportNode extends DefaultMutableTreeNode
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
	 * @param txt the main text of the node
	 * @param pt  the point, if any, in the map that this represents something on
	 */
	public ComplexReportNode(final Point pt, final String txt) {
		super(txt);
		text = txt; // required by Eclipse
		setText(txt);
		point = pt;
	}

	/**
	 * Constructor for the point-is-null case.
	 * @param txt the main text of the node
	 */
	public ComplexReportNode(final String txt) {
		super(txt);
		text = txt; // required by Eclipse
		setText(txt);
		point = null;
	}

	/**
	 * No-arg constructor.
	 */
	public ComplexReportNode() {
		super("");
		text = ""; // required by Eclipse
		setText("");
		point = null;
	}

	/**
	 * Writes the header text, followed by the HTML representation of each child in
	 * order, to the StringBuilder.
	 * @param formatter a Formatter to write the HTML representation to
	 */
	@Override
	public void produce(final Formatter formatter) {
		formatter.format("%s", text);
		for (final IReportNode node : this) {
			node.produce(formatter);
		}
	}

	/**
	 * Calculate the size in characters of the HTML representation of this node and its
	 * children.
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		return text.length() + stream().mapToInt(IReportNode::size).sum();
	}

	/**
	 * We are equal only to other ComplexReportNodes with the same header text and equal
	 * children.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof ComplexReportNode) &&
										 text.equals(((IReportNode) obj).getText()) &&
										 children()
												 .equals(((IReportNode) obj).children()));
	}

	/**
	 * Our hash code only reflects our header text, to ensure its stability.
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
	 * The header text for the node.
	 * @return the text of the node, usually the header.
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * Set new header text for the node.
	 * @param txt the new text for the node
	 */
	@Override
	public void setText(final String txt) {
		text = txt;
		setUserObject(text);
	}

	/**
	 * We just return the header text.
	 *
	 * TODO: should this reflect children?
	 *
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return text;
	}

	/**
	 * Set the associated map location.
	 * @param pt the point, if any, in the map that this represents something on
	 */
	@Override
	public void setPoint(final Point pt) {
		point = pt;
	}

	/**
	 * Get the associated map location.
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
	public ComplexReportNode clone() {
		throw new NoCloneException("cloning prohibited");
	}
}

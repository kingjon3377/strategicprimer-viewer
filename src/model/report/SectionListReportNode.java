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
 * A node for a section consisting only of a list. This is a common case, and we'd
 * otherwise end up with a section node containing only a list.
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
public class SectionListReportNode extends DefaultMutableTreeNode
		implements IReportNode {
	/**
	 * The size of the boilerplate text we have even before we add the size of the
	 * children and the header.
	 */
	private static final int MIN_BOILERPLATE =
			"<h1></h1><p></p><ul></ul>".length() + 4;
	/**
	 * The size of the boilerplate text we have to add for each child.
	 */
	private static final int PER_CHILD_BPLATE = "<li></li>".length() + 1;
	/**
	 * An optional sub-header. Since this only comes up once at present, we only
	 * expose it
	 * in the constructor.
	 */
	private final String subHeader;
	/**
	 * The header level.
	 */
	private final int level;
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
	 * @param pt     the point, if any, in the map that this represents something on
	 * @param lvl    the header level
	 * @param header the header text
	 */
	public SectionListReportNode(final Point pt, final int lvl,
								 final String header) {
		super(header);
		text = header; // required by Eclipse
		setText(header);
		point = pt;
		level = lvl;
		subHeader = "";
	}

	/**
	 * Constructor.
	 *
	 * @param lvl    the header level
	 * @param header the header text
	 */
	public SectionListReportNode(final int lvl, final String header) {
		super(header);
		text = header; // required by Eclipse
		setText(header);
		point = null;
		level = lvl;
		subHeader = "";
	}

	/**
	 * Write the HTML representation to a Formatter.
	 * @param formatter a Formatter to write the HTML representation to
	 */
	@Override
	public void produce(final Formatter formatter) {
		formatter.format("<h%d>%s</h%d>%n", Integer.valueOf(level), text,
				Integer.valueOf(level));
		if (!subHeader.isEmpty()) {
			formatter.format("<p>%s</p>%n", subHeader);
		}
		if (getChildCount() != 0) {
			formatter.format("<ul>%n");
			for (final IReportNode child : this) {
				formatter.format("<li>");
				child.produce(formatter);
				formatter.format("</li>%n");
			}
			formatter.format("</ul>%n");
		}
	}

	/**
	 * The approximate size of the HTML representation of this node.
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		return MIN_BOILERPLATE + text.length() + subHeader.length() +
					   stream().mapToInt(node -> node.size() + PER_CHILD_BPLATE).sum();
	}

	/**
	 * An object is equal iff it is a SectionListReportNode with the same header text
	 * and section level and equal children in the same order.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof SectionListReportNode) &&
										 (((SectionListReportNode) obj).level ==
												  level) &&
										 text.equals(((IReportNode) obj).getText()) &&
										 children()
												 .equals(((IReportNode) obj).children
																					 ()));
	}

	/**
	 * Use the level and the header text in determining the hash code.
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return level + text.hashCode() /* | children().hashCode() */;
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
	 * The header text.
	 * @return the text of the node, usually the header.
	 */
	@Override
	public final String getText() {
		return text;
	}

	/**
	 * Set the header text. Null is ignored.
	 * @param txt the new text for the node
	 */
	@Override
	public final void setText(@Nullable final String txt) {
		if (txt != null) {
			text = txt;
			setUserObject(text);
		}
	}

	/**
	 * Returns the header text as the String representation.
	 * TODO: Should we use the children?
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
	public final void setPoint(final Point pt) {
		point = pt;
	}

	/**
	 * Get the point, if any, that this node (as opposed to its children) represents.
	 * @return the point, if any, in the map that this node in particular represents
	 * something on
	 */
	@Override
	@Nullable
	public final Point getLocalPoint() {
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
	public final SectionListReportNode clone() {
		throw new NoCloneException("cloning prohibited");
	}
}

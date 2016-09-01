package controller.map.formatexceptions;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;
import util.NullCleaner;

/**
 * A custom exception for when a tag has a child tag it can't handle.
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
public final class UnwantedChildException extends SPFormatException {

	/**
	 * The current tag.
	 */
	private final QName tag;

	/**
	 * The unwanted child.
	 */
	private final QName childTag;
	/**
	 * For when the unwanted child isn't an unwanted *tag* but an unwanted tag *with
	 * some property* that we want to describe using a QName.
	 * @param parent    the current tag
	 * @param child     the unwanted child
	 * @param location  the location where this occurred
	 * @param cause		why this occurred
	 */
	public UnwantedChildException(final QName parent, final QName child,
								final Location location, final Throwable cause) {
		super("Unexpected child " + child.getLocalPart() + " in tag " +
					parent.getLocalPart(), location, cause);
		tag = parent;
		childTag = child;
	}
	/**
	 * @param parent    the current tag
	 * @param child     the unwanted child
	 */
	public UnwantedChildException(final QName parent, final StartElement child) {
		super("Unexpected child " + child.getName().getLocalPart() + " in tag "
				+ parent.getLocalPart(),
				NullCleaner.assertNotNull(child.getLocation()));
		tag = parent;
		childTag = NullCleaner.assertNotNull(child.getName());
	}

	/**
	 * @param parent    the current tag
	 * @param child     the unwanted child
	 * @param cause     another exception that caused this one
	 */
	public UnwantedChildException(final QName parent, final StartElement child,
								final Throwable cause) {
		super("Unexpected child " + child.getName().getLocalPart() + " in tag "
				+ parent.getLocalPart(),
				NullCleaner.assertNotNull(child.getLocation()), cause);
		tag = parent;
		childTag = NullCleaner.assertNotNull(child.getName());
	}
	/**
	 * Copy-constructor-with-replacement, for cases where the original thrower didn't
	 * know the parent tag.
	 * @param parent the parent tag
	 * @param except the exception to copy
	 */
	public UnwantedChildException(final QName parent, final UnwantedChildException except) {
		super("Unexpected child " + except.childTag.getLocalPart() + " in tag " +
					parent.getLocalPart(), except.getLocation());
		tag = parent;
		childTag = except.childTag;
		if (except.getCause() != null) {
			initCause(except.getCause());
		}
	}
	/**
	 * @return the current tag.
	 */
	public QName getTag() {
		return tag;
	}

	/**
	 * @return the unwanted child.
	 */
	public QName getChild() {
		return childTag;
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}

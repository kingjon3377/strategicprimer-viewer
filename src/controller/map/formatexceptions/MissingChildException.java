package controller.map.formatexceptions;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import util.NullCleaner;

/**
 * A custom exception for when a tag (or a Node) requires a child and it isn't there.
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
public final class MissingChildException extends SPFormatException {
	/**
	 * The current tag.
	 */
	private final QName context;

	/**
	 * @param tag the current tag (the one that needs a child)
	 */
	public MissingChildException(final StartElement tag) {
		super("Tag " + tag.getName().getLocalPart() + " missing a child",
				NullCleaner.assertNotNull(tag.getLocation()));
		context = NullCleaner.assertNotNull(tag.getName());
	}

	/**
	 * @return the current tag.
	 */
	public QName getTag() {
		return context;
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
}

package controller.map.formatexceptions;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * A custom exception for cases where a tag has a property it doesn't support.
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
public final class UnsupportedPropertyException extends SPFormatException {

	/**
	 * The current tag.
	 */
	private final QName context;
	/**
	 * The unsupported parameter.
	 */
	private final String param;

	/**
	 * Constructor.
	 * @param tag       the current tag
	 * @param parameter the unsupported parameter
	 */
	public UnsupportedPropertyException(final StartElement tag, final String parameter) {
		super("Unsupported property " + parameter + " in tag "
					  + tag.getName().getLocalPart(), tag.getLocation());
		context = tag.getName();
		param = parameter;
	}

	/**
	 * The current tag.
	 * @return the current tag
	 */
	public QName getTag() {
		return context;
	}

	/**
	 * The unupported property.
	 * @return the unsupported parameter
	 */
	public String getParam() {
		return param;
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
}

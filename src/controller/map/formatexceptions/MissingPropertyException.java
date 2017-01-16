package controller.map.formatexceptions;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * An exception for cases where a parameter is required (or, if this is merely logged,
 * recommended) but missing.
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
public final class MissingPropertyException extends SPFormatException {

	/**
	 * The current tag.
	 */
	private final QName context;
	/**
	 * The missing parameter.
	 */
	private final String param;

	/**
	 * Constructor taking an exception that caused this one.
	 * @param tag       the current tag
	 * @param parameter the missing parameter
	 * @param cause     the underlying cause
	 */
	public MissingPropertyException(final StartElement tag, final String parameter,
									final Throwable cause) {
		super("Missing parameter " + parameter + " in tag " +
					  tag.getName().getLocalPart(), tag.getLocation(), cause);
		context = tag.getName();
		param = parameter;
	}

	/**
	 * Constructor.
	 * @param tag       the current tag
	 * @param parameter the missing parameter
	 */
	public MissingPropertyException(final StartElement tag, final String parameter) {
		super("Missing parameter " + parameter + " in tag " +
					  tag.getName().getLocalPart(), tag.getLocation());
		context = tag.getName();
		param = parameter;
	}

	/**
	 * The tag missing a parameter.
	 * @return the current tag
	 */
	public QName getTag() {
		return context;
	}

	/**
	 * The parameter it wants.
	 * @return the missing parameter
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

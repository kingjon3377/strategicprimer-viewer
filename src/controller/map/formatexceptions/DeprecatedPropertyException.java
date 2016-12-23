package controller.map.formatexceptions;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * A custom exception for cases where one property is deprecated in favor of another.
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
public final class DeprecatedPropertyException extends SPFormatException {
	/**
	 * The current tag.
	 */
	private final QName context;

	/**
	 * The old property.
	 */
	private final String old;

	/**
	 * The preferred form.
	 */
	private final String preferred;

	/**
	 * Constructor.
	 * @param tag        the current tag
	 * @param deprecated the old form
	 * @param newForm    the preferred form
	 */
	public DeprecatedPropertyException(final StartElement tag,
									   final String deprecated, final String newForm) {
		super("Use of the property '" + deprecated + "' in tag '"
					  + tag.getName().getLocalPart() + "' is deprecated; use '"
					  + newForm + "' instead", tag.getLocation());
		context = tag.getName();
		old = deprecated;
		preferred = newForm;
	}

	/**
	 * The current tag.
	 * @return the current tag.
	 */
	public QName getTag() {
		return context;
	}

	/**
	 * The old name for the property.
	 * @return the old name for the property.
	 */
	public String getOld() {
		return old;
	}

	/**
	 * The preferred form for the property.
	 * @return the preferred orm.
	 */
	public String getPreferred() {
		return preferred;
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
}

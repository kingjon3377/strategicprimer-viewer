package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An interface for XML readers that can read multiple related types, in the sixth
 * generation of SP XML I/O ("yet another SP XML reader").
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the common supertype of all types this can return
 * @author Jonathan Lovelace
 */
public interface YAReader<@NonNull T> {
	/**
	 * Read an object from XML.
	 * @param element   the element being parsed
	 * @param parent    the parent tag
	 * @param stream    to read more elements from
	 * @return the object parsed from XML
	 * @throws SPFormatException on SP format errors
	 */
	T read(StartElement element, final QName parent, Iterable<XMLEvent> stream)
			throws SPFormatException;

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  the current indentation level.
	 * @throws IOException on I/O problems.
	 */
	void write(Appendable ostream, T obj, int indent) throws IOException;

	/**
	 * Whether we can read the supported tag.
	 * @param tag a tag.
	 * @return whether we support it.
	 */
	boolean isSupportedTag(String tag);

	/**
	 * Whether we can write the given object.
	 * @param obj an object
	 * @return whether we can write it
	 */
	boolean canWrite(Object obj);

	/**
	 * Write, when the caller knows the object is the right type but doesn't know what
	 * type that is. Throws ClassCastException if it's not the right type.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  the current indentation level.
	 * @throws IOException on I/O problems.
	 */
	@SuppressWarnings("unchecked")
	default void writeRaw(final Appendable ostream, final Object obj, final int indent)
			throws IOException {
		write(ostream, (T) obj, indent);
	}
}

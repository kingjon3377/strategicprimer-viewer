package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import org.eclipse.jdt.annotation.NonNull;
import util.IteratorWrapper;
import util.Warning;

/**
 * An interface for XML readers that can read multiple related types.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the common supertype of all types this can return
 * @author Jonathan Lovelace
 */
public interface CompactReader<@NonNull T> {
	/**
	 * @param element   the element being parsed
	 * @param stream    to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use as needed
	 * @param idFactory the ID factory to use as needed
	 * @return the object parsed from XML
	 * @throws SPFormatException on SP format errors
	 */
	T read(StartElement element, IteratorWrapper<XMLEvent> stream,
	       IMutablePlayerCollection players, Warning warner, IDFactory idFactory)
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
	 * @param tag a tag. May be null, to simplify callers.
	 * @return whether we support it. Should return false if null.
	 */
	boolean isSupportedTag(String tag);

	/**
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

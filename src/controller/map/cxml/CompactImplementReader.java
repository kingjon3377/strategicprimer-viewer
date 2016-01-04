package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.Implement;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for implements.
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
 * @author Jonathan Lovelace
 *
 *         TODO: Should this also handle (under a different class name) other fortress
 *         members?
 */
public final class CompactImplementReader extends AbstractCompactReader<Implement> {
	/**
	 * Singleton object.
	 */
	public static final CompactReader<Implement> READER = new CompactImplementReader();
	static {
		CompactReaderAdapter.register(READER);
	}
	/**
	 * Singleton.
	 */
	private CompactImplementReader() {
		// Singleton.
	}

	/**
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed implement
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Implement read(final StartElement element,
	                      final IteratorWrapper<XMLEvent> stream,
	                      final IMutablePlayerCollection players, final Warning warner,
	                      final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "implement");
		final Implement retval =
				new Implement(getOrGenerateID(element, warner, idFactory),
						             getParameter(element, "kind"));
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	/**
	 * @param tag a tag
	 * @return whether it's one we supported
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "implement".equalsIgnoreCase(tag);
	}

	/**
	 * Write an implementto a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the implement to write
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final Implement obj,
	                  final int indent) throws IOException {
		for (int i = 0; i < indent; i++) {
			ostream.append('\t');
		}
		ostream.append("<implement id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append("\" kind=\"");
		ostream.append(obj.getKind());
		ostream.append('"').append(imageXML(obj)).append(" />\n");
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactImplementReader";
	}
}

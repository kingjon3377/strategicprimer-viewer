package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.Implement;
import util.LineEnd;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for implements.
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
 * @author Jonathan Lovelace
 * @deprecated CompactXML is deprecated in favor of FluidXML
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Deprecated
public final class CompactImplementReader extends AbstractCompactReader<Implement> {
	/**
	 * Singleton object.
	 */
	public static final CompactReader<Implement> READER = new CompactImplementReader();
	/**
	 * Singleton.
	 */
	private CompactImplementReader() {
		// Singleton.
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent
	 *@param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @param stream    the stream to read more elements from
	 * @return the parsed implement
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Implement read(final StartElement element,
						  final QName parent, final IMutablePlayerCollection players,
						  final Warning warner, final IDRegistrar idFactory,
						  final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "implement");
		final Implement retval =
				new Implement(getParameter(element, "kind"),
									 getOrGenerateID(element, warner, idFactory)
				);
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
	 * Write an implement to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the implement to write
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final Implement obj,
					final int indent) throws IOException {
		writeTag(ostream, "implement", indent);
		ostream.append(" kind=\"");
		ostream.append(obj.getKind());
		ostream.append("\" id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append('"').append(imageXML(obj)).append(" />");
		ostream.append(LineEnd.LINE_SEP);
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Implement;
	}
}

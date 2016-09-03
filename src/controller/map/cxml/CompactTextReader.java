package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.TextFixture;
import util.LineEnd;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for tiles, including rivers.
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
public final class CompactTextReader extends AbstractCompactReader<TextFixture> {
	/**
	 * Singleton object.
	 */
	public static final CompactReader<TextFixture> READER = new CompactTextReader();
	/**
	 * Singleton.
	 */
	private CompactTextReader() {
		// Singleton.
	}

	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "text".equalsIgnoreCase(tag);
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent	the parent tag
	 *@param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format errors
	 */
	@Override
	public TextFixture read(final StartElement element,
							final QName parent, final IMutablePlayerCollection players,
							final Warning warner, final IDRegistrar idFactory,
							final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "text");
		// Of all the uses of a StringBuilder, this one can't know what size we
		// need. But cases above 2K will be vanishingly rare in practice.
		final StringBuilder builder = new StringBuilder(2048);
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException(
						NullCleaner.assertNotNull(element.getName()),
						NullCleaner.assertNotNull(event.asStartElement()));
			} else if (event.isCharacters()) {
				builder.append(event.asCharacters().getData());
			} else if (event.isEndElement()
							&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		final TextFixture fix = new TextFixture(NullCleaner.assertNotNull(
				builder.toString().trim()), getIntegerParameter(element, "turn", -1));
		fix.setImage(getParameter(element, "image", ""));
		return fix;
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final TextFixture obj,
					final int indent) throws IOException {
		writeTag(ostream, "text", indent);
		if (obj.getTurn() != -1) {
			ostream.append(" turn=\"");
			ostream.append(Integer.toString(obj.getTurn()));
			ostream.append('"');
		}
		ostream.append(imageXML(obj));
		ostream.append('>');
		ostream.append(obj.getText().trim());
		ostream.append("</text>");
		ostream.append(LineEnd.LINE_SEP);
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof TextFixture;
	}

}

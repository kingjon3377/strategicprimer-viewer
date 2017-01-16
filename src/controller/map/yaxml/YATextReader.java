package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.fixtures.TextFixture;
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
 */
public final class YATextReader extends YAAbstractReader<TextFixture> {
	/**
	 * Constructor.
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YATextReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	/**
	 * We only support the "text" tag.
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "text".equalsIgnoreCase(tag);
	}

	/**
	 * Read a text fixture from XML.
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format errors
	 */
	@Override
	public TextFixture read(final StartElement element, final QName parent,
							final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "text");
		// Of all the uses of a StringBuilder, this one can't know what size we
		// need. But cases above 2K will be vanishingly rare in practice.
		final StringBuilder builder = new StringBuilder(2048);
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException(element.getName(),
														event.asStartElement());
			} else if (event.isCharacters()) {
				builder.append(event.asCharacters().getData());
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		final TextFixture fix = new TextFixture(builder.toString().trim(),
													   getIntegerParameter(element,
															   "turn", -1));
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
			writeProperty(ostream, "turn", obj.getTurn());
		}
		writeImageXML(ostream, obj);
		ostream.append('>');
		ostream.append(obj.getText().trim());
		closeTag(ostream, 0, "text");
	}

	/**
	 * We can only write TextFixtures.
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof TextFixture;
	}

}

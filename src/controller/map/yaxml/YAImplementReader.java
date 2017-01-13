package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.fixtures.Implement;
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
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class YAImplementReader extends YAAbstractReader<Implement> {
	/**
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YAImplementReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}
	/**
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @return the parsed implement
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Implement read(final StartElement element,
						  final QName parent,
						  final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "implement");
		final Implement retval =
				new Implement(getParameter(element, "kind"), getOrGenerateID(element));
		spinUntilEnd(element.getName(), stream);
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
		writeProperty(ostream, "kind", obj.getKind());
		writeProperty(ostream, "id", obj.getID());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
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

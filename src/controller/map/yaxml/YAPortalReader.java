package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.fixtures.explorable.Portal;
import util.Warning;

/**
 * A reader for portals.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class YAPortalReader extends YAAbstractReader<Portal> {
	/**
	 * Constructor.
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YAPortalReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}
	/**
	 * Read a portal from XML.
	 *
	 * @param element   The XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from     @return the parsed
	 *                  portal
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Portal read(final StartElement element,
					   final QName parent,
					   final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "portal");
		final Portal retval = new Portal(getParameter(element, "world"),
												parsePoint(element),
												getOrGenerateID(element));
		retval.setImage(getParameter(element, "image", ""));
		spinUntilEnd(element.getName(), stream);
		return retval;
	}

	/**
	 * Write a portal to XML.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the portal to write
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final Portal obj,
					  final int indent) throws IOException {
		writeTag(ostream, "portal", indent);
		writeProperty(ostream, "world", obj.getDestinationWorld());
		writeProperty(ostream, "row", obj.getDestinationCoordinates().getRow());
		writeProperty(ostream, "column", obj.getDestinationCoordinates().getCol());
		writeProperty(ostream, "id", obj.getID());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	/**
	 * We only support the "portal" tag.
	 * @param tag a tag
	 * @return whether it is one we support
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "portal".equalsIgnoreCase(tag);
	}

	/**
	 * We can only write Portals.
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Portal;
	}
}

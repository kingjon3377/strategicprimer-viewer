package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.fixtures.Ground;
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
public final class YAGroundReader extends YAAbstractReader<Ground> {

	/**
	 * Constructor.
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory to use for ID numbers
	 */
	public YAGroundReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}
	/**
	 * Read a Ground from XML.
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Ground read(final StartElement element,
					   final QName parent,
					   final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "ground");
		final String kind = getParamWithDeprecatedForm(element, "kind",
				"ground");
		requireNonEmptyParameter(element, "exposed", true);
		spinUntilEnd(element.getName(), stream);
		final Ground retval = new Ground(kind, Boolean.parseBoolean(
				getParameter(element, "exposed")));
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	/**
	 * We only support the "ground" tag.
	 * @param tag a tag
	 * @return whether it's one we support
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "ground".equalsIgnoreCase(tag);
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
	public void write(final Appendable ostream, final Ground obj, final int indent)
			throws IOException {
		writeTag(ostream, "ground", indent);
		writeProperty(ostream, "kind", obj.getKind());
		writeProperty(ostream, "exposed", Boolean.toString(obj.isExposed()));
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	/**
	 * We can only write Ground.
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Ground;
	}
}

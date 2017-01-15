package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.ExplorableFixture;
import util.Warning;

/**
 * A reader for Caves and Battlefields.
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
@SuppressWarnings("ClassHasNoToStringMethod")
public final class YAExplorableReader extends YAAbstractReader<ExplorableFixture> {
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS =
			Collections.unmodifiableSet(
					new HashSet<>(Arrays.asList("cave", "battlefield")));
	/**
	 * Constructor.
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YAExplorableReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	/**
	 * Get the "DC" parameter. TODO: Is this worth having an extra method?
	 * @param element a tag
	 * @return the value of its 'dc' property.
	 * @throws SPFormatException on SP format problem
	 */
	private static int getDC(final StartElement element)
			throws SPFormatException {
		return getIntegerParameter(element, "dc");
	}

	/**
	 * Whether we can read the given tag.
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return SUPP_TAGS.contains(tag);
	}

	/**
	 * Read a cave or battlefield from XML.
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from     @return the parsed
	 *                  resource
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public ExplorableFixture read(final StartElement element,
								  final QName parent,
								  final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "battlefield", "cave");
		final int idNum = getOrGenerateID(element);
		final ExplorableFixture retval;
		final String tag = element.getName().getLocalPart();
		if ("battlefield".equalsIgnoreCase(tag)) {
			retval = new Battlefield(getDC(element), idNum);
		} else if ("cave".equalsIgnoreCase(tag)) {
			retval = new Cave(getDC(element), idNum);
		} else {
			throw new UnsupportedTagException(element);
		}
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
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
	public void write(final Appendable ostream, final ExplorableFixture obj,
					  final int indent) throws IOException {
		if (obj instanceof Battlefield) {
			writeTag(ostream, "battlefield", indent);
		} else if (obj instanceof Cave) {
			writeTag(ostream, "cave", indent);
		} else {
			throw new IllegalStateException("Unhandled ExplorableFixture subtype");
		}
		writeProperty(ostream, "dc", obj.getDC());
		writeProperty(ostream, "id", obj.getID());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	/**
	 * We can only write Caves and Battlefields.
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return (obj instanceof Battlefield) || (obj instanceof Cave);
	}

}

package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.Ground;
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
@Deprecated
@SuppressWarnings("ClassHasNoToStringMethod")
public final class CompactGroundReader extends AbstractCompactReader<Ground> {
	/**
	 * Singleton object.
	 */
	public static final CompactReader<Ground> READER = new CompactGroundReader();

	/**
	 * Singleton.
	 */
	private CompactGroundReader() {
		// Singleton.
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Ground read(final StartElement element,
					   final QName parent, final IMutablePlayerCollection players,
					   final Warning warner, final IDRegistrar idFactory,
					   final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "ground");
		final String kind = getParamWithDeprecatedForm(element, "kind",
				"ground", warner);
		requireNonEmptyParameter(element, "exposed", true, warner);
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Ground retval = new Ground(kind,
												Boolean.parseBoolean(
														getParameter(element,
																"exposed")));
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	/**
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
		ostream.append(imageXML(obj));
		closeLeafTag(ostream);
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Ground;
	}
}

package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.Ground;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for tiles, including rivers.
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
 */
public final class CompactGroundReader extends AbstractCompactReader<Ground> {
	/**
	 * Singleton object.
	 */
	public static final CompactGroundReader READER = new CompactGroundReader();
	/**
	 * Singleton.
	 */
	private CompactGroundReader() {
		// Singleton.
	}

	/**
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Ground read(final StartElement element,
	                   final IteratorWrapper<XMLEvent> stream,
	                   final IMutablePlayerCollection players, final Warning warner,
	                   final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "ground");
		final String kind = getParamWithDeprecatedForm(element, "kind",
				"ground", warner);
		requireNonEmptyParameter(element, "exposed", true, warner);
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Ground retval = new Ground(kind,
				                                Boolean.parseBoolean(getParameter
						                                                     (element,
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
		for (int i = 0; i < indent; i++) {
			ostream.append('\t');
		}
		ostream.append("<ground kind=\"");
		ostream.append(obj.getKind());
		ostream.append("\" exposed=\"");
		ostream.append(Boolean.toString(obj.isExposed()));
		ostream.append('"').append(imageXML(obj)).append(" />\n");
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactGroundReader";
	}
	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	public boolean canWrite(final Object obj) {
		return obj instanceof Ground;
	}
}

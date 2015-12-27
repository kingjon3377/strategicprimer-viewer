package controller.map.readerng;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.TextFixture;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for text elements.
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
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class TextReader implements INodeHandler<TextFixture> {
	/**
	 * Parse a TextFixture.
	 *
	 * @param element   the element to parse
	 * @param stream    the stream to get more elements (in this case, the text) from
	 * @param players   ignored
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the TextFixture
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TextFixture parse(final StartElement element,
							 final Iterable<XMLEvent> stream,
							 final IMutablePlayerCollection players,
							 final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		// Of all our uses of StringBuilder, here we can't know how much size
		// we're going to need beforehand. But cases where we'll need more than
		// 2K will be vanishingly rare in practice.
		final StringBuilder sbuild = new StringBuilder(2048); // NOPMD
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("text", NullCleaner.assertNotNull(
						event.asStartElement().getName().getLocalPart()),
						                                event.getLocation());
			} else if (event.isCharacters()) {
				sbuild.append(event.asCharacters().getData());
			} else if (event.isEndElement()
							   &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		final TextFixture fix =
				new TextFixture(NullCleaner.assertNotNull(sbuild.toString().trim()),
						               XMLHelper
								               .getIntegerAttribute(element, "turn", -1));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("text"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<TextFixture> writtenClass() {
		return TextFixture.class;
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the adapter
	 *            work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends TextFixture> SPIntermediateRepresentation write(
																			 final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
																							"text");
		if (obj.getTurn() != -1) {
			retval.addIntegerAttribute("turn", obj.getTurn());
		}
		retval.addAttribute("text-contents",
				NullCleaner.assertNotNull(obj.getText().trim()));
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TextReader";
	}
}

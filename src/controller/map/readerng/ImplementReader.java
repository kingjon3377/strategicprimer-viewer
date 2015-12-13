package controller.map.readerng;

import static controller.map.readerng.XMLHelper.addImage;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.NonNull;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.Implement;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for Implements.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class ImplementReader implements INodeHandler<@NonNull Implement> {
	/**
	 * Parse an implement.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the implement represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Implement parse(final StartElement element,
			final Iterable<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Implement retval =
				new Implement(getOrGenerateID(element, warner, idFactory),
						getAttribute(element, "kind"));
		addImage(element, retval);
		return retval;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("implement"));
	}
	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Implement> writes() {
		return Implement.class;
	}
	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Implement obj) {
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("implement");
		retval.addIdAttribute(obj.getID());
		retval.addAttribute("kind", obj.getKind());
		retval.addImageAttribute(obj);
		return retval;
	}
	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return "ImplementReader";
	}
}

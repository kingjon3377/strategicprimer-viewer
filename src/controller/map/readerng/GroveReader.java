package controller.map.readerng;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.resources.Grove;
import util.NullCleaner;
import util.Pair;
import util.Warning;

import static controller.map.readerng.XMLHelper.addImage;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.hasAttribute;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

/**
 * A reader for Groves.
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
public final class GroveReader implements INodeHandler<Grove> {
	/**
	 * The name of the 'cultivated' property.
	 */
	private static final String CULTIVATED_ATTR = "cultivated";

	/**
	 * Parse a grove.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the grove represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Grove parse(final StartElement element,
					   final Iterable<XMLEvent> stream,
					   final IMutablePlayerCollection players,
					   final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final boolean cultivated = isCultivated(element, warner);
		final Grove fix = new Grove(
										   "orchard".equalsIgnoreCase(
												   element.getName().getLocalPart()),
										   cultivated,
										   getAttributeWithDeprecatedForm(element,
												   "kind",
												   "tree", warner),
										   getOrGenerateID(element, warner, idFactory));
		addImage(element, fix);
		return fix;
	}

	/**
	 * @param element the element representing the XML tag representing the grove
	 * @param warner  the Warning instance to use
	 * @return whether the grove or orchard is cultivated
	 * @throws SPFormatException on XML format problems: use of 'wild' rather than
	 *                           'cultivated' if warnings are fatal, or both properties
	 *                           missing ever.
	 */
	private static boolean isCultivated(final StartElement element,
										final Warning warner) throws SPFormatException {
		if (hasAttribute(element, CULTIVATED_ATTR)) {
			return Boolean.parseBoolean(getAttribute(element, // NOPMD
					CULTIVATED_ATTR));
		} else {
		final QName local = element.getName();
			if (hasAttribute(element, "wild")) {
				warner.warn(
						new DeprecatedPropertyException(local, "wild", CULTIVATED_ATTR,
								                               element.getLocation()));
				return !Boolean.parseBoolean(getAttribute(element,
						"wild")); // NOPMD
			} else {
				throw new MissingPropertyException(element, CULTIVATED_ATTR);
			}
		}
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Arrays.asList("grove", "orchard"));
	}

	/**
	 * @return the kind we know how to parse
	 */
	@Override
	public Class<Grove> writtenClass() {
		return Grove.class;
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
	public <S extends Grove> SPIntermediateRepresentation write(final S obj) {
		final String tag; // NOPMD
		if (obj.isOrchard()) {
			tag = "orchard";
		} else {
			tag = "grove";
		}
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation(tag, Pair.of(CULTIVATED_ATTR,
						NullCleaner.assertNotNull(Boolean.toString(obj
																		   .isCultivated()))),
														Pair.of("kind",
																obj.getKind()));
		retval.addIdAttribute(obj.getID());
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "GroveReader";
	}
}

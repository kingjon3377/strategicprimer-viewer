package controller.map.readerng;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.fixtures.explorable.Portal;
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;
import util.Pair;
import util.Warning;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Collections;
import java.util.List;

import static controller.map.readerng.XMLHelper.spinUntilEnd;

/**
 * A reader for portals.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
public final class PortalReader implements INodeHandler<@NonNull Portal> {
	/**
	 * Parse a portal. Parse an adventure hook.
	 *
	 * @param element   the element to read from
	 * @param stream    a stream of more elements
	 * @param players   the list of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the parsed portal
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Portal parse(final StartElement element,
	                    final Iterable<XMLEvent> stream,
	                    final IMutablePlayerCollection players,
	                    final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Portal retval =
				new Portal(
						          XMLHelper.getAttribute(element, "world"),
						          PointFactory.point(XMLHelper.parseInt(XMLHelper
								                                                .getAttribute(
										                                                element,
										                                                "row"),

								          NullCleaner
										          .assertNotNull(element.getLocation())),
								          XMLHelper.parseInt(XMLHelper.getAttribute(
										          element, "column"), NullCleaner
												                              .assertNotNull(
														                              element.getLocation()))),
						          XMLHelper.getOrGenerateID(element, warner, idFactory));
		XMLHelper.addImage(element, retval);
		return retval;
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Portal> writtenClass() {
		return Portal.class;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("portal"));
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Portal obj) {
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("portal", Pair.of("world",
						obj.getDestinationWorld()));
		final Point dest = obj.getDestinationCoordinates();
		retval.addIntegerAttribute("row", dest.row);
		retval.addIntegerAttribute("column", dest.col);
		retval.addIdAttribute(obj.getID());
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return "PortalReader";
	}
}

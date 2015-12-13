package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.getPlayerOrIndependent;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.NonNull;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for fortifications.
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
public final class FortificationReader implements INodeHandler<@NonNull Fortification> {
	/**
	 * Parse a fortification.
	 *
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the parsed city
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Fortification parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IMutablePlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "name", false, warner);
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Fortification fix =
				new Fortification(TownStatus.parseTownStatus(getAttribute(
						element, "status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")), XMLHelper.parseInt(
						getAttribute(element, "dc"),
						NullCleaner.assertNotNull(element.getLocation())),
						getAttribute(element, "name", ""), getOrGenerateID(
								element, warner, idFactory),
						getPlayerOrIndependent(element, warner, players));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("fortification"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Fortification> writes() {
		return Fortification.class;
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Fortification obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"fortification");
		retval.addAttribute("status", obj.status().toString());
		retval.addAttribute("size", obj.size().toString());
		retval.addIntegerAttribute("dc", obj.getDC());
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addIdAttribute(obj.getID());
		retval.addIntegerAttribute("owner", obj.getOwner().getPlayerId());
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FortificationReader";
	}
}

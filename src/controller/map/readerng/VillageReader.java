package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.getPlayerOrIndependent;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import model.workermgmt.RaceFactory;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for Villages.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class VillageReader implements INodeHandler<Village> {
	/**
	 * Parse a village.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the village represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Village parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IMutablePlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "name", false, warner);
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final Village fix = new Village(
				TownStatus.parseTownStatus(getAttribute(element, "status")),
				getAttribute(element, "name", ""), idNum,
				getPlayerOrIndependent(element, warner, players),
				getAttribute(element, "race",
						RaceFactory.getRace(new Random(idNum))));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("village");
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends Village> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"village");
		retval.addAttribute("status", obj.status().toString());
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addIdAttribute(obj.getID());
		retval.addIntegerAttribute("owner", obj.getOwner().getPlayerId());
		retval.addAttribute("race", obj.getRace());
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return The class we know how to parse
	 */
	@Override
	public Class<Village> writes() {
		return Village.class;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "VillageReader";
	}
}

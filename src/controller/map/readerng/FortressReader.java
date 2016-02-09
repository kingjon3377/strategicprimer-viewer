package controller.map.readerng;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import util.EqualsAny;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;

/**
 * A reader for fortresses.
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
public final class FortressReader implements INodeHandler<Fortress> {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(FortressReader.class);

	/**
	 * Parse a fortress.
	 *
	 * @param element   the element to start with
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the fortress
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Fortress parse(final StartElement element,
	                      final Iterable<XMLEvent> stream,
	                      final IMutablePlayerCollection players,
	                      final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "owner", false, warner);
		requireNonEmptyParameter(element, "name", false, warner);
		final Fortress fort =
				new Fortress(players.getPlayer(getIntegerAttribute(element, "owner",
						-1)),
						            getAttribute(element, "name", ""),
						            getOrGenerateID(element, warner, idFactory));
		addImage(element, fort);
		for (final XMLEvent event : stream) {
			if (event.isStartElement() && EqualsAny.equalsAny(
					event.asStartElement().getName().getNamespaceURI(),
					ISPReader.NAMESPACE, XMLConstants.NULL_NS_URI)) {
				final String memberTag = event.asStartElement().getName()
												 .getLocalPart().toLowerCase();
				if ("unit".equals(memberTag)) {
					fort.addMember(UNIT_READER.parse(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream, players, warner, idFactory));
				} else if ("implement".equals(memberTag)) {
					fort.addMember(IMPL_READER.parse(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream, players, warner, idFactory));
				} else if ("resource".equals(memberTag)) {
					fort.addMember(RES_READER.parse(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(),
							                                event.asStartElement());
				}
			} else if (event.isEndElement()
							   &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		return fort;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("fortress"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Fortress> writtenClass() {
		return Fortress.class;
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
	public <S extends Fortress> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
																							"fortress");
		retval.addIntegerAttribute("owner", obj.getOwner().getPlayerId());
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addIdAttribute(obj.getID());
		for (final FortressMember member : obj) {
			if (member instanceof Unit) {
				retval.addChild(UNIT_READER.write((Unit) member));
			} else if (member instanceof Implement) {
				retval.addChild(IMPL_READER.write((Implement) member));
			} else if (member instanceof ResourcePile) {
				retval.addChild(RES_READER.write((ResourcePile) member));
			} else {
				LOGGER.severe("Unhandled FortressMember class: "
									  + member.getClass().getName());
			}
		}
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * The reader to use to parse units.
	 */
	private static final UnitReader UNIT_READER = new UnitReader();
	/**
	 * The reader to use to parse Implements.
	 */
	private static final ImplementReader IMPL_READER = new ImplementReader();
	/**
	 * The reader to use to parse Resource Piles.
	 */
	private static final ResourceReader RES_READER = new ResourceReader();

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "FortressReader";
	}
}

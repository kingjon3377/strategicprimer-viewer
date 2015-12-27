package controller.map.readerng;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import util.NullCleaner;
import util.Warning;

import static controller.map.readerng.XMLHelper.addImage;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.parseInt;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;

/**
 * A reader for Units.
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
public final class UnitReader implements INodeHandler<Unit> {
	/**
	 * The name of the property telling what kind of unit.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Parse a unit.
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
	public Unit parse(final StartElement element,
					  final Iterable<XMLEvent> stream,
					  final IMutablePlayerCollection players,
					  final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "owner", false, warner);
		requireNonEmptyParameter(element, "name", false, warner);
		final Unit fix =
				new Unit(players.getPlayer(parseInt(
						ensureNumeric(getAttribute(element, "owner", "-1")),
						NullCleaner.assertNotNull(element.getLocation()))),
								parseKind(element, warner), getAttribute(element,
						"name", ""), getOrGenerateID(element, warner,
						idFactory));
		addImage(element, fix);
		final StringBuilder orders = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement selem =
						NullCleaner.assertNotNull(event.asStartElement());
				final Object result = ReaderAdapter.ADAPTER.parse(selem,
						stream, players, warner, idFactory);
				if (result instanceof UnitMember) {
					fix.addMember((UnitMember) result);
				} else {
					throw new UnwantedChildException(NullCleaner.assertNotNull(
							element.getName().getLocalPart()), NullCleaner.assertNotNull(
							selem.getName().getLocalPart()), event.getLocation());
				}
			} else if (event.isCharacters()) {
				orders.append(event.asCharacters().getData());
			} else if (event.isEndElement()
							   &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		fix.setOrders(NullCleaner.assertNotNull(orders.toString().trim()));
		return fix;
	}

	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the empty
	 * string.
	 *
	 * @param element the current element
	 * @param warner  the Warning instance to use
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private static String parseKind(final StartElement element,
									final Warning warner) throws SPFormatException {
		try {
			final String retval = getAttributeWithDeprecatedForm(element, // NOPMD
					KIND_PROPERTY, "type", warner);
			if (retval.isEmpty()) {
				warner.warn(new MissingPropertyException(NullCleaner.assertNotNull(
						element.getName()), KIND_PROPERTY, element.getLocation()));
			}
			return retval; // NOPMD
		} catch (final MissingPropertyException except) {
			warner.warn(except);
			return ""; // NOPMD
		}
	}

	/**
	 * @param text a string that may be either numeric or empty.
	 * @return it, or "-1" if it's empty.
	 */
	private static String ensureNumeric(final String text) {
		if (text.isEmpty()) {
			return "-1"; // NOPMD
		} else {
			return text;
		}
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("unit"));
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
	public <S extends Unit> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
																							"unit");
		retval.addIntegerAttribute("owner", obj.getOwner().getPlayerId());
		if (!obj.getKind().isEmpty()) {
			retval.addAttribute("kind", obj.getKind());
		}
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addIdAttribute(obj.getID());
		for (final UnitMember member : obj) {
			retval.addChild(ReaderAdapter.ADAPTER.write(member));
		}
		if (!obj.getOrders().trim().isEmpty()) {
			retval.addAttribute("text-contents", obj.getOrders().trim() + '\n');
		}
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return The type we know how to write
	 */
	@Override
	public Class<Unit> writtenClass() {
		return Unit.class;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "UnitReader";
	}
}

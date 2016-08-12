package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDRegistrar;
import javax.xml.XMLConstants;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.IPlayerCollection;
import model.map.Player;
import model.map.PointFactory;
import model.map.fixtures.TextFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.Portal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import util.EqualsAny;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.createElement;
import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeNonEmptyAttribute;
import static util.NullCleaner.assertNotNull;

/**
 * A class to hold XML I/O for explorable fixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
public final class FluidExplorableHandler {
	/**
	 * Do not instantiate.
	 */
	private FluidExplorableHandler() {
		// Do not instantiate
	}
	/**
	 * Read an adventure from XML.
	 *
	 * @param element   The XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed adventure
	 * @throws SPFormatException on SP format problems
	 */
	public static AdventureFixture readAdventure(final StartElement element,
												 final Iterable<XMLEvent> stream,
												 final IPlayerCollection players,
												 final Warning warner,
												 final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "adventure");
		final Player player;
		if (XMLHelper.hasAttribute(element, "owner")) {
			player = players.getPlayer(getIntegerAttribute(element, "owner"));
		} else {
			player = players.getIndependent();
		}
		final AdventureFixture retval =
				setImage(new AdventureFixture(player, getAttribute(element, "brief", ""),
											getAttribute(element, "full", ""),
											getOrGenerateID(element, warner, idFactory)), element, warner);
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return retval;
	}
	/**
	 * Read a portal from XML.
	 *
	 * @param element   The XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed portal
	 * @throws SPFormatException on SP format problems
	 */
	@SuppressWarnings("UnusedParameters")
	public static Portal readPortal(final StartElement element,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner,
									final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, "portal");
		final Portal retval = setImage(new Portal(getAttribute(element, "world"),
												PointFactory.point(getIntegerAttribute(
														element, "row"),
														getIntegerAttribute(element,
																"column")),
												getOrGenerateID(element, warner,
														idFactory)), element, warner);
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return retval;
	}

	/**
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed resource
	 * @throws SPFormatException on SP format problems
	 */
	@SuppressWarnings("UnusedParameters")
	public static Cave readCave(final StartElement element,
								final Iterable<XMLEvent> stream,
								final IMutablePlayerCollection players,
								final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "cave");
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final Cave retval = new Cave(getIntegerAttribute(element, "dc"), idNum);
		spinUntilEnd(element.getName(), stream);
		return setImage(retval, element, warner);
	}

	/**
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed resource
	 * @throws SPFormatException on SP format problems
	 */
	@SuppressWarnings("UnusedParameters")
	public static Battlefield readBattlefield(final StartElement element,
											  final Iterable<XMLEvent> stream,
											  final IMutablePlayerCollection
															players,
											  final Warning warner,
											  final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "battlefield");
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final Battlefield retval =
				new Battlefield(getIntegerAttribute(element, "dc"), idNum);
		spinUntilEnd(element.getName(), stream);
		return setImage(retval, element, warner);
	}
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
	@SuppressWarnings("UnusedParameters")
	public static TextFixture readTextFixture(final StartElement element,
											  final Iterable<XMLEvent> stream,
											  final IMutablePlayerCollection players,
											  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "text");
		// Of all our uses of StringBuilder, here we can't know how much size
		// we're going to need beforehand. But cases where we'll need more than
		// 2K will be vanishingly rare in practice.
		final StringBuilder builder = new StringBuilder(2048);
		for (final XMLEvent event : stream) {
			if (event.isStartElement() && EqualsAny.equalsAny(
					assertNotNull(event.asStartElement().getName().getNamespaceURI()),
					ISPReader.NAMESPACE, XMLConstants.NULL_NS_URI)) {
				throw new UnwantedChildException(assertNotNull(element.getName()),
														assertNotNull(event.asStartElement()));
			} else if (event.isCharacters()) {
				builder.append(event.asCharacters().getData());
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return setImage(new TextFixture(assertNotNull(builder.toString().trim()),
							   getIntegerAttribute(element, "turn", -1)), element, warner);
	}
	/**
	 * Create DOM subtree representing an adventure hook.
	 * @param document the Document object, used to get new Elements
	 * @param parent The parent Element to which the subtree should be attached
	 * @param obj The object being written.
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeAdventure(final Document document, final Node parent, Object obj) {
		// TODO: Create helper method for this idiom, so we don't have to sacrifice
		// one coverage-miss line per method.
		if (!(obj instanceof AdventureFixture)) {
			throw new IllegalArgumentException("Can only write AdventureFixtures");
		}
		final AdventureFixture adv = (AdventureFixture) obj;
		final Element element = createElement(document, "adventure");
		writeIntegerAttribute(element, "id", adv.getID());
		if (!adv.getOwner().isIndependent()) {
			writeIntegerAttribute(element, "owner", adv.getOwner().getPlayerId());
		}
		writeNonEmptyAttribute(element, "brief", adv.getBriefDescription());
		writeNonEmptyAttribute(element, "full", adv.getFullDescription());
		writeImage(element, adv);
		parent.appendChild(element);
	}
	/**
	 * Write a portal to XML.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @param obj The object being written. Must be a Portal.
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writePortal(final Document document, final Node parent,
								   Object obj) {
		if (!(obj instanceof Portal)) {
			throw new IllegalArgumentException("Can only write Portals");
		}
		final Portal portal = (Portal) obj;
		final Element element = createElement(document, "portal");
		writeAttribute(element, "world", portal.getDestinationWorld());
		writeIntegerAttribute(element, "row",
				portal.getDestinationCoordinates().getRow());
		writeIntegerAttribute(element, "column",
				portal.getDestinationCoordinates().getCol());
		writeIntegerAttribute(element, "id", portal.getID());
		writeImage(element, portal);
		parent.appendChild(element);
	}
	/**
	 * Write a cave to XML.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @param obj the object to write to the stream. Must be a Cave.
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeCave(final Document document, final Node parent,
								 Object obj) {
		if (!(obj instanceof Cave)) {
			throw new IllegalArgumentException("Can only write Caves");
		}
		final Cave cave = (Cave) obj;
		final Element element = createElement(document, "cave");
		writeIntegerAttribute(element, "dc", cave.getDC());
		writeIntegerAttribute(element, "id", cave.getID());
		writeImage(element, cave);
		parent.appendChild(element);
	}
	/**
	 * Write a battlefield to XML.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @param obj the object to write to the stream. Must be a Battlefield.
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeBattlefield(final Document document, final Node parent,
										Object obj) {
		if (!(obj instanceof Battlefield)) {
			throw new IllegalArgumentException("Can only write Caves");
		}
		final Battlefield field = (Battlefield) obj;
		final Element element = createElement(document, "battlefield");
		writeIntegerAttribute(element, "dc", field.getDC());
		writeIntegerAttribute(element, "id", field.getID());
		writeImage(element, field);
		parent.appendChild(element);
	}
	/**
	 * Write an arbitrary-text note to XML.
	 * @param obj the object to write to the stream. Must be a TextFixture.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeTextFixture(final Document document, final Node parent,
										Object obj) {
		if (!(obj instanceof TextFixture)) {
			throw new IllegalArgumentException("Can only write TextFixture");
		}
		final TextFixture fix = (TextFixture) obj;
		final Element element = createElement(document, "text");
		if (fix.getTurn() != -1) {
			writeIntegerAttribute(element, "turn", fix.getTurn());
		}
		writeImage(element, fix);
		element.appendChild(document.createTextNode(fix.getText().trim()));
		parent.appendChild(element);
	}
}


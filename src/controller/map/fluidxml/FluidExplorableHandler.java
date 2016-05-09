package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.Player;
import model.map.PointFactory;
import model.map.fixtures.TextFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.Portal;
import util.EqualsAny;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.imageXML;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.writeTag;
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
public class FluidExplorableHandler {
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
	public static final AdventureFixture readAdventure(final StartElement element,
												final Iterable<XMLEvent> stream,
												final IMutablePlayerCollection players,
												final Warning warner,
												final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "adventure");
		final Player player;
		if (XMLHelper.hasAttribute(element, "owner")) {
			player = players.getPlayer(getIntegerAttribute(element, "owner"));
		} else {
			player = players.getIndependent();
		}
		final AdventureFixture retval =
				new AdventureFixture(player, getAttribute(element, "brief", ""),
											getAttribute(element, "full", ""),
											getOrGenerateID(element, warner, idFactory));
		retval.setImage(getAttribute(element, "image", ""));
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
	public static final Portal readPortal(final StartElement element,
										  final Iterable<XMLEvent> stream,
										  final IMutablePlayerCollection players,
										  final Warning warner,
										  final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "portal");
		final Portal retval = new Portal(getAttribute(element, "world"),
												PointFactory.point(getIntegerAttribute(
														element, "row"),
														getIntegerAttribute(element,
																"column")),
												getOrGenerateID(element, warner,
														idFactory));
		retval.setImage(getAttribute(element, "image", ""));
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
	public static final Cave readCave(final StartElement element,
									  final Iterable<XMLEvent> stream,
									  final IMutablePlayerCollection players,
									  final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "cave");
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final Cave retval = new Cave(getIntegerAttribute(element, "dc"), idNum);
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getAttribute(element, "image", ""));
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
	public static final Battlefield readBattlefield(final StartElement element,
													final Iterable<XMLEvent> stream,
													final IMutablePlayerCollection
															players,
													final Warning warner,
													final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "battlefield");
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final Battlefield retval =
				new Battlefield(getIntegerAttribute(element, "dc"), idNum);
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getAttribute(element, "image", ""));
		return retval;
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
	public static final TextFixture readTextFixture(final StartElement element,
							 final Iterable<XMLEvent> stream,
							 final IMutablePlayerCollection players,
							 final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "text");
		// Of all our uses of StringBuilder, here we can't know how much size
		// we're going to need beforehand. But cases where we'll need more than
		// 2K will be vanishingly rare in practice.
		final StringBuilder sbuild = new StringBuilder(2048); // NOPMD
		for (final XMLEvent event : stream) {
			if (event.isStartElement() && EqualsAny.equalsAny(
					assertNotNull(event.asStartElement().getName().getNamespaceURI()),
					ISPReader.NAMESPACE, XMLConstants.NULL_NS_URI)) {
				throw new UnwantedChildException(assertNotNull(element.getName()),
														assertNotNull(event.asStartElement()));
			} else if (event.isCharacters()) {
				sbuild.append(event.asCharacters().getData());
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		final TextFixture fix =
				new TextFixture(assertNotNull(sbuild.toString().trim()),
									   getIntegerAttribute(element, "turn", -1));
		fix.setImage(getAttribute(element, "image", ""));
		return fix;
	}
	/**
	 * Write an adventure hook to XML.
	 * @param ostream the stream to write to
	 * @param obj the object to write to the stream. Must be an AdventureFixture.
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static final void writeAdventure(final Appendable ostream,
											final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof AdventureFixture)) {
			throw new IllegalArgumentException("Can only write AdventureFixtures");
		}
		final AdventureFixture adv = (AdventureFixture) obj;
		writeTag(ostream, "adventure", indent);
		writeIntegerAttribute(ostream, "id", adv.getID());
		if (!adv.getOwner().isIndependent()) {
			writeIntegerAttribute(ostream, "owner", adv.getOwner().getPlayerId());
		}
		writeNonEmptyAttribute(ostream, "brief", adv.getBriefDescription());
		writeNonEmptyAttribute(ostream, "full", adv.getFullDescription());
		ostream.append(imageXML(adv));
		ostream.append(" />\n");
	}
	/**
	 * Write a portal to XML.
	 * @param ostream the stream to write to
	 * @param obj the object to write to the stream. Must be a Portal.
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static final void writePortal(final Appendable ostream,
											final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof Portal)) {
			throw new IllegalArgumentException("Can only write Portals");
		}
		final Portal portal = (Portal) obj;
		writeTag(ostream, "portal", indent);
		writeAttribute(ostream, "world", portal.getDestinationWorld());
		writeIntegerAttribute(ostream, "row", portal.getDestinationCoordinates().row);
		writeIntegerAttribute(ostream, "column", portal.getDestinationCoordinates().col);
		writeIntegerAttribute(ostream, "id", portal.getID());
		ostream.append(imageXML(portal));
		ostream.append(" />\n");
	}
	/**
	 * Write a cave to XML.
	 * @param ostream the stream to write to
	 * @param obj the object to write to the stream. Must be a Cave.
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static final void writeCave(final Appendable ostream,
											final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof Cave)) {
			throw new IllegalArgumentException("Can only write Caves");
		}
		final Cave cave = (Cave) obj;
		writeTag(ostream, "cave", indent);
		writeIntegerAttribute(ostream, "dc", cave.getDC());
		writeIntegerAttribute(ostream, "id", cave.getID());
		ostream.append(imageXML(cave));
		ostream.append(" />\n");
	}
	/**
	 * Write a battlefield to XML.
	 * @param ostream the stream to write to
	 * @param obj the object to write to the stream. Must be a Battlefield.
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static final void writeBattlefield(final Appendable ostream,
									   final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof Battlefield)) {
			throw new IllegalArgumentException("Can only write Caves");
		}
		final Battlefield field = (Battlefield) obj;
		writeTag(ostream, "battlefield", indent);
		writeIntegerAttribute(ostream, "dc", field.getDC());
		writeIntegerAttribute(ostream, "id", field.getID());
		ostream.append(imageXML(field));
		ostream.append(" />\n");
	}
	/**
	 * Write an arbitrary-text note to XML.
	 * @param ostream the stream to write to
	 * @param obj the object to write to the stream. Must be a TextFixture.
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeTextFixture(final Appendable ostream, final Object obj, final int indent) throws IOException {
		if (!(obj instanceof TextFixture)) {
			throw new IllegalArgumentException("Can only write TextFixture");
		}
		final TextFixture fix = (TextFixture) obj;
		writeTag(ostream, "text", indent);
		if (fix.getTurn() != -1) {
			writeIntegerAttribute(ostream, "turn", fix.getTurn());
		}
		ostream.append(imageXML(fix));
		ostream.append('>');
		ostream.append(fix.getText().trim());
		ostream.append("</text>\n");
	}
}


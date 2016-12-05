package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.getTextUntil;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
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
 * Foundation; see COPYING or
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
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed adventure
	 * @throws SPFormatException on SP format problems
	 */
	public static AdventureFixture readAdventure(final StartElement element,
												 final QName parent,
												 final Iterable<XMLEvent> stream,
												 final IPlayerCollection players,
												 final Warning warner,
												 final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "adventure");
		final Player player;
		if (XMLHelper.hasAttribute(element, "owner")) {
			player = players.getPlayer(getIntegerAttribute(element, "owner"));
		} else {
			player = players.getIndependent();
		}
		final AdventureFixture retval =
				setImage(new AdventureFixture(player, getAttribute(element, "brief", ""),
													 getAttribute(element, "full", ""),
													 getOrGenerateID(element, warner,
															 idFactory)), element,
						warner);
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return retval;
	}

	/**
	 * Read a portal from XML.
	 *
	 * @param element   The XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed portal
	 * @throws SPFormatException on SP format problems
	 */
	@SuppressWarnings("UnusedParameters")
	public static Portal readPortal(final StartElement element,
									final QName parent,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner,
									final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "portal");
		final Portal retval = setImage(new Portal(getAttribute(element, "world"),
														 PointFactory
																 .point
																		  (getIntegerAttribute(
																		 element, "row"),
																		 getIntegerAttribute(
																				 element,
																				 "column")),
														 getOrGenerateID(element, warner,
																 idFactory)), element,
				warner);
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return retval;
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed resource
	 * @throws SPFormatException on SP format problems
	 */
	@SuppressWarnings("UnusedParameters")
	public static Cave readCave(final StartElement element,
								final QName parent,
								final Iterable<XMLEvent> stream,
								final IMutablePlayerCollection players,
								final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "cave");
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final Cave retval = new Cave(getIntegerAttribute(element, "dc"), idNum);
		spinUntilEnd(element.getName(), stream);
		return setImage(retval, element, warner);
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed resource
	 * @throws SPFormatException on SP format problems
	 */
	@SuppressWarnings("UnusedParameters")
	public static Battlefield readBattlefield(final StartElement element,
											  final QName parent,
											  final Iterable<XMLEvent> stream,
											  final IMutablePlayerCollection
													  players,
											  final Warning warner,
											  final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "battlefield");
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
	 * @param parent    the parent tag
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
											  final QName parent,
											  final Iterable<XMLEvent> stream,
											  final IMutablePlayerCollection players,
											  final Warning warner,
											  final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "text");
		return setImage(new TextFixture(getTextUntil(element.getName(), stream),
											   getIntegerAttribute(element, "turn", -1)),
				element, warner);
	}

	/**
	 * Write an adventure hook to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeAdventure(final XMLStreamWriter ostream, final Object obj,
									  final int indent) throws XMLStreamException {
		// TODO: Create helper method for this idiom, so we don't have to sacrifice
		// one coverage-miss line per method.
		if (!(obj instanceof AdventureFixture)) {
			throw new IllegalArgumentException("Can only write AdventureFixtures");
		}
		final AdventureFixture adv = (AdventureFixture) obj;
		writeTag(ostream, "adventure", indent, true);
		writeIntegerAttribute(ostream, "id", adv.getID());
		if (!adv.getOwner().isIndependent()) {
			writeIntegerAttribute(ostream, "owner", adv.getOwner().getPlayerId());
		}
		writeNonEmptyAttribute(ostream, "brief", adv.getBriefDescription());
		writeNonEmptyAttribute(ostream, "full", adv.getFullDescription());
		writeImage(ostream, adv);
	}

	/**
	 * Write a portal to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writePortal(final XMLStreamWriter ostream, final Object obj,
								   final int indent) throws XMLStreamException {
		if (!(obj instanceof Portal)) {
			throw new IllegalArgumentException("Can only write Portals");
		}
		final Portal portal = (Portal) obj;
		writeTag(ostream, "portal", indent, true);
		writeAttribute(ostream, "world", portal.getDestinationWorld());
		writeIntegerAttribute(ostream, "row",
				portal.getDestinationCoordinates().getRow());
		writeIntegerAttribute(ostream, "column",
				portal.getDestinationCoordinates().getCol());
		writeIntegerAttribute(ostream, "id", portal.getID());
		writeImage(ostream, portal);
	}

	/**
	 * Write a cave to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeCave(final XMLStreamWriter ostream, final Object obj,
								 final int indent) throws XMLStreamException {
		if (!(obj instanceof Cave)) {
			throw new IllegalArgumentException("Can only write Caves");
		}
		final Cave cave = (Cave) obj;
		writeTag(ostream, "cave", indent, true);
		writeIntegerAttribute(ostream, "dc", cave.getDC());
		writeIntegerAttribute(ostream, "id", cave.getID());
		writeImage(ostream, cave);
	}

	/**
	 * Write a battlefield to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeBattlefield(final XMLStreamWriter ostream, final Object obj,
										final int indent) throws XMLStreamException {
		if (!(obj instanceof Battlefield)) {
			throw new IllegalArgumentException("Can only write Caves");
		}
		final Battlefield field = (Battlefield) obj;
		writeTag(ostream, "battlefield", indent, true);
		writeIntegerAttribute(ostream, "dc", field.getDC());
		writeIntegerAttribute(ostream, "id", field.getID());
		writeImage(ostream, field);
	}

	/**
	 * Write an arbitrary-text note to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeTextFixture(final XMLStreamWriter ostream, final Object obj,
										final int indent) throws XMLStreamException {
		if (!(obj instanceof TextFixture)) {
			throw new IllegalArgumentException("Can only write TextFixture");
		}
		final TextFixture fix = (TextFixture) obj;
		writeTag(ostream, "text", indent, false);
		if (fix.getTurn() != -1) {
			writeIntegerAttribute(ostream, "turn", fix.getTurn());
		}
		writeImage(ostream, fix);
		ostream.writeCharacters(fix.getText().trim());
		ostream.writeEndElement();
	}
}


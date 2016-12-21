package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.util.Random;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IPlayerCollection;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.Village;
import model.workermgmt.RaceFactory;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.getPlayerOrIndependent;
import static controller.map.fluidxml.XMLHelper.requireNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.writeTag;
import static model.map.fixtures.towns.TownStatus.parseTownStatus;

/**
 * A class to hold XML I/O for towns, other than fortresses.
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
 * @deprecated FluidXML is deprecated in favor of YAXML
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
@Deprecated
public final class FluidTownHandler {
	/**
	 * Do not instantiate.
	 */
	private FluidTownHandler() {
		// Do not instantiate
	}

	/**
	 * Parse a town.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players in the map
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	public static Town readTown(final StartElement element,
								final QName parent,
								final Iterable<XMLEvent> stream,
								final IPlayerCollection players,
								final Warning warner,
								final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "town");
		requireNonEmptyAttribute(element, "name", false, warner);
		spinUntilEnd(element.getName(), stream);
		final Town fix =
				new Town(parseTownStatus(getAttribute(element, "status")),
								TownSize.parseTownSize(getAttribute(element, "size")),
								getIntegerAttribute(element, "dc"),
								getAttribute(element, "name", ""),
								getOrGenerateID(element, warner, idFactory),
								getPlayerOrIndependent(element, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		return setImage(fix, element, warner);
	}

	/**
	 * Parse a fortification.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players in the map
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed fortification
	 * @throws SPFormatException on SP format problems
	 */
	public static Fortification readFortification(final StartElement element,
												  final QName parent,
												  final Iterable<XMLEvent> stream,
												  final IPlayerCollection players,
												  final Warning warner,
												  final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "fortification");
		requireNonEmptyAttribute(element, "name", false, warner);
		spinUntilEnd(element.getName(), stream);
		final Fortification fix =
				new Fortification(parseTownStatus(getAttribute(element, "status")),
										 TownSize.parseTownSize(
												 getAttribute(element, "size")),
										 getIntegerAttribute(element, "dc"),
										 getAttribute(element, "name", ""),
										 getOrGenerateID(element, warner, idFactory),
										 getPlayerOrIndependent(element, warner,
												 players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		return setImage(fix, element, warner);
	}

	/**
	 * Parse a city.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players in the map
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed city
	 * @throws SPFormatException on SP format problems
	 */
	public static City readCity(final StartElement element,
								final QName parent,
								final Iterable<XMLEvent> stream,
								final IPlayerCollection players,
								final Warning warner,
								final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "city");
		requireNonEmptyAttribute(element, "name", false, warner);
		spinUntilEnd(element.getName(), stream);
		final City fix =
				new City(parseTownStatus(getAttribute(element, "status")),
								TownSize.parseTownSize(getAttribute(element, "size")),
								getIntegerAttribute(element, "dc"),
								getAttribute(element, "name", ""),
								getOrGenerateID(element, warner, idFactory),
								getPlayerOrIndependent(element, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		return setImage(fix, element, warner);
	}

	/**
	 * Parse a village.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players in the map
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed village
	 * @throws SPFormatException on SP format problems
	 */
	public static Village readVillage(final StartElement element,
									  final QName parent,
									  final Iterable<XMLEvent> stream,
									  final IPlayerCollection players,
									  final Warning warner,
									  final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "village");
		requireNonEmptyAttribute(element, "name", false, warner);
		spinUntilEnd(element.getName(), stream);
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final Village retval =
				new Village(parseTownStatus(getAttribute(element, "status")),
								   getAttribute(element, "name", ""), idNum,
								   getPlayerOrIndependent(element, warner, players),
								   getAttribute(element, "race",
										   RaceFactory.getRace(new Random(idNum))));
		retval.setPortrait(getAttribute(element, "portrait", ""));
		return setImage(retval, element, warner);
	}

	/**
	 * Write a Village to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeVillage(final XMLStreamWriter ostream, final Object obj,
									final int indent) throws XMLStreamException {
		if (!(obj instanceof Village)) {
			throw new IllegalArgumentException("Can only write Village");
		}
		final Village fix = (Village) obj;
		writeTag(ostream, "village", indent, true);
		writeAttribute(ostream, "status", fix.status().toString());
		writeNonEmptyAttribute(ostream, "name", fix.getName());
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeIntegerAttribute(ostream, "owner", fix.getOwner().getPlayerId());
		writeAttribute(ostream, "race", fix.getRace());
		writeImage(ostream, fix);
		writeNonEmptyAttribute(ostream, "portrait", fix.getPortrait());
	}

	/**
	 * Write an AbstractTown to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeTown(final XMLStreamWriter ostream, final Object obj,
								 final int indent) throws XMLStreamException {
		if (!(obj instanceof AbstractTown)) {
			throw new IllegalArgumentException("Can only write AbstractTown");
		}
		final AbstractTown fix = (AbstractTown) obj;
		writeTag(ostream, fix.kind(), indent, true);
		writeAttribute(ostream, "status", fix.status().toString());
		writeAttribute(ostream, "size", fix.size().toString());
		writeIntegerAttribute(ostream, "dc", fix.getDC());
		writeNonEmptyAttribute(ostream, "name", fix.getName());
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeIntegerAttribute(ostream, "owner", fix.getOwner().getPlayerId());
		writeImage(ostream, fix);
		writeNonEmptyAttribute(ostream, "portrait", fix.getPortrait());
	}
}

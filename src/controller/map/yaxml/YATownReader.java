package controller.map.yaxml;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IPlayerCollection;
import model.map.Player;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import model.workermgmt.RaceFactory;
import util.EqualsAny;
import util.LineEnd;
import util.TypesafeLogger;
import util.Warning;

/**
 * A reader for fortresses, villages, and other towns.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class YATownReader extends YAAbstractReader<ITownFixture> {
	/**
	 * The Warning instance to use.
	 */
	private final Warning warner;
	/**
	 * The map's growing collection of players.
	 */
	private final IPlayerCollection players;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
												 .getLogger(YATownReader.class);
	/**
	 * The "owner" parameter.
	 */
	private static final String OWNER_PARAM = "owner";
	/**
	 * The 'name' parameter.
	 */
	private static final String NAME_PARAM = "name";
	/**
	 * The unit reader. TODO: Use a Collection of readers instead of individual fields.
	 */
	private final YAReader<IUnit> unitReader;
	/**
	 * The reader for resource piles.
	 */
	private final YAReader<ResourcePile> rpReader;
	/**
	 * The reader for Implements.
	 */
	private final YAReader<Implement> implReader;
	/**
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 * @param playerCollection the map's collection of players
	 */
	public YATownReader(final Warning warning, final IDRegistrar idRegistrar,
						 final IPlayerCollection playerCollection) {
		super(warning, idRegistrar);
		warner = warning;
		players = playerCollection;
		unitReader = new YAUnitReader(warning, idRegistrar, playerCollection);
		rpReader = new YAResourcePileReader(warning, idRegistrar);
		implReader = new YAImplementReader(warning, idRegistrar);
	}

	/**
	 * Parse a village.
	 *
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @return the parsed village
	 * @throws SPFormatException on SP format problems
	 */
	private ITownFixture parseVillage(final StartElement element,
											 final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireNonEmptyParameter(element, NAME_PARAM, false);
		spinUntilEnd(element.getName(), stream);
		final int idNum = getOrGenerateID(element);
		final Village retval = new Village(TownStatus.parseTownStatus(
				getParameter(element, "status")), getParameter(element, NAME_PARAM, ""),
												  idNum, getOwnerOrIndependent(element),
												  getParameter(element, "race",
				RaceFactory.getRace(new Random(idNum))));
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		return retval;
	}

	/**
	 * Parse a town, city, or fortification.
	 *
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	private ITownFixture parseTown(final StartElement element,
										  final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireNonEmptyParameter(element, NAME_PARAM, false);
		final String name = getParameter(element, NAME_PARAM, "");
		final TownStatus status = TownStatus.parseTownStatus(getParameter(
				element, "status"));
		final TownSize size = TownSize.parseTownSize(getParameter(element,
				"size"));
		final int dc = getIntegerParameter(element, "dc");
		final int id = getOrGenerateID(element);
		final Player owner = getOwnerOrIndependent(element);
		final AbstractTown retval;
		if ("town".equals(element.getName().getLocalPart())) {
			retval = new Town(status, size, dc, name, id, owner);
		} else if ("city".equals(element.getName().getLocalPart())) {
			retval = new City(status, size, dc, name, id, owner);
		} else {
			retval = new Fortification(status, size, dc, name, id, owner);
		}
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		return retval;
	}

	/**
	 * If the tag has an "owner" parameter, return the player it indicates; otherwise,
	 * trigger a warning and return the "independent" player.
	 *
	 * @param element the tag being parsed
	 * @return the indicated player, or the independent player if none
	 * @throws SPFormatException on SP format error reading the parameter.
	 */
	private Player getOwnerOrIndependent(final StartElement element)
			throws SPFormatException {
		final Player retval;
		if (hasParameter(element, OWNER_PARAM)) {
			retval = players.getPlayer(getIntegerParameter(element, OWNER_PARAM));
		} else {
			warner.warn(new MissingPropertyException(element, OWNER_PARAM));
			retval = players.getIndependent();
		}
		return retval;
	}

	/**
	 * Parse a fortress.
	 *
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	private ITownFixture parseFortress(final StartElement element,
											  final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireNonEmptyParameter(element, OWNER_PARAM, false);
		requireNonEmptyParameter(element, NAME_PARAM, false);
		final Fortress retval =
				new Fortress(getOwnerOrIndependent(element),
									getParameter(element, NAME_PARAM, ""),
									getOrGenerateID(element),
									TownSize.parseTownSize(
											getParameter(element, "size", "small")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement() && isSupportedNamespace(
							event.asStartElement().getName())) {
				final String memberTag = event.asStartElement().getName()
												 .getLocalPart().toLowerCase();
				switch (memberTag) {
				case "unit":
					retval.addMember(unitReader.read(event.asStartElement(),
							element.getName(), stream));
					break;
				case "implement":
					retval.addMember(implReader.read(event.asStartElement(),
							element.getName(), stream));
					break;
				case "resource":
					retval.addMember(
							rpReader.read(event.asStartElement(), element.getName(),
									stream));
					break;
				default:
					throw new UnwantedChildException(element.getName(),
															event.asStartElement());
				}
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		return retval;
	}

	/**
	 * @param ostream the stream to write to
	 * @param obj     the AbstractTownEvent to write
	 * @param indent  how far to indent the tag
	 * @throws IOException on I/O error
	 */
	private static void writeAbstractTown(final Appendable ostream,
										  final AbstractTown obj, final int indent)
			throws
			IOException {
		if (obj instanceof Fortification) {
			writeTag(ostream, "fortification", indent);
		} else if (obj instanceof Town) {
			writeTag(ostream, "town", indent);
		} else if (obj instanceof City) {
			writeTag(ostream, "city", indent);
		} else {
			throw new IllegalStateException("Unknown AbstractTownEvent type");
		}
		writeProperty(ostream, "status", obj.status().toString());
		writeProperty(ostream, "size", obj.size().toString());
		writeProperty(ostream, "dc", Integer.toString(obj.getDC()));
		if (!obj.getName().isEmpty()) {
			writeProperty(ostream, "name", obj.getName());
		}
		writeProperty(ostream, "id", Integer.toString(obj.getID()));
		writeProperty(ostream, "owner", Integer.toString(obj.getOwner().getPlayerId()));
		writeImageXML(ostream, obj);
		writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
		closeLeafTag(ostream);
	}

	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return EqualsAny.equalsAny(tag, "village", "fortress", "town", "city",
				"fortification");
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from     @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public ITownFixture read(final StartElement element,
							 final QName parent,
							 final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "village", "fortress", "town", "city",
				"fortification");
		final ITownFixture retval;
		if ("village".equals(element.getName().getLocalPart())) {
			retval = parseVillage(element, stream);
		} else if ("fortress".equals(element.getName().getLocalPart())) {
			retval = parseFortress(element, stream);
		} else {
			retval = parseTown(element, stream);
		}
		return retval;
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final ITownFixture obj,
					  final int indent) throws IOException {
		if (obj instanceof AbstractTown) {
			writeAbstractTown(ostream, (AbstractTown) obj, indent);
		} else if (obj instanceof Village) {
			writeTag(ostream, "village", indent);
			writeProperty(ostream, "status", obj.status().toString());
			if (!obj.getName().isEmpty()) {
				writeProperty(ostream, "name", obj.getName());
			}
			writeProperty(ostream, "id", Integer.toString(obj.getID()));
			writeProperty(ostream, "owner",
					Integer.toString(obj.getOwner().getPlayerId()));
			final Village village = (Village) obj;
			writeProperty(ostream, "race", village.getRace());
			writeImageXML(ostream, (Village) obj);
			writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
			closeLeafTag(ostream);
		} else if (obj instanceof Fortress) {
			writeTag(ostream, "fortress", indent);
			writeProperty(ostream, "owner",
					Integer.toString(obj.getOwner().getPlayerId()));
			writeNonemptyProperty(ostream, "name", obj.getName());
			if (TownSize.Small != obj.size()) {
				writeProperty(ostream, "size", obj.size().toString());
			}
			writeProperty(ostream, "id", Integer.toString(obj.getID()));
			final Fortress fortress = (Fortress) obj;
			writeImageXML(ostream, (Fortress) obj);
			writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
			ostream.append('>');
			if (fortress.iterator().hasNext()) {
				ostream.append(LineEnd.LINE_SEP);
				for (final FortressMember unit : fortress) {
					if (unit instanceof Unit) {
						unitReader.write(ostream, (Unit) unit,
								indent + 1);
					} else if (unit instanceof Implement) {
						implReader.write(ostream, (Implement) unit, indent + 1);
					} else if (unit instanceof ResourcePile) {
						rpReader.write(ostream,
								(ResourcePile) unit, indent + 1);
					} else {
						LOGGER.severe("Unhandled FortressMember class "
											  + unit.getClass().getName());
					}
				}
				indent(ostream, indent);
			}
			ostream.append("</fortress>");
			ostream.append(LineEnd.LINE_SEP);
		} else {
			throw new IllegalStateException("Unexpected TownFixture type");
		}
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof ITownFixture;
	}
}

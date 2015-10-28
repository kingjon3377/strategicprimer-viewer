package controller.map.cxml;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import model.map.IEvent;
import model.map.IMutablePlayerCollection;
import model.map.IPlayerCollection;
import model.map.Player;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
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
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for tiles, including rivers.
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
 *
 */
public final class CompactTownReader extends AbstractCompactReader<ITownFixture> {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(CompactTownReader.class.getName());
	/**
	 * Singleton object.
	 */
	public static final CompactTownReader READER = new CompactTownReader();

	/**
	 * The "owner" parameter.
	 */
	private static final String OWNER_PARAM = "owner";
	/**
	 * The 'name' parameter.
	 */
	private static final String NAME_PARAM = "name";

	/**
	 * Singleton.
	 */
	private CompactTownReader() {
		// Singleton.
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
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public ITownFixture read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "village", "fortress", "town", "city",
				"fortification");
		// ESCA-JAVA0177:
		final ITownFixture retval; // NOPMD
		if ("village".equals(element.getName().getLocalPart())) {
			retval = parseVillage(element, stream, players, warner, idFactory);
		} else if ("fortress".equals(element.getName().getLocalPart())) {
			retval = parseFortress(element, stream, players, warner, idFactory);
		} else {
			retval = parseTown(element, stream, players, warner, idFactory);
		}
		return retval;
	}

	/**
	 * Parse a village.
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players in the map
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed village
	 * @throws SPFormatException on SP format problems
	 */
	private static Village parseVillage(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IPlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final Village retval = new Village(
				TownStatus.parseTownStatus(getParameter(element, "status")),
				getParameter(element, NAME_PARAM, ""), idNum,
				getOwnerOrIndependent(element, warner, players),
				getParameter(element, "race",
						RaceFactory.getRace(new Random(idNum))));
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	/**
	 * Parse a town, city, or fortification.
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players in the map
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	private static AbstractTown parseTown(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IPlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		final String name = getParameter(element, NAME_PARAM, "");
		final TownStatus status = TownStatus.parseTownStatus(getParameter(
				element, "status"));
		final TownSize size = TownSize.parseTownSize(getParameter(element,
				"size"));
		final int dc = getIntegerParameter(element, "dc"); // NOPMD
		final int id = getOrGenerateID(element, warner, idFactory); // NOPMD
		final Player owner = getOwnerOrIndependent(element, warner, players); //NOPMD
		final AbstractTown retval; // NOPMD
		if ("town".equals(element.getName().getLocalPart())) {
			retval = new Town(status, size, dc, name, id, owner);
		} else if ("city".equals(element.getName().getLocalPart())) {
			retval = new City(status, size, dc, name, id, owner);
		} else {
			retval = new Fortification(status, size, dc, name, id, owner);
		}
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	/**
	 * If the tag has an "owner" parameter, return the player it indicates;
	 * otherwise, trigger a warning and return the "independent" player.
	 *
	 * @param element the tag being parsed
	 * @param warner the Warning instance to send the warning on
	 * @param players the collection of players to refer to
	 * @return the indicated player, or the independent player if none
	 * @throws SPFormatException on SP format error reading the parameter.
	 */
	private static Player getOwnerOrIndependent(final StartElement element,
			final Warning warner, final IPlayerCollection players)
			throws SPFormatException {
		// ESCA-JAVA0177:
		final Player retval; // NOPMD
		if (hasParameter(element, OWNER_PARAM)) {
			retval = players
					.getPlayer(getIntegerParameter(element, OWNER_PARAM));
		} else {
			warner.warn(new MissingPropertyException(NullCleaner
					.assertNotNull(element.getName().getLocalPart()),
					OWNER_PARAM, element.getLocation().getLineNumber()));
			retval = players.getIndependent();
		}
		return retval;
	}

	/**
	 * Parse a fortress.
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	private static Fortress parseFortress(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, OWNER_PARAM, false, warner);
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		final Fortress retval = new Fortress(getOwnerOrIndependent(element,
				warner, players), getParameter(element, NAME_PARAM, ""),
				getOrGenerateID(element, warner, idFactory));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				String memberTag = event.asStartElement().getName().getLocalPart().toLowerCase();
				if ("unit".equals(memberTag)) {
					retval.addMember(CompactUnitReader.READER.read(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream, players, warner, idFactory));
				} else if ("implement".equals(memberTag)) {
					retval.addMember(CompactImplementReader.READER.read(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream, players, warner, idFactory));
				} else if ("resource".equals(memberTag)) {
					retval.addMember(CompactResourcePileReader.READER.read(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(
							NullCleaner.assertNotNull(element.getName()
									.getLocalPart()),
							NullCleaner.assertNotNull(event.asStartElement()
									.getName().getLocalPart()), event.getLocation()
									.getLineNumber());
				}
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final ITownFixture obj,
			final int indent) throws IOException {
		ostream.append(indent(indent));
		if (obj instanceof AbstractTown) {
			writeAbstractTown(ostream, (AbstractTown) obj);
		} else if (obj instanceof Village) {
			ostream.append("<village status=\"");
			ostream.append(((Village) obj).status().toString());
			if (!obj.getName().isEmpty()) {
				ostream.append("\" name=\"");
				ostream.append(obj.getName());
			}
			ostream.append("\" id=\"");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append("\" owner=\"");
			ostream.append(Integer.toString(obj.getOwner().getPlayerId()));
			ostream.append("\" race=\"");
			ostream.append(((Village) obj).getRace());
			ostream.append("\" ").append(imageXML((Village) obj)).append("/>\n");
		} else if (obj instanceof Fortress) {
			ostream.append("<fortress owner=\"");
			ostream.append(Integer.toString(obj.getOwner().getPlayerId()));
			if (!obj.getName().isEmpty()) {
				ostream.append("\" name=\"");
				ostream.append(obj.getName());
			}
			ostream.append("\" id=\"");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append('"').append(imageXML((Fortress) obj)).append('>');
			if (((Fortress) obj).iterator().hasNext()) {
				ostream.append('\n');
				for (final FortressMember unit : (Fortress) obj) {
					if (unit instanceof Unit) {
						CompactUnitReader.READER.write(ostream, (Unit) unit,
								indent + 1);
					} else if (unit instanceof Implement) {
						CompactImplementReader.READER.write(ostream, (Implement) unit, indent);
					} else if (unit instanceof ResourcePile) {
						CompactResourcePileReader.READER.write(ostream, (ResourcePile) unit, indent);
					} else {
						LOGGER.severe("Unhandled FortressMember class " + unit.getClass().getName());
					}
				}
				ostream.append(indent(indent));
			}
			ostream.append("</fortress>\n");
		} else {
			throw new IllegalStateException("Unexpected TownFixture type");
		}
	}

	/**
	 * @param ostream the stream to write to
	 * @param obj the AbstractTownEvent to write
	 * @throws IOException on I/O error
	 */
	private static void writeAbstractTown(final Appendable ostream,
			final AbstractTown obj) throws IOException {
		if (obj instanceof Fortification) {
			ostream.append("<fortification ");
		} else if (obj instanceof Town) {
			ostream.append("<town ");
		} else if (obj instanceof City) {
			ostream.append("<city ");
		} else {
			throw new IllegalStateException("Unknown AbstractTownEvent type");
		}
		ostream.append("status=\"");
		ostream.append(obj.status().toString());
		ostream.append("\" size=\"");
		ostream.append(obj.size().toString());
		ostream.append("\" dc=\"");
		ostream.append(Integer.toString(((IEvent) obj).getDC()));
		if (!obj.getName().isEmpty()) {
			ostream.append("\" name=\"");
			ostream.append(obj.getName());
		}
		ostream.append("\" id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append("\" owner=\"");
		ostream.append(Integer.toString(obj.getOwner().getPlayerId()));
		ostream.append('"').append(imageXML(obj)).append(" />\n");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactTownReader";
	}
}

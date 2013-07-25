package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IEvent;
import model.map.Player;
import model.map.PlayerCollection;
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
import util.EqualsAny;
import util.IteratorWrapper;
import util.Warning;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTownReader extends AbstractCompactReader implements CompactReader<ITownFixture> {
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
	 * Singleton object.
	 */
	public static final CompactTownReader READER = new CompactTownReader();
	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return EqualsAny.equalsAny(tag, "village", "fortress", "town", "city", "fortification");
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
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "village", "fortress", "town", "city", "fortification");
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
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players in the map
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed village
	 * @throws SPFormatException on SP format problems
	 */
	private static Village parseVillage(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		spinUntilEnd(element.getName(), stream);
		return new Village(TownStatus.parseTownStatus(getParameter(element,
				"status")), getParameter(element, NAME_PARAM, ""), getOrGenerateID(
				element, warner, idFactory), getOwnerOrIndependent(element, warner, players));
	}
	/**
	 * Parse a town, city, or fortification.
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players in the map
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	private static AbstractTown parseTown(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		final String name = getParameter(element, NAME_PARAM, "");
		final TownStatus status = TownStatus.parseTownStatus(getParameter(element, "status"));
		final TownSize size = TownSize.parseTownSize(getParameter(element, "size"));
		final int dc = Integer.parseInt(getParameter(element, "dc")); // NOPMD
		final int id = getOrGenerateID(element, warner, idFactory); // NOPMD
		final Player owner = getOwnerOrIndependent(element, warner, players); // NOPMD
		final AbstractTown retval; // NOPMD
		if ("town".equals(element.getName().getLocalPart())) {
			retval = new Town(status, size, dc, name, id, owner);
		} else if ("city".equals(element.getName().getLocalPart())) {
			retval = new City(status, size, dc, name, id, owner);
		} else {
			retval = new Fortification(status, size, dc, name, id, owner);
		}
		spinUntilEnd(element.getName(), stream);
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
			final Warning warner, final PlayerCollection players)
			throws SPFormatException {
		// ESCA-JAVA0177:
		final Player retval; // NOPMD
		if (hasParameter(element, OWNER_PARAM)) {
			retval = players.getPlayer(Integer.parseInt(getParameter(element, OWNER_PARAM)));
		} else {
			warner.warn(new MissingPropertyException(element.getName()
					.getLocalPart(), OWNER_PARAM, element.getLocation()
					.getLineNumber()));
			retval = players.getIndependent();
		}
		return retval;
	}
	/**
	 * Parse a fortress.
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	private static Fortress parseFortress(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, OWNER_PARAM, false, warner);
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		final Fortress retval = new Fortress(getOwnerOrIndependent(element,
				warner, players), getParameter(element, NAME_PARAM, ""),
				getOrGenerateID(element, warner, idFactory));
		for (final XMLEvent event : stream) {
			if (event.isStartElement() && "unit".equalsIgnoreCase(event.asStartElement().getName().getLocalPart())) {
				retval.addUnit(CompactUnitReader.READER.read(
						event.asStartElement(), stream, players, warner,
						idFactory));
			} else if (event.isEndElement() && element.getName().equals(event.asEndElement().getName())) {
				break;
			} else if (event.isStartElement()) {
				throw new UnwantedChildException(element.getName()
						.getLocalPart(), event.asStartElement().getName()
						.getLocalPart(), event.getLocation().getLineNumber());
			}
		}
		return retval;
	}
	/**
	 * Write an object to a stream.
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final ITownFixture obj, final int indent) throws IOException {
		out.append(indent(indent));
		if (obj instanceof AbstractTown) {
			writeAbstractTown(out, (AbstractTown) obj);
		} else if (obj instanceof Village) {
			out.append("<village status=\"");
			out.append(((Village) obj).status().toString());
			if (!obj.getName().isEmpty()) {
				out.append("\" name=\"");
				out.append(obj.getName());
			}
			out.append("\" id=\"");
			out.append(Integer.toString(obj.getID()));
			out.append("\" owner=\"");
			out.append(Integer.toString(obj.getOwner().getPlayerId()));
			out.append("\" />\n");
		} else if (obj instanceof Fortress) {
			out.append("<fortress owner=\"");
			out.append(Integer.toString(obj.getOwner().getPlayerId()));
			if (!obj.getName().isEmpty()) {
				out.append("\" name=\"");
				out.append(obj.getName());
			}
			out.append("\" id=\"");
			out.append(Integer.toString(obj.getID()));
			out.append("\">");
			if (((Fortress) obj).iterator().hasNext()) {
				out.append('\n');
				for (final Unit unit : (Fortress) obj) {
					CompactUnitReader.READER.write(out, unit, indent + 1);
				}
				out.append(indent(indent));
			}
			out.append("</fortress>\n");
		} else {
			throw new IllegalStateException("Unexpected TownFixture type");
		}
	}
	/**
	 * @param out the stream to write to
	 * @param obj the AbstractTownEvent to write
	 * @throws IOException on I/O error
	 */
	private static void writeAbstractTown(final Writer out, final AbstractTown obj)
			throws IOException {
		if (obj instanceof Fortification) {
			out.append("<fortification ");
		} else if (obj instanceof Town) {
			out.append("<town ");
		} else if (obj instanceof City) {
			out.append("<city ");
		} else {
			throw new IllegalStateException("Unknown AbstractTownEvent type");
		}
		out.append("status=\"");
		out.append(obj.status().toString());
		out.append("\" size=\"");
		out.append(obj.size().toString());
		out.append("\" dc=\"");
		out.append(Integer.toString(((IEvent) obj).getDC()));
		if (!obj.getName().isEmpty()) {
			out.append("\" name=\"");
			out.append(obj.getName());
		}
		out.append("\" id=\"");
		out.append(Integer.toString(obj.getID()));
		out.append("\" owner=\"");
		out.append(Integer.toString(obj.getOwner().getPlayerId()));
		out.append("\" />\n");
	}
}

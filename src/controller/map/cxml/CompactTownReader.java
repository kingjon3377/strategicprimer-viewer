package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IEvent;
import model.map.PlayerCollection;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.AbstractTownEvent;
import model.map.fixtures.towns.CityEvent;
import model.map.fixtures.towns.FortificationEvent;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.TownEvent;
import model.map.fixtures.towns.TownFixture;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import util.EqualsAny;
import util.IteratorWrapper;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTownReader extends AbstractCompactReader implements CompactReader<TownFixture> {
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
	public TownFixture read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "village", "fortress", "town", "city", "fortification");
		// ESCA-JAVA0177:
		final TownFixture retval; // NOPMD
		if ("village".equals(element.getName().getLocalPart())) {
			retval = parseVillage(element, stream, warner, idFactory);
		} else if ("fortress".equals(element.getName().getLocalPart())) {
			retval = parseFortress(element, stream, players, warner, idFactory);
		} else {
			retval = parseTown(element, stream, warner, idFactory);
		}
		return retval;
	}
	/**
	 * Parse a village.
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed village
	 * @throws SPFormatException on SP format problems
	 */
	private Village parseVillage(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		spinUntilEnd(element.getName(), stream);
		return new Village(TownStatus.parseTownStatus(getParameter(element,
				"status")), getParameter(element, NAME_PARAM, ""), getOrGenerateID(
				element, warner, idFactory));
	}
	/**
	 * Parse a town, city, or fortification.
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed town
	 * @throws SPFormatException on SP format problems
	 */
	private AbstractTownEvent parseTown(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		final String name = getParameter(element, NAME_PARAM, "");
		final TownStatus status = TownStatus.parseTownStatus(getParameter(element, "status"));
		final TownSize size = TownSize.parseTownSize(getParameter(element, "size"));
		final int dc = Integer.parseInt(getParameter(element, "dc")); // NOPMD
		final int id = getOrGenerateID(element, warner, idFactory); // NOPMD
		final AbstractTownEvent retval; // NOPMD
		if ("town".equals(element.getName().getLocalPart())) {
			retval = new TownEvent(status, size, dc, name, id);
		} else if ("city".equals(element.getName().getLocalPart())) {
			retval = new CityEvent(status, size, dc, name, id);
		} else {
			retval = new FortificationEvent(status, size, dc, name, id);
		}
		spinUntilEnd(element.getName(), stream);
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
	private Fortress parseFortress(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, "owner", false, warner);
		requireNonEmptyParameter(element, NAME_PARAM, false, warner);
		final Fortress retval = new Fortress(players.getPlayer(Integer
				.parseInt(getParameter(element, "owner", "-1"))), getParameter(
				element, NAME_PARAM, ""), getOrGenerateID(element, warner,
				idFactory));
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
	public void write(final Writer out, final TownFixture obj, final int indent) throws IOException {
		out.append(indent(indent));
		if (obj instanceof AbstractTownEvent) {
			writeAbstractTown(out, (AbstractTownEvent) obj);
		} else if (obj instanceof Village) {
			out.append("<village status=\"");
			out.append(((Village) obj).status().toString());
			if (!obj.getName().isEmpty()) {
				out.append("\" name=\"");
				out.append(obj.getName());
			}
			out.append("\" id=\"");
			out.append(Integer.toString(obj.getID()));
			out.append("\" />\n");
		} else if (obj instanceof Fortress) {
			out.append("<fortress owner=\"");
			out.append(Integer.toString(((Fortress) obj).getOwner().getPlayerId()));
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
					CompactReaderAdapter.ADAPTER.write(out, unit, indent + 1);
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
	private static void writeAbstractTown(final Writer out, final AbstractTownEvent obj)
			throws IOException {
		if (obj instanceof FortificationEvent) {
			out.append("<fortification ");
		} else if (obj instanceof TownEvent) {
			out.append("<town ");
		} else if (obj instanceof CityEvent) {
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
		out.append("\" />\n");
	}
}

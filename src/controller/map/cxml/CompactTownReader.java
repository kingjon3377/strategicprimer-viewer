package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
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
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTownReader extends CompactReaderSuperclass implements CompactReader<TownFixture> {
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
			spinUntilEnd(element.getName(), stream);
		} else if ("fortress".equals(element.getName().getLocalPart())) {
			retval = parseFortress(element, stream, players, warner, idFactory);
		} else {
			retval = parseTown(element, stream, warner, idFactory);
			spinUntilEnd(element.getName(), stream);
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
		return new Village(TownStatus.parseTownStatus(getParameter(element,
				"status")), getParameter(element, NAME_PARAM, ""), getOrGenerateID(
				element, warner, idFactory), getFile(stream));
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
		retval.setFile(getFile(stream));
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
				idFactory), getFile(stream));
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
	 * @param file The file we're writing to.
	 * @param inclusion Whether to change files if a sub-object was read from a different file
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final TownFixture obj, final String file,
			final boolean inclusion, final int indent) throws IOException {
		// TODO Auto-generated method stub

	}
}

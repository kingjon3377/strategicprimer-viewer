package controller.map.cxml;

import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Unit;
import util.IteratorWrapper;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactUnitReader extends CompactReaderSuperclass implements CompactReader<Unit> {
	/**
	 * Singleton.
	 */
	private CompactUnitReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactUnitReader READER = new CompactUnitReader();
	/**
	 *
	 * @param <U> the actual type of the object
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public <U extends Unit> U read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "unit");
		requireNonEmptyParameter(element, "name", false, warner);
		requireNonEmptyParameter(element, "owner", false, warner);
		spinUntilEnd(element.getName(), stream);
		return (U) new Unit(
				players.getPlayer(Integer.parseInt(ensureNumeric(getParameter(
						element, "owner", "-1")))), parseKind(element, warner),
				getParameter(element, "name", ""), getOrGenerateID(element,
						warner, idFactory), getFile(stream));
	}
	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the
	 * empty string.
	 *
	 * @param element the current element
	 * @param warner the Warning instance to use
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private static String parseKind(final StartElement element,
			final Warning warner) throws SPFormatException {
		String retval = "";
		try {
			retval = getParameterWithDeprecatedForm(element, // NOPMD
					"kind", "type", warner);
		} catch (final MissingParameterException except) {
			warner.warn(except);
			return ""; // NOPMD
		}
		if (retval.isEmpty()) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "kind", element.getLocation()
					.getLineNumber()));
		}
		return retval;
	}
	/**
	 * @param string a string which should be numeric or empty
	 * @return it, or "-1" if it's empty.
	 */
	private static String ensureNumeric(final String string) {
		return string.isEmpty() ? "-1" : string;
	}
	/**
	 * @param tag a tag
	 * @return whether it's one we can read
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "unit".equalsIgnoreCase(tag);
	}
}

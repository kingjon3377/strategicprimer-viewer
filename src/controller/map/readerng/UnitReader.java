package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Unit;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Units.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class UnitReader implements INodeReader<Unit> {
	/**
	 * The name of the property telling what kind of unit.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Parse a unit.
	 * 
	 * @param element
	 *            the element to start with
	 * @param stream
	 *            the stream to read more elements from
	 * @param players
	 *            the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the fortress
	 * @throws SPFormatException
	 *             on SP format error
	 */
	@Override
	public Unit parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		if (XMLHelper.getAttributeWithDefault(element, "owner", "").isEmpty()) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "owner", element.getLocation()
					.getLineNumber()));
		}
		if (XMLHelper.getAttributeWithDefault(element, "name", "").isEmpty()) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "name", element.getLocation()
					.getLineNumber()));
		}
		// ESCA-JAVA0177:
		long id; // NOPMD
		if (XMLHelper.hasAttribute(element, "id")) {
			id = idFactory.register(
					Long.parseLong(XMLHelper.getAttribute(element, "id")));
		} else {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "id", element.getLocation()
					.getLineNumber()));
			id = idFactory.getID();
		}
		final Unit fix = new Unit(players.getPlayer(Integer.parseInt(ensureNumeric(XMLHelper
				.getAttributeWithDefault(element, "owner", "-1")))),
				parseKind(element, warner),
				XMLHelper.getAttributeWithDefault(element, "name", ""), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}

	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the empty string. 
	 * @param element the current element
	 * @param warner the Warning instance to use
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private static String parseKind(final StartElement element, final Warning warner)
			throws SPFormatException {
		String retval = "";
		try {
			retval = XMLHelper.getAttributeWithDeprecatedForm(element, // NOPMD
					KIND_PROPERTY, "type", warner);
		} catch (final MissingParameterException except) {
			warner.warn(except);
			return ""; // NOPMD
		}
		if (retval.isEmpty()) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), KIND_PROPERTY, element.getLocation()
					.getLineNumber()));
		}
		return retval;
	}
	/**
	 * @param string a string that may be either numeric or empty.
	 * @return it, or "-1" if it's empty.
	 */
	private static String ensureNumeric(final String string) {
		return string.isEmpty() ? "-1" : string;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("unit");
	}
}

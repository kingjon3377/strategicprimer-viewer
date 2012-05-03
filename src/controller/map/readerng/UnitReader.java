package controller.map.readerng;

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
	 * @return the class this produces.
	 */
	@Override
	public Class<Unit> represents() {
		return Unit.class;
	}

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
	 * @return the fortress
	 * @throws SPFormatException
	 *             on SP format error
	 */
	@Override
	public Unit parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		if ("".equals(XMLHelper.getAttributeWithDefault(element, "owner", ""))) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "owner", element.getLocation()
					.getLineNumber()));
		}
		if ("".equals(XMLHelper.getAttributeWithDefault(element, "name", ""))) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "name", element.getLocation()
					.getLineNumber()));
		}
		// ESCA-JAVA0177:
		long id; // NOPMD
		if (XMLHelper.hasAttribute(element, "id")) {
			id = IDFactory.FACTORY.register(
					Long.parseLong(XMLHelper.getAttribute(element, "id")));
		} else {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "id", element.getLocation()
					.getLineNumber()));
			id = IDFactory.FACTORY.getID();
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
		try {
			return XMLHelper.getAttributeWithDeprecatedForm(element, // NOPMD
					KIND_PROPERTY, "type", warner);
		} catch (final MissingParameterException except) {
			warner.warn(except);
			return "";
		}
	}
	/**
	 * @param string a string that may be either numeric or empty.
	 * @return it, or "-1" if it's empty.
	 */
	private static String ensureNumeric(final String string) {
		return "".equals(string) ? "-1" : string;
	}
}

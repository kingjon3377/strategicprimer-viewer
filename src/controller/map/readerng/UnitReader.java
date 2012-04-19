package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Unit;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Units.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class UnitReader implements INodeReader<Unit> {
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
		if (!XMLHelper.hasAttribute(element, "owner") || "".equals(XMLHelper.getAttribute(element, "owner"))) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "owner", element.getLocation()
					.getLineNumber()));
		}
		String kind = "";
		if (XMLHelper.hasAttribute(element, "kind")) {
			kind = XMLHelper.getAttribute(element, "kind");
			if ("".equals(kind)) {
				warner.warn(new MissingParameterException(element.getName()
						.getLocalPart(), "kind", element.getLocation()
						.getLineNumber()));
			}
		} else if (XMLHelper.hasAttribute(element, "type")) {
			kind = XMLHelper.getAttribute(element, "type");
			warner.warn(new DeprecatedPropertyException(element.getName()
					.getLocalPart(), "type", "kind", element.getLocation()
					.getLineNumber()));
			
		} else {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "kind", element.getLocation()
					.getLineNumber()));
		}
		if ("".equals(XMLHelper.getAttributeWithDefault(element, "name", ""))) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "name", element.getLocation()
					.getLineNumber()));
		}
		final Unit fix = new Unit(players.getPlayer(Integer.parseInt(ensureNumeric(XMLHelper
				.getAttributeWithDefault(element, "owner", "-1")))),
				kind,
				XMLHelper.getAttributeWithDefault(element, "name", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("unit", event.asStartElement()
						.getName().getLocalPart(), event.getLocation()
						.getLineNumber());
			} else if (event.isEndElement()
					&& "unit".equals(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}
	/**
	 * @param string a string that may be either numeric or empty.
	 * @return it, or "-1" if it's empty.
	 */
	private static String ensureNumeric(final String string) {
		return "".equals(string) ? "-1" : string;
	}
}

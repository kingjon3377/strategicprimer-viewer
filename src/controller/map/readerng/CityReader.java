package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.CityEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for cities.
 * @author Jonathan Lovelace
 */
public class CityReader implements INodeReader<CityEvent> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<CityEvent> represents() {
		return CityEvent.class;
	}
	/**
	 * Parse a city.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed city
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public CityEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		if (XMLHelper.getAttributeWithDefault(element, "name", "").isEmpty()) {
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
		final CityEvent fix = new CityEvent(
				TownStatus.parseTownStatus(XMLHelper.getAttribute(element,
						"status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")), Integer
						.parseInt(XMLHelper.getAttribute(element, "dc")),
						XMLHelper.getAttributeWithDefault(element, "name", ""), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
}

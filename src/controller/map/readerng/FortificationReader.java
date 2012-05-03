package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.FortificationEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for fortifications.
 * @author Jonathan Lovelace
 *
 */
public class FortificationReader implements INodeReader<FortificationEvent> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<FortificationEvent> represents() {
		return FortificationEvent.class;
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
	public FortificationEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
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
		final FortificationEvent fix = new FortificationEvent(
				TownStatus.parseTownStatus(XMLHelper.getAttribute(element,
						"status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")), Integer
						.parseInt(XMLHelper.getAttribute(element, "dc")),
						XMLHelper.getAttributeWithDefault(element, "name", ""), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
}

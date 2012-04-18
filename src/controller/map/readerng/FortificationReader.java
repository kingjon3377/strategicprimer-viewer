package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.FortificationEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

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
	 * @return the parsed city
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public FortificationEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final FortificationEvent fix = new FortificationEvent(
				TownStatus.parseTownStatus(XMLHelper.getAttribute(element,
						"status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")), Integer
						.parseInt(XMLHelper.getAttribute(element, "dc")),
						XMLHelper.getAttributeWithDefault(element, "name", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("fortifiction", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "fortification".equalsIgnoreCase(event.asEndElement()
							.getName().getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

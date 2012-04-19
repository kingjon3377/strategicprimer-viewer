package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for towns.
 * @author Jonathan Lovelace
 *
 */
public class TownReader implements INodeReader<TownEvent> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<TownEvent> represents() {
		return TownEvent.class;
	}
	/**
	 * Parse a town.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed town
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TownEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final TownEvent fix = new TownEvent(
				TownStatus.parseTownStatus(XMLHelper.getAttribute(element,
						"status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")), Integer
						.parseInt(XMLHelper.getAttribute(element, "dc")),
						XMLHelper.getAttributeWithDefault(element, "name", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException(element.getName()
						.getLocalPart(), event.asStartElement().getName()
						.getLocalPart(), event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "town".equalsIgnoreCase(event.asEndElement()
							.getName().getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

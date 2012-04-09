package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import controller.map.SPFormatException;

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
	 * @return the parsed town
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TownEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final TownEvent fix = new TownEvent(
				TownStatus.parseTownStatus(XMLHelper.getAttribute(element,
						"status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")), Integer
						.parseInt(XMLHelper.getAttribute(element, "dc")),
						XMLHelper.getAttributeWithDefault(element, "name", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Town can't (yet) have child tag",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "town".equalsIgnoreCase(event.asEndElement()
							.getName().getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

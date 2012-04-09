package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Village;
import controller.map.SPFormatException;

/**
 * A reader for Villages.
 * @author Jonathan Lovelace
 *
 */
public class VillageReader implements INodeReader<Village> {
	/**
	 * @return the type this produces.
	 */
	@Override
	public Class<Village> represents() {
		return Village.class;
	}
	/**
	 * Parse a village.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the village represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Village parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Village fix = new Village(TownStatus.parseTownStatus(XMLHelper
				.getAttribute(element, "status")),
				XMLHelper.getAttributeWithDefault(element, "name", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Village can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "village".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

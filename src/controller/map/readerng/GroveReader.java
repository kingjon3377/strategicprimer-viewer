package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Grove;
import util.EqualsAny;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A reader for Groves.
 * @author Jonathan Lovelace
 *
 */
public class GroveReader implements INodeReader<Grove> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Grove> represents() {
		return Grove.class;
	}
	/**
	 * Parse a grove.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the grove represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Grove parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Grove fix = new Grove("orchard".equalsIgnoreCase(element
				.getName().getLocalPart()), Boolean.parseBoolean(XMLHelper
				.getAttribute(element, "wild")), XMLHelper.getAttribute(
				element, "kind"));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException(element.getName()
						.getLocalPart(), event.asStartElement().getName()
						.getLocalPart(), event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& EqualsAny.equalsAny(event.asEndElement().getName()
							.getLocalPart(), "grove", "orchard")) {
				break;
			}
		}
		return fix;
	}
}

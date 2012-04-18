package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Phoenix;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Phoenixes.
 * @author Jonathan Lovelace
 *
 */
public class PhoenixReader implements INodeReader<Phoenix> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Phoenix> represents() {
		return Phoenix.class;
	}
	/**
	 * Parse a phoenix.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the phoenix represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Phoenix parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("phoenix", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "phoenix".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Phoenix();
	}
}

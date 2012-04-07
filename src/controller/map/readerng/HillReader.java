package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Hill;
import controller.map.SPFormatException;

/**
 * A reader for Hills.
 * @author Jonathan Lovelace
 *
 */
public class HillReader implements INodeReader<Hill> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Hill> represents() {
		return Hill.class;
	}
	/**
	 * Parse a hill.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the hill represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Hill parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Hills can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "hill".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Hill();
	}
}

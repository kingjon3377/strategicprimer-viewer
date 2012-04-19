package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Hill;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

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
	 * @param warner the Warning instance to use for warnings
	 * @return the hill represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Hill parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("hill", event.asStartElement()
						.getName().getLocalPart(), event.getLocation()
						.getLineNumber());
			} else if (event.isEndElement()
					&& "hill".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Hill();
	}
}

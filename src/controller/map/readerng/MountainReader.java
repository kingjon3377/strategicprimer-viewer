package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Mountain;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Mountains.
 * @author Jonathan Lovelace
 *
 */
public class MountainReader implements INodeReader<Mountain> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Mountain> represents() {
		return Mountain.class;
	}
	/**
	 * Parse a mountain.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the mountan represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Mountain parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("mountain", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "mountain".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Mountain();
	}
}

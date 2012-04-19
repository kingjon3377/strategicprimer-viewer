package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Oasis;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Oases.
 * @author Jonathan Lovelace
 *
 */
public class OasisReader implements INodeReader<Oasis> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Oasis> represents() {
		return Oasis.class;
	}
	/**
	 * Parse an oasis.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the oasis represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Oasis parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("oasis", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "oasis".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Oasis();
	}
}

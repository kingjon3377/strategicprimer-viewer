package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Troll;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Trolls.
 * @author Jonathan Lovelace
 *
 */
public class TrollReader implements INodeReader<Troll> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Troll> represents() {
		return Troll.class;
	}
	/**
	 * Parse a troll.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the troll represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Troll parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("troll", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "troll".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Troll();
	}
}

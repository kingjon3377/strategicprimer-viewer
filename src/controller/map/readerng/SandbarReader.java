package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Sandbar;
import controller.map.SPFormatException;

/**
 * A reader for Sandbars.
 * @author Jonathan Lovelace
 *
 */
public class SandbarReader implements INodeReader<Sandbar> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Sandbar> represents() {
		return Sandbar.class;
	}
	/**
	 * Parse a sandbar.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the sandbar represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Sandbar parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Sandbar can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "sandbar".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Sandbar();
	}
}

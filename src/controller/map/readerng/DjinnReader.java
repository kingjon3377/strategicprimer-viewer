package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Djinn;
import controller.map.SPFormatException;
/**
 * A reader for djinn.
 * @author Jonathan Lovelace
 *
 */
public class DjinnReader implements INodeReader<Djinn> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Djinn> represents() {
		return Djinn.class;
	}
	/**
	 * Parse a djinn.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the djinn represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Djinn parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Djinn can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "djinn".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Djinn();
	}

}

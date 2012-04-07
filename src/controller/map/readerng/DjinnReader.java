package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.SPFormatException;
import model.map.fixtures.Djinn;
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
	 * @return the djinn represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Djinn parse(final StartElement element, final Iterable<XMLEvent> stream)
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

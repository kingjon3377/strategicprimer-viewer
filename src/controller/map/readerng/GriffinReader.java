package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Griffin;
import controller.map.SPFormatException;

/**
 * A reader for griffins.
 * @author Jonathan Lovelace
 *
 */
public class GriffinReader implements INodeReader<Griffin> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Griffin> represents() {
		return Griffin.class;
	}
	/**
	 * Parse a griffin.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the griffin represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Griffin parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Griffins can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "griffin".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Griffin();
	}

}

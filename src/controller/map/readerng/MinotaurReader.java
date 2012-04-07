package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Minotaur;
import controller.map.SPFormatException;

/**
 * A reader for minotaurs.
 * @author Jonathan Lovelace
 *
 */
public class MinotaurReader implements INodeReader<Minotaur> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Minotaur> represents() {
		return Minotaur.class;
	}
	/**
	 * Parse a minotaur.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the minotaur represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Minotaur parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Minotaur can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "minotaur".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Minotaur();
	}

}

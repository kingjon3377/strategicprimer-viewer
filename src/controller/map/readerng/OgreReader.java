package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Ogre;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Ogres.
 * @author Jonathan Lovelace
 *
 */
public class OgreReader implements INodeReader<Ogre> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Ogre> represents() {
		return Ogre.class;
	}
	/**
	 * Parse an ogre.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the ogre represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Ogre parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("ogre", event.asStartElement()
						.getName().getLocalPart(), event.getLocation()
						.getLineNumber());
			} else if (event.isEndElement()
					&& "ogre".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Ogre();
	}
}

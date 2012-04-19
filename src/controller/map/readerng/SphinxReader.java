package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Sphinx;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Sphinxes.
 * @author Jonathan Lovelace
 *
 */
public class SphinxReader implements INodeReader<Sphinx> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Sphinx> represents() {
		return Sphinx.class;
	}
	/**
	 * Parse a sphinx.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the sphinx represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Sphinx parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("sphinx", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "sphinx".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Sphinx();
	}
}

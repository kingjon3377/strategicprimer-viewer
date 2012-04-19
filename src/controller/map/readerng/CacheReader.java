package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.CacheFixture;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for CacheFixtures.
 * @author Jonathan Lovelace
 *
 */
public class CacheReader implements INodeReader<CacheFixture> {
	/**
	 * @return the type this will produce.
	 */
	@Override
	public Class<CacheFixture> represents() {
		return CacheFixture.class;
	}
	/**
	 * Parse a cache.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the cache represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public CacheFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final CacheFixture fix = new CacheFixture(XMLHelper.getAttribute(
				element, "kind"), XMLHelper.getAttribute(element, "contents"));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("cache", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "cache".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}

}

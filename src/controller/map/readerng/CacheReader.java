package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.SPFormatException;
import model.map.fixtures.CacheFixture;

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
	 * @return the cache represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public CacheFixture parse(final StartElement element, final Iterable<XMLEvent> stream)
			throws SPFormatException {
		final CacheFixture fix = new CacheFixture(XMLHelper.getAttribute(
				element, "kind"), XMLHelper.getAttribute(element, "contents"));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Cache can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "cache".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}

}

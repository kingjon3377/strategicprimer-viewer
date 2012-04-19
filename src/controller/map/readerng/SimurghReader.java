package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Simurgh;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Simurghs.
 * @author Jonathan Lovelace
 *
 */
public class SimurghReader implements INodeReader<Simurgh> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Simurgh> represents() {
		return Simurgh.class;
	}
	/**
	 * Parse a simurgh.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the simurgh represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Simurgh parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("simurgh", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "simurgh".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Simurgh();
	}
}

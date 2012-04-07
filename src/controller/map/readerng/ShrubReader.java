package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Shrub;
import controller.map.SPFormatException;
/**
 * A reader for Shrubs.
 * @author Jonathan Lovelace
 *
 */
public class ShrubReader implements INodeReader<Shrub> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Shrub> represents() {
		return Shrub.class;
	}
	/**
	 * Parse a shrub.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the shrub represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Shrub parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Shrub fix = new Shrub(XMLHelper.getAttribute(element, "kind"));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Shrub can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "shrub".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}

}

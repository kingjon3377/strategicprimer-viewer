package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.events.CaveEvent;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Caves.
 * @author Jonathan Lovelace
 *
 */
public class CaveReader implements INodeReader<CaveEvent> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<CaveEvent> represents() {
		return CaveEvent.class;
	}
	/**
	 * Parse a cave.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed cave
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public CaveEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final CaveEvent fix = new CaveEvent(
				Integer.parseInt(XMLHelper.getAttribute(element, "dc")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("cave", event.asStartElement()
						.getName().getLocalPart(), event.getLocation()
						.getLineNumber());
			} else if (event.isEndElement()
					&& "cave".equalsIgnoreCase(event.asEndElement()
							.getName().getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

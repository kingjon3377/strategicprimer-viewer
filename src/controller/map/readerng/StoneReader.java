package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.events.StoneEvent;
import model.map.events.StoneKind;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Stones.
 * @author Jonathan Lovelace
 *
 */
public class StoneReader implements INodeReader<StoneEvent> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<StoneEvent> represents() {
		return StoneEvent.class;
	}
	/**
	 * Parse a Stone.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed stone
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public StoneEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final StoneEvent fix = new StoneEvent(
				StoneKind.parseStoneKind(XMLHelper
						.getAttributeWithDeprecatedForm(element, "kind",
								"stone", warner)), Integer.parseInt(XMLHelper
						.getAttribute(element, "dc")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("stone", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "stone".equalsIgnoreCase(event.asEndElement()
							.getName().getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

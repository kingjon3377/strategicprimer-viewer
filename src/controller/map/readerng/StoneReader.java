package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.StoneEvent;
import model.map.events.StoneKind;
import controller.map.SPFormatException;

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
	 * @return the parsed stone
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public StoneEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final StoneEvent fix = new StoneEvent(
				StoneKind.parseStoneKind(XMLHelper
						.getAttribute(element, "kind")), Integer.parseInt(XMLHelper.getAttribute(
				element, "dc")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Stone can't have child tag",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "stone".equalsIgnoreCase(event.asEndElement()
							.getName().getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

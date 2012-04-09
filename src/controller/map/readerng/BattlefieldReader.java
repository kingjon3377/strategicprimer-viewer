package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.BattlefieldEvent;
import controller.map.SPFormatException;

/**
 * A reader for Battlefields.
 * @author Jonathan Lovelace
 *
 */
public class BattlefieldReader implements INodeReader<BattlefieldEvent> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<BattlefieldEvent> represents() {
		return BattlefieldEvent.class;
	}
	/**
	 * Parse a battlefield.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @return the parsed battlefield
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public BattlefieldEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final BattlefieldEvent fix = new BattlefieldEvent(
				Integer.parseInt(XMLHelper.getAttribute(element, "dc")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Battlefield can't have child tag",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "battlefield".equalsIgnoreCase(event.asEndElement()
							.getName().getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

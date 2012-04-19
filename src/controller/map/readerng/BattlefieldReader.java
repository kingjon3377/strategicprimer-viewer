package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.events.BattlefieldEvent;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

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
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed battlefield
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public BattlefieldEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final BattlefieldEvent fix = new BattlefieldEvent(
				Integer.parseInt(XMLHelper.getAttribute(element, "dc")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("battlefield", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "battlefield".equalsIgnoreCase(event.asEndElement()
							.getName().getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

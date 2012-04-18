package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Ground.
 * @author Jonathan Lovelace
 *
 */
public class GroundReader implements INodeReader<Ground> {
	/**
	 * @return the type this produces.
	 */
	@Override
	public Class<Ground> represents() {
		return Ground.class;
	}
	/**
	 * Parse ground.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the ground represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Ground parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Ground fix = new Ground(
				XMLHelper.getAttribute(element, "kind"),
				Boolean.parseBoolean(XMLHelper.getAttribute(element, "exposed")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("ground", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "ground".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

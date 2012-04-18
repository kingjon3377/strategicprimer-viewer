package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.River;
import model.map.fixtures.RiverFixture;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Rivers.
 * @author Jonathan Lovelace
 *
 */
public class RiverReader implements INodeReader<RiverFixture> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<RiverFixture> represents() {
		return RiverFixture.class;
	}
	/**
	 * Parse a river.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the river represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public RiverFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final RiverFixture fix = new RiverFixture(
				"lake".equalsIgnoreCase(element.getName().getLocalPart()) ? River.Lake
						: River.getRiver(XMLHelper.getAttribute(element,
								"direction")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("river", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "river".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

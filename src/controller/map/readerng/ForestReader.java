package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Forest;
import controller.map.SPFormatException;
/**
 * A reader for Forests.
 * @author Jonathan Lovelace
 *
 */
public class ForestReader implements INodeReader<Forest> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Forest> represents() {
		return Forest.class;
	}
	/**
	 * Parse a forest.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the forest represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Forest parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Forest fix = new Forest(XMLHelper.getAttribute(element, "kind"),
				XMLHelper.hasAttribute(element, "rows"));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Forest can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "forest".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}

}

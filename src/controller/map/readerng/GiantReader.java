package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Giant;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A reader for Giants.
 * @author Jonathan Lovelace
 *
 */
public class GiantReader implements INodeReader<Giant> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Giant> represents() {
		return Giant.class;
	}
	/**
	 * Parse a giant.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the giant represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Giant parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Giant fix = new Giant(XMLHelper.getAttribute(element, "kind"));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("giant", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "giant".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}

}

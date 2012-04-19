package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Dragon;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A reader for Dragons.
 * @author Jonathan Lovelace
 *
 */
public class DragonReader implements INodeReader<Dragon> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Dragon> represents() {
		return Dragon.class;
	}
	/**
	 * Parse a dragon.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the dragon represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Dragon parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Dragon fix = new Dragon(XMLHelper.getAttribute(element, "kind"));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("dragon", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "dragon".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}

}

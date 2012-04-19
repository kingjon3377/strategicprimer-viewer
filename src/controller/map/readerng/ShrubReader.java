package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Shrub;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A reader for Shrubs.
 * @author Jonathan Lovelace
 *
 */
public class ShrubReader implements INodeReader<Shrub> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Shrub> represents() {
		return Shrub.class;
	}
	/**
	 * Parse a shrub.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the shrub represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Shrub parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Shrub fix = new Shrub(XMLHelper.getAttributeWithDeprecatedForm(element, "kind", "shrub", warner));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("shrub", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "shrub".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}

}

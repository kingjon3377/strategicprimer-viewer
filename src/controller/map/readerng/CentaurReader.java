package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.fixtures.Centaur;
import controller.map.SPFormatException;
/**
 * A reader for Centaurs.
 * @author Jonathan Lovelace
 *
 */
public class CentaurReader implements INodeReader<Centaur> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Centaur> represents() {
		return Centaur.class;
	}
	/**
	 * Parse a centaur.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @return the centaur represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Centaur parse(final StartElement element, final Iterable<XMLEvent> stream)
			throws SPFormatException {
		final Centaur fix = new Centaur(XMLHelper.getAttribute(element, "kind"));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Centaur can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "centaur".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}

}

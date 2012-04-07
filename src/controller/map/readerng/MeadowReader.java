package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Meadow;
import util.EqualsAny;
import controller.map.SPFormatException;
/**
 * A reader for Meadows.
 * @author Jonathan Lovelace
 *
 */
public class MeadowReader implements INodeReader<Meadow> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Meadow> represents() {
		return Meadow.class;
	}
	/**
	 * Parse a meadow.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the meadow represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Meadow parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Meadow fix = new Meadow(XMLHelper.getAttribute(
				element, "kind"), "field".equalsIgnoreCase(element
				.getName().getLocalPart()), Boolean.parseBoolean(XMLHelper
				.getAttribute(element, "cultivated")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Meadow can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& EqualsAny.equalsAny(event.asEndElement().getName()
							.getLocalPart(), "field", "meadow")) {
				break;
			}
		}
		return fix;
	}
}

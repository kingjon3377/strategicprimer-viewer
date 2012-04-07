package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttributeWithDefault;
import static java.lang.Integer.parseInt;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Fortress;
import model.map.fixtures.Unit;
import controller.map.SPFormatException;
/**
 * A reader for fortresses.
 * @author Jonathan Lovelace
 */
public class FortressReader implements INodeReader<Fortress> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<Fortress> represents() {
		return Fortress.class;
	}
	/**
	 * Parse a fortress. 
	 * @param element the element to start with
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the fortress
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Fortress parse(final StartElement element, final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Fortress fort = new Fortress(
				players.getPlayer(parseInt(getAttributeWithDefault(element,
						"owner", "-1"))), getAttributeWithDefault(element,
						"name", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()
					&& "unit".equalsIgnoreCase(event.asStartElement().getName()
							.getLocalPart())) {
				fort.addUnit(ReaderFactory.createReader(Unit.class).parse(
						event.asStartElement(), stream, players));
			} else if (event.isEndElement() && "fortress".equalsIgnoreCase(event.asEndElement().getName().getLocalPart())) {
				break;
			} else if (event.isStartElement()) {
				throw new SPFormatException(
						"Fortress can only have units as children", event
								.getLocation().getLineNumber());
			}
		}
		return fort;
	}

}

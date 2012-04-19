package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Unit;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Units.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class UnitReader implements INodeReader<Unit> {
	/**
	 * @return the class this produces.
	 */
	@Override
	public Class<Unit> represents() {
		return Unit.class;
	}

	/**
	 * Parse a unit.
	 * 
	 * @param element
	 *            the element to start with
	 * @param stream
	 *            the stream to read more elements from
	 * @param players
	 *            the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the fortress
	 * @throws SPFormatException
	 *             on SP format error
	 */
	@Override
	public Unit parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Unit fix = new Unit(players.getPlayer(Integer.parseInt(XMLHelper
				.getAttributeWithDefault(element, "owner", "-1"))),
				XMLHelper.getAttributeWithDefault(element, "kind", ""),
				XMLHelper.getAttributeWithDefault(element, "name", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("unit", event.asStartElement()
						.getName().getLocalPart(), event.getLocation()
						.getLineNumber());
			} else if (event.isEndElement()
					&& "unit".equals(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}
}

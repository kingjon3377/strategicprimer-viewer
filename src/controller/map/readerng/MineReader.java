package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Mine;
import controller.map.SPFormatException;

/**
 * A reader for Mines.
 * @author Jonathan Lovelace
 *
 */
public class MineReader implements INodeReader<Mine> {
	/**
	 * @return The type this will produce.
	 */
	@Override
	public Class<Mine> represents() {
		return Mine.class;
	}
	/**
	 * Parse a mine.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the mine represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Mine parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Mine fix = new Mine(XMLHelper.getAttribute(element, "kind"),
				TownStatus.parseTownStatus(XMLHelper.getAttribute(element,
						"status")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new SPFormatException("Mine can't have child node",
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "mine".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return fix;
	}
	}

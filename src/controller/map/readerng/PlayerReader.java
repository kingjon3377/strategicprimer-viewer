package controller.map.readerng;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.Player;
import model.map.PlayerCollection;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A reader to produce Players.
 * @author Jonathan Lovelace
 *
 */
public class PlayerReader implements INodeReader<Player> {
	/**
	 * @return the type this will produce
	 */
	@Override
	public Class<Player> represents() {
		return Player.class;
	}
	/**
	 * Parse a player from the XML.
	 * @param element the start element to read from
	 * @param stream the stream to get more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the player produced
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Player parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("player", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement() && "player".equalsIgnoreCase(event.asEndElement().getName().getLocalPart())) {
				break;
			}
		}
		return new Player(Integer.parseInt(element.getAttributeByName(
				new QName("number")).getValue()), element.getAttributeByName(
				new QName("code_name")).getValue());
	}

}

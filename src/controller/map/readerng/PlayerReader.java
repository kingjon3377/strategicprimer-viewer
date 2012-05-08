package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.Player;
import model.map.PlayerCollection;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A reader to produce Players.
 * @author Jonathan Lovelace
 *
 */
public class PlayerReader implements INodeReader<Player> {
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
		return new Player(Integer.parseInt(XMLHelper.getAttribute(element,
				"number")), XMLHelper.getAttribute(element, "code_name"));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("player");
	}

}

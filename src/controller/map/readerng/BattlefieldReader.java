package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.BattlefieldEvent;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Battlefields.
 * @author Jonathan Lovelace
 *
 */
public class BattlefieldReader implements INodeReader<BattlefieldEvent> {
	/**
	 * Parse a battlefield.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the parsed battlefield
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public BattlefieldEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		return new BattlefieldEvent(
				Integer.parseInt(getAttribute(element, "dc")),
				getOrGenerateID(element, warner, idFactory));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("battlefield");
	}
}

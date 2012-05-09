package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.StoneEvent;
import model.map.events.StoneKind;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Stones.
 * @author Jonathan Lovelace
 *
 */
public class StoneReader implements INodeReader<StoneEvent> {
	/**
	 * Parse a Stone.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the parsed stone
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public StoneEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return new StoneEvent(
				StoneKind.parseStoneKind(XMLHelper
						.getAttributeWithDeprecatedForm(element, "kind",
								"stone", warner)), Integer.parseInt(XMLHelper
						.getAttribute(element, "dc")), getOrGenerateID(element,
						warner, idFactory));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("stone");
	}
}

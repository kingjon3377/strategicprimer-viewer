package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Village;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Villages.
 * @author Jonathan Lovelace
 *
 */
public class VillageReader implements INodeReader<Village> {
	/**
	 * Parse a village.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the village represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Village parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, "name", false, warner);
		final Village fix = new Village(TownStatus.parseTownStatus(XMLHelper
				.getAttribute(element, "status")),
				XMLHelper.getAttributeWithDefault(element, "name", ""),
				getOrGenerateID(element, warner, idFactory));
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("village");
	}
}

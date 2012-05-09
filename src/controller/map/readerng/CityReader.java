package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.CityEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for cities.
 * @author Jonathan Lovelace
 */
public class CityReader implements INodeReader<CityEvent> {
	/**
	 * Parse a city.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the parsed city
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public CityEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, "name", false, warner);
		final CityEvent fix = new CityEvent(
				TownStatus.parseTownStatus(XMLHelper.getAttribute(element,
						"status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")),
				Integer.parseInt(XMLHelper.getAttribute(element, "dc")),
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
		return Collections.singletonList("city");
	}
}

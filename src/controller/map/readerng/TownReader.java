package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for towns.
 * @author Jonathan Lovelace
 *
 */
public class TownReader implements INodeReader<TownEvent> {
	/**
	 * Parse a town.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the parsed town
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TownEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		if (XMLHelper.getAttributeWithDefault(element, "name", "").isEmpty()) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "name", element.getLocation()
					.getLineNumber()));
		}
		// ESCA-JAVA0177:
		long id; // NOPMD
		if (XMLHelper.hasAttribute(element, "id")) {
			id = idFactory.register(
					Long.parseLong(XMLHelper.getAttribute(element, "id")));
		} else {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "id", element.getLocation()
					.getLineNumber()));
			id = idFactory.getID();
		}
		final TownEvent fix = new TownEvent(
				TownStatus.parseTownStatus(XMLHelper.getAttribute(element,
						"status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")), Integer
						.parseInt(XMLHelper.getAttribute(element, "dc")),
						XMLHelper.getAttributeWithDefault(element, "name", ""), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("town");
	}
}

package controller.map.readerng;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Grove;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Groves.
 * @author Jonathan Lovelace
 *
 */
public class GroveReader implements INodeReader<Grove> {
	/**
	 * Parse a grove.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the grove represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Grove parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
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
		final Grove fix = new Grove("orchard".equalsIgnoreCase(element
				.getName().getLocalPart()), Boolean.parseBoolean(XMLHelper
				.getAttribute(element, "wild")),
				XMLHelper.getAttributeWithDeprecatedForm(element, "kind",
						"tree", warner), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Arrays.asList("grove", "orchard");
	}
}

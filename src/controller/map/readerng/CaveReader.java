package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.CaveEvent;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Caves.
 * @author Jonathan Lovelace
 *
 */
public class CaveReader implements INodeReader<CaveEvent> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<CaveEvent> represents() {
		return CaveEvent.class;
	}
	/**
	 * Parse a cave.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed cave
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public CaveEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		// ESCA-JAVA0177:
		long id; // NOPMD
		if (XMLHelper.hasAttribute(element, "id")) {
			id = IDFactory.FACTORY.register(
					Long.parseLong(XMLHelper.getAttribute(element, "id")));
		} else {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "id", element.getLocation()
					.getLineNumber()));
			id = IDFactory.FACTORY.getID();
		}
		final CaveEvent fix = new CaveEvent(
				Integer.parseInt(XMLHelper.getAttribute(element, "dc")), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
}

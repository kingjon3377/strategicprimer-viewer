package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Dragon;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Dragons.
 * @author Jonathan Lovelace
 *
 */
public class DragonReader implements INodeReader<Dragon> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Dragon> represents() {
		return Dragon.class;
	}
	/**
	 * Parse a dragon.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the dragon represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Dragon parse(final StartElement element,
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
		final Dragon fix = new Dragon(XMLHelper.getAttribute(element, "kind"), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}

}

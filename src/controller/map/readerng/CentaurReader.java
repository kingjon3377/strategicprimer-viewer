package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Centaur;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Centaurs.
 * @author Jonathan Lovelace
 *
 */
public class CentaurReader implements INodeReader<Centaur> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Centaur> represents() {
		return Centaur.class;
	}
	/**
	 * Parse a centaur.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the centaur represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Centaur parse(final StartElement element,
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
		final Centaur fix = new Centaur(XMLHelper.getAttribute(element, "kind"), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}

}

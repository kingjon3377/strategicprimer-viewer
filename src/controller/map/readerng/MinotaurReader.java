package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Minotaur;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for minotaurs.
 * @author Jonathan Lovelace
 *
 */
public class MinotaurReader implements INodeReader<Minotaur> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<Minotaur> represents() {
		return Minotaur.class;
	}
	/**
	 * Parse a minotaur.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the minotaur represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Minotaur parse(final StartElement element,
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
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("minotaur", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "minotaur".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return new Minotaur(id);
	}

}

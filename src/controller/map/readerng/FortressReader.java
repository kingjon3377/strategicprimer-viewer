package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttributeWithDefault;
import static java.lang.Integer.parseInt;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.PlayerCollection;
import model.map.fixtures.Fortress;
import model.map.fixtures.Unit;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
/**
 * A reader for fortresses.
 * @author Jonathan Lovelace
 */
public class FortressReader implements INodeReader<Fortress> {
	/**
	 * @return the type we produce.
	 */
	@Override
	public Class<Fortress> represents() {
		return Fortress.class;
	}
	/**
	 * Parse a fortress. 
	 * @param element the element to start with
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the fortress
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Fortress parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner)
			throws SPFormatException {
		if ("".equals(getAttributeWithDefault(element, "owner", ""))) {
			warner.warn(new MissingParameterException("fortress", "owner", element
					.getLocation().getLineNumber()));
		}
		if ("".equals(getAttributeWithDefault(element, "name", ""))) {
			warner.warn(new MissingParameterException("fortress", "name",
					element.getLocation().getLineNumber()));
		}
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
		final Fortress fort = new Fortress(
				players.getPlayer(parseInt(getAttributeWithDefault(element,
						"owner", "-1"))), getAttributeWithDefault(element,
						"name", ""), id);
		for (final XMLEvent event : stream) {
			if (event.isStartElement()
					&& "unit".equalsIgnoreCase(event.asStartElement().getName()
							.getLocalPart())) {
				fort.addUnit(ReaderFactory.createReader(Unit.class).parse(
						event.asStartElement(), stream, players, warner));
			} else if (event.isEndElement() && "fortress".equalsIgnoreCase(event.asEndElement().getName().getLocalPart())) {
				break;
			} else if (event.isStartElement()) {
				throw new UnwantedChildException(
						"fortress can only have units as children", event
								.asStartElement().getName().getLocalPart(),
						event.getLocation().getLineNumber());
			}
		}
		return fort;
	}

}

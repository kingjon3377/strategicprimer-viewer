package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttributeWithDefault;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static java.lang.Integer.parseInt;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Fortress;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
/**
 * A reader for fortresses.
 * @author Jonathan Lovelace
 */
public class FortressReader implements INodeReader<Fortress> {
	/**
	 * Parse a fortress. 
	 * @param element the element to start with
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the fortress
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Fortress parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, "owner", false, warner);
		requireNonEmptyParameter(element, "name", false, warner);
		final Fortress fort = new Fortress(
				players.getPlayer(parseInt(getAttributeWithDefault(element,
						"owner", "-1"))), getAttributeWithDefault(element,
						"name", ""),
				getOrGenerateID(element, warner, idFactory));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()
					&& "unit".equalsIgnoreCase(event.asStartElement().getName()
							.getLocalPart())) {
				fort.addUnit(new UnitReader().parse(event.asStartElement(), //NOPMD
						stream, players, warner, idFactory));
			} else if (event.isEndElement() && "fortress".equalsIgnoreCase(event.asEndElement().getName().getLocalPart())) {
				break;
			} else if (event.isStartElement()) {
				throw new UnwantedChildException(
						"fortress", event
								.asStartElement().getName().getLocalPart(),
						event.getLocation().getLineNumber());
			}
		}
		return fort;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("fortress");
	}

}

package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDefault;
import static controller.map.readerng.XMLHelper.hasAttribute;
import static controller.map.readerng.XMLHelper.hasAttributes;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import model.map.Player;
import model.map.PlayerCollection;
import model.map.SPMap;
import model.map.Tile;
import controller.map.MissingChildException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader to produce SPMaps.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SPMapReader implements INodeReader<SPMap> {
	/**
	 * @return the type this will produce
	 */
	@Override
	public Class<SPMap> represents() {
		return SPMap.class;
	}

	/**
	 * Parse a map from XML.
	 * 
	 * @param element
	 *            the eleent to start parsing with
	 * @param stream
	 *            the XML tags and such
	 * @param players the collection of players, most likely null at this point
	 * @param warner the Warning instance to use for warnings
	 * @return the produced type
	 * @throws SPFormatException
	 *             on format problems
	 */
	@Override
	public SPMap parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		if ("map".equalsIgnoreCase(element.getName().getLocalPart())) {
			if (!hasAttribute(element, "rows")) {
				throw new MissingParameterException("map", "rows", element
						.getLocation().getLineNumber());
			} else if (!hasAttribute(element, "columns")) {
				throw new MissingParameterException("map", "columns", element
						.getLocation().getLineNumber());
			}
		} else {
			throw new MissingChildException("root",
					element.getLocation().getLineNumber());
		}
		final SPMap map = new SPMap(Integer.parseInt(getAttributeWithDefault(
				element, "version", "1")), Integer.parseInt(getAttribute(
				element, "rows")), Integer.parseInt(getAttribute(element,
				"columns")));
		for (XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement elem = event.asStartElement();
				final String type = elem.getName().getLocalPart();
				if ("player".equalsIgnoreCase(type)) {
					map.addPlayer(ReaderFactory.createReader(Player.class)
							.parse(elem, stream, map.getPlayers(), warner));
				} else if ("row".equalsIgnoreCase(type)) {
					// deliberately ignore
					continue;
				} else if ("tile".equalsIgnoreCase(type)) {
					map.addTile(ReaderFactory.createReader(Tile.class).parse(
							elem, stream, map.getPlayers(), warner));
				} else {
					throw new UnwantedChildException("map", elem.getName()
							.getLocalPart(), elem.getLocation().getLineNumber());
				}
			} else if (event.isEndElement()
					&& "map".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return map;
	}

}

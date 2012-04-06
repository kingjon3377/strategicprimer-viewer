package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDefault;
import static controller.map.readerng.XMLHelper.hasAttribute;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.Player;
import model.map.SPMap;
import model.map.Tile;
import controller.map.SPFormatException;

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
	 * @return the produced type
	 * @throws SPFormatException
	 *             on format problems
	 */
	@Override
	public SPMap parse(final StartElement element,
			final Iterable<XMLEvent> stream) throws SPFormatException {
		if ("map".equalsIgnoreCase(element.getName().getLocalPart())) {
			if (!hasAttribute(element, "rows")
					|| !hasAttribute(element, "columns")) {
				throw new SPFormatException(
						"<map> tag must have 'rows' and 'columns' attributes",
						element.getLocation().getLineNumber());
			}
		} else {
			throw new SPFormatException("Map must begin with <map> tag",
					element.getLocation().getLineNumber());
		}
		final SPMap map = new SPMap(Integer.parseInt(getAttributeWithDefault(
				element, "version", "1")), Integer.parseInt(getAttribute(
				element, "rows")), Integer.parseInt(getAttribute(element,
				"columns")));
		boolean inRow = false;
		for (XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement elem = event.asStartElement();
				final String type = elem.getName().getLocalPart();
				if ("player".equalsIgnoreCase(type)) {
					map.addPlayer(ReaderFactory.createReader(Player.class)
							.parse(elem, stream));
				} else if ("row".equalsIgnoreCase(type)) {
					if (inRow) {
						throw new SPFormatException("Rows can't be nested", elem.getLocation().getLineNumber());
					} else {
						inRow = true;
					}
				} else if ("tile".equalsIgnoreCase(type)) {
					map.addTile(ReaderFactory.createReader(Tile.class).parse(
							elem, stream));
				} else {
					throw new SPFormatException(
							"<map> can only directly contain <player>s, <row>s, and <tile>s.",
							elem.getLocation().getLineNumber());
				}
			} else if (event.isEndElement()) {
				if (inRow) {
					inRow = false;
				} else {
					break;
				}
			}
		}
		return map;
	}

}

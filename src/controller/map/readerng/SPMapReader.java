package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDefault;
import static controller.map.readerng.XMLHelper.hasAttribute;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.SPMap;
import util.EqualsAny;
import util.Warning;
import controller.map.ISPReader;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnsupportedTagException;
import controller.map.UnwantedChildException;

/**
 * A reader to produce SPMaps.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SPMapReader implements INodeReader<SPMap> {
	/**
	 * The tag we read.
	 */
	private static final String TAG = "map";

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
			if (hasAttribute(element, "rows")) {
				if (!hasAttribute(element, "columns")) {
					throw new MissingParameterException(TAG, "columns", element
							.getLocation().getLineNumber());
				}
			} else {
				throw new MissingParameterException(TAG, "rows", element
						.getLocation().getLineNumber());
			}
		final SPMap map = new SPMap(Integer.parseInt(getAttributeWithDefault(
				element, "version", "1")), Integer.parseInt(getAttribute(
				element, "rows")), Integer.parseInt(getAttribute(element,
				"columns")));
		for (XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement elem = event.asStartElement();
				parseChild(stream, warner, map, elem); 
			} else if (event.isEndElement()
					&& TAG.equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return map;
	}

	/** Parse a child element.
	 * @param stream the stream we're reading from---only here to pass to children
	 * @param warner the Warning instance to use.
	 * @param map the map we're building.
	 * @param elem the current tag.
	 * @throws SPFormatException on SP map format error
	 */
	private static void parseChild(final Iterable<XMLEvent> stream,
			final Warning warner, final SPMap map, 
			final StartElement elem) throws SPFormatException {
		final String type = elem.getName().getLocalPart();
		if ("player".equalsIgnoreCase(type)) {
			map.addPlayer(new PlayerReader()
					.parse(elem, stream, map.getPlayers(), warner));
		} else if (!"row".equalsIgnoreCase(type)) {
			// We deliberately ignore "row"; that had been a "continue",
			// but we want to extract this as a method.
			if ("tile".equalsIgnoreCase(type)) {
				map.addTile(new TileReader().parse(
						elem, stream, map.getPlayers(), warner));
			} else if (EqualsAny.equalsAny(type, ISPReader.FUTURE)) { 
				warner.warn(new UnsupportedTagException(type, elem // NOPMD
						.getLocation().getLineNumber()));
			} else {
				throw new UnwantedChildException(TAG, elem.getName()
						.getLocalPart(), elem.getLocation().getLineNumber());
			}
		}
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("map");
	}

}

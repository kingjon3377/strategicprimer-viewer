package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Forest;
import util.Warning;
import controller.map.SPFormatException;
/**
 * A reader for Forests.
 * @author Jonathan Lovelace
 *
 */
public class ForestReader implements INodeReader<Forest> {
	/**
	 * Parse a forest.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the forest represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Forest parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Forest fix = new Forest(XMLHelper.getAttribute(element, "kind"),
				XMLHelper.hasAttribute(element, "rows"));
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("forest");
	}

}

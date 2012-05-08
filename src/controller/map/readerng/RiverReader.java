package controller.map.readerng;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.River;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A reader for Rivers.
 * @author Jonathan Lovelace
 *
 */
public class RiverReader implements INodeReader<River> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<River> represents() {
		return River.class;
	}
	/**
	 * Parse a river.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the river represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public River parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final River fix = 
				"lake".equalsIgnoreCase(element.getName().getLocalPart()) ? River.Lake
						: River.getRiver(XMLHelper.getAttribute(element,
								"direction"));
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Arrays.asList("river", "lake");
	}
}

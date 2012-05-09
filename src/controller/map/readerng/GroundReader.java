package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Ground.
 * @author Jonathan Lovelace
 *
 */
public class GroundReader implements INodeReader<Ground> {
	/**
	 * Parse ground.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the ground represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Ground parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		final Ground fix = new Ground(
				XMLHelper.getAttributeWithDeprecatedForm(element, "kind", "ground", warner),
				Boolean.parseBoolean(XMLHelper.getAttribute(element, "exposed")));
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("ground");
	}
}

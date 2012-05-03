package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A reader for Ground.
 * @author Jonathan Lovelace
 *
 */
public class GroundReader implements INodeReader<Ground> {
	/**
	 * @return the type this produces.
	 */
	@Override
	public Class<Ground> represents() {
		return Ground.class;
	}
	/**
	 * Parse ground.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the ground represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Ground parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Ground fix = new Ground(
				XMLHelper.getAttributeWithDeprecatedForm(element, "kind", "ground", warner),
				Boolean.parseBoolean(XMLHelper.getAttribute(element, "exposed")));
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return fix;
	}
}

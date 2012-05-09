package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Shrub;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Shrubs.
 * @author Jonathan Lovelace
 *
 */
public class ShrubReader implements INodeReader<Shrub> {
	/**
	 * Parse a shrub.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the shrub represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Shrub parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return new Shrub(XMLHelper.getAttributeWithDeprecatedForm(
				element, "kind", "shrub", warner), getOrGenerateID(element, warner, idFactory));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("shrub");
	}

}

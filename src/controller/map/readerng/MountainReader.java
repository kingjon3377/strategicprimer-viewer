package controller.map.readerng;

import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Mountain;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Mountains.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MountainReader implements INodeHandler<Mountain> {
	/**
	 * Parse a mountain.
	 * 
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the mountan represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Mountain parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		return new Mountain(XMLHelper.getFile(stream));
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("mountain");
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Mountain> writes() {
		return Mountain.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 * 
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends Mountain> SPIntermediateRepresentation write(final S obj) {
		return new SPIntermediateRepresentation("mountain");
	}
}

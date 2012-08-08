package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.CaveEvent;
import util.Pair;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * A reader for Caves.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class CaveReader implements INodeHandler<CaveEvent> {
	/**
	 * Parse a cave.
	 * 
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the parsed cave
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public CaveEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		final CaveEvent fix = new CaveEvent(Integer.parseInt(XMLHelper
				.getAttribute(element, "dc")), getOrGenerateID(element, warner,
				idFactory));
		if (stream.iterator() instanceof IncludingIterator) {
			fix.setFile(((IncludingIterator) stream.iterator()).getFile());
		}
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("cave");
	}

	/** @return the class we know how to write */
	@Override
	public Class<CaveEvent> writes() {
		return CaveEvent.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 * 
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SPIntermediateRepresentation write(final CaveEvent obj) {
		return new SPIntermediateRepresentation("cave", Pair.of("dc",
				Integer.toString(obj.getDC())), Pair.of("id",
				Long.toString(obj.getID())));
	}
}

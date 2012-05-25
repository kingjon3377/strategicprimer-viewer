package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Griffin;
import util.Pair;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * A reader for griffins.
 * @author Jonathan Lovelace
 *
 */
public class GriffinReader implements INodeHandler<Griffin> {
	/**
	 * Parse a griffin.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the griffin represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Griffin parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		final Griffin fix = new Griffin(
				getOrGenerateID(element, warner, idFactory),
				(stream.iterator() instanceof IncludingIterator ? ((IncludingIterator) stream
						.iterator()).getFile() : ""));
		return fix;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("griffin");
	}
	
	/**
	 * @return the kind of class we know how to read.
	 */
	@Override
	public Class<Griffin> writes() {
		return Griffin.class;
	}
	/**
	 * Create an intermediate representation to write to a Writer.
	 * 
	 * @param <S> the type of the object---it can be a subclass, to make the adapter work.
	 * @param obj
	 *            the object to write
	 * @return an intermediate representation
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends Griffin> SPIntermediateRepresentation write(final S obj) {
		return new SPIntermediateRepresentation("griffin", Pair.of("id",
				Long.toString(obj.getID())));
	}

}

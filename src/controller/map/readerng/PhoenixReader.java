package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Phoenix;
import util.Pair;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Phoenixes.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class PhoenixReader implements INodeHandler<Phoenix> {
	/**
	 * Parse a phoenix.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the phoenix represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Phoenix parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		final Phoenix fix = new Phoenix(getOrGenerateID(element, warner,
				idFactory));
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("phoenix");
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Phoenix> writes() {
		return Phoenix.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends Phoenix> SPIntermediateRepresentation write(final S obj) {
		return new SPIntermediateRepresentation("phoenix", Pair.of("id",
				Long.toString(obj.getID())));
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "PhoenixReader";
	}
}

package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Minotaur;
import util.Pair;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for minotaurs.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class MinotaurReader implements INodeHandler<Minotaur> {
	/**
	 * Parse a minotaur.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the minotaur represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Minotaur parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		final Minotaur fix = new Minotaur(getOrGenerateID(element, warner,
				idFactory));
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("minotaur");
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Minotaur> writes() {
		return Minotaur.class;
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
	public <S extends Minotaur> SPIntermediateRepresentation write(final S obj) {
		return new SPIntermediateRepresentation("minotaur", Pair.of("id",
				Long.toString(obj.getID())));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MinotaurReader";
	}
}

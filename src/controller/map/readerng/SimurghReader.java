package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IPlayerCollection;
import model.map.fixtures.mobile.Simurgh;
import util.NullCleaner;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Simurghs.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class SimurghReader implements INodeHandler<Simurgh> {
	/**
	 * Parse a simurgh.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the simurgh represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Simurgh parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IPlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Simurgh fix = new Simurgh(getOrGenerateID(element, warner,
				idFactory));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("simurgh"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Simurgh> writes() {
		return Simurgh.class;
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
	public <S extends Simurgh> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"simurgh");
		retval.addIdAttribute(obj.getID());
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SimurghReader";
	}
}

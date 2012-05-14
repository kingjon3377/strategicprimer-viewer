package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import util.Pair;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;
/**
 * A reader for Ground.
 * @author Jonathan Lovelace
 *
 */
public class GroundReader implements INodeHandler<Ground> {
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
		spinUntilEnd(element.getName(), stream);
		final Ground fix = new Ground(
				getAttributeWithDeprecatedForm(element, "kind", "ground", warner),
				Boolean.parseBoolean(getAttribute(element, "exposed")));
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
		return Collections.singletonList("ground");
	}
	
	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Ground> writes() {
		return Ground.class;
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
	public <S extends Ground> SPIntermediateRepresentation write(final S obj) {
		return new SPIntermediateRepresentation("ground", Pair.of("kind",
				obj.getKind()), Pair.of("exposed",
				Boolean.toString(obj.isExposed())));
	}
}

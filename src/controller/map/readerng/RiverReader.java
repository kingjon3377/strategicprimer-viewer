package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.River;
import util.Pair;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Rivers.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class RiverReader implements INodeHandler<River> {
	/**
	 * Parse a river.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the river represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public River parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		final River fix = "lake".equalsIgnoreCase(element.getName()
				.getLocalPart()) ? River.Lake : River.getRiver(getAttribute(
				element, "direction"));
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Arrays.asList("river", "lake");
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<River> writes() {
		return River.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final River obj) {
		return River.Lake.equals(obj) ? new SPIntermediateRepresentation("lake")
				: new SPIntermediateRepresentation("river", Pair.of(
						"direction", obj.getDescription()));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "RiverReader";
	}
}

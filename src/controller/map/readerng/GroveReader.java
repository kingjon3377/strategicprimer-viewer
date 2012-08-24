package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.resources.Grove;
import util.Pair;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Groves.
 *
 * @author Jonathan Lovelace
 *
 */
public class GroveReader implements INodeHandler<Grove> {
	/**
	 * Parse a grove.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the grove represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Grove parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		final Grove fix = new Grove(
				"orchard".equalsIgnoreCase(element.getName().getLocalPart()),
				Boolean.parseBoolean(XMLHelper.getAttribute(element, "wild")),
				getAttributeWithDeprecatedForm(element, "kind", "tree", warner),
				getOrGenerateID(element, warner, idFactory), XMLHelper
						.getFile(stream));
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Arrays.asList("grove", "orchard");
	}

	/**
	 * @return the kind we know how to parse
	 */
	@Override
	public Class<Grove> writes() {
		return Grove.class;
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
	public <S extends Grove> SPIntermediateRepresentation write(final S obj) {
		return new SPIntermediateRepresentation(obj.isOrchard() ? "orchard"
				: "grove", Pair.of("wild", Boolean.toString(obj.isWild())),
				Pair.of("kind", obj.getKind()), Pair.of("id",
						Long.toString(obj.getID())));
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "GroveReader";
	}
}

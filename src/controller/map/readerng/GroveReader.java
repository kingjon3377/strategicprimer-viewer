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
import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingParameterException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Groves.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class GroveReader implements INodeHandler<Grove> {
	/**
	 * The name of the 'cultivated' property.
	 */
	private static final String CULTIVATED_ATTR = "cultivated";
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
		final boolean cultivated = isCultivated(element, warner);
		final Grove fix = new Grove(
				"orchard".equalsIgnoreCase(element.getName().getLocalPart()),
				cultivated,
				getAttributeWithDeprecatedForm(element, "kind", "tree", warner),
				getOrGenerateID(element, warner, idFactory));
		return fix;
	}

	/**
	 * @param element the element representing the XML tag representing the
	 *        grove
	 * @param warner the Warning instance to use
	 * @return whether the grove or orchard is cultivated
	 * @throws SPFormatException on XML format problems: use of 'wild' rather
	 *         than 'cultivated' if warnings are fatal, or both properties
	 *         missing ever.
	 */
	private static boolean isCultivated(final StartElement element,
			final Warning warner) throws SPFormatException {
		if (XMLHelper.hasAttribute(element, CULTIVATED_ATTR)) {
			return Boolean.parseBoolean(XMLHelper.getAttribute(element, // NOPMD
					CULTIVATED_ATTR));
		} else {
			if (XMLHelper.hasAttribute(element, "wild")) {
				warner.warn(new DeprecatedPropertyException(element.getName()
						.getLocalPart(), "wild", CULTIVATED_ATTR, element
						.getLocation().getLineNumber()));
				return !Boolean.parseBoolean(XMLHelper.getAttribute(element, "wild")); // NOPMD
			} else {
				throw new MissingParameterException(element.getName()
						.getLocalPart(), CULTIVATED_ATTR, element.getLocation()
						.getLineNumber());
			}
		}
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
				: "grove", Pair.of(CULTIVATED_ATTR, Boolean.toString(obj.isCultivated())),
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

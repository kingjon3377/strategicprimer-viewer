package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.hasAttribute;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Meadow;
import util.Pair;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Meadows.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class MeadowReader implements INodeHandler<Meadow> {
	/**
	 * The name of the property giving the status of the field.
	 */
	private static final String STATUS_ATTR = "status";

	/**
	 * Parse a meadow.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the meadow represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Meadow parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		final int id = getOrGenerateID(element, warner, idFactory); // NOPMD
		if (!hasAttribute(element, STATUS_ATTR)) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), STATUS_ATTR, element.getLocation()
					.getLineNumber()));
		}
		final Meadow fix = new Meadow(getAttribute(element, "kind"),
				"field".equalsIgnoreCase(element.getName().getLocalPart()),
				Boolean.parseBoolean(getAttribute(element, "cultivated")), id,
				FieldStatus.parse(getAttribute(element, STATUS_ATTR,
						FieldStatus.random(id).toString())),
				XMLHelper.getFile(stream));
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Arrays.asList("meadow", "field");
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Meadow> writes() {
		return Meadow.class;
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
	public <S extends Meadow> SPIntermediateRepresentation write(final S obj) {
		return new SPIntermediateRepresentation(obj.isField() ? "field"
				: "meadow", Pair.of("kind", obj.getKind()), Pair.of(
				"cultivated", Boolean.toString(obj.isCultivated())), Pair.of(
				STATUS_ATTR, obj.getStatus().toString()), Pair.of("id",
				Long.toString(obj.getID())));
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MeadowReader";
	}
}

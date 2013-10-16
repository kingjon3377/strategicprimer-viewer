package controller.map.readerng;

import static controller.map.readerng.XMLHelper.assertNonNullList;
import static controller.map.readerng.XMLHelper.assertNonNullQName;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import util.Pair;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Stones.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class StoneReader implements INodeHandler<StoneDeposit> {
	/**
	 * Parse a Stone.
	 *
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the parsed stone
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public StoneDeposit parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(assertNonNullQName(element.getName()), stream);
		final StoneDeposit fix = new StoneDeposit(
				StoneKind.parseStoneKind(XMLHelper
						.getAttributeWithDeprecatedForm(element, "kind",
								"stone", warner)), Integer.parseInt(XMLHelper
						.getAttribute(element, "dc")), getOrGenerateID(element,
						warner, idFactory));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return assertNonNullList(Collections.singletonList("stone"));
	}

	/**
	 * @return The class we know how to write.
	 */
	@Override
	public Class<StoneDeposit> writes() {
		return StoneDeposit.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final StoneDeposit obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"stone", Pair.of("kind", obj.stone().toString()), Pair.of("dc",
						Integer.toString(obj.getDC())), Pair.of("id",
						Long.toString(obj.getID())));
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "StoneReader";
	}
}

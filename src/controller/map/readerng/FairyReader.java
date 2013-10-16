package controller.map.readerng;

import static controller.map.readerng.XMLHelper.assertNonNullList;
import static controller.map.readerng.XMLHelper.assertNonNullQName;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Fairy;
import util.Pair;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Fairies.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class FairyReader implements INodeHandler<Fairy> {
	/**
	 * Parse a fairy.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the fairy represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Fairy parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(assertNonNullQName(element.getName()), stream);
		final Fairy fix = new Fairy(getAttribute(element, "kind"),
				getOrGenerateID(element, warner, idFactory));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return assertNonNullList(Collections.singletonList("fairy"));
	}

	/** @return the class we know how to write */
	@Override
	public Class<Fairy> writes() {
		return Fairy.class;
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
	public <S extends Fairy> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"fairy", Pair.of("kind", obj.getKind()), Pair.of("id",
						Long.toString(obj.getID())));
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FairyReader";
	}
}

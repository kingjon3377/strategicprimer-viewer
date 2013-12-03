package controller.map.readerng;

import static controller.map.readerng.XMLHelper.assertNonNullList;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static java.lang.Integer.parseInt;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IPlayerCollection;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for fortresses.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class FortressReader implements INodeHandler<Fortress> {
	/**
	 * Parse a fortress.
	 *
	 * @param element the element to start with
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the fortress
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Fortress parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IPlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "owner", false, warner);
		requireNonEmptyParameter(element, "name", false, warner);
		final Fortress fort = new Fortress(
				players.getPlayer(parseInt(getAttribute(element, "owner", "-1"))),
				getAttribute(element, "name", ""), getOrGenerateID(element,
						warner, idFactory));
		XMLHelper.addImage(element, fort);
		for (final XMLEvent event : stream) {
			if (event.isStartElement()
					&& "unit".equalsIgnoreCase(event.asStartElement().getName()
							.getLocalPart())) {
				final StartElement selem = event.asStartElement();
				assert selem != null;
				fort.addUnit(UNIT_READER.parse(selem, stream, players, warner,
						idFactory));
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			} else if (event.isStartElement()) {
				final String local = event.asStartElement().getName()
						.getLocalPart();
				assert local != null;
				throw new UnwantedChildException("fortress", local, event
						.getLocation().getLineNumber());
			}
		}
		return fort;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return assertNonNullList(Collections.singletonList("fortress"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Fortress> writes() {
		return Fortress.class;
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
	public <S extends Fortress> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"fortress");
		final String owner = Integer.toString(obj.getOwner().getPlayerId());
		assert owner != null;
		retval.addAttribute("owner", owner);
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addIdAttribute(obj.getID());
		for (final Unit unit : obj) {
			if (unit != null) {
				retval.addChild(ReaderAdapter.ADAPTER.write(unit));
			}
		}
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * The reader to use to parse units.
	 */
	private static final UnitReader UNIT_READER = new UnitReader();

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FortressReader";
	}
}

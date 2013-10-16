package controller.map.readerng;

import static controller.map.readerng.XMLHelper.assertNonNullList;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import util.Warning;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for Units.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class UnitReader implements INodeHandler<Unit> {
	/**
	 * The name of the property telling what kind of unit.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Parse a unit.
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
	public Unit parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "owner", false, warner);
		requireNonEmptyParameter(element, "name", false, warner);
		final Unit fix = new Unit(
				players.getPlayer(Integer.parseInt(ensureNumeric(getAttribute(
						element, "owner", "-1")))), parseKind(element, warner),
				getAttribute(element, "name", ""), getOrGenerateID(element,
						warner, idFactory));
		XMLHelper.addImage(element, fix);
		final StringBuilder orders = new StringBuilder();
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final Object result = ReaderAdapter.ADAPTER.parse(
						event.asStartElement(), stream, players, warner,
						idFactory);
				if (result instanceof UnitMember) {
					fix.addMember((UnitMember) result);
				} else {
					throw new UnwantedChildException(element.getName()
							.getLocalPart(), event.asStartElement().getName()
							.getLocalPart(), event.getLocation()
							.getLineNumber());
				}
			} else if (event.isCharacters()) {
				orders.append(event.asCharacters().getData());
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		fix.setOrders(orders.toString().trim());
		return fix;
	}

	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the
	 * empty string.
	 *
	 * @param element the current element
	 * @param warner the Warning instance to use
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private static String parseKind(final StartElement element,
			final Warning warner) throws SPFormatException {
		String retval = "";
		try {
			retval = getAttributeWithDeprecatedForm(element, // NOPMD
					KIND_PROPERTY, "type", warner);
		} catch (final MissingPropertyException except) {
			warner.warn(except);
			return ""; // NOPMD
		}
		if (retval.isEmpty()) {
			warner.warn(new MissingPropertyException(element.getName()
					.getLocalPart(), KIND_PROPERTY, element.getLocation()
					.getLineNumber()));
		}
		return retval;
	}

	/**
	 * @param string a string that may be either numeric or empty.
	 * @return it, or "-1" if it's empty.
	 */
	private static String ensureNumeric(final String string) {
		return string.isEmpty() ? "-1" : string;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return assertNonNullList(Collections.singletonList("unit"));
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
	public <S extends Unit> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"unit");
		retval.addAttribute("owner",
				Integer.toString(obj.getOwner().getPlayerId()));
		if (!obj.getKind().isEmpty()) {
			retval.addAttribute("kind", obj.getKind());
		}
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addAttribute("id", Long.toString(obj.getID()));
		for (final UnitMember member : obj) {
			retval.addChild(ReaderAdapter.ADAPTER.write(member));
		}
		if (!obj.getOrders().trim().isEmpty()) {
			retval.addAttribute("text-contents", obj.getOrders().trim() + '\n');
		}
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return The type we know how to write
	 */
	@Override
	public Class<Unit> writes() {
		return Unit.class;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "UnitReader";
	}
}

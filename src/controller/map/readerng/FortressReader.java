package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static controller.map.readerng.SPIntermediateRepresentation.createTagMap;
import static java.lang.Integer.parseInt;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for fortresses.
 *
 * @author Jonathan Lovelace
 */
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
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "owner", false, warner);
		requireNonEmptyParameter(element, "name", false, warner);
		final Fortress fort = new Fortress(
				players.getPlayer(parseInt(getAttribute(element, "owner", "-1"))),
				getAttribute(element, "name", ""), getOrGenerateID(element,
						warner, idFactory), XMLHelper.getFile(stream));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()
					&& "unit".equalsIgnoreCase(event.asStartElement().getName()
							.getLocalPart())) {
				fort.addUnit(UNIT_READER.parse(event.asStartElement(), // NOPMD
						stream, players, warner, idFactory));
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			} else if (event.isStartElement()) {
				throw new UnwantedChildException("fortress", event
						.asStartElement().getName().getLocalPart(), event
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
		return Collections.singletonList("fortress");
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
		retval.addAttribute("owner",
				Integer.toString(obj.getOwner().getPlayerId()));
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addAttribute("id", Long.toString(obj.getID()));
		final Map<String, SPIntermediateRepresentation> tagMap = createTagMap();
		tagMap.put(obj.getFile(), retval);
		if (!obj.getUnits().isEmpty()) {
			for (final Unit unit : obj.getUnits()) {
				addChild(tagMap, unit, retval);
			}
		}
		return retval;
	}

	/**
	 * Add a child node to a node---the parent node, or an 'include' node
	 * representing its chosen file.
	 *
	 * @param map the mapping from filenames to IRs.
	 * @param obj the object we're handling
	 * @param parent the parent node, so we can add any include nodes created to
	 *        it
	 */
	private static void addChild(
			final Map<String, SPIntermediateRepresentation> map,
			final Unit obj, final SPIntermediateRepresentation parent) {
		if (!map.containsKey(obj.getFile())) {
			final SPIntermediateRepresentation includeTag = new SPIntermediateRepresentation(
					"include");
			includeTag.addAttribute("file", obj.getFile());
			parent.addChild(includeTag);
		}
		map.get(obj.getFile()).addChild(UNIT_READER.write(obj));
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

package controller.map.readerng;

import static controller.map.readerng.XMLHelper.assertNonNullList;
import static controller.map.readerng.XMLHelper.assertNonNullQName;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.getPlayerOrIndependent;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for towns.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class TownReader implements INodeHandler<Town> {
	/**
	 * Parse a town.
	 *
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the parsed town
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Town parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "name", false, warner);
		spinUntilEnd(assertNonNullQName(element.getName()), stream);
		final Town fix = new Town(TownStatus.parseTownStatus(getAttribute(
				element, "status")), TownSize.parseTownSize(XMLHelper
				.getAttribute(element, "size")), Integer.parseInt(getAttribute(
				element, "dc")), getAttribute(element, "name", ""),
				getOrGenerateID(element, warner, idFactory),
				getPlayerOrIndependent(element, warner, players));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return assertNonNullList(Collections.singletonList("town"));
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Town obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"town");
		retval.addAttribute("status", obj.status().toString());
		retval.addAttribute("size", obj.size().toString());
		retval.addAttribute("dc", Integer.toString(obj.getDC()));
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addAttribute("id", Long.toString(obj.getID()));
		retval.addAttribute("owner",
				Integer.toString(obj.getOwner().getPlayerId()));
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return the class we can write
	 */
	@Override
	public Class<Town> writes() {
		return Town.class;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TownReader";
	}
}

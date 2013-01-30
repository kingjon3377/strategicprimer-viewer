package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.towns.FortificationEvent;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for fortifications.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class FortificationReader implements INodeHandler<FortificationEvent> {
	/**
	 * Parse a city.
	 *
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the parsed city
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public FortificationEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireNonEmptyParameter(element, "name", false, warner);
		spinUntilEnd(element.getName(), stream);
		final FortificationEvent fix = new FortificationEvent(
				TownStatus.parseTownStatus(getAttribute(element, "status")),
				TownSize.parseTownSize(XMLHelper.getAttribute(element, "size")),
				Integer.parseInt(getAttribute(element, "dc")), getAttribute(
						element, "name", ""), getOrGenerateID(element, warner,
						idFactory));
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("fortification");
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<FortificationEvent> writes() {
		return FortificationEvent.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final FortificationEvent obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"fortification");
		retval.addAttribute("status", obj.status().toString());
		retval.addAttribute("size", obj.size().toString());
		retval.addAttribute("dc", Integer.toString(obj.getDC()));
		if (!obj.getName().isEmpty()) {
			retval.addAttribute("name", obj.getName());
		}
		retval.addAttribute("id", Long.toString(obj.getID()));
		return retval;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FortificationReader";
	}
}

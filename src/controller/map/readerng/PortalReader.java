package controller.map.readerng;

import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IMutablePlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.fixtures.Portal;
import util.NullCleaner;
import util.Pair;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for portals.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class PortalReader implements INodeHandler<Portal> {
	/**
	 * Parse a portal. Parse an adventure hook.
	 *
	 * @param element
	 *            the element to read from
	 * @param stream
	 *            a stream of more elements
	 * @param players
	 *            the list of players
	 * @param warner
	 *            the Warning instance to use for warnings
	 * @param idFactory
	 *            the factory to use to register ID numbers and generate new
	 *            ones as needed
	 * @return the parsed portal
	 * @throws SPFormatException
	 *             on SP format error
	 */
	@Override
	public Portal parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IMutablePlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		Portal retval =
				new Portal(
						XMLHelper.getAttribute(element, "world"),
						PointFactory.point(XMLHelper.parseInt(XMLHelper
								.getAttribute(element, "row"), NullCleaner
								.assertNotNull(element.getLocation())),
								XMLHelper.parseInt(XMLHelper.getAttribute(
										element, "column"), NullCleaner
										.assertNotNull(element.getLocation()))),
						XMLHelper.getOrGenerateID(element, warner, idFactory));
		XMLHelper.addImage(element, retval);
		return retval;
	}

	/** @return the class we know how to write */
	@Override
	public Class<Portal> writes() {
		return Portal.class;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("portal"));
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param obj
	 *            the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Portal obj) {
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("portal", Pair.of("world",
						obj.getDestinationWorld()));
		Point dest = obj.getDestinationCoordinates();
		retval.addAttribute("row",
				NullCleaner.assertNotNull(Integer.toString(dest.row)));
		retval.addAttribute("column",
				NullCleaner.assertNotNull(Integer.toString(dest.col)));
		retval.addIdAttribute(obj.getID());
		retval.addImageAttribute(obj);
		return retval;
	}
}

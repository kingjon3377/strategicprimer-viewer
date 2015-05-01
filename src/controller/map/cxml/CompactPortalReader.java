package controller.map.cxml;

import java.io.IOException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IMutablePlayerCollection;
import model.map.PointFactory;
import model.map.fixtures.Portal;

import org.eclipse.jdt.annotation.Nullable;

import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for portals.
 *
 * @author Jonathan Lovelace
 */
public class CompactPortalReader extends AbstractCompactReader<Portal> {
	/**
	 * Singleton object.
	 */
	public static final CompactPortalReader READER = new CompactPortalReader();

	/**
	 * Read a portal from XML.
	 *
	 * @param element
	 *            The XML element to parse
	 * @param stream
	 *            the stream to read more elements from
	 * @param players
	 *            the collection of players
	 * @param warner
	 *            the Warning instance to use for warnings
	 * @param idFactory
	 *            the ID factory to use to generate IDs
	 * @return the parsed portal
	 * @throws SPFormatException
	 *             on SP format problems
	 */
	@Override
	public Portal read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "portal");
		Portal retval =
				new Portal(getParameter(element, "world"), PointFactory.point(
						Integer.parseInt(getParameter(element, "row")),
						Integer.parseInt(getParameter(element, "column"))),
						getOrGenerateID(element, warner, idFactory));
		retval.setImage(getParameter(element, "image", ""));
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		return retval;
	}

	/**
	 * Write a portal to XML.
	 *
	 * @param ostream
	 *            the stream to write to
	 * @param obj
	 *            the portal to write
	 * @param indent
	 *            the current indentation level
	 * @throws IOException
	 *             on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final Portal obj,
			final int indent) throws IOException {
		ostream.append(indent(indent));
		ostream.append("<portal world=\"");
		ostream.append(obj.getDestinationWorld());
		ostream.append("\" row=\"");
		ostream.append(Integer.toString(obj.getDestinationCoordinates().row));
		ostream.append("\" column=\"");
		ostream.append(Integer.toString(obj.getDestinationCoordinates().col));
		ostream.append("\" id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append("\" ");
		ostream.append(imageXML(obj));
		ostream.append(" />\n");
	}
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
		return "portal".equalsIgnoreCase(tag);
	}
}

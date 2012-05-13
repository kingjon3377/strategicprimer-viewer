package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.requireNonEmptyParameter;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * A reader for towns.
 * @author Jonathan Lovelace
 *
 */
public class TownReader implements INodeHandler<TownEvent> {
	/**
	 * Parse a town.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the parsed town
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TownEvent parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, "name", false, warner);
		spinUntilEnd(element.getName(), stream);
		final TownEvent fix = new TownEvent(
				TownStatus.parseTownStatus(getAttribute(element,
						"status")), TownSize.parseTownSize(XMLHelper
						.getAttribute(element, "size")),
				Integer.parseInt(getAttribute(element, "dc")),
				getAttribute(element, "name", ""),
				getOrGenerateID(element, warner, idFactory));
		if (stream.iterator() instanceof IncludingIterator) {
			fix.setFile(((IncludingIterator) stream.iterator()).getFile());
		}
		return fix;
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("town");
	}
	/**
	 * Write an instance of the type to a Writer.
	 * @param obj
	 *            the object to write
	 * @param writer
	 *            the Writer we're currently writing to
	 * @param inclusion
	 *            whether to create 'include' tags and separate files for
	 *            elements whose 'file' is different from that of their parents
	 * @throws IOException
	 *             on I/O error while writing
	 */
	@Override
	public void write(final TownEvent obj, final Writer writer, final boolean inclusion)
			throws IOException {
		writer.append("<town status=\"");
		writer.append(obj.status().toString());
		writer.append("\" size=\"");
		writer.append(obj.size().toString());
		writer.append("\" dc=\"");
		writer.append(Integer.toString(obj.getDC()));
		if (!obj.name().isEmpty()) {
			writer.append("\" name=\"");
			writer.append(obj.name());
		}
		writer.append("\" id=\"");
		writer.append(Long.toString(obj.getID()));
		writer.append("\" />");
	}
	/**
	 * @return the class we can write
	 */
	@Override
	public Class<TownEvent> writes() {
		return TownEvent.class;
	}
}

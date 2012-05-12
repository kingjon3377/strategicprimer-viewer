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
import model.map.events.TownStatus;
import model.map.fixtures.Village;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Villages.
 * @author Jonathan Lovelace
 *
 */
public class VillageReader implements INodeHandler<Village> {
	/**
	 * Parse a village.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the village represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Village parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireNonEmptyParameter(element, "name", false, warner);
		spinUntilEnd(element.getName(), stream);
		return new Village(TownStatus.parseTownStatus(getAttribute(element, "status")),
				getAttribute(element, "name", ""),
				getOrGenerateID(element, warner, idFactory));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("village");
	}
	/**
	 * Write an instance of the type to a Writer.
	 * 
	 * @param <S> the actual type of the object to write
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
	public <S extends Village> void write(final S obj, final Writer writer,
			final boolean inclusion) throws IOException {
		writer.append("<village status=\"");
		writer.append(obj.getStatus().toString());
		if (obj.getName().isEmpty()) {
			writer.append("\" name=\"");
			writer.append(obj.getName());
		}
		writer.append("\" id=\"");
		writer.append(Long.toString(obj.getID()));
		writer.append("\" />");
	}
	/**
	 * @return The class we know how to parse
	 */
	@Override
	public Class<Village> writes() {
		return Village.class;
	}
}

package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Meadow;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Meadows.
 * @author Jonathan Lovelace
 *
 */
public class MeadowReader implements INodeHandler<Meadow> {
	/**
	 * Parse a meadow.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the meadow represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Meadow parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		return new Meadow(getAttribute(element, "kind"),
				"field".equalsIgnoreCase(element.getName().getLocalPart()),
				Boolean.parseBoolean(getAttribute(element,
						"cultivated")), getOrGenerateID(element, warner,
						idFactory));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Arrays.asList("meadow", "field");
	}
	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Meadow> writes() {
		return Meadow.class;
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
	public <S extends Meadow> void write(final S obj, final Writer writer, final boolean inclusion)
			throws IOException {
		if (obj.isField()) {
			writer.write("<field");
		} else {
			writer.write("<meadow");
		}
		writer.write(" kind=\"");
		writer.write(obj.getKind());
		writer.write("\" cultivated=\"");
		writer.write(Boolean.toString(obj.isCultivated()));
		writer.write("\" id=\"");
		writer.write(Long.toString(obj.getID()));
		writer.write("\" />");
	}
}

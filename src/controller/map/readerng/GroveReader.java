package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Grove;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Groves.
 * @author Jonathan Lovelace
 *
 */
public class GroveReader implements INodeHandler<Grove> {
	/**
	 * Parse a grove.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the grove represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Grove parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		return new Grove("orchard".equalsIgnoreCase(element
				.getName().getLocalPart()), Boolean.parseBoolean(XMLHelper
				.getAttribute(element, "wild")),
				getAttributeWithDeprecatedForm(element, "kind",
						"tree", warner), getOrGenerateID(element, warner, idFactory));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Arrays.asList("grove", "orchard");
	}
	/**
	 * @return the kind we know how to parse
	 */
	@Override
	public Class<Grove> writes() {
		return Grove.class;
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
	public <S extends Grove> void write(final S obj, final Writer writer, final boolean inclusion)
			throws IOException {
		if (obj.isOrchard()) {
			writer.write("<orchard");
		} else {
			writer.write("<grove");
		}
		writer.write(" wild=\"");
		writer.write(Boolean.toString(obj.isWild()));
		writer.write("\" kind=\"");
		writer.write(obj.getKind());
		writer.write("\" id=\"");
		writer.write(Long.toString(obj.getID()));
		writer.write("\" />");
	}
}

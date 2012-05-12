package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.hasAttribute;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Forest;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A reader for Forests.
 * @author Jonathan Lovelace
 *
 */
public class ForestReader implements INodeHandler<Forest> {
	/**
	 * Parse a forest.
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the forest represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Forest parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		return new Forest(getAttribute(element, "kind"),
				hasAttribute(element, "rows"));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("forest");
	}
	/** @return the class we know how to write */
	@Override
	public Class<Forest> writes() {
		return Forest.class;
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
	public <S extends Forest> void write(final S obj, final Writer writer, final boolean inclusion)
			throws IOException {
		writer.write("<forest kind=\"");
		writer.write(obj.getKind());
		if (obj.isRows()) {
			writer.write("\" rows=\"true");
		}
		writer.write("\" />");
	}

}

package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.TextFixture;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
/**
 * A reader for text elements.
 * @author Jonathan Lovelace
 *
 */
public class TextReader implements INodeHandler<TextFixture> {
	/**
	 * Parse a TextFixture.
	 * @param element the element to parse
	 * @param stream the stream to get more elements (in this case, the text) from
	 * @param players ignored
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the TextFixture
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TextFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		final StringBuilder sbuild = new StringBuilder(""); // NOPMD
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("text", event.asStartElement()
						.getName().getLocalPart(), event.getLocation()
						.getLineNumber());
			} else if (event.isCharacters()) {
				sbuild.append(event.asCharacters().getData());
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return new TextFixture(sbuild.toString().trim(),
				Integer.parseInt(getAttribute(element,
						"turn", "-1")));
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("text");
	}
	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<TextFixture> writes() {
		return TextFixture.class;
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
	public <S extends TextFixture> void write(final S obj, final Writer writer,
			final boolean inclusion) throws IOException {
		if (obj.getTurn() == -1) {
			writer.append("<text>");
		} else {
			writer.append("<text turn=\"");
			writer.append(Integer.toString(obj.getTurn()));
			writer.append("\">");
		}
		writer.append(obj.getText().trim());
		writer.append("</text>");
	}
}

package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.TextFixture;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTextReader extends AbstractCompactReader implements CompactReader<TextFixture> {
	/**
	 * Singleton.
	 */
	private CompactTextReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactTextReader READER = new CompactTextReader();
	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "text".equalsIgnoreCase(tag);
	}
	/**
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format errors
	 */
	@Override
	public TextFixture read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		final StringBuilder sbuild = new StringBuilder(""); // NOPMD
		requireTag(element, "text");
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
		final TextFixture fix = new TextFixture(sbuild.toString().trim(),
				Integer.parseInt(getParameter(element, "turn", "-1")));
		return fix;
	}
	/**
	 * Write an object to a stream.
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final TextFixture obj, final int indent) throws IOException {
		out.append(indent(indent));
		if (obj.getTurn() == -1) {
			out.append("<text>");
		} else {
			out.append("<text turn=\"");
			out.append(Integer.toString(obj.getTurn()));
			out.append("\">");
		}
		out.append(obj.getText().trim());
		out.append("</text>\n");
	}
}



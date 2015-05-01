package controller.map.cxml;

import java.io.IOException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IMutablePlayerCollection;
import model.map.fixtures.TextFixture;

import org.eclipse.jdt.annotation.Nullable;

import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 *
 * @author Jonathan Lovelace
 *
 */
public final class CompactTextReader extends AbstractCompactReader<TextFixture> {
	/**
	 * Singleton object.
	 */
	public static final CompactTextReader READER = new CompactTextReader();

	/**
	 * Singleton.
	 */
	private CompactTextReader() {
		// Singleton.
	}

	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
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
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "text");
		// Of all the uses of a StringBuilder, this one can't know what size we
		// need. But cases above 2K will be vanishingly rare in practice.
		final StringBuilder sbuild = new StringBuilder(2048); // NOPMD
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("text",
						NullCleaner.assertNotNull(event.asStartElement()
								.getName().getLocalPart()), event.getLocation()
								.getLineNumber());
			} else if (event.isCharacters()) {
				sbuild.append(event.asCharacters().getData());
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		final TextFixture fix =
				new TextFixture(NullCleaner.assertNotNull(sbuild.toString()
						.trim()), parseInt(getParameter(element,
						"turn", "-1"), element.getLocation().getLineNumber()));
		fix.setImage(getParameter(element, "image", ""));
		return fix;
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final TextFixture obj,
			final int indent) throws IOException {
		ostream.append(indent(indent));
		if (obj.getTurn() == -1) {
			ostream.append("<text");
		} else {
			ostream.append("<text turn=\"");
			ostream.append(Integer.toString(obj.getTurn()));
			ostream.append('"');
		}
		ostream.append(imageXML(obj));
		ostream.append('>');
		ostream.append(obj.getText().trim());
		ostream.append("</text>\n");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactTextReader";
	}
}

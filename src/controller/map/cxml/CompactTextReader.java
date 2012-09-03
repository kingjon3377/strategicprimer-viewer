package controller.map.cxml;

import static controller.map.readerng.XMLHelper.getAttribute;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.TextFixture;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTextReader extends CompactReaderSuperclass implements CompactReader<TextFixture> {
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
	public boolean isSupportedTag(final String tag) {
		return "text".equalsIgnoreCase(tag);
	}
	/**
	 *
	 * @param <U> the actual type of the object
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format errors
	 */
	@Override
	public <U extends TextFixture> U read(final StartElement element,
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
				Integer.parseInt(getAttribute(element, "turn", "-1")));
		if (stream.iterator() instanceof IncludingIterator) {
			fix.setFile(((IncludingIterator) stream.iterator()).getFile());
		}
		return (U) fix;
	}
}



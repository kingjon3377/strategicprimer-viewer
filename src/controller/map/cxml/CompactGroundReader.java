package controller.map.cxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactGroundReader extends CompactReaderSuperclass implements CompactReader<Ground> {
	/**
	 * Singleton.
	 */
	private CompactGroundReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactGroundReader READER = new CompactGroundReader();
	/**
	 *
	 * @param <U> the actual type of the object
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Ground read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "ground");
		final String kind = getParameterWithDeprecatedForm(element, "kind", "ground", warner);
		requireNonEmptyParameter(element, "exposed", true, warner);
		spinUntilEnd(element.getName(), stream);
		return new Ground(kind, Boolean.parseBoolean(getParameter(element, "exposed")), getFile(stream));
	}
	/**
	 * @param tag a tag
	 * @return whether it's one we support
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "ground".equalsIgnoreCase(tag);
	}
}

package controller.map.cxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.IteratorWrapper;
import util.Warning;
import controller.map.misc.IDFactory;
import model.map.IMap;
import model.map.PlayerCollection;

/**
 * A reader for maps.
 * @author Jonathan Lovelace
 *
 */
public final class CompactMapReader extends CompactReaderSuperclass implements CompactReader<IMap> {
	/**
	 * Read a map from XML.
	 * @param <U> The actual type we'll return
	 * @param element the element we're parsing
	 * @param stream the source to read more elements from
	 * @param players The collection to put players in
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed map
	 */
	@Override
	public <U extends IMap> U read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Singleton.
	 */
	private CompactMapReader() {
		// Singleton.
	}
	/**
	 * Singleton instance.
	 */
	public static final CompactMapReader READER = new CompactMapReader();
	/**
	 * @param tag a tag
	 * @return whether it's one we support
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "map".equalsIgnoreCase(tag) || "view".equalsIgnoreCase(tag);
	}
}

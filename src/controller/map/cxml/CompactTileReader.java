package controller.map.cxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.River;
import model.map.Tile;
import util.IteratorWrapper;
import util.Warning;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTileReader extends CompactReaderSuperclass implements CompactReader<Tile> {
	/**
	 * Singleton.
	 */
	private CompactTileReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactTileReader READER = new CompactTileReader();
	/**
	 *
	 * @param <U> the actual type of the object
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 */
	@Override
	public <U extends Tile> U read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Parse a river from XML.
	 * @param element the element to parse
	 * @param stream the stream to read further elements from (FIXME: do we need this parameter?)
	 * @param warner the Warning instance to use as needed
	 * @return the parsed river
	 */
	public River parseRiver(final StartElement element, final IteratorWrapper<XMLEvent> stream,
			final Warning warner) {
		// TODO Auto-generated method stub
		return null;
	}
}

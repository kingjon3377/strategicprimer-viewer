package controller.map.cxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.TerrainFixture;
import util.IteratorWrapper;
import util.Warning;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTerrainReader implements CompactReader<TerrainFixture> {
	/**
	 * Singleton.
	 */
	private CompactTerrainReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactTerrainReader READER = new CompactTerrainReader();
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
	public <U extends TerrainFixture> U read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) {
		// TODO Auto-generated method stub
		return null;
	}
}

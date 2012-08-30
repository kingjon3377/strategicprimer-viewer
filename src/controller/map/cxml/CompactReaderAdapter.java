package controller.map.cxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IMap;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.River;
import model.map.TerrainFixture;
import model.map.Tile;
import model.map.TileFixture;
import model.map.XMLWritable;
import model.map.fixtures.Ground;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.MobileFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.HarvestableFixture;
import model.map.fixtures.towns.TownFixture;
import util.IteratorWrapper;
import util.Warning;
import controller.map.misc.IDFactory;

/**
 * @author Jonathan Lovelace
 */
public final class CompactReaderAdapter {
	/**
	 * Singleton constructor.
	 */
	private CompactReaderAdapter() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactReaderAdapter ADAPTER = new CompactReaderAdapter();
	/**
	 * Parse an object from XML.
	 * @param <T> the type the caller expects
	 * @param <U> the type of the object we'll return
	 * @param type the type the caller expects
	 * @param element the element we're immediately dealing with
	 * @param stream the stream from which to read more elements
	 * @param players the PlayerCollecton to use when needed
	 * @param warner the Warning instance if warnings need to be issued
	 * @param idFactory the ID factory to get IDs from
	 * @return the object encoded by the XML
	 */
	@SuppressWarnings("unchecked")
	// We *do* check ... but neither Java nor Eclipse can know that
	public <T extends XMLWritable, U extends T> U parse(final Class<T> type,
			final StartElement element, final IteratorWrapper<XMLEvent> stream,
			final PlayerCollection players, final Warning warner,
			final IDFactory idFactory) {
		// ESCA-JAVA0177:
		final CompactReader<T> reader; // NOPMD
		if (River.class.isAssignableFrom(type)) {
			// Handle rivers specially.
			return (U) CompactTileReader.READER.parseRiver(element, stream, // NOPMD
					warner);
		} else {
			reader = getReader(type);
		}
		return reader.read(element, stream, players, warner, idFactory);
	}
	/**
	 * Get a reader for the specified type.
	 * @param <T> the type
	 * @param type the type
	 * @return a reader for the type
	 */
	@SuppressWarnings("unchecked")
	private static <T extends XMLWritable> CompactReader<T> getReader(final Class<T> type) {
		final CompactReader<T> reader; // NOPMD
		if (IMap.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactMapReader.READER;
		} else if (Tile.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactTileReader.READER;
		} else if (Player.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactPlayerReader.READER;
		} else if (TileFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) getFixtureReader((Class<? extends TileFixture>) type);
		} else {
			throw new IllegalStateException("Unhandled type "
					+ type.getName());
		}
		return reader;
	}
	/**
	 * @param <T> the type
	 * @param type the type
	 * @return a reader for that type
	 */
	@SuppressWarnings("unchecked")
	private static <T extends TileFixture> CompactReader<T> getFixtureReader(final Class<T> type) {
		final CompactReader<T> reader; // NOPMD
		if (TerrainFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactTerrainReader.READER;
		} else if (TownFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactTownReader.READER;
		} else if (HarvestableFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactResourceReader.READER;
		} else if (Unit.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactUnitReader.READER;
		} else if (MobileFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactMobileReader.READER;
		} else if (Ground.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactGroundReader.READER;
		} else if (TextFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactTextReader.READER;
		} else {
			throw new IllegalStateException("Unhandled type "
					+ type.getName());
		}
		return reader;
	}
}

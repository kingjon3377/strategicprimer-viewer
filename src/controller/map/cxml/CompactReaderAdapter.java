package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

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
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.MobileFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.resources.HarvestableFixture;
import model.map.fixtures.towns.TownFixture;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
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
	 * @param type the type the caller expects
	 * @param element the element we're immediately dealing with
	 * @param stream the stream from which to read more elements
	 * @param players the PlayerCollecton to use when needed
	 * @param warner the Warning instance if warnings need to be issued
	 * @param idFactory the ID factory to get IDs from
	 * @return the object encoded by the XML
	 * @throws SPFormatException on SP format problems
	 */
	public <T extends XMLWritable> T parse(final Class<T> type,
			final StartElement element, final IteratorWrapper<XMLEvent> stream,
			final PlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		// ESCA-JAVA0177:
		final CompactReader<T> reader; // NOPMD
		if (River.class.isAssignableFrom(type)) {
			// Handle rivers specially.
			return (T) CompactTileReader.READER.parseRiver(element, stream, // NOPMD
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
	// We *do* check ... but neither Java nor Eclipse can know that
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
		} else if (Worker.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactWorkerReader.READER;
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
	/**
	 * Write an object to XML.
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent the current indentation level.
	 * @throws IOException on I/O problems
	 */
	@SuppressWarnings("unchecked")
	public void write(final Writer out, final XMLWritable obj, final int indent) throws IOException {
			@SuppressWarnings("rawtypes") // NOPMD
			final CompactReader reader; // NOPMD
			if (obj instanceof IMap) {
				reader = CompactMapReader.READER;
			} else if (obj instanceof Tile) {
				reader = CompactTileReader.READER;
			} else if (obj instanceof River) {
				CompactTileReader.READER.writeRiver(out, (River) obj, indent);
				return; // NOPMD
			} else if (obj instanceof RiverFixture) {
				CompactTileReader.READER.writeRivers(out, (RiverFixture) obj, indent);
				return; // NOPMD
			} else if (obj instanceof Job) {
				CompactWorkerReader.READER.writeJob(out, (Job) obj, indent);
				return; // NOPMD
			} else if (obj instanceof Skill) {
				CompactWorkerReader.READER.writeSkill(out, (Skill) obj, indent);
				return; // NOPMD
			} else if (obj instanceof Player) {
				reader = CompactPlayerReader.READER;
			} else if (obj instanceof TileFixture) {
				reader = getFixtureReader(((TileFixture) obj).getClass());
			} else if (obj instanceof Worker) {
				reader = CompactWorkerReader.READER;
			} else {
				throw new IllegalStateException("Don't know how to write this type");
			}
			reader.write(out, obj, indent);
	}
}

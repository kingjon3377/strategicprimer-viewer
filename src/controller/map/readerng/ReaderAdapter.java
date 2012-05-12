package controller.map.readerng;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.XMLWritable;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.XMLEscapingStringWriter;

/**
 * An alternative approach, to hopefully replace the ReaderFactory---instead of
 * asking for a reader based on what type you expect, you tell the Adapter to
 * parse the node and it does The Right Thing. In theory. This is absolutely
 * necessary to implement the "include" tag.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ReaderAdapter implements INodeHandler<XMLWritable> {
	/**
	 * Parse an element.
	 * 
	 * @param element
	 *            the element to parse
	 * @param stream
	 *            the stream to get more elements from
	 * @param players
	 *            the collection of players to use if needed
	 * @param warner
	 *            the Warning instance to use
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the result of parsing the element
	 * @throws SPFormatException
	 *             on SP format problems.
	 */
	@Override
	public XMLWritable parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		if (READ_CACHE.containsKey(element.getName().getLocalPart())) {
			return READ_CACHE.get(element.getName().getLocalPart()).parse(element,
					stream, players, warner, idFactory);
		} else {
			throw new UnwantedChildException("unknown", element.getName()
					.getLocalPart(), element.getLocation().getLineNumber());
		}
	}

	/**
	 * Map from tags to readers. Initializer moved to static block below because
	 * here it made the line *way* too long.
	 */
	private static final Map<String, INodeHandler<? extends XMLWritable>> READ_CACHE;
	/**
	 * Map from writable objects to writers. Initializer in static block below.
	 */
	private static final Map<Class<? extends XMLWritable>, INodeHandler<? extends XMLWritable>> WRITE_CACHE;
	/**
	 * Add a reader to the cache.
	 * 
	 * @param reader
	 *            the reader to add
	 */
	private static void factory(final INodeHandler<? extends XMLWritable> reader) {
		for (String tag : reader.understands()) {
			READ_CACHE.put(tag, reader);
		}
		WRITE_CACHE.put(reader.writes(), reader);
	}

	static {
		READ_CACHE = new TreeMap<String, INodeHandler<? extends XMLWritable>>(
				String.CASE_INSENSITIVE_ORDER);
		WRITE_CACHE = new HashMap<Class<? extends XMLWritable>, INodeHandler<? extends XMLWritable>>();
		factory(new SPMapReader());
		factory(new PlayerReader());
		factory(new TileReader());
		factory(new AnimalReader());
		factory(new CacheReader());
		factory(new CentaurReader());
		factory(new DjinnReader());
		factory(new DragonReader());
		factory(new FairyReader());
		factory(new ForestReader());
		factory(new FortressReader());
		factory(new GiantReader());
		factory(new GriffinReader());
		factory(new GroundReader());
		factory(new GroveReader());
		factory(new HillReader());
		factory(new MeadowReader());
		factory(new MineReader());
		factory(new MinotaurReader());
		factory(new MountainReader());
		factory(new OasisReader());
		factory(new OgreReader());
		factory(new PhoenixReader());
		factory(new SandbarReader());
		factory(new ShrubReader());
		factory(new SimurghReader());
		factory(new SphinxReader());
		factory(new TrollReader());
		factory(new TextReader());
		factory(new UnitReader());
		factory(new VillageReader());
		factory(new BattlefieldReader());
		factory(new CaveReader());
		factory(new CityReader());
		factory(new FortificationReader());
		factory(new TownReader());
		factory(new MineralReader());
		factory(new StoneReader());
		factory(new RiverReader());
	}

	/**
	 * @return nothing, as this method always throws IllegalStateException---if
	 *         it gets called, we're adding it to its own cache.
	 */
	@Override
	public List<String> understands() {
		throw new IllegalStateException(
				"ReaderAdapter#understands() should never be called");
	}

	/**
	 * @param <T>
	 *            a type
	 * @param obj
	 *            an object
	 * @param type
	 *            a type
	 * @return obj, if it is assignable to the specified type; throw an
	 *         exception otherwise.
	 */
	@SuppressWarnings("unchecked")
	// The point here is that we *are* checking ... but you can't do that
	// statically in a generic method.
	public static <T> T checkedCast(final Object obj, final Class<T> type) {
		if (type.isAssignableFrom(obj.getClass())) {
			return (T) obj;
		} else {
			throw new IllegalStateException("Wanted " + type.getSimpleName()
					+ ", was " + obj.getClass().getSimpleName());
		}
	}
	/**
	 * Write an instance of the type to a Writer.
	 * 
	 * @param <S> the actual type of the object
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
	public <S extends XMLWritable> void write(final S obj, final Writer writer, final boolean inclusion)
			throws IOException {
		if (WRITE_CACHE.containsKey(obj.getClass())) {
			((INodeHandler<S>) WRITE_CACHE.get(obj.getClass())).write(obj, writer, inclusion);
		} else {
			throw new IllegalArgumentException("Writable type this adapter can't handle");
		}
	}
	/**
	 * @return nothing---this should never be called, so we object with an exception.
	 */
	@Override
	public Class<XMLWritable> writes() {
		throw new IllegalStateException("This should never be called.");
	}
	
	/**
	 * Write an object to a *new* file that it specifies, and return the name of
	 * that file---or a String containing the XML-encoded object for inline
	 * purposes.
	 * 
	 * @param fix the object to write
	 * @return the filename or "filename" written to
	 * @throws IOException on I/O error in writing
	 */
	public String writeForInclusion(final XMLWritable fix) throws IOException {
		if ("string:".equals(fix.getFile())) {
			final XMLEscapingStringWriter writer = new XMLEscapingStringWriter();
			write(fix, writer, true);
			return new StringBuilder(fix.getFile()).append(writer.asText()).toString(); // NOPMD
		} else {
			final Writer writer = new FileWriter(fix.getFile());
			try {
				write(fix, writer, true);
			} finally {
				writer.close();
			}
			return fix.getFile();
		}
	}
}

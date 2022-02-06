package impl.xmlio.yaxml;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import lovelace.util.MalformedXMLException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import common.idreg.IDRegistrar;
import common.idreg.IDFactory;
import common.map.River;
import common.map.IMutablePlayerCollection;
import common.map.PlayerCollection;
import common.map.fixtures.mobile.ProxyFor;
import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.ISkill;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import impl.xmlio.exceptions.UnwantedChildException;
import common.map.fixtures.towns.CommunityStats;
import java.util.HashMap;
import java.util.Map;
import lovelace.util.ThrowingConsumer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Arrays;
import java.util.Collections;

/**
 * A class to hide the complexity of YAXML from callers.
 */
/* protected */ class YAReaderAdapter {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(YAReaderAdapter.class.getName());

	public YAReaderAdapter() {
		this(Warning.getDefaultHandler());
	}

	public YAReaderAdapter(final Warning warning) {
		this(warning, new IDFactory());
	}

	/**
	 * @param warning The Warning instance to use
	 * @param idFactory The factory for ID numbers
	 */
	public YAReaderAdapter(final Warning warning, final IDRegistrar idFactory) {
		players = new PlayerCollection();
		mapReader = new YAMapReader(warning, idFactory, players);
		townReader = new YATownReader(warning, idFactory, players);
		readers = Collections.unmodifiableList(Arrays.asList(
			new YAAdventureReader(warning, idFactory, players),
			new YAExplorableReader(warning, idFactory), new YAGroundReader(warning, idFactory),
			new YAImplementReader(warning, idFactory), mapReader,
			new YAMobileReader(warning, idFactory), new YAPlayerReader(warning, idFactory),
			new YAPortalReader(warning, idFactory), new YAResourcePileReader(warning, idFactory),
			new YAResourceReader(warning, idFactory), new YATerrainReader(warning, idFactory),
			new YATextReader(warning, idFactory), townReader,
			new YAUnitReader(warning, idFactory, players),
			new YAWorkerReader(warning, idFactory, players)));
	}

	/**
	 * The player collection to use.
	 */
	private final IMutablePlayerCollection players;

	/**
	 * The map reader
	 */
	private final YAMapReader mapReader;

	/**
	 * The reader for towns, etc.
	 */
	private final YATownReader townReader;

	/**
	 * The set of readers.
	 */
	private final List<YAReader<?, ?>> readers;

	private final Map<String, YAReader<?, ?>> readerCache = new HashMap<>();

	private final Map<Class<?>, YAReader<?, ?>> writerCache = new HashMap<>();

	/**
	 * Parse an object from XML.
	 *
	 * @throws SPFormatException on SP format problems
	 */
	public Object parse(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, MalformedXMLException {
		// Since all implementations of necessity check the tag's namespace, we leave that
		// to them.
		String tag = element.getName().getLocalPart().toLowerCase();
		// Handle rivers specially.
		if ("river".equals(tag) || "lake".equals(tag)) {
			return mapReader.parseRiver(element, parent);
		}
		// Handle "population" specially.
		if ("population".equals(tag)) {
			return townReader.parseCommunityStats(element, parent, stream);
		}
		if (readerCache.containsKey(tag)) {
			return readerCache.get(tag).read(element, parent, stream);
		}
		for (YAReader<?, ?> reader : readers) {
			if (reader.isSupportedTag(tag)) {
				readerCache.put(tag, reader);
				return reader.read(element, parent, stream);
			}
		}
		throw new UnwantedChildException(parent, element);
	}

	/**
	 * Write a series of rivers.
	 *
	 * TODO: Test this
	 *
	 * TODO: Take Collection, not just Iterable
	 *
	 * @throws IOException on I/O error
	 */
	private void writeAllRivers(final ThrowingConsumer<String, IOException> ostream, final Iterable<River> rivers, final int indent)
			throws IOException {
		for (River river : StreamSupport.stream(rivers.spliterator(), false).sorted()
				.collect(Collectors.toList())) {
			mapReader.writeRiver(ostream, river, indent);
		}
	}

	/**
	 * Write an object to XML.
	 *
	 * TODO: Improve test coverage
	 *
	 * @throws IOException on I/O error
	 * @param ostream The stream to write to
	 * @param obj The object to write
	 * @param indent The current indentation level
	 */
	public void write(final ThrowingConsumer<String, IOException> ostream, final Object obj, final int indent) throws IOException {
		Class<?> cls = obj.getClass();
		if (obj instanceof River) {
			mapReader.writeRiver(ostream, (River) obj, indent);
		} else if (obj instanceof ProxyFor<?>) {
			if (((ProxyFor<?>) obj).getProxied().isEmpty()) {
				throw new IllegalArgumentException(
					"To write a proxy object, it has to be proxying for at least one object.");
			}
			// TODO: Handle proxies in their respective types
			LOGGER.log(Level.SEVERE, "Wanted to write a proxy",
				new IllegalStateException("Shouldn't try to write proxy objects"));
			write(ostream, ((ProxyFor<?>) obj).getProxied().iterator().next(), indent);
			return;
		} else if (obj instanceof IJob) {
			YAWorkerReader.writeJob(ostream, (IJob) obj, indent);
		} else if (obj instanceof ISkill) {
			YAWorkerReader.writeSkill(ostream, (ISkill) obj, indent);
		} else if (obj instanceof CommunityStats) {
			townReader.writeCommunityStats(ostream, (CommunityStats) obj, indent);
		} else if (writerCache.containsKey(cls)) {
			writerCache.get(cls).writeRaw(ostream, obj, indent);
		} else {
			for (YAReader<?, ?> reader : readers) {
				if (reader.canWrite(obj)) {
					writerCache.put(cls, reader);
					reader.writeRaw(ostream, obj, indent);
					return;
				}
			}
			throw new IllegalArgumentException(String.format("After checking %d readers, don't know how to write a %s",
				readers.size(), cls.getName()));
		}
	}
}

package legacy.xmlio.yaxml;

import java.util.Collection;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import legacy.idreg.IDRegistrar;
import legacy.idreg.IDFactory;
import legacy.map.IMutableLegacyPlayerCollection;
import legacy.map.LegacyPlayerCollection;
import legacy.map.River;
import legacy.map.fixtures.mobile.ProxyFor;
import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.ISkill;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.map.fixtures.towns.CommunityStats;

import java.util.HashMap;
import java.util.Map;

import lovelace.util.LovelaceLogger;
import lovelace.util.ThrowingConsumer;

import java.util.List;

/**
 * A class to hide the complexity of YAXML from callers.
 */
/* protected */ class YAReaderAdapter {
	public YAReaderAdapter() {
		this(Warning.getDefaultHandler());
	}

	public YAReaderAdapter(final Warning warning) {
		this(warning, new IDFactory());
	}

	/**
	 * @param warning   The Warning instance to use
	 * @param idFactory The factory for ID numbers
	 */
	public YAReaderAdapter(final Warning warning, final IDRegistrar idFactory) {
		final IMutableLegacyPlayerCollection players = new LegacyPlayerCollection();
		mapReader = new YAMapReader(warning, idFactory, players);
		townReader = new YATownReader(warning, idFactory, players);
		readers = List.of(new YAAdventureReader(warning, idFactory, players), new YAExplorableReader(warning, idFactory), new YAGroundReader(warning, idFactory), new YAImplementReader(warning, idFactory), mapReader, new YAMobileReader(warning, idFactory), new YAPlayerReader(warning, idFactory), new YAPortalReader(warning, idFactory), new YAResourcePileReader(warning, idFactory), new YAResourceReader(warning, idFactory), new YATerrainReader(warning, idFactory), new YATextReader(warning, idFactory), townReader, new YAUnitReader(warning, idFactory, players), new YAWorkerReader(warning, idFactory, players));
	}

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
			throws SPFormatException, XMLStreamException {
		// Since all implementations of necessity check the tag's namespace, we leave that
		// to them.
		final String tag = element.getName().getLocalPart().toLowerCase();
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
		for (final YAReader<?, ?> reader : readers) {
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
	 * @throws IOException on I/O error
	 */
	private static void writeAllRivers(final ThrowingConsumer<String, IOException> ostream, final Collection<River> rivers, final int indent)
			throws IOException {
		for (final River river : rivers.stream().sorted().toList()) {
			YAMapReader.writeRiver(ostream, river, indent);
		}
	}

	/**
	 * Write an object to XML.
	 *
	 * TODO: Improve test coverage
	 *
	 * @param ostream The stream to write to
	 * @param obj     The object to write
	 * @param indent  The current indentation level
	 * @throws IOException on I/O error
	 */
	public void write(final ThrowingConsumer<String, IOException> ostream, final Object obj, final int indent) throws IOException {
		final Class<?> cls = obj.getClass();
		switch (obj) {
			case final River r -> YAMapReader.writeRiver(ostream, r, indent);
			case final ProxyFor<?> p -> {
				if (p.getProxied().isEmpty()) {
					throw new IllegalArgumentException(
							"To write a proxy object, it has to be proxying for at least one object.");
				}
				// TODO: Handle proxies in their respective types
				LovelaceLogger.error(new IllegalStateException("Shouldn't try to write proxy objects"),
						"Wanted to write a proxy");
				write(ostream, p.getProxied().iterator().next(), indent);
				return;
			}
			case final IJob j -> YAWorkerReader.writeJob(ostream, j, indent);
			case final ISkill s -> YAWorkerReader.writeSkill(ostream, s, indent);
			case final CommunityStats cs -> townReader.writeCommunityStats(ostream, cs, indent);
			default -> {
				if (writerCache.containsKey(cls)) {
					writerCache.get(cls).writeRaw(ostream, obj, indent);
				} else {
					for (final YAReader<?, ?> reader : readers) {
						if (reader.canWrite(obj)) {
							writerCache.put(cls, reader);
							reader.writeRaw(ostream, obj, indent);
							return;
						}
					}
					throw new IllegalArgumentException(
							"After checking %d readers, don't know how to write a %s".formatted(
								readers.size(), cls.getName()));
				}
			}
		}
	}
}

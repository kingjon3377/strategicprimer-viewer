package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;
import controller.map.misc.TypesafeXMLEventReader;
import controller.map.readerng.AdventureReader;
import controller.map.readerng.AnimalReader;
import controller.map.readerng.BattlefieldReader;
import controller.map.readerng.CacheReader;
import controller.map.readerng.CaveReader;
import controller.map.readerng.CentaurReader;
import controller.map.readerng.CityReader;
import controller.map.readerng.DjinnReader;
import controller.map.readerng.DragonReader;
import controller.map.readerng.FairyReader;
import controller.map.readerng.ForestReader;
import controller.map.readerng.FortificationReader;
import controller.map.readerng.FortressReader;
import controller.map.readerng.GiantReader;
import controller.map.readerng.GriffinReader;
import controller.map.readerng.GroundReader;
import controller.map.readerng.GroveReader;
import controller.map.readerng.HillReader;
import controller.map.readerng.INodeHandler;
import controller.map.readerng.ImplementReader;
import controller.map.readerng.JobReader;
import controller.map.readerng.MapNGReader;
import controller.map.readerng.MeadowReader;
import controller.map.readerng.MineReader;
import controller.map.readerng.MineralReader;
import controller.map.readerng.MinotaurReader;
import controller.map.readerng.MountainReader;
import controller.map.readerng.OasisReader;
import controller.map.readerng.OgreReader;
import controller.map.readerng.PhoenixReader;
import controller.map.readerng.PlayerReader;
import controller.map.readerng.PortalReader;
import controller.map.readerng.ResourceReader;
import controller.map.readerng.RiverReader;
import controller.map.readerng.SandbarReader;
import controller.map.readerng.ShrubReader;
import controller.map.readerng.SimurghReader;
import controller.map.readerng.SkillReader;
import controller.map.readerng.SphinxReader;
import controller.map.readerng.StatsReader;
import controller.map.readerng.StoneReader;
import controller.map.readerng.TextReader;
import controller.map.readerng.TownReader;
import controller.map.readerng.TrollReader;
import controller.map.readerng.UnitReader;
import controller.map.readerng.VillageReader;
import controller.map.readerng.WorkerReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutableMapNG;
import model.map.IMutablePlayerCollection;
import model.map.PlayerCollection;
import model.map.SPMapNG;
import org.eclipse.jdt.annotation.NonNull;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * The main reader-from-XML class in the 'fluid XML' implementation.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SPFluidReader implements IMapReader, ISPReader, FluidXMLReader {
	/**
	 * The collection of readers, mapped to the tags they read.
	 */
	private final Map<String, FluidXMLReader> readers = new HashMap<>();
	public SPFluidReader() {
		for (INodeHandler<?> reader : Arrays.asList(new AdventureReader(),
				new AnimalReader(), new BattlefieldReader(), new CacheReader(),
				new CaveReader(), new CentaurReader(), new CityReader(),
				new DjinnReader(), new DragonReader(), new FairyReader(),
				new ForestReader(), new FortificationReader(), new FortressReader(),
				new GiantReader(), new GriffinReader(), new GroundReader(),
				new GroveReader(), new HillReader(), new ImplementReader(),
				new JobReader(), new MapNGReader(), new MeadowReader(), new MineralReader(),
				new MineReader(), new MinotaurReader(), new MountainReader(),
				new OasisReader(), new OgreReader(), new PhoenixReader(),
				new PlayerReader(), new PortalReader(), new ResourceReader(),
				new SandbarReader(), new RiverReader(), new ShrubReader(),
				new SimurghReader(), new SkillReader(), new SphinxReader(),
				new StatsReader(), new StoneReader(), new TextReader(), new TownReader(),
				new TrollReader(), new UnitReader(), new VillageReader(),
				new WorkerReader())) {
			for (final String tag : reader.understands()) {
				readers.put(tag, reader::parse);
			}
		}
	}
	/**
	 * @param <T>     A supertype of the object the XML represents
	 * @param file    the file we're reading from
	 * @param istream the stream to read from
	 * @param type    the type of the object the caller wants
	 * @param warner  the Warning instance to use for warnings
	 * @return the wanted object
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException  on SP XML format error
	 */
	@Override
	public <@NonNull T> T readXML(final File file, final Reader istream,
								  final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final TypesafeXMLEventReader reader = new TypesafeXMLEventReader(istream);
		final IteratorWrapper<XMLEvent> eventReader =
				new IteratorWrapper<>(new IncludingIterator(file, reader));
		final IMutablePlayerCollection players = new PlayerCollection();
		final IDFactory idFactory = new IDFactory();
		for (final XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				final Object retval = readSPObject(
						NullCleaner.assertNotNull(event.asStartElement()),
						eventReader, players, warner, idFactory);
				if (type.isAssignableFrom(retval.getClass())) {
					//noinspection unchecked
					return (T) retval;
				} else {
					throw new IllegalStateException("Reader produced different type than we expected");
				}
			}
		}
		throw new XMLStreamException("XML stream didn't contain a start element");
	}

	/**
	 * @param file   the file to read from
	 * @param warner a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws IOException        on I/O error
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException  on SP format problems
	 */
	@Override
	public IMutableMapNG readMap(final File file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		try (final Reader istream = new FileReader(file)) {
			return readMap(file, istream, warner);
		}
	}

	/**
	 * @param file    the file we're reading from
	 * @param istream the stream to read from
	 * @param warner  a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException  on SP format problems
	 */
	@Override
	public IMutableMapNG readMap(final File file, final Reader istream,
								 final Warning warner)
			throws XMLStreamException, SPFormatException {
		return readXML(file, istream, SPMapNG.class, warner);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactXMLReader";
	}

	@Override
	public Object readSPObject(final StartElement element,
							   final IteratorWrapper<XMLEvent> stream,
							   final IMutablePlayerCollection players,
							   final Warning warner,
							   final IDFactory idFactory)
			throws SPFormatException, IllegalArgumentException {
		final String namespace = element.getName().getNamespaceURI();
		final String tag = element.getName().getLocalPart().toLowerCase();
		if (namespace.isEmpty() || NAMESPACE.equals(namespace)) {
			if (readers.containsKey(tag)) {
				return readers.get(tag)
							   .readSPObject(element, stream, players, warner, idFactory);
			} else {
				throw new UnsupportedTagException(element);
			}
		} else {
			throw new UnsupportedTagException(element);
		}
	}
}

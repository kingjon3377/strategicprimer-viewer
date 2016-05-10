package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;
import controller.map.misc.TypesafeXMLEventReader;
import controller.map.readerng.CityReader;
import controller.map.readerng.FortificationReader;
import controller.map.readerng.FortressReader;
import controller.map.readerng.INodeHandler;
import controller.map.readerng.JobReader;
import controller.map.readerng.MapNGReader;
import controller.map.readerng.PlayerReader;
import controller.map.readerng.RiverReader;
import controller.map.readerng.SkillReader;
import controller.map.readerng.StatsReader;
import controller.map.readerng.TownReader;
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
import java.util.function.IntFunction;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasMutableImage;
import model.map.IMutableMapNG;
import model.map.IMutablePlayerCollection;
import model.map.PlayerCollection;
import model.map.SPMapNG;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Phoenix;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Sphinx;
import model.map.fixtures.mobile.Troll;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import org.eclipse.jdt.annotation.NonNull;
import util.IteratorWrapper;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static util.NullCleaner.assertNotNull;

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
		for (INodeHandler<?> reader : Arrays.asList(new CityReader(),
				new FortificationReader(), new FortressReader(),
				new JobReader(), new MapNGReader(),
				new PlayerReader(),
				new RiverReader(),
				new SkillReader(),
				new StatsReader(), new TownReader(),
				new UnitReader(), new VillageReader(),
				new WorkerReader())) {
			for (final String tag : reader.understands()) {
				readers.put(tag, reader::parse);
			}
		}
		readers.put("adventure", FluidExplorableHandler::readAdventure);
		readers.put("portal", FluidExplorableHandler::readPortal);
		readers.put("cave", FluidExplorableHandler::readCave);
		readers.put("battlefield", FluidExplorableHandler::readBattlefield);
		readers.put("ground", FluidTerrainHandler::readGround);
		readers.put("forest", FluidTerrainHandler::readForest);
		addSimpleFixtureReader("hill", Hill::new);
		readers.put("mountain", FluidTerrainHandler::readMountain);
		addSimpleFixtureReader("oasis", Oasis::new);
		addSimpleFixtureReader("sandbar", Sandbar::new);
		addSimpleFixtureReader("djinn", Djinn::new);
		addSimpleFixtureReader("griffin", Griffin::new);
		addSimpleFixtureReader("minotaur", Minotaur::new);
		addSimpleFixtureReader("ogre", Ogre::new);
		addSimpleFixtureReader("phoenix", Phoenix::new);
		addSimpleFixtureReader("simurgh", Simurgh::new);
		addSimpleFixtureReader("sphinx", Sphinx::new);
		addSimpleFixtureReader("troll", Troll::new);
		readers.put("animal", FluidMobileHandler::readAnimal);
		readers.put("centaur", FluidMobileHandler::readCentaur);
		readers.put("dragon", FluidMobileHandler::readDragon);
		readers.put("fairy", FluidMobileHandler::readFairy);
		readers.put("giant", FluidMobileHandler::readGiant);
		readers.put("text", FluidExplorableHandler::readTextFixture);
		readers.put("implement", FluidResourceHandler::readImplement);
		readers.put("resource", FluidResourceHandler::readResource);
		readers.put("cache", FluidResourceHandler::readCache);
		readers.put("grove", FluidResourceHandler::readGrove);
		readers.put("orchard", FluidResourceHandler::readOrchard);
		readers.put("meadow", FluidResourceHandler::readMeadow);
		readers.put("field", FluidResourceHandler::readField);
		readers.put("mine", FluidResourceHandler::readMine);
		readers.put("mineral", FluidResourceHandler::readMineral);
		readers.put("shrub", FluidResourceHandler::readShrub);
		readers.put("stone", FluidResourceHandler::readStone);
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
						assertNotNull(event.asStartElement()),
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

	/**
	 * Create a reader for a simple object having only an ID number and maybe an image,
	 * and add this reader to our collection.
	 * @param tag the tag this class should be instantiated from
	 * @param constr the constructor to create an object of the class. Must take the ID
	 *                  number in its constructor, and nothing else.
	 */
	private void addSimpleFixtureReader(final String tag, final IntFunction<?> constr) {
		readers.put(tag, (element, stream, players, warner, idFactory) -> {
			requireTag(element, tag);
			spinUntilEnd(assertNotNull(element.getName()), stream);
			final Object retval = constr.apply(getOrGenerateID(element, warner, idFactory));
			if (retval instanceof HasMutableImage) {
				((HasMutableImage) retval).setImage(getAttribute(element, "image", ""));
			}
			return retval;
		});
	}
}

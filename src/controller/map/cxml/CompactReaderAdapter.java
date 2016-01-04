package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.IMutablePlayerCollection;
import model.map.Player;
import model.map.River;
import model.map.TerrainFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.ExplorableFixture;
import model.map.fixtures.explorable.Portal;
import model.map.fixtures.mobile.MobileFixture;
import model.map.fixtures.mobile.ProxyFor;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.resources.HarvestableFixture;
import model.map.fixtures.towns.ITownFixture;
import model.viewer.TileTypeFixture;
import util.IteratorWrapper;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;

/**
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public final class CompactReaderAdapter {
	/**
	 * Singleton object.
	 */
	public static final CompactReaderAdapter ADAPTER = new CompactReaderAdapter();

	/**
	 * Singleton constructor.
	 */
	private CompactReaderAdapter() {
		// Singleton.
	}
	/**
	 * The set of readers.
	 */
	private static final Set<CompactReader<?>> READERS = new HashSet<>();
	/**
	 * Register a reader.
	 */
	public static void register(final CompactReader<?> reader) {
		READERS.add(reader);
	}
	/**
	 * Parse an object from XML.
	 * @param element   the element we're immediately dealing with
	 * @param stream    the stream from which to read more elements
	 * @param players   the PlayerCollecton to use when needed
	 * @param warner    the Warning instance if warnings need to be issued
	 * @param idFactory the ID factory to get IDs from
	 * @return the object encoded by the XML
	 * @throws SPFormatException on SP format problems
	 */
	public static Object parse(final StartElement element, final IteratorWrapper<XMLEvent> stream,
	                           final IMutablePlayerCollection players, final Warning warner,
	                           final IDFactory idFactory) throws SPFormatException {
		String tag = element.getName().getLocalPart();
		// Handle rivers specially.
		if ("river".equals(tag) || "lake".equals(tag)) {
			return CompactMapNGReader.parseRiver(element, warner);
		}
		for (CompactReader<?> reader : READERS) {
			if (reader.isSupportedTag(tag)) {
				return reader.read(element, stream, players, warner, idFactory);
			}
		}
		throw new IllegalStateException("Unhandled tag " + tag);
	}

	/**
	 * @param <T>  the type
	 * @param type the type
	 * @return a reader for that type
	 */
	@SuppressWarnings("unchecked")
	private static <T extends IFixture> CompactReader<T> getFixtureReader(
			                                                                     final
			                                                                     Class<T> type) {
		final CompactReader<T> reader; // NOPMD
		if (TerrainFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactTerrainReader.READER;
		} else if (ITownFixture.class.isAssignableFrom(type)) {
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
		} else if (Worker.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactWorkerReader.READER;
		} else if (AdventureFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactAdventureReader.READER;
		} else if (Portal.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactPortalReader.READER;
		} else if (ExplorableFixture.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactExplorableReader.READER;
		} else if (Implement.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactImplementReader.READER;
		} else if (ResourcePile.class.isAssignableFrom(type)) {
			reader = (CompactReader<T>) CompactResourcePileReader.READER;
		} else {
			throw new IllegalStateException("Unhandled type " + type.getName());
		}
		return reader;
	}

	/**
	 * Write an object to XML.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  the current indentation level.
	 * @throws IOException on I/O problems
	 */
	@SuppressWarnings("unchecked")
	public static void write(final Appendable ostream, final Object obj,
	                         final int indent) throws IOException {
		@SuppressWarnings("rawtypes") // NOPMD
		final CompactReader reader; // NOPMD
		if (obj instanceof IMapNG) {
			reader = CompactMapNGReader.READER;
		} else if (obj instanceof River) {
			CompactMapNGReader.writeRiver(ostream, (River) obj, indent);
			return; // NOPMD
		} else if (obj instanceof RiverFixture) {
			CompactMapNGReader.writeAllRivers(ostream, (RiverFixture) obj, indent);
			return; // NOPMD
		} else if (obj instanceof Job) {
			CompactWorkerReader.writeJob(ostream, (Job) obj, indent);
			return; // NOPMD
		} else if (obj instanceof Skill) {
			CompactWorkerReader.writeSkill(ostream, (Skill) obj, indent);
			return; // NOPMD
		} else if (obj instanceof Player) {
			reader = CompactPlayerReader.READER;
		} else if (obj instanceof TileTypeFixture) {
			// Skip it.
			return;
		} else if (obj instanceof ProxyFor) {
			final Iterator<?> iter = ((ProxyFor<?>) obj).getProxied().iterator();
			if (iter.hasNext()) {
				final Object proxied = iter.next();
				assert proxied != null;
				TypesafeLogger.getLogger(CompactReaderAdapter.class).log(Level.SEVERE,
						"Wanted to write a proxy",
						new IllegalArgumentException("Wanted to write a proxy object"));
				write(ostream, proxied, indent);
				return;
			} else {
				throw new IllegalStateException("Don't know how to write this type (a proxy " +

						                                "not proxying any objects)");
			}
		} else if (obj instanceof IFixture) {
			reader =
					getFixtureReader(NullCleaner.assertNotNull(((IFixture) obj)
							                                           .getClass()));
		} else {
			throw new IllegalStateException("Don't know how to write this type");
		}
		reader.write(ostream, obj, indent);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactReaderAdapter";
	}
}

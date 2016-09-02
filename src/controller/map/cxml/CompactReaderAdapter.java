package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.River;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.mobile.ProxyFor;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;

/**
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated CompactXML is deprecated in favor of FluidXML
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Deprecated
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
	private static final Set<CompactReader<@NonNull ?>> READERS =
			new HashSet<>(Arrays.asList(CompactAdventureReader.READER,
					CompactExplorableReader.READER, CompactGroundReader.READER,
					CompactImplementReader.READER, CompactMapNGReader.READER,
					CompactMobileReader.READER, CompactPlayerReader.READER,
					CompactPortalReader.READER, CompactResourcePileReader.READER,
					CompactResourceReader.READER, CompactTerrainReader.READER,
					CompactTextReader.READER, CompactTownReader.READER,
					CompactUnitReader.READER, CompactWorkerReader.READER));

	/**
	 * Parse an object from XML.
	 * @param element   the element we're immediately dealing with
	 * @param parent	the parent tag
	 * @param stream    the stream from which to read more elements
	 * @param players   the PlayerCollection to use when needed
	 * @param warner    the Warning instance if warnings need to be issued
	 * @param idFactory the ID factory to get IDs from
	 * @return the object encoded by the XML
	 * @throws SPFormatException on SP format problems
	 */
	public static Object parse(final StartElement element,
							final QName parent,
							final Iterable<XMLEvent> stream,
							final IMutablePlayerCollection players,
							final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		// Since all impls of necessity check tag's namespace, we leave that to them.
		final String tag = NullCleaner.assertNotNull(element.getName().getLocalPart());
		// Handle rivers specially.
		if ("river".equals(tag) || "lake".equals(tag)) {
			return CompactMapNGReader.parseRiver(element, parent, warner);
		}
		for (final CompactReader<@NonNull ?> reader : READERS) {
			if (reader.isSupportedTag(tag)) {
				return reader.read(element, parent, players, warner, idFactory, stream);
			}
		}
		throw new UnwantedChildException(parent, element);
	}

	/**
	 * Write an object to XML. TODO: Improve test coverage
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  the current indentation level.
	 * @throws IOException on I/O problems
	 */
	@SuppressWarnings("unchecked")
	public static void write(final Appendable ostream, final Object obj,
							final int indent) throws IOException {
		if (obj instanceof River) {
			CompactMapNGReader.writeRiver(ostream, (River) obj, indent);
		} else if (obj instanceof RiverFixture) {
			CompactMapNGReader.writeAllRivers(ostream, (RiverFixture) obj, indent);
		} else if (obj instanceof Job) {
			CompactWorkerReader.writeJob(ostream, (Job) obj, indent);
		} else if (obj instanceof Skill) {
			CompactWorkerReader.writeSkill(ostream, (Skill) obj, indent);
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
				throw new IllegalStateException("Don't know how to write this type (a " +
														"proxy not proxying any " +
														"objects)");
			}
		} else {
			for (final CompactReader<@NonNull ?> reader : READERS) {
				if (reader.canWrite(obj)) {
					reader.writeRaw(ostream, obj, indent);
					return;
				}
			}
			throw new IllegalArgumentException("After checking " + READERS.size() +
													" readers, don't know how to write a " +
													obj.getClass().getSimpleName());
		}
	}
}

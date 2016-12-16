package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.PlayerCollection;
import model.map.River;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.mobile.ProxyFor;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
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
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class YAReaderAdapter {
	/**
	 * The set of readers.
	 */
	private final Collection<YAReader<?>> readers;
	/**
	 * The map reader.
	 */
	private final YAMapReader mapReader;
	/**
	 * No-arg constructor.
	 */
	public YAReaderAdapter() {
		this(Warning.DEFAULT, new IDFactory());
	}
	/**
	 * @param warning the Warning instance to use
	 * @param idFactory the factory for ID numbers
	 */
	public YAReaderAdapter(final Warning warning, final IDRegistrar idFactory) {
		final IMutablePlayerCollection players = new PlayerCollection();
		mapReader = new YAMapReader(warning, idFactory, players);
		readers = new HashSet<>(Arrays.asList(new YAAdventureReader(warning, idFactory,
																		   players),
				new YAExplorableReader(warning, idFactory),
				new YAGroundReader(warning, idFactory),
				new YAImplementReader(warning, idFactory),
				new YAMapReader(warning, idFactory, players),
				new YAMobileReader(warning, idFactory),
				new YAPlayerReader(warning, idFactory),
				new YAPortalReader(warning, idFactory),
				new YAResourcePileReader(warning, idFactory),
				new YAResourceReader(warning, idFactory),
				new YATerrainReader(warning, idFactory),
				new YATextReader(warning, idFactory),
				new YATownReader(warning, idFactory, players),
				new YAUnitReader(warning, idFactory, players),
				new YAWorkerReader(warning, idFactory)));
	}

	/**
	 * Parse an object from XML.
	 *
	 * @param element   the element we're immediately dealing with
	 * @param parent    the parent tag
	 * @param stream    the stream from which to read more elements
	 * @return the object encoded by the XML
	 * @throws SPFormatException on SP format problems
	 */
	public Object parse(final StartElement element,
							   final QName parent,
							   final Iterable<XMLEvent> stream)
			throws SPFormatException {
		// Since all implementations of necessity check tag's namespace, we leave that
		// to them.
		final String tag = element.getName().getLocalPart();
		// Handle rivers specially.
		if ("river".equals(tag) || "lake".equals(tag)) {
			return mapReader.parseRiver(element, parent);
		}
		return readers.stream().filter(yar -> yar.isSupportedTag(tag)).findFirst()
				.orElseThrow(() -> new UnwantedChildException(parent, element))
				.read(element, parent, stream);
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
	public void write(final Appendable ostream, final Object obj,
							 final int indent) throws IOException {
		if (obj instanceof River) {
			YAMapReader.writeRiver(ostream, (River) obj, indent);
		} else if (obj instanceof RiverFixture) {
			writeAllRivers(ostream, (RiverFixture) obj, indent);
		} else if (obj instanceof ProxyFor) {
			// TODO: Handle proxies in their respective types
			final Iterator<?> iter = ((ProxyFor<?>) obj).getProxied().iterator();
			if (iter.hasNext()) {
				final Object proxied = iter.next();
				assert proxied != null;
				TypesafeLogger.getLogger(YAReaderAdapter.class).log(Level.SEVERE,
						"Wanted to write a proxy",
						new IllegalArgumentException("Wanted to write a proxy object"));
				write(ostream, proxied, indent);
				return;
			} else if (obj instanceof IJob) {
				YAWorkerReader.writeJob(ostream, (IJob) obj, indent);
			} else if (obj instanceof ISkill) {
				YAWorkerReader.writeSkill(ostream, (ISkill) obj, indent);
			} else {
				throw new IllegalStateException("Don't know how to write this type (a " +
														"proxy not proxying any " +
														"objects)");
			}
		} else {
			final String msg = String.format(
					"After checking %d readers, don't know how to write a %s",
					Integer.valueOf(readers.size()), obj.getClass().getSimpleName());
			readers.stream().filter(yar -> yar.canWrite(obj)).findFirst()
					.orElseThrow(() -> new IllegalArgumentException(msg))
					.writeRaw(ostream, obj, indent);
		}
	}

	/**
	 * Write a series of rivers. TODO: test this
	 *
	 * @param ostream the stream to write to
	 * @param iter    a series of rivers to write
	 * @param indent  the indentation level
	 * @throws IOException on I/O error
	 */
	private static void writeAllRivers(final Appendable ostream,
									   final Iterable<River> iter, final int indent)
			throws IOException {
		for (final River river : iter) {
			YAMapReader.writeRiver(ostream, river, indent);
		}
	}
}

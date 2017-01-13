package controller.map.yaxml;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasPortrait;
import model.map.IFixture;
import model.map.IPlayerCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import util.Warning;

import static java.util.Collections.unmodifiableList;

/**
 * A reader for tiles, including rivers.
 *
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
public final class YAUnitReader extends YAAbstractReader<IUnit> {
	/**
	 * The Warning instance to use.
	 */
	private final Warning warner;
	/**
	 * The map's growing collection of players.
	 */
	private final IPlayerCollection players;
	/**
	 * The tag used for a unit.
	 */
	private static final String UNIT_TAG = "unit";
	/**
	 * List of readers we'll try sub-tags on.
	 */
	private final List<YAReader<? extends IFixture>> readers;

	/**
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 * @param playerCollection the map's collection of players
	 */
	public YAUnitReader(final Warning warning, final IDRegistrar idRegistrar,
						final IPlayerCollection playerCollection) {
		super(warning, idRegistrar);
		warner = warning;
		players = playerCollection;
		readers = unmodifiableList(
				Arrays.asList(new YAMobileReader(warning, idRegistrar),
						new YAResourceReader(warning, idRegistrar),
						new YATerrainReader(warning, idRegistrar),
						new YATextReader(warning, idRegistrar),
						new YAWorkerReader(warning, idRegistrar),
						new YAResourcePileReader(warning, idRegistrar),
						new YAImplementReader(warning, idRegistrar)));
	}

	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the empty
	 * string.
	 *
	 * @param element the current element
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private String parseKind(final StartElement element) throws SPFormatException {
		try {
			final String retval =
					getParamWithDeprecatedForm(element, "kind", "type");
			if (retval.isEmpty()) {
				warner.warn(new MissingPropertyException(element, "kind"));
			}
			return retval;
		} catch (final MissingPropertyException except) {
			warner.warn(except);
			return "";
		}
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @return the parsed unit
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public IUnit read(final StartElement element, final QName parent,
					  final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, UNIT_TAG);
		requireNonEmptyParameter(element, "name", false);
		requireNonEmptyParameter(element, "owner", false);
		final Unit retval =
				new Unit(players.getPlayer(getIntegerParameter(element, "owner", -1)),
								parseKind(element), getParameter(element, "name", ""),
								getOrGenerateID(element));
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		final StringBuilder orders = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (event.isStartElement() &&
						isSupportedNamespace(event.asStartElement().getName())) {
				if ("orders".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					parseOrders(event.asStartElement(), retval,
							stream);
				} else if ("results".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					parseResults(event.asStartElement(), retval,
							stream);
				} else {
					retval.addMember(parseChild(event.asStartElement(),
							element.getName(), stream));
				}
			} else if (event.isCharacters()) {
				orders.append(event.asCharacters().getData());
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		final String tempOrders = orders.toString().trim();
		if (!tempOrders.isEmpty()) {
			retval.setOrders(-1, tempOrders);
		}
		return retval;
	}

	/**
	 * Parse orders for a unit for a specified turn.
	 *
	 * @param element the orders element
	 * @param unit    the unit to whom these orders are directed
	 * @param stream  the stream of further tags.
	 * @throws SPFormatException on SP format problem
	 */
	private static void parseOrders(final StartElement element,
							 final IUnit unit,
							 final Iterable<XMLEvent> stream) throws SPFormatException {
		final int turn = getIntegerParameter(element, "turn", -1);
		final StringBuilder builder = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (event.isCharacters()) {
				builder.append(event.asCharacters().getData().trim());
			} else if (event.isStartElement()) {
				throw new UnwantedChildException(element.getName(),
														event.asStartElement());
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		unit.setOrders(turn, builder.toString().trim());
	}

	/**
	 * Parse results for a unit for a specified turn.
	 *
	 * @param element the results element
	 * @param unit    the unit to whom these orders are directed
	 * @param stream  the stream of further tags.
	 * @throws SPFormatException on SP format problem
	 */
	private static void parseResults(final StartElement element,
							  final IUnit unit,
							  final Iterable<XMLEvent> stream) throws SPFormatException {
		final int turn = getIntegerParameter(element, "turn", -1);
		final StringBuilder builder = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (event.isCharacters()) {
				builder.append(event.asCharacters().getData().trim());
			} else if (event.isStartElement()) {
				throw new UnwantedChildException(element.getName(),
														event.asStartElement());
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		unit.setResults(turn, builder.toString().trim());
	}

	/**
	 * Parse what should be a TileFixture from the XML.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @return the parsed fixture.
	 * @throws SPFormatException on SP format problem
	 */
	private UnitMember parseChild(final StartElement element, final QName parent,
								  final Iterable<XMLEvent> stream)
			throws SPFormatException {
		final String name = element.getName().getLocalPart();
		for (final YAReader<? extends IFixture> item : readers) {
			if (item.isSupportedTag(name)) {
				final IFixture retval = item.read(element, parent,
						stream);
				if (retval instanceof UnitMember) {
					return (UnitMember) retval;
				} else {
					throw new UnwantedChildException(parent, element);
				}
			}
		}
		throw new UnwantedChildException(parent, element);
	}

	/**
	 * @param tag a tag
	 * @return whether it's one we can read
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return UNIT_TAG.equalsIgnoreCase(tag);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final IUnit obj, final int indent)
			throws IOException {
		writeTag(ostream, "unit", indent);
		writeProperty(ostream, "owner", obj.getOwner().getPlayerId());
		writeNonemptyProperty(ostream, "kind", obj.getKind());
		writeNonemptyProperty(ostream, "name", obj.getName());
		writeProperty(ostream, "id", obj.getID());
		writeImageXML(ostream, obj);
		if (obj instanceof HasPortrait) {
			writeNonemptyProperty(ostream, "portrait", ((HasPortrait) obj).getPortrait());

		}
		if (obj.iterator().hasNext() || !obj.getAllOrders().isEmpty() ||
					!obj.getAllResults().isEmpty()) {
			finishParentTag(ostream);
			for (final Map.Entry<Integer, String> pair : obj.getAllOrders().entrySet()) {
				writeOrders(ostream, "orders", pair.getKey().intValue(),
						pair.getValue(), indent + 1);
			}
			for (final Map.Entry<Integer, String> pair : obj.getAllResults().entrySet()) {
				writeOrders(ostream, "results", pair.getKey().intValue(),
						pair.getValue(), indent + 1);
			}
			for (final UnitMember member : obj) {
				writeChild(ostream, member, indent + 1);
			}
			closeTag(ostream, indent, "unit");
		} else {
			closeLeafTag(ostream);
		}
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Unit;
	}
	/**
	 * Write orders or results.
	 * @param ostream the stream to write to
	 * @param tag the tag to use
	 * @param turn which turn these are for
	 * @param value the text to write
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 */
	private static void writeOrders(final Appendable ostream, final String tag,
									final int turn, final String value,
									final int indent) throws IOException {
		if (value.isEmpty()) {
			return;
		}
		writeTag(ostream, tag, indent);
		if (turn >= 0) {
			writeProperty(ostream, "turn", turn);
		}
		ostream.append('>');
		ostream.append(simpleQuote(value));
		closeTag(ostream, 0, tag);
	}
	/**
	 * @param child a child object to write
	 * @param ostream the stream to write it to
	 * @param indent  how far indented we are already
	 * @throws IOException on I/O error in writing
	 */
	private void writeChild(final Appendable ostream, final UnitMember child,
							final int indent) throws IOException {
		final String msg = String.format(
				"After checking %d readers, don't know how to write a %s",
				Integer.valueOf(readers.size()), child.getClass().getSimpleName());
		readers.stream().filter(reader -> reader.canWrite(child)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(msg))
				.writeRaw(ostream, child, indent);
	}
}

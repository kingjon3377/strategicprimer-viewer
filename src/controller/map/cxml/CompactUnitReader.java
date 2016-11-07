package controller.map.cxml;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasPortrait;
import model.map.IFixture;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import org.eclipse.jdt.annotation.NonNull;
import util.EqualsAny;
import util.LineEnd;
import util.NullCleaner;
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
 * @deprecated CompactXML is deprecated in favor of FluidXML
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Deprecated
public final class CompactUnitReader extends AbstractCompactReader<IUnit> {
	/**
	 * The tag used for a unit.
	 */
	private static final String UNIT_TAG = "unit";

	/**
	 * List of readers we'll try sub-tags on.
	 */
	private final List<CompactReader<? extends IFixture>> readers;

	/**
	 * Singleton object.
	 */
	public static final CompactReader<IUnit> READER = new CompactUnitReader();

	/**
	 * Singleton.
	 */
	private CompactUnitReader() {
		final List<@NonNull CompactReader<@NonNull ? extends IFixture>> temp =
				new ArrayList<>();
		temp.add(CompactMobileReader.READER);
		temp.add(CompactResourceReader.READER);
		temp.add(CompactTerrainReader.READER);
		temp.add(CompactTextReader.READER);
		temp.add(CompactTownReader.READER);
		temp.add(CompactWorkerReader.READER);
		readers = NullCleaner.assertNotNull(unmodifiableList(temp));
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent	the parent tag
	 *@param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @param stream    the stream to read more elements from
	 * @return the parsed unit
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public Unit read(final StartElement element,
					 final QName parent, final IMutablePlayerCollection players,
					 final Warning warner, final IDRegistrar idFactory,
					 final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, UNIT_TAG);
		requireNonEmptyParameter(element, "name", false, warner);
		requireNonEmptyParameter(element, "owner", false, warner);
		final Unit retval = new Unit(
											players.getPlayer(
													getIntegerParameter(element, "owner",
															-1)), parseKind(element,
				warner), getParameter(element, "name", ""),
											getOrGenerateID(element, warner, idFactory));
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		final StringBuilder orders = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (event.isStartElement() && EqualsAny.equalsAny(
					NullCleaner.assertNotNull(
							event.asStartElement().getName().getNamespaceURI()),
					ISPReader.NAMESPACE, XMLConstants.NULL_NS_URI)) {
				if ("orders".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					parseOrders(event.asStartElement(), element.getName(), retval,
							warner, stream);
				} else if ("results".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					parseResults(event.asStartElement(), element.getName(), retval,
							warner, stream);
				} else {
					retval.addMember(parseChild(
							NullCleaner.assertNotNull(event.asStartElement()),
							element.getName(), stream, players, idFactory, warner));
				}
			} else if (event.isCharacters()) {
				orders.append(event.asCharacters().getData());
			} else if (event.isEndElement() &&
							element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		final String tempOrders = orders.toString().trim();
		if (!tempOrders.isEmpty()) {
			retval.setOrders(-1, NullCleaner.assertNotNull(tempOrders));
		}
		return retval;
	}

	/**
	 * Parse orders for a unit for a specified turn.
	 * @param element the orders element
	 * @param parent the parent tag
	 * @param unit the unit to whom these orders are directed
	 * @param warner the Warning instance to use for warnings
	 * @param stream the stream of further tags.
	 * @throws SPFormatException on SP format problem
	 */
	private void parseOrders(final StartElement element, final QName parent,
							 final Unit unit, final Warning warner,
							 final Iterable<XMLEvent> stream) throws SPFormatException {
		final int turn = getIntegerParameter(element, "turn", -1);
		final StringBuilder builder = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (event.isCharacters()) {
				builder.append(event.asCharacters().getData().trim());
			} else if (event.isStartElement()) {
				throw new UnwantedChildException(element.getName(),
														event.asStartElement());
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		unit.setOrders(turn, builder.toString().trim());
	}
	/**
	 * Parse results for a unit for a specified turn.
	 * @param element the results element
	 * @param parent the parent tag
	 * @param unit the unit to whom these orders are directed
	 * @param warner the Warning instance to use for warnings
	 * @param stream the stream of further tags.
	 * @throws SPFormatException on SP format problem
	 */
	private void parseResults(final StartElement element, final QName parent,
							 final Unit unit, final Warning warner,
							 final Iterable<XMLEvent> stream) throws SPFormatException {
		final int turn = getIntegerParameter(element, "turn", -1);
		final StringBuilder builder = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (event.isCharacters()) {
				builder.append(event.asCharacters().getData().trim());
			} else if (event.isStartElement()) {
				throw new UnwantedChildException(element.getName(),
														event.asStartElement());
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		unit.setResults(turn, builder.toString().trim());
	}

	/**
	 * Parse what should be a TileFixture from the XML.
	 *
	 * @param element   the XML element to parse
	 * @param parent	the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param idFactory the ID factory to generate IDs with
	 * @param warner    the Warning instance to use for warnings
	 * @return the parsed fixture.
	 * @throws SPFormatException on SP format problem
	 */
	private UnitMember parseChild(final StartElement element,
								final QName parent,
								final Iterable<XMLEvent> stream,
								final IMutablePlayerCollection players,
								final IDRegistrar idFactory,
								final Warning warner) throws SPFormatException {
		final String name = NullCleaner.assertNotNull(element.getName().getLocalPart());
		for (final CompactReader<? extends IFixture> item : readers) {
			if (item.isSupportedTag(name)) {
				final IFixture retval = item.read(element, parent, players,
						warner, idFactory, stream);
				if (retval instanceof UnitMember) {
					return (UnitMember) retval;
				} else {
					throw new UnwantedChildException(new QName(element.getName()
																	.getNamespaceURI(),
																	UNIT_TAG), element);
				}
			}
		}
		throw new UnwantedChildException(new QName(element.getName().getNamespaceURI(),
														UNIT_TAG), element);
	}

	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the empty
	 * string.
	 *
	 * @param element the current element
	 * @param warner  the Warning instance to use
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private static String parseKind(final StartElement element,
									final Warning warner) throws SPFormatException {
		try {
			final String retval =
					getParamWithDeprecatedForm(element, "kind", "type", warner);
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
	public void
	write(final Appendable ostream, final IUnit obj, final int indent)
			throws IOException {
		writeTag(ostream, "unit", indent);
		ostream.append(" owner=\"");
		ostream.append(Integer.toString(obj.getOwner().getPlayerId()));
		if (!obj.getKind().isEmpty()) {
			ostream.append("\" kind=\"");
			ostream.append(obj.getKind());
		}
		if (!obj.getName().isEmpty()) {
			ostream.append("\" name=\"");
			ostream.append(obj.getName());
		}
		ostream.append("\" id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append('"');
		ostream.append(imageXML(obj));
		if (obj instanceof HasPortrait) {
			ostream.append(portraitXML((HasPortrait) obj));
		}
		if (obj.iterator().hasNext() || !obj.getAllOrders().isEmpty() ||
					!obj.getAllResults().isEmpty()) {
			ostream.append('>');
			ostream.append(LineEnd.LINE_SEP);
			for (final Map.Entry<Integer, String> entry : obj.getAllOrders().entrySet()) {
				if (entry.getValue().trim().isEmpty()) {
					continue;
				}
				indent(ostream, indent + 1);
				ostream.append("<orders");
				if (entry.getKey().intValue() >= 0) {
					ostream.append(" turn=\"");
					ostream.append(Integer.toString(entry.getKey().intValue()));
					ostream.append('"');
				}
				ostream.append('>');
				// FIXME: Ensure, and test, that XML special characters are escaped
				ostream.append(entry.getValue().trim());
				ostream.append("</orders>");
				ostream.append(LineEnd.LINE_SEP);
			}
			for (final Map.Entry<Integer, String> entry : obj.getAllResults()
																  .entrySet()) {
				if (entry.getValue().trim().isEmpty()) {
					continue;
				}
				indent(ostream, indent + 1);
				ostream.append("<results");
				if (entry.getKey().intValue() >= 0) {
					ostream.append(" turn=\"");
					ostream.append(Integer.toString(entry.getKey().intValue()));
					ostream.append('"');
				}
				ostream.append('>');
				// FIXME: Ensure, and test, that XML special characters are escaped
				ostream.append(entry.getValue().trim());
				ostream.append("</orders>");
				ostream.append(LineEnd.LINE_SEP);
			}
			for (final UnitMember member : obj) {
				CompactReaderAdapter.write(ostream, member, indent + 1);
			}
			indent(ostream, indent);
			ostream.append("</unit>");
			ostream.append(LineEnd.LINE_SEP);
		} else {
			ostream.append(" />");
			ostream.append(LineEnd.LINE_SEP);
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

}

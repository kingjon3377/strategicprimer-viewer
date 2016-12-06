package controller.map.cxml;

import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.IMutablePlayerCollection;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import util.EqualsAny;
import util.LineEnd;
import util.Warning;

import static java.util.Collections.unmodifiableList;
import static util.NullCleaner.assertNotNull;

/**
 * A reader for new-API maps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
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
public final class CompactMapNGReader extends AbstractCompactReader<IMapNG> {
	/**
	 * Singleton instance.
	 */
	public static final CompactReader<IMapNG> READER = new CompactMapNGReader();
	/**
	 * List of readers we'll try sub-tags on.
	 */
	private final List<CompactReader<? extends TileFixture>> readers;

	/**
	 * Singleton.
	 */
	private CompactMapNGReader() {
		final List<CompactReader<? extends TileFixture>> list =
				new ArrayList<>(Arrays.asList(CompactMobileReader.READER,
						CompactResourceReader.READER,
						CompactTerrainReader.READER, CompactTextReader.READER,
						CompactTownReader.READER, CompactGroundReader.READER,
						CompactAdventureReader.READER,
						CompactPortalReader.READER,
						CompactExplorableReader.READER));
		readers = assertNotNull(unmodifiableList(list));
	}

	/**
	 * Add a fixture to a point in a map, accounting for the special cases.
	 *
	 * @param map   the map
	 * @param point where to add the fixture
	 * @param fix   the fixture to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addFixture(final IMutableMapNG map, final Point point,
								   final TileFixture fix) {
		if (fix instanceof Ground) {
			final Ground ground = (Ground) fix;
			final Ground oldGround = map.getGround(point);
			if (oldGround == null) {
				map.setGround(point, ground);
			} else if (ground.isExposed() && !oldGround.isExposed()) {
				map.setGround(point, ground);
				map.addFixture(point, oldGround);
			} else if (!oldGround.equals(ground)) {
				map.addFixture(point, ground);
			}
		} else if (fix instanceof Forest) {
			final Forest forest = (Forest) fix;
			final Forest oldForest = map.getForest(point);
			if (oldForest == null) {
				map.setForest(point, forest);
			} else if (!oldForest.equals(forest)) {
				map.addFixture(point, forest);
			}
		} else if (fix instanceof Mountain) {
			// We shouldn't get here, since the parser above doesn't even
			// instantiate Mountains, but we don't want to lose data if I
			// forget.
			map.setMountainous(point, true);
		} else if (fix instanceof RiverFixture) {
			// Similarly
			for (final River river : (RiverFixture) fix) {
				map.addRivers(point, river);
			}
		} else {
			// We shouldn't get here either, since the parser above handles
			// other fixtures directly, but again we don't want to lose data if
			// I forget.
			map.addFixture(point, fix);
		}
	}

	/**
	 * @param stream a stream of XMLEvents
	 * @param parent the parent tag
	 * @return the first start-element in the stream
	 * @throws SPFormatException if no start element in stream
	 */
	private static StartElement getFirstStartElement(final Iterable<XMLEvent> stream,
													 final StartElement parent)
			throws SPFormatException {
		final StartElement retval = StreamSupport
											.stream(stream.spliterator(), false)
											.filter(XMLEvent::isStartElement)
											.map(XMLEvent::asStartElement)
											.filter(elem -> isSupportedNamespace(
													elem.getName()))
											.findFirst()
											.orElseThrow(
													() -> new MissingChildException
																  (parent));
		return assertNotNull(retval);
	}

	/**
	 * Write a newline if needed.
	 *
	 * @param writer  the writer to write to
	 * @param needEOL whether we need a newline.
	 * @throws IOException on I/O error
	 */
	private static void eolIfNeeded(final boolean needEOL,
									final Appendable writer) throws IOException {
		if (needEOL) {
			writer.append(LineEnd.LINE_SEP);
		}
	}

	/**
	 * Parse a river from XML. The caller is now responsible for getting past the closing
	 * tag.
	 *
	 * @param element the element to parse
	 * @param parent  the parent tag
	 * @param warner  the Warning instance to use as needed
	 * @return the parsed river
	 * @throws SPFormatException on SP format problem
	 */
	public static River parseRiver(final StartElement element, final QName parent,
								   final Warning warner) throws SPFormatException {
		requireTag(element, parent, "river", "lake");
		if ("lake".equalsIgnoreCase(element.getName().getLocalPart())) {
			return River.Lake;
		} else {
			requireNonEmptyParameter(element, "direction", true, warner);
			return River.getRiver(getParameter(element, "direction"));
		}
	}

	/**
	 * Write a river.
	 *
	 * @param ostream the stream we're writing to
	 * @param obj     the river to write
	 * @param indent  the indentation level
	 * @throws IOException on I/O error
	 */
	public static void writeRiver(final Appendable ostream, final River obj,
								  final int indent) throws IOException {
		if (River.Lake == obj) {
			writeTag(ostream, "lake", indent);
		} else {
			writeTag(ostream, "river", indent);
			writeProperty(ostream, "direction", obj.getDescription());
		}
		closeLeafTag(ostream);
	}

	/**
	 * Write a series of rivers. TODO: test this
	 *
	 * @param ostream the stream to write to
	 * @param iter    a series of rivers to write
	 * @param indent  the indentation level
	 * @throws IOException on I/O error
	 */
	public static void writeAllRivers(final Appendable ostream,
									  final Iterable<River> iter, final int indent)
			throws IOException {
		for (final River river : iter) {
			writeRiver(ostream, river, indent);
		}
	}

	/**
	 * Read a map from XML.
	 *
	 * @param element   the element we're parsing
	 * @param parent    the parent tag
	 * @param players   The collection to put players in
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs.
	 * @param stream    the source to read more elements from     @return the parsed map
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public IMutableMapNG read(final StartElement element,
							  final QName parent, final IMutablePlayerCollection players,
							  final Warning warner, final IDRegistrar idFactory,
							  final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "map", "view");
		final int currentTurn;
		final StartElement mapTag;
		final String outerTag = assertNotNull(element.getName().getLocalPart());
		if ("view".equalsIgnoreCase(outerTag)) {
			currentTurn = getIntegerParameter(element, "current_turn");
			mapTag = getFirstStartElement(stream, element);
			if (!"map".equalsIgnoreCase(mapTag.getName().getLocalPart())) {
				throw new UnwantedChildException(assertNotNull(element.getName()),
														mapTag);
			}
		} else if ("map".equalsIgnoreCase(outerTag)) {
			currentTurn = 0;
			mapTag = element;
		} else {
			throw new UnwantedChildException(new QName("xml"), element);
		}
		final MapDimensions dimensions =
				new MapDimensions(getIntegerParameter(mapTag, "rows"),
										 getIntegerParameter(mapTag, "columns"),
										 getIntegerParameter(mapTag, "version"));
		final Deque<QName> tagStack = new LinkedList<>();
		tagStack.push(element.getName());
		tagStack.push(mapTag.getName());
		final IMutableMapNG retval = new SPMapNG(dimensions, players, currentTurn);
		final Point nullPoint = PointFactory.point(-1, -1);
		Point point = nullPoint;
		for (final XMLEvent event : stream) {
			if (event.isStartElement() &&
						isSupportedNamespace(event.asStartElement().getName())) {
				final StartElement current = event.asStartElement();
				final String type = assertNotNull(current.getName().getLocalPart());
				if ("player".equalsIgnoreCase(type)) {
					retval.addPlayer(CompactPlayerReader.READER.read(current,
							tagStack.peek(), players, warner, idFactory, stream));
				} else if ("row".equalsIgnoreCase(type)) {
					tagStack.push(current.getName());
					// Deliberately ignore "row"s.
					continue;
				} else if ("tile".equalsIgnoreCase(type)) {
					if (!nullPoint.equals(point)) {
						throw new UnwantedChildException(tagStack.peek(), current);
					}
					tagStack.push(current.getName());
					point = PointFactory.point(
							getIntegerParameter(current, "row"),
							getIntegerParameter(current, "column"));
					// Since tiles have sometimes been *written* without "kind",
					// then failed to load, be liberal in what we accept here
					if (hasParameter(current, "kind")
								|| hasParameter(current, "type")) {
						retval.setBaseTerrain(point,
								TileType.getTileType(
										getParamWithDeprecatedForm(current, "kind",
												"type", warner)));
					} else {
						//noinspection ObjectAllocationInLoop
						warner.warn(new MissingPropertyException(current, "kind"));
					}
				} else if (EqualsAny.equalsAny(type, ISPReader.FUTURE)) {
					tagStack.push(current.getName());
					//noinspection ObjectAllocationInLoop
					warner.warn(new UnsupportedTagException(current));
				} else if (nullPoint.equals(point)) {
					// fixture outside tile
					throw new UnwantedChildException(tagStack.peek(), current);
				} else if ("lake".equalsIgnoreCase(type)
								   || "river".equalsIgnoreCase(type)) {
					retval.addRivers(point,
							parseRiver(current, tagStack.peek(), warner));
					spinUntilEnd(assertNotNull(current.getName()),
							stream);
				} else if ("ground".equalsIgnoreCase(type)) {
					addFixture(retval, point,
							CompactGroundReader.READER
									.read(current, tagStack.peek(), players, warner,
											idFactory, stream));
				} else if ("forest".equalsIgnoreCase(type)) {
					addFixture(retval, point, CompactTerrainReader.READER
													  .read(current, tagStack.peek(),
															  players, warner, idFactory,
															  stream));
				} else if ("mountain".equalsIgnoreCase(type)) {
					tagStack.push(current.getName());
					retval.setMountainous(point, true);
				} else {
					final TileFixture fix =
							parseFixture(current, tagStack.peek(), stream, players,
									idFactory, warner);
					if ((fix instanceof StoneDeposit) &&
								(StoneKind.Laterite ==
										 ((StoneDeposit) fix).stone()) &&
								(TileType.Jungle != retval.getBaseTerrain(point))) {
						//noinspection ObjectAllocationInLoop
						warner.warn(new UnsupportedPropertyException(current,
																			"laterite"));
					}
					retval.addFixture(point, fix);
				}
			} else if (event.isEndElement()) {
				if (!tagStack.isEmpty() &&
							tagStack.peek().equals(event.asEndElement().getName())) {
					tagStack.pop();
				}
				if (element.getName().equals(event.asEndElement().getName())) {
					break;
				} else if ("tile".equalsIgnoreCase(event.asEndElement()
														   .getName().getLocalPart())) {
					point = PointFactory.point(-1, -1);
				}
			} else if (event.isCharacters()) {
				final String data =
						assertNotNull(event.asCharacters().getData().trim());
				if (!data.isEmpty()) {
					//noinspection ObjectAllocationInLoop
					retval.addFixture(point, new TextFixture(data, -1));
				}
			}
		}
		if (hasParameter(mapTag, "current_player")) {
			retval.setCurrentPlayer(players.getPlayer(getIntegerParameter(mapTag,
													"current_player")));
		} else if (hasParameter(element, "current_player")) {
			retval.setCurrentPlayer(players.getPlayer(getIntegerParameter(element,
													"current_player")));
		}
		return retval;
	}

	/**
	 * Parse what should be a TileFixture from the XML.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param idFactory the ID factory to generate IDs with
	 * @param warner    the Warning instance to use for warnings
	 * @return the parsed fixture.
	 * @throws SPFormatException on SP format problem
	 */
	private TileFixture parseFixture(final StartElement element,
									 final QName parent,
									 final Iterable<XMLEvent> stream,
									 final IMutablePlayerCollection players,
									 final IDRegistrar idFactory,
									 final Warning warner) throws SPFormatException {
		final String name = assertNotNull(element.getName().getLocalPart());
		for (final CompactReader<? extends TileFixture> item : readers) {
			if (item.isSupportedTag(name)) {
				return item.read(element, parent, players, warner, idFactory, stream);
			}
		}
		throw new UnwantedChildException(new QName(element.getName().getNamespaceURI(),
														  "tile"), element);
	}

	/**
	 * @param obj     a map
	 * @param ostream the stream to write it to
	 * @param indent  how far indented we are already
	 * @throws IOException on I/O error in writing
	 */
	@Override
	public void write(final Appendable ostream, final IMapNG obj, final int indent)
			throws IOException {
		writeTag(ostream, "view", indent);
		writeProperty(ostream, "current_player",
				Integer.toString(obj.getCurrentPlayer().getPlayerId()));
		writeProperty(ostream, "current_turn", Integer.toString(obj.getCurrentTurn()));
		finishParentTag(ostream);
		writeTag(ostream, "map", indent + 1);
		final MapDimensions dim = obj.dimensions();
		writeProperty(ostream, "version", Integer.toString(dim.version));
		writeProperty(ostream, "rows", Integer.toString(dim.rows));
		writeProperty(ostream, "columns", Integer.toString(dim.cols));
		finishParentTag(ostream);
		for (final Player player : obj.players()) {
			CompactPlayerReader.READER.write(ostream, player, indent + 2);
		}
		for (int i = 0; i < dim.rows; i++) {
			boolean rowEmpty = true;
			for (int j = 0; j < dim.cols; j++) {
				final Point point = PointFactory.point(i, j);
				final TileType terrain = obj.getBaseTerrain(point);
				if (!obj.isLocationEmpty(point)) {
					if (rowEmpty) {
						rowEmpty = false;
						writeTag(ostream, "row", indent + 2);
						writeProperty(ostream, "index", Integer.toString(i));
						finishParentTag(ostream);
					}
					writeTag(ostream, "tile", indent + 3);
					writeProperty(ostream, "row", Integer.toString(i));
					writeProperty(ostream, "column", Integer.toString(j));
					if (TileType.NotVisible != terrain) {
						writeProperty(ostream, "kind", terrain.toXML());
					}
					ostream.append('>');
					boolean needEOL = true;
					if (obj.isMountainous(point)) {
						eolIfNeeded(true, ostream);
						needEOL = false;
						writeTag(ostream, "mountain", indent + 4);
						closeLeafTag(ostream);
					}
					for (final River river : obj.getRivers(point)) {
						eolIfNeeded(needEOL, ostream);
						needEOL = false;
						writeRiver(ostream, river, indent + 4);
					}
					final Ground ground = obj.getGround(point);
					if (ground != null) {
						eolIfNeeded(needEOL, ostream);
						needEOL = false;
						CompactReaderAdapter.write(ostream, ground, indent + 4);
					}
					final Forest forest = obj.getForest(point);
					if (forest != null) {
						eolIfNeeded(needEOL, ostream);
						needEOL = false;
						CompactReaderAdapter.write(ostream, forest, indent + 4);
					}
					for (final TileFixture fixture : obj.getOtherFixtures(point)) {
						eolIfNeeded(needEOL, ostream);
						needEOL = false;
						CompactReaderAdapter.write(ostream, fixture, indent + 4);
					}
					if (!needEOL) {
						indent(ostream, indent + 3);
					}
					closeTag(ostream,0,  "tile");
				}
			}
			if (!rowEmpty) {
				closeTag(ostream, indent + 2, "row");
			}
		}
		closeTag(ostream, indent + 1, "map");
		closeTag(ostream, indent, "view");
	}

	/**
	 * @param tag a tag
	 * @return whether this class supports it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "map".equalsIgnoreCase(tag) || "view".equalsIgnoreCase(tag);
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof IMapNG;
	}
}

package controller.map.cxml;

import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
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
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import util.EqualsAny;
import util.IteratorWrapper;
import util.Warning;

import static java.util.Collections.unmodifiableList;
import static util.NullCleaner.assertNotNull;

/**
 * A reader for new-API maps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
public final class CompactMapNGReader extends AbstractCompactReader<IMapNG> {
	/**
	 * Singleton instance.
	 */
	public static final CompactReader<IMapNG> READER = new CompactMapNGReader();
	private static final Pattern EXCEPT_PATTERN =
			Pattern.compile("^Wanted [^ ]*, was [^ " +
									"]*$");
	/**
	 * List of readers we'll try subtags on.
	 */
	private final List<AbstractCompactReader<? extends TileFixture>> readers;

	/**
	 * Singleton.
	 */
	private CompactMapNGReader() {
		final List<AbstractCompactReader<? extends TileFixture>> list =
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
	 * Read a map from XML.
	 *
	 * @param element   the element we're parsing
	 * @param stream    the source to read more elements from
	 * @param players   The collection to put players in
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed map
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public IMutableMapNG read(final StartElement element,
							  final IteratorWrapper<XMLEvent> stream,
							  final IMutablePlayerCollection players,
							  final Warning warner,
							  final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "map", "view");
		final int currentTurn;
		final StartElement mapTag;
		final Location outerLoc = assertNotNull(element.getLocation());
		final String outerTag = assertNotNull(element.getName().getLocalPart());
		if ("view".equalsIgnoreCase(outerTag)) {
			currentTurn = getIntegerParameter(element, "current_turn");
			mapTag = getFirstStartElement(stream, element);
			if (!"map".equalsIgnoreCase(mapTag.getName().getLocalPart())) {
				throw new UnwantedChildException(element.getName(), mapTag);
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
		final IMutableMapNG retval = new SPMapNG(dimensions, players, currentTurn);
		final Point nullPoint = PointFactory.point(-1, -1);
		Point point = nullPoint;
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement current = event.asStartElement();
				final String type = current.getName().getLocalPart();
				final Location currentLoc = assertNotNull(current.getLocation());
				if (type == null) {
					continue;
				} else if ("player".equalsIgnoreCase(type)) {
					retval.addPlayer(CompactPlayerReader.READER.read(current,
							stream, players, warner, idFactory));
				} else if ("row".equalsIgnoreCase(type)) {
					// Deliberately ignore "row"s.
					continue;
				} else if ("tile".equalsIgnoreCase(type)) {
					if (!nullPoint.equals(point)) {
						throw new UnwantedChildException(new QName("tile"), current);
					}
					point = PointFactory.point(
							getIntegerParameter(current, "row"),
							getIntegerParameter(current, "column"));
					// Since tiles have sometimes been *written* without "kind",
					// then failed to load, be liberal in what we accept here
					if (hasParameter(current, "kind")
								|| hasParameter(current, "type")) {
						retval.setBaseTerrain(point, TileType
															 .getTileType(
																	 getParamWithDeprecatedForm(
																			 current,
																			 "kind",
																			 "type",
																			 warner)));
					} else {
						warner.warn(new MissingPropertyException(current, "kind"));
					}
				} else if (EqualsAny.equalsAny(type, ISPReader.FUTURE)) {
					warner.warn(new UnsupportedTagException(current));
				} else if (nullPoint.equals(point)) {
					// fixture outside tile
					throw new UnwantedChildException(mapTag.getName(), current);
				} else if ("lake".equalsIgnoreCase(type)
								   || "river".equalsIgnoreCase(type)) {
					retval.addRivers(point,
							parseRiver(current, warner));
					spinUntilEnd(assertNotNull(current.getName()),
							stream);
				} else if ("ground".equalsIgnoreCase(type)) {
					addFixture(retval, point,
							CompactGroundReader.READER.read(current, stream, players,
									warner, idFactory));
				} else if ("forest".equalsIgnoreCase(type)) {
					addFixture(retval, point, CompactTerrainReader.READER.read(current,
							stream, players, warner, idFactory));
				} else if ("mountain".equalsIgnoreCase(type)) {
					retval.setMountainous(point, true);
				} else {
					final String mapName =
							assertNotNull(mapTag.getName().getLocalPart());
					try {
						retval.addFixture(point, parseFixture(current, stream,
								players, idFactory, warner));
					} catch (final UnwantedChildException except) {
						if ("unknown".equals(except.getTag())) {
							throw new UnwantedChildException(mapTag.getName(), except);
						} else {
							throw except;
						}
					} catch (final IllegalStateException except) {
						if (EXCEPT_PATTERN.matcher(except.getMessage()).matches()) {
							throw new UnwantedChildException(mapTag.getName(), current,
									                                except);
						} else {
							throw except;
						}
					}
				}
			} else if (event.isEndElement()) {
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
					retval.addFixture(point, new TextFixture(data, -1));
				}
			}
		}
		if (hasParameter(mapTag, "current_player")) {
			retval.setCurrentPlayer(players
											.getPlayer(getIntegerParameter(mapTag,
													"current_player")));
		} else if (hasParameter(element, "current_player")) {
			retval.setCurrentPlayer(players
											.getPlayer(getIntegerParameter(element,
													"current_player")));
		}
		return retval;
	}

	/**
	 * Add a fixture to a point in a map, accounting for the special cases.
	 *
	 * @param map   the map
	 * @param point where to add the fixture
	 * @param fix   the fixture to add
	 */
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
				// TODO: Should we do some ordering of Ground other than
				// the order they are in the XML?
				map.addFixture(point, ground);
			}
		} else if (fix instanceof Forest) {
			final Forest forest = (Forest) fix;
			final Forest oldForest = map.getForest(point);
			if (oldForest == null) {
				map.setForest(point, forest);
			} else if (!oldForest.equals(forest)) {
				// TODO: Should we do some ordering of Forests other
				// than the order they are in the XML?
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
	 * Parse what should be a TileFixture from the XML.
	 *
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param idFactory the ID factory to generate IDs with
	 * @param warner    the Warning instance to use for warnings
	 * @return the parsed fixture.
	 * @throws SPFormatException on SP format problem
	 */
	private TileFixture parseFixture(final StartElement element,
									 final IteratorWrapper<XMLEvent> stream,
									 final IMutablePlayerCollection players,
									 final IDFactory idFactory,
									 final Warning warner) throws SPFormatException {
		final String name = assertNotNull(element.getName().getLocalPart());
		for (final CompactReader<? extends TileFixture> item : readers) {
			if (item.isSupportedTag(name)) {
				return item.read(element, stream, players, warner, idFactory);
			}
		}
		throw new UnwantedChildException(new QName("tile"), element);
	}

	/**
	 * @param stream a stream of XMLEvents
	 * @param parent the parent tag
	 * @return the first start-element in the stream
	 * @throws SPFormatException if no start element in stream
	 */
	private static StartElement getFirstStartElement(
			                                                final Iterable<XMLEvent>
					                                                stream,
			                                                final StartElement parent)
			throws SPFormatException {
		return StreamSupport.stream(stream.spliterator(), false)
				       .filter(XMLEvent::isStartElement).findFirst()
				       .orElseThrow(() -> new MissingChildException(parent))
				       .asStartElement();
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
		indent(ostream, indent);
		ostream.append("<view current_player=\"");
		ostream.append(Integer.toString(obj.getCurrentPlayer().getPlayerId()));
		ostream.append("\" current_turn=\"");
		ostream.append(Integer.toString(obj.getCurrentTurn()));
		ostream.append("\">\n");
		indent(ostream, indent + 1);
		final MapDimensions dim = obj.dimensions();
		ostream.append("<map version=\"");
		ostream.append(Integer.toString(dim.version));
		ostream.append("\" rows=\"");
		ostream.append(Integer.toString(dim.rows));
		ostream.append("\" columns=\"");
		ostream.append(Integer.toString(dim.cols));
		ostream.append("\">\n");
		for (final Player player : obj.players()) {
			CompactPlayerReader.READER.write(ostream, player, indent + 2);
		}
		for (int i = 0; i < dim.rows; i++) {
			boolean rowEmpty = true;
			for (int j = 0; j < dim.cols; j++) {
				final Point point = PointFactory.point(i, j);
				if ((TileType.NotVisible != obj.getBaseTerrain(point))
							|| obj.isMountainous(point)
							|| (obj.getGround(point) != null)
							|| (obj.getForest(point) != null)
							|| obj.getOtherFixtures(point).iterator().hasNext()) {
					if (rowEmpty) {
						rowEmpty = false;
						indent(ostream, indent + 2);
						ostream.append("<row index=\"");
						ostream.append(Integer.toString(i));
						ostream.append("\">\n");
					}
					indent(ostream, indent + 3);
					ostream.append("<tile row=\"");
					ostream.append(Integer.toString(i));
					ostream.append("\" column=\"");
					ostream.append(Integer.toString(j));
					if (TileType.NotVisible != obj.getBaseTerrain(point)) {
						ostream.append("\" kind=\"");
						ostream.append(obj.getBaseTerrain(point).toXML());
					}
					ostream.append("\">");
					boolean needeol = true;
					if (obj.isMountainous(point)) {
						eolIfNeeded(true, ostream);
						needeol = false;
						indent(ostream, indent + 4);
						ostream.append("<mountain />\n");
					}
					for (final River river : obj.getRivers(point)) {
						eolIfNeeded(needeol, ostream);
						needeol = false;
						writeRiver(ostream, river, indent + 4);
					}
					final Ground ground = obj.getGround(point);
					if (ground != null) {
						eolIfNeeded(needeol, ostream);
						needeol = false;
						CompactReaderAdapter.write(ostream, ground, indent + 4);
					}
					final Forest forest = obj.getForest(point);
					if (forest != null) {
						eolIfNeeded(needeol, ostream);
						needeol = false;
						CompactReaderAdapter.write(ostream, forest, indent + 4);
					}
					for (final TileFixture fixture : obj.getOtherFixtures(point)) {
						eolIfNeeded(needeol, ostream);
						needeol = false;
						CompactReaderAdapter.write(ostream, fixture, indent + 4);
					}
					if (!needeol) {
						indent(ostream, indent + 3);
					}
					ostream.append("</tile>\n");
				}
			}
			if (!rowEmpty) {
				indent(ostream, indent + 2);
				ostream.append("</row>\n");
			}
		}
		indent(ostream, indent + 1);
		ostream.append("</map>\n");
		indent(ostream, indent);
		ostream.append("</view>\n");
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
	 * Write a newline if needed.
	 *
	 * @param writer  the writer to write to
	 * @param needeol whether we need a newline.
	 * @throws IOException on I/O error
	 */
	private static void eolIfNeeded(final boolean needeol,
									final Appendable writer) throws IOException {
		if (needeol) {
			writer.append('\n');
		}
	}

	/**
	 * Parse a river from XML. The caller is now responsible for getting past the closing
	 * tag.
	 *
	 * @param element the element to parse
	 * @param warner  the Warning instance to use as needed
	 * @return the parsed river
	 * @throws SPFormatException on SP format problem
	 */
	public static River parseRiver(final StartElement element,
								   final Warning warner) throws SPFormatException {
		requireTag(element, "river", "lake");
		if ("lake".equalsIgnoreCase(element.getName().getLocalPart())) {
			return River.Lake; // NOPMD
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
		for (int i = 0; i < indent; i++) {
			ostream.append('\t');
		}
		if (River.Lake == obj) {
			ostream.append("<lake />");
		} else {
			ostream.append("<river direction=\"");
			ostream.append(obj.getDescription());
			ostream.append("\" />");
		}
		ostream.append('\n');
	}

	/**
	 * Write a series of rivers.
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
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactMapNGReader";
	}
}

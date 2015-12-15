package controller.map.readerng;

import static util.NullCleaner.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
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
import util.NullCleaner;
import util.Pair;
import util.Warning;

/**
 * A reader to read new-API maps from XML and turn them into XML.
 *
 * TODO: changesets
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class MapNGReader implements INodeHandler<@NonNull IMapNG> {
	/**
	 * The reader to use to parse players.
	 */
	private static final PlayerReader PLAYER_READER = new PlayerReader();
	/**
	 * The reader to use to parse rivers.
	 */
	private static final RiverReader RIVER_READER = new RiverReader();
	/**
	 * The reader to use to parse Ground.
	 */
	private static final GroundReader GROUND_READER = new GroundReader();
	/**
	 * The reader to use to parse fortests.
	 */
	private static final ForestReader FOREST_READER = new ForestReader();
	/**
	 * The tags we know how to deal with.
	 */
	private static final List<String> tags;
	static {
		// FIXME: Use Arrays.asList
		final List<String> temp = new ArrayList<>();
		temp.add("map");
		temp.add("view");
		tags = assertNotNull(Collections.unmodifiableList(temp));
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return assertNotNull(Collections.unmodifiableList(tags));
	}

	/**
	 * Parse a map (which might be a 'map' or a 'view' tag) from XML.
	 *
	 * @param element
	 *            the element to start parsing with
	 * @param stream
	 *            the XML tags we haven't gotten to yet
	 * @param players
	 *            the collection of players
	 * @param warner
	 *            the Warning instance to use for warnings
	 * @param factory
	 *            the factory to use to register ID numbers and generate new
	 *            ones
	 * @return the produced map
	 * @throws SPFormatException
	 *             on format problems
	 */
	@Override
	public IMutableMapNG parse(final StartElement element,
			final Iterable<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory factory) throws SPFormatException {
		final int currentTurn;
		final StartElement mapTag;
		final Location outerLoc = assertNotNull(element.getLocation());
		final String outerTag = assertNotNull(element.getName().getLocalPart());
		if ("view".equalsIgnoreCase(element.getName().getLocalPart())) {
			currentTurn = XMLHelper.parseInt(
					XMLHelper.getAttribute(element, "current_turn"), outerLoc);
			mapTag = getFirstStartElement(stream, outerLoc.getLineNumber());
			if (!"map".equals(mapTag.getName().getLocalPart())) {
				throw new UnwantedChildException(outerTag, assertNotNull(mapTag
						.getName().getLocalPart()), mapTag.getLocation()
						.getLineNumber());
			}
		} else if ("map".equalsIgnoreCase(outerTag)) {
			currentTurn = 0;
			mapTag = element;
		} else {
			throw new UnwantedChildException("xml", assertNotNull(outerTag),
					outerLoc.getLineNumber());
		}
		final Location mapTagLocation = assertNotNull(mapTag.getLocation());
		final MapDimensions dimensions = new MapDimensions(
				XMLHelper.parseInt(XMLHelper.getAttribute(mapTag, "rows"),
						mapTagLocation),
				XMLHelper.parseInt(XMLHelper.getAttribute(mapTag, "columns"),
						mapTagLocation),
				XMLHelper.parseInt(XMLHelper.getAttribute(mapTag, "version"),
						mapTagLocation));
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
					retval.addPlayer(PLAYER_READER.parse(current, stream,
							players, warner, factory));
				} else if ("row".equalsIgnoreCase(type)) {
					// Deliberately ignore "row"s.
					continue;
				} else if ("tile".equalsIgnoreCase(type)) {
					if (!nullPoint.equals(point)) {
						throw new UnwantedChildException("tile", type,
								currentLoc.getLineNumber());
					}
					point = PointFactory.point(XMLHelper.parseInt(
							XMLHelper.getAttribute(current, "row"), currentLoc),
							XMLHelper.parseInt(
									XMLHelper.getAttribute(current, "column"),
									currentLoc));
					// Since tiles have been known to be *written* without
					// "kind" and then fail to load, let's be liberal in what we
					// accept here, since we can.
					if (XMLHelper.hasAttribute(current, "kind")
							|| XMLHelper.hasAttribute(current, "type")) {
						retval.setBaseTerrain(point,
								TileType.getTileType(XMLHelper
										.getAttributeWithDeprecatedForm(current,
												"kind", "type", warner)));
					} else {
						warner.warn(new MissingPropertyException(type, "kind",
								currentLoc.getLineNumber()));
					}
				} else if (EqualsAny.equalsAny(type, ISPReader.FUTURE)) {
					warner.warn(new UnsupportedTagException(type, currentLoc
							.getLineNumber()));
				} else if (nullPoint.equals(point)) {
					// fixture outside tile
					throw new UnwantedChildException("map", type,
							currentLoc.getLineNumber());
				} else if ("lake".equalsIgnoreCase(type)
						|| "river".equalsIgnoreCase(type)) {
					retval.addRivers(point, RIVER_READER.parse(current, stream,
							players, warner, factory));
				} else if ("ground".equalsIgnoreCase(type)) {
					final Ground ground = GROUND_READER.parse(current, stream,
							players, warner, factory);
					addFixture(retval, point, ground);
				} else if ("forest".equalsIgnoreCase(type)) {
					final Forest forest = FOREST_READER.parse(current, stream,
							players, warner, factory);
					addFixture(retval, point, forest);
				} else if ("mountain".equalsIgnoreCase(type)) {
					retval.setMountainous(point, true);
				} else {
					try {
						retval.addFixture(point, ReaderAdapter.checkedCast(
								ReaderAdapter.ADAPTER.parse(current, stream,
										players, warner, factory),
								TileFixture.class));
					} catch (final UnwantedChildException except) {
						if ("unknown".equals(except.getTag())) {
							throw new UnwantedChildException(
									assertNotNull(mapTag.getName()
											.getLocalPart()),
									except.getChild(),
									currentLoc.getLineNumber());
						} else {
							throw except;
						}
					} catch (final IllegalStateException except) {
						if (except.getMessage().matches(
								"^Wanted [^ ]*, was [^ ]*$")) {
							final UnwantedChildException nexcept =
									new UnwantedChildException(
											assertNotNull(mapTag.getName()
													.getLocalPart()),
											assertNotNull(current.getName()
													.getLocalPart()),
											currentLoc.getLineNumber());
							nexcept.initCause(except);
							throw nexcept;
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
		if (XMLHelper.hasAttribute(mapTag, "current_player")) {
			retval.setCurrentPlayer(players.getPlayer(XMLHelper.parseInt(
					XMLHelper.getAttribute(mapTag, "current_player"),
					mapTagLocation)));
		} else if (XMLHelper.hasAttribute(element, "current_player")) {
			retval.setCurrentPlayer(players.getPlayer(XMLHelper.parseInt(
					XMLHelper.getAttribute(element, "current_player"),
					mapTagLocation)));
		}
		return retval;
	}
	/**
	 * Add a fixture to a point in a map, accounting for the special cases.
	 * @param map the map
	 * @param point where to add the fixture
	 * @param fix the fixture to add
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
	 * @param stream
	 *            a stream of XMLEvents
	 * @param line
	 *            the line the parent tag is on
	 * @throws SPFormatException
	 *             if no start element in stream
	 * @return the first start-element in the stream
	 */
	private static StartElement getFirstStartElement(
			final Iterable<XMLEvent> stream, final int line)
			throws SPFormatException {
		return StreamSupport.stream(stream.spliterator(), false).filter(event -> event.isStartElement()).findFirst()
				       .orElseThrow(() -> new MissingChildException("map", line)).asStartElement();
	}

	/**
	 * Create an intermediate representation of the map to convert it to XML.
	 *
	 * TODO: changesets
	 *
	 * @param obj
	 *            the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final IMapNG obj) {
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("view", Pair.of(
						"current_player", assertNotNull(Integer.toString(obj
								.getCurrentPlayer().getPlayerId()))), Pair.of(
						"current_turn",
						assertNotNull(Integer.toString(obj.getCurrentTurn()))));
		final MapDimensions dim = obj.dimensions();
		final SPIntermediateRepresentation mapTag =
				new SPIntermediateRepresentation("map", Pair.of("version",
						assertNotNull(Integer.toString(dim.version))), Pair.of(
						"rows", assertNotNull(Integer.toString(dim.rows))),
						Pair.of("columns",
								assertNotNull(Integer.toString(dim.cols))));
		retval.addChild(mapTag);
		for (final Player player : obj.players()) {
			mapTag.addChild(PLAYER_READER.write(player));
		}
		for (int i = 0; i < dim.rows; i++) {
			final SPIntermediateRepresentation row =
					new SPIntermediateRepresentation("row", Pair.of("index",
							assertNotNull(Integer.toString(i))));
			for (int j = 0; j < dim.cols; j++) {
				final Point point = PointFactory.point(i, j);
				if (TileType.NotVisible != obj.getBaseTerrain(point)
						|| obj.isMountainous(point)
						|| obj.getGround(point) != null
						|| obj.getForest(point) != null
						|| obj.getOtherFixtures(point).iterator().hasNext()) {
					mapTag.addChild(row);
					row.addChild(writeTile(obj, point));
				}
			}
		}
		return retval;
	}

	/**
	 * Create an intermediate representation of a tile to write to a writer.
	 *
	 * @param map
	 *            the map to write from
	 * @param point
	 *            the point to write about
	 * @return an intermediate representation
	 */
	private static SPIntermediateRepresentation writeTile(final IMapNG map,
			final Point point) {
		// We can safely assume that an empty retval is not called for.
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("tile");
		retval.addIntegerAttribute("row", point.row);
		retval.addIntegerAttribute("column", point.col);
		if (TileType.NotVisible != map.getBaseTerrain(point)) {
			retval.addAttribute("kind", map.getBaseTerrain(point).toXML());
		}
		if (map.isMountainous(point)) {
			retval.addChild(new SPIntermediateRepresentation("mountain"));
		}
		for (final River river : map.getRivers(point)) {
			retval.addChild(RIVER_READER.write(river));
		}
		retval.addChild(writeFixture(map.getGround(point)));
		retval.addChild(writeFixture(map.getForest(point)));
		for (final TileFixture fixture : map.getOtherFixtures(point)) {
			retval.addChild(writeFixture(fixture));
		}
		return retval;
	}

	/**
	 * "Write" a TileFixture by creating its SPIR.
	 *
	 * @param fixture
	 *            the fixture to write. Nullable to simplify writing a tile's
	 *            ground and forest.
	 * @return an intermediate representation of it
	 */
	private static SPIntermediateRepresentation writeFixture(
			@Nullable final TileFixture fixture) {
		if (fixture == null) {
			return new SPIntermediateRepresentation("");
		} else {
			return ReaderAdapter.ADAPTER.write(fixture);
		}
	}
	/**
	 * @return the class this knows how to write
	 */
	@Override
	public Class<IMapNG> writtenClass() {
		return IMapNG.class;
	}
	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return "MapNGReader";
	}
}

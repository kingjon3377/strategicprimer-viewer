package controller.map.readerng;

import static util.NullCleaner.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import model.map.fixtures.TextFixture;
import model.map.fixtures.terrain.Forest;

import org.eclipse.jdt.annotation.Nullable;

import util.EqualsAny;
import util.Pair;
import util.Warning;
import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;

/**
 * A reader to read new-API maps from XML and turn them into XML.
 *
 * TODO: changesets
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class MapNGReader implements INodeHandler<IMapNG> {
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
	private static List<String> tags;
	static {
		List<String> temp = new ArrayList<>();
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
	 * @param fctory
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
		Location outerLoc = assertNotNull(element.getLocation());
		String outerTag = assertNotNull(element.getName().getLocalPart());
		if ("view".equalsIgnoreCase(element.getName().getLocalPart())) {
			currentTurn =
					XMLHelper.parseInt(
							XMLHelper.getAttribute(element, "current_turn"),
							outerLoc);
			mapTag = getFirstStartElement(stream, outerLoc.getLineNumber());
			if (!"map".equals(mapTag.getName().getLocalPart())) {
				throw new UnwantedChildException(outerTag, mapTag.getName()
						.getLocalPart(), mapTag.getLocation().getLineNumber());
			}
		} else if ("map".equalsIgnoreCase(outerTag)) {
			currentTurn = 0;
			mapTag = element;
		} else {
			throw new UnwantedChildException("xml", assertNotNull(outerTag),
					outerLoc.getLineNumber());
		}
		Location mapTagLocation = assertNotNull(mapTag.getLocation());
		final MapDimensions dimensions =
				new MapDimensions(
						XMLHelper.parseInt(
								XMLHelper.getAttribute(mapTag, "rows"),
								mapTagLocation), XMLHelper.parseInt(
								XMLHelper.getAttribute(mapTag, "columns"),
								mapTagLocation), XMLHelper.parseInt(
								XMLHelper.getAttribute(mapTag, "version"),
								mapTagLocation));
		SPMapNG retval = new SPMapNG(dimensions, players, currentTurn);
		final Point nullPoint = PointFactory.point(-1, -1);
		Point point = nullPoint;
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				StartElement current = event.asStartElement();
				String type = current.getName().getLocalPart();
				Location currentLoc = assertNotNull(current.getLocation());
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
					point =
							PointFactory.point(XMLHelper.parseInt(
									XMLHelper.getAttribute(current, "row"),
									currentLoc), XMLHelper.parseInt(
									XMLHelper.getAttribute(current, "column"),
									currentLoc));
					// Since tiles have been known to be *written* without
					// "kind" and then fail to load, let's be liberal in what we
					// accept here, since we can.
					if (XMLHelper.hasAttribute(current, "kind")
							|| XMLHelper.hasAttribute(current, "type")) {
						retval.setBaseTerrain(
								point,
								TileType.getTileType(XMLHelper
										.getAttributeWithDeprecatedForm(
												current, "kind", "type", warner)));
					} else {
						warner.warn(new MissingPropertyException(current
								.getName().getLocalPart(), "kind", currentLoc
								.getLineNumber()));
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
					Ground ground =
							GROUND_READER.parse(current, stream, players,
									warner, factory);
					Ground oldGround = retval.getGround(point);
					if (oldGround == null) {
						retval.setGround(point, ground);
					} else if (ground.isExposed() && !oldGround.isExposed()) {
						retval.setGround(point, ground);
						retval.addFixture(point, oldGround);
					} else {
						// TODO: Should we do some ordering of Ground other than
						// the order they are in the XML?
						retval.addFixture(point, ground);
					}
				} else if ("forest".equalsIgnoreCase(type)) {
					Forest forest =
							FOREST_READER.parse(current, stream, players,
									warner, factory);
					Forest oldForest = retval.getForest(point);
					if (oldForest == null) {
						retval.setForest(point, forest);
					} else {
						// TODO: Should we do some ordering of Forests other
						// than the order they are in the XML?
						retval.addFixture(point, forest);
					}
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
				String data =
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
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				return assertNotNull(event.asStartElement());
			}
		}
		throw new MissingChildException("map", line);
	}

	/**
	 * Create an intermediate representation of the map to convert it to XML.
	 *
	 * TODO: changesets
	 *
	 * @param <S>
	 *            the type of the object
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
		for (Player player : obj.players()) {
			if (player != null) {
				mapTag.addChild(PLAYER_READER.write(player));
			}
		}
		for (int i = 0; i < dim.rows; i++) {
			final SPIntermediateRepresentation row =
					new SPIntermediateRepresentation("row", Pair.of("index",
							assertNotNull(Integer.toString(i))));
			for (int j = 0; j < dim.cols; j++) {
				Point point = PointFactory.point(i, j);
				if (!TileType.NotVisible.equals(obj.getBaseTerrain(point))
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
				new SPIntermediateRepresentation("tile", Pair.of("row",
						assertNotNull(Integer.toString(point.row))), Pair.of(
						"column", assertNotNull(Integer.toString(point.col))));
		if (!TileType.NotVisible.equals(map.getBaseTerrain(point))) {
			retval.addAttribute("kind", map.getBaseTerrain(point).toXML());
		}
		if (map.isMountainous(point)) {
			retval.addChild(new SPIntermediateRepresentation("mountain"));
		}
		for (River river : map.getRivers(point)) {
			if (river != null) {
				retval.addChild(RIVER_READER.write(river));
			}
		}
		retval.addChild(writeFixture(map.getGround(point)));
		retval.addChild(writeFixture(map.getForest(point)));
		for (TileFixture fixture : map.getOtherFixtures(point)) {
			retval.addChild(writeFixture(fixture));
		}
		return retval;
	}

	/**
	 * "Write" a TileFixture by creating its SPIR.
	 *
	 * @param fixture
	 *            the fixture to write
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

	@Override
	public Class<IMapNG> writes() {
		return IMapNG.class;
	}
}

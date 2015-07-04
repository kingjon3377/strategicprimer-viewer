package controller.map.cxml;

import static java.util.Collections.unmodifiableList;
import static util.NullCleaner.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

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
import model.map.fixtures.TextFixture;
import model.map.fixtures.terrain.Forest;
import util.EqualsAny;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;
/**
 * A reader for new-API maps.
 * @author Jonathan Lovelace
 *
 */
public final class CompactMapNGReader extends AbstractCompactReader<IMapNG> {
	/**
	 * Singleton instance.
	 */
	public static final CompactMapNGReader READER = new CompactMapNGReader();
	/**
	 * List of readers we'll try subtags on.
	 */
	private final List<CompactReader<? extends TileFixture>> readers;
	/**
	 * Singleton.
	 */
	private CompactMapNGReader() {
		final List<CompactReader<? extends TileFixture>> list =
				new ArrayList<CompactReader<? extends TileFixture>>(
						Arrays.asList(CompactMobileReader.READER,
								CompactResourceReader.READER,
								CompactTerrainReader.READER,
								CompactTextReader.READER,
								CompactTownReader.READER,
								CompactGroundReader.READER,
								CompactAdventureReader.READER,
								CompactPortalReader.READER));
		readers = assertNotNull(unmodifiableList(list));
	}
	/**
	 * Read a map from XML.
	 *
	 * @param element the element we're parsing
	 * @param stream the source to read more elements from
	 * @param players The collection to put players in
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed map
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public IMutableMapNG read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "map", "view");
		final int currentTurn;
		final StartElement mapTag;
		Location outerLoc = assertNotNull(element.getLocation());
		String outerTag = assertNotNull(element.getName().getLocalPart());
		final int outerLine = outerLoc.getLineNumber();
		if ("view".equalsIgnoreCase(outerTag)) {
			currentTurn =
					parseInt(getParameter(element, "current_turn"),
							outerLine);
			mapTag = getFirstStartElement(stream, outerLine);
			if (!"map".equalsIgnoreCase(mapTag.getName().getLocalPart())) {
				throw new UnwantedChildException(outerTag, assertNotNull(mapTag
						.getName().getLocalPart()), mapTag.getLocation()
						.getLineNumber());
			}
		} else if ("map".equalsIgnoreCase(outerTag)) {
			currentTurn = 0;
			mapTag = element;
		} else {
			throw new UnwantedChildException("xml", assertNotNull(outerTag),
					outerLine);
		}
		final MapDimensions dimensions =
				new MapDimensions(parseInt(getParameter(mapTag, "rows"),
						outerLine), parseInt(getParameter(mapTag, "columns"),
						outerLine), parseInt(getParameter(mapTag, "version"),
						outerLine));
		SPMapNG retval = new SPMapNG(dimensions, players, currentTurn);
		final Point nullPoint = PointFactory.point(-1, -1);
		Point point = nullPoint;
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				StartElement current = event.asStartElement();
				String type = current.getName().getLocalPart();
				Location currentLoc = assertNotNull(current.getLocation());
				int currentLine = currentLoc.getLineNumber();
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
						throw new UnwantedChildException("tile", type,
								currentLoc.getLineNumber());
					}
					point =
							PointFactory.point(
									parseInt(getParameter(current, "row"),
											currentLine),
									parseInt(getParameter(current, "column"),
											currentLine));
					// Since tiles have been known to be *written* without
					// "kind" and then fail to load, let's be liberal in what we
					// accept here, since we can.
					if (hasParameter(current, "kind")
							|| hasParameter(current, "type")) {
						retval.setBaseTerrain(point, TileType
								.getTileType(getParamWithDeprecatedForm(
										current, "kind", "type", warner)));
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
					retval.addRivers(point,
							CompactTileReader.parseRiver(current, warner));
					spinUntilEnd(NullCleaner.assertNotNull(current.getName()),
							stream);
				} else if ("ground".equalsIgnoreCase(type)) {
					Ground ground =
							CompactGroundReader.READER.read(current, stream, players,
									warner, idFactory);
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
							(Forest) CompactTerrainReader.READER.read(current,
									stream, players, warner, idFactory);
					if (retval.getForest(point) == null) {
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
						retval.addFixture(
								point,
								parseFixture(current, stream, players,
										idFactory, warner));
					} catch (final UnwantedChildException except) {
						if ("unknown".equals(except.getTag())) {
							throw new UnwantedChildException(
									assertNotNull(mapTag.getName()
											.getLocalPart()),
									except.getChild(), currentLine);
						} else {
							throw except;
						}
					} catch (final IllegalStateException except) {
						if (except.getMessage().matches(
								"^Wanted [^ ]*, was [^ ]*$")) {
							final UnwantedChildException nexcept =
									new UnwantedChildException(
											assertNotNull(mapTag.getName()
													.getLocalPart()), type,
											currentLine);
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
		if (hasParameter(mapTag, "current_player")) {
			retval.setCurrentPlayer(players.getPlayer(parseInt(
					getParameter(mapTag, "current_player"), outerLine)));
		} else if (hasParameter(element, "current_player")) {
			retval.setCurrentPlayer(players.getPlayer(parseInt(
					getParameter(element, "current_player"), outerLine)));
		}
		return retval;
	}
	/**
	 * Parse what should be a TileFixture from the XML.
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param idFactory the ID factory to generate IDs with
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed fixture.
	 * @throws SPFormatException on SP format problem
	 */
	private TileFixture parseFixture(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final IDFactory idFactory,
			final Warning warner) throws SPFormatException {
		final String name =
				assertNotNull(element.getName().getLocalPart());
		for (final CompactReader<? extends TileFixture> item : readers) {
			if (item.isSupportedTag(name)) {
				return item.read(element, stream, players, warner, idFactory);
			}
		}
		throw new UnwantedChildException("tile", name, element.getLocation()
				.getLineNumber());
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
	 * @param obj a map
	 * @param ostream the stream to write it to
	 * @param indent how far indented we are already
	 */
	@Override
	public void write(final Appendable ostream, final IMapNG obj, final int indent)
			throws IOException {
		ostream.append(indent(indent));
		ostream.append("<view current_player=\"");
		ostream.append(Integer.toString(obj.getCurrentPlayer().getPlayerId()));
		ostream.append("\" current_turn=\"");
		ostream.append(Integer.toString(obj.getCurrentTurn()));
		ostream.append("\">\n");
		ostream.append(indent(indent + 1));
		final MapDimensions dim = obj.dimensions();
		ostream.append("<map version=\"");
		ostream.append(Integer.toString(dim.version));
		ostream.append("\" rows=\"");
		ostream.append(Integer.toString(dim.rows));
		ostream.append("\" columns=\"");
		ostream.append(Integer.toString(dim.cols));
		ostream.append("\">\n");
		for (Player player : obj.players()) {
			if (player != null) {
				CompactPlayerReader.READER.write(ostream, player, indent + 2);
			}
		}
		for (int i = 0; i < dim.rows; i++) {
			boolean rowEmpty = true;
			for (int j = 0; j < dim.cols; j++) {
				Point point = PointFactory.point(i, j);
				if (!TileType.NotVisible.equals(obj.getBaseTerrain(point))
						|| (obj.isMountainous(point)
						|| obj.getGround(point) != null
						|| obj.getForest(point) != null
						|| obj.getOtherFixtures(point).iterator().hasNext())) {
					if (rowEmpty) {
						rowEmpty = false;
						ostream.append(indent(indent + 2));
						ostream.append("<row index=\"");
						ostream.append(Integer.toString(i));
						ostream.append("\">\n");
					}
					ostream.append(indent(indent + 3));
					ostream.append("<tile row=\"");
					ostream.append(Integer.toString(i));
					ostream.append("\" column=\"");
					ostream.append(Integer.toString(j));
					if (!TileType.NotVisible.equals(obj.getBaseTerrain(point))) {
						ostream.append("\" kind=\"");
						ostream.append(obj.getBaseTerrain(point).toXML());
					}
					ostream.append("\">");
					boolean needeol = true;
					if (obj.isMountainous(point)) {
						eolIfNeeded(needeol, ostream);
						needeol = false;
						ostream.append(indent(indent + 4));
						ostream.append("<mountain />\n");
					}
					for (River river : obj.getRivers(point)) {
						if (river != null) {
							eolIfNeeded(needeol, ostream);
							needeol = false;
							CompactTileReader.writeRiver(ostream, river, indent + 4);
						}
					}
					Ground ground = obj.getGround(point);
					if (ground != null) {
						eolIfNeeded(needeol, ostream);
						needeol = false;
						CompactReaderAdapter.write(ostream, ground, indent + 4);
					}
					Forest forest = obj.getForest(point);
					if (forest != null) {
						eolIfNeeded(needeol, ostream);
						needeol = false;
						CompactReaderAdapter.write(ostream, forest, indent + 4);
					}
					for (TileFixture fixture : obj.getOtherFixtures(point)) {
						if (fixture != null) {
							eolIfNeeded(needeol, ostream);
							needeol = false;
							CompactReaderAdapter.write(ostream, fixture, indent + 4);
						}
					}
					if (!needeol) {
						ostream.append(indent(indent + 3));
					}
					ostream.append("</tile>\n");
				}
			}
			if (!rowEmpty) {
				ostream.append(indent(indent + 2));
				ostream.append("</row>\n");
			}
		}
		ostream.append(indent(indent + 1));
		ostream.append("</map>\n");
		ostream.append(indent(indent));
		ostream.append("</view>\n");
	}
	/**
	 * @param tag a tag
	 * @return whether this class supports it
	 */
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
		return "map".equalsIgnoreCase(tag) || "view".equalsIgnoreCase(tag);
	}
	/**
	 * Write a newline if needed.
	 * @param writer the writer to write to
	 * @param needeol whether we need a newline.
	 * @throws IOException on I/O error
	 */
	private static void eolIfNeeded(final boolean needeol,
			final Appendable writer) throws IOException {
		if (needeol) {
			writer.append("\n");
		}
	}
}

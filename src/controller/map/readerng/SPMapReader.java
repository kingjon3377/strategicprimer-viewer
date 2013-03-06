package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static java.lang.Integer.parseInt;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMap;
import model.map.Tile;
import util.EqualsAny;
import util.Pair;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;

/**
 * A reader to produce SPMaps.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class SPMapReader implements INodeHandler<SPMap> {
	/**
	 * The tag we read.
	 */
	private static final String TAG = "map";

	/**
	 * Parse a map from XML.
	 *
	 * @param element the eleent to start parsing with
	 * @param stream the XML tags and such
	 * @param players the collection of players, most likely null at this point
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the produced type
	 * @throws SPFormatException on format problems
	 */
	@Override
	public SPMap parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final SPMap map = new SPMap(new MapDimensions(
				Integer.parseInt(getAttribute(element, "rows")),
				Integer.parseInt(getAttribute(element, "columns")),
				Integer.parseInt(getAttribute(element, "version", "1"))));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement elem = event.asStartElement();
				parseChild(stream, warner, map, elem, idFactory);
			} else if (event.isEndElement()
					&& TAG.equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		if (XMLHelper.hasAttribute(element, "current_player")) {
			map.getPlayers()
					.getPlayer(
							Integer.parseInt(getAttribute(element,
									"current_player"))).setCurrent(true);
		}
		return map;
	}

	/**
	 * Parse a child element.
	 *
	 * @param stream the stream we're reading from---only here to pass to
	 *        children
	 * @param warner the Warning instance to use.
	 * @param map the map we're building.
	 * @param elem the current tag.
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException on SP map format error
	 */
	private static void parseChild(final Iterable<XMLEvent> stream,
			final Warning warner, final SPMap map, final StartElement elem,
			final IDFactory idFactory) throws SPFormatException {
		final String type = elem.getName().getLocalPart();
		if ("player".equalsIgnoreCase(type)) {
			map.addPlayer(PLAYER_READER.parse(elem, stream, map.getPlayers(),
					warner, idFactory));
		} else if (!"row".equalsIgnoreCase(type)) {
			// We deliberately ignore "row"; that had been a "continue",
			// but we want to extract this as a method.
			if ("tile".equalsIgnoreCase(type)) {
				final int row = parseInt(getAttribute(elem, "row"));
				final int col = parseInt(getAttribute(elem, "column"));
				final Point loc = PointFactory.point(row, col);
				map.addTile(loc, TILE_READER.parse(elem, stream, map.getPlayers(),
						warner, idFactory));
			} else if (EqualsAny.equalsAny(type, ISPReader.FUTURE)) {
				warner.warn(new UnsupportedTagException(type, elem // NOPMD
						.getLocation().getLineNumber()));
			} else {
				throw new UnwantedChildException(TAG, elem.getName()
						.getLocalPart(), elem.getLocation().getLineNumber());
			}
		}
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("map");
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<SPMap> writes() {
		return SPMap.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends SPMap> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"map");
		retval.addAttribute("version", Integer.toString(obj.getDimensions().version));
		retval.addAttribute("rows", Integer.toString(obj.getDimensions().rows));
		retval.addAttribute("columns", Integer.toString(obj.getDimensions().cols));
		if (!obj.getPlayers().getCurrentPlayer().getName().isEmpty()) {
			retval.addAttribute(
					"current_player",
					Integer.toString(obj.getPlayers().getCurrentPlayer()
							.getPlayerId()));
		}
		for (final Player player : obj.getPlayers()) {
			retval.addChild(ReaderAdapter.ADAPTER.write(player));
		}
		final MapDimensions dim = obj.getDimensions();
		for (int i = 0; i < dim.rows; i++) {
			@SuppressWarnings("unchecked")
			final SPIntermediateRepresentation row = new SPIntermediateRepresentation(// NOPMD
					"row", Pair.of("index", Integer.toString(i)));
			for (int j = 0; j < dim.cols; j++) {
				final Point point = PointFactory.point(i, j);
				final Tile tile = obj.getTile(point);
				if (!tile.isEmpty()) {
					retval.addChild(row);
					row.addChild(TILE_READER.writeTile(point, tile));
				}
			}
		}
		return retval;
	}

	/**
	 * The reader to use to parse players.
	 */
	private static final PlayerReader PLAYER_READER = new PlayerReader();
	/**
	 * The reader to use to parse tiles.
	 */
	private static final TileReader TILE_READER = new TileReader();
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPMapReader";
	}
}

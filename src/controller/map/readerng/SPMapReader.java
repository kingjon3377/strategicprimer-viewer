package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static util.NullCleaner.assertNotNull;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.ITile;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMap;
import util.EqualsAny;
import util.NullCleaner;
import util.Warning;

/**
 * A reader to produce SPMaps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class SPMapReader implements INodeHandler<SPMap> {
	/**
	 * The reader to use to parse players.
	 */
	private static final PlayerReader PLAYER_READER = new PlayerReader();
	/**
	 * The reader to use to parse tiles.
	 */
	private static final TileReader TILE_READER = new TileReader();

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
			final Iterable<XMLEvent> stream, final IMutablePlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final Location loc = NullCleaner.assertNotNull(element.getLocation());
		final SPMap map = new SPMap(new MapDimensions(
				XMLHelper.parseInt(getAttribute(element, "rows"), loc),
				XMLHelper.parseInt(getAttribute(element, "columns"), loc),
				XMLHelper.parseInt(getAttribute(element, "version", "1"), loc)));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				parseChild(stream, warner, map,
						assertNotNull(event.asStartElement()), idFactory);
			} else if (event.isEndElement()
					&& TAG.equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		if (XMLHelper.hasAttribute(element, "current_player")) {
			map.getPlayers()
					.getPlayer(
							XMLHelper.parseInt(getAttribute(element,
									"current_player"), loc)).setCurrent(true);
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
		if (type == null) {
			return;
		} else if ("player".equalsIgnoreCase(type)) {
			map.addPlayer(PLAYER_READER.parse(elem, stream, map.getPlayers(),
					warner, idFactory));
		} else if (!"row".equalsIgnoreCase(type)) {
			// We deliberately ignore "row"; that had been a "continue",
			// but we want to extract this as a method.
			if ("tile".equalsIgnoreCase(type)) {
				final int row =
						XMLHelper.parseInt(getAttribute(elem, "row"),
								NullCleaner.assertNotNull(elem.getLocation()));
				final int col =
						XMLHelper.parseInt(getAttribute(elem, "column"),
								NullCleaner.assertNotNull(elem.getLocation()));
				final Point loc = PointFactory.point(row, col);
				map.addTile(loc, TILE_READER.parse(elem, stream,
						map.getPlayers(), warner, idFactory));
			} else if (EqualsAny.equalsAny(type, ISPReader.FUTURE)) {
				warner.warn(new UnsupportedTagException(type, elem // NOPMD
						.getLocation().getLineNumber()));
			} else {
				throw new UnwantedChildException(TAG, type, elem.getLocation()
						.getLineNumber());
			}
		}
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("map"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<SPMap> writes() {
		return SPMap.class;
	}

	/**
	 * Create an intermediate representation to convert to XML.
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
		retval.addIntegerAttribute("version",  obj.getDimensions().version);
		retval.addIntegerAttribute("rows",  obj.getDimensions().rows);
		retval.addIntegerAttribute("columns",  obj.getDimensions().cols);
		if (!obj.getPlayers().getCurrentPlayer().getName().isEmpty()) {
			retval.addIntegerAttribute("current_player",
					obj.getPlayers().getCurrentPlayer().getPlayerId());
		}
		for (final Player player : obj.getPlayers()) {
			if (player != null) {
				retval.addChild(PLAYER_READER.write(player));
			}
		}
		final MapDimensions dim = obj.getDimensions();
		for (int i = 0; i < dim.rows; i++) {
			final SPIntermediateRepresentation row =
					new SPIntermediateRepresentation(// NOPMD
							"row");
			row.addIntegerAttribute("index", i);
			for (int j = 0; j < dim.cols; j++) {
				final Point point = PointFactory.point(i, j);
				final ITile tile = obj.getTile(point);
				if (!tile.isEmpty()) {
					retval.addChild(row);
					row.addChild(TileReader.writeTile(point, tile));
				}
			}
		}
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPMapReader";
	}
}

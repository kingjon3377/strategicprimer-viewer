package controller.map.cxml;

import static java.util.Collections.unmodifiableList;
import static util.NullCleaner.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.IMutableTile;
import model.map.ITile;
import model.map.Point;
import model.map.River;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for tiles, including rivers.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
@SuppressWarnings("deprecation")
public final class CompactTileReader extends AbstractCompactReader<ITile> {
	/**
	 * List of readers we'll try subtags on.
	 */
	private final List<AbstractCompactReader<? extends TileFixture>> readers;

	/**
	 * Singleton object.
	 */
	public static final CompactTileReader READER = new CompactTileReader();

	/**
	 * Singleton.
	 */
	private CompactTileReader() {
		final List<AbstractCompactReader<? extends TileFixture>> list =
				new ArrayList<>(Arrays.asList(CompactMobileReader.READER,
						CompactResourceReader.READER,
						CompactTerrainReader.READER, CompactTextReader.READER,
						CompactTownReader.READER, CompactGroundReader.READER,
						CompactAdventureReader.READER,
						CompactPortalReader.READER));
		readers = assertNotNull(unmodifiableList(list));
	}

	/**
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public IMutableTile read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		final IMutableTile retval = new Tile(
				TileType.getTileType(getParamWithDeprecatedForm(element,
						"kind", "type", warner)));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if (isRiver(NullCleaner.assertNotNull(event.asStartElement()
						.getName()))) {
					retval.addFixture(new RiverFixture(parseRiver(// NOPMD
							NullCleaner.assertNotNull(event.asStartElement()),
							warner)));
					spinUntilEnd(NullCleaner.assertNotNull(event.asStartElement()
					.getName()), stream);
				} else {
					retval.addFixture(parseFixture(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream, players, idFactory, warner));
				}
			} else if (event.isCharacters()) {
				final String text = event.asCharacters().getData().trim();
				if (text != null && !text.isEmpty()) {
					warner.warn(new UnwantedChildException("tile", // NOPMD
							"arbitrary text", event.getLocation()
									.getLineNumber()));
					retval.addFixture(new TextFixture(text, -1)); // NOPMD
				}
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
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
		final String name = element.getName().getLocalPart();
		assert name != null;
		for (final CompactReader<? extends TileFixture> item : readers) {
			if (item.isSupportedTag(name)) {
				return item.read(element, stream, players, warner, idFactory);
			}
		}
		throw new UnwantedChildException("tile", name, element.getLocation()
				.getLineNumber());
	}

	/**
	 * @param name the name associated with an element
	 * @return whether it represents a river.
	 */
	private static boolean isRiver(final QName name) {
		return "river".equalsIgnoreCase(name.getLocalPart())
				|| "lake".equalsIgnoreCase(name.getLocalPart());
	}

	/**
	 * Parse a river from XML. The caller is now responsible for getting past
	 * the closing tag.
	 *
	 * @param element the element to parse
	 * @param warner the Warning instance to use as needed
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
	 * @param tag a tag
	 * @return whether it's one we can read
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "tile".equalsIgnoreCase(tag);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final ITile obj, final int indent)
			throws IOException {
		throw new IllegalStateException(
				"Don't call this; call writeTile() instead");
	}

	/**
	 * Write a tile to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj the tile to write
	 * @param point the location of the tile
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 */
	public static void writeTile(final Appendable ostream, final Point point,
			final ITile obj, final int indent) throws IOException {
		if (!obj.isEmpty()) {
			ostream.append(indent(indent));
			ostream.append("<tile row=\"");
			ostream.append(Integer.toString(point.row));
			ostream.append("\" column=\"");
			ostream.append(Integer.toString(point.col));
			if (!TileType.NotVisible.equals(obj.getTerrain())) {
				ostream.append("\" kind=\"");
				ostream.append(obj.getTerrain().toXML());
			}
			ostream.append("\">");
			if (obj.iterator().hasNext()) {
				ostream.append('\n');
				for (final TileFixture fix : obj) {
					if (fix != null) {
						CompactReaderAdapter.write(ostream, fix, indent + 1);
					}
				}
				ostream.append(indent(indent));
			}
			ostream.append("</tile>\n");
		}
	}

	/**
	 * Write a river.
	 *
	 * @param ostream the stream we're writing to
	 * @param obj the river to write
	 * @param indent the indentation level
	 * @throws IOException on I/O error
	 */
	public static void writeRiver(final Appendable ostream, final River obj,
			final int indent) throws IOException {
		for (int i = 0; i < indent; i++) {
			ostream.append('\t');
		}
		if (River.Lake.equals(obj)) {
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
	 * @param iter a series of rivers to write
	 * @param indent the indentation level
	 * @throws IOException on I/O error
	 */
	public static void writeRivers(final Appendable ostream,
			final Iterable<River> iter, final int indent) throws IOException {
		for (final River river : iter) {
			if (river != null) {
				writeRiver(ostream, river, indent);
			}
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactTileReader";
	}
}

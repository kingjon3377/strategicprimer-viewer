package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;
import static java.lang.Integer.parseInt;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.River;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A reader for Tiles.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class TileReader implements INodeReader<Tile> {
	/**
	 * @return the type we produce
	 */
	@Override
	public Class<Tile> represents() {
		return Tile.class;
	}

	/**
	 * @param element
	 *            the element to start with
	 * @param stream
	 *            the stream to get more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @return the tile we're at in the stream
	 * @throws SPFormatException
	 *             on map format error
	 */
	@Override
	public Tile parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Tile tile = new Tile(parseInt(getAttribute(element, "row")), //NOPMD
				parseInt(getAttribute(element, "column")),
				TileType.getTileType(getAttributeWithDeprecatedForm(element, "kind", "type", warner)));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if (FixtureReader.supports(event.asStartElement().getName()
						.getLocalPart())) {
					tile.addFixture(ReaderFactory.createReader(
							TileFixture.class).parse(event.asStartElement(),
							stream, players, warner));
				} else if (isRiver(event.asStartElement()
								.getName().getLocalPart())) {
					tile.addFixture(new RiverFixture(ReaderFactory.createReader(River.class) // NOPMD
							.parse(event.asStartElement(), stream, players, warner)));
				} else {
					throw new UnwantedChildException("tile", event.asStartElement()
							.getName().getLocalPart(), event.getLocation()
							.getLineNumber());
				}
			} else if (event.isCharacters()) {
				tile.addFixture(new TextFixture(event.asCharacters().getData().trim(), // NOPMD
						-1));
			} else if (event.isEndElement()
					&& "tile".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return tile;
	}
	/**
	 * @param tag a tag
	 * @return whether it's a river tag.
	 */
	private static boolean isRiver(final String tag) {
		return "river".equalsIgnoreCase(tag) || "lake".equalsIgnoreCase(tag);
	}
	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("tile");
	}
}

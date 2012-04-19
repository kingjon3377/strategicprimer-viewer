package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static java.lang.Integer.parseInt;

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
		final Tile tile = new Tile(parseInt(getAttribute(element, "row")),
				parseInt(getAttribute(element, "column")),
				TileType.getTileType(getAttribute(element, "kind")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()
					&& FixtureReader.supports(event.asStartElement().getName()
							.getLocalPart())) {
				tile.addFixture(ReaderFactory.createReader(TileFixture.class)
						.parse(event.asStartElement(), stream, players, warner));
			} else if (event.isCharacters()) {
				tile.addFixture(new TextFixture(event.asCharacters().getData(), // NOPMD
						-1));
			} else if (event.isEndElement()
					&& "tile".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			} else if (event.isStartElement()
					&& "river".equalsIgnoreCase(event.asStartElement()
							.getName().getLocalPart())) {
				tile.addFixture(new RiverFixture(ReaderFactory.createReader(River.class) // NOPMD
						.parse(event.asStartElement(), stream, players, warner)));
			}
		}
		return tile;
	}

}

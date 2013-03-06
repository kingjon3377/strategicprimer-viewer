package controller.map.readerng;

import static controller.map.readerng.ReaderAdapter.checkedCast;
import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.Point;
import model.map.River;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for Tiles.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class TileReader implements INodeHandler<Tile> {
	/**
	 * @param element the element to start with
	 * @param stream the stream to get more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the tile we're at in the stream
	 * @throws SPFormatException on map format error
	 */
	@Override
	public Tile parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final Tile tile = new Tile(
				TileType.getTileType(getAttributeWithDeprecatedForm(element,
						"kind", "type", warner)));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if (isRiver(event.asStartElement().getName().getLocalPart())) {
					tile.addFixture(parseRiver(stream, players, warner,
							idFactory, event));
				} else {
					perhapsAddFixture(stream, players, warner, tile, event,
							element.getName().getLocalPart(), idFactory);
				}
			} else if (event.isCharacters()) {
				tile.addFixture(new TextFixture(event.asCharacters().getData()// NOPMD
						.trim(), -1));
			} else if (event.isEndElement()
					&& "tile".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return tile;
	}

	/**
	 * Parse a river from XML. Method extracted to avoid a "instantiation inside loops" warning.
	 * @param stream the stream of XML events
	 * @param players the collection of players
	 * @param warner the Warning instance
	 * @param idFactory the ID factory
	 * @param event the current XML event.
	 * @return the river fixture.
	 * @throws SPFormatException on SP format problems.
	 */
	private static RiverFixture parseRiver(final Iterable<XMLEvent> stream,
			final PlayerCollection players, final Warning warner,
			final IDFactory idFactory, final XMLEvent event)
			throws SPFormatException {
		return new RiverFixture(READER.parse(
				event.asStartElement(), stream, players, warner,
				idFactory));
	}

	// ESCA-JAVA0138:
	/**
	 * We expect the next start element to be a TileFixture. If it is, parse and
	 * add it.
	 *
	 * @param stream the stream to read events from
	 * @param players the players collection (required by the spec of the
	 *        methods we call)
	 * @param warner the Warning instance
	 * @param tile the tile under construction.
	 * @param event the tag to be parsed
	 * @param tag the tile's tag
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException on SP format problems
	 */
	private static void perhapsAddFixture(final Iterable<XMLEvent> stream,
			final PlayerCollection players, final Warning warner,
			final Tile tile, final XMLEvent event, final String tag,
			final IDFactory idFactory) throws SPFormatException {
		try {
			tile.addFixture(checkedCast(
					ReaderAdapter.ADAPTER.parse(
							// NOPMD
							event.asStartElement(), stream, players, warner,
							idFactory), TileFixture.class));
		} catch (final UnwantedChildException except) {
			// ESCA-JAVA0049:
			if ("unknown".equals(except.getTag())) {
				throw new UnwantedChildException(tag, // NOPMD
						except.getChild(), event.getLocation().getLineNumber());
			} else {
				throw except;
			}
		} catch (final IllegalStateException except) {
			if (except.getMessage().matches("^Wanted [^ ]*, was [^ ]*$")) {
				throw new UnwantedChildException(tag, // NOPMD
						event.asStartElement().getName().getLocalPart(), event
								.getLocation().getLineNumber());
			} else {
				throw except;
			}
		}
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

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Tile obj) {
		throw new IllegalStateException("Never call this; call writeTile() instead");
	}
	/**
	 * Create an intermediate representation to write to a writer.
	 * @param obj the Tile to write
	 * @param point its location
	 * @return an intermediate representation
	 */
	public SPIntermediateRepresentation writeTile(final Point point, final Tile obj) {
		if (obj.isEmpty()) {
			return new SPIntermediateRepresentation(""); // NOPMD
		} else {
			final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
					"tile");
			retval.addAttribute("row",
					Integer.toString(point.row));
			retval.addAttribute("column",
					Integer.toString(point.col));
			if (!(TileType.NotVisible.equals(obj.getTerrain()))) {
				retval.addAttribute("kind", obj.getTerrain().toXML());
			}
			if (obj.iterator().hasNext()) {
				for (final TileFixture fix : obj) {
					writeFixture(fix, retval);
				}
			}
			return retval;
		}
	}
	/**
	 * "Write" a TileFixture by creating its SPIR and attaching it to the proper parent.
	 * @param fix the current fixture
	 * @param parent the SPIR node representing the Tile, for rivers
	 */
	private static void writeFixture(final TileFixture fix,
			final SPIntermediateRepresentation parent) {
		if (fix instanceof RiverFixture) {
			for (final River river : (RiverFixture) fix) {
				parent.addChild(READER.write(river));
			}
		} else {
			parent.addChild(ReaderAdapter.ADAPTER.write(fix));
		}
	}

	/**
	 * @return the type of object we know how to write.
	 */
	@Override
	public Class<Tile> writes() {
		return Tile.class;
	}

	/**
	 * A reader to use to parse and write Rivers.
	 */
	private static final RiverReader READER = new RiverReader();
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TileReader";
	}
}

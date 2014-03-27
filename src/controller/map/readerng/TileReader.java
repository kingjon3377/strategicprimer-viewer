package controller.map.readerng;

import static controller.map.readerng.ReaderAdapter.checkedCast;
import static controller.map.readerng.XMLHelper.assertNonNullList;
import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IMutableTile;
import model.map.IPlayerCollection;
import model.map.ITile;
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
public class TileReader implements INodeHandler<ITile> {
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
	public IMutableTile parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IPlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final IMutableTile tile = new Tile(
				TileType.getTileType(getAttributeWithDeprecatedForm(element,
						"kind", "type", warner)));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement selem = event.asStartElement();
				final String slocal = selem.getName().getLocalPart();
				assert slocal != null;
				if (isRiver(slocal)) {
					tile.addFixture(parseRiver(stream, players, warner,
							idFactory, selem));
				} else {
					final String olocal = element.getName().getLocalPart();
					assert olocal != null;
					perhapsAddFixture(stream, players, warner, tile, selem,
							olocal, idFactory);
				}
			} else if (event.isCharacters()) {
				final String data = event.asCharacters().getData()
						.trim();
				assert data != null;
				tile.addFixture(new TextFixture(data, -1)); // NOPMD
			} else if (event.isEndElement()
					&& "tile".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return tile;
	}

	/**
	 * Parse a river from XML. Method extracted to avoid a
	 * "instantiation inside loops" warning.
	 *
	 * @param stream the stream of XML events
	 * @param players the collection of players
	 * @param warner the Warning instance
	 * @param idFactory the ID factory
	 * @param event the current XML event.
	 * @return the river fixture.
	 * @throws SPFormatException on SP format problems.
	 */
	private static RiverFixture parseRiver(final Iterable<XMLEvent> stream,
			final IPlayerCollection players, final Warning warner,
			final IDFactory idFactory, final StartElement event)
			throws SPFormatException {
		return new RiverFixture(READER.parse(event, stream, players, warner,
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
			final IPlayerCollection players, final Warning warner,
			final IMutableTile tile, final StartElement event, final String tag,
			final IDFactory idFactory) throws SPFormatException {
		try {
			tile.addFixture(checkedCast(ReaderAdapter.ADAPTER.parse(event,
					stream, players, warner, idFactory), TileFixture.class));
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
				final String local = event.asStartElement().getName().getLocalPart();
				assert local != null;
				throw new UnwantedChildException(tag, local, event// NOPMD
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
		return assertNonNullList(Collections.singletonList("tile"));
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final ITile obj) {
		throw new IllegalStateException(
				"Never call this; call writeTile() instead");
	}

	/**
	 * Create an intermediate representation to write to a writer.
	 *
	 * @param obj the Tile to write
	 * @param point its location
	 * @return an intermediate representation
	 */
	public static SPIntermediateRepresentation writeTile(final Point point,
			final ITile obj) {
		if (obj.isEmpty()) {
			return new SPIntermediateRepresentation(""); // NOPMD
		} 
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"tile");
		final String row = Integer.toString(point.row);
		assert row != null;
		retval.addAttribute("row", row);
		final String col = Integer.toString(point.col);
		assert col != null;
		retval.addAttribute("column", col);
		if (!(TileType.NotVisible.equals(obj.getTerrain()))) {
			retval.addAttribute("kind", obj.getTerrain().toXML());
		}
		if (obj.iterator().hasNext()) {
			for (final TileFixture fix : obj) {
				if (fix != null) {
					writeFixture(fix, retval);
				}
			}
		}
		return retval;
	}

	/**
	 * "Write" a TileFixture by creating its SPIR and attaching it to the proper
	 * parent.
	 *
	 * @param fix the current fixture
	 * @param parent the SPIR node representing the Tile, for rivers
	 */
	private static void writeFixture(final TileFixture fix,
			final SPIntermediateRepresentation parent) {
		if (fix instanceof RiverFixture) {
			for (final River river : (RiverFixture) fix) {
				if (river != null) {
					parent.addChild(READER.write(river));
				}
			}
		} else {
			parent.addChild(ReaderAdapter.ADAPTER.write(fix));
		}
	}

	/**
	 * @return the type of object we know how to write.
	 */
	@Override
	public Class<ITile> writes() {
		return ITile.class;
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

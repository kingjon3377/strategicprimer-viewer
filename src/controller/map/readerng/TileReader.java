package controller.map.readerng;

import static controller.map.readerng.ReaderAdapter.checkedCast;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getAttributeWithDeprecatedForm;
import static java.lang.Integer.parseInt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.River;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.XMLWritable;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * A reader for Tiles.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class TileReader implements INodeHandler<Tile> {
	/**
	 * @param element
	 *            the element to start with
	 * @param stream
	 *            the stream to get more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the tile we're at in the stream
	 * @throws SPFormatException
	 *             on map format error
	 */
	@Override
	public Tile parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		final Tile tile = new Tile(
				parseInt(getAttribute(element, "row")), // NOPMD
				parseInt(getAttribute(element, "column")),
				TileType.getTileType(getAttributeWithDeprecatedForm(element,
						"kind", "type", warner)),
				(stream.iterator() instanceof IncludingIterator ? ((IncludingIterator) stream
						.iterator()).getFile() : ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if (isRiver(event.asStartElement().getName().getLocalPart())) {
					tile.addFixture(new RiverFixture(new RiverReader().parse(// NOPMD
							event.asStartElement(), stream, players, warner, idFactory)));
				} else {
					perhapsAddFixture(stream, players, warner, tile, event,
							element.getName().getLocalPart(), idFactory);
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
	// ESCA-JAVA0138:
	/**
	 * We expect the next start element to be a TileFixture. If it is, parse and add it.
	 * @param stream the stream to read events from
	 * @param players the players collection (required by the spec of the methods we call)
	 * @param warner the Warning instance
	 * @param tile the tile under construction.
	 * @param event the tag to be parsed
	 * @param tag the tile's tag
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException on SP format problems
	 */
	private static void perhapsAddFixture(final Iterable<XMLEvent> stream,
			final PlayerCollection players, final Warning warner,
			final Tile tile, final XMLEvent event, final String tag, final IDFactory idFactory)
			throws SPFormatException {
		try {
			tile.addFixture(checkedCast(new ReaderAdapter().parse(//NOPMD
				event.asStartElement(), stream, players, warner, idFactory),
				TileFixture.class));
		} catch (final UnwantedChildException except) {
			// ESCA-JAVA0049:
			if ("unknown".equals(except.getTag())) {
				throw new UnwantedChildException(tag, //NOPMD
						except.getChild(), event
						.getLocation().getLineNumber());
			} else {
				throw except;
			}
		} catch (final IllegalStateException except) {
			if (except.getMessage().matches("^Wanted [^ ]*, was [^ ]*$")) {
				throw new UnwantedChildException(tag, //NOPMD
						event.asStartElement()
						.getName().getLocalPart(), event
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
	 * @param obj
	 *            the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Tile obj) {
		if (obj.isEmpty()) {
			return new SPIntermediateRepresentation(""); // NOPMD
		} else {
			final SPIntermediateRepresentation retval = new SPIntermediateRepresentation("tile");
			retval.addAttribute("row", Integer.toString(obj.getLocation().row()));
			retval.addAttribute("column", Integer.toString(obj.getLocation().col()));
			if (!(TileType.NotVisible.equals(obj.getTerrain()))) {
				retval.addAttribute("kind", obj.getTerrain().toXML());
			}
			if (!obj.getContents().isEmpty()) {
				final ReaderAdapter adapter = new ReaderAdapter();
				final RiverReader riverAdapter = new RiverReader();
				final Map<String, SPIntermediateRepresentation> tagMap = new HashMap<String, SPIntermediateRepresentation>();
				tagMap.put(obj.getFile(), retval);
				for (final TileFixture fix : obj.getContents()) {
					if (fix instanceof RiverFixture) {
						for (River river : (RiverFixture) fix) {
							retval.addChild(riverAdapter.write(river));
						}
					} else {
						addChild(tagMap, fix, adapter, retval);
					}
				}
				tagMap.remove(obj.getFile());
				for (SPIntermediateRepresentation child : tagMap.values()) {
					retval.addChild(child);
				}
			}
			return retval;
		}
	}
	/**
	 * Add a child node to a node---the parent node, or an 'include' node representing its chosen file.
	 * @param map the mapping from filenames to IRs.
	 * @param obj the object we're handling
	 * @param adapter the adapter to use to call the right handler.
	 * @param parent the parent node, so we can add any include nodes created to it
	 */
	private static void addChild(final Map<String, SPIntermediateRepresentation> map,
			final XMLWritable obj, final ReaderAdapter adapter, final SPIntermediateRepresentation parent) {
		if (obj.getFile() == null) {
			parent.addChild(adapter.write(obj));
		} else {
			if (!map.containsKey(obj.getFile())) {
				final SPIntermediateRepresentation includeTag = new SPIntermediateRepresentation(
						"include");
				includeTag.addAttribute("file", obj.getFile());
				parent.addChild(includeTag);
				map.put(obj.getFile(), includeTag);
			}
			map.get(obj.getFile()).addChild(adapter.write(obj));
		}
	}
	/**
	 * @return the type of object we know how to write.
	 */
	@Override
	public Class<Tile> writes() {
		return Tile.class;
	}
}

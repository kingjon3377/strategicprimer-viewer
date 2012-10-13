package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.River;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTileReader extends AbstractCompactReader implements CompactReader<Tile> {
	/**
	 * Singleton.
	 */
	private CompactTileReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactTileReader READER = new CompactTileReader();
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
	public Tile read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		final Tile retval = new Tile(Integer.parseInt(getParameter(element, "row")),
				Integer.parseInt(getParameter(element, "column")),
				TileType.getTileType(getParameterWithDeprecatedForm(element,
						"kind", "type", warner)));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if (isRiver(event.asStartElement().getName())) {
					retval.addFixture(new RiverFixture(parseRiver(//NOPMD
							event.asStartElement(), stream, warner)));
				} else {
					retval.addFixture(parseFixture(event.asStartElement(), stream, players, idFactory, warner));
				}
			} else if (event.isCharacters()) {
				final String text = event.asCharacters().getData().trim();
				if (!text.isEmpty()) {
					warner.warn(new UnwantedChildException("tile", // NOPMD
							"arbitrary text", event.getLocation()
									.getLineNumber()));
					retval.addFixture(new TextFixture(event.asCharacters()//NOPMD
							.getData().trim(), -1));
				}
			} else if (event.isEndElement() && element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return retval;
	}
	/**
	 * List of readers we'll try subtags on.
	 */
	private final List<AbstractCompactReader> readers = Arrays
			.asList(new AbstractCompactReader[] { CompactMobileReader.READER,
					CompactResourceReader.READER, CompactTerrainReader.READER,
					CompactTextReader.READER, CompactTownReader.READER, CompactGroundReader.READER });

	/**
	 * Parse what should be a TileFixture from the XML.
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param idFactory the ID factory to generate IDs with
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed fixture.
	 * @throws SPFormatException on SP format problem
	 */
	@SuppressWarnings("unchecked")
	private TileFixture parseFixture(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final IDFactory idFactory, final Warning warner) throws SPFormatException {
		final String name = element.getName().getLocalPart();
		for (AbstractCompactReader item : readers) {
			if (item.isSupportedTag(name)) {
				return ((CompactReader<? extends TileFixture>) item).read(
						element, stream, players, warner, idFactory);
			}
		}
		throw new UnwantedChildException("tile", name, element.getLocation().getLineNumber());
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
	 * Parse a river from XML.
	 * @param element the element to parse
	 * @param stream the stream to read further elements from (FIXME: do we need this parameter?)
	 * @param warner the Warning instance to use as needed
	 * @return the parsed river
	 * @throws SPFormatException on SP format problem
	 */
	public River parseRiver(final StartElement element, final IteratorWrapper<XMLEvent> stream,
			final Warning warner) throws SPFormatException {
		requireTag(element, "river", "lake");
		spinUntilEnd(element.getName(), stream);
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
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final Tile obj, final int indent) throws IOException {
		if (!obj.isEmpty()) {
			out.append(indent(indent));
			out.append("<tile row=\"");
			out.append(Integer.toString(obj.getLocation().row));
			out.append("\" column=\"");
			out.append(Integer.toString(obj.getLocation().col));
			if (!TileType.NotVisible.equals(obj.getTerrain())) {
				out.append("\" kind=\"");
				out.append(obj.getTerrain().toXML());
			}
			out.append("\">");
			if (obj.iterator().hasNext()) {
				out.append('\n');
				for (final TileFixture fix : obj) {
					CompactReaderAdapter.ADAPTER.write(out, fix, indent + 1);
				}
				out.append(indent(indent));
			}
			out.append("</tile>\n");
		}
	}
	/**
	 * Write a river.
	 * @param out the stream we're writing to
	 * @param obj the river to write
	 * @param indent the indentation level
	 * @throws IOException on I/O error
	 */
	public void writeRiver(final Writer out, final River obj, final int indent) throws IOException {
		for (int i = 0; i < indent; i++) {
			out.append('\t');
		}
		if (River.Lake.equals(obj)) {
			out.append("<lake />");
		} else {
			out.append("<river direction=\"");
			out.append(obj.getDescription());
			out.append("\" />");
		}
		out.append('\n');
	}
	/**
	 * Write a series of rivers.
	 * @param out the stream to write to
	 * @param iter a series of rivers to write
	 * @param indent the indentation level
	 * @throws IOException on I/O error
	 */
	public void writeRivers(final Writer out, final Iterable<River> iter,
			final int indent) throws IOException {
		for (River river : iter) {
			writeRiver(out, river, indent);
		}
	}
}

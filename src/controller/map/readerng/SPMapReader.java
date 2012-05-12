package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.Player;
import model.map.PlayerCollection;
import model.map.SPMap;
import model.map.Tile;
import model.map.XMLWritable;
import util.EqualsAny;
import util.Warning;
import controller.map.ISPReader;
import controller.map.SPFormatException;
import controller.map.UnsupportedTagException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader to produce SPMaps.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SPMapReader implements INodeHandler<SPMap> {
	/**
	 * The tag we read.
	 */
	private static final String TAG = "map";
	/**
	 * Parse a map from XML.
	 * 
	 * @param element
	 *            the eleent to start parsing with
	 * @param stream
	 *            the XML tags and such
	 * @param players the collection of players, most likely null at this point
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the produced type
	 * @throws SPFormatException
	 *             on format problems
	 */
	@Override
	public SPMap parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		final SPMap map = new SPMap(Integer.parseInt(getAttribute(
				element, "version", "1")), Integer.parseInt(getAttribute(
				element, "rows")), Integer.parseInt(getAttribute(element,
				"columns")));
		for (XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement elem = event.asStartElement();
				parseChild(stream, warner, map, elem, idFactory); 
			} else if (event.isEndElement()
					&& TAG.equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return map;
	}

	/** Parse a child element.
	 * @param stream the stream we're reading from---only here to pass to children
	 * @param warner the Warning instance to use.
	 * @param map the map we're building.
	 * @param elem the current tag.
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException on SP map format error
	 */
	private static void parseChild(final Iterable<XMLEvent> stream,
			final Warning warner, final SPMap map, 
			final StartElement elem, final IDFactory idFactory) throws SPFormatException {
		final String type = elem.getName().getLocalPart();
		if ("player".equalsIgnoreCase(type)) {
			map.addPlayer(new PlayerReader()
					.parse(elem, stream, map.getPlayers(), warner, idFactory));
		} else if (!"row".equalsIgnoreCase(type)) {
			// We deliberately ignore "row"; that had been a "continue",
			// but we want to extract this as a method.
			if ("tile".equalsIgnoreCase(type)) {
				map.addTile(new TileReader().parse(
						elem, stream, map.getPlayers(), warner, idFactory));
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
	 * Write an instance of the type to a Writer.
	 * 
	 * @param <S> the actual type of the object to write
	 * @param obj
	 *            the object to write
	 * @param writer
	 *            the Writer we're currently writing to
	 * @param inclusion
	 *            whether to create 'include' tags and separate files for
	 *            elements whose 'file' is different from that of their parents
	 * @throws IOException
	 *             on I/O error while writing
	 */
	@Override
	public <S extends SPMap> void write(final S obj, final Writer writer,
			final boolean inclusion) throws IOException {
		final ReaderAdapter adapter = new ReaderAdapter();
		writer.write("<map version=\"");
		writer.write(obj.getVersion());
		writer.write("\" rows=\"");
		writer.write(obj.rows());
		writer.write("\" columns=\"");
		writer.write(obj.cols());
		if (!obj.getPlayers().getCurrentPlayer().getName().isEmpty()) {
			writer.write("\" current_player=\"");
			writer.write(obj.getPlayers().getCurrentPlayer().getId());
		}
		writer.write("\">\n");
		for (Player player : obj.getPlayers()) {
			writer.write('\t');
			writeOrInclude(player, adapter, writer, inclusion, obj.getFile());
			writer.write('\n');
		}
		for (int i = 0; i < obj.rows(); i++) {
			boolean anyTiles = false;
			for (int j = 0; j < obj.cols(); j++) {
				final Tile tile = obj.getTile(i, j);
				if (!anyTiles && !tile.isEmpty()) {
					anyTiles = true;
					writer.write("\t<row index=\"");
					writer.write(i);
					writer.write("\">\n");
				}
				if (!tile.isEmpty()) {
					writer.write("\t\t");
					writeOrInclude(tile, adapter, writer, inclusion, obj.getFile());
					writer.write('\n');
				}
			}
			if (anyTiles) {
				writer.write("\t</row>\n");
			}
		}
		writer.write("</map>");
	}
	/**
	 * Write something to this Writer or to its own if appropriate.
	 * @param <T> the type of object
	 * @param obj the object
	 * @param adapter the adapter to get the write helper to do the actual writing
	 * @param writer the writer to write it, or the 'include' tag, to
	 * @param inclusion whether we're even doing inclusion
	 * @param file the file we're in now
	 * @throws IOException on I/O error
	 */
	private static <T extends XMLWritable> void writeOrInclude(final T obj,
			final ReaderAdapter adapter, final Writer writer,
			final boolean inclusion, final String file) throws IOException {
		if (!inclusion || obj.getFile().equals(file)) {
			adapter.write(obj, writer, inclusion);
		} else {
			writer.write("<include file=\"");
			writer.write(adapter.writeForInclusion(obj));
			writer.write("\" />");
		}
	}
}

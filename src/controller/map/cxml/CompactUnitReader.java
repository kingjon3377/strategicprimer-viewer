package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.XMLWritable;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import util.IteratorWrapper;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactUnitReader extends AbstractCompactReader implements CompactReader<Unit> {
	/**
	 * Singleton.
	 */
	private CompactUnitReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactUnitReader READER = new CompactUnitReader();
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
	public Unit read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "unit");
		requireNonEmptyParameter(element, "name", false, warner);
		requireNonEmptyParameter(element, "owner", false, warner);
		final Unit retval = new Unit(
				players.getPlayer(Integer.parseInt(ensureNumeric(getParameter(
						element, "owner", "-1")))), parseKind(element, warner),
				getParameter(element, "name", ""), getOrGenerateID(element,
						warner, idFactory), getFile(stream));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				retval.addMember(parseChild(event.asStartElement(), stream, players, idFactory, warner));
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
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
					CompactTextReader.READER, CompactTownReader.READER });

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
	private UnitMember parseChild(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final IDFactory idFactory, final Warning warner) throws SPFormatException {
		final String name = element.getName().getLocalPart();
		for (AbstractCompactReader item : readers) {
			if (item.isSupportedTag(name)) {
				XMLWritable retval = ((CompactReader<? extends XMLWritable>) item).read(
						element, stream, players, warner, idFactory);
				if (retval instanceof UnitMember) {
					return (UnitMember) retval;
				} else {
					throw new UnwantedChildException("unit", element.getName()
							.getLocalPart(), element.getLocation()
							.getLineNumber());
				}
			}
		}
		throw new UnwantedChildException("tile", name, element.getLocation().getLineNumber());
	}
	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the
	 * empty string.
	 *
	 * @param element the current element
	 * @param warner the Warning instance to use
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private static String parseKind(final StartElement element,
			final Warning warner) throws SPFormatException {
		String retval = "";
		try {
			retval = getParameterWithDeprecatedForm(element, // NOPMD
					"kind", "type", warner);
		} catch (final MissingParameterException except) {
			warner.warn(except);
			return ""; // NOPMD
		}
		if (retval.isEmpty()) {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "kind", element.getLocation()
					.getLineNumber()));
		}
		return retval;
	}
	/**
	 * @param string a string which should be numeric or empty
	 * @return it, or "-1" if it's empty.
	 */
	private static String ensureNumeric(final String string) {
		return string.isEmpty() ? "-1" : string;
	}
	/**
	 * @param tag a tag
	 * @return whether it's one we can read
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "unit".equalsIgnoreCase(tag);
	}
	/**
	 * Write an object to a stream.
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param file The file we're writing to.
	 * @param inclusion Whether to change files if a sub-object was read from a different file
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final Unit obj, final String file, final boolean inclusion,
			final int indent) throws IOException {
		out.append(indent(indent));
		out.append("<unit owner=\"");
		out.append(Integer.toString(obj.getOwner().getPlayerId()));
		if (!obj.getKind().isEmpty()) {
			out.append("\" kind=\"");
			out.append(obj.getKind());
		}
		if (!obj.getName().isEmpty()) {
			out.append("\" name=\"");
			out.append(obj.getName());
		}
		out.append("\" id=\"");
		out.append(Integer.toString(obj.getID()));
		out.append('"');
		if (obj.iterator().hasNext()) {
			out.append(">\n");
			for (final UnitMember member : obj) {
				CompactReaderAdapter.ADAPTER.write(out, member, file, inclusion, indent + 1);
			}
			out.append(indent(indent));
			out.append("</unit>\n");
		} else {
			out.append(" />\n");
		}
	}
}

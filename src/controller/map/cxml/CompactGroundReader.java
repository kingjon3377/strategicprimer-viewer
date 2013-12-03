package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.Nullable;

import model.map.IPlayerCollection;
import model.map.fixtures.Ground;
import util.IteratorWrapper;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 *
 * @author Jonathan Lovelace
 *
 */
public final class CompactGroundReader extends AbstractCompactReader<Ground> {
	/**
	 * Singleton.
	 */
	private CompactGroundReader() {
		// Singleton.
	}

	/**
	 * Singleton object.
	 */
	public static final CompactGroundReader READER = new CompactGroundReader();

	/**
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Ground read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IPlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "ground");
		final String kind = getParamWithDeprecatedForm(element, "kind",
				"ground", warner);
		requireNonEmptyParameter(element, "exposed", true, warner);
		spinUntilEnd(assertNotNullQName(element.getName()), stream);
		final Ground retval = new Ground(kind,
				Boolean.parseBoolean(getParameter(element, "exposed")));
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	/**
	 * @param tag a tag
	 * @return whether it's one we support
	 */
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
		return "ground".equalsIgnoreCase(tag);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final Ground obj, final int indent)
			throws IOException {
		for (int i = 0; i < indent; i++) {
			out.append('\t');
		}
		out.append("<ground kind=\"");
		out.append(obj.getKind());
		out.append("\" exposed=\"");
		out.append(Boolean.toString(obj.isExposed()));
		out.append('"').append(imageXML(obj)).append(" />\n");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactGroundReader";
	}
}

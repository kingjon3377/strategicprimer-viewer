package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Player;
import model.map.PlayerCollection;
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
public final class CompactPlayerReader extends AbstractCompactReader implements
		CompactReader<Player> {
	/**
	 * Singleton.
	 */
	private CompactPlayerReader() {
		// Singleton.
	}

	/**
	 * Singleton object.
	 */
	public static final CompactPlayerReader READER = new CompactPlayerReader();

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
	public Player read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final PlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "player");
		requireNonEmptyParameter(element, "number", true, warner);
		requireNonEmptyParameter(element, "code_name", true, warner);
		spinUntilEnd(assertNotNullQName(element.getName()), stream);
		return new Player(Integer.parseInt(getParameter(element, "number")),
				getParameter(element, "code_name"));
	}

	/**
	 * @param tag a tag
	 * @return whether it's one we can read
	 */
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
		return "player".equalsIgnoreCase(tag);
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
	public void write(final Writer out, final Player obj, final int indent)
			throws IOException {
		out.append(indent(indent));
		out.append("<player number=\"");
		out.append(Integer.toString(obj.getPlayerId()));
		out.append("\" code_name=\"");
		out.append(obj.getName());
		out.append("\" />\n");
	}
}

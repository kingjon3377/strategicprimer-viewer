package controller.map.cxml;

import java.io.IOException;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IPlayerCollection;
import model.map.Player;

import org.eclipse.jdt.annotation.Nullable;

import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 *
 * @author Jonathan Lovelace
 *
 */
public final class CompactPlayerReader extends AbstractCompactReader<Player> {
	/**
	 * Singleton object.
	 */
	public static final CompactPlayerReader READER = new CompactPlayerReader();

	/**
	 * Singleton.
	 */
	private CompactPlayerReader() {
		// Singleton.
	}

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
			final IPlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "player");
		requireNonEmptyParameter(element, "number", true, warner);
		requireNonEmptyParameter(element, "code_name", true, warner);
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
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
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final Player obj, final int indent)
			throws IOException {
		ostream.append(indent(indent));
		ostream.append("<player number=\"");
		ostream.append(Integer.toString(obj.getPlayerId()));
		ostream.append("\" code_name=\"");
		ostream.append(obj.getName());
		ostream.append("\" />\n");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactPlayerReader";
	}
}

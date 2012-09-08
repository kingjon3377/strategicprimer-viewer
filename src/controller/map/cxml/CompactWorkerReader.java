package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.PlayerCollection;
import model.map.fixtures.mobile.Worker;

/**
 * A reader for Workers.
 * @author Jonathan Lovelace
 */
public final class CompactWorkerReader extends AbstractCompactReader implements
		CompactReader<Worker> {
	/**
	 * Singleton.
	 */
	private CompactWorkerReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactWorkerReader READER = new CompactWorkerReader();
	/**
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed worker
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Worker read(final StartElement element, final IteratorWrapper<XMLEvent> stream,
			final PlayerCollection players, final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "worker");
		final Worker retval = new Worker(getParameter(element, "name"), getFile(stream),
				getOrGenerateID(element, warner, idFactory));
		spinUntilEnd(element.getName(), stream);
		return retval;
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
	public void write(final Writer out, final Worker obj, final String file, final boolean inclusion,
			final int indent) throws IOException {
		out.append(indent(indent));
		out.append("<worker name=\"");
		out.append(obj.getName());
		out.append("\" id=\"");
		out.append(Integer.toString(obj.getID()));
		out.append("\" />\n");
	}

	/**
	 * TODO: extend when Worker grows sub-tags.
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "worker".equals(tag);
	}

}

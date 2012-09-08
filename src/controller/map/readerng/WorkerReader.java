package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.PlayerCollection;
import model.map.fixtures.mobile.Worker;
/**
 * A reader for Workers.
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class WorkerReader implements INodeHandler<Worker> {
	/**
	 * @return the class this knows how to write.
	 */
	@Override
	public Class<Worker> writes() {
		return Worker.class;
	}
	/**
	 * TODO: expand as it gets subtags.
	 * @return the list of tags this knows how to read.
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("worker");
	}
	/**
	 * Parse a worker from XML.
	 * @param element the current tag
	 * @param stream the stream to read more tags from
	 * @param players ignored
	 * @param warner the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed Worker
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Worker parse(final StartElement element, final Iterable<XMLEvent> stream,
			final PlayerCollection players, final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		return new Worker(XMLHelper.getAttribute(element, "name"),
				XMLHelper.getFile(stream), XMLHelper.getOrGenerateID(element, warner, idFactory));
	}
	/**
	 * @param obj a worker
	 * @return the SPIR representing it.
	 */
	@Override
	public SPIntermediateRepresentation write(final Worker obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation("worker");
		retval.addAttribute("name", obj.getName());
		retval.addAttribute("id", Integer.toString(obj.getID()));
		return retval;
	}

}

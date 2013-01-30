package controller.map.readerng;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.XMLWritable;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
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
		final Worker retval = new Worker(
				XMLHelper.getAttribute(element, "name"),
				XMLHelper.getAttribute(element, "race", "human"),
				XMLHelper.getOrGenerateID(element,
						warner, idFactory));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final XMLWritable result = ReaderAdapter.ADAPTER.parse(
						event.asStartElement(), stream, players, warner,
						idFactory);
				if (result instanceof Job) {
					retval.addJob((Job) result);
				} else {
					throw new UnwantedChildException(element.getName()
							.getLocalPart(), event.asStartElement().getName()
							.getLocalPart(), event.getLocation()
							.getLineNumber());
				}
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return retval;
	}
	/**
	 * @param obj a worker
	 * @return the SPIR representing it.
	 */
	@Override
	public SPIntermediateRepresentation write(final Worker obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation("worker");
		retval.addAttribute("name", obj.getName());
		if (!"human".equals(obj.getRace())) {
			retval.addAttribute("race", obj.getRace());
		}
		retval.addAttribute("id", Integer.toString(obj.getID()));
		for (Job job : obj) {
			retval.addChild(ReaderAdapter.ADAPTER.write(job));
		}
		return retval;
	}

}

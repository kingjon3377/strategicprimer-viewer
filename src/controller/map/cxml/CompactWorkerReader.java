package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

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
		final Worker retval = new Worker(getParameter(element, "name"),
				getParameter(element, "race", "human"), getOrGenerateID(element, warner, idFactory));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if ("job".equalsIgnoreCase(event.asStartElement().getName().getLocalPart())) {
					retval.addJob(parseJob(event.asStartElement(), stream));
				} else {
					throw new UnwantedChildException(element.getName().getLocalPart(), event
							.asStartElement().getName().getLocalPart(), event
							.getLocation().getLineNumber());
				}
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return retval;
	}
	/**
	 * Parse a Job.
	 * @param element the element to parse
	 * @param stream the stream to read further elements from (FIXME: do we need this parameter?)
	 * @return the parsed job
	 * @throws SPFormatException on SP format problem
	 */
	public Job parseJob(final StartElement element,
			final IteratorWrapper<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, "job");
		final Job retval = new Job(getParameter(element, "name"),
				Integer.parseInt(getParameter(element, "level")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if ("skill".equalsIgnoreCase(event.asStartElement().getName().getLocalPart())) {
					retval.addSkill(parseSkill(event.asStartElement(), stream));
				} else {
					throw new UnwantedChildException(element.getName().getLocalPart(), event
							.asStartElement().getName().getLocalPart(), event
							.getLocation().getLineNumber());
				}
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return retval;
	}
	/**
	 * Parse a Skill.
	 * @param element the element to parse
	 * @param stream the stream to read further elements from (FIXME: do we need this parameter?)
	 * @return the parsed skill
	 * @throws SPFormatException on SP format problem
	 */
	public Skill parseSkill(final StartElement element,
			final IteratorWrapper<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, "skill");
		spinUntilEnd(element.getName(), stream);
		return new Skill(getParameter(element, "name"),
				Integer.parseInt(getParameter(element, "level")),
				Integer.parseInt(getParameter(element, "hours")));
	}
	/**
	 * Write an object to a stream.
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final Worker obj, final int indent) throws IOException {
		out.append(indent(indent));
		out.append("<worker name=\"");
		out.append(obj.getName());
		if (!"human".equals(obj.getRace())) {
			out.append("\" race=\"");
			out.append(obj.getRace());
		}
		out.append("\" id=\"");
		out.append(Integer.toString(obj.getID()));
		out.append('"');
		if (obj.iterator().hasNext()) {
			out.append(">\n");
			for (Job job : obj) {
				CompactReaderAdapter.ADAPTER.write(out, job, indent + 1);
			}
			out.append(indent(indent));
			out.append("</worker>\n");
		} else {
			out.append(" />\n");
		}
	}
	/**
	 * Write a Job to a stream.
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	public void writeJob(final Writer out, final Job obj, final int indent) throws IOException {
		out.append(indent(indent));
		out.append("<job name=\"");
		out.append(obj.getName());
		out.append("\" level=\"");
		out.append(Integer.toString(obj.getLevel()));
		out.append('"');
		if (obj.iterator().hasNext()) {
			out.append(">\n");
			for (Skill skill : obj) {
				CompactReaderAdapter.ADAPTER.write(out, skill, indent + 1);
			}
			out.append(indent(indent));
			out.append("</job>\n");
		} else {
			out.append(" />\n");
		}
	}

	/**
	 * Write a Skill to a stream.
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	public void writeSkill(final Writer out, final Skill obj, final int indent) throws IOException {
		out.append(indent(indent));
		out.append("<skill name=\"");
		out.append(obj.getName());
		out.append("\" level=\"");
		out.append(Integer.toString(obj.getLevel()));
		out.append("\" hours=\"");
		out.append(Integer.toString(obj.getHours()));
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

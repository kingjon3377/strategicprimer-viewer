package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.mobile.worker.WorkerStats;

import org.eclipse.jdt.annotation.Nullable;

import util.IteratorWrapper;
import util.Warning;
import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for Workers.
 *
 * @author Jonathan Lovelace
 */
public final class CompactWorkerReader extends AbstractCompactReader<Worker> {
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
	public Worker read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final PlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "worker");
		final Worker retval = new Worker(getParameter(element, "name"),
				getParameter(element, "race", "human"), getOrGenerateID(
						element, warner, idFactory));
		retval.setImage(getParameter(element, "image", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final String iLocal = event.asStartElement().getName()
						.getLocalPart();
				assert iLocal != null;
				if ("job".equalsIgnoreCase(iLocal)) {
					retval.addJob(parseJob(
							assertNotNullStartElement(event.asStartElement()),
							stream, warner));
				} else if ("stats".equalsIgnoreCase(iLocal)) {
					retval.setStats(parseStats(
							assertNotNullStartElement(event.asStartElement()),
							stream));
				} else {
					final String oLocal = element.getName().getLocalPart();
					assert oLocal != null;
					throw new UnwantedChildException(oLocal, iLocal, event
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
	 * Parse the worker's stats.
	 *
	 * @param element the element to parse
	 * @param stream the stream to read further elements from
	 * @return the parsed stats
	 * @throws SPFormatException on SP format problem
	 */
	private static WorkerStats parseStats(final StartElement element,
			final IteratorWrapper<XMLEvent> stream) throws SPFormatException {
		requireTag(element, "stats");
		final WorkerStats retval = new WorkerStats(
				Integer.parseInt(getParameter(element, "hp")),
				Integer.parseInt(getParameter(element, "max")),
				Integer.parseInt(getParameter(element, "str")),
				Integer.parseInt(getParameter(element, "dex")),
				Integer.parseInt(getParameter(element, "con")),
				Integer.parseInt(getParameter(element, "int")),
				Integer.parseInt(getParameter(element, "wis")),
				Integer.parseInt(getParameter(element, "cha")));
		spinUntilEnd(assertNotNullQName(element.getName()), stream);
		return retval;
	}

	/**
	 * Parse a Job.
	 *
	 * @param element the element to parse
	 * @param stream the stream to read further elements from
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed job
	 * @throws SPFormatException on SP format problem
	 */
	public static Job parseJob(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final Warning warner)
			throws SPFormatException {
		requireTag(element, "job");
		final Job retval = new Job(getParameter(element, "name"),
				Integer.parseInt(getParameter(element, "level")));
		if (hasParameter(element, "hours")) {
			warner.warn(new UnsupportedPropertyException("job", "hours",
					element.getLocation().getLineNumber()));
		}
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final String iLocal = event.asStartElement().getName()
						.getLocalPart();
				assert iLocal != null;
				if ("skill".equalsIgnoreCase(iLocal)) {
					retval.addSkill(parseSkill(
							assertNotNullStartElement(event.asStartElement()),
							warner));
					spinUntilEnd(assertNotNullQName(event.asStartElement()
							.getName()), stream);
				} else {
					final String oLocal = element.getName()
							.getLocalPart();
					assert oLocal != null;
					throw new UnwantedChildException(oLocal, iLocal, event.getLocation()
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
	 * Parse a Skill.
	 *
	 * @param element the element to parse
	 * @param warner the Warning instance to use
	 * @return the parsed skill
	 * @throws SPFormatException on SP format problem
	 */
	public static Skill parseSkill(final StartElement element,
			final Warning warner) throws SPFormatException {
		requireTag(element, "skill");
		final Skill retval = new Skill(getParameter(element, "name"),
				Integer.parseInt(getParameter(element, "level")),
				Integer.parseInt(getParameter(element, "hours")));
		if ("miscellaneous".equals(retval.getName()) && retval.getLevel() > 0) {
			warner.warn(new DeprecatedPropertyException("skill",
					"miscellaneous", "other", element.getLocation()
							.getLineNumber()));
		}
		return retval;
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
	public void write(final Writer out, final Worker obj, final int indent)
			throws IOException {
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
		out.append(imageXML(obj));
		if (obj.iterator().hasNext() || obj.getStats() != null) {
			out.append(">\n");
			writeStats(out, obj.getStats(), indent + 1);
			for (final Job job : obj) {
				if (job != null) {
					writeJob(out, job, indent + 1);
				}
			}
			out.append(indent(indent));
			out.append("</worker>\n");
		} else {
			out.append(" />\n");
		}
	}

	/**
	 * Write the worker's stats.
	 *
	 * @param out the writer to write to
	 * @param stats the object to write
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 */
	private static void writeStats(final Writer out,
			@Nullable final WorkerStats stats, final int indent)
			throws IOException {
		if (stats != null) {
			out.append(indent(indent));
			out.append("<stats hp=\"");
			out.append(Integer.toString(stats.getHitPoints()));
			out.append("\" max=\"");
			out.append(Integer.toString(stats.getMaxHitPoints()));
			out.append("\" str=\"");
			out.append(Integer.toString(stats.getStrength()));
			out.append("\" dex=\"");
			out.append(Integer.toString(stats.getDexterity()));
			out.append("\" con=\"");
			out.append(Integer.toString(stats.getConstitution()));
			out.append("\" int=\"");
			out.append(Integer.toString(stats.getIntelligence()));
			out.append("\" wis=\"");
			out.append(Integer.toString(stats.getWisdom()));
			out.append("\" cha=\"");
			out.append(Integer.toString(stats.getCharisma()));
			out.append("\" />\n");
		}
	}

	/**
	 * Write a Job to a stream.
	 *
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeJob(final Writer out, final Job obj,
			final int indent) throws IOException {
		out.append(indent(indent));
		out.append("<job name=\"");
		out.append(obj.getName());
		out.append("\" level=\"");
		out.append(Integer.toString(obj.getLevel()));
		out.append('"');
		if (obj.iterator().hasNext()) {
			out.append(">\n");
			for (final Skill skill : obj) {
				if (skill != null) {
					writeSkill(out, skill, indent + 1);
				}
			}
			out.append(indent(indent));
			out.append("</job>\n");
		} else {
			out.append(" />\n");
		}
	}

	/**
	 * Write a Skill to a stream.
	 *
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeSkill(final Writer out, final Skill obj,
			final int indent) throws IOException {
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
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
		return "worker".equals(tag);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactWorkerReader";
	}
}

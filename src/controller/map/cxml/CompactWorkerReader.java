package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IPlayerCollection;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.mobile.worker.WorkerStats;

import org.eclipse.jdt.annotation.Nullable;

import util.IteratorWrapper;
import util.NullCleaner;
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
	 * Singleton object.
	 */
	public static final CompactWorkerReader READER = new CompactWorkerReader();

	/**
	 * Singleton.
	 */
	private CompactWorkerReader() {
		// Singleton.
	}

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
			final IPlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "worker");
		final Worker retval = new Worker(getParameter(element, "name"),
				getParameter(element, "race", "human"), getOrGenerateID(
						element, warner, idFactory));
		retval.setImage(getParameter(element, "image", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if ("job".equalsIgnoreCase(NullCleaner.assertNotNull(event
						.asStartElement().getName().getLocalPart()))) {
					retval.addJob(parseJob(
							assertNotNullStartElement(event.asStartElement()),
							stream, warner));
				} else if ("stats".equalsIgnoreCase(NullCleaner
						.assertNotNull(event.asStartElement().getName()
								.getLocalPart()))) {
					retval.setStats(parseStats(
							assertNotNullStartElement(event.asStartElement()),
							stream));
				} else {
					throw new UnwantedChildException(
							NullCleaner.assertNotNull(element.getName()
									.getLocalPart()),
							NullCleaner.assertNotNull(event.asStartElement()
									.getName().getLocalPart()), event
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
				if ("skill".equalsIgnoreCase(NullCleaner.assertNotNull(event
						.asStartElement().getName().getLocalPart()))) {
					retval.addSkill(parseSkill(
							assertNotNullStartElement(event.asStartElement()),
							warner));
					spinUntilEnd(assertNotNullQName(event.asStartElement()
							.getName()), stream);
				} else {
					throw new UnwantedChildException(
							NullCleaner.assertNotNull(element.getName()
									.getLocalPart()),
							NullCleaner.assertNotNull(event.asStartElement()
									.getName().getLocalPart()), event
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
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer ostream, final Worker obj, final int indent)
			throws IOException {
		ostream.append(indent(indent));
		ostream.append("<worker name=\"");
		ostream.append(obj.getName());
		if (!"human".equals(obj.getRace())) {
			ostream.append("\" race=\"");
			ostream.append(obj.getRace());
		}
		ostream.append("\" id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append('"');
		ostream.append(imageXML(obj));
		if (obj.iterator().hasNext() || obj.getStats() != null) {
			ostream.append(">\n");
			writeStats(ostream, obj.getStats(), indent + 1);
			for (final Job job : obj) {
				if (job != null) {
					writeJob(ostream, job, indent + 1);
				}
			}
			ostream.append(indent(indent));
			ostream.append("</worker>\n");
		} else {
			ostream.append(" />\n");
		}
	}

	/**
	 * Write the worker's stats.
	 *
	 * @param ostream the writer to write to
	 * @param stats the object to write
	 * @param indent the current indentation level
	 * @throws IOException on I/O error
	 */
	private static void writeStats(final Writer ostream,
			@Nullable final WorkerStats stats, final int indent)
			throws IOException {
		if (stats != null) {
			ostream.append(indent(indent));
			ostream.append("<stats hp=\"");
			ostream.append(Integer.toString(stats.getHitPoints()));
			ostream.append("\" max=\"");
			ostream.append(Integer.toString(stats.getMaxHitPoints()));
			ostream.append("\" str=\"");
			ostream.append(Integer.toString(stats.getStrength()));
			ostream.append("\" dex=\"");
			ostream.append(Integer.toString(stats.getDexterity()));
			ostream.append("\" con=\"");
			ostream.append(Integer.toString(stats.getConstitution()));
			ostream.append("\" int=\"");
			ostream.append(Integer.toString(stats.getIntelligence()));
			ostream.append("\" wis=\"");
			ostream.append(Integer.toString(stats.getWisdom()));
			ostream.append("\" cha=\"");
			ostream.append(Integer.toString(stats.getCharisma()));
			ostream.append("\" />\n");
		}
	}

	/**
	 * Write a Job to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeJob(final Writer ostream, final Job obj,
			final int indent) throws IOException {
		ostream.append(indent(indent));
		ostream.append("<job name=\"");
		ostream.append(obj.getName());
		ostream.append("\" level=\"");
		ostream.append(Integer.toString(obj.getLevel()));
		ostream.append('"');
		if (obj.iterator().hasNext()) {
			ostream.append(">\n");
			for (final Skill skill : obj) {
				if (skill != null) {
					writeSkill(ostream, skill, indent + 1);
				}
			}
			ostream.append(indent(indent));
			ostream.append("</job>\n");
		} else {
			ostream.append(" />\n");
		}
	}

	/**
	 * Write a Skill to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeSkill(final Writer ostream, final Skill obj,
			final int indent) throws IOException {
		ostream.append(indent(indent));
		ostream.append("<skill name=\"");
		ostream.append(obj.getName());
		ostream.append("\" level=\"");
		ostream.append(Integer.toString(obj.getLevel()));
		ostream.append("\" hours=\"");
		ostream.append(Integer.toString(obj.getHours()));
		ostream.append("\" />\n");
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

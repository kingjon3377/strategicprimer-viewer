package controller.map.cxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.mobile.worker.WorkerStats;
import org.eclipse.jdt.annotation.Nullable;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for Workers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class CompactWorkerReader extends AbstractCompactReader<Worker> {
	/**
	 * Singleton object.
	 */
	public static final CompactReader<Worker> READER = new CompactWorkerReader();

	/**
	 * Singleton.
	 */
	private CompactWorkerReader() {
		// Singleton.
	}

	/**
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed worker
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public Worker read(final StartElement element,
					   final IteratorWrapper<XMLEvent> stream,
					   final IMutablePlayerCollection players, final Warning warner,
					   final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "worker");
		final Worker retval = new Worker(getParameter(element, "name"),
												getParameter(element, "race", "human"),
												getOrGenerateID(
														element, warner, idFactory));
		retval.setImage(getParameter(element, "image", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if ("job".equalsIgnoreCase(NullCleaner.assertNotNull(event
																			 .asStartElement()
																			 .getName()
																			 .getLocalPart()))) {
					retval.addJob(parseJob(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream, warner));
				} else if ("stats".equalsIgnoreCase(NullCleaner
															.assertNotNull(
																	event
																			.asStartElement()
																			.getName()
																			.getLocalPart()))) {
					retval.setStats(parseStats(
							NullCleaner.assertNotNull(event.asStartElement()),
							stream));
				} else {
					throw new UnwantedChildException(element.getName(),
							                                event.asStartElement()
									                                .getName(),
							                                event.getLocation());
				}
			} else if (event.isEndElement()
							   &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		return retval;
	}

	/**
	 * Parse the worker's stats.
	 *
	 * @param element the element to parse
	 * @param stream  the stream to read further elements from
	 * @return the parsed stats
	 * @throws SPFormatException on SP format problem
	 */
	private static WorkerStats parseStats(final StartElement element,
										  final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, "stats");
		final WorkerStats retval =
				new WorkerStats(getIntegerParameter(element, "hp"),
									   getIntegerParameter(element, "max"),
									   getIntegerParameter(element, "str"),
									   getIntegerParameter(element, "dex"),
									   getIntegerParameter(element, "con"),
									   getIntegerParameter(element, "int"),
									   getIntegerParameter(element, "wis"),
									   getIntegerParameter(element, "cha"));
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		return retval;
	}

	/**
	 * Parse a Job.
	 *
	 * @param element the element to parse
	 * @param stream  the stream to read further elements from
	 * @param warner  the Warning instance to use for warnings
	 * @return the parsed job
	 * @throws SPFormatException on SP format problem
	 */
	public static IJob parseJob(final StartElement element,
								final Iterable<XMLEvent> stream, final Warning warner)
			throws SPFormatException {
		requireTag(element, "job");
		final IJob retval =
				new Job(getParameter(element, "name"),
							   getIntegerParameter(element, "level"));
		if (hasParameter(element, "hours")) {
			warner.warn(new UnsupportedPropertyException(element, "hours"));
		}
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if ("skill".equalsIgnoreCase(NullCleaner.assertNotNull(event
																			   .asStartElement()
																			   .getName()
																			   .getLocalPart()))) {
					retval.addSkill(parseSkill(
							NullCleaner.assertNotNull(event.asStartElement()),
							warner));
					spinUntilEnd(NullCleaner.assertNotNull(event.asStartElement()
																   .getName()), stream);
				} else {
					throw new UnwantedChildException(element.getName(),
							                                event.asStartElement()
									                                .getName(),
							                                event.getLocation());
				}
			} else if (event.isEndElement()
							   &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		return retval;
	}

	/**
	 * Parse a Skill.
	 *
	 * @param element the element to parse
	 * @param warner  the Warning instance to use
	 * @return the parsed skill
	 * @throws SPFormatException on SP format problem
	 */
	public static ISkill parseSkill(final StartElement element,
									final Warning warner) throws SPFormatException {
		requireTag(element, "skill");
		final ISkill retval =
				new Skill(getParameter(element, "name"),
								 getIntegerParameter(element, "level"),
								 getIntegerParameter(element, "hours"));
		if ("miscellaneous".equals(retval.getName()) && (retval.getLevel() > 0)) {
			warner.warn(
					new DeprecatedPropertyException(element, "miscellaneous", "other"));
		}
		return retval;
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final Worker obj,
					  final int indent) throws IOException {
		indent(ostream, indent);
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
		if (obj.iterator().hasNext() || (obj.getStats() != null)) {
			ostream.append(">\n");
			writeStats(ostream, obj.getStats(), indent + 1);
			for (final IJob job : obj) {
				if (job instanceof Job) {
					writeJob(ostream, (Job) job, indent + 1);
				}
			}
			indent(ostream, indent);
			ostream.append("</worker>\n");
		} else {
			ostream.append(" />\n");
		}
	}

	/**
	 * Write the worker's stats.
	 *
	 * @param ostream the writer to write to
	 * @param stats   the object to write
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	private static void writeStats(final Appendable ostream,
								   @Nullable final WorkerStats stats, final int indent)
			throws IOException {
		if (stats != null) {
			indent(ostream, indent);
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
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeJob(final Appendable ostream, final Job obj,
								final int indent) throws IOException {
		if ((obj.getLevel() <= 0) && !obj.iterator().hasNext()) {
			return;
		}
		indent(ostream, indent);
		ostream.append("<job name=\"");
		ostream.append(obj.getName());
		ostream.append("\" level=\"");
		ostream.append(Integer.toString(obj.getLevel()));
		ostream.append('"');
		if (obj.iterator().hasNext()) {
			ostream.append(">\n");
			for (final ISkill skill : obj) {
				if (skill instanceof Skill) {
					writeSkill(ostream, skill, indent + 1);
				}
			}
			indent(ostream, indent);
			ostream.append("</job>\n");
		} else {
			ostream.append(" />\n");
		}
	}

	/**
	 * Write a Skill to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeSkill(final Appendable ostream, final ISkill obj,
								  final int indent) throws IOException {
		indent(ostream, indent);
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
	public boolean isSupportedTag(final String tag) {
		return "worker".equals(tag);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactWorkerReader";
	}
}

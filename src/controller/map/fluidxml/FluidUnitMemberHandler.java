package controller.map.fluidxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasPortrait;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.mobile.worker.WorkerStats;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.indent;
import static controller.map.fluidxml.XMLHelper.requireNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeBooleanAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.writeTag;
import static java.lang.Boolean.parseBoolean;
import static util.EqualsAny.equalsAny;
import static util.NullCleaner.assertNotNull;

/**
 * A class to hold XML I/O for workers and animals, the only not-trivially-simple unit
 * members.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
public final class FluidUnitMemberHandler {
	/**
	 * Do not instantiate.
	 */
	private FluidUnitMemberHandler() {
		// Do not instantiate
	}
	/**
	 * Parse a worker from XML.
	 *
	 * @param element   the current tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed Worker
	 * @throws SPFormatException on SP format error
	 */
	public static Worker readWorker(final StartElement element,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "worker");
		final Worker retval = setImage(new Worker(getAttribute(element, "name"),
												getAttribute(element, "race", "human"),
												getOrGenerateID(element, warner,
														idFactory)), element, warner);
		// TODO: Add a setPortrait() method to XMLHelper
		retval.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				switch (startElement.getName().getLocalPart()) {
				case "job":
					retval.addJob(
							readJob(startElement, stream, players, warner, idFactory));
					break;
				case "stats":
					retval.setStats(
							readStats(startElement, stream, players, warner, idFactory));
					break;
				default:
					throw new UnwantedChildException(assertNotNull(element.getName()),
															startElement);
				}
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return retval;
	}
	/**
	 * Parse a job from XML.
	 *
	 * @param element   the current tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed job
	 * @throws SPFormatException on SP format error
	 */
	public static IJob readJob(final StartElement element,
							   final Iterable<XMLEvent> stream,
							   final IMutablePlayerCollection players,
							   final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "job");
		if (hasAttribute(element, "hours")) {
			warner.warn(new UnsupportedPropertyException(element, "hours"));
		}
		final Job retval = new Job(getAttribute(element, "name"),
										  getIntegerAttribute(element, "level"));
		StartElement lastSkill = element;
		boolean anySkills = false;
		boolean onlyOneSkill = true;
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				if ("skill".equals(startElement.getName().getLocalPart())) {
					if (anySkills) {
						onlyOneSkill = false;
					} else {
						anySkills = true;
					}
					retval.addSkill(
							readSkill(startElement, stream, players, warner, idFactory));
					lastSkill = startElement;
				} else {
					throw new UnwantedChildException(assertNotNull(element.getName()),
															startElement);
				}
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		if (anySkills && onlyOneSkill) {
			final String skill = retval.iterator().next().getName();
			if (equalsAny(skill, IJob.SUSPICIOUS_SKILLS) || skill.equals(retval.getName())) {
				warner.warn(new UnwantedChildException(element.getName(),
															  new QName(ISPReader.NAMESPACE,
																			   skill),
															  lastSkill.getLocation(),
															  new DeprecatedPropertyException(lastSkill,
																									 skill,
																									 "miscellaneous")));
			}
		}
		return retval;
	}
	/**
	 * Parse a skill from XML.
	 *
	 * @param element   the current tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed job
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static ISkill readSkill(final StartElement element,
								   final Iterable<XMLEvent> stream,
								   final IMutablePlayerCollection players,
								   final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "skill");
		requireNonEmptyAttribute(element, "name", true, warner);
		requireNonEmptyAttribute(element, "level", true, warner);
		requireNonEmptyAttribute(element, "hours", true, warner);
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final ISkill retval = new Skill(getAttribute(element, "name"),
											  getIntegerAttribute(element, "level"),
											  getIntegerAttribute(element, "hours"));
		if ("miscellaneous".equals(retval.getName()) && (retval.getLevel() > 0)) {
			warner.warn(
					new DeprecatedPropertyException(element, "miscellaneous", "other"));
		}
		return retval;
	}
	/**
	 * Parse stats from XML.
	 *
	 * @param element   the current tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed stats object
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static WorkerStats readStats(final StartElement element,
										final Iterable<XMLEvent> stream,
										final IMutablePlayerCollection players,
										final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "stats");
		final WorkerStats retval =
				new WorkerStats(getIntegerAttribute(element, "hp"),
									   getIntegerAttribute(element, "max"),
									   getIntegerAttribute(element, "str"),
									   getIntegerAttribute(element, "dex"),
									   getIntegerAttribute(element, "con"),
									   getIntegerAttribute(element, "int"),
									   getIntegerAttribute(element, "wis"),
									   getIntegerAttribute(element, "cha"));
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return retval;
	}
	/**
	 * Write a worker to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an IWorker
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeWorker(final Appendable ostream, final Object obj,
								   final int indent) throws IOException {
		if (!(obj instanceof IWorker)) {
			throw new IllegalArgumentException("Can only write IWorker");
		}
		final IWorker work = (IWorker) obj;
		writeTag(ostream, "worker", indent);
		writeAttribute(ostream, "name", work.getName());
		if (!"human".equals(work.getRace())) {
			writeAttribute(ostream, "race", work.getRace());
		}
		writeIntegerAttribute(ostream, "id", work.getID());
		writeImage(ostream, work);
		// TODO: Add portraitXML() to XMLHelper
		if (work instanceof HasPortrait) {
			writeNonEmptyAttribute(ostream, "portrait",
					((HasPortrait) work).getPortrait());
		}
		final WorkerStats stats;
		if (work instanceof Worker) {
			stats = ((Worker) work).getStats();
		} else {
			stats = null;
		}
		if (work.iterator().hasNext() || (stats != null)) {
			ostream.append(">\n");
			if (stats != null) {
				writeStats(ostream, stats, indent + 1);
			}
			for (final IJob job : work) {
				if (job instanceof Job) {
					writeJob(ostream, job, indent + 1);
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
	 * @param obj   the object to write. Must be a WorkerStats
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	public static void writeStats(final Appendable ostream,
								   final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof WorkerStats)) {
			throw new IllegalArgumentException("Can only write WorkerStats");
		}
		final WorkerStats stats = (WorkerStats) obj;
		writeTag(ostream, "stats", indent);
		writeIntegerAttribute(ostream, "hp", stats.getHitPoints());
		writeIntegerAttribute(ostream, "max", stats.getMaxHitPoints());
		writeIntegerAttribute(ostream, "str", stats.getStrength());
		writeIntegerAttribute(ostream, "dex", stats.getDexterity());
		writeIntegerAttribute(ostream, "con", stats.getConstitution());
		writeIntegerAttribute(ostream, "int", stats.getIntelligence());
		writeIntegerAttribute(ostream, "wis", stats.getWisdom());
		writeIntegerAttribute(ostream, "cha", stats.getCharisma());
		ostream.append(" />\n");
	}
	/**
	 * Write a Job to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an IJob
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeJob(final Appendable ostream, final Object obj,
								final int indent) throws IOException {
		if (!(obj instanceof IJob)) {
			throw new IllegalArgumentException("Can only write IJob");
		}
		final IJob job = (IJob) obj;
		if ((job.getLevel() <= 0) && !job.iterator().hasNext()) {
			return;
		}
		writeTag(ostream, "job", indent);
		writeAttribute(ostream, "name", job.getName());
		writeIntegerAttribute(ostream, "level", job.getLevel());
		if (job.iterator().hasNext()) {
			ostream.append(">\n");
			for (final ISkill skill : job) {
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
	 * @param obj     The object to write. Must be an ISkill
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeSkill(final Appendable ostream, final Object obj,
								  final int indent) throws IOException {
		if (!(obj instanceof ISkill)) {
			throw new IllegalArgumentException("Can only write ISkill");
		}
		final ISkill skl = (ISkill) obj;
		writeTag(ostream, "skill", indent);
		writeAttribute(ostream, "name", skl.getName());
		writeIntegerAttribute(ostream, "level", skl.getLevel());
		writeIntegerAttribute(ostream, "hours", skl.getHours());
		ostream.append(" />\n");
	}

	/**
	 * @param element   the element containing an animal
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the animal
	 * @throws SPFormatException if the data is invalid
	 */
	@SuppressWarnings("UnusedParameters")
	public static Animal readAnimal(final StartElement element,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "animal");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new Animal(getAttribute(element, "kind"), hasAttribute(element,
				"traces"),
						  parseBoolean(getAttribute(element, "talking",
								  "false")),
						  getAttribute(element, "status", "wild"),
						  getOrGenerateID(element, warner, idFactory)), element, warner);
	}

	/**
	 * Write an Animal to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj the object to write. Must be an instance of Animal.
	 * @param indent the current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeAnimal(final Appendable ostream, final Object obj,
								   final int indent) throws IOException {
		if (!(obj instanceof Animal)) {
			throw new IllegalArgumentException("Can only write Animal");
		}
		final Animal fix = (Animal) obj;
		writeTag(ostream, "animal", indent);
		writeAttribute(ostream, "kind", fix.getKind());
		if (fix.isTraces()) {
			writeAttribute(ostream, "traces", "");
		}
		if (fix.isTalking()) {
			writeBooleanAttribute(ostream, "talking", true);
		}
		if (!"wild".equals(fix.getStatus())) {
			writeAttribute(ostream, "status", fix.getStatus());
		}
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeImage(ostream, fix);
		ostream.append(" />\n");
	}
}

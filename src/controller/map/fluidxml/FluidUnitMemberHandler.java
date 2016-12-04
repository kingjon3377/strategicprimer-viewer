package controller.map.fluidxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDRegistrar;
import java.util.Optional;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
 * Foundation; see COPYING or
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
	 * @param parent    the parent tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed Worker
	 * @throws SPFormatException on SP format error
	 */
	public static Worker readWorker(final StartElement element,
									final QName parent,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "worker");
		final Worker retval = setImage(new Worker(getAttribute(element, "name"),
														 getAttribute(element, "race",
																 "human"),
														 getOrGenerateID(element, warner,
																 idFactory)), element,
				warner);
		retval.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				switch (startElement.getName().getLocalPart()) {
				case "job":
					retval.addJob(
							readJob(startElement, element.getName(), stream, players,
									warner, idFactory));
					break;
				case "stats":
					retval.setStats(
							readStats(startElement, element.getName(), stream, players,
									warner, idFactory));
					break;
				default:
					throw new UnwantedChildException(assertNotNull(element.getName()),
															startElement);
				}
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		return retval;
	}

	/**
	 * Parse a job from XML.
	 *
	 * @param element   the current tag
	 * @param parent    the parent tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed job
	 * @throws SPFormatException on SP format error
	 */
	public static IJob readJob(final StartElement element,
							   final QName parent,
							   final Iterable<XMLEvent> stream,
							   final IMutablePlayerCollection players,
							   final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "job");
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
							readSkill(startElement, element.getName(), stream, players,
									warner, idFactory));
					lastSkill = startElement;
				} else {
					throw new UnwantedChildException(assertNotNull(element.getName()),
															startElement);
				}
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		if (anySkills && onlyOneSkill) {
			final String skill = retval.iterator().next().getName();
			if (equalsAny(skill, IJob.SUSPICIOUS_SKILLS) ||
						skill.equals(retval.getName())) {
				warner.warn(new UnwantedChildException(element.getName(),
															  new QName(ISPReader
																				.NAMESPACE,
																			   skill),
															  lastSkill.getLocation(),
															  new
																	  DeprecatedPropertyException(lastSkill,
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
	 * @param parent    the parent tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed job
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static ISkill readSkill(final StartElement element,
								   final QName parent,
								   final Iterable<XMLEvent> stream,
								   final IMutablePlayerCollection players,
								   final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "skill");
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
	 * @param parent    the parent tag
	 * @param stream    the stream to read more tags from
	 * @param players   ignored
	 * @param warner    the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed stats object
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static WorkerStats readStats(final StartElement element,
										final QName parent,
										final Iterable<XMLEvent> stream,
										final IMutablePlayerCollection players,
										final Warning warner, final IDRegistrar
																	  idFactory)
			throws SPFormatException {
		requireTag(element, parent, "stats");
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
	 * Write a worker to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeWorker(final XMLStreamWriter ostream, final Object obj,
								   final int indent) throws XMLStreamException {
		if (!(obj instanceof IWorker)) {
			throw new IllegalArgumentException("Can only write IWorker");
		}
		final IWorker work = (IWorker) obj;
		final Optional<WorkerStats> stats = Optional.ofNullable(work.getStats());
		final boolean hasJobs = work.iterator().hasNext();
		writeTag(ostream, "worker", indent, !hasJobs && !stats.isPresent());
		writeAttribute(ostream, "name", work.getName());
		if (!"human".equals(work.getRace())) {
			writeAttribute(ostream, "race", work.getRace());
		}
		writeIntegerAttribute(ostream, "id", work.getID());
		writeImage(ostream, work);
		if (work instanceof HasPortrait) {
			writeNonEmptyAttribute(ostream, "portrait",
					((HasPortrait) work).getPortrait());
		}
		if (stats.isPresent()) {
			writeStats(ostream, stats.get(), indent + 1);
		}
		for (final IJob job : work) {
			if (job instanceof Job) {
				writeJob(ostream, job, indent + 1);
			}
		}
		if (hasJobs || stats.isPresent()) {
			indent(ostream, indent);
			ostream.writeEndElement();
		}
	}

	/**
	 * Write the worker's stats.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeStats(final XMLStreamWriter ostream, final Object obj,
								  final int indent) throws XMLStreamException {
		if (!(obj instanceof WorkerStats)) {
			throw new IllegalArgumentException("Can only write WorkerStats");
		}
		final WorkerStats stats = (WorkerStats) obj;
		writeTag(ostream, "stats", indent, true);
		writeIntegerAttribute(ostream, "hp", stats.getHitPoints());
		writeIntegerAttribute(ostream, "max", stats.getMaxHitPoints());
		writeIntegerAttribute(ostream, "str", stats.getStrength());
		writeIntegerAttribute(ostream, "dex", stats.getDexterity());
		writeIntegerAttribute(ostream, "con", stats.getConstitution());
		writeIntegerAttribute(ostream, "int", stats.getIntelligence());
		writeIntegerAttribute(ostream, "wis", stats.getWisdom());
		writeIntegerAttribute(ostream, "cha", stats.getCharisma());
	}

	/**
	 * Write a Job to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeJob(final XMLStreamWriter ostream, final Object obj,
								final int indent) throws XMLStreamException {
		if (!(obj instanceof IJob)) {
			throw new IllegalArgumentException("Can only write IJob");
		}
		final IJob job = (IJob) obj;
		if ((job.getLevel() <= 0) && !job.iterator().hasNext()) {
			return;
		}
		final boolean hasSkills = job.iterator().hasNext();
		writeTag(ostream, "job", indent, !hasSkills);
		writeAttribute(ostream, "name", job.getName());
		writeIntegerAttribute(ostream, "level", job.getLevel());
		if (hasSkills) {
			for (final ISkill skill : job) {
				if (skill instanceof Skill) {
					writeSkill(ostream, skill, indent + 1);
				}
			}
			indent(ostream, indent);
			ostream.writeEndElement();
		}
	}

	/**
	 * Write a Skill to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeSkill(final XMLStreamWriter ostream, final Object obj,
								  final int indent) throws XMLStreamException {
		if (!(obj instanceof ISkill)) {
			throw new IllegalArgumentException("Can only write ISkill");
		}
		final ISkill skl = (ISkill) obj;
		writeTag(ostream, "skill", indent, true);
		writeAttribute(ostream, "name", skl.getName());
		writeIntegerAttribute(ostream, "level", skl.getLevel());
		writeIntegerAttribute(ostream, "hours", skl.getHours());
	}

	/**
	 * @param element   the element containing an animal
	 * @param parent    the parent tag
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
									final QName parent,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "animal");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new Animal(getAttribute(element, "kind"), hasAttribute(element,
				"traces"),
										  parseBoolean(getAttribute(element, "talking",
												  "false")),
										  getAttribute(element, "status", "wild"),
										  getOrGenerateID(element, warner, idFactory)),
				element, warner);
	}

	/**
	 * Write an Animal to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeAnimal(final XMLStreamWriter ostream, final Object obj,
								   final int indent) throws XMLStreamException {
		if (!(obj instanceof Animal)) {
			throw new IllegalArgumentException("Can only write Animal");
		}
		final Animal fix = (Animal) obj;
		writeTag(ostream, "animal", indent, true);
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
	}
}

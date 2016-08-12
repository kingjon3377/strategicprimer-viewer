package controller.map.fluidxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDRegistrar;
import java.util.Optional;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.createElement;
import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.requireNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeBooleanAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeNonEmptyAttribute;
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
									final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "worker");
		final Worker retval = setImage(new Worker(getAttribute(element, "name"),
												getAttribute(element, "race", "human"),
												getOrGenerateID(element, warner,
														idFactory)), element, warner);
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
							   final Warning warner, final IDRegistrar idFactory)
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
								   final Warning warner, final IDRegistrar idFactory)
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
										final Warning warner, final IDRegistrar idFactory)
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
	 * Write a worker to XML.
	 *
	 * @param obj     The object to write. Must be an IWorker
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeWorker(final Document document, final Node parent,
								   Object obj) {
		if (!(obj instanceof IWorker)) {
			throw new IllegalArgumentException("Can only write IWorker");
		}
		final IWorker work = (IWorker) obj;
		final Element element = createElement(document, "worker");
		writeAttribute(element, "name", work.getName());
		if (!"human".equals(work.getRace())) {
			writeAttribute(element, "race", work.getRace());
		}
		writeIntegerAttribute(element, "id", work.getID());
		writeImage(element, work);
		if (work instanceof HasPortrait) {
			writeNonEmptyAttribute(element, "portrait",
					((HasPortrait) work).getPortrait());
		}
		final Optional<WorkerStats> stats;
		stats = Optional.ofNullable(work.getStats());
		if (stats.isPresent()) {
			writeStats(document, element, stats.get());
		}
		for (final IJob job : work) {
			if (job instanceof Job) {
				writeJob(document, element, job);
			}
		}
		parent.appendChild(element);
	}
	/**
	 * Write the worker's stats.
	 *
	 * @param obj   the object to write. Must be a WorkerStats
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeStats(final Document document, final Node parent,
								  Object obj) {
		if (!(obj instanceof WorkerStats)) {
			throw new IllegalArgumentException("Can only write WorkerStats");
		}
		final WorkerStats stats = (WorkerStats) obj;
		final Element element = createElement(document, "stats");
		writeIntegerAttribute(element, "hp", stats.getHitPoints());
		writeIntegerAttribute(element, "max", stats.getMaxHitPoints());
		writeIntegerAttribute(element, "str", stats.getStrength());
		writeIntegerAttribute(element, "dex", stats.getDexterity());
		writeIntegerAttribute(element, "con", stats.getConstitution());
		writeIntegerAttribute(element, "int", stats.getIntelligence());
		writeIntegerAttribute(element, "wis", stats.getWisdom());
		writeIntegerAttribute(element, "cha", stats.getCharisma());
		parent.appendChild(element);
	}
	/**
	 * Write a Job to XML.
	 *
	 * @param obj     The object to write. Must be an IJob
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeJob(final Document document, final Node parent,
								Object obj) {
		if (!(obj instanceof IJob)) {
			throw new IllegalArgumentException("Can only write IJob");
		}
		final IJob job = (IJob) obj;
		if ((job.getLevel() <= 0) && !job.iterator().hasNext()) {
			return;
		}
		final Element element = createElement(document, "job");
		writeAttribute(element, "name", job.getName());
		writeIntegerAttribute(element, "level", job.getLevel());
		for (final ISkill skill : job) {
			if (skill instanceof Skill) {
				writeSkill(document, element, skill);
			}
		}
		parent.appendChild(element);
	}
	/**
	 * Write a Skill to XML.
	 *
	 * @param obj     The object to write. Must be an ISkill
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeSkill(final Document document, final Node parent,
								  Object obj) {
		if (!(obj instanceof ISkill)) {
			throw new IllegalArgumentException("Can only write ISkill");
		}
		final ISkill skl = (ISkill) obj;
		final Element element = createElement(document, "skill");
		writeAttribute(element, "name", skl.getName());
		writeIntegerAttribute(element, "level", skl.getLevel());
		writeIntegerAttribute(element, "hours", skl.getHours());
		parent.appendChild(element);
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
									final Warning warner, final IDRegistrar idFactory)
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
	 * Write an Animal to XML.
	 *
	 * @param obj the object to write. Must be an instance of Animal.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeAnimal(final Document document, final Node parent,
								   Object obj) {
		if (!(obj instanceof Animal)) {
			throw new IllegalArgumentException("Can only write Animal");
		}
		final Animal fix = (Animal) obj;
		final Element element = createElement(document, "animal");
		writeAttribute(element, "kind", fix.getKind());
		if (fix.isTraces()) {
			writeAttribute(element, "traces", "");
		}
		if (fix.isTalking()) {
			writeBooleanAttribute(element, "talking", true);
		}
		if (!"wild".equals(fix.getStatus())) {
			writeAttribute(element, "status", fix.getStatus());
		}
		writeIntegerAttribute(element, "id", fix.getID());
		writeImage(element, fix);
		parent.appendChild(element);
	}
}

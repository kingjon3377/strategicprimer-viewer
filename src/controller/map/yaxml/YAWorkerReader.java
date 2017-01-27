package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasPortrait;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.mobile.worker.WorkerStats;
import org.eclipse.jdt.annotation.Nullable;
import util.Warning;

/**
 * A reader for Workers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class YAWorkerReader extends YAAbstractReader<IWorker> {
	/**
	 * The Warning instance to use.
	 */
	private final Warning warner;

	/**
	 * Constructor.
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YAWorkerReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
		warner = warning;
	}

	/**
	 * Parse the worker's stats.
	 *
	 * @param element the element to parse
	 * @param parent  the parent tag
	 * @param stream  the stream to read further elements from
	 * @return the parsed stats
	 * @throws SPFormatException on SP format problem
	 */
	private static WorkerStats parseStats(final StartElement element,
										  final QName parent,
										  final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "stats");
		final WorkerStats retval =
				new WorkerStats(getIntegerParameter(element, "hp"),
									   getIntegerParameter(element, "max"),
									   getIntegerParameter(element, "str"),
									   getIntegerParameter(element, "dex"),
									   getIntegerParameter(element, "con"),
									   getIntegerParameter(element, "int"),
									   getIntegerParameter(element, "wis"),
									   getIntegerParameter(element, "cha"));
		spinUntilEnd(element.getName(), stream);
		return retval;
	}

	/**
	 * Parse a Job.
	 *
	 * @param element the element to parse
	 * @param parent  the parent tag
	 * @param stream  the stream to read further elements from
	 * @return the parsed job
	 * @throws SPFormatException on SP format problem
	 */
	private IJob parseJob(final StartElement element, final QName parent,
						 final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "job");
		final IJob retval =
				new Job(getParameter(element, "name"),
							   getIntegerParameter(element, "level"));
		if (hasParameter(element, "hours")) {
			warner.warn(new UnsupportedPropertyException(element, "hours"));
		}
		for (final XMLEvent event : stream) {
			if (isSPStartElement(event)) {
				if ("skill".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					retval.addSkill(
							parseSkill(event.asStartElement(), element.getName()));
					spinUntilEnd(event.asStartElement().getName(), stream);
				} else {
					throw new UnwantedChildException(element.getName(),
															event.asStartElement());
				}
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		return retval;
	}

	/**
	 * Parse a Skill.
	 *
	 * @param element the element to parse
	 * @param parent  the parent tag
	 * @return the parsed skill
	 * @throws SPFormatException on SP format problem
	 */
	private static ISkill parseSkill(final StartElement element,
							 final QName parent) throws SPFormatException {
		requireTag(element, parent, "skill");
		return new Skill(getParameter(element, "name"),
						 getIntegerParameter(element, "level"),
						 getIntegerParameter(element, "hours"));
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
			writeTag(ostream, "stats", indent);
			writeProperty(ostream, "hp", stats.getHitPoints());
			writeProperty(ostream, "max", stats.getMaxHitPoints());
			writeProperty(ostream, "str", stats.getStrength());
			writeProperty(ostream, "dex", stats.getDexterity());
			writeProperty(ostream, "con", stats.getConstitution());
			writeProperty(ostream, "int", stats.getIntelligence());
			writeProperty(ostream, "wis", stats.getWisdom());
			writeProperty(ostream, "cha", stats.getCharisma());
			closeLeafTag(ostream);
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
	public static void writeJob(final Appendable ostream, final IJob obj,
								final int indent) throws IOException {
		if ((obj.getLevel() <= 0) && !obj.iterator().hasNext()) {
			return;
		}
		writeTag(ostream, "job", indent);
		writeProperty(ostream, "name", obj.getName());
		writeProperty(ostream, "level", obj.getLevel());
		if (obj.iterator().hasNext()) {
			finishParentTag(ostream);
			for (final ISkill skill : obj) {
				writeSkill(ostream, skill, indent + 1);
			}
			closeTag(ostream, indent, "job");
		} else {
			closeLeafTag(ostream);
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
		if (!obj.isEmpty()) {
			writeTag(ostream, "skill", indent);
			writeProperty(ostream, "name", obj.getName());
			writeProperty(ostream, "level", obj.getLevel());
			writeProperty(ostream, "hours", obj.getHours());
			closeLeafTag(ostream);
		}
	}

	/**
	 * Read a worker from XML.
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from     @return the parsed
	 *                  worker
	 * @throws SPFormatException on SP format problems
	 * @return the object read from XML
	 */
	@Override
	public IWorker read(final StartElement element,
						final QName parent,
						final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "worker");
		final Worker retval = new Worker(getParameter(element, "name"),
												getParameter(element, "race", "human"),
												getOrGenerateID(element));
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			if (isSPStartElement(event)) {
				if ("job".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					retval.addJob(
							parseJob(event.asStartElement(), element.getName(), stream));
				} else if ("stats".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					retval.setStats(parseStats(event.asStartElement(), element.getName(),
							stream));
				} else {
					throw new UnwantedChildException(element.getName(),
															event.asStartElement());
				}
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
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
	public void write(final Appendable ostream, final IWorker obj,
					  final int indent) throws IOException {
		writeTag(ostream, "worker", indent);
		writeProperty(ostream, "name", obj.getName());
		if (!"human".equals(obj.getRace())) {
			writeProperty(ostream, "race", obj.getRace());
		}
		writeProperty(ostream, "id", obj.getID());
		writeImageXML(ostream, obj);
		if (obj instanceof HasPortrait) {
			writeNonemptyProperty(ostream, "portrait", ((HasPortrait) obj).getPortrait());

		}
		if (obj.iterator().hasNext() || (obj.getStats() != null)) {
			finishParentTag(ostream);
			writeStats(ostream, obj.getStats(), indent + 1);
			for (final IJob job : obj) {
				writeJob(ostream, job, indent + 1);
			}
			closeTag(ostream, indent, "worker");
		} else {
			closeLeafTag(ostream);
		}
	}

	/**
	 * We only support the "worker" tag.
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "worker".equals(tag);
	}

	/**
	 * We can only write Workers. (TODO: Should this be the interface?)
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Worker;
	}
}

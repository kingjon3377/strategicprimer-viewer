package controller.map.readerng;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;
import util.Warning;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Collections;
import java.util.List;

/**
 * A reader for Jobs.
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
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class JobReader implements INodeHandler<@NonNull Job> {
	/**
	 * Reader to write skills.
	 */
	private static final SkillReader SKILL_READER = new SkillReader();

	static {
		ReaderAdapter.factory(new JobReader());
	}

	/**
	 * @return the class this knows how to write
	 */
	@Override
	public Class<Job> writtenClass() {
		return Job.class;
	}

	/**
	 * @return the list of tags this knows how to read
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("job"));
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
	@Override
	public Job parse(final StartElement element,
	                 final Iterable<XMLEvent> stream,
	                 final IMutablePlayerCollection players,
	                 final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		if (XMLHelper.hasAttribute(element, "hours")) {
			warner.warn(new UnsupportedPropertyException("job", "hours",
					                                            element.getLocation()
							                                            .getLineNumber
									                                             ()));
		}
		final Job retval =
				new Job(XMLHelper.getAttribute(element, "name"),
						       XMLHelper.parseInt(XMLHelper.getAttribute(element,
								       "level"), NullCleaner.assertNotNull(element
										                                           .getLocation())));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final Object result =
						ReaderAdapter.ADAPTER.parse(NullCleaner
								                            .assertNotNull(
										                            event.asStartElement
												                                  ()),
								stream,
								players, warner, idFactory);
				if (result instanceof Skill) {
					retval.addSkill((Skill) result);
				} else {
					throw new UnwantedChildException(
							                                NullCleaner.assertNotNull(
									                                element.getName()
											                                .getLocalPart()),

							                                NullCleaner.assertNotNull(
									                                NullCleaner
											                                .assertNotNull(
													                                event.asStartElement())
											                                .getName()
											                                .getLocalPart()),
							                                event
									                                .getLocation()
									                                .getLineNumber());
				}
			} else if (event.isEndElement()
					           &&
					           element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return retval;
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param obj the object to write
	 * @return the intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Job obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				                                                                            "job");
		retval.addAttribute("name", obj.getName());
		retval.addIntegerAttribute("level", obj.getLevel());
		for (final ISkill skill : obj) {
			if (skill instanceof Skill) {
				retval.addChild(SKILL_READER.write((Skill) skill));
			}
		}
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "JobReader";
	}
}

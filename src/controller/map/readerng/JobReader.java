package controller.map.readerng;

import static controller.map.readerng.XMLHelper.assertNonNullList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IPlayerCollection;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader for Jobs.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class JobReader implements INodeHandler<Job> {
	/**
	 * @return the class this knows how to write
	 */
	@Override
	public Class<Job> writes() {
		return Job.class;
	}

	/**
	 * @return the list of tags this knows how to read
	 */
	@Override
	public List<String> understands() {
		return assertNonNullList(Collections.singletonList("job"));
	}

	/**
	 * Parse a job from XML.
	 *
	 * @param element the current tag
	 * @param stream the stream to read more tags from
	 * @param players ignored
	 * @param warner the Warning instance to report errors on
	 * @param idFactory the ID factory to use to generate IDs.
	 * @return the parsed job
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Job parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IPlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		if (XMLHelper.hasAttribute(element, "hours")) {
			warner.warn(new UnsupportedPropertyException("job", "hours",
					element.getLocation().getLineNumber()));
		}
		final List<Skill> skills = new ArrayList<>();
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement selem = event.asStartElement();
				assert selem != null;
				final Object result = ReaderAdapter.ADAPTER.parse(selem,
						stream, players, warner, idFactory);
				if (result instanceof Skill) {
					skills.add((Skill) result);
				} else {
					final String oLocal = element.getName().getLocalPart();
					final String iLocal = selem.getName().getLocalPart();
					assert iLocal != null && oLocal != null;
					throw new UnwantedChildException(oLocal, iLocal, event
							.getLocation().getLineNumber());
				}
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		final Skill[] skillArray = skills.toArray(new Skill[skills.size()]);
		assert skillArray != null;
		return new Job(XMLHelper.getAttribute(element, "name"),
				Integer.parseInt(XMLHelper.getAttribute(element, "level")),
				skillArray);
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param obj the object to write
	 * @return the intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Job obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"job");
		retval.addAttribute("name", obj.getName());
		final String level = Integer.toString(obj.getLevel());
		assert level != null;
		retval.addAttribute("level", level);
		for (final Skill skill : obj) {
			if (skill != null) {
				retval.addChild(SKILL_READER.write(skill));
			}
		}
		return retval;
	}
	/**
	 * Reader to write skills.
	 */
	private static final SkillReader SKILL_READER = new SkillReader();
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "JobReader";
	}
}

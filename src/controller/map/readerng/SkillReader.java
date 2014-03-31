package controller.map.readerng;

import static controller.map.readerng.XMLHelper.assertNonNullList;
import static controller.map.readerng.XMLHelper.assertNonNullQName;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IPlayerCollection;
import model.map.fixtures.mobile.worker.Skill;
import util.NullCleaner;
import util.Warning;
import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Skills.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class SkillReader implements INodeHandler<Skill> {
	/**
	 * @return the class this knows how to write
	 */
	@Override
	public Class<Skill> writes() {
		return Skill.class;
	}

	/**
	 * @return the list of tags this knows how to read
	 */
	@Override
	public List<String> understands() {
		return assertNonNullList(Collections.singletonList("skill"));
	}

	/**
	 * Parse a skill from XML.
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
	public Skill parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IPlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		XMLHelper.requireNonEmptyParameter(element, "name", true, warner);
		XMLHelper.requireNonEmptyParameter(element, "level", true, warner);
		XMLHelper.requireNonEmptyParameter(element, "hours", true, warner);
		XMLHelper.spinUntilEnd(assertNonNullQName(element.getName()), stream);
		final Skill retval = new Skill(XMLHelper.getAttribute(element, "name"),
				Integer.parseInt(XMLHelper.getAttribute(element, "level")),
				Integer.parseInt(XMLHelper.getAttribute(element, "hours")));
		if ("miscellaneous".equals(retval.getName()) && retval.getLevel() > 0) {
			warner.warn(new DeprecatedPropertyException("skill",
					"miscellaneous", "other", element.getLocation()
							.getLineNumber()));
		}
		return retval;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param obj the object to write
	 * @return the intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final Skill obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"skill");
		retval.addAttribute("name", obj.getName());
		retval.addAttribute("level",
				NullCleaner.assertNotNull(Integer.toString(obj.getLevel())));
		retval.addAttribute("hours",
				NullCleaner.assertNotNull(Integer.toString(obj.getHours())));
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SkillReader";
	}
}

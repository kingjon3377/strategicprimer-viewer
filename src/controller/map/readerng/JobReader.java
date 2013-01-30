package controller.map.readerng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.XMLWritable;
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
		return Collections.singletonList("job");
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
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		if (XMLHelper.hasAttribute(element, "hours")) {
			warner.warn(new UnsupportedPropertyException("job", "hours",
					element.getLocation().getLineNumber()));
		}
		final List<Skill> skills = new ArrayList<Skill>();
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final XMLWritable result = ReaderAdapter.ADAPTER.parse(
						event.asStartElement(), stream, players, warner,
						idFactory);
				if (result instanceof Skill) {
					skills.add((Skill) result);
				} else {
					throw new UnwantedChildException(element.getName()
							.getLocalPart(), event.asStartElement().getName()
							.getLocalPart(), event.getLocation()
							.getLineNumber());
				}
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return new Job(XMLHelper.getAttribute(element, "name"),
				Integer.parseInt(XMLHelper.getAttribute(element, "level")),
				skills.toArray(new Skill[skills
						.size()]));
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
		retval.addAttribute("level", Integer.toString(obj.getLevel()));
		for (final Skill skill : obj) {
			retval.addChild(ReaderAdapter.ADAPTER.write(skill));
		}
		return retval;
	}

}

package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.hasAttribute;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IPlayerCollection;
import model.map.fixtures.mobile.Animal;
import util.NullCleaner;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for Animals.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class AnimalReader implements INodeHandler<Animal> {
	/**
	 * @param element the element containing an animal
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the animal
	 * @throws SPFormatException if the data is invalid
	 */
	@Override
	public Animal parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IPlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Animal fix = new Animal(
				getAttribute(element, "kind"),
				hasAttribute(element, "traces"),
				Boolean.parseBoolean(getAttribute(element, "talking", "false")),
				getAttribute(element, "status", "wild"), getOrGenerateID(
						element, warner, idFactory));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("animal"));
	}

	/** @return the class we know how to write */
	@Override
	public Class<Animal> writes() {
		return Animal.class;
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends Animal> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"animal");
		retval.addAttribute("kind", obj.getKind());
		if (obj.isTraces()) {
			retval.addAttribute("traces", "");
		}
		if (obj.isTalking()) {
			retval.addAttribute("talking", "true");
		}
		if (!"wild".equals(obj.getStatus())) {
			retval.addAttribute("status", obj.getStatus());
		}
		retval.addIdAttribute(obj.getID());
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "AnimalReader";
	}
}

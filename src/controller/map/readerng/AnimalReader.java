package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Animal;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A reader for Animals.
 * @author Jonathan Lovelace
 *
 */
public class AnimalReader implements INodeReader<Animal> {
	/**
	 * @return the type we produce
	 */
	@Override
	public Class<Animal> represents() {
		return Animal.class;
	}
	/**
	 * @param element the element containing an animal
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @return the animal
	 * @throws SPFormatException if the data is invalid
	 */
	@Override
	public Animal parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final Animal animal = new Animal(
				XMLHelper.getAttribute(element, "kind"),
				XMLHelper.hasAttribute(element, "traces"),
				Boolean.parseBoolean(XMLHelper.getAttributeWithDefault(element,
						"talking", "false")));
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("animal", event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& "animal".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		return animal;
	}

}

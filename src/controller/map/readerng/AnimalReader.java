package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.Animal;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
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
	 * @param warner the Warning instance to use for warnings
	 * @return the animal
	 * @throws SPFormatException if the data is invalid
	 */
	@Override
	public Animal parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		// ESCA-JAVA0177:
		long id; // NOPMD
		if (XMLHelper.hasAttribute(element, "id")) {
			id = IDFactory.FACTORY.register(
					Long.parseLong(XMLHelper.getAttribute(element, "id")));
		} else {
			warner.warn(new MissingParameterException(element.getName()
					.getLocalPart(), "id", element.getLocation()
					.getLineNumber()));
			id = IDFactory.FACTORY.getID();
		}
		final Animal animal = new Animal(
				XMLHelper.getAttribute(element, "kind"),
				XMLHelper.hasAttribute(element, "traces"),
				Boolean.parseBoolean(XMLHelper.getAttributeWithDefault(element,
						"talking", "false")), id);
		XMLHelper.spinUntilEnd(element.getName(), stream);
		return animal;
	}

}

package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import model.map.PlayerCollection;
import model.map.fixtures.TextFixture;
/**
 * A reader for text elements.
 * @author Jonathan Lovelace
 *
 */
public class TextReader implements INodeReader<TextFixture> {
	/**
	 * @return the class this produces
	 */
	@Override
	public Class<TextFixture> represents() {
		return TextFixture.class;
	}
	/**
	 * Parse a TextFixture.
	 * @param element the element to parse
	 * @param stream the stream to get more elements (in this case, the text) from
	 * @param players ignored
	 * @param warner the Warning instance to use for warnings
	 * @return the TextFixture
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TextFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final StringBuilder sbuild = new StringBuilder(""); // NOPMD
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("text", event.asStartElement()
						.getName().getLocalPart(), event.getLocation()
						.getLineNumber());
			} else if (event.isCharacters()) {
				sbuild.append(event.asCharacters().getData());
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		return new TextFixture(sbuild.toString().trim(),
				Integer.parseInt(XMLHelper.getAttributeWithDefault(element,
						"turn", "-1")));
	}
}

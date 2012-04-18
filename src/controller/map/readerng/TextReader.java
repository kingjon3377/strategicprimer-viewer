package controller.map.readerng;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

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
	 * @return the TextFixture
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TextFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		final StringBuilder sbuild = new StringBuilder("");
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				throw new UnwantedChildException("text", event.asStartElement()
						.getName().getLocalPart(), event.getLocation()
						.getLineNumber());
			} else if (event.isEndElement()
					&& "text".equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			} else if (event.isCharacters()) {
				sbuild.append(event.asCharacters().getData());
			}
		}
		return new TextFixture(sbuild.toString().trim(),
				Integer.parseInt(XMLHelper.getAttributeWithDefault(element,
						"turn", "-1")));
	}
}

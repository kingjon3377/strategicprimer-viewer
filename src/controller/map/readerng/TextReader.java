package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.TextFixture;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * A reader for text elements.
 *
 * @author Jonathan Lovelace
 *
 */
public class TextReader implements INodeHandler<TextFixture> {
	/**
	 * Parse a TextFixture.
	 *
	 * @param element the element to parse
	 * @param stream the stream to get more elements (in this case, the text)
	 *        from
	 * @param players ignored
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the TextFixture
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public TextFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
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
		final TextFixture fix = new TextFixture(sbuild.toString().trim(),
				Integer.parseInt(getAttribute(element, "turn", "-1")));
		if (stream.iterator() instanceof IncludingIterator) {
			fix.setFile(((IncludingIterator) stream.iterator()).getFile());
		}
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("text");
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<TextFixture> writes() {
		return TextFixture.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends TextFixture> SPIntermediateRepresentation write(
			final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				"text");
		if (obj.getTurn() != -1) {
			retval.addAttribute("turn", Integer.toString(obj.getTurn()));
		}
		retval.addAttribute("text-contents", obj.getText().trim());
		return retval;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TextReader";
	}
}

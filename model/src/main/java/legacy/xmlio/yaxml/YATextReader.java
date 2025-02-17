package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Characters;
import java.io.IOException;
import java.nio.file.Path;

import impl.xmlio.exceptions.MissingPropertyException;
import lovelace.util.ThrowingConsumer;

import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.TextFixture;
import common.xmlio.Warning;
import impl.xmlio.exceptions.UnwantedChildException;
import org.jetbrains.annotations.Nullable;

/**
 * A reader for arbitrary-text notes.
 */
/* package */ final class YATextReader extends YAAbstractReader<TextFixture, TextFixture> {
	public YATextReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "text".equalsIgnoreCase(tag);
	}

	@SuppressWarnings("ChainOfInstanceofChecks")
	@Override
	public TextFixture read(final StartElement element, final @Nullable Path path, final QName parent,
	                        final Iterable<XMLEvent> stream)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "text");
		expectAttributes(element, path, "turn", "image");
		final StringBuilder builder = new StringBuilder();
		for (final XMLEvent event : stream) {
			if (event instanceof final StartElement se) {
				throw new UnwantedChildException(element.getName(), se, path);
			} else if (event instanceof final Characters c) {
				builder.append(c.getData());
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		final TextFixture fixture = new TextFixture(builder.toString().strip(),
				getIntegerParameter(element, path, "turn", -1));
		fixture.setImage(getParameter(element, "image", ""));
		return fixture;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final TextFixture obj, final int indent)
			throws IOException {
		writeTag(ostream, "text", indent);
		if (obj.getTurn() != -1) {
			writeProperty(ostream, "turn", obj.getTurn());
		}
		writeImageXML(ostream, obj);
		ostream.accept(">");
		ostream.accept(obj.getText().strip());
		closeTag(ostream, 0, "text");
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof TextFixture;
	}
}

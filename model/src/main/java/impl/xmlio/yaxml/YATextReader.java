package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Characters;
import java.io.IOException;

import common.xmlio.SPFormatException;
import lovelace.util.IOConsumer;

import common.idreg.IDRegistrar;
import common.map.fixtures.TextFixture;
import common.xmlio.Warning;
import impl.xmlio.exceptions.UnwantedChildException;

/**
 * A reader for arbitrary-text notes.
 */
/* package */ class YATextReader extends YAAbstractReader<TextFixture, TextFixture> {
	public YATextReader(Warning warning, IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "text".equalsIgnoreCase(tag);
	}

	@Override
	public TextFixture read(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "text");
		expectAttributes(element, "turn", "image");
		StringBuilder builder = new StringBuilder();
		for (XMLEvent event : stream) {
			if (event instanceof StartElement) {
				throw new UnwantedChildException(element.getName(), (StartElement) event);
			} else if (event instanceof Characters) {
				builder.append(((Characters) event).getData());
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		TextFixture fixture = new TextFixture(builder.toString().trim(),
			getIntegerParameter(element, "turn", -1));
		fixture.setImage(getParameter(element, "image", ""));
		return fixture;
	}

	@Override
	public void write(IOConsumer<String> ostream, TextFixture obj, int indent) throws IOException {
		writeTag(ostream, "text", indent);
		if (obj.getTurn() != -1) {
			writeProperty(ostream, "turn", obj.getTurn());
		}
		writeImageXML(ostream, obj);
		ostream.accept(">");
		ostream.accept(obj.getText().trim());
		closeTag(ostream, 0, "text");
	}

	@Override
	public boolean canWrite(Object obj) {
		return obj instanceof TextFixture;
	}
}

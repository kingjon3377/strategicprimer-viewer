package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.explorable.Portal;
import common.xmlio.Warning;

import lovelace.util.ThrowingConsumer;

/**
 * A reader for portals.
 */
/* package */ class YAPortalReader extends YAAbstractReader<Portal, Portal> {
	public YAPortalReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Portal read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "portal");
		expectAttributes(element, "world", "row", "column", "id", "image");
		final Portal retval = new Portal(getParameter(element, "world"), parsePoint(element),
				getOrGenerateID(element));
		retval.setImage(getParameter(element, "image", ""));
		spinUntilEnd(element.getName(), stream);
		return retval;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final Portal obj, final int indent)
			throws IOException {
		writeTag(ostream, "portal", indent);
		writeProperty(ostream, "world", obj.getDestinationWorld());
		writeProperty(ostream, "row", obj.getDestinationCoordinates().row());
		writeProperty(ostream, "column", obj.getDestinationCoordinates().column());
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "portal".equalsIgnoreCase(tag);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Portal;
	}
}

package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.fixtures.explorable.Portal;
import common.xmlio.Warning;

import lovelace.util.IOConsumer;

/**
 * A reader for portals.
 */
/* package */ class YAPortalReader extends YAAbstractReader<Portal, Portal> {
	public YAPortalReader(Warning warning, IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Portal read(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "portal");
		expectAttributes(element, "world", "row", "column", "id", "image");
		Portal retval = new Portal(getParameter(element, "world"), parsePoint(element),
			getOrGenerateID(element));
		retval.setImage(getParameter(element, "image", ""));
		spinUntilEnd(element.getName(), stream);
		return retval;
	}

	@Override
	public void write(IOConsumer<String> ostream, Portal obj, int indent) throws IOException {
		writeTag(ostream, "portal", indent);
		writeProperty(ostream, "world", obj.getDestinationWorld());
		writeProperty(ostream, "row", obj.getDestinationCoordinates().getRow());
		writeProperty(ostream, "column", obj.getDestinationCoordinates().getColumn());
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "portal".equalsIgnoreCase(tag);
	}

	@Override
	public boolean canWrite(Object obj) {
		return obj instanceof Portal;
	}
}

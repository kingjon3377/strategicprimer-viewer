package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.nio.file.Path;

import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.explorable.Portal;
import common.xmlio.Warning;

import lovelace.util.ThrowingConsumer;
import org.jspecify.annotations.Nullable;

/**
 * A reader for portals.
 */
/* package */ final class YAPortalReader extends YAAbstractReader<Portal, Portal> {
	public YAPortalReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Portal read(final StartElement element, final @Nullable Path path, final QName parent,
					   final Iterable<XMLEvent> stream)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "portal");
		expectAttributes(element, path, "world", "row", "column", "id", "image");
		final Portal retval = new Portal(getParameter(element, path, "world"), parsePoint(element, path),
				getOrGenerateID(element, path));
		retval.setImage(getParameter(element, "image", ""));
		spinUntilEnd(element.getName(), path, stream);
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

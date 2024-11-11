package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.nio.file.Path;

import common.xmlio.SPFormatException;
import lovelace.util.ThrowingConsumer;

import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.Implement;
import common.xmlio.Warning;
import org.jetbrains.annotations.Nullable;

/**
 * A reader for {@link Implement}s.
 */
/* package */ final class YAImplementReader extends YAAbstractReader<Implement, Implement> {
	public YAImplementReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Implement read(final StartElement element, final @Nullable Path path, final QName parent,
	                      final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "implement");
		expectAttributes(element, "kind", "id", "image");
		final Implement retval = new Implement(getParameter(element, "kind"),
				getOrGenerateID(element, path), getIntegerParameter(element, "count", 1));
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "implement".equalsIgnoreCase(tag);
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final Implement obj, final int indent)
			throws IOException {
		writeTag(ostream, "implement", indent);
		writeProperty(ostream, "kind", obj.getKind());
		writeProperty(ostream, "id", obj.getId());
		if (obj.getCount() > 1) {
			writeProperty(ostream, "count", obj.getCount());
		}
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Implement;
	}
}

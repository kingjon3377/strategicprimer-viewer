package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.nio.file.Path;

import common.xmlio.SPFormatException;
import lovelace.util.ThrowingConsumer;

import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.Ground;
import common.xmlio.Warning;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * A reader for {@link Ground}.
 */
/* package */ final class YAGroundReader extends YAAbstractReader<Ground, Ground> {
	public YAGroundReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Ground read(final StartElement element, final @Nullable Path path, final QName parent,
	                   final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "ground");
		expectAttributes(element, "kind", "ground", "exposed", "id", "image");
		final String kind = getParamWithDeprecatedForm(element, "kind", "ground");
		requireNonEmptyParameter(element, "exposed", true);
		spinUntilEnd(element.getName(), stream);
		final int id = getIntegerParameter(element, "id", -1);
		if (id >= 0) {
			registerID(id, path, element.getLocation());
		}
		final Ground retval = new Ground(id, kind, getBooleanParameter(element, "exposed"));
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "ground".equalsIgnoreCase(tag);
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final Ground obj, final int indent)
			throws IOException {
		writeTag(ostream, "ground", indent);
		writeProperty(ostream, "kind", obj.getKind());
		writeProperty(ostream, "exposed", Boolean.toString(obj.isExposed()));
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Ground;
	}
}

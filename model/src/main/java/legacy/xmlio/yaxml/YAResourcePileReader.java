package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.nio.file.Path;

import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.map.fixtures.LegacyQuantity;
import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.IMutableResourcePile;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.ResourcePileImpl;
import common.xmlio.Warning;
import org.jetbrains.annotations.Nullable;

/**
 * A reader for resource piles.
 */
/* package */ final class YAResourcePileReader extends YAAbstractReader<IResourcePile, IResourcePile> {
	public YAResourcePileReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public IMutableResourcePile read(final StartElement element, final @Nullable Path path, final QName parent,
	                                 final Iterable<XMLEvent> stream)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "resource");
		expectAttributes(element, path, "quantity", "kind", "contents", "unit", "created", "id", "image");
		final IMutableResourcePile retval = new ResourcePileImpl(getOrGenerateID(element, path),
				getParameter(element, path, "kind"), getParameter(element, path, "contents"),
				new LegacyQuantity(getNumericParameter(element, path, "quantity"),
						getParameter(element, "unit", "")));
		if (hasParameter(element, "created")) {
			retval.setCreated(getIntegerParameter(element, path, "created"));
		}
		spinUntilEnd(element.getName(), path, stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "resource".equalsIgnoreCase(tag);
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final IResourcePile obj, final int indent)
			throws IOException {
		writeTag(ostream, "resource", indent);
		writeProperty(ostream, "id", obj.getId());
		writeProperty(ostream, "kind", obj.getKind());
		writeProperty(ostream, "contents", obj.getContents());
		writeProperty(ostream, "quantity", obj.getQuantity().number().toString());
		writeProperty(ostream, "unit", obj.getQuantity().units());
		if (obj.getCreated() >= 0) {
			writeProperty(ostream, "created", obj.getCreated());
		}
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof IResourcePile;
	}
}

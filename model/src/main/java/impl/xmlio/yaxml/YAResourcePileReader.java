package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.ResourcePileImpl;
import common.xmlio.Warning;

/**
 * A reader for resource piles.
 */
/* package */ class YAResourcePileReader extends YAAbstractReader<IResourcePile, IResourcePile> {
	public YAResourcePileReader(Warning warning, IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public IMutableResourcePile read(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "resource");
		expectAttributes(element, "quantity", "kind", "contents", "unit", "created", "id", "image");
		IMutableResourcePile retval = new ResourcePileImpl(getOrGenerateID(element),
			getParameter(element, "kind"), getParameter(element, "contents"),
			new Quantity(getNumericParameter(element, "quantity"),
				getParameter(element, "unit", "")));
		if (hasParameter(element, "created")) {
			retval.setCreated(getIntegerParameter(element, "created"));
		}
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "resource".equalsIgnoreCase(tag);
	}

	@Override
	public void write(ThrowingConsumer<String, IOException> ostream, IResourcePile obj, int indent) throws IOException {
		writeTag(ostream, "resource", indent);
		writeProperty(ostream, "id", obj.getId());
		writeProperty(ostream, "kind", obj.getKind());
		writeProperty(ostream, "contents", obj.getContents());
		writeProperty(ostream, "quantity", obj.getQuantity().getNumber().toString());
		writeProperty(ostream, "unit", obj.getQuantity().getUnits());
		if (obj.getCreated() >= 0) {
			writeProperty(ostream, "created", obj.getCreated());
		}
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(Object obj) {
		return obj instanceof IResourcePile;
	}
}

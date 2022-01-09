package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import lovelace.util.IOConsumer;

import common.idreg.IDRegistrar;
import common.map.fixtures.Implement;
import common.xmlio.Warning;

/**
 * A reader for {@link Implement}s.
 */
/* package */ class YAImplementReader extends YAAbstractReader<Implement, Implement> {
	public YAImplementReader(Warning warning, IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Implement read(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "implement");
		expectAttributes(element, "kind", "id", "image");
		Implement retval = new Implement(getParameter(element, "kind"),
			getOrGenerateID(element), getIntegerParameter(element, "count", 1));
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "implement".equalsIgnoreCase(tag);
	}

	@Override
	public void write(IOConsumer<String> ostream, Implement obj, int indent) throws IOException{
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
	public boolean canWrite(Object obj) {
		return obj instanceof Implement;
	}
}
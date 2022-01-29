package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import lovelace.util.ThrowingConsumer;

import common.idreg.IDRegistrar;
import common.map.fixtures.Ground;
import common.xmlio.Warning;

/**
 * A reader for {@link Ground}.
 */
/* package */ class YAGroundReader extends YAAbstractReader<Ground, Ground> {
	public YAGroundReader(Warning warning, IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Ground read(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "ground");
		expectAttributes(element, "kind", "ground", "exposed", "id", "image");
		String kind = getParamWithDeprecatedForm(element, "kind", "ground");
		requireNonEmptyParameter(element, "exposed", true);
		spinUntilEnd(element.getName(), stream);
		int id = getIntegerParameter(element, "id", -1);
		if (id >= 0) {
			registerID(id, element.getLocation());
		}
		Ground retval = new Ground(id, kind, getBooleanParameter(element, "exposed"));
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "ground".equalsIgnoreCase(tag);
	}

	@Override
	public void write(ThrowingConsumer<String, IOException> ostream, Ground obj, int indent) throws IOException {
		writeTag(ostream, "ground", indent);
		writeProperty(ostream, "kind", obj.getKind());
		writeProperty(ostream, "exposed", Boolean.toString(obj.isExposed()));
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(Object obj) {
		return obj instanceof Ground;
	}
}

package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.fixtures.explorable.Battlefield;
import common.map.fixtures.explorable.ExplorableFixture;
import common.map.fixtures.explorable.Cave;
import common.xmlio.Warning;
import impl.xmlio.exceptions.UnsupportedTagException;

import static impl.xmlio.SPWriter.IOConsumer;

/**
 * A reader for Caves and Battlefields.
 *
 * FIXME: Extract interface for these two classes so we don't have to pass
 * the too-general ExplorableFixture as the type parameter here.
 */
/* package */ class YAExplorableReader extends YAAbstractReader<ExplorableFixture, ExplorableFixture> {
	public YAExplorableReader(Warning warning, IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "cave".equalsIgnoreCase(tag) || "battlefield".equalsIgnoreCase(tag);
	}

	@Override
	public ExplorableFixture read(StartElement element, QName parent, Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "battlefield", "cave");
		expectAttributes(element, "id", "dc", "image");
		int idNum = getOrGenerateID(element);
		ExplorableFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "battlefield":
			retval = new Battlefield(getIntegerParameter(element, "dc"), idNum);
			break;
		case "cave":
			retval = new Cave(getIntegerParameter(element, "dc"), idNum);
			break;
		default:
			throw UnsupportedTagException.future(element);
		}
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public void write(IOConsumer<String> ostream, ExplorableFixture obj, int indent) 
			throws IOException {
		if (obj instanceof Battlefield) {
			writeTag(ostream, "battlefield", indent);
		} else if (obj instanceof Cave) {
			writeTag(ostream, "cave", indent);
		} else {
			throw new IllegalArgumentException("Only supports Battlefields and Caves");
		}
		writeProperty(ostream, "dc", obj.getDC());
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(Object obj) {
		return obj instanceof Cave || obj instanceof Battlefield;
	}
}

package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.ExplorableFixture;
import legacy.map.fixtures.explorable.Cave;
import common.xmlio.Warning;
import impl.xmlio.exceptions.UnsupportedTagException;

import lovelace.util.ThrowingConsumer;

/**
 * A reader for Caves and Battlefields.
 *
 * FIXME: Extract interface for these two classes so we don't have to pass
 * the too-general ExplorableFixture as the type parameter here.
 */
/* package */ final class YAExplorableReader extends YAAbstractReader<ExplorableFixture, ExplorableFixture> {
	public YAExplorableReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "cave".equalsIgnoreCase(tag) || "battlefield".equalsIgnoreCase(tag);
	}

	@Override
	public ExplorableFixture read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "battlefield", "cave");
		expectAttributes(element, "id", "dc", "image");
		final int idNum = getOrGenerateID(element);
		final ExplorableFixture retval = switch (element.getName().getLocalPart().toLowerCase()) {
			case "battlefield" -> new Battlefield(getIntegerParameter(element, "dc"), idNum);
			case "cave" -> new Cave(getIntegerParameter(element, "dc"), idNum);
			default -> throw UnsupportedTagException.future(element);
		};
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final ExplorableFixture obj,
	                  final int indent) throws IOException {
		switch (obj) {
			case final Battlefield battlefield -> writeTag(ostream, "battlefield", indent);
			case final Cave cave -> writeTag(ostream, "cave", indent);
			default -> throw new IllegalArgumentException("Only supports Battlefields and Caves");
		}
		writeProperty(ostream, "dc", obj.getDC());
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Cave || obj instanceof Battlefield;
	}
}

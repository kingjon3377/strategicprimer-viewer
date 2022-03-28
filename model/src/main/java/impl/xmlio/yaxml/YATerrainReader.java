package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import lovelace.util.ThrowingConsumer;

import common.map.HasExtent;
import common.map.HasImage;
import common.idreg.IDRegistrar;
import common.map.HasMutableImage;
import common.map.fixtures.TerrainFixture;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;
import common.xmlio.Warning;

import java.util.Set;

/**
 * A reader for {@link TerrainFixture}s.
 */
/* package */ class YATerrainReader extends YAAbstractReader<TerrainFixture, TerrainFixture> {
	public YATerrainReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	// TODO: This seems overkill for this (in Java)
	private final Set<String> supportedTags = Set.of("forest", "hill", "oasis");

	@Override
	public boolean isSupportedTag(final String tag) {
		return supportedTags.contains(tag.toLowerCase());
	}

	@Override
	public TerrainFixture read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		requireTag(element, parent, supportedTags);
		final TerrainFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "forest":
			expectAttributes(element, "id", "image", "kind", "rows", "acres");
			final int id = getIntegerParameter(element, "id", -1);
			if (id >= 0) {
				registerID(id, element.getLocation());
			}
			retval = new Forest(getParameter(element, "kind"),
				getBooleanParameter(element, "rows", false), id,
				getNumericParameter(element, "acres", -1));
			break;
		case "hill":
			expectAttributes(element, "id", "image");
			retval = new Hill(getOrGenerateID(element));
			break;
		case "oasis":
			expectAttributes(element, "id", "image");
			retval = new Oasis(getOrGenerateID(element));
			break;
		default:
			throw new IllegalArgumentException("Unhandled terrain fixture tag " +
				element.getName().getLocalPart());
		}
		spinUntilEnd(element.getName(), stream);
		if (retval instanceof HasMutableImage) {
			((HasMutableImage) retval).setImage(getParameter(element, "image", ""));
		}
		return retval;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final TerrainFixture obj, final int indent) throws IOException {
		if (obj instanceof Forest) {
			writeTag(ostream, "forest", indent);
			writeProperty(ostream, "kind", ((Forest) obj).getKind());
			if (((Forest) obj).isRows()) {
				writeProperty(ostream, "rows", "true");
			}
			if (HasExtent.isPositive(((Forest) obj).getAcres())) {
				writeProperty(ostream, "acres", ((Forest) obj).getAcres().toString());
			}
		} else if (obj instanceof Hill) {
			writeTag(ostream, "hill", indent);
		} else if (obj instanceof Oasis) {
			writeTag(ostream, "oasis", indent);
		} else {
			throw new IllegalArgumentException("Unhandled TerrainFixture type");
		}
		if (obj instanceof HasImage) {
			writeImageXML(ostream, (HasImage) obj);
		}
		writeProperty(ostream, "id", obj.getId());
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof TerrainFixture;
	}
}

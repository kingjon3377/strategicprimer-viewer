package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import static impl.xmlio.yaxml.YAReader.IOConsumer;

import common.map.HasExtent;
import common.map.HasImage;
import common.idreg.IDRegistrar;
import common.map.HasMutableImage;
import common.map.fixtures.TerrainFixture;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;
import common.xmlio.Warning;

import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Set;

/**
 * A reader for [[TerrainFixture]]s.
 */
/* package */ class YATerrainReader extends YAAbstractReader<TerrainFixture, TerrainFixture> {
	public YATerrainReader(Warning warning, IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	// TODO: This seems overkill for this (in Java)
	private Set<String> supportedTags = Collections.unmodifiableSet(Stream.of("forest",
		"hill", "oasis").collect(Collectors.toSet()));

	@Override
	public boolean isSupportedTag(String tag) {
		return supportedTags.contains(tag.toLowerCase());
	}

	@Override
	public TerrainFixture read(StartElement element, QName parent, Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, supportedTags.stream().toArray(String[]::new));
		TerrainFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "forest":
			expectAttributes(element, "id", "image", "kind", "rows", "acres");
			int id = getIntegerParameter(element, "id", -1);
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
	public void write(IOConsumer<String> ostream, TerrainFixture obj, int indent) throws IOException {
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
	public boolean canWrite(Object obj) {
		return obj instanceof TerrainFixture;
	}
}

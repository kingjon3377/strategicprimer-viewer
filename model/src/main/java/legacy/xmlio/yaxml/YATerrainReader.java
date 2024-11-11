package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import lovelace.util.ThrowingConsumer;

import legacy.map.HasExtent;
import legacy.map.HasImage;
import legacy.idreg.IDRegistrar;
import legacy.map.HasMutableImage;
import legacy.map.fixtures.TerrainFixture;
import legacy.map.fixtures.terrain.Oasis;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Forest;
import common.xmlio.Warning;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;

/**
 * A reader for {@link TerrainFixture}s.
 */
/* package */ final class YATerrainReader extends YAAbstractReader<TerrainFixture, TerrainFixture> {
	public YATerrainReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	// TODO: This seems overkill for this (in Java)
	private static final Set<String> SUPPORTED_TAGS = Set.of("forest", "hill", "oasis");

	@Override
	public boolean isSupportedTag(final String tag) {
		return SUPPORTED_TAGS.contains(tag.toLowerCase());
	}

	@Override
	public TerrainFixture read(final StartElement element, final @Nullable Path path, final QName parent,
	                           final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, SUPPORTED_TAGS);
		final TerrainFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
			case "forest" -> {
				expectAttributes(element, "id", "image", "kind", "rows", "acres");
				final int id = getIntegerParameter(element, "id", -1);
				if (id >= 0) {
					registerID(id, path, element.getLocation());
				}
				retval = new Forest(getParameter(element, "kind"),
						getBooleanParameter(element, "rows", false), id,
						getNumericParameter(element, "acres", -1));
			}
			case "hill" -> {
				expectAttributes(element, "id", "image");
				retval = new Hill(getOrGenerateID(element, path));
			}
			case "oasis" -> {
				expectAttributes(element, "id", "image");
				retval = new Oasis(getOrGenerateID(element, path));
			}
			default -> throw new IllegalArgumentException("Unhandled terrain fixture tag " +
					element.getName().getLocalPart());
		}
		spinUntilEnd(element.getName(), stream);
		// All types we currently support implement HasMutableImage
		((HasMutableImage) retval).setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final TerrainFixture obj, final int indent)
			throws IOException {
		switch (obj) {
			case final Forest f -> {
				writeTag(ostream, "forest", indent);
				writeProperty(ostream, "kind", f.getKind());
				if (f.isRows()) {
					writeProperty(ostream, "rows", "true");
				}
				if (HasExtent.isPositive(f.getAcres())) {
					writeProperty(ostream, "acres", f.getAcres().toString());
				}
			}
			case final Hill hill -> writeTag(ostream, "hill", indent);
			case final Oasis oasis -> writeTag(ostream, "oasis", indent);
			default -> throw new IllegalArgumentException("Unhandled TerrainFixture type");
		}
		// All types we currently support implement HasImage
		writeImageXML(ostream, (HasImage) obj);
		writeProperty(ostream, "id", obj.getId());
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof TerrainFixture;
	}
}

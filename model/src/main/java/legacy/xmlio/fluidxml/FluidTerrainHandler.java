package legacy.xmlio.fluidxml;

import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.HasExtent;
import legacy.map.ILegacyPlayerCollection;
import legacy.map.River;
import legacy.map.fixtures.Ground;
import legacy.map.fixtures.resources.ExposureStatus;
import legacy.map.fixtures.terrain.Forest;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Collection;

/* package */ class FluidTerrainHandler extends FluidBase {
	public static Ground readGround(final StartElement element, final @Nullable Path path, final QName parent,
	                                final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                                final Warning warner, final IDRegistrar idFactory)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "ground");
		expectAttributes(element, path, warner, "id", "kind", "ground", "image", "exposed");
		final int id = getIntegerAttribute(element, "id", -1, warner);
		if (id >= 0) {
			idFactory.register(id, warner, Pair.with(path, element.getLocation()));
		}
		final String kind = getAttrWithDeprecatedForm(element, path, "kind", "ground", warner);
		spinUntilEnd(element.getName(), path, stream);
		return setImage(new Ground(id, kind, getBooleanAttribute(element, path, "exposed") ? ExposureStatus.EXPOSED :
						ExposureStatus.HIDDEN), element, path, warner);
	}

	public static Forest readForest(final StartElement element, final @Nullable Path path, final QName parent,
	                                final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                                final Warning warner, final IDRegistrar idFactory)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "forest");
		expectAttributes(element, path, warner, "id", "kind", "rows", "image", "acres");
		final int id = getIntegerAttribute(element, "id", -1, warner);
		if (id >= 0) {
			idFactory.register(id, warner, Pair.with(path, element.getLocation()));
		}
		final Forest retval = new Forest(getAttribute(element, path, "kind"),
				getBooleanAttribute(element, path, "rows", false), id,
				getNumericAttribute(element, path, "acres", -1));
		spinUntilEnd(element.getName(), path, stream);
		return setImage(retval, element, path, warner);
	}

	public static void writeGround(final XMLStreamWriter ostream, final Ground obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "ground", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()),
				Pair.with("exposed", obj.getExposure() == ExposureStatus.EXPOSED), Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeForest(final XMLStreamWriter ostream, final Forest obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "forest", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()));
		if (obj.isRows()) {
			writeAttributes(ostream, Pair.with("rows", true));
		}
		if (HasExtent.isPositive(obj.getAcres())) {
			writeAttributes(ostream, Pair.with("acres", obj.getAcres()));
		}
		writeAttributes(ostream, Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static River readLake(final StartElement element, final @Nullable Path path, final QName parent,
	                             final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                             final Warning warner, final IDRegistrar idFactory)
			throws UnwantedChildException {
		requireTag(element, path, parent, "lake");
		expectAttributes(element, path, warner);
		spinUntilEnd(element.getName(), path, stream);
		return River.Lake;
	}

	public static River readRiver(final StartElement element, final @Nullable Path path, final QName parent,
	                              final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                              final Warning warner, final IDRegistrar idFactory)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "river");
		expectAttributes(element, path, warner, "direction");
		spinUntilEnd(element.getName(), path, stream);
		try {
			return River.parse(getAttribute(element, path, "direction"));
		} catch (final ParseException | IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "direction", except);
		}
	}

	public static void writeRiver(final XMLStreamWriter ostream, final River obj, final int indent)
			throws XMLStreamException {
		if (River.Lake == obj) {
			writeTag(ostream, "lake", indent, true);
		} else {
			writeTag(ostream, "river", indent, true);
			writeAttributes(ostream, Pair.with("direction", obj.getDescription()));
		}
	}

	public static void writeRivers(final XMLStreamWriter ostream, final Collection<River> obj, final int indent)
			throws XMLStreamException {
		// Can't use forEach() instead of collecting to a new list because of declared exception
		for (final River river : obj.stream().sorted().toList()) {
			writeRiver(ostream, river, indent);
		}
	}
}

package impl.xmlio.fluidxml;

import org.javatuples.Pair;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;

import common.idreg.IDRegistrar;
import common.map.IPlayerCollection;
import common.map.River;
import common.map.fixtures.Ground;
import common.map.fixtures.terrain.Forest;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import common.xmlio.SPFormatException;
import lovelace.util.MalformedXMLException;
import common.map.HasExtent;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

/* package */ class FluidTerrainHandler extends FluidBase {
	public static Ground readGround(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IPlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		requireTag(element, parent, "ground");
		expectAttributes(element, warner, "id", "kind", "ground", "image", "exposed");
		int id = getIntegerAttribute(element, "id", -1, warner);
		if (id >= 0) {
			idFactory.register(id, warner, element.getLocation());
		}
		String kind = getAttrWithDeprecatedForm(element, "kind", "ground", warner);
		spinUntilEnd(element.getName(), stream);
		return setImage(new Ground(id, kind, getBooleanAttribute(element, "exposed")),
			element, warner);
	}

	public static Forest readForest(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IPlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		requireTag(element, parent, "forest");
		expectAttributes(element, warner, "id", "kind", "rows", "image", "acres");
		int id = getIntegerAttribute(element, "id", -1, warner);
		if (id >= 0) {
			idFactory.register(id, warner, element.getLocation());
		}
		Forest retval = new Forest(getAttribute(element, "kind"),
			getBooleanAttribute(element, "rows", false), id,
			getNumericAttribute(element, "acres", -1));
		spinUntilEnd(element.getName(), stream);
		return setImage(retval, element, warner);
	}

	public static void writeGround(XMLStreamWriter ostream, Ground obj, int indent) 
			throws MalformedXMLException {
		writeTag(ostream, "ground", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()),
			Pair.with("exposed", obj.isExposed()), Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeForest(XMLStreamWriter ostream, Forest obj, int indent) 
			throws MalformedXMLException {
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

	public static River readLake(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IPlayerCollection players, Warning warner, IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "lake");
		expectAttributes(element, warner);
		spinUntilEnd(element.getName(), stream);
		return River.Lake;
	}

	public static River readRiver(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IPlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		requireTag(element, parent, "river");
		expectAttributes(element, warner, "direction");
		spinUntilEnd(element.getName(), stream);
		try {
			return River.parse(getAttribute(element, "direction"));
		} catch (ParseException|IllegalArgumentException except) {
			throw new MissingPropertyException(element, "direction", except);
		}
	}

	public static void writeRiver(XMLStreamWriter ostream, River obj, int indent)
			throws MalformedXMLException {
		if (River.Lake.equals(obj)) {
			writeTag(ostream, "lake", indent, true);
		} else {
			writeTag(ostream, "river", indent, true);
			writeAttributes(ostream, Pair.with("direction", obj.getDescription()));
		}
	}

	public static void writeRivers(XMLStreamWriter ostream, Iterable<River> obj, int indent) 
			throws MalformedXMLException {
		for (River river : StreamSupport.stream(obj.spliterator(), false).sorted()
				.collect(Collectors.toList())) {
			writeRiver(ostream, river, indent);
		}
	}
}

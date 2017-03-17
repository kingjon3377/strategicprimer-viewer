import controller.map.fluidxml {
    XMLHelper
}
import controller.map.formatexceptions {
    MissingPropertyException
}
import controller.map.misc {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
    IllegalArgumentException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamWriter
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import model.map {
    IPlayerCollection,
    River
}
import model.map.fixtures {
    Ground,
    RiverFixture
}
import model.map.fixtures.terrain {
    Forest
}

import util {
    Warning
}
Ground readGround(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "ground");
    Integer id = XMLHelper.getIntegerAttribute(element, "id", -1);
    if (id >= 0) {
        idFactory.register(warner, id);
    }
    String kind = XMLHelper.getAttrWithDeprecatedForm(element, "kind", "ground", warner);
    XMLHelper.spinUntilEnd(element.name, stream);
    value exposed = Boolean.parse(XMLHelper.getAttribute(element, "exposed"));
    if (is Boolean exposed) {
        return XMLHelper.setImage(Ground(id, kind, exposed), element, warner);
    } else {
        throw MissingPropertyException(element, "exposed", exposed);
    }
}

Forest readForest(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "forest");
    Integer id = XMLHelper.getIntegerAttribute(element, "id", -1);
    if (id >= 0) {
        idFactory.register(warner, id);
    }
    // TODO: support """rows="false""""
    Forest retval = Forest(XMLHelper.getAttribute(element, "kind"),
        XMLHelper.hasAttribute(element, "rows"), id);
    XMLHelper.spinUntilEnd(element.name, stream);
    return XMLHelper.setImage(retval, element, warner);
}

void writeGround(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Ground obj) {
        XMLHelper.writeTag(ostream, "ground", indent, true);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        XMLHelper.writeBooleanAttribute(ostream, "exposed", obj.exposed);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Ground");
    }
}

void writeForest(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Forest obj) {
        XMLHelper.writeTag(ostream, "forest", indent, true);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        if (obj.rows) {
            XMLHelper.writeBooleanAttribute(ostream, "rows", true);
        }
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Forests");
    }
}

River readLake(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "lake");
    XMLHelper.spinUntilEnd(element.name, stream);
    return River.lake;
}

River readRiver(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "river");
    XMLHelper.spinUntilEnd(element.name, stream);
    return River.getRiver(XMLHelper.getAttribute(element, "direction"));
}

void writeRivers(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (River.lake == obj) {
        XMLHelper.writeTag(ostream, "lake", indent, true);
    } else if (is River obj) {
        XMLHelper.writeTag(ostream, "river", indent, true);
        XMLHelper.writeAttribute(ostream, "direction", obj.description);
    } else if (is RiverFixture obj) { // TODO: change to {River*} once ported
        for (river in obj) {
            writeRivers(ostream, river, indent);
        }
    } else {
        throw IllegalArgumentException("Can only write River or RiverFixture");
    }
}
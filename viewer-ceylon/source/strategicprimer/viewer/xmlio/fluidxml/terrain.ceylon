import controller.map.formatexceptions {
    MissingPropertyException
}
import strategicprimer.viewer.model {
    IDRegistrar
}

import java.lang {
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
import strategicprimer.viewer.model.map {
    IPlayerCollection
}
import model.map {
    River
}
import strategicprimer.viewer.model.map.fixtures {
    RiverFixture,
    Ground
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Forest
}

import strategicprimer.viewer.xmlio {
    Warning
}
Ground readGround(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "ground");
    Integer id = getIntegerAttribute(element, "id", -1);
    if (id >= 0) {
        idFactory.register(id, warner);
    }
    String kind = getAttrWithDeprecatedForm(element, "kind", "ground", warner);
    spinUntilEnd(element.name, stream);
    value exposed = Boolean.parse(getAttribute(element, "exposed"));
    if (is Boolean exposed) {
        return setImage(Ground(id, kind, exposed), element, warner);
    } else {
        throw MissingPropertyException(element, "exposed", exposed);
    }
}

Forest readForest(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "forest");
    Integer id = getIntegerAttribute(element, "id", -1);
    if (id >= 0) {
        idFactory.register(id, warner);
    }
    // TODO: support """rows="false""""
    Forest retval = Forest(getAttribute(element, "kind"),
        hasAttribute(element, "rows"), id);
    spinUntilEnd(element.name, stream);
    return setImage(retval, element, warner);
}

void writeGround(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Ground obj) {
        writeTag(ostream, "ground", indent, true);
        writeAttribute(ostream, "kind", obj.kind);
        writeBooleanAttribute(ostream, "exposed", obj.exposed);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Ground");
    }
}

void writeForest(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Forest obj) {
        writeTag(ostream, "forest", indent, true);
        writeAttribute(ostream, "kind", obj.kind);
        if (obj.rows) {
            writeBooleanAttribute(ostream, "rows", true);
        }
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Forests");
    }
}

River readLake(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "lake");
    spinUntilEnd(element.name, stream);
    return River.lake;
}

River readRiver(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "river");
    spinUntilEnd(element.name, stream);
    return River.getRiver(getAttribute(element, "direction"));
}

void writeRivers(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (River.lake == obj) {
        writeTag(ostream, "lake", indent, true);
    } else if (is River obj) {
        writeTag(ostream, "river", indent, true);
        writeAttribute(ostream, "direction", obj.description);
    } else if (is RiverFixture obj) { // TODO: change to {River*} once ported
        for (river in obj) {
            writeRivers(ostream, river, indent);
        }
    } else {
        throw IllegalArgumentException("Can only write River or RiverFixture");
    }
}
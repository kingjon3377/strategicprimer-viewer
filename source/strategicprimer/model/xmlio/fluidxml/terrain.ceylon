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

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    IPlayerCollection,
    River
}
import strategicprimer.model.map.fixtures {
    Ground
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    MissingPropertyException
}
object fluidTerrainHandler extends FluidBase() {
    shared Ground readGround(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "ground");
        expectAttributes(element, warner, "id", "kind", "ground", "image", "exposed");
        Integer id = getIntegerAttribute(element, "id", -1, warner);
        if (id >= 0) {
            idFactory.register(id, warner, element.location);
        }
        String kind = getAttrWithDeprecatedForm(element, "kind", "ground", warner);
        spinUntilEnd(element.name, stream);
        return setImage(Ground(id, kind, getBooleanAttribute(element, "exposed")),
            element, warner);
    }

    shared Forest readForest(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "forest");
        expectAttributes(element, warner, "id", "kind", "rows", "image", "acres");
        Integer id = getIntegerAttribute(element, "id", -1, warner);
        if (id >= 0) {
            idFactory.register(id, warner, element.location);
        }
        Forest retval = Forest(getAttribute(element, "kind"),
            getBooleanAttribute(element, "rows", false), id,
            getNumericAttribute(element, "acres", -1));
        spinUntilEnd(element.name, stream);
        return setImage(retval, element, warner);
    }

    shared void writeGround(XMLStreamWriter ostream, Ground obj, Integer indent) {
        writeTag(ostream, "ground", indent, true);
        writeAttributes(ostream, "kind"->obj.kind, "exposed"->obj.exposed, "id"->obj.id);
        writeImage(ostream, obj);
    }

    shared void writeForest(XMLStreamWriter ostream, Forest obj, Integer indent) {
        writeTag(ostream, "forest", indent, true);
        writeAttributes(ostream, "kind"->obj.kind);
        if (obj.rows) {
            writeAttributes(ostream, "rows"->true);
        }
        if (obj.acres.positive) {
            writeAttributes(ostream, "acres"->obj.acres);
        }
        writeAttributes(ostream, "id"->obj.id);
        writeImage(ostream, obj);
    }

    shared River readLake(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "lake");
        expectAttributes(element, warner);
        spinUntilEnd(element.name, stream);
        return River.lake;
    }

    shared River readRiver(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "river");
        expectAttributes(element, warner, "direction");
        spinUntilEnd(element.name, stream);
        value river = River.parse(getAttribute(element, "direction"));
        if (is River river) {
            return river;
        } else {
            throw MissingPropertyException(element, "direction", river);
        }
    }

    shared void writeRivers(XMLStreamWriter ostream, River|{River*} obj, Integer indent) {
        if (River.lake == obj) {
            writeTag(ostream, "lake", indent, true);
        } else {
            switch (obj)
            case (is River) {
                writeTag(ostream, "river", indent, true);
                writeAttributes(ostream, "direction"->obj.description);
            }
            case (is {River*}) {
                for (river in sort(obj)) {
                    writeRivers(ostream, river, indent);
                }
            }
        }
    }
}
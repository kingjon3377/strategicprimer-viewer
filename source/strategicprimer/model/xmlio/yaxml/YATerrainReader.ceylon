import java.lang {
    IllegalArgumentException,
    IllegalStateException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    HasMutableImage,
    HasImage
}
import strategicprimer.model.map.fixtures {
    TerrainFixture
}
import strategicprimer.model.map.fixtures.terrain {
    Sandbar,
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.xmlio {
    Warning
}
"A reader for [[TerrainFixture]]s."
class YATerrainReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<TerrainFixture>(warning, idRegistrar) {
    Set<String> supportedTags = set { "forest", "hill", "oasis", "sandbar" };
    shared actual Boolean isSupportedTag(String tag) =>
            supportedTags.contains(tag.lowercased);
    shared actual TerrainFixture read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, *supportedTags);
        TerrainFixture retval;
        switch (element.name.localPart.lowercased)
        case ("forest") {
            expectAttributes(element, "id", "image", "kind", "rows", "acres");
            Integer id = getIntegerParameter(element, "id", -1);
            if (id >= 0) {
                registerID(id, element.location);
            }
            retval = Forest(getParameter(element, "kind"),
                getBooleanParameter(element, "rows", false), id,
                getNumericParameter(element, "acres", -1));
        }
        case ("hill") {
            expectAttributes(element, "id", "image");
            retval = Hill(getOrGenerateID(element));
        }
        case ("oasis") {
            expectAttributes(element, "id", "image");
            retval = Oasis(getOrGenerateID(element));
        }
        case ("sandbar") {
            expectAttributes(element, "id", "image");
            retval = Sandbar(getOrGenerateID(element));
        }
        else {
            throw IllegalArgumentException("Unhandled terrain fixture tag ``element.name
                .localPart``");
        }
        spinUntilEnd(element.name, stream);
        if (is HasMutableImage retval) {
            retval.image = getParameter(element, "image", "");
        }
        return retval;
    }
    shared actual void write(Anything(String) ostream, TerrainFixture obj,
            Integer indent) {
        if (is Forest obj) {
            writeTag(ostream, "forest", indent);
            writeProperty(ostream, "kind", obj.kind);
            if (obj.rows) {
                writeProperty(ostream, "rows", "true");
            }
            if (obj.acres.positive) {
                writeProperty(ostream, "acres", obj.acres.string);
            }
        } else if (is Hill obj) {
            writeTag(ostream, "hill", indent);
        } else if (is Oasis obj) {
            writeTag(ostream, "oasis", indent);
        } else if (is Sandbar obj) {
            writeTag(ostream, "sandbar", indent);
        } else {
            throw IllegalStateException("Unexpected TerrainFixture type");
        }
        if (is HasImage obj) {
            writeImageXML(ostream, obj);
        }
        writeProperty(ostream, "id", obj.id);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is TerrainFixture;
}

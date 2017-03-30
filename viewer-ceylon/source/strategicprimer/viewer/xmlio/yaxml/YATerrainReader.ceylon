import strategicprimer.viewer.model {
    IDRegistrar
}

import java.lang {
    JAppendable=Appendable,
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
import strategicprimer.viewer.model.map.fixtures {
    TerrainFixture
}
import model.map {
    HasMutableImage,
    HasImage
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Sandbar,
    Oasis,
    Hill,
    Forest
}

import util {
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
            Integer id = getIntegerParameter(element, "id", -1);
            if (id >= 0) {
                registerID(id);
            }
            // TODO: support 'rows="false"'
            retval = Forest(getParameter(element, "kind"), hasParameter(element, "rows"),
                id);
        }
        case ("hill") { retval = Hill(getOrGenerateID(element)); }
        case ("oasis") { retval = Oasis(getOrGenerateID(element)); }
        case ("sandbar") { retval = Sandbar(getOrGenerateID(element)); }
        else {
            throw IllegalArgumentException("Unhandled terrain fixture tag ``element.name
                .localPart``");
        }
        spinUntilEnd(element.name, stream);
        if (is HasMutableImage retval) {
            retval.setImage(getParameter(element, "image", ""));
        }
        return retval;
    }
    shared actual void write(JAppendable ostream, TerrainFixture obj, Integer indent) {
        if (is Forest obj) {
            writeTag(ostream, "forest", indent);
            writeProperty(ostream, "kind", obj.kind);
            if (obj.rows) {
                writeProperty(ostream, "rows", "true");
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
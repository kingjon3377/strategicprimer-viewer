import controller.map.formatexceptions {
    UnsupportedTagException
}
import strategicprimer.viewer.model {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
    JAppendable=Appendable,
    IllegalStateException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import lovelace.util.common {
    todo
}

import model.map.fixtures.explorable {
    ExplorableFixture,
    Battlefield,
    Cave
}

import util {
    Warning
}
"A reader for Caves and Battlefields."
todo("Specify Cave|Battlefield instead of ExplorableFixture")
class YAExplorableReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<ExplorableFixture>(warning, idRegistrar) {
    shared actual Boolean isSupportedTag(String tag) =>
            {"cave", "battlefield"}.contains(tag.lowercased);
    shared actual ExplorableFixture read(StartElement element, QName parent,
            JIterable<XMLEvent> stream) {
        requireTag(element, parent, "battlefield", "cave");
        Integer idNum = getOrGenerateID(element);
        ExplorableFixture retval;
        switch (tag = element.name.localPart.lowercased)
        case ("battlefield") {
            retval = Battlefield(getIntegerParameter(element, "dc"), idNum);
        }
        case ("cave") { retval = Cave(getIntegerParameter(element, "dc"), idNum); }
        else { throw UnsupportedTagException(element); }
        spinUntilEnd(element.name, stream);
        retval.setImage(getParameter(element, "image", ""));
        return retval;
    }
    shared actual void write(JAppendable ostream, ExplorableFixture obj, Integer indent) {
        switch (obj)
        case (is Battlefield) { writeTag(ostream, "battlefield", indent); }
        case (is Cave) { writeTag(ostream, "cave", indent); }
        else { throw IllegalStateException("Unhandled ExplorableFixture subtype"); }
        writeProperty(ostream, "dc", obj.dc);
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is Battlefield|Cave;
}
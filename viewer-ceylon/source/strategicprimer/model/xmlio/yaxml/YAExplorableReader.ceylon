import java.lang {
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

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map.fixtures.explorable {
    Battlefield,
    Cave,
    ExplorableFixture
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    UnsupportedTagException
}
"A reader for Caves and Battlefields."
todo("Specify Cave|Battlefield instead of ExplorableFixture")
class YAExplorableReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<ExplorableFixture>(warning, idRegistrar) {
    shared actual Boolean isSupportedTag(String tag) =>
            {"cave", "battlefield"}.contains(tag.lowercased);
    shared actual ExplorableFixture read(StartElement element, QName parent,
            {XMLEvent*} stream) {
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
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual void write(Anything(String) ostream, ExplorableFixture obj, Integer indent) {
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
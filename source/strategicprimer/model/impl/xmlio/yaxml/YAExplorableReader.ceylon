import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures.explorable {
    Battlefield,
    Cave
}
import strategicprimer.model.common.xmlio {
    Warning
}
import strategicprimer.model.impl.xmlio.exceptions {
    UnsupportedTagException
}
"A reader for Caves and Battlefields."
class YAExplorableReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<Cave|Battlefield>(warning, idRegistrar) {
    shared actual Boolean isSupportedTag(String tag) =>
            ["cave", "battlefield"].contains(tag.lowercased);
    shared actual Cave|Battlefield read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "battlefield", "cave");
        expectAttributes(element, "id", "dc", "image");
        Integer idNum = getOrGenerateID(element);
        Cave|Battlefield retval;
        switch (tag = element.name.localPart.lowercased)
        case ("battlefield") {
            retval = Battlefield(getIntegerParameter(element, "dc"), idNum);
        }
        case ("cave") { retval = Cave(getIntegerParameter(element, "dc"), idNum); }
        else { throw UnsupportedTagException.future(element); }
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual void write(Anything(String) ostream, Cave|Battlefield obj,
            Integer indent) {
        switch (obj)
        case (is Battlefield) { writeTag(ostream, "battlefield", indent); }
        case (is Cave) { writeTag(ostream, "cave", indent); }
        writeProperty(ostream, "dc", obj.dc);
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
}

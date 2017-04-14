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
import strategicprimer.model.map.fixtures.explorable {
    Portal
}
import strategicprimer.model.xmlio {
    Warning
}
"A reader for portals."
class YAPortalReader(Warning warning, IDRegistrar idRegistrar) extends YAAbstractReader<Portal>(warning, idRegistrar) {
    shared actual Portal read(StartElement element, QName parent, {XMLEvent*} stream) {
        requireTag(element, parent, "portal");
        Portal retval = Portal(getParameter(element, "world"), parsePoint(element),
            getOrGenerateID(element));
        retval.image = getParameter(element, "image", "");
        spinUntilEnd(element.name, stream);
        return retval;
    }
    shared actual void write(Anything(String) ostream, Portal obj, Integer indent) {
        writeTag(ostream, "portal", indent);
        writeProperty(ostream, "world", obj.destinationWorld);
        writeProperty(ostream, "row", obj.destinationCoordinates.row);
        writeProperty(ostream, "column", obj.destinationCoordinates.column);
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean isSupportedTag(String tag) => "portal" == tag.lowercased;
    shared actual Boolean canWrite(Object obj) => obj is Portal;
}
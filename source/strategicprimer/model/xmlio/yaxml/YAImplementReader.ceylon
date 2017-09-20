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
import strategicprimer.model.map.fixtures {
    Implement
}
import strategicprimer.model.xmlio {
    Warning
}
"A reader for [[Implement]]s."
class YAImplementReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<Implement>(warning, idRegistrar) {
    shared actual Implement read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "implement");
        expectAttributes(element, "kind", "id", "image");
        Implement retval = Implement(getParameter(element, "kind"),
            getOrGenerateID(element));
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual Boolean isSupportedTag(String tag) => "implement" == tag.lowercased;
    shared actual void write(Anything(String) ostream, Implement obj, Integer indent) {
        writeTag(ostream, "implement", indent);
        writeProperty(ostream, "kind", obj.kind);
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is Implement;
}
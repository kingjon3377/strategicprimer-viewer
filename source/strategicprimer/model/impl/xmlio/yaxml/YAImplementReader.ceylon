import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import strategicprimer.model.impl.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures {
    Implement
}
import strategicprimer.model.common.xmlio {
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
            getOrGenerateID(element), getIntegerParameter(element, "count", 1));
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual Boolean isSupportedTag(String tag) => "implement" == tag.lowercased;
    shared actual void write(Anything(String) ostream, Implement obj, Integer indent) {
        writeTag(ostream, "implement", indent);
        writeProperty(ostream, "kind", obj.kind);
        writeProperty(ostream, "id", obj.id);
        if (obj.count > 1) {
            writeProperty(ostream, "count", obj.count);
        }
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is Implement;
}

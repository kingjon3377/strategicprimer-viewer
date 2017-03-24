import strategicprimer.viewer.model {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
    JAppendable=Appendable
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import model.map.fixtures {
    Implement
}

import util {
    Warning
}
"A reader for [[Implement]]s."
class YAImplementReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<Implement>(warning, idRegistrar) {
    shared actual Implement read(StartElement element, QName parent,
            JIterable<XMLEvent> stream) {
        requireTag(element, parent, "implement");
        Implement retval = Implement(getParameter(element, "kind"),
            getOrGenerateID(element));
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual Boolean isSupportedTag(String tag) => "implement" == tag.lowercased;
    shared actual void write(JAppendable ostream, Implement obj, Integer indent) {
        writeTag(ostream, "implement", indent);
        writeProperty(ostream, "kind", obj.kind);
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is Implement;
}
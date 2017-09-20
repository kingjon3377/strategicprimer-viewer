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
    Ground
}
import strategicprimer.model.xmlio {
    Warning
}
"A reader for [[Ground]]."
class YAGroundReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<Ground>(warning, idRegistrar) {
    shared actual Ground read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "ground");
        expectAttributes(element, "kind", "ground", "exposed", "id", "image");
        String kind = getParamWithDeprecatedForm(element, "kind", "ground");
        requireNonEmptyParameter(element, "exposed", true);
        spinUntilEnd(element.name, stream);
        Integer id = getIntegerParameter(element, "id", -1);
        if (id >= 0) {
            registerID(id, element.location);
        }
        Ground retval = Ground(id, kind, getBooleanParameter(element, "exposed"));
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual Boolean isSupportedTag(String tag) => "ground" == tag.lowercased;
    shared actual void write(Anything(String) ostream, Ground obj, Integer indent) {
        writeTag(ostream, "ground", indent);
        writeProperty(ostream, "kind", obj.kind);
        writeProperty(ostream, "exposed", obj.exposed.string);
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is Ground;
}
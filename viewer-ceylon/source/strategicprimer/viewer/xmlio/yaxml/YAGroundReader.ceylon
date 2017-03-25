import controller.map.formatexceptions {
    MissingPropertyException
}
import strategicprimer.viewer.model {
    IDRegistrar
}

import java.lang {
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
    Ground
}

import util {
    Warning
}
"A reader for [[Ground]]."
class YAGroundReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<Ground>(warning, idRegistrar) {
    shared actual Ground read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "ground");
        String kind = getParamWithDeprecatedForm(element, "kind", "ground");
        requireNonEmptyParameter(element, "exposed", true);
        spinUntilEnd(element.name, stream);
        Integer id = getIntegerParameter(element, "id", -1);
        if (id >= 0) {
            registerID(id);
        }
        value exposed  = Boolean.parse(getParameter(element, "exposed"));
        if (is Boolean exposed) {
            Ground retval = Ground(id, kind, exposed);
            retval.image =getParameter(element, "image", "");
            return retval;
        } else {
            throw MissingPropertyException(element, "exposed", exposed);
        }
    }
    shared actual Boolean isSupportedTag(String tag) => "ground" == tag.lowercased;
    shared actual void write(JAppendable ostream, Ground obj, Integer indent) {
        writeTag(ostream, "ground", indent);
        writeProperty(ostream, "kind", obj.kind);
        writeProperty(ostream, "exposed", obj.exposed.string);
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is Ground;
}
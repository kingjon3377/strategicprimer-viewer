import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
    Characters
}

import strategicprimer.model.impl.idreg {
    IDRegistrar
}
import strategicprimer.model.impl.map.fixtures {
    TextFixture
}
import strategicprimer.model.impl.xmlio {
    Warning
}
import strategicprimer.model.impl.xmlio.exceptions {
    UnwantedChildException
}
"A reader for arbitrary-text notes."
class YATextReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<TextFixture>(warning, idRegistrar) {
    shared actual Boolean isSupportedTag(String tag) => "text" == tag.lowercased;
    shared actual TextFixture read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "text");
        expectAttributes(element, "turn", "image");
        StringBuilder builder = StringBuilder();
        for (event in stream) {
            if (is StartElement event) {
                throw UnwantedChildException(element.name, event);
            } else if (is Characters event) {
                builder.append(event.data);
            } else if (isMatchingEnd(element.name, event)) {
                break;
            }
        }
        TextFixture fixture = TextFixture(builder.string.trimmed,
            getIntegerParameter(element, "turn", -1));
        fixture.image = getParameter(element, "image", "");
        return fixture;
    }
    shared actual void write(Anything(String) ostream, TextFixture obj, Integer indent) {
        writeTag(ostream, "text", indent);
        if (obj.turn != -1) {
            writeProperty(ostream, "turn", obj.turn);
        }
        writeImageXML(ostream, obj);
        ostream(">");
        ostream(obj.text.trimmed);
        closeTag(ostream, 0, "text");
    }
    shared actual Boolean canWrite(Object obj) => obj is TextFixture;
}

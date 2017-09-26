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
import strategicprimer.model.map {
    Player,
    PlayerImpl
}
import strategicprimer.model.xmlio {
    Warning
}
"A reader for [[Player]]s."
class YAPlayerReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<Player>(warning, idRegistrar) {
    shared actual Player read(StartElement element, QName parent, {XMLEvent*} stream) {
        requireTag(element, parent, "player");
        expectAttributes(element, "number", "code_name");
        requireNonEmptyParameter(element, "number", true);
        requireNonEmptyParameter(element, "code_name", true);
        spinUntilEnd(element.name, stream);
        return PlayerImpl(getIntegerParameter(element, "number"),
            getParameter(element, "code_name"));
    }
    shared actual Boolean isSupportedTag(String tag) => "player" == tag.lowercased;
    shared actual void write(Anything(String) ostream, Player obj, Integer indent) {
        if (!obj.name.empty) {
            writeTag(ostream, "player", indent);
            writeProperty(ostream, "number", obj.playerId);
            writeProperty(ostream, "code_name", obj.name);
            closeLeafTag(ostream);
        }
    }
    shared actual Boolean canWrite(Object obj) => obj is Player;
}

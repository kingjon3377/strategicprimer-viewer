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
        // We're thinking about storing "standing orders" in the XML under the <player> tag;
        // so as to not require players to upgrade to even read their maps once we start doing
        // so, we *now* only *warn* instead of *dying* if the XML contains that idiom.
        spinUntilEnd(element.name, stream, ["orders", "results", "science"]);
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

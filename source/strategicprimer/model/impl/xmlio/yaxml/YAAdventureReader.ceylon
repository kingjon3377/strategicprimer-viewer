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
import strategicprimer.model.common.map {
    IPlayerCollection,
    Player
}
import strategicprimer.model.common.map.fixtures.explorable {
    AdventureFixture
}
import strategicprimer.model.common.xmlio {
    Warning
}

"A reader for adventure hooks."
class YAAdventureReader(Warning warner, IDRegistrar idFactory, IPlayerCollection players)
        extends YAAbstractReader<AdventureFixture>(warner, idFactory) {
    "Read an adventure from XML."
    shared actual AdventureFixture read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "adventure");
        expectAttributes(element, "owner", "brief", "full", "image", "id");
        Player player;
        if (hasParameter(element, "owner")) {
            player = players.getPlayer(getIntegerParameter(element, "owner"));
        } else {
            player = players.independent;
        }
        AdventureFixture retval = AdventureFixture(player,
            getParameter(element, "brief", ""), getParameter(element, "full", ""),
            getOrGenerateID(element));
        retval.image = getParameter(element, "image", "");
        spinUntilEnd(element.name, stream);
        return retval;
    }

    "Write an adventure to XML."
    shared actual void write(Anything(String) ostream, AdventureFixture obj,
            Integer indent) {
        writeTag(ostream, "adventure", indent);
        writeProperty(ostream, "id", obj.id);
        if (!obj.owner.independent) {
            writeProperty(ostream, "owner", obj.owner.playerId);
        }
        writeNonemptyProperty(ostream, "brief", obj.briefDescription);
        writeNonemptyProperty(ostream, "full", obj.fullDescription);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }

    shared actual Boolean isSupportedTag(String tag) => "adventure" == tag.lowercased;

    shared actual Boolean canWrite(Object obj) => obj is AdventureFixture;
}

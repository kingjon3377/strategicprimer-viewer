import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamWriter
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import strategicprimer.model.impl.idreg {
    IDRegistrar
}
import strategicprimer.model.impl.map {
    IPlayerCollection
}
import strategicprimer.model.common.map {
    Point,
	Player
}
import strategicprimer.model.common.map.fixtures {
    TextFixture
}
import strategicprimer.model.common.map.fixtures.explorable {
    Portal,
    AdventureFixture,
    Battlefield,
    Cave
}
import strategicprimer.model.common.xmlio {
    Warning
}
object fluidExplorableHandler extends FluidBase() {
    shared AdventureFixture readAdventure(StartElement element, QName parent,
            {XMLEvent*} stream, IPlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        requireTag(element, parent, "adventure");
        expectAttributes(element, warner, "owner", "brief", "full", "id", "image");
        Player player;
        if (hasAttribute(element, "owner")) {
            player = players.getPlayer(getIntegerAttribute(element, "owner"));
        } else {
            player = players.independent;
        }
        AdventureFixture retval = setImage(AdventureFixture(player,
            getAttribute(element, "brief", ""),
            getAttribute(element, "full", ""),
            getOrGenerateID(element, warner, idFactory)), element, warner);
        spinUntilEnd(element.name, stream);
        return retval;
    }

    shared Portal readPortal(StartElement element, QName parent,
            {XMLEvent*} stream, IPlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        requireTag(element, parent, "portal");
        expectAttributes(element, warner, "row", "column", "world", "id", "image");
        Point location = Point(getIntegerAttribute(element, "row"),
            getIntegerAttribute(element, "column"));
        Portal retval = setImage(Portal(
            getAttribute(element, "world"), location,
            getOrGenerateID(element, warner, idFactory)),
            element, warner);
        spinUntilEnd(element.name, stream);
        return retval;
    }

    shared Cave readCave(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "cave");
        expectAttributes(element, warner, "id", "dc", "image");
        Integer idNum = getOrGenerateID(element, warner, idFactory);
        Cave retval = Cave(getIntegerAttribute(element, "dc"), idNum);
        spinUntilEnd(element.name, stream);
        return setImage(retval, element, warner);
    }

    shared Battlefield readBattlefield(StartElement element, QName parent,
            {XMLEvent*} stream, IPlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        requireTag(element, parent, "battlefield");
        expectAttributes(element, warner, "id", "dc", "image");
        Integer idNum = getOrGenerateID(element, warner, idFactory);
        Battlefield retval = Battlefield(getIntegerAttribute(element, "dc"), idNum);
        spinUntilEnd(element.name, stream);
        return setImage(retval, element, warner);
    }

    shared TextFixture readTextFixture(StartElement element, QName parent,
            {XMLEvent*} stream, IPlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        requireTag(element, parent, "text");
        expectAttributes(element, warner, "turn", "image");
        return setImage(TextFixture(getTextUntil(element.name, stream),
            getIntegerAttribute(element, "turn", -1, warner)),
            element, warner);
    }

    shared void writeAdventure(XMLStreamWriter ostream, AdventureFixture obj,
            Integer indent) {
        writeTag(ostream, "adventure", indent, true);
        writeAttributes(ostream, "id"->obj.id);
        if (!obj.owner.independent) {
            writeAttributes(ostream, "owner"->obj.owner.playerId);
        }
        writeNonEmptyAttributes(ostream, "brief"->obj.briefDescription,
            "full"->obj.fullDescription);
        writeImage(ostream, obj);
    }

    shared void writePortal(XMLStreamWriter ostream, Portal obj, Integer indent) {
        writeTag(ostream, "portal", indent, true);
        writeAttributes(ostream, "world"->obj.destinationWorld,
            "row"->obj.destinationCoordinates.row,
            "column"->obj.destinationCoordinates.column, "id"->obj.id);
        writeImage(ostream, obj);
    }

    shared void writeCave(XMLStreamWriter ostream, Cave obj, Integer indent) {
        writeTag(ostream, "cave", indent, true);
        writeAttributes(ostream, "dc"->obj.dc, "id"->obj.id);
        writeImage(ostream, obj);
    }

    shared void writeBattlefield(XMLStreamWriter ostream, Battlefield obj,
		    Integer indent) {
        writeTag(ostream, "battlefield", indent, true);
        writeAttributes(ostream, "dc"->obj.dc, "id"->obj.id);
        writeImage(ostream, obj);
    }

    shared void writeTextFixture(XMLStreamWriter ostream, TextFixture obj,
		    Integer indent) {
        writeTag(ostream, "text", indent, false);
        if (obj.turn != -1) {
            writeAttributes(ostream, "turn"->obj.turn);
        }
        writeImage(ostream, obj);
        ostream.writeCharacters(obj.text.trimmed);
        ostream.writeEndElement();
    }
}
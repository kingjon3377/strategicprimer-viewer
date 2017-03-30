import java.lang {
    IllegalArgumentException
}

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

import model.map {
    Player,
    Point
}

import strategicprimer.viewer.model {
    IDRegistrar
}
import strategicprimer.viewer.model.map {
    IPlayerCollection,
    pointFactory
}
import strategicprimer.viewer.model.map.fixtures {
    TextFixture
}
import strategicprimer.viewer.model.map.fixtures.explorable {
    Portal,
    AdventureFixture,
    Battlefield,
    Cave
}

import util {
    Warning
}
AdventureFixture readAdventure(StartElement element, QName parent,
        {XMLEvent*} stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    requireTag(element, parent, "adventure");
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

Portal readPortal(StartElement element, QName parent,
        {XMLEvent*} stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    requireTag(element, parent, "portal");
    Point location = pointFactory(getIntegerAttribute(element, "row"),
        getIntegerAttribute(element, "column"));
    Portal retval = setImage(Portal(
        getAttribute(element, "world"), location,
        getOrGenerateID(element, warner, idFactory)),
        element, warner);
    spinUntilEnd(element.name, stream);
    return retval;
}

Cave readCave(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "cave");
    Integer idNum = getOrGenerateID(element, warner, idFactory);
    Cave retval = Cave(getIntegerAttribute(element, "dc"), idNum);
    spinUntilEnd(element.name, stream);
    return setImage(retval, element, warner);
}

Battlefield readBattlefield(StartElement element, QName parent,
        {XMLEvent*} stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    requireTag(element, parent, "battlefield");
    Integer idNum = getOrGenerateID(element, warner, idFactory);
    Battlefield retval = Battlefield(getIntegerAttribute(element, "dc"), idNum);
    spinUntilEnd(element.name, stream);
    return setImage(retval, element, warner);
}

TextFixture readTextFixture(StartElement element, QName parent,
        {XMLEvent*} stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    requireTag(element, parent, "text");
    return setImage(TextFixture(getTextUntil(element.name, stream),
        getIntegerAttribute(element, "turn", -1)),
        element, warner);
}

void writeAdventure(XMLStreamWriter ostream, Object obj, Integer indent) {
    // TODO: Create helper method for this idiom, to allow us to condense these methods
    if (is AdventureFixture obj) {
        writeTag(ostream, "adventure", indent, true);
        writeIntegerAttribute(ostream, "id", obj.id);
        if (!obj.owner.independent) {
            writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
        }
        writeNonEmptyAttribute(ostream, "brief", obj.briefDescription);
        writeNonEmptyAttribute(ostream, "full", obj.fullDescription);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write AdventureFixtures");
    }
}

void writePortal(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Portal obj) {
        writeTag(ostream, "portal", indent, true);
        writeAttribute(ostream, "world", obj.destinationWorld);
        writeIntegerAttribute(ostream, "row", obj.destinationCoordinates.row);
        writeIntegerAttribute(ostream, "column", obj.destinationCoordinates.col);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Portals");
    }
}

void writeCave(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Cave obj) {
        writeTag(ostream, "cave", indent, true);
        writeIntegerAttribute(ostream, "dc", obj.dc);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Caves");
    }
}

void writeBattlefield(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Battlefield obj) {
        writeTag(ostream, "battlefield", indent, true);
        writeIntegerAttribute(ostream, "dc", obj.dc);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Battlefields");
    }
}

void writeTextFixture(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is TextFixture obj) {
        writeTag(ostream, "text", indent, false);
        if (obj.turn != -1) {
            writeIntegerAttribute(ostream, "turn", obj.turn);
        }
        writeImage(ostream, obj);
        ostream.writeCharacters(obj.text.trimmed);
        ostream.writeEndElement();
    } else {
        throw IllegalArgumentException("Can only write TextFixtures");
    }
}
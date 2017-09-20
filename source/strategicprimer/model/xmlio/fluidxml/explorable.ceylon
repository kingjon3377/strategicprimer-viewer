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

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    Point,
    Player,
    IPlayerCollection,
    pointFactory
}
import strategicprimer.model.map.fixtures {
    TextFixture
}
import strategicprimer.model.map.fixtures.explorable {
    Portal,
    AdventureFixture,
    Battlefield,
    Cave
}
import strategicprimer.model.xmlio {
    Warning
}
AdventureFixture readAdventure(StartElement element, QName parent,
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

Portal readPortal(StartElement element, QName parent,
        {XMLEvent*} stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    requireTag(element, parent, "portal");
    expectAttributes(element, warner, "row", "column", "world", "id", "image");
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
    expectAttributes(element, warner, "id", "dc", "image");
    Integer idNum = getOrGenerateID(element, warner, idFactory);
    Cave retval = Cave(getIntegerAttribute(element, "dc"), idNum);
    spinUntilEnd(element.name, stream);
    return setImage(retval, element, warner);
}

Battlefield readBattlefield(StartElement element, QName parent,
        {XMLEvent*} stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    requireTag(element, parent, "battlefield");
    expectAttributes(element, warner, "id", "dc", "image");
    Integer idNum = getOrGenerateID(element, warner, idFactory);
    Battlefield retval = Battlefield(getIntegerAttribute(element, "dc"), idNum);
    spinUntilEnd(element.name, stream);
    return setImage(retval, element, warner);
}

TextFixture readTextFixture(StartElement element, QName parent,
        {XMLEvent*} stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    requireTag(element, parent, "text");
    expectAttributes(element, warner, "turn", "image");
    return setImage(TextFixture(getTextUntil(element.name, stream),
        getIntegerAttribute(element, "turn", -1, warner)),
        element, warner);
}

void writeAdventure(XMLStreamWriter ostream, AdventureFixture obj, Integer indent) {
    writeTag(ostream, "adventure", indent, true);
    writeAttributes(ostream, "id"->obj.id);
    if (!obj.owner.independent) {
        writeAttributes(ostream, "owner"->obj.owner.playerId);
    }
    writeNonEmptyAttributes(ostream, "brief"->obj.briefDescription,
        "full"->obj.fullDescription);
    writeImage(ostream, obj);
}

void writePortal(XMLStreamWriter ostream, Portal obj, Integer indent) {
    writeTag(ostream, "portal", indent, true);
    writeAttributes(ostream, "world"->obj.destinationWorld,
        "row"->obj.destinationCoordinates.row,
        "column"->obj.destinationCoordinates.column, "id"->obj.id);
    writeImage(ostream, obj);
}

void writeCave(XMLStreamWriter ostream, Cave obj, Integer indent) {
    writeTag(ostream, "cave", indent, true);
    writeAttributes(ostream, "dc"->obj.dc, "id"->obj.id);
    writeImage(ostream, obj);
}

void writeBattlefield(XMLStreamWriter ostream, Battlefield obj, Integer indent) {
    writeTag(ostream, "battlefield", indent, true);
    writeAttributes(ostream, "dc"->obj.dc, "id"->obj.id);
    writeImage(ostream, obj);
}

void writeTextFixture(XMLStreamWriter ostream, TextFixture obj, Integer indent) {
    writeTag(ostream, "text", indent, false);
    if (obj.turn != -1) {
        writeAttributes(ostream, "turn"->obj.turn);
    }
    writeImage(ostream, obj);
    ostream.writeCharacters(obj.text.trimmed);
    ostream.writeEndElement();
}
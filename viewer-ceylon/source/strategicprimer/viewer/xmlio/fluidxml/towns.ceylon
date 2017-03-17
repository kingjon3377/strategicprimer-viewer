import controller.map.misc {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
    IllegalArgumentException
}
import java.util {
    JRandom=Random
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
    IPlayerCollection
}
import model.map.fixtures.towns {
    Town,
    TownStatus,
    TownSize,
    Fortification,
    City,
    Village,
    AbstractTown
}
import model.workermgmt {
    RaceFactory
}

import util {
    Warning
}
Town readTown(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "town");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    Town fix = Town(
        TownStatus.parseTownStatus(getAttribute(element, "status")),
        TownSize.parseTownSize(getAttribute(element, "size")),
        getIntegerAttribute(element, "dc"),
        getAttribute(element, "name", ""),
        getOrGenerateID(element, warner, idFactory),
        getPlayerOrIndependent(element, warner, players));
    fix.portrait = getAttribute(element, "portrait", "");
    return setImage(fix, element, warner);
}

Fortification readFortification(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "fortification");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    Fortification fix = Fortification(
        TownStatus.parseTownStatus(getAttribute(element, "status")),
        TownSize.parseTownSize(getAttribute(element, "size")),
        getIntegerAttribute(element, "dc"),
        getAttribute(element, "name", ""),
        getOrGenerateID(element, warner, idFactory),
        getPlayerOrIndependent(element, warner, players));
    fix.portrait = getAttribute(element, "portrait", "");
    return setImage(fix, element, warner);
}

City readCity(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "city");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    City fix = City(
        TownStatus.parseTownStatus(getAttribute(element, "status")),
        TownSize.parseTownSize(getAttribute(element, "size")),
        getIntegerAttribute(element, "dc"),
        getAttribute(element, "name", ""),
        getOrGenerateID(element, warner, idFactory),
        getPlayerOrIndependent(element, warner, players));
    fix.portrait = getAttribute(element, "portrait", "");
    return setImage(fix, element, warner);
}

Village readVillage(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "village");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    Integer idNum = getOrGenerateID(element, warner, idFactory);
    Village retval = Village(
        TownStatus.parseTownStatus(getAttribute(element, "status")),
        getAttribute(element, "name", ""), idNum,
        getPlayerOrIndependent(element, warner, players),
        getAttribute(element, "race", RaceFactory.getRace(JRandom(idNum))));
    retval.portrait = getAttribute(element, "portrait", "");
    return setImage(retval, element, warner);
}

void writeVillage(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Village obj) {
        writeTag(ostream, "village", indent, true);
        writeAttribute(ostream, "status", obj.status().string);
        writeNonEmptyAttribute(ostream, "name", obj.name);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
        writeAttribute(ostream, "race", obj.race);
        writeImage(ostream, obj);
        writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
    } else {
        throw IllegalArgumentException("Can only write Villages");
    }
}

void writeTown(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is AbstractTown obj) {
        writeTag(ostream, obj.kind(), indent, true);
        writeAttribute(ostream, "status", obj.status().string);
        writeAttribute(ostream, "size", obj.size().string);
        writeIntegerAttribute(ostream, "dc", obj.dc);
        writeNonEmptyAttribute(ostream, "name", obj.name);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
        writeImage(ostream, obj);
        writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
    } else {
        throw IllegalArgumentException("Can only write AbstractTowns");
    }
}
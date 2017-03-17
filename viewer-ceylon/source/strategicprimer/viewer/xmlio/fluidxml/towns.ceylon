import controller.map.fluidxml {
    XMLHelper
}
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
    XMLHelper.requireTag(element, parent, "town");
    XMLHelper.requireNonEmptyAttribute(element, "name", false, warner);
    XMLHelper.spinUntilEnd(element.name, stream);
    Town fix = Town(
        TownStatus.parseTownStatus(XMLHelper.getAttribute(element, "status")),
        TownSize.parseTownSize(XMLHelper.getAttribute(element, "size")),
        XMLHelper.getIntegerAttribute(element, "dc"),
        XMLHelper.getAttribute(element, "name", ""),
        XMLHelper.getOrGenerateID(element, warner, idFactory),
        XMLHelper.getPlayerOrIndependent(element, warner, players));
    fix.portrait = XMLHelper.getAttribute(element, "portrait", "");
    return XMLHelper.setImage(fix, element, warner);
}

Fortification readFortification(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "fortification");
    XMLHelper.requireNonEmptyAttribute(element, "name", false, warner);
    XMLHelper.spinUntilEnd(element.name, stream);
    Fortification fix = Fortification(
        TownStatus.parseTownStatus(XMLHelper.getAttribute(element, "status")),
        TownSize.parseTownSize(XMLHelper.getAttribute(element, "size")),
        XMLHelper.getIntegerAttribute(element, "dc"),
        XMLHelper.getAttribute(element, "name", ""),
        XMLHelper.getOrGenerateID(element, warner, idFactory),
        XMLHelper.getPlayerOrIndependent(element, warner, players));
    fix.portrait = XMLHelper.getAttribute(element, "portrait", "");
    return XMLHelper.setImage(fix, element, warner);
}

City readCity(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "city");
    XMLHelper.requireNonEmptyAttribute(element, "name", false, warner);
    XMLHelper.spinUntilEnd(element.name, stream);
    City fix = City(
        TownStatus.parseTownStatus(XMLHelper.getAttribute(element, "status")),
        TownSize.parseTownSize(XMLHelper.getAttribute(element, "size")),
        XMLHelper.getIntegerAttribute(element, "dc"),
        XMLHelper.getAttribute(element, "name", ""),
        XMLHelper.getOrGenerateID(element, warner, idFactory),
        XMLHelper.getPlayerOrIndependent(element, warner, players));
    fix.portrait = XMLHelper.getAttribute(element, "portrait", "");
    return XMLHelper.setImage(fix, element, warner);
}

Village readVillage(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "village");
    XMLHelper.requireNonEmptyAttribute(element, "name", false, warner);
    XMLHelper.spinUntilEnd(element.name, stream);
    Integer idNum = XMLHelper.getOrGenerateID(element, warner, idFactory);
    Village retval = Village(
        TownStatus.parseTownStatus(XMLHelper.getAttribute(element, "status")),
        XMLHelper.getAttribute(element, "name", ""), idNum,
        XMLHelper.getPlayerOrIndependent(element, warner, players),
        XMLHelper.getAttribute(element, "race", RaceFactory.getRace(JRandom(idNum))));
    retval.portrait = XMLHelper.getAttribute(element, "portrait", "");
    return XMLHelper.setImage(retval, element, warner);
}

void writeVillage(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Village obj) {
        XMLHelper.writeTag(ostream, "village", indent, true);
        XMLHelper.writeAttribute(ostream, "status", obj.status().string);
        XMLHelper.writeNonEmptyAttribute(ostream, "name", obj.name);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
        XMLHelper.writeAttribute(ostream, "race", obj.race);
        XMLHelper.writeImage(ostream, obj);
        XMLHelper.writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
    } else {
        throw IllegalArgumentException("Can only write Villages");
    }
}

void writeTown(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is AbstractTown obj) {
        XMLHelper.writeTag(ostream, obj.kind(), indent, true);
        XMLHelper.writeAttribute(ostream, "status", obj.status().string);
        XMLHelper.writeAttribute(ostream, "size", obj.size().string);
        XMLHelper.writeIntegerAttribute(ostream, "dc", obj.dc);
        XMLHelper.writeNonEmptyAttribute(ostream, "name", obj.name);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
        XMLHelper.writeImage(ostream, obj);
        XMLHelper.writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
    } else {
        throw IllegalArgumentException("Can only write AbstractTowns");
    }
}
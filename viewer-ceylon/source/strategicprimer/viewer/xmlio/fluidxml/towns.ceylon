import controller.map.formatexceptions {
    MissingPropertyException
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

import strategicprimer.viewer.drivers.advancement {
    randomRace
}
import strategicprimer.viewer.model {
    IDRegistrar
}
import strategicprimer.viewer.model.map.fixtures.towns {
    TownStatus,
    TownSize,
    Village,
    AbstractTown,
    City,
    Town,
    Fortification
}

import util {
    Warning
}
Town readTown(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "town");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    if (exists size = TownSize.parse(getAttribute(element, "size"))) {
        if (exists status = TownStatus.parse(getAttribute(element, "status"))) {
            Town fix = Town(status, size, getIntegerAttribute(element, "dc"),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory),
                getPlayerOrIndependent(element, warner, players));
            fix.portrait =getAttribute(element, "portrait", "");
            return setImage(fix, element, warner);
        } else {
            throw MissingPropertyException(element, "status");
        }
    } else {
        throw MissingPropertyException(element, "size");
    }
}

Fortification readFortification(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "fortification");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    if (exists size = TownSize.parse(getAttribute(element, "size"))) {
        if (exists status = TownStatus.parse(getAttribute(element, "status"))) {
            Fortification fix = Fortification(status, size,
                getIntegerAttribute(element, "dc"),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory),
                getPlayerOrIndependent(element, warner, players));
            fix.portrait =getAttribute(element, "portrait", "");
            return setImage(fix, element, warner);
        } else {
            throw MissingPropertyException(element, "status");
        }
    } else {
        throw MissingPropertyException(element, "size");
    }
}

City readCity(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "city");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    if (exists size = TownSize.parse(getAttribute(element, "size"))) {
        if (exists status = TownStatus.parse(getAttribute(element, "status"))) {
            City fix = City(status, size, getIntegerAttribute(element, "dc"),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory),
                getPlayerOrIndependent(element, warner, players));
            fix.portrait =getAttribute(element, "portrait", "");
            return setImage(fix, element, warner);
        } else {
            throw MissingPropertyException(element, "status");
        }
    } else {
        throw MissingPropertyException(element, "size");
    }
}

Village readVillage(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "village");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    Integer idNum = getOrGenerateID(element, warner, idFactory);
    JRandom rng = JRandom(idNum);
    if (exists status = TownStatus.parse(getAttribute(element, "status"))) {
        Village retval = Village(status, getAttribute(element, "name", ""), idNum,
            getPlayerOrIndependent(element, warner, players),
            getAttribute(element, "race", randomRace((bound) => rng.nextInt(bound))));
        retval.portrait =getAttribute(element, "portrait", "");
        return setImage(retval, element, warner);
    } else {
        throw MissingPropertyException(element, "status");
    }
}

void writeVillage(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Village obj) {
        writeTag(ostream, "village", indent, true);
        writeAttribute(ostream, "status", obj.status.string);
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
        writeTag(ostream, obj.kind, indent, true);
        writeAttribute(ostream, "status", obj.status.string);
        writeAttribute(ostream, "size", obj.size.string);
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
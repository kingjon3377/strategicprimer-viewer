import java.lang {
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

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    IPlayerCollection
}
import strategicprimer.model.map.fixtures.mobile.worker {
    randomRace
}
import strategicprimer.model.map.fixtures.towns {
    TownStatus,
    TownSize,
    Village,
    AbstractTown,
    City,
    Town,
    Fortification
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    MissingPropertyException
}
Town readTown(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "town");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    value size = TownSize.parse(getAttribute(element, "size"));
    if (is TownSize size) {
        value status = TownStatus.parse(getAttribute(element, "status"));
        if (is TownStatus status) {
            Town fix = Town(status, size, getIntegerAttribute(element, "dc"),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory),
                getPlayerOrIndependent(element, warner, players));
            fix.portrait =getAttribute(element, "portrait", "");
            return setImage(fix, element, warner);
        } else {
            throw MissingPropertyException(element, "status", status);
        }
    } else {
        throw MissingPropertyException(element, "size", size);
    }
}

Fortification readFortification(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "fortification");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    value size = TownSize.parse(getAttribute(element, "size"));
    if (is TownSize size) {
        value status = TownStatus.parse(getAttribute(element, "status"));
        if (is TownStatus status) {
            Fortification fix = Fortification(status, size,
                getIntegerAttribute(element, "dc"),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory),
                getPlayerOrIndependent(element, warner, players));
            fix.portrait =getAttribute(element, "portrait", "");
            return setImage(fix, element, warner);
        } else {
            throw MissingPropertyException(element, "status", status);
        }
    } else {
        throw MissingPropertyException(element, "size", size);
    }
}

City readCity(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "city");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    value size = TownSize.parse(getAttribute(element, "size"));
    if (is TownSize size) {
        value status = TownStatus.parse(getAttribute(element, "status"));
        if (is TownStatus status) {
            City fix = City(status, size, getIntegerAttribute(element, "dc"),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory),
                getPlayerOrIndependent(element, warner, players));
            fix.portrait =getAttribute(element, "portrait", "");
            return setImage(fix, element, warner);
        } else {
            throw MissingPropertyException(element, "status", status);
        }
    } else {
        throw MissingPropertyException(element, "size", size);
    }
}

Village readVillage(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "village");
    requireNonEmptyAttribute(element, "name", false, warner);
    spinUntilEnd(element.name, stream);
    Integer idNum = getOrGenerateID(element, warner, idFactory);
    JRandom rng = JRandom(idNum);
    value status = TownStatus.parse(getAttribute(element, "status"));
    if (is TownStatus status) {
        Village retval = Village(status, getAttribute(element, "name", ""), idNum,
            getPlayerOrIndependent(element, warner, players),
            getAttribute(element, "race", randomRace((bound) => rng.nextInt(bound))));
        retval.portrait =getAttribute(element, "portrait", "");
        return setImage(retval, element, warner);
    } else {
        throw MissingPropertyException(element, "status", status);
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
        writeAttribute(ostream, "size", obj.townSize.string);
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
import ceylon.language.meta {
    classDeclaration
}

import java.lang {
    JAppendable=Appendable,
    IllegalStateException
}
import java.util {
    JRandom=Random
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    XMLEvent,
    StartElement
}

import strategicprimer.viewer.drivers.advancement {
    randomRace
}
import strategicprimer.viewer.model {
    IDRegistrar
}
import strategicprimer.viewer.model.map {
    IPlayerCollection,
    Player
}
import strategicprimer.viewer.model.map.fixtures.towns {
    TownStatus,
    TownSize,
    ITownFixture,
    Fortress,
    Village,
    AbstractTown,
    City,
    Fortification,
    Town
}
import strategicprimer.viewer.xmlio {
    Warning,
    UnwantedChildException,
    MissingPropertyException
}
"A reader for fortresses, villages, and other towns."
class YATownReader(Warning warner, IDRegistrar idRegistrar, IPlayerCollection players)
        extends YAAbstractReader<ITownFixture>(warner, idRegistrar) {
    value memberReaders = {
        YAUnitReader(warner, idRegistrar, players),
        YAResourcePileReader(warner, idRegistrar), YAImplementReader(warner, idRegistrar)
    };
    """If the tag has an "owner" parameter, return the player it indicates; otherwise
       trigger a warning and return the "independent" player."""
    Player getOwnerOrIndependent(StartElement element) {
        if (hasParameter(element, "owner")) {
            return players.getPlayer(getIntegerParameter(element, "owner"));
        } else {
            warner.handle(MissingPropertyException(element, "owner"));
            return players.independent;
        }
    }
    ITownFixture parseVillage(StartElement element, {XMLEvent*} stream) {
        requireNonEmptyParameter(element, "name", false);
        spinUntilEnd(element.name, stream);
        Integer idNum = getOrGenerateID(element);
        JRandom rng = JRandom(idNum);
        if (exists status = TownStatus.parse(getParameter(element, "status"))) {
            Village retval = Village(status, getParameter(element, "name", ""), idNum,
                getOwnerOrIndependent(element), getParameter(element, "race",
                    randomRace((bound) => rng.nextInt(bound))));
            retval.image = getParameter(element, "image", "");
            retval.portrait =getParameter(element, "portrait", "");
            return retval;
        } else {
            throw MissingPropertyException(element, "status");
        }
    }
    ITownFixture parseTown(StartElement element, {XMLEvent*} stream) {
        requireNonEmptyParameter(element, "name", false);
        String name = getParameter(element, "name", "");
        TownStatus? status = TownStatus.parse(getParameter(element, "status"));
        TownSize? size = TownSize.parse(getParameter(element, "size"));
        if (!size exists) {
            throw MissingPropertyException(element, "size");
        }
        if (!status exists) {
            throw MissingPropertyException(element, "status");
        }
        assert (exists size, exists status);
        Integer dc = getIntegerParameter(element, "dc");
        Integer id = getOrGenerateID(element);
        Player owner = getOwnerOrIndependent(element);
        AbstractTown retval;
        switch (element.name.localPart.lowercased)
        case ("town") { retval = Town(status, size, dc, name, id, owner); }
        case ("city") { retval = City(status, size, dc, name, id, owner); }
        case ("fortification") {
            retval = Fortification(status, size, dc, name, id, owner);
        }
        else {
            throw IllegalStateException("Unhandled town tag");
        }
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        retval.portrait = getParameter(element, "portrait", "");
        return retval;
    }
    ITownFixture parseFortress(StartElement element, {XMLEvent*} stream) {
        requireNonEmptyParameter(element, "owner", false);
        requireNonEmptyParameter(element, "name", false);
        Fortress retval;
        if (exists size = TownSize.parse(getParameter(element, "size", "small"))) {
            retval = Fortress(getOwnerOrIndependent(element),
                getParameter(element, "name", ""), getOrGenerateID(element),
                size);
        } else {
            throw MissingPropertyException(element, "size");
        }
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                String memberTag = event.name.localPart.lowercased;
                if (exists reader = memberReaders
                        .find((yar) => yar.isSupportedTag(memberTag))) {
                    retval.addMember(reader.read(event, element.name, stream));
                } else {
                    throw UnwantedChildException(element.name, event);
                }
            } else if (isMatchingEnd(element.name, event)) {
                break;
            }
        }
        retval.image = getParameter(element, "image", "");
        retval.portrait = getParameter(element, "portrait", "");
        return retval;
    }
    void writeAbstractTown(JAppendable ostream, AbstractTown obj, Integer tabs) {
        writeTag(ostream, obj.kind, tabs);
        writeProperty(ostream, "status", obj.status.string);
        writeProperty(ostream, "size", obj.townSize.string);
        writeProperty(ostream, "dc", obj.dc);
        writeNonemptyProperty(ostream, "name", obj.name);
        writeProperty(ostream, "id", obj.id);
        writeProperty(ostream, "owner", obj.owner.playerId);
        writeImageXML(ostream, obj);
        writeNonemptyProperty(ostream, "portrait", obj.portrait);
        closeLeafTag(ostream);
    }
    shared actual Boolean isSupportedTag(String tag) =>
            {"village", "fortress", "town", "city", "fortification"}
                .contains(tag.lowercased);
    shared actual ITownFixture read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "village", "fortress", "town", "city",
            "fortification");
        switch (element.name.localPart.lowercased)
        case ("village") { return parseVillage(element, stream); }
        case ("fortress") { return parseFortress(element, stream); }
        else { return parseTown(element, stream); }
    }
    shared actual void write(JAppendable ostream, ITownFixture obj, Integer tabs) {
        if (is AbstractTown obj) {
            writeAbstractTown(ostream, obj, tabs);
        } else if (is Village obj) {
            writeTag(ostream, "village", tabs);
            writeProperty(ostream, "status", obj.status.string);
            writeNonemptyProperty(ostream, "name", obj.name);
            writeProperty(ostream, "id", obj.id);
            writeProperty(ostream, "owner", obj.owner.playerId);
            writeProperty(ostream, "race", obj.race);
            writeImageXML(ostream, obj);
            writeNonemptyProperty(ostream, "portrait", obj.portrait);
            closeLeafTag(ostream);
        } else if (is Fortress obj) {
            writeTag(ostream, "fortress", tabs);
            writeProperty(ostream, "owner", obj.owner.playerId);
            writeNonemptyProperty(ostream, "name", obj.name);
            if (TownSize.small != obj.townSize) {
                writeProperty(ostream, "size", obj.size.string);
            }
            writeProperty(ostream, "id", obj.id);
            writeImageXML(ostream, obj);
            writeNonemptyProperty(ostream, "portrait", obj.portrait);
            ostream.append('>');
            if (!obj.empty) {
                ostream.append(operatingSystem.newline);
                for (member in obj) {
                    if (exists reader = memberReaders.find((yar) => yar.canWrite(member))) {
                        reader.writeRaw(ostream, member, tabs + 1);
                    } else {
                        log.error("Unhandled FortressMember type ``
                            classDeclaration(member).name``");
                    }
                }
                indent(ostream, tabs);
            }
            ostream.append("</fortress>");
            ostream.append(operatingSystem.newline);
        } else {
            throw IllegalStateException("Unexpected TownFixture type");
        }
    }
    shared actual Boolean canWrite(Object obj) => obj is ITownFixture;
}
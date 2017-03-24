import ceylon.interop.java {
    CeylonIterable
}
import ceylon.language.meta {
    classDeclaration
}

import controller.map.formatexceptions {
    MissingPropertyException,
    UnwantedChildException
}
import strategicprimer.viewer.model {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
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

import model.map {
    IPlayerCollection,
    Player
}
import model.map.fixtures.towns {
    ITownFixture,
    TownStatus,
    TownSize,
    AbstractTown,
    Town,
    City,
    Fortification,
    Fortress
}

import strategicprimer.viewer.drivers.advancement {
    randomRace
}
import strategicprimer.viewer.model.map.fixtures.towns {
    Village
}

import util {
    Warning,
    LineEnd
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
            warner.warn(MissingPropertyException(element, "owner"));
            return players.independent;
        }
    }
    ITownFixture parseVillage(StartElement element, JIterable<XMLEvent> stream) {
        requireNonEmptyParameter(element, "name", false);
        spinUntilEnd(element.name, stream);
        Integer idNum = getOrGenerateID(element);
        JRandom rng = JRandom(idNum);
        Village retval = Village(
            TownStatus.parseTownStatus(getParameter(element, "status")),
            getParameter(element, "name", ""), idNum, getOwnerOrIndependent(element),
            getParameter(element, "race", randomRace((bound) => rng.nextInt(bound))));
        retval.setImage(getParameter(element, "image", ""));
        retval.portrait = getParameter(element, "portrait", "");
        return retval;
    }
    ITownFixture parseTown(StartElement element, JIterable<XMLEvent> stream) {
        requireNonEmptyParameter(element, "name", false);
        String name = getParameter(element, "name", "");
        TownStatus status = TownStatus.parseTownStatus(getParameter(element, "status"));
        TownSize size = TownSize.parseTownSize(getParameter(element, "size"));
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
    ITownFixture parseFortress(StartElement element, JIterable<XMLEvent> stream) {
        requireNonEmptyParameter(element, "owner", false);
        requireNonEmptyParameter(element, "name", false);
        Fortress retval = Fortress(getOwnerOrIndependent(element),
            getParameter(element, "name", ""), getOrGenerateID(element),
            TownSize.parseTownSize(getParameter(element, "size", "small")));
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
        writeTag(ostream, obj.kind(), tabs);
        writeProperty(ostream, "status", obj.status().string);
        writeProperty(ostream, "size", obj.size().string);
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
            JIterable<XMLEvent> stream) {
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
            writeProperty(ostream, "status", obj.status().string);
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
            if (TownSize.small != obj.size()) {
                writeProperty(ostream, "size", obj.size().string);
            }
            writeProperty(ostream, "id", obj.id);
            writeImageXML(ostream, obj);
            writeNonemptyProperty(ostream, "portrait", obj.portrait);
            ostream.append('>');
            if (!CeylonIterable(obj).empty) {
                ostream.append(LineEnd.lineSep);
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
            ostream.append(LineEnd.lineSep);
        } else {
            throw IllegalStateException("Unexpected TownFixture type");
        }
    }
    shared actual Boolean canWrite(Object obj) => obj is ITownFixture;
}
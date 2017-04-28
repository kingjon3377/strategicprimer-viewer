import ceylon.language.meta {
    classDeclaration
}

import java.lang {
    IllegalStateException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    XMLEvent,
    StartElement,
    EndElement
}

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    IPlayerCollection,
    Player
}
import strategicprimer.model.map.fixtures.mobile.worker {
    randomRace
}
import strategicprimer.model.map.fixtures.towns {
    TownStatus,
    TownSize,
    ITownFixture,
    Fortress,
    Village,
    AbstractTown,
    City,
    Fortification,
    Town,
    CommunityStats
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    MissingPropertyException,
    UnwantedChildException
}
import ceylon.random {
    DefaultRandom
}
import strategicprimer.model.map.fixtures {
    ResourcePile
}
import ceylon.collection {
    Stack,
    LinkedList
}
"A reader for fortresses, villages, and other towns."
class YATownReader(Warning warner, IDRegistrar idRegistrar, IPlayerCollection players)
        extends YAAbstractReader<ITownFixture>(warner, idRegistrar) {
    value resourceReader = YAResourcePileReader(warner, idRegistrar);
    value memberReaders = {
        YAUnitReader(warner, idRegistrar, players),
        resourceReader, YAImplementReader(warner, idRegistrar)
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
    shared CommunityStats parseCommunityStats(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "population");
        CommunityStats retval = CommunityStats(getIntegerParameter(element, "size"));
        variable String? current = null;
        Stack<StartElement> stack = LinkedList<StartElement>();
        stack.push(element);
        for (event in stream) {
            if (is EndElement event, event.name == element.name) {
                break;
            } else if (is StartElement event, isSPStartElement(event)) {
                switch (event.name.localPart)
                case ("expertise") {
                    retval.setSkillLevel(getParameter(event, "skill"),
                        getIntegerParameter(event, "level"));
                    stack.push(event);
                }
                case ("claim") {
                    retval.addWorkedField(getIntegerParameter(event, "resource"));
                    stack.push(event);
                }
                case ("production"|"consumption") {
                    if (current is Null) {
                        current = event.name.localPart;
                        stack.push(event);
                    } else {
                        assert (exists top = stack.top);
                        throw UnwantedChildException(top.name, event);
                    }
                }
                case ("resource") {
                    assert (exists top = stack.top);
                    Anything(ResourcePile) lambda;
                    switch (current)
                    case ("production") {
                        lambda = retval.yearlyProduction.add;
                    }
                    case ("consumption") {
                        lambda = retval.yearlyConsumption.add;
                    }
                    else {
                        throw UnwantedChildException(top.name, event);
                    }
                    lambda(resourceReader.read(event, top.name, stream));
                }
                else {}
            } else if (is EndElement event, exists top = stack.top, event.name == top.name) {
                stack.pop();
                if (top == element) {
                    break;
                }
            }
        }
        return retval;
    }
    ITownFixture parseVillage(StartElement element, {XMLEvent*} stream) {
        requireNonEmptyParameter(element, "name", false);
        Integer idNum = getOrGenerateID(element);
        value status = TownStatus.parse(getParameter(element, "status"));
        if (is TownStatus status) {
            Village retval = Village(status, getParameter(element, "name", ""), idNum,
                getOwnerOrIndependent(element), getParameter(element, "race",
                    randomRace(DefaultRandom(idNum))));
            retval.image = getParameter(element, "image", "");
            retval.portrait =getParameter(element, "portrait", "");
            for (event in stream) {
                if (is StartElement event, isSPStartElement(event)) {
                    if (retval.population exists) {
                        throw UnwantedChildException(element.name, event);
                    } else {
                        retval.population = parseCommunityStats(event, element.name,
                            stream);
                    }
                } else if (isMatchingEnd(element.name, event)) {
                    break;
                }
            }
            return retval;
        } else {
            throw MissingPropertyException(element, "status", status);
        }
    }
    ITownFixture parseTown(StartElement element, {XMLEvent*} stream) {
        requireNonEmptyParameter(element, "name", false);
        String name = getParameter(element, "name", "");
        value status = TownStatus.parse(getParameter(element, "status"));
        if (is TownStatus status) {
            value size = TownSize.parse(getParameter(element, "size"));
            if (is TownSize size) {
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
                for (event in stream) {
                    if (is StartElement event, isSPStartElement(event)) {
                        if (retval.population exists) {
                            throw UnwantedChildException(element.name, event);
                        } else {
                            retval.population = parseCommunityStats(event, element.name,
                                stream);
                        }
                    } else if (isMatchingEnd(element.name, event)) {
                        break;
                    }
                }
                retval.image = getParameter(element, "image", "");
                retval.portrait = getParameter(element, "portrait", "");
                return retval;
            } else {
                throw MissingPropertyException(element, "size", size);
            }
        } else {
            throw MissingPropertyException(element, "status", status);
        }
    }
    ITownFixture parseFortress(StartElement element, {XMLEvent*} stream) {
        requireNonEmptyParameter(element, "owner", false);
        requireNonEmptyParameter(element, "name", false);
        Fortress retval;
        value size = TownSize.parse(getParameter(element, "size", "small"));
        if (is TownSize size) {
            retval = Fortress(getOwnerOrIndependent(element),
                getParameter(element, "name", ""), getOrGenerateID(element),
                size);
        } else {
            throw MissingPropertyException(element, "size", size);
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
    void writeAbstractTown(Anything(String) ostream, AbstractTown obj, Integer tabs) {
        writeTag(ostream, obj.kind, tabs);
        writeProperty(ostream, "status", obj.status.string);
        writeProperty(ostream, "size", obj.townSize.string);
        writeProperty(ostream, "dc", obj.dc);
        writeNonemptyProperty(ostream, "name", obj.name);
        writeProperty(ostream, "id", obj.id);
        writeProperty(ostream, "owner", obj.owner.playerId);
        writeImageXML(ostream, obj);
        writeNonemptyProperty(ostream, "portrait", obj.portrait);
        if (exists temp = obj.population) {
            finishParentTag(ostream);
            writeCommunityStats(ostream, temp, tabs + 1);
            closeTag(ostream, tabs, obj.kind);
        } else {
            closeLeafTag(ostream);
        }
    }
    shared void writeCommunityStats(Anything(String) ostream, CommunityStats obj, Integer tabs) {
        writeTag(ostream, "population", tabs);
        writeProperty(ostream, "size", obj.population);
        finishParentTag(ostream);
        for (skill->level in obj.highestSkillLevels) {
            writeTag(ostream, "expertise", tabs + 1);
            writeProperty(ostream, "skill", skill);
            writeProperty(ostream, "level", level);
            closeLeafTag(ostream);
        }
        for (claim in obj.workedFields) {
            writeTag(ostream, "claim", tabs + 1);
            writeProperty(ostream, "resource", claim);
            closeLeafTag(ostream);
        }
        if (!obj.yearlyProduction.empty) {
            writeTag(ostream, "production", tabs + 1);
            finishParentTag(ostream);
            for (resource in obj.yearlyProduction) {
                resourceReader.write(ostream, resource, tabs + 2);
            }
            closeTag(ostream, tabs + 1, "production");
        }
        if (!obj.yearlyConsumption.empty) {
            writeTag(ostream, "consumption", tabs + 1);
            finishParentTag(ostream);
            for (resource in obj.yearlyConsumption) {
                resourceReader.write(ostream, resource, tabs + 2);
            }
            closeTag(ostream, tabs + 1, "consumption");
        }
        closeTag(ostream, tabs, "population");
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
    shared actual void write(Anything(String) ostream, ITownFixture obj, Integer tabs) {
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
            if (exists temp = obj.population) {
                finishParentTag(ostream);
                writeCommunityStats(ostream, temp, tabs + 1);
                closeTag(ostream, tabs, "village");
            } else {
                closeLeafTag(ostream);
            }
        } else if (is Fortress obj) {
            writeTag(ostream, "fortress", tabs);
            writeProperty(ostream, "owner", obj.owner.playerId);
            writeNonemptyProperty(ostream, "name", obj.name);
            if (TownSize.small != obj.townSize) {
                writeProperty(ostream, "size", obj.townSize.string);
            }
            writeProperty(ostream, "id", obj.id);
            writeImageXML(ostream, obj);
            writeNonemptyProperty(ostream, "portrait", obj.portrait);
            ostream(">");
            if (!obj.empty) {
                ostream(operatingSystem.newline);
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
            ostream("</fortress>");
            ostream(operatingSystem.newline);
        } else {
            throw IllegalStateException("Unexpected TownFixture type");
        }
    }
    shared actual Boolean canWrite(Object obj) => obj is ITownFixture;
}
import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamWriter
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
    EndElement
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map {
    IPlayerCollection
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    raceFactory
}
import strategicprimer.model.common.map.fixtures.towns {
    TownStatus,
    TownSize,
    Village,
    AbstractTown,
    City,
    Town,
    Fortification,
    CommunityStats
}
import strategicprimer.model.common.xmlio {
    Warning
}
import strategicprimer.model.impl.xmlio.exceptions {
    MissingPropertyException,
    UnwantedChildException
}
import ceylon.random {
    DefaultRandom
}
import ceylon.collection {
    Stack,
    LinkedList
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile
}
import lovelace.util.common {
    comparingOn
}

object fluidTownHandler extends FluidBase() {
    shared Town readTown(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "town");
        expectAttributes(element, warner, "name", "size", "status", "dc", "id",
            "portrait", "image", "owner");
        requireNonEmptyAttribute(element, "name", false, warner);
        value size = TownSize.parse(getAttribute(element, "size"));
        if (is TownSize size) {
            value status = TownStatus.parse(getAttribute(element, "status"));
            if (is TownStatus status) {
                Town fix = Town(status, size, getIntegerAttribute(element, "dc"),
                    getAttribute(element, "name", ""),
                    getOrGenerateID(element, warner, idFactory),
                    getPlayerOrIndependent(element, warner, players));
                fix.portrait = getAttribute(element, "portrait", "");
                for (event in stream) {
                    if (is StartElement event, isSPStartElement(event)) {
                        if (!fix.population exists) {
                            fix.population = readCommunityStats(event, element.name,
                                stream, players, warner, idFactory);
                        } else {
                            throw UnwantedChildException(element.name, event);
                        }
                    } else if (is EndElement event, event.name == element.name) {
                        break;
                    }
                }
                return setImage(fix, element, warner);
            } else {
                throw MissingPropertyException(element, "status", status);
            }
        } else {
            throw MissingPropertyException(element, "size", size);
        }
    }

    shared Fortification readFortification(StartElement element, QName parent,
            {XMLEvent*} stream, IPlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        requireTag(element, parent, "fortification");
        expectAttributes(element, warner, "name", "size", "status", "dc", "id",
            "portrait", "image", "owner");
        requireNonEmptyAttribute(element, "name", false, warner);
        value size = TownSize.parse(getAttribute(element, "size"));
        if (is TownSize size) {
            value status = TownStatus.parse(getAttribute(element, "status"));
            if (is TownStatus status) {
                Fortification fix = Fortification(status, size,
                    getIntegerAttribute(element, "dc"),
                    getAttribute(element, "name", ""),
                    getOrGenerateID(element, warner, idFactory),
                    getPlayerOrIndependent(element, warner, players));
                fix.portrait = getAttribute(element, "portrait", "");
                for (event in stream) {
                    if (is StartElement event, isSPStartElement(event)) {
                        if (!fix.population exists) {
                            fix.population = readCommunityStats(event, element.name,
                                stream, players, warner, idFactory);
                        } else {
                            throw UnwantedChildException(element.name, event);
                        }
                    } else if (is EndElement event, event.name == element.name) {
                        break;
                    }
                }
                return setImage(fix, element, warner);
            } else {
                throw MissingPropertyException(element, "status", status);
            }
        } else {
            throw MissingPropertyException(element, "size", size);
        }
    }

    shared City readCity(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "city");
        expectAttributes(element, warner, "name", "size", "status", "dc", "id",
            "portrait", "image", "owner");
        requireNonEmptyAttribute(element, "name", false, warner);
        value size = TownSize.parse(getAttribute(element, "size"));
        if (is TownSize size) {
            value status = TownStatus.parse(getAttribute(element, "status"));
            if (is TownStatus status) {
                City fix = City(status, size, getIntegerAttribute(element, "dc"),
                    getAttribute(element, "name", ""),
                    getOrGenerateID(element, warner, idFactory),
                    getPlayerOrIndependent(element, warner, players));
                fix.portrait = getAttribute(element, "portrait", "");
                for (event in stream) {
                    if (is StartElement event, isSPStartElement(event)) {
                        if (!fix.population exists) {
                            fix.population = readCommunityStats(event, element.name,
                                stream, players, warner, idFactory);
                        } else {
                            throw UnwantedChildException(element.name, event);
                        }
                    } else if (is EndElement event, event.name == element.name) {
                        break;
                    }
                }
                return setImage(fix, element, warner);
            } else {
                throw MissingPropertyException(element, "status", status);
            }
        } else {
            throw MissingPropertyException(element, "size", size);
        }
    }

    shared Village readVillage(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "village");
        expectAttributes(element, warner, "status", "race", "owner", "id", "image",
            "portrait", "name");
        requireNonEmptyAttribute(element, "name", false, warner);
        Integer idNum = getOrGenerateID(element, warner, idFactory);
        value status = TownStatus.parse(getAttribute(element, "status"));
        if (is TownStatus status) {
            Village retval = Village(status, getAttribute(element, "name", ""), idNum,
                getPlayerOrIndependent(element, warner, players),
                getAttribute(element, "race",
                    raceFactory.randomRace(DefaultRandom(idNum))));
            retval.portrait = getAttribute(element, "portrait", "");
            for (event in stream) {
                if (is StartElement event, isSPStartElement(event)) {
                    if (!retval.population exists) {
                        retval.population = readCommunityStats(event, element.name,
                            stream, players, warner, idFactory);
                    } else {
                        throw UnwantedChildException(element.name, event);
                    }
                } else if (is EndElement event, event.name == element.name) {
                    break;
                }
            }
            return setImage(retval, element, warner);
        } else {
            throw MissingPropertyException(element, "status", status);
        }
    }

    shared CommunityStats readCommunityStats(StartElement element, QName parent,
            {XMLEvent*} stream, IPlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        requireTag(element, parent, "population");
        expectAttributes(element, warner, "size");
        CommunityStats retval = CommunityStats(getIntegerAttribute(element, "size"));
        variable String? current = null;
        Stack<StartElement> stack = LinkedList<StartElement>();
        stack.push(element);
        for (event in stream) {
            if (is EndElement event, event.name == element.name) {
                break;
            } else if (is StartElement event, isSPStartElement(event)) {
                switch (event.name.localPart.lowercased)
                case ("expertise") {
                    expectAttributes(event, warner, "skill", "level");
                    retval.setSkillLevel(getAttribute(event, "skill"),
                        getIntegerAttribute(event, "level"));
                    stack.push(event);
                }
                case ("claim") {
                    expectAttributes(event, warner, "resource");
                    retval.addWorkedField(getIntegerAttribute(event, "resource"));
                    stack.push(event);
                }
                case ("production"|"consumption") {
                    if (current is Null) {
                        expectAttributes(event, warner);
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
                        throw UnwantedChildException.listingExpectedTags(top.name, event,
                            ["production", "consumption"]);
                    }
                    lambda(fluidResourceHandler.readResource(event, top.name, stream,
                        players, warner, idFactory));
                }
                else {
                    throw UnwantedChildException.listingExpectedTags(event.name, element,
                        ["expertise", "claim", "production", "consumption", "resource"]);
                }
            } else if (is EndElement event, exists top = stack.top,
                    event.name == top.name) {
                stack.pop();
                if (top == element) {
                    break;
                } else if (exists temp = current, temp == top.name.localPart) {
                    current = null;
                }
            }
        }
        return retval;
    }

    shared void writeVillage(XMLStreamWriter ostream, Village obj, Integer indent) {
        writeTag(ostream, "village", indent, !obj.population exists);
        writeAttributes(ostream, "status"->obj.status.string);
        writeNonEmptyAttributes(ostream, "name"->obj.name);
        writeAttributes(ostream, "id"->obj.id, "owner"->obj.owner.playerId,
            "race"->obj.race);
        writeImage(ostream, obj);
        writeNonEmptyAttributes(ostream, "portrait"->obj.portrait);
        if (exists population = obj.population) {
            writeCommunityStats(ostream, population, indent);
            ostream.writeEndElement();
        }
    }

    shared void writeTown(XMLStreamWriter ostream, AbstractTown obj, Integer indent) {
        writeTag(ostream, obj.kind, indent, !obj.population exists);
        writeAttributes(ostream, "status"->obj.status.string,
            "size"->obj.townSize.string, "dc"->obj.dc);
        writeNonEmptyAttributes(ostream, "name"->obj.name);
        writeAttributes(ostream, "id"->obj.id, "owner"->obj.owner.playerId);
        writeImage(ostream, obj);
        writeNonEmptyAttributes(ostream, "portrait"->obj.portrait);
        if (exists population = obj.population) {
            writeCommunityStats(ostream, population, indent);
            ostream.writeEndElement();
        }
    }

    shared void writeCommunityStats(XMLStreamWriter ostream, CommunityStats obj,
            Integer indent) {
        writeTag(ostream, "population", indent, false);
        writeAttributes(ostream, "size"->obj.population);
        for (skill->level in obj.highestSkillLevels.sort(comparingOn(
            Entry<String, Integer>.key, increasing<String>))) {
            writeTag(ostream, "expertise", indent + 1, true);
            writeAttributes(ostream, "skill"->skill, "level"->level);
        }
        for (claim in obj.workedFields) {
            writeTag(ostream, "claim", indent + 1, true);
            writeAttributes(ostream, "resource"->claim);
        }
        if (!obj.yearlyProduction.empty) {
            writeTag(ostream, "production", indent + 1, false);
            for (resource in obj.yearlyProduction) {
                fluidResourceHandler.writeResource(ostream, resource, indent + 2);
            }
            ostream.writeEndElement();
        }
        if (!obj.yearlyConsumption.empty) {
            writeTag(ostream, "consumption", indent + 1, false);
            for (resource in obj.yearlyConsumption) {
                fluidResourceHandler.writeResource(ostream, resource, indent + 2);
            }
            ostream.writeEndElement();
        }
        ostream.writeEndElement();
    }
}

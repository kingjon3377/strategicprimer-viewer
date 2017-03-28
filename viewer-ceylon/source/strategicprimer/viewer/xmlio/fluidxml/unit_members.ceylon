import ceylon.interop.java {
    CeylonIterable
}

import controller.map.formatexceptions {
    UnwantedChildException,
    UnsupportedPropertyException,
    MissingPropertyException
}
import strategicprimer.viewer.model {
    IDRegistrar
}

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
    XMLEvent,
    EndElement
}

import model.map {
    IPlayerCollection,
    HasPortrait
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IWorker,
    Worker
}
import model.map.fixtures.mobile {
    Animal,
    SimpleImmortal
}
import model.map.fixtures.mobile.worker {
    ISkill
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    IJob,
    Job,
    WorkerStats,
    Skill
}

import util {
    Warning
}
Worker readWorker(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "worker");
    Worker retval = setImage(
        Worker(getAttribute(element, "name"),
            getAttribute(element, "race", "human"),
            getOrGenerateID(element, warner, idFactory)),
        element, warner);
    retval.portrait = getAttribute(element, "portrait", "");
    for (event in stream) {
        if (is StartElement event, isSPStartElement(event)) {
            switch (event.name.localPart)
            case ("job") { retval.addJob(readJob(event, element.name, stream, players, warner, idFactory)); }
            case ("stats") { retval.stats = readStats(event, element.name, stream, players, warner, idFactory); }
            else { throw UnwantedChildException(element.name, event); }
        } else if (is EndElement event, element.name == event.name) {
            break;
        }
    }
    return retval;
}

IJob readJob(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "job");
    if (hasAttribute(element, "hours")) {
        warner.warn(UnsupportedPropertyException(element, "hours"));
    }
    IJob retval = Job(getAttribute(element, "name"),
        getIntegerAttribute(element, "level"));
    for (event in stream) {
        if (is StartElement event, isSPStartElement(event)) {
            if ("skill" == event.name.localPart) {
                retval.addSkill(readSkill(event, element.name, stream, players, warner, idFactory));
            } else {
                throw UnwantedChildException(element.name, event);
            }
        } else if (is EndElement event, element.name == event.name) {
            break;
        }
    }
    return retval;
}

ISkill readSkill(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "skill");
    requireNonEmptyAttribute(element, "name", true, warner);
    spinUntilEnd(element.name, stream);
    return Skill(getAttribute(element, "name"),
        getIntegerAttribute(element, "level"),
        getIntegerAttribute(element, "hours"));
}

WorkerStats readStats(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "stats");
    spinUntilEnd(element.name, stream);
    return WorkerStats(getIntegerAttribute(element, "hp"),
        getIntegerAttribute(element, "max"),
        getIntegerAttribute(element, "str"),
        getIntegerAttribute(element, "dex"),
        getIntegerAttribute(element, "con"),
        getIntegerAttribute(element, "int"),
        getIntegerAttribute(element, "wis"),
        getIntegerAttribute(element, "cha"));
}

void writeWorker(XMLStreamWriter ostream, Object obj, Integer indentation) {
    if (is IWorker obj) {
        WorkerStats? stats = obj.stats;
        // TODO: what about empty Jobs?
        Boolean hasJobs = !obj.empty;
        writeTag(ostream, "worker", indentation, !hasJobs && !stats exists);
        writeAttribute(ostream, "name", obj.name);
        if ("human" != obj.race) {
            writeAttribute(ostream, "race", obj.race);
        }
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
        if (is HasPortrait obj) {
            writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
        }
        if (exists stats) {
            writeStats(ostream, stats, indentation + 1);
        }
        for (job in obj) {
            writeJob(ostream, job, indentation + 1);
        }
        if (hasJobs || stats exists) {
            indent(ostream, indentation);
            ostream.writeEndElement();
        }
    } else {
        throw IllegalArgumentException("Can only write IWorkers");
    }
}

void writeStats(XMLStreamWriter ostream, Object obj, Integer indentation) {
    if (is WorkerStats obj) {
        writeTag(ostream, "stats", indentation, true);
        writeIntegerAttribute(ostream, "hp", obj.hitPoints);
        writeIntegerAttribute(ostream, "max", obj.maxHitPoints);
        writeIntegerAttribute(ostream, "str", obj.strength);
        writeIntegerAttribute(ostream, "dex", obj.dexterity);
        writeIntegerAttribute(ostream, "con", obj.constitution);
        writeIntegerAttribute(ostream, "int", obj.intelligence);
        writeIntegerAttribute(ostream, "wis", obj.wisdom);
        writeIntegerAttribute(ostream, "cha", obj.charisma);
    } else {
        throw IllegalArgumentException("Can only write WorkerStats");
    }
}

void writeJob(XMLStreamWriter ostream, Object obj, Integer indentation) {
    if (is IJob obj) {
        Boolean hasSkills = !obj.empty;
        if (obj.level <= 0, !hasSkills) {
            return;
        }
        writeTag(ostream, "job", indentation, !hasSkills);
        writeAttribute(ostream, "name", obj.name);
        writeIntegerAttribute(ostream, "level", obj.level);
        for (skill in obj) {
            writeSkill(ostream, skill, indentation + 1);
        }
        if (hasSkills) {
            indent(ostream, indentation);
            ostream.writeEndElement();
        }
    } else {
        throw IllegalArgumentException("Can only write IJobs");
    }
}

void writeSkill(XMLStreamWriter ostream, Object obj, Integer indentation) {
    if (is ISkill obj) {
        if (!obj.empty) {
            writeTag(ostream, "skill", indentation, true);
            writeAttribute(ostream, "name", obj.name);
            writeIntegerAttribute(ostream, "level", obj.level);
            writeIntegerAttribute(ostream, "hours", obj.hours);
        }
    } else {
        throw IllegalArgumentException("Can only write ISkills");
    }
}

Animal readAnimal(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "animal");
    spinUntilEnd(element.name, stream);
    // TODO: support """traces="false""""
    Boolean traces = hasAttribute(element, "traces");
    Integer id;
    if (traces, !hasAttribute(element, "id")) {
        id = -1;
    } else {
        id = getOrGenerateID(element, warner, idFactory);
    }
    value talking = Boolean.parse(getAttribute(element, "talking", "false"));
    if (is Boolean talking) {
        return setImage(
            Animal(getAttribute(element, "kind"), traces,
                talking, getAttribute(element, "status", "wild"),
                id), element, warner);
    } else {
        throw MissingPropertyException(element, "talking", talking);
    }
}

void writeAnimal(XMLStreamWriter ostream, Object obj, Integer indentation) {
    if (is Animal obj) {
        writeTag(ostream, "animal", indentation, true);
        writeAttribute(ostream, "kind", obj.kind);
        if (obj.traces) {
            writeAttribute(ostream, "traces", "");
        }
        if (obj.talking) {
            writeBooleanAttribute(ostream, "talking", true);
        }
        if ("wild" != obj.status) {
            writeAttribute(ostream, "status", obj.status);
        }
        if (!obj.traces) {
            writeIntegerAttribute(ostream, "id", obj.id);
        }
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Animal");
    }
}

void writeSimpleImmortal(XMLStreamWriter ostream, Object obj, Integer indentation) {
    if (is SimpleImmortal obj) {
        writeTag(ostream, obj.kind, indentation, true);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write SimpleImmortals");
    }
}
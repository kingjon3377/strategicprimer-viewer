import ceylon.interop.java {
    CeylonIterable
}

import controller.map.fluidxml {
    XMLHelper
}
import controller.map.formatexceptions {
    UnwantedChildException,
    UnsupportedPropertyException,
    MissingPropertyException
}
import controller.map.misc {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
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
import model.map.fixtures.mobile {
    Worker,
    IWorker,
    Animal,
    SimpleImmortal
}
import model.map.fixtures.mobile.worker {
    IJob,
    Job,
    ISkill,
    Skill,
    WorkerStats
}

import util {
    Warning
}
Worker readWorker(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "worker");
    Worker retval = XMLHelper.setImage(
        Worker(XMLHelper.getAttribute(element, "name"),
            XMLHelper.getAttribute(element, "race", "human"),
            XMLHelper.getOrGenerateID(element, warner, idFactory)),
        element, warner);
    retval.portrait = XMLHelper.getAttribute(element, "portrait", "");
    for (event in stream) {
        if (is StartElement event, XMLHelper.isSPStartElement(event)) {
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

IJob readJob(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "job");
    if (XMLHelper.hasAttribute(element, "hours")) {
        warner.warn(UnsupportedPropertyException(element, "hours"));
    }
    IJob retval = Job(XMLHelper.getAttribute(element, "name"),
        XMLHelper.getIntegerAttribute(element, "level"));
    for (event in stream) {
        if (is StartElement event, XMLHelper.isSPStartElement(event)) {
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

ISkill readSkill(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "skill");
    XMLHelper.requireNonEmptyAttribute(element, "name", true, warner);
    XMLHelper.spinUntilEnd(element.name, stream);
    return Skill(XMLHelper.getAttribute(element, "name"),
        XMLHelper.getIntegerAttribute(element, "level"),
        XMLHelper.getIntegerAttribute(element, "hours"));
}

WorkerStats readStats(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "stats");
    XMLHelper.spinUntilEnd(element.name, stream);
    return WorkerStats(XMLHelper.getIntegerAttribute(element, "hp"),
        XMLHelper.getIntegerAttribute(element, "max"),
        XMLHelper.getIntegerAttribute(element, "str"),
        XMLHelper.getIntegerAttribute(element, "dex"),
        XMLHelper.getIntegerAttribute(element, "con"),
        XMLHelper.getIntegerAttribute(element, "int"),
        XMLHelper.getIntegerAttribute(element, "wis"),
        XMLHelper.getIntegerAttribute(element, "cha"));
}

void writeWorker(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is IWorker obj) {
        WorkerStats? stats = obj.stats;
        Boolean hasJobs = !CeylonIterable(obj).empty;
        XMLHelper.writeTag(ostream, "worker", indent, !hasJobs && !stats exists);
        XMLHelper.writeAttribute(ostream, "name", obj.name);
        if ("human" != obj.race) {
            XMLHelper.writeAttribute(ostream, "race", obj.race);
        }
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
        if (is HasPortrait obj) {
            XMLHelper.writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
        }
        if (exists stats) {
            writeStats(ostream, stats, indent + 1);
        }
        for (job in obj) {
            writeJob(ostream, job, indent + 1);
        }
        if (hasJobs || stats exists) {
            XMLHelper.indent(ostream, indent);
            ostream.writeEndElement();
        }
    } else {
        throw IllegalArgumentException("Can only write IWorkers");
    }
}

void writeStats(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is WorkerStats obj) {
        XMLHelper.writeTag(ostream, "stats", indent, true);
        XMLHelper.writeIntegerAttribute(ostream, "hp", obj.hitPoints);
        XMLHelper.writeIntegerAttribute(ostream, "max", obj.maxHitPoints);
        XMLHelper.writeIntegerAttribute(ostream, "str", obj.strength);
        XMLHelper.writeIntegerAttribute(ostream, "dex", obj.dexterity);
        XMLHelper.writeIntegerAttribute(ostream, "con", obj.constitution);
        XMLHelper.writeIntegerAttribute(ostream, "int", obj.intelligence);
        XMLHelper.writeIntegerAttribute(ostream, "wis", obj.wisdom);
        XMLHelper.writeIntegerAttribute(ostream, "cha", obj.charisma);
    } else {
        throw IllegalArgumentException("Can only write WorkerStats");
    }
}

void writeJob(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is IJob obj) {
        Boolean hasSkills = !CeylonIterable(obj).empty;
        if (obj.level <= 0, !hasSkills) {
            return;
        }
        XMLHelper.writeTag(ostream, "job", indent, !hasSkills);
        XMLHelper.writeAttribute(ostream, "name", obj.name);
        XMLHelper.writeIntegerAttribute(ostream, "level", obj.level);
        for (skill in obj) {
            writeSkill(ostream, skill, indent + 1);
        }
        if (hasSkills) {
            XMLHelper.indent(ostream, indent);
            ostream.writeEndElement();
        }
    } else {
        throw IllegalArgumentException("Can only write IJobs");
    }
}

void writeSkill(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is ISkill obj) {
        if (!obj.empty) {
            XMLHelper.writeTag(ostream, "skill", indent, true);
            XMLHelper.writeAttribute(ostream, "name", obj.name);
            XMLHelper.writeIntegerAttribute(ostream, "level", obj.level);
            XMLHelper.writeIntegerAttribute(ostream, "hours", obj.hours);
        }
    } else {
        throw IllegalArgumentException("Can only write ISkills");
    }
}

Animal readAnimal(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "animal");
    XMLHelper.spinUntilEnd(element.name, stream);
    // TODO: support """traces="false""""
    Boolean traces = XMLHelper.hasAttribute(element, "traces");
    Integer id;
    if (traces, !XMLHelper.hasAttribute(element, "id")) {
        id = -1;
    } else {
        id = XMLHelper.getOrGenerateID(element, warner, idFactory);
    }
    value talking = Boolean.parse(XMLHelper.getAttribute(element, "talking", "false"));
    if (is Boolean talking) {
        return XMLHelper.setImage(
            Animal(XMLHelper.getAttribute(element, "kind"), traces,
                talking, XMLHelper.getAttribute(element, "status", "wild"),
                id), element, warner);
    } else {
        throw MissingPropertyException(element, "talking", talking);
    }
}

void writeAnimal(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Animal obj) {
        XMLHelper.writeTag(ostream, "animal", indent, true);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        if (obj.traces) {
            XMLHelper.writeAttribute(ostream, "traces", "");
        }
        if (obj.talking) {
            XMLHelper.writeBooleanAttribute(ostream, "talking", true);
        }
        if ("wild" != obj.status) {
            XMLHelper.writeAttribute(ostream, "status", obj.status);
        }
        if (!obj.traces) {
            XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        }
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Animal");
    }
}

void writeSimpleImmortal(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is SimpleImmortal obj) {
        XMLHelper.writeTag(ostream, obj.kind, indent, true);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write SimpleImmortals");
    }
}
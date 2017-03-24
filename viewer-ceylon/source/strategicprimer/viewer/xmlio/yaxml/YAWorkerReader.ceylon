import ceylon.interop.java {
    CeylonIterable
}

import controller.map.formatexceptions {
    UnsupportedPropertyException,
    UnwantedChildException
}
import controller.map.misc {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
    JAppendable=Appendable
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import model.map {
    HasPortrait
}
import model.map.fixtures.mobile {
    IWorker,
    Worker
}
import model.map.fixtures.mobile.worker {
    WorkerStats,
    ISkill,
    Skill,
    IJob,
    Job
}

import util {
    Warning
}
"A reader for workers."
class YAWorkerReader extends YAAbstractReader<IWorker> {
    shared static void writeSkill(JAppendable ostream, ISkill obj, Integer indent) {
        if (!obj.empty) {
            writeTag(ostream, "skill", indent);
            writeProperty(ostream, "name", obj.name);
            writeProperty(ostream, "level", obj.level);
            writeProperty(ostream, "hours", obj.hours);
            closeLeafTag(ostream);
        }
    }
    shared static void writeJob(JAppendable ostream, IJob obj, Integer indent) {
        if (obj.level <= 0, CeylonIterable(obj).empty) {
            return;
        }
        writeTag(ostream, "job", indent);
        writeProperty(ostream, "name", obj.name);
        writeProperty(ostream, "level", obj.level);
        if (CeylonIterable(obj).empty) {
            closeLeafTag(ostream);
        } else {
            finishParentTag(ostream);
            for (skill in obj) {
                writeSkill(ostream, skill, indent + 1);
            }
            closeTag(ostream, indent, "job");
        }
    }
    Warning warner;
    shared new (Warning warning, IDRegistrar idRegistrar)
            extends YAAbstractReader<IWorker>(warning, idRegistrar) {
        warner = warning;
    }
    WorkerStats parseStats(StartElement element, QName parent,
            JIterable<XMLEvent> stream) {
        requireTag(element, parent, "stats");
        Integer inner(String attr) => getIntegerParameter(element, attr);
        WorkerStats retval = WorkerStats(inner("hp"), inner("max"), inner("str"),
            inner("dex"), inner("con"), inner("int"), inner("wis"), inner("cha"));
        spinUntilEnd(element.name, stream);
        return retval;
    }
    ISkill parseSkill(StartElement element, QName parent) {
        requireTag(element, parent, "skill");
        return Skill(getParameter(element, "name"), getIntegerParameter(element, "level"),
            getIntegerParameter(element, "hours"));
    }
    IJob parseJob(StartElement element, QName parent, JIterable<XMLEvent> stream) {
        requireTag(element, parent, "job");
        IJob retval = Job(getParameter(element, "name"),
            getIntegerParameter(element, "level"));
        if (hasParameter(element, "hours")) {
            warner.warn(UnsupportedPropertyException(element, "hours"));
        }
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                if ("skill" == event.name.localPart.lowercased) {
                    retval.addSkill(parseSkill(event, element.name));
                    spinUntilEnd(event.name, stream);
                } else {
                    throw UnwantedChildException(element.name, event);
                }
            } else if (isMatchingEnd(element.name, event)) {
                break;
            }
        }
        return retval;
    }
    void writeStats(JAppendable ostream, WorkerStats? stats, Integer indent) {
        if (exists stats) {
            writeTag(ostream, "stats", indent);
            writeProperty(ostream, "hp", stats.hitPoints);
            writeProperty(ostream, "max", stats.maxHitPoints);
            writeProperty(ostream, "str", stats.strength);
            writeProperty(ostream, "dex", stats.dexterity);
            writeProperty(ostream, "con", stats.constitution);
            writeProperty(ostream, "int", stats.intelligence);
            writeProperty(ostream, "wis", stats.wisdom);
            writeProperty(ostream, "cha", stats.charisma);
            closeLeafTag(ostream);
        }
    }
    shared actual IWorker read(StartElement element, QName parent,
            JIterable<XMLEvent> stream) {
        requireTag(element, parent, "worker");
        Worker retval = Worker(getParameter(element, "name"),
            getParameter(element, "race", "human"), getOrGenerateID(element));
        retval.image = getParameter(element, "image", "");
        retval.portrait = getParameter(element, "portrait", "");
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                if ("job" == event.name.localPart.lowercased) {
                    retval.addJob(parseJob(event, element.name, stream));
                } else if ("stats" == event.name.localPart.lowercased) {
                    retval.stats = parseStats(event, element.name, stream);
                } else {
                    throw UnwantedChildException(element.name, event);
                }
            } else if (isMatchingEnd(element.name, event)) {
                break;
            }
        }
        return retval;
    }
    shared actual void write(JAppendable ostream, IWorker obj, Integer indent) {
        writeTag(ostream, "worker", indent);
        writeProperty(ostream, "name", obj.name);
        if ("human" != obj.race) {
            writeProperty(ostream, "race", obj.race);
        }
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        if (is HasPortrait obj) {
            writeNonemptyProperty(ostream, "portrait", obj.portrait);
        }
        if (!CeylonIterable(obj).empty || obj.stats exists) {
            finishParentTag(ostream);
            writeStats(ostream, obj.stats, indent + 1);
            for (job in obj) {
                writeJob(ostream, job, indent + 1);
            }
            closeTag(ostream, indent, "worker");
        } else {
            closeLeafTag(ostream);
        }
    }
    shared actual Boolean isSupportedTag(String tag) => "worker" == tag.lowercased;
    shared actual Boolean canWrite(Object obj) => obj is IWorker;
}
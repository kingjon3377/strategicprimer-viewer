
import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    HasPortrait
}
import strategicprimer.model.map.fixtures.mobile {
    IWorker,
    Worker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    WorkerStats,
    Job,
    IJob,
    Skill,
    ISkill
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    UnsupportedPropertyException,
    UnwantedChildException
}
"A reader for workers."
class YAWorkerReader extends YAAbstractReader<IWorker> {
    shared static void writeSkill(Anything(String) ostream, ISkill obj, Integer indent) {
        if (!obj.empty) {
            writeTag(ostream, "skill", indent);
            writeProperty(ostream, "name", obj.name);
            writeProperty(ostream, "level", obj.level);
            writeProperty(ostream, "hours", obj.hours);
            closeLeafTag(ostream);
        }
    }
    shared static void writeJob(Anything(String) ostream, IJob obj, Integer indent) {
        if (obj.level <= 0, obj.empty) {
            return;
        }
        writeTag(ostream, "job", indent);
        writeProperty(ostream, "name", obj.name);
        writeProperty(ostream, "level", obj.level);
        if (obj.empty) {
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
            {XMLEvent*} stream) {
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
    IJob parseJob(StartElement element, QName parent, {XMLEvent*} stream) {
        requireTag(element, parent, "job");
        IJob retval = Job(getParameter(element, "name"),
            getIntegerParameter(element, "level"));
        if (hasParameter(element, "hours")) {
            warner.handle(UnsupportedPropertyException(element, "hours"));
        }
        for (event in stream) {
            if (is StartElement event, isSupportedNamespace(event.name)) {
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
    void writeStats(Anything(String) ostream, WorkerStats? stats, Integer indent) {
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
            {XMLEvent*} stream) {
        requireTag(element, parent, "worker");
        Worker retval = Worker(getParameter(element, "name"),
            getParameter(element, "race", "human"), getOrGenerateID(element));
        retval.image = getParameter(element, "image", "");
        retval.portrait = getParameter(element, "portrait", "");
        for (event in stream) {
            if (is StartElement event, isSupportedNamespace(event.name)) {
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
    shared actual void write(Anything(String) ostream, IWorker obj, Integer indent) {
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
        if (!obj.empty || obj.stats exists) {
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
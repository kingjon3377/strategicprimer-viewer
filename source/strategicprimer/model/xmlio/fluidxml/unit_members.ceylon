
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

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    IPlayerCollection,
    HasPortrait
}
import strategicprimer.model.map.fixtures.mobile {
    IWorker,
    Worker,
    SimpleImmortal,
    Animal,
    maturityModel
}
import strategicprimer.model.map.fixtures.mobile.worker {
    IJob,
    Job,
    WorkerStats,
    ISkill,
    Skill
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    UnwantedChildException,
    UnsupportedPropertyException,
    MissingPropertyException
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
            case ("job") { retval.addJob(readJob(event, element.name, stream, players,
                warner, idFactory)); }
            case ("stats") { retval.stats = readStats(event, element.name, stream,
                players, warner, idFactory); }
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
        warner.handle(UnsupportedPropertyException(element, "hours"));
    }
    IJob retval = Job(getAttribute(element, "name"),
        getIntegerAttribute(element, "level"));
    for (event in stream) {
        if (is StartElement event, isSPStartElement(event)) {
            if ("skill" == event.name.localPart) {
                retval.addSkill(readSkill(event, element.name, stream, players, warner,
                    idFactory));
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

void writeWorker(XMLStreamWriter ostream, IWorker obj, Integer indentation) {
    WorkerStats? stats = obj.stats;
    {IJob*} jobs = obj.filter((job) => !job.emptyJob);
    Boolean hasJobs = !jobs.empty;
    writeTag(ostream, "worker", indentation, !hasJobs && !stats exists);
    writeAttributes(ostream, "name"->obj.name);
    if ("human" != obj.race) {
        writeAttributes(ostream, "race"->obj.race);
    }
    writeAttributes(ostream, "id"->obj.id);
    writeImage(ostream, obj);
    if (is HasPortrait obj) {
        writeNonEmptyAttributes(ostream, "portrait"->obj.portrait);
    }
    if (exists stats) {
        writeStats(ostream, stats, indentation + 1);
    }
    for (job in jobs) {
        writeJob(ostream, job, indentation + 1);
    }
    if (hasJobs || stats exists) {
        indent(ostream, indentation);
        ostream.writeEndElement();
    }
}

void writeStats(XMLStreamWriter ostream, WorkerStats obj, Integer indentation) {
    writeTag(ostream, "stats", indentation, true);
    writeAttributes(ostream, "hp"->obj.hitPoints, "max"->obj.maxHitPoints,
        "str"->obj.strength, "dex"->obj.dexterity, "con"->obj.constitution,
        "int"->obj.intelligence, "wis"->obj.wisdom, "cha"->obj.charisma);
}

void writeJob(XMLStreamWriter ostream, IJob obj, Integer indentation) {
    Boolean hasSkills = !obj.empty;
    if (obj.level <= 0, !hasSkills) {
        return;
    }
    writeTag(ostream, "job", indentation, !hasSkills);
    writeAttributes(ostream, "name"->obj.name, "level"->obj.level);
    for (skill in obj) {
        writeSkill(ostream, skill, indentation + 1);
    }
    if (hasSkills) {
        indent(ostream, indentation);
        ostream.writeEndElement();
    }
}

void writeSkill(XMLStreamWriter ostream, ISkill obj, Integer indentation) {
    if (!obj.empty) {
        writeTag(ostream, "skill", indentation, true);
        writeAttributes(ostream, "name"->obj.name, "level"->obj.level,
            "hours"->obj.hours);
    }
}

Animal readAnimal(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "animal");
    spinUntilEnd(element.name, stream);
    // To get the intended meaning of existing maps, we have to parse
    // traces="" as traces="true". If compatibility with existing maps
    // ever becomes unnecessary, I will change the default-value here to
    // simply `false`.
    Boolean traces = getBooleanAttribute(element, "traces",
        hasAttribute(element, "traces") && getAttribute(element, "traces", "").empty,
        warner);
    Integer id;
    if (traces, !hasAttribute(element, "id")) {
        id = -1;
    } else {
        id = getOrGenerateID(element, warner, idFactory);
    }
    Integer count = getIntegerAttribute(element, "count", 1, warner);
    if (count < 1) {
        throw MissingPropertyException(element, "count",
            IllegalArgumentException("Animal population must be positive"));
    }
    return setImage(
        Animal(getAttribute(element, "kind"), traces,
            getBooleanAttribute(element, "talking", false, warner),
            getAttribute(element, "status", "wild"),
            id, getIntegerAttribute(element, "born", -1, warner), count),
            element, warner);
}

void writeAnimal(XMLStreamWriter ostream, Animal obj, Integer indentation) {
    writeTag(ostream, "animal", indentation, true);
    writeAttributes(ostream, "kind"->obj.kind);
    if (obj.traces) {
        writeAttributes(ostream, "traces"->true);
    }
    if (obj.talking) {
        writeAttributes(ostream, "talking"->true);
    }
    if ("wild" != obj.status) {
        writeAttributes(ostream, "status"->obj.status);
    }
    if (!obj.traces) {
        writeAttributes(ostream, "id"->obj.id);
        if (obj.born >= 0) {
            // Write turn-of-birth if and only if it is fewer turns before the current
            // turn than this kind of animal's age of maturity.
            if (currentTurn >= 0, exists maturityAge = maturityModel.maturityAges[obj.kind],
                    maturityAge <= (currentTurn - obj.born)) {
                // do nothing
            } else {
                writeAttributes(ostream, "born"->obj.born);
            }
        }
        if (obj.population > 1) {
            writeAttributes(ostream, "count"->obj.population);
        }
    }
    writeImage(ostream, obj);
}

void writeSimpleImmortal(XMLStreamWriter ostream, SimpleImmortal obj, Integer indentation) {
    writeTag(ostream, obj.kind, indentation, true);
    writeAttributes(ostream, "id"->obj.id);
    writeImage(ostream, obj);
}
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

import strategicprimer.model.impl.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map {
    IPlayerCollection,
    HasPortrait
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    Worker,
    SimpleImmortal,
    immortalAnimals,
    Animal,
    maturityModel,
    AnimalImpl,
    AnimalTracks
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob,
    Job,
    WorkerStats,
    ISkill,
    Skill
}
import strategicprimer.model.common.xmlio {
    Warning
}
import strategicprimer.model.impl.xmlio.exceptions {
    UnwantedChildException,
    UnsupportedPropertyException,
    UnsupportedTagException
}
import lovelace.util.common {
    matchingValue
}
object unitMemberHandler extends FluidBase() {
    shared Worker readWorker(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "worker");
        expectAttributes(element, warner, "name", "race", "portrait", "id", "image");
        Worker retval = setImage(
            Worker(getAttribute(element, "name"),
                getAttribute(element, "race", "human"),
                getOrGenerateID(element, warner, idFactory)),
            element, warner);
        retval.portrait = getAttribute(element, "portrait", "");
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                switch (event.name.localPart.lowercased)
                case ("job") { retval.addJob(readJob(event, element.name, stream, players,
                    warner, idFactory)); }
                case ("stats") { retval.stats = readStats(event, element.name, stream,
                    players, warner, idFactory); }
                else {
                    throw UnwantedChildException.listingExpectedTags(element.name, event,
                        ["job", "stats"]);
                }
            } else if (is EndElement event, element.name == event.name) {
                break;
            }
        }
        return retval;
    }

    shared IJob readJob(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "job");
        expectAttributes(element, warner, "name", "level");
        IJob retval = Job(getAttribute(element, "name"),
            getIntegerAttribute(element, "level"));
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                if ("skill" == event.name.localPart.lowercased) {
                    retval.addSkill(readSkill(event, element.name, stream, players,
                        warner, idFactory));
                } else {
                    throw UnwantedChildException.listingExpectedTags(element.name, event,
                        Singleton("skill"));
                }
            } else if (is EndElement event, element.name == event.name) {
                break;
            }
        }
        return retval;
    }

    shared ISkill readSkill(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "skill");
        expectAttributes(element, warner, "name", "level", "hours");
        requireNonEmptyAttribute(element, "name", true, warner);
        spinUntilEnd(element.name, stream);
        return Skill(getAttribute(element, "name"),
            getIntegerAttribute(element, "level"),
            getIntegerAttribute(element, "hours"));
    }

    shared WorkerStats readStats(StartElement element, QName parent, {XMLEvent*} stream,
            IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "stats");
        expectAttributes(element, warner, "hp", "max", "str", "dex", "con", "int",
            "wis", "cha");
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

    shared void writeWorker(XMLStreamWriter ostream, IWorker obj, Integer indentation) {
        WorkerStats? stats = obj.stats;
        {IJob*} jobs = obj.filter(matchingValue(false, IJob.emptyJob));
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

    shared void writeStats(XMLStreamWriter ostream, WorkerStats obj,
            Integer indentation) {
        writeTag(ostream, "stats", indentation, true);
        writeAttributes(ostream, "hp"->obj.hitPoints, "max"->obj.maxHitPoints,
            "str"->obj.strength, "dex"->obj.dexterity, "con"->obj.constitution,
            "int"->obj.intelligence, "wis"->obj.wisdom, "cha"->obj.charisma);
    }

    shared void writeJob(XMLStreamWriter ostream, IJob obj, Integer indentation) {
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

    shared void writeSkill(XMLStreamWriter ostream, ISkill obj, Integer indentation) {
        if (!obj.empty) {
            writeTag(ostream, "skill", indentation, true);
            writeAttributes(ostream, "name"->obj.name, "level"->obj.level,
                "hours"->obj.hours);
        }
    }

    shared Animal|AnimalTracks readAnimal(StartElement element, QName parent,
            {XMLEvent*} stream, IPlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        requireTag(element, parent, "animal", *immortalAnimals);
        String tag = element.name.localPart.lowercased;
        String kind;
        if (tag == "animal") {
            expectAttributes(element, warner, "traces", "id", "count", "kind", "talking",
                "status", "wild", "born", "image");
            kind = getAttribute(element, "kind");
        } else {
            warner.handle(UnsupportedTagException.future(element));
            expectAttributes(element, warner, "id", "count", "image");
            kind = tag;
        }
        spinUntilEnd(element.name, stream);
        // To get the intended meaning of existing maps, we have to parse
        // traces="" as traces="true". If compatibility with existing maps
        // ever becomes unnecessary, I will change the default-value here to
        // simply `false`.
        Boolean traces = getBooleanAttribute(element, "traces",
            hasAttribute(element, "traces") && getAttribute(element, "traces", "").empty,
            warner);
        Boolean talking = getBooleanAttribute(element, "talking", false, warner);
        String status = getAttribute(element, "status", "wild");
        Integer born = getIntegerAttribute(element, "born", -1, warner);
        // TODO: We'd like the default to be 1 inside a unit and -1 outside
        Integer count = getIntegerAttribute(element, "count", 1, warner);
        Integer id;
        if (traces) {
            if (hasAttribute(element, "id")) {
                warner.handle(UnsupportedPropertyException.inContext(element, "id",
                    """when tracks="true""""));
            }
            if (talking) {
                warner.handle(UnsupportedPropertyException.inContext(element, "talking",
                    """when tracks="true""""));
            }
            if (status != "wild") {
                warner.handle(UnsupportedPropertyException.inContext(element, "status",
                    """when tracks="true""""));
            }
            if (born != -1) {
                warner.handle(UnsupportedPropertyException.inContext(element, "born",
                    """when tracks="true""""));
            }
            if (count != 1) {
                warner.handle(UnsupportedPropertyException.inContext(element, "count",
                    """when tracks="true""""));
            }
            return setImage(AnimalTracks(kind), element, warner);
        } else {
            id = getOrGenerateID(element, warner, idFactory);
            return setImage(
                AnimalImpl(kind, talking, status, id, born, count), element, warner);
        }
    }
    shared void writeAnimalTracks(XMLStreamWriter ostream, AnimalTracks obj,
            Integer indentation) {
        writeTag(ostream, "animal", indentation, true);
        writeAttributes(ostream, "kind"->obj.kind, "traces"->true);
        writeImage(ostream, obj);
    }
    shared void writeAnimal(XMLStreamWriter ostream, Animal obj, Integer indentation) {
        writeTag(ostream, "animal", indentation, true);
        writeAttributes(ostream, "kind"->obj.kind);
        if (obj.talking) {
            writeAttributes(ostream, "talking"->true);
        }
        if ("wild" != obj.status) {
            writeAttributes(ostream, "status"->obj.status);
        }
        writeAttributes(ostream, "id"->obj.id);
        if (obj.born >= 0) {
            // Write turn-of-birth if and only if it is fewer turns before the current
            // turn than this kind of animal's age of maturity.
            if (currentTurn >= 0, exists maturityAge =
                    maturityModel.maturityAges[obj.kind],
                    maturityAge <= (currentTurn - obj.born)) {
                // do nothing
            } else {
                writeAttributes(ostream, "born"->obj.born);
            }
        }
        if (obj.population > 1) {
            writeAttributes(ostream, "count"->obj.population);
        }
        writeImage(ostream, obj);
    }

    shared void writeSimpleImmortal(XMLStreamWriter ostream, SimpleImmortal obj,
            Integer indentation) {
        writeTag(ostream, obj.kind, indentation, true);
        writeAttributes(ostream, "id"->obj.id);
        writeImage(ostream, obj);
    }
}
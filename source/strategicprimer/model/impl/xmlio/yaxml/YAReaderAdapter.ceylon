import ceylon.language.meta {
    classDeclaration,
    type
}
import ceylon.logging {
    logger,
    Logger
}

import java.io {
    IOException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.impl.idreg {
    IDRegistrar,
    IDFactory
}
import strategicprimer.model.common.map {
    River,
    IMutablePlayerCollection,
    PlayerCollection
}
import strategicprimer.model.common.map.fixtures.mobile {
    ProxyFor,
    immortalAnimals
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob,
    ISkill
}
import strategicprimer.model.common.xmlio {
    Warning,
    warningLevels,
    SPFormatException
}
import strategicprimer.model.impl.xmlio.exceptions {
    UnwantedChildException
}
import strategicprimer.model.common.map.fixtures.towns {
    CommunityStats
}
import ceylon.collection {
    HashMap,
    MutableMap
}
import ceylon.language.meta.model {
    ClassOrInterface
}
"A logger."
Logger log = logger(`module strategicprimer.model.impl`);
"A class to hide the complexity of YAXML from callers."
class YAReaderAdapter(
        "The Warning instance to use" Warning warning = warningLevels.default,
        "The factory for ID numbers" IDRegistrar idFactory = IDFactory()) {
    "The player collection to use."
    IMutablePlayerCollection players = PlayerCollection();
    "The map reader"
    YAMapReader mapReader = YAMapReader(warning, idFactory, players);
    "The reader for towns, etc."
    YATownReader townReader = YATownReader(warning, idFactory, players);
    "The set of readers."
    value readers = [ YAAdventureReader(warning, idFactory, players),
        YAExplorableReader(warning, idFactory), YAGroundReader(warning, idFactory),
        YAImplementReader(warning, idFactory), mapReader,
        YAMobileReader(warning, idFactory), YAPlayerReader(warning, idFactory),
        YAPortalReader(warning, idFactory), YAResourcePileReader(warning, idFactory),
        YAResourceReader(warning, idFactory), YATerrainReader(warning, idFactory),
        YATextReader(warning, idFactory), townReader,
        YAUnitReader(warning, idFactory, players), YAWorkerReader(warning, idFactory) ];
    MutableMap<String, YAAbstractReader<out Object>> readerCache =
            HashMap<String, YAAbstractReader<out Object>>();
    MutableMap<ClassOrInterface<Object>, YAAbstractReader<out Object>> writerCache =
            HashMap<ClassOrInterface<Object>, YAAbstractReader<out Object>>();
    "Parse an object from XML."
    throws(`class SPFormatException`, "on SP format problems")
    shared Object parse(StartElement element, QName parent, {XMLEvent*} stream) {
        // Since all implementations of necessity check the tag's namespace, we leave that
        // to them.
        String tag = element.name.localPart.lowercased;
        // Handle rivers specially.
        if ("river" == tag || "lake" == tag) {
            return mapReader.parseRiver(element, parent);
        }
        // Handle "population" specially.
        if ("population" == tag) {
            return townReader.parseCommunityStats(element, parent, stream);
        }
        if (exists reader = readerCache[tag]) {
            return reader.read(element, parent, stream);
        }
        for (reader in readers) {
            if (reader.isSupportedTag(tag)) {
                readerCache[tag] = reader;
                return reader.read(element, parent, stream);
            }
        } else {
            if (immortalAnimals.contains(tag)) {
                if (exists reader = readerCache["animal"]) {
                    return reader.read(element, parent, stream);
                } else {
                    for (reader in readers) {
                        if (reader.isSupportedTag("animal")) {
                            return reader.read(element, parent, stream);
                        }
                    }
                }
            }
            throw UnwantedChildException(parent, element);
        }
    }
    "Write a series of rivers."
    todo("Test this")
    throws(`class IOException`, "on I/O error")
    void writeAllRivers(Anything(String) ostream, {River*} rivers, Integer indent) {
        for (river in sort(rivers)) {
            mapReader.writeRiver(ostream, river, indent);
        }
    }
    "Write an object to XML."
    todo("Improve test coverage")
    throws(`class IOException`, "on I/O error")
    shared void write("The stream to write to" Anything(String) ostream,
            "The object to write" Object obj,
            "The current indentation level" Integer indent) {
        value cls = type(obj);
        if (is River obj) {
            mapReader.writeRiver(ostream, obj, indent);
        } else if (is {River*} obj) {
            writeAllRivers(ostream, obj, indent);
        } else if (is ProxyFor<out Anything> obj) {
            "To write a proxy object, it has to be proxying for at least one object."
            assert (exists proxied = obj.proxied.first);
            // TODO: Handle proxies in their respective types
            log.error("Wanted to write a proxy",
                AssertionError("Shouldn't try to write proxy objects"));
            write(ostream, proxied, indent);
            return;
        } else if (is IJob obj) {
            YAWorkerReader.writeJob(ostream, obj, indent);
        } else if (is ISkill obj) {
            YAWorkerReader.writeSkill(ostream, obj, indent);
        } else if (is CommunityStats obj) {
            townReader.writeCommunityStats(ostream, obj, indent);
        } else if (exists writer = writerCache[cls]) {
            writer.writeRaw(ostream, obj, indent);
        } else {
            for (reader in readers) {
                if (reader.canWrite(obj)) {
                    writerCache[cls] = reader;
                    reader.writeRaw(ostream, obj, indent);
                    return;
                }
            } else {
                throw AssertionError("After checking ``readers
                    .size`` readers, don't know how to write a ``classDeclaration(obj)
                    .name``");
            }
        }
    }
}

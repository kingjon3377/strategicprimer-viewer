import ceylon.interop.java {
    CeylonIterable
}
import ceylon.language.meta {
    type
}
import ceylon.logging {
    logger,
    Logger
}

import controller.map.formatexceptions {
    SPFormatException,
    UnwantedChildException
}
import controller.map.misc {
    IDRegistrar,
    IDFactory
}
import controller.map.yaxml {
    YAReader,
    YAImplementReader,
    YAMobileReader,
    YAResourcePileReader,
    YAResourceReader,
    YATerrainReader,
    YATextReader,
    YAWorkerReader
}

import java.io {
    IOException
}
import java.lang {
    JIterable=Iterable,
    JAppendable=Appendable,
    IllegalArgumentException,
    IllegalStateException
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

import model.map {
    IMutablePlayerCollection,
    PlayerCollection,
    River
}
import model.map.fixtures {
    RiverFixture
}
import model.map.fixtures.mobile {
    ProxyFor
}
import model.map.fixtures.mobile.worker {
    IJob,
    ISkill
}

import util {
    Warning
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A class to hide the complexity of YAXML from callers."
class YAReaderAdapter(
        "The Warning instance to use" Warning warning = Warning.default,
        "The factory for ID numbers" IDRegistrar idFactory = IDFactory()) {
    "The player collection to use."
    IMutablePlayerCollection players = PlayerCollection();
    "The map reader"
    YAMapReader mapReader = YAMapReader(warning, idFactory, players);
    "The set of readers."
    {YAReader<out Object>*} readers = { YAAdventureReader(warning, idFactory, players),
        YAExplorableReader(warning, idFactory), YAGroundReader(warning, idFactory),
        YAImplementReader(warning, idFactory), mapReader,
        YAMobileReader(warning, idFactory), YAPlayerReader(warning, idFactory),
        YAPortalReader(warning, idFactory), YAResourcePileReader(warning, idFactory),
        YAResourceReader(warning, idFactory), YATerrainReader(warning, idFactory),
        YATextReader(warning, idFactory), YATownReader(warning, idFactory, players),
        YAUnitReader(warning, idFactory, players), YAWorkerReader(warning, idFactory) };
    "Parse an object from XML."
    throws(`class SPFormatException`, "on SP format problems")
    todo("Use Ceylon Iterable instead")
    shared Object parse(StartElement element, QName parent, JIterable<XMLEvent> stream) {
        // Since all implementations of necessity check the tag's namespace, we leave that
        // to them.
        String tag = element.name.localPart;
        // Handle rivers specially.
        if ("river" == tag || "lake" == tag) {
            return mapReader.parseRiver(element, parent);
        }
        for (reader in readers) {
            if (reader.isSupportedTag(tag)) {
                return reader.read(element, parent, stream);
            }
        } else {
            throw UnwantedChildException(parent, element);
        }
    }
    "Write a series of rivers."
    todo("Test this")
    throws(`class IOException`, "on I/O error")
    void writeAllRivers(JAppendable ostream, {River*} rivers, Integer indent) {
        for (river in rivers) {
            mapReader.writeRiver(ostream, river, indent);
        }
    }
    "Write an object to XML."
    todo("Improve test coverage")
    throws(`class IOException`, "on I/O error")
    shared void write("The stream to write to" JAppendable ostream,
            "The object to write" Object obj,
            "The current indentation level" Integer indent) {
        if (is River obj) {
            mapReader.writeRiver(ostream, obj, indent);
        } else if (is RiverFixture obj) {
            writeAllRivers(ostream, CeylonIterable(obj), indent);
        } else if (is ProxyFor<out Anything> obj) {
            // TODO: Handle proxies in their respective types
            value iter = obj.proxied.iterator();
            if (iter.hasNext()) {
                assert (exists proxied = iter.next());
                log.error("Wanted to write a proxy",
                    IllegalArgumentException("Wanted to write a proxy object"));
                write(ostream, proxied, indent);
                return;
            } else {
                throw IllegalStateException(
                    "Don't know how to write a proxy not proxying any objects");
            }
        } else if (is IJob obj) {
            YAWorkerReader.writeJob(ostream, obj, indent);
        } else if (is ISkill obj) {
            YAWorkerReader.writeSkill(ostream, obj, indent);
        } else {
            for (reader in readers) {
                if (reader.canWrite(obj)) {
                    reader.writeRaw(ostream, obj, indent);
                    return;
                }
            } else {
                throw IllegalArgumentException("After checking ``readers
                    .size`` readers, don't know how to write a ``type(obj).declaration
                    .name``");
            }
        }
    }
}
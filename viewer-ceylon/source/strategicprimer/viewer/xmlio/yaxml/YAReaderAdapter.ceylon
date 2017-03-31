import ceylon.language.meta {
    classDeclaration
}
import ceylon.logging {
    logger,
    Logger
}

import controller.map.formatexceptions {
    SPFormatException,
    UnwantedChildException
}

import java.io {
    IOException
}
import java.lang {
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
    River
}

import strategicprimer.viewer.model {
    IDFactory,
    IDRegistrar
}
import strategicprimer.viewer.model.map {
    IMutablePlayerCollection,
    PlayerCollection
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    ProxyFor
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    IJob,
    ISkill
}
import strategicprimer.viewer.xmlio {
    Warning,
    warningLevels
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A class to hide the complexity of YAXML from callers."
class YAReaderAdapter(
        "The Warning instance to use" Warning warning = warningLevels.default,
        "The factory for ID numbers" IDRegistrar idFactory = IDFactory()) {
    "The player collection to use."
    IMutablePlayerCollection players = PlayerCollection();
    "The map reader"
    YAMapReader mapReader = YAMapReader(warning, idFactory, players);
    "The set of readers."
    value readers = { YAAdventureReader(warning, idFactory, players),
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
    shared Object parse(StartElement element, QName parent, {XMLEvent*} stream) {
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
        } else if (is {River*} obj) {
            writeAllRivers(ostream, obj, indent);
        } else if (is ProxyFor<out Anything> obj) {
            // TODO: Handle proxies in their respective types
            if (exists proxied = obj.proxied.first) {
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
                    .size`` readers, don't know how to write a ``classDeclaration(obj)
                    .name``");
            }
        }
    }
}
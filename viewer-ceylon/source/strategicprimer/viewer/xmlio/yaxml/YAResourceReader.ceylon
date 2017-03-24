import controller.map.formatexceptions {
    MissingPropertyException,
    DeprecatedPropertyException
}
import strategicprimer.viewer.model {
    IDRegistrar
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

import model.map.fixtures.resources {
    HarvestableFixture,
    Meadow,
    FieldStatus,
    Grove,
    CacheFixture,
    Mine,
    MineralVein,
    Shrub,
    StoneDeposit,
    StoneKind
}
import model.map.fixtures.towns {
    TownStatus
}

import util {
    Warning
}
"A reader for resource-bearing [[TileFixture]]s."
class YAResourceReader(Warning warner, IDRegistrar idRegistrar)
        extends YAAbstractReader<HarvestableFixture>(warner, idRegistrar) {
    {String*} supportedTags = set { "cache", "grove", "orchard", "field", "meadow", "mine",
        "mineral", "shrub", "stone"};
    HarvestableFixture createMeadow(StartElement element, Boolean field, Integer idNum) {
        requireNonEmptyParameter(element, "status", false);
        value cultivated = Boolean.parse(getParameter(element, "cultivated"));
        if (is Boolean cultivated) {
            return Meadow(getParameter(element, "kind"), field, cultivated, idNum, FieldStatus.parse(getParameter(element, "status", FieldStatus.random(idNum).string)));
        } else {
            throw MissingPropertyException(element, "cultivated", cultivated);
        }
    }
    Boolean isCultivated(StartElement element) {
        if (hasParameter(element, "cultivated")) {
            value cultivated = Boolean.parse(getParameter(element, "cultivated"));
            if (is Boolean cultivated) {
                return cultivated;
            } else {
                throw MissingPropertyException(element, "cultivated", cultivated);
            }
        } else if (hasParameter(element, "wild")) {
            value wild = Boolean.parse(getParameter(element, "wild"));
            if (is Boolean wild) {
                warner.warn(DeprecatedPropertyException(element, "wild", "cultivated"));
                return !wild;
            } else {
                throw MissingPropertyException(element, "cultivated", wild);
            }
        } else {
            throw MissingPropertyException(element, "cultivated");
        }
    }
    todo("Inline?")
    HarvestableFixture createGrove(StartElement element, Boolean orchard, Integer idNum)
            => Grove(orchard, isCultivated(element),
                getParamWithDeprecatedForm(element, "kind", "tree"), idNum);
    shared actual Boolean isSupportedTag(String tag) =>
            supportedTags.contains(tag.lowercased);
    shared actual HarvestableFixture read(StartElement element, QName parent,
            JIterable<XMLEvent> stream) {
        requireTag(element, parent, *supportedTags);
        Integer idNum = getOrGenerateID(element);
        HarvestableFixture retval;
        switch (element.name.localPart.lowercased)
        case ("cache") {
            retval = CacheFixture(getParameter(element, "kind"),
                getParameter(element, "contents"), idNum);
        }
        case ("field") { retval = createMeadow(element, true, idNum); }
        case ("grove") { retval = createGrove(element, false, idNum); }
        case ("meadow") { retval = createMeadow(element, false, idNum); }
        case ("mine") {
            retval = Mine(getParamWithDeprecatedForm(element, "kind", "product"),
                TownStatus.parseTownStatus(getParameter(element, "status")), idNum);
        }
        case ("mineral") {
            value exposed = Boolean.parse(getParameter(element, "exposed"));
            if (is Boolean exposed) {
                retval = MineralVein(
                    getParamWithDeprecatedForm(element, "kind", "mineral"), exposed,
                    getIntegerParameter(element, "dc"), idNum);
            } else {
                throw MissingPropertyException(element, "exposed", exposed);
            }
        }
        case ("orchard") { retval = createGrove(element, true, idNum); }
        case ("shrub") {
            retval = Shrub(getParamWithDeprecatedForm(element, "kind", "shrub"), idNum);
        }
        case ("stone") {
            retval = StoneDeposit(
                StoneKind.parseStoneKind(getParamWithDeprecatedForm(element, "kind",
                    "stone")), getIntegerParameter(element, "dc"), idNum);
        }
        else {
            throw IllegalArgumentException("Unhandled harvestable tag");
        }
        spinUntilEnd(element.name, stream);
        retval.setImage(getParameter(element, "image", ""));
        return retval;
    }
    shared actual void write(JAppendable ostream, HarvestableFixture obj, Integer indent) {
        if (is CacheFixture obj) {
            writeTag(ostream, "cache", indent);
            writeProperty(ostream, "kind", obj.kind);
            writeProperty(ostream, "contents", obj.contents);
        } else if (is Meadow obj) {
            writeTag(ostream, (obj.field) then "field" else "meadow", indent);
            writeProperty(ostream, "kind", obj.kind);
            writeProperty(ostream, "cultivated", obj.cultivated.string);
            writeProperty(ostream, "status", obj.status.string);
        } else if (is Grove obj) {
            writeTag(ostream, (obj.orchard) then "orchard" else "grove", indent);
            writeProperty(ostream, "cultivated", obj.cultivated.string);
            writeProperty(ostream, "kind", obj.kind);
        } else if (is Mine obj) {
            writeTag(ostream, "mine", indent);
            writeProperty(ostream, "kind", obj.kind);
            writeProperty(ostream, "status", obj.status.string);
        } else if (is MineralVein obj) {
            writeTag(ostream, "mineral", indent);
            writeProperty(ostream, "kind", obj.kind);
            writeProperty(ostream, "exposed", obj.exposed.string);
            writeProperty(ostream, "dc", obj.dc);
        } else if (is Shrub obj) {
            writeTag(ostream, "shrub", indent);
            writeProperty(ostream, "kind", obj.kind);
        } else if (is StoneDeposit obj) {
            writeTag(ostream, "stone", indent);
            writeProperty(ostream, "kind", obj.stone().string);
            writeProperty(ostream, "dc", obj.dc);
        } else {
            throw IllegalStateException("Unhandled HarvestableFixture subtype");
        }
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is HarvestableFixture;
}
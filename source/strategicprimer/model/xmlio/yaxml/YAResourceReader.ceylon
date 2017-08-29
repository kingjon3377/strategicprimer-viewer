import java.lang {
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

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map.fixtures.resources {
    HarvestableFixture,
    Meadow,
    CacheFixture,
    Mine,
    StoneDeposit,
    StoneKind,
    Shrub,
    MineralVein,
    Grove,
    FieldStatus
}
import strategicprimer.model.map.fixtures.towns {
    TownStatus
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    MissingPropertyException,
    DeprecatedPropertyException
}
"A reader for resource-bearing [[strategicprimer.model.map::TileFixture]]s."
class YAResourceReader(Warning warner, IDRegistrar idRegistrar)
        extends YAAbstractReader<HarvestableFixture>(warner, idRegistrar) {
    {String*} supportedTags = set { "cache", "grove", "orchard", "field", "meadow",
        "mine", "mineral", "shrub", "stone"};
    HarvestableFixture createMeadow(StartElement element, Boolean field, Integer idNum) {
        requireNonEmptyParameter(element, "status", false);
        value status = FieldStatus.parse(getParameter(element, "status",
            FieldStatus.random(idNum).string));
        if (is FieldStatus status) {
            return Meadow(getParameter(element, "kind"), field,
                getBooleanParameter(element, "cultivated"), idNum, status);
        } else {
            throw MissingPropertyException(element, "status", status);
        }
    }
    Boolean isCultivated(StartElement element) {
        if (hasParameter(element, "cultivated")) {
            return getBooleanParameter(element, "cultivated");
        } else if (hasParameter(element, "wild")) {
            warner.handle(DeprecatedPropertyException(element, "wild", "cultivated"));
            return !getBooleanParameter(element, "wild");
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
            {XMLEvent*} stream) {
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
            value status = TownStatus.parse(getParameter(element, "status"));
            if (is TownStatus status) {
                retval = Mine(getParamWithDeprecatedForm(element, "kind", "product"),
                    status, idNum);
            } else {
                throw MissingPropertyException(element, "status", status);
            }
        }
        case ("mineral") {
            retval = MineralVein(getParamWithDeprecatedForm(element, "kind", "mineral"),
                getBooleanParameter(element, "exposed"),
                getIntegerParameter(element, "dc"), idNum);
        }
        case ("orchard") { retval = createGrove(element, true, idNum); }
        case ("shrub") {
            retval = Shrub(getParamWithDeprecatedForm(element, "kind", "shrub"), idNum);
        }
        case ("stone") {
            value stone = StoneKind.parse(getParamWithDeprecatedForm(element, "kind",
                "stone"));
            if (is StoneKind stone) {
                retval = StoneDeposit(stone, getIntegerParameter(element, "dc"), idNum);
            } else {
                throw MissingPropertyException(element, "kind", stone);
            }
        }
        else {
            throw IllegalArgumentException("Unhandled harvestable tag");
        }
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual void write(Anything(String) ostream, HarvestableFixture obj,
            Integer indent) {
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
            writeProperty(ostream, "kind", obj.stone.string);
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
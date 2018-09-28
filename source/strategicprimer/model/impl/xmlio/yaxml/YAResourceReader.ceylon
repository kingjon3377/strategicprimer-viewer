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
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures.resources {
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
import strategicprimer.model.common.map.fixtures.towns {
    TownStatus
}
import strategicprimer.model.common.xmlio {
    Warning
}
import strategicprimer.model.impl.xmlio.exceptions {
    MissingPropertyException,
    DeprecatedPropertyException
}
"A reader for resource-bearing [[strategicprimer.model.common.map::TileFixture]]s."
class YAResourceReader(Warning warner, IDRegistrar idRegistrar)
        extends YAAbstractReader<HarvestableFixture>(warner, idRegistrar) {
    {String*} supportedTags = set { "cache", "grove", "orchard", "field", "meadow",
        "mine", "mineral", "shrub", "stone"};
    HarvestableFixture createMeadow(StartElement element, Boolean field, Integer idNum) {
        expectAttributes(element, "status", "kind", "id", "cultivated", "image", "acres");
        requireNonEmptyParameter(element, "status", false);
        value status = FieldStatus.parse(getParameter(element, "status",
            FieldStatus.random(idNum).string));
        if (is FieldStatus status) {
            return Meadow(getParameter(element, "kind"), field,
                getBooleanParameter(element, "cultivated"), idNum, status,
                getNumericParameter(element, "acres", -1));
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
    HarvestableFixture createGrove(StartElement element, Boolean orchard, Integer idNum) {
            expectAttributes(element, "kind", "tree", "cultivated", "wild", "id",
                "image", "count");
            return Grove(orchard, isCultivated(element),
                getParamWithDeprecatedForm(element, "kind", "tree"), idNum,
                getIntegerParameter(element, "count", -1));
    }
    shared actual Boolean isSupportedTag(String tag) =>
            supportedTags.contains(tag.lowercased);
    shared actual HarvestableFixture read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, *supportedTags);
        Integer idNum = getOrGenerateID(element);
        HarvestableFixture retval;
        switch (element.name.localPart.lowercased)
        case ("cache") {
            expectAttributes(element, "kind", "contents", "id", "image");
            retval = CacheFixture(getParameter(element, "kind"),
                getParameter(element, "contents"), idNum);
            // We want to transition from arbitrary-String 'contents' to sub-tags. As a
            // first step, future-proof *this* version of the suite by only firing a
            // warning if such children are detected, instead of aborting.
            spinUntilEnd(element.name, stream, ["resource", "implement"]);
            retval.image = getParameter(element, "image", "");
            return retval;
        }
        case ("field") { retval = createMeadow(element, true, idNum); }
        case ("grove") { retval = createGrove(element, false, idNum); }
        case ("meadow") { retval = createMeadow(element, false, idNum); }
        case ("mine") {
            expectAttributes(element, "status", "kind", "product", "id", "image");
            value status = TownStatus.parse(getParameter(element, "status"));
            if (is TownStatus status) {
                retval = Mine(getParamWithDeprecatedForm(element, "kind", "product"),
                    status, idNum);
            } else {
                throw MissingPropertyException(element, "status", status);
            }
        }
        case ("mineral") {
            expectAttributes(element, "kind", "mineral", "exposed", "id", "dc", "image");
            retval = MineralVein(getParamWithDeprecatedForm(element, "kind", "mineral"),
                getBooleanParameter(element, "exposed"),
                getIntegerParameter(element, "dc"), idNum);
        }
        case ("orchard") { retval = createGrove(element, true, idNum); }
        case ("shrub") {
            expectAttributes(element, "kind", "shrub", "id", "image", "count");
            retval = Shrub(getParamWithDeprecatedForm(element, "kind", "shrub"), idNum,
                getIntegerParameter(element, "count", -1));
        }
        case ("stone") {
            expectAttributes(element, "kind", "stone", "id", "dc", "image");
            value stone = StoneKind.parse(getParamWithDeprecatedForm(element, "kind",
                "stone"));
            if (is StoneKind stone) {
                retval = StoneDeposit(stone, getIntegerParameter(element, "dc"), idNum);
            } else {
                throw MissingPropertyException(element, "kind", stone);
            }
        }
        else {
            throw AssertionError("Unhandled harvestable tag");
        }
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual void write(Anything(String) ostream, HarvestableFixture obj,
            Integer indent) {
        assert (is CacheFixture|Meadow|Grove|Mine|MineralVein|Shrub|StoneDeposit obj);
        switch (obj)
        case (is CacheFixture) {
            writeTag(ostream, "cache", indent);
            writeProperty(ostream, "kind", obj.kind);
            writeProperty(ostream, "contents", obj.contents);
        }
        case (is Meadow) {
            writeTag(ostream, (obj.field) then "field" else "meadow", indent);
            writeProperty(ostream, "kind", obj.kind);
            writeProperty(ostream, "cultivated", obj.cultivated.string);
            writeProperty(ostream, "status", obj.status.string);
            if (obj.acres.positive) {
                writeProperty(ostream, "acres", obj.acres.string);
            }
        }
        case (is Grove) {
            writeTag(ostream, (obj.orchard) then "orchard" else "grove", indent);
            writeProperty(ostream, "cultivated", obj.cultivated.string);
            writeProperty(ostream, "kind", obj.kind);
            if (obj.population >= 1) {
                writeProperty(ostream, "count", obj.population);
            }
        }
        case (is Mine) {
            writeTag(ostream, "mine", indent);
            writeProperty(ostream, "kind", obj.kind);
            writeProperty(ostream, "status", obj.status.string);
        }
        case (is MineralVein) {
            writeTag(ostream, "mineral", indent);
            writeProperty(ostream, "kind", obj.kind);
            writeProperty(ostream, "exposed", obj.exposed.string);
            writeProperty(ostream, "dc", obj.dc);
        }
        case (is Shrub) {
            writeTag(ostream, "shrub", indent);
            writeProperty(ostream, "kind", obj.kind);
            if (obj.population >= 1) {
                writeProperty(ostream, "count", obj.population);
            }
        }
        case (is StoneDeposit) {
            writeTag(ostream, "stone", indent);
            writeProperty(ostream, "kind", obj.stone.string);
            writeProperty(ostream, "dc", obj.dc);
        }
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is HarvestableFixture;
}

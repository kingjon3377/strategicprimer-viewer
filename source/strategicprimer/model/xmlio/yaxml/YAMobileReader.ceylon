import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassOrInterface
}

import java.lang {
    IllegalArgumentException
}

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
    HasMutableImage,
    HasImage,
    HasKind
}
import strategicprimer.model.map.fixtures.mobile {
    Centaur,
    IUnit,
    SimpleImmortal,
    SimpleImmortalKind,
    Giant,
    Fairy,
    Dragon,
    Animal,
    MobileFixture
}
import strategicprimer.model.xmlio {
    Warning
}
"A reader for 'mobile fixtures'"
class YAMobileReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<MobileFixture>(warning, idRegistrar) {
    Map<ClassOrInterface<MobileFixture>, String> tagMap = map {
        `Animal`->"animal", `Centaur`->"centaur", `Dragon`->"dragon",
        `Fairy`->"fairy", `Giant`->"giant"
    };
    Set<String> supportedTags = set { *tagMap.items }.union(set {
        *{*`SimpleImmortalKind`.caseValues}
            .map((SimpleImmortalKind kind) => kind.tag)});
    MobileFixture createAnimal(StartElement element) {
        // To get the intended meaning of existing maps, we have to parse
        // traces="" as traces="true". If compatibility with existing maps
        // ever becomes unnecessary, I will change the default-value here to
        // simply `false`.
        Boolean tracks = getBooleanParameter(element, "traces",
            hasParameter(element, "traces") && getParameter(element, "traces", "").empty);
        Integer idNum;
        if (tracks && !hasParameter(element, "id")) {
            idNum = -1;
        } else {
            idNum = getOrGenerateID(element);
        }
        return Animal(getParameter(element, "kind"), tracks,
            getBooleanParameter(element, "talking", false),
            getParameter(element, "status", "wild"), idNum,
            getIntegerParameter(element, "born", -1));
    }
    MobileFixture readSimple(String tag, Integer idNum) {
        value kind = SimpleImmortalKind.parse(tag);
        if (is SimpleImmortalKind kind) {
            return SimpleImmortal(kind, idNum);
        } else {
            throw IllegalArgumentException("No simple immortal matches ``tag``", kind);
        }
    }
    shared actual Boolean isSupportedTag(String tag) =>
            supportedTags.contains(tag.lowercased);
    shared actual MobileFixture read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, *supportedTags);
        MobileFixture twoParam(MobileFixture(String, Integer) constr) =>
            constr(getParameter(element, "kind"), getOrGenerateID(element));
        MobileFixture retval;
        switch (type = element.name.localPart.lowercased)
        case ("animal") { retval = createAnimal(element); }
        case ("centaur") { retval = twoParam(Centaur); }
        case ("dragon") { retval = twoParam(Dragon); }
        case ("fairy") { retval = twoParam(Fairy); }
        case ("giant") { retval = twoParam(Giant); }
        else { retval = readSimple(type, getOrGenerateID(element)); }
        spinUntilEnd(element.name, stream);
        if (is HasMutableImage retval) {
            retval.image = getParameter(element, "image", "");
        }
        return retval;
    }
    shared actual void write(Anything(String) ostream, MobileFixture obj,
            Integer indent) {
        if (is IUnit obj) {
            throw IllegalArgumentException("Unit handled elsewhere");
        } else if (is Animal obj) {
            writeTag(ostream, "animal", indent);
            writeProperty(ostream, "kind", obj.kind);
            if (obj.traces) {
                writeProperty(ostream, "traces", "true");
            }
            if (obj.talking) {
                writeProperty(ostream, "talking", "true");
            }
            if ("wild" != obj.status) {
                writeProperty(ostream, "status", obj.status);
            }
            if (!obj.traces) {
                writeProperty(ostream, "id", obj.id);
                if (obj.born >= 0) {
                    // TODO: Write if, but only if, the 'born' turn is less than the
                    // animal kind's age of maturity before the current turn.
                    writeProperty(ostream, "born", obj.born);
                }
            }
            writeImageXML(ostream, obj);
        } else if (is SimpleImmortal obj) {
            writeTag(ostream, obj.kind, indent);
            writeProperty(ostream, "id", obj.id);
            writeImageXML(ostream, obj);
        } else if (exists tag = tagMap.get(type(obj))) {
            writeTag(ostream, tag, indent);
            if (is HasKind obj) {
                writeProperty(ostream, "kind", obj.kind);
            }
            writeProperty(ostream, "id", obj.id);
            if (is HasImage obj) {
                writeImageXML(ostream, obj);
            }
        } else {
            throw IllegalArgumentException("No tag for ``obj.shortDescription``");
        }
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is MobileFixture && !obj is IUnit;
}
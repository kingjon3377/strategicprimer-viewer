import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassOrInterface,
	Class
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
    Giant,
    Fairy,
    Dragon,
    Animal,
    MobileFixture,
    maturityModel,
	Sphinx,
	Djinn,
	Griffin,
	Minotaur,
	Ogre,
	Phoenix,
	Simurgh,
	Troll,
	AnimalImpl
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    MissingPropertyException
}
"A reader for 'mobile fixtures'"
class YAMobileReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<MobileFixture>(warning, idRegistrar) {
    Map<ClassOrInterface<MobileFixture>, String> tagMap = map {
        `Animal`->"animal", `Centaur`->"centaur", `Dragon`->"dragon",
        `Fairy`->"fairy", `Giant`->"giant", `Sphinx`->"sphinx",
        `Djinn`->"djinn", `Griffin`->"griffin", `Minotaur`->"minotaur",
        `Ogre`->"ogre", `Phoenix`->"phoenix", `Simurgh`->"simurgh",
        `Troll`->"troll"
    };
    Set<String> supportedTags = set { *tagMap.items };
    Map<String, Class<SimpleImmortal, [Integer]>> simples = map {
        "sphinx"->`Sphinx`,
        "djinn"->`Djinn`, "griffin"->`Griffin`, "minotaur"->`Minotaur`,
        "ogre"->`Ogre`, "phoenix"->`Phoenix`, "simurgh"->`Simurgh`,
        "troll"->`Troll` };
    MobileFixture createAnimal(StartElement element) {
        expectAttributes(element, "traces", "id", "count", "talking", "kind", "status", "wild", "born", "image");
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
        Integer count = getIntegerParameter(element, "count", 1);
        if (count < 1) { // FIXME: Remove this restriciton
            throw MissingPropertyException(element, "count",
                AssertionError("Animal population must be positive"));
        }
        return AnimalImpl(getParameter(element, "kind"), tracks,
            getBooleanParameter(element, "talking", false),
            getParameter(element, "status", "wild"), idNum,
            getIntegerParameter(element, "born", -1), count);
    }
    MobileFixture readSimple(String tag, Integer idNum) {
        "We have to have a reader for ``tag``"
        assert (exists cls = simples[tag]);
        return cls(idNum);
    }
    shared actual Boolean isSupportedTag(String tag) =>
            supportedTags.contains(tag.lowercased);
    shared actual MobileFixture read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, *supportedTags);
        MobileFixture twoParam(MobileFixture(String, Integer) constr) {
            expectAttributes(element, "id", "kind", "image");
                return constr(getParameter(element, "kind"), getOrGenerateID(element));
        }
        MobileFixture retval;
        switch (type = element.name.localPart.lowercased)
        case ("animal") { retval = createAnimal(element); }
        case ("centaur") { retval = twoParam(Centaur); }
        case ("dragon") { retval = twoParam(Dragon); }
        case ("fairy") { retval = twoParam(Fairy); }
        case ("giant") { retval = twoParam(Giant); }
        else {
            expectAttributes(element, "image", "id");
            retval = readSimple(type, getOrGenerateID(element));
        }
        spinUntilEnd(element.name, stream);
        if (is HasMutableImage retval) {
            retval.image = getParameter(element, "image", "");
        }
        return retval;
    }
    shared actual void write(Anything(String) ostream, MobileFixture obj,
            Integer indent) {
        if (is IUnit obj) {
            throw AssertionError("Unit handled elsewhere");
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
                    // Write turn-of-birth if and only if it is fewer turns before the current
                    // turn than this kind of animal's age of maturity.
                    if (exists maturityAge = maturityModel.maturityAges[obj.kind],
                            maturityAge <= (currentTurn - obj.born)) {
                        // do nothing
                    } else {
                        writeProperty(ostream, "born", obj.born);
                    }
                }
                if (obj.population > 1) {
                    writeProperty(ostream, "count", obj.population);
                }
            }
            writeImageXML(ostream, obj);
        } else if (is SimpleImmortal obj) {
            writeTag(ostream, obj.kind, indent);
            writeProperty(ostream, "id", obj.id);
            writeImageXML(ostream, obj);
        } else if (exists tag = tagMap[type(obj)]) {
            writeTag(ostream, tag, indent);
            if (is HasKind obj) {
                writeProperty(ostream, "kind", obj.kind);
            }
            writeProperty(ostream, "id", obj.id);
            if (is HasImage obj) {
                writeImageXML(ostream, obj);
            }
        } else {
            throw AssertionError("No tag for ``obj.shortDescription``");
        }
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is MobileFixture && !obj is IUnit;
}

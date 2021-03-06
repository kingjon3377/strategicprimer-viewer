import ceylon.collection {
    Stack,
    LinkedList
}

import java.io {
    JReader=Reader,
    FileNotFoundException
}
import java.nio.file {
    JPaths=Paths,
    JFiles=Files,
    NoSuchFileException
}

import javax.xml {
    XMLConstants
}
import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamException
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
    Characters,
    EndElement
}

import lovelace.util.common {
    IteratorWrapper,
    matchingValue,
    simpleMap,
    PathWrapper,
    MissingFileException,
    MalformedXMLException
}
import lovelace.util.jvm {
    TypesafeXMLEventReader
}

import strategicprimer.model.common.idreg {
    IDRegistrar,
    IDFactory
}
import strategicprimer.model.common.map {
    HasKind,
    Player,
    PlayerImpl,
    MapDimensions,
    MapDimensionsImpl,
    Point,
    TileType,
    River,
    IMutablePlayerCollection,
    TileFixture,
    PlayerCollection,
    IMutableMapNG,
    SPMapNG,
    Direction
}
import strategicprimer.model.common.map.fixtures {
    FortressMember,
    UnitMember,
    TextFixture
}
import strategicprimer.model.common.map.fixtures.mobile {
    IMutableUnit,
    IUnit,
    Unit,
    Giant,
    Centaur,
    Fairy,
    Dragon,
    Ogre,
    Troll,
    Sphinx,
    Phoenix,
    Griffin,
    Djinn,
    Simurgh,
    Minotaur,
    immortalAnimals,
    maturityModel,
    Snowbird,
    Thunderbird,
    Pegasus,
    Unicorn,
    Kraken,
    ImmortalAnimal
}
import strategicprimer.model.common.map.fixtures.terrain {
    Hill,
    Oasis
}
import strategicprimer.model.common.map.fixtures.towns {
    FortressImpl,
    IFortress,
    IMutableFortress,
    TownSize
}
import strategicprimer.model.impl.xmlio {
    IMapReader,
    ISPReader
}
import strategicprimer.model.common.xmlio {
    Warning,
    spNamespace,
    futureTags
}
import strategicprimer.model.impl.xmlio.exceptions {
    UnsupportedTagException,
    MissingChildException,
    UnwantedChildException,
    MissingPropertyException,
    MapVersionException
}
import strategicprimer.model.impl.xmlio.fluidxml {
    FluidBase { ... }
}

import java.nio.charset {
    StandardCharsets
}

"The main reader-from-XML class in the 'fluid XML' implementation."
shared class SPFluidReader() satisfies IMapReader&ISPReader {
    alias LocalXMLReader=>Object(StartElement, QName, {XMLEvent*},
        IMutablePlayerCollection, Warning, IDRegistrar);

    late Map<String, LocalXMLReader> readers;

    Object readSPObject(StartElement element, QName parent,
            {XMLEvent*} stream, IMutablePlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        String namespace = element.name.namespaceURI;
        String tag = element.name.localPart.lowercased;
        if (namespace.empty || namespace == spNamespace ||
                namespace == XMLConstants.nullNsUri) {
            if ("animal" == tag,
                        immortalAnimals.contains(getAttribute(element, "kind")),
                    !getBooleanAttribute(element, "traces", false)) {
                return setImage(ImmortalAnimal.parse(getAttribute(element, "kind"))
                    (getOrGenerateID(element, warner, idFactory)), element, warner);
            } else if (exists reader = readers[tag]) {
                return reader(element, parent, stream, players, warner, idFactory);
            }
        }
        throw UnsupportedTagException.future(element);
    }

    class SimpleFixtureReader(String tag, Object(Integer) factory) {
        shared Object reader(StartElement element, QName parent, {XMLEvent*} stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
            requireTag(element, parent, tag);
            expectAttributes(element, warner, "id", "image");
            spinUntilEnd(element.name, stream);
            return setImage(factory(getOrGenerateID(element, warner,
                idFactory)), element, warner);
        }
        shared String->LocalXMLReader entry => tag->reader;
    }

    class SimpleHasKindReader(String tag, HasKind(String, Integer) factory) {
        shared Object reader(StartElement element, QName parent, {XMLEvent*} stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
            requireTag(element, parent, tag);
            expectAttributes(element, warner, "id", "kind", "image");
            spinUntilEnd(element.name, stream);
            return setImage(factory(getAttribute(element, "kind"),
                getOrGenerateID(element, warner, idFactory)), element, warner);
        }
        shared String->LocalXMLReader entry => tag->reader;
    }

    StartElement firstStartElement({XMLEvent*} stream, StartElement parent) {
        if (exists element = stream.narrow<StartElement>().find(isSPStartElement)) {
            return element;
        } else {
            throw MissingChildException(parent);
        }
    }

    Boolean isFutureTag(StartElement tag, Warning warner) {
        if (futureTags.contains(tag.name.localPart.lowercased)) {
            warner.handle(UnsupportedTagException.future(tag));
            return true;
        } else {
            return false;
        }
    }

    void parseTileChild(IMutableMapNG map, StartElement parent,
            {XMLEvent*} stream, IMutablePlayerCollection players, Warning warner,
            IDRegistrar idFactory, Point currentTile, StartElement element) {
        String type = element.name.localPart.lowercased;
        if (isFutureTag(element, warner)) {
            return;
        } else if ("sandbar" == type) {
            warner.handle(UnsupportedTagException.obsolete(element));
            return;
        } else if ("tile" == type) {
            throw UnwantedChildException(parent.name, element);
        } else if ("mountain" == type) {
            map.mountainous[currentTile] = true;
            return;
        } else if ("bookmark" == type) {
            expectAttributes(element, warner, "player");
            map.addBookmark(currentTile, players.getPlayer(getIntegerAttribute(element, "player")));
            return;
        } else if ("road" == type) {
            expectAttributes(element, warner, "direction", "quality");
            if (is Direction direction = Direction.parse(getAttribute(element, "direction"))) {
                map.setRoadLevel(currentTile, direction, getIntegerAttribute(element, "quality"));
                return;
            } else {
                throw MissingPropertyException(element, "direction");
            }
        }
        Object child = readSPObject(element, parent.name, stream, players, warner,
            idFactory);
        if (is River child) {
            map.addRivers(currentTile, child);
        } else if (is TileFixture child) {
            if (is IFortress child, map.fixtures.get(currentTile).narrow<IFortress>()
                    .any(matchingValue(child.owner, IFortress.owner))) {
                warner.handle(UnwantedChildException.withMessage(parent.name, element,
                        "Multiple fortresses owned by same player on same tile"));
            }
            map.addFixture(currentTile, child);
        } else {
            throw UnwantedChildException(parent.name, element);
        }
    }

    void parseTile(IMutableMapNG map, StartElement element, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        expectAttributes(element, warner, "row", "column", "kind", "type");
        Point loc = Point(getIntegerAttribute(element, "row"),
            getIntegerAttribute(element, "column"));
        // Tiles have been known to be *written* without "kind" and then fail to load, so
        // let's be liberal in what we accept here, since we can.
        if ((hasAttribute(element, "kind") || hasAttribute(element, "type"))) {
            value kind = TileType.parse(getAttrWithDeprecatedForm(element, "kind",
                "type", warner));
            if (is TileType kind) {
                map.baseTerrain[loc] = kind;
            } else {
                warner.handle(MissingPropertyException(element, "kind", kind));
            }
        } else {
            warner.handle(MissingPropertyException(element, "kind"));
        }
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                parseTileChild(map, element, stream, players, warner, idFactory, loc,
                    event);
                continue;
            } else if (is EndElement event, element.name == event.name) {
                break;
            } else if (is Characters event) {
                String data = event.data.trimmed;
                if (!data.empty) {
                    map.addFixture(loc, TextFixture(data, -1));
                }
            }
        }
    }

    void parseElsewhere(IMutableMapNG map, StartElement element, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        expectAttributes(element, warner);
        Point loc = Point.invalidPoint;
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                parseTileChild(map, element, stream, players, warner, idFactory, loc,
                    event);
                continue;
            } else if (is EndElement event, element.name == event.name) {
                break;
            } else if (is Characters event) {
                String data = event.data.trimmed;
                if (!data.empty) {
                    map.addFixture(loc, TextFixture(data, -1));
                }
            }
        }
    }

    IMutableMapNG readMapOrViewTag(StartElement element, QName parent, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "map", "view");
        Integer currentTurn;
        StartElement mapTag;
        switch (element.name.localPart.lowercased)
        case ("view") {
            expectAttributes(element, warner, "current_player", "current_turn");
            currentTurn = getIntegerAttribute(element, "current_turn");
            maturityModel.currentTurn = currentTurn;
            mapTag = firstStartElement(stream, element);
            requireTag(mapTag, element.name, "map");
            expectAttributes(mapTag, warner, "version", "rows", "columns");
        }
        case ("map") {
            currentTurn = 0;
            mapTag = element;
            expectAttributes(mapTag, warner, "version", "rows", "columns",
                "current_player");
        }
        else {
            throw UnwantedChildException(parent, element);
        }
        MapDimensions dimensions;
        MapDimensions readDimensions = MapDimensionsImpl(
            getIntegerAttribute(mapTag, "rows"),
            getIntegerAttribute(mapTag, "columns"),
            getIntegerAttribute(mapTag, "version"));
        if (readDimensions.version != 2) {
            warner.handle(MapVersionException(mapTag, readDimensions.version, 2, 2));
            dimensions = MapDimensionsImpl(readDimensions.rows, readDimensions.columns, 2);
        } else {
            dimensions = readDimensions;
        }
        Stack<QName> tagStack = LinkedList<QName>();
        tagStack.push(element.name);
        tagStack.push(mapTag.name);
        IMutableMapNG retval = SPMapNG(dimensions, players, currentTurn);
        for (event in stream) {
            QName? stackTop = tagStack.top;
            if (is StartElement event, isSPStartElement(event)) {
                String type = event.name.localPart.lowercased;
                if ("row" == type) {
                    expectAttributes(event, warner, "index");
                    tagStack.push(event.name);
                    // Deliberately ignore
                    continue;
                } else if (isFutureTag(event, warner)) {
                    tagStack.push(event.name);
                    // Deliberately ignore
                    continue;
                } else if ("tile" == type) {
                    parseTile(retval, event, stream, players, warner, idFactory);
                    continue;
                } else if ("elsewhere" == type) {
                    parseElsewhere(retval, event, stream, players, warner, idFactory);
                    continue;
                }
                assert (exists stackTop);
                if (is Player player = readSPObject(event, stackTop, stream, players,
                        warner, idFactory)) {
                    retval.addPlayer(player);
                } else {
                    throw UnwantedChildException(mapTag.name, event);
                }
            } else if (is EndElement event) {
                if (exists stackTop, stackTop == event.name) {
                    tagStack.pop();
                }
                if (element.name == event.name) {
                    break;
                }
            } else if (is Characters event, !event.data.trimmed.empty) {
                assert (exists top = stackTop);
                warner.handle(UnwantedChildException.childInTag(top,
                    QName(XMLConstants.nullNsUri, "text"),
                    event.location,
                    AssertionError("Random text outside any tile")));
            }
        }
        if (hasAttribute(mapTag, "current_player")) {
            retval.currentPlayer = players.getPlayer(
                getIntegerAttribute(mapTag, "current_player"));
        } else if (hasAttribute(element, "current_player")) {
            retval.currentPlayer = players.getPlayer(
                getIntegerAttribute(element, "current_player"));
        } else {
            warner.handle(MissingPropertyException(mapTag, "current_player"));
        }
        retval.modified = false;
        return retval;
    }

    Player readPlayer(StartElement element, QName parent, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "player");
        requireNonEmptyAttribute(element, "number", true, warner);
        requireNonEmptyAttribute(element, "code_name", true, warner);
        String country = getAttribute(element, "country", "");
        expectAttributes(element, warner, "number", "code_name", "country", "portrait");
        // We're thinking about storing "standing orders" in the XML under the <player>
        // tag, and also possibly scientific progress; so as to not require players to
        // upgrade to even read their maps once we start doing so, we *now* only *warn*
        // instead of *dying* if the XML contains that idiom.
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                if (event.name.localPart.lowercased == "orders" ||
                            event.name.localPart.lowercased == "results" ||
                            event.name.localPart.lowercased == "science") {
                    warner.handle(UnwantedChildException(element.name, event));
                } else {
                    throw UnwantedChildException(element.name, event);
                }
            } else if (is EndElement event, element.name == event.name) {
                break;
            }
        }
        value retval = PlayerImpl(getIntegerAttribute(element, "number"),
            getAttribute(element, "code_name"),
            if (country.empty) then null else country);
        retval.portrait = getAttribute(element, "portrait", "");
        return retval;
    }

    void parseOrders(StartElement element, IMutableUnit unit, {XMLEvent*} stream,
            Warning warner) {
        expectAttributes(element, warner, "turn");
        Integer turn = getIntegerAttribute(element, "turn", -1, warner);
        unit.setOrders(turn, getTextUntil(element.name, stream));
    }

    void parseResults(StartElement element, IMutableUnit unit, {XMLEvent*} stream,
            Warning warner) {
        expectAttributes(element, warner, "turn");
        Integer turn = getIntegerAttribute(element, "turn", -1, warner);
        unit.setResults(turn, getTextUntil(element.name, stream));
    }

    IUnit readUnit(StartElement element, QName parent, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "unit");
        requireNonEmptyAttribute(element, "name", false, warner);
        requireNonEmptyAttribute(element, "owner", false, warner);
        expectAttributes(element, warner, "name", "owner", "id", "kind", "image",
            "portrait", "type");
        variable String? temp = null;
        try {
            temp = getAttrWithDeprecatedForm(element, "kind", "type", warner);
        } catch (MissingPropertyException except) {
            warner.handle(except);
        }
        String kind = temp else "";
        if (kind.empty) {
            warner.handle(MissingPropertyException(element, "kind"));
        }
        Unit retval = setImage(Unit(
            getPlayerOrIndependent(element, warner, players), kind,
            getAttribute(element, "name", ""),
            getOrGenerateID(element, warner, idFactory)), element, warner);
        retval.portrait = getAttribute(element, "portrait", "");
        StringBuilder orders = StringBuilder();
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                if ("orders" == event.name.localPart.lowercased) {
                    parseOrders(event, retval, stream, warner);
                    continue;
                } else if ("results" == event.name.localPart.lowercased) {
                    parseResults(event, retval, stream, warner);
                    continue;
                }
                if (is UnitMember child = readSPObject(event, element.name, stream,
                        players, warner, idFactory)) {
                    retval.addMember(child);
                } else {
                    throw UnwantedChildException(element.name, event);
                }
            } else if (is Characters event) {
                orders.append(event.data);
            } else if (is EndElement event, element.name == event.name) {
                break;
            }
        }
        String tempOrders = orders.string.trimmed;
        if (!tempOrders.empty) {
            retval.setOrders(-1, tempOrders);
        }
        return retval;
    }

    IFortress readFortress(StartElement element, QName parent, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "fortress");
        requireNonEmptyAttribute(element, "owner", false, warner);
        requireNonEmptyAttribute(element, "name", false, warner);
        expectAttributes(element, warner, "owner", "name", "id", "size", "status",
            "image", "portrait");
        IMutableFortress retval;
        value size = TownSize.parse(getAttribute(element, "size", "small"));
        if (is TownSize size) {
            retval = FortressImpl(
                getPlayerOrIndependent(element, warner, players),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory), size);
        } else {
            throw MissingPropertyException(element, "size", size);
        }
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                // We're thinking about storing per-fortress "standing orders" or general
                // regulations, building-progress results, and possibly scientific
                // research progress within fortresses. To ease the transition, we *now*
                // warn, instead of aborting, if the tags we expect to use for this
                // appear in this position in the XML.
                if (event.name.localPart == "orders" ||
                        event.name.localPart == "results" ||
                        event.name.localPart == "science") {
                    warner.handle(UnwantedChildException(element.name, event));
                    continue;
                }
                if (is FortressMember child = readSPObject(event, element.name, stream,
                        players, warner, idFactory)) {
                    retval.addMember(child);
                } else {
                    throw UnwantedChildException(element.name, event);
                }
            } else if (is EndElement event, element.name == event.name) {
                break;
            }
        }
        retval.portrait = getAttribute(element, "portrait", "");
        return setImage(retval, element, warner);
    }

    readers = simpleMap("adventure"->fluidExplorableHandler.readAdventure,
        "portal"->fluidExplorableHandler.readPortal,
        "cave"->fluidExplorableHandler.readCave,
        "battlefield"->fluidExplorableHandler.readBattlefield,
        "ground"->fluidTerrainHandler.readGround,
        "forest"->fluidTerrainHandler.readForest,
        SimpleFixtureReader("hill", Hill).entry,
        SimpleFixtureReader("oasis", Oasis).entry,
        "animal"->unitMemberHandler.readAnimal,
        SimpleHasKindReader("centaur", Centaur).entry,
        SimpleHasKindReader("dragon", Dragon).entry,
        SimpleHasKindReader("fairy", Fairy).entry,
        SimpleHasKindReader("giant", Giant).entry,
        "text"->fluidExplorableHandler.readTextFixture,
        "implement"->fluidResourceHandler.readImplement,
        "resource"->fluidResourceHandler.readResource,
        "cache"->fluidResourceHandler.readCache,
        "grove"->fluidResourceHandler.readGrove,
        "orchard"->fluidResourceHandler.readOrchard,
        "meadow"->fluidResourceHandler.readMeadow,
        "field"->fluidResourceHandler.readField,
        "mine"->fluidResourceHandler.readMine,
        "mineral"->fluidResourceHandler.readMineral,
        "shrub"->fluidResourceHandler.readShrub,
        "stone"->fluidResourceHandler.readStone,
        "worker"->unitMemberHandler.readWorker,
        "job"->unitMemberHandler.readJob,
        "skill"->unitMemberHandler.readSkill,
        "stats"->unitMemberHandler.readStats,
        "unit"->readUnit,
        "fortress"->readFortress,
        "town"->fluidTownHandler.readTown,
        "city"->fluidTownHandler.readCity,
        "fortification"->fluidTownHandler.readFortification,
        "village"->fluidTownHandler.readVillage,
        "map"->readMapOrViewTag,
        "view"->readMapOrViewTag,
        "river"->fluidTerrainHandler.readRiver,
        "lake"->fluidTerrainHandler.readLake,
        "player"->readPlayer,
        "population"->fluidTownHandler.readCommunityStats,
        SimpleFixtureReader("sphinx", `Sphinx`).entry,
        SimpleFixtureReader("djinn", `Djinn`).entry,
        SimpleFixtureReader("griffin", `Griffin`).entry,
        SimpleFixtureReader("minotaur", `Minotaur`).entry,
        SimpleFixtureReader("ogre", `Ogre`).entry,
        SimpleFixtureReader("phoenix", `Phoenix`).entry,
        SimpleFixtureReader("simurgh", `Simurgh`).entry,
        SimpleFixtureReader("troll", `Troll`).entry,
        SimpleFixtureReader("snowbird", `Snowbird`).entry,
        SimpleFixtureReader("thunderbird", `Thunderbird`).entry,
        SimpleFixtureReader("pegasus", `Pegasus`).entry,
        SimpleFixtureReader("unicorn", `Unicorn`).entry,
        SimpleFixtureReader("kraken", `Kraken`).entry);

    shared actual Type readXML<Type>(PathWrapper file, JReader istream, Warning warner)
            given Type satisfies Object {
        try (Iterator<XMLEvent>&Destroyable reader = TypesafeXMLEventReader(istream)) {
            {XMLEvent*} eventReader = IteratorWrapper(reader);
            IMutablePlayerCollection players = PlayerCollection();
            IDRegistrar idFactory = IDFactory();
            if (exists event = eventReader.narrow<StartElement>()
                    .find(isSPStartElement)) {
                assert (is Type retval = readSPObject(event, QName("root"), eventReader,
                    players, warner, idFactory));
                return retval;
            }
        } catch (XMLStreamException except) {
            throw MalformedXMLException(except);
        }
        throw MalformedXMLException.notWrapping(
            "XML stream didn't contain a start element");
    }

    shared actual IMutableMapNG readMap(PathWrapper file, Warning warner) {
        try (istream = JFiles.newBufferedReader(JPaths.get(file.string), StandardCharsets.utf8)) {
            return readMapFromStream(file, istream, warner);
        } catch (FileNotFoundException|NoSuchFileException except) {
            throw MissingFileException(file, except);
        }
    }
    shared actual IMutableMapNG readMapFromStream(PathWrapper file, JReader istream,
            Warning warner) => readXML<IMutableMapNG>(file, istream, warner);
}

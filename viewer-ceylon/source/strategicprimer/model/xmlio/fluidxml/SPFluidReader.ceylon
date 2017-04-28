import ceylon.collection {
    Stack,
    LinkedList
}

import java.io {
    JReader=Reader
}
import java.lang {
    IllegalStateException
}
import java.nio.file {
    JPath=Path,
    JFiles=Files
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
    IteratorWrapper
}
import lovelace.util.jvm {
    TypesafeXMLEventReader
}

import strategicprimer.model.idreg {
    IDRegistrar,
    IDFactory
}
import strategicprimer.model.map {
    Point,
    MapDimensions,
    HasKind,
    Player,
    IMutablePlayerCollection,
    TileType,
    IMutableMapNG,
    TileFixture,
    pointFactory,
    River,
    MapDimensionsImpl,
    SPMapNG,
    PlayerImpl,
    PlayerCollection
}
import strategicprimer.model.map.fixtures {
    FortressMember,
    UnitMember,
    TextFixture,
    Implement,
    Ground
}
import strategicprimer.model.map.fixtures.mobile {
    SimpleImmortalKind,
    IUnit,
    SimpleImmortal,
    Unit,
    Giant,
    Centaur,
    Fairy,
    Dragon
}
import strategicprimer.model.map.fixtures.terrain {
    Hill,
    Oasis,
    Sandbar,
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    Fortress,
    TownSize
}
import strategicprimer.model.xmlio {
    IMapReader,
    ISPReader,
    Warning,
    spNamespace,
    futureTags
}
import strategicprimer.model.xmlio.exceptions {
    UnsupportedTagException,
    MissingChildException,
    UnwantedChildException,
    MissingPropertyException
}
import strategicprimer.model.xmlio.io_impl {
    IncludingIterator
}
"The main reader-from-XML class in the 'fluid XML' implementation."
shared class SPFluidReader() satisfies IMapReader&ISPReader {
    alias LocalXMLReader=>Object(StartElement, QName, {XMLEvent*}, IMutablePlayerCollection, Warning, IDRegistrar);
    late Map<String, LocalXMLReader> readers;
    Object readSPObject(StartElement element, QName parent,
            {XMLEvent*} stream, IMutablePlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        String namespace = element.name.namespaceURI;
        String tag = element.name.localPart.lowercased;
        if (namespace.empty || namespace == spNamespace ||
                namespace == XMLConstants.nullNsUri) {
            if (exists reader = readers.get(tag)) {
                return reader(element, parent, stream, players, warner, idFactory);
            }
        }
        throw UnsupportedTagException(element);
    }
    String->LocalXMLReader simpleFixtureReader(String tag, Object(Integer) factory) {
        Object retval(StartElement element, QName parent, {XMLEvent*} stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
            requireTag(element, parent, tag);
            spinUntilEnd(element.name, stream);
            return setImage(factory(getOrGenerateID(element, warner,
                idFactory)), element, warner);
        }
        return tag->retval;
    }
    String->LocalXMLReader simpleHasKindReader(String tag,
            HasKind(String, Integer) factory) {
        Object retval(StartElement element, QName parent, {XMLEvent*} stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
            requireTag(element, parent, tag);
            spinUntilEnd(element.name, stream);
            return setImage(factory(getAttribute(element, "kind"),
                getOrGenerateID(element, warner, idFactory)), element, warner);
        }
        return tag->retval;
    }
    {<String->LocalXMLReader>*} simpleImmortalReaders = {
        for (kind in `SimpleImmortalKind`.caseValues)
            simpleFixtureReader(kind.tag, (id) => SimpleImmortal(kind, id))
    };
    StartElement firstStartElement({XMLEvent*} stream, StartElement parent) {
        for (element in stream) {
            if (is StartElement element, isSPStartElement(element)) {
                return element;
            }
        } else {
            throw MissingChildException(parent);
        }
    }
    Boolean isFutureTag(StartElement tag, Warning warner) {
        if (futureTags.contains(tag.name.localPart)) {
            warner.handle(UnsupportedTagException(tag));
            return true;
        } else {
            return false;
        }
    }
    void parseTileChild(IMutableMapNG map, StartElement parent,
            {XMLEvent*} stream, IMutablePlayerCollection players, Warning warner,
            IDRegistrar idFactory, Point currentTile, StartElement element) {
        String type = element.name.localPart;
        if (isFutureTag(element, warner)) {
            return;
        } else if ("tile" == type) {
            throw UnwantedChildException(parent.name, element);
        } else if ("mountain" == type) {
            map.setMountainous(currentTile, true);
            return;
        }
        Object child = readSPObject(element, parent.name, stream, players, warner, idFactory);
        if (is River child) {
            map.addRivers(currentTile, child);
        } else if (is Ground child) {
            if (exists oldGround = map.ground(currentTile)) {
                if (child.exposed, !oldGround.exposed) {
                    map.setGround(currentTile, child);
                    map.addFixture(currentTile, oldGround);
                } else if (oldGround != child) {
                    // TODO: Should we do additional ordering of Ground?
                    map.addFixture(currentTile, child);
                }
            } else {
                map.setGround(currentTile, child);
            }
        } else if (is Forest child) {
            if (exists oldForest = map.forest(currentTile)) {
                if (oldForest != child) {
                    // TODO: Should we do additional ordering of Forests?
                    map.addFixture(currentTile, child);
                }
            } else {
                map.setForest(currentTile, child);
            }
        } else if (is {River*} child) {
            map.addRivers(currentTile, *child);
        } else if (is TileFixture child) {
            map.addFixture(currentTile, child);
        } else {
            throw UnwantedChildException(parent.name, element);
        }
    }
    void parseTile(IMutableMapNG map, StartElement element, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        Point loc = pointFactory(getIntegerAttribute(element, "row"),
            getIntegerAttribute(element, "column"));
        // Tiles have been known to be *written* without "kind" and then fail to load, so
        // let's be liberal in what we accept here, since we can.
        if ((hasAttribute(element, "kind") || hasAttribute(element, "type"))) {
            value kind = TileType.parse(getAttrWithDeprecatedForm(element, "kind",
                "type", warner));
            if (is TileType kind) {
                map.setBaseTerrain(loc, kind);
            } else {
                warner.handle(MissingPropertyException(element, "kind", kind));
            }
        } else {
            warner.handle(MissingPropertyException(element, "kind"));
        }
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                parseTileChild(map, element, stream, players, warner, idFactory, loc, event);
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
        String outerTag = element.name.localPart;
        if ("view" == outerTag.lowercased) {
            currentTurn = getIntegerAttribute(element, "current_turn");
            mapTag = firstStartElement(stream, element);
            requireTag(mapTag, element.name, "map");
        } else if ("map" == outerTag.lowercased) {
            currentTurn = 0;
            mapTag = element;
        } else {
            throw UnwantedChildException(parent, element);
        }
        MapDimensions dimensions = MapDimensionsImpl(
            getIntegerAttribute(mapTag, "rows"),
            getIntegerAttribute(mapTag, "columns"),
            getIntegerAttribute(mapTag, "version"));
        Stack<QName> tagStack = LinkedList<QName>();
        tagStack.push(element.name);
        tagStack.push(mapTag.name);
        IMutableMapNG retval = SPMapNG(dimensions, players, currentTurn);
        for (event in stream) {
            QName? stackTop = tagStack.top;
            if (is StartElement event, isSPStartElement(event)) {
                String type = event.name.localPart;
                if ("row" == type || isFutureTag(event, warner)) {
                    tagStack.push(event.name);
                    // Deliberately ignore
                    continue;
                } else if ("tile" == type) {
                    parseTile(retval, event, stream, players, warner, idFactory);
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
                    IllegalStateException("Random text outside any tile")));
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
        return retval;
    }
    Player readPlayer(StartElement element, QName parent, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "player");
        requireNonEmptyAttribute(element, "number", true, warner);
        requireNonEmptyAttribute(element, "code_name", true, warner);
        spinUntilEnd(element.name, stream);
        return PlayerImpl(getIntegerAttribute(element, "number"),
            getAttribute(element, "code_name"));
    }
    void parseOrders(StartElement element, IUnit unit, {XMLEvent*} stream) {
        Integer turn = getIntegerAttribute(element, "turn", -1);
        unit.setOrders(turn, getTextUntil(element.name, stream));
    }
    void parseResults(StartElement element, IUnit unit, {XMLEvent*} stream) {
        Integer turn = getIntegerAttribute(element, "turn", -1);
        unit.setResults(turn, getTextUntil(element.name, stream));
    }
    IUnit readUnit(StartElement element, QName parent, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "unit");
        requireNonEmptyAttribute(element, "name", false, warner);
        requireNonEmptyAttribute(element, "owner", false, warner);
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
                    parseOrders(event, retval, stream);
                    continue;
                } else if ("results" == event.name.localPart.lowercased) {
                    parseResults(event, retval, stream);
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
    Fortress readFortress(StartElement element, QName parent, {XMLEvent*} stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        requireTag(element, parent, "fortress");
        requireNonEmptyAttribute(element, "owner", false, warner);
        requireNonEmptyAttribute(element, "name", false, warner);
        Fortress retval;
        value size = TownSize.parse(getAttribute(element, "size", "small"));
        if (is TownSize size) {
            retval = Fortress(
                getPlayerOrIndependent(element, warner, players),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory), size);
        } else {
            throw MissingPropertyException(element, "size", size);
        }
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
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
    readers = map {
        "adventure"->readAdventure,
        "portal"->readPortal,
        "cave"->readCave,
        "battlefield"->readBattlefield,
        "ground"->readGround,
        "forest"->readForest,
        simpleFixtureReader("hill", Hill),
        simpleFixtureReader("oasis", Oasis),
        simpleFixtureReader("sandbar", Sandbar),
        "animal"->readAnimal,
        simpleHasKindReader("centaur", Centaur),
        simpleHasKindReader("dragon", Dragon),
        simpleHasKindReader("fairy", Fairy),
        simpleHasKindReader("giant", Giant),
        "text"->readTextFixture,
        simpleHasKindReader("implement", Implement),
        "resource"->readResource,
        "cache"->readCache,
        "grove"->readGrove,
        "orchard"->readOrchard,
        "meadow"->readMeadow,
        "field"->readField,
        "mine"->readMine,
        "mineral"->readMineral,
        "shrub"->readShrub,
        "stone"->readStone,
        "worker"->readWorker,
        "job"->readJob,
        "skill"->readSkill,
        "stats"->readStats,
        "unit"->readUnit,
        "fortress"->readFortress,
        "town"->readTown,
        "city"->readCity,
        "fortification"->readFortification,
        "village"->readVillage,
        "map"->((StartElement element, QName parent, {XMLEvent*} stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory)
            => readMapOrViewTag(element, parent, stream, players, warner, idFactory)),
        "view"->((StartElement element, QName parent, {XMLEvent*} stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory)
            => readMapOrViewTag(element, parent, stream, players, warner, idFactory)),
        "river"->readRiver,
        "lake"->readLake,
        "player"->readPlayer,
        "population"->readCommunityStats,
        *simpleImmortalReaders
    };
    shared actual Type readXML<Type>(JPath file, JReader istream, Warning warner)
            given Type satisfies Object {
        Iterator<XMLEvent> reader = TypesafeXMLEventReader(istream);
        {XMLEvent*} eventReader = IteratorWrapper(IncludingIterator(file, reader));
        IMutablePlayerCollection players = PlayerCollection();
        IDRegistrar idFactory = IDFactory();
        for (event in eventReader) {
            if (is StartElement event, isSPStartElement(event)) {
                Object retval = readSPObject(event, QName("root"), eventReader, players,
                    warner, idFactory);
                assert (is Type retval);
                return retval;
            }
        }
        throw XMLStreamException("XML stream didn't contain a start element");
    }
    shared actual IMutableMapNG readMap(JPath file, Warning warner) {
        try (istream = JFiles.newBufferedReader(file)) {
            return readMapFromStream(file, istream, warner);
        }
    }
    shared actual IMutableMapNG readMapFromStream(JPath file, JReader istream, Warning warner) =>
            readXML<IMutableMapNG>(file, istream, warner);
}
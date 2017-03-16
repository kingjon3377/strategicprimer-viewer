import ceylon.collection {
    Stack,
    LinkedList
}
import ceylon.interop.java {
    javaClass,
    CeylonIterable
}

import controller.map.fluidxml {
    FluidXMLReader,
    FluidExplorableHandler,
    FluidResourceHandler,
    XMLHelper,
    FluidTerrainHandler,
    FluidUnitMemberHandler,
    FluidTownHandler
}
import controller.map.formatexceptions {
    MissingPropertyException,
    MissingChildException,
    UnwantedChildException,
    UnsupportedTagException
}
import controller.map.iointerfaces {
    IMapReader,
    ISPReader
}
import controller.map.misc {
    IDRegistrar,
    TypesafeXMLEventReader,
    IncludingIterator,
    IDFactory
}

import java.io {
    JReader=Reader
}
import java.lang {
    JIterable=Iterable,
    JClass=Class,
    IllegalStateException
}
import java.nio.file {
    JPath=Path,
    JFiles=Files
}
import java.util {
    JIterator=Iterator
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

import model.map {
    IMutablePlayerCollection,
    HasKind,
    Player,
    PlayerImpl,
    PlayerCollection,
    IMutableMapNG,
    MapDimensions,
    MapDimensionsImpl,
    SPMapNG,
    Point,
    PointFactory,
    TileType,
    River,
    TileFixture
}
import model.map.fixtures {
    UnitMember,
    FortressMember,
    Implement,
    Ground,
    TextFixture,
    RiverFixture
}
import model.map.fixtures.mobile {
    SimpleImmortal,
    IUnit,
    Unit,
    Centaur,
    Dragon,
    Fairy,
    Giant
}
import model.map.fixtures.terrain {
    Hill,
    Oasis,
    Sandbar,
    Forest
}
import model.map.fixtures.towns {
    Fortress,
    TownSize
}

import util {
    Warning,
    IteratorWrapper
}
// FIXME: rename this file to match this class
"The main reader-from-XML class in the 'fluid XML' implementation."
shared class SPFluidReader() satisfies IMapReader&ISPReader&FluidXMLReader {
    alias LocalXMLReader=>Object(StartElement, QName, JIterable<XMLEvent>, IMutablePlayerCollection, Warning, IDRegistrar);
    late Map<String, LocalXMLReader> readers;
    shared actual Object readSPObject(StartElement element, QName parent,
            JIterable<XMLEvent> stream, IMutablePlayerCollection players, Warning warner,
            IDRegistrar idFactory) {
        String namespace = element.name.namespaceURI;
        String tag = element.name.localPart.lowercased;
        if (namespace.empty || namespace == ISPReader.namespace) {
            if (exists reader = readers.get(tag)) {
                return reader(element, parent, stream, players, warner, idFactory);
            }
        }
        throw UnsupportedTagException(element);
    }
    String->LocalXMLReader simpleFixtureReader(String tag, Object(Integer) factory) {
        Object retval(StartElement element, QName parent, JIterable<XMLEvent> stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
            XMLHelper.requireTag(element, parent, tag);
            XMLHelper.spinUntilEnd(element.name, stream);
            return XMLHelper.setImage(factory(XMLHelper.getOrGenerateID(element, warner,
                idFactory)), element, warner);
        }
        return tag->retval;
    }
    String->LocalXMLReader simpleHasKindReader(String tag,
            HasKind(String, Integer) factory) {
        Object retval(StartElement element, QName parent, JIterable<XMLEvent> stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
            XMLHelper.requireTag(element, parent, tag);
            XMLHelper.spinUntilEnd(element.name, stream);
            return XMLHelper.setImage(factory(XMLHelper.getAttribute(element, "kind"),
                XMLHelper.getOrGenerateID(element, warner, idFactory)), element, warner);
        }
        return tag->retval;
    }
    {<String->LocalXMLReader>*} simpleImmortalReaders = {
        for (kind in SimpleImmortal.SimpleImmortalKind.values())
            simpleFixtureReader(kind.tag, (id) => SimpleImmortal(kind, id))
    };
    StartElement firstStartElement({XMLEvent*} stream, StartElement parent) {
        for (element in stream) {
            if (is StartElement element, XMLHelper.isSPStartElement(element)) {
                return element;
            }
        } else {
            throw MissingChildException(parent);
        }
    }
    Boolean isFutureTag(StartElement tag, Warning warner) {
        if (CeylonIterable(ISPReader.future).contains(tag.name.localPart)) {
            warner.warn(UnsupportedTagException(tag));
            return true;
        } else {
            return false;
        }
    }
    void parseTileChild(IMutableMapNG map, StartElement parent,
            JIterable<XMLEvent> stream, IMutablePlayerCollection players, Warning warner,
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
            if (exists oldGround = map.getGround(currentTile)) {
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
            if (exists oldForest = map.getForest(currentTile)) {
                if (oldForest != child) {
                    // TODO: Should we do additional ordering of Forests?
                    map.addFixture(currentTile, child);
                }
            } else {
                map.setForest(currentTile, child);
            }
        } else if (is RiverFixture child) { // TODO: change to (is {River*} child)
            map.addRivers(currentTile, *CeylonIterable(child));
        } else if (is TileFixture child) {
            map.addFixture(currentTile, child);
        } else {
            throw UnwantedChildException(parent.name, element);
        }
    }
    void parseTile(IMutableMapNG map, StartElement element, JIterable<XMLEvent> stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        Point loc = PointFactory.point(XMLHelper.getIntegerAttribute(element, "row"),
            XMLHelper.getIntegerAttribute(element, "column"));
        // Tiles have been known to be *written* without "kind" and then fail to load, so
        // let's be liberal in what we accept here, since we can.
        if (XMLHelper.hasAttribute(element, "kind") || XMLHelper.hasAttribute(element, "type")) {
            map.setBaseTerrain(loc, TileType.getTileType(
                XMLHelper.getAttrWithDeprecatedForm(element,
                    "kind", "tile", warner)));
        } else {
            warner.warn(MissingPropertyException(element, "kind"));
        }
        for (event in stream) {
            if (is StartElement event, XMLHelper.isSPStartElement(event)) {
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
    IMutableMapNG readMapOrViewTag(StartElement element, QName parent, JIterable<XMLEvent> stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        XMLHelper.requireTag(element, parent, "map", "view");
        Integer currentTurn;
        StartElement mapTag;
        String outerTag = element.name.localPart;
        if ("view" == outerTag.lowercased) {
            currentTurn = XMLHelper.getIntegerAttribute(element, "current_turn");
            mapTag = firstStartElement(CeylonIterable(stream), element);
            XMLHelper.requireTag(mapTag, element.name, "map");
        } else if ("map" == outerTag.lowercased) {
            currentTurn = 0;
            mapTag = element;
        } else {
            throw UnwantedChildException(parent, element);
        }
        MapDimensions dimensions = MapDimensionsImpl(
            XMLHelper.getIntegerAttribute(mapTag, "rows"),
            XMLHelper.getIntegerAttribute(mapTag, "columns"),
            XMLHelper.getIntegerAttribute(mapTag, "version"));
        Stack<QName> tagStack = LinkedList<QName>();
        tagStack.push(element.name);
        tagStack.push(mapTag.name);
        IMutableMapNG retval = SPMapNG(dimensions, players, currentTurn);
        for (event in stream) {
            QName? stackTop = tagStack.top;
            if (is StartElement event, XMLHelper.isSPStartElement(event)) {
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
                warner.warn(UnwantedChildException(stackTop,
                    QName(XMLConstants.nullNsUri, "text"),
                    event.location,
                    IllegalStateException("Random text outside any tile")));
            }
        }
        if (XMLHelper.hasAttribute(mapTag, "current_player")) {
            retval.setCurrentPlayer(players.getPlayer(
                XMLHelper.getIntegerAttribute(mapTag, "current_player")));
        } else if (XMLHelper.hasAttribute(element, "current_player")) {
            retval.setCurrentPlayer(players.getPlayer(
                XMLHelper.getIntegerAttribute(element, "current_player")));
        }
        return retval;
    }
    Player readPlayer(StartElement element, QName parent, JIterable<XMLEvent> stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        XMLHelper.requireTag(element, parent, "player");
        XMLHelper.requireNonEmptyAttribute(element, "number", true, warner);
        XMLHelper.requireNonEmptyAttribute(element, "code_name", true, warner);
        XMLHelper.spinUntilEnd(element.name, stream);
        return PlayerImpl(XMLHelper.getIntegerAttribute(element, "number"),
            XMLHelper.getAttribute(element, "code_name"));
    }
    void parseOrders(StartElement element, IUnit unit, JIterable<XMLEvent> stream) {
        Integer turn = XMLHelper.getIntegerAttribute(element, "turn", -1);
        unit.setOrders(turn, XMLHelper.getTextUntil(element.name, stream));
    }
    void parseResults(StartElement element, IUnit unit, JIterable<XMLEvent> stream) {
        Integer turn = XMLHelper.getIntegerAttribute(element, "turn", -1);
        unit.setResults(turn, XMLHelper.getTextUntil(element.name, stream));
    }
    IUnit readUnit(StartElement element, QName parent, JIterable<XMLEvent> stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        XMLHelper.requireTag(element, parent, "unit");
        XMLHelper.requireNonEmptyAttribute(element, "name", false, warner);
        XMLHelper.requireNonEmptyAttribute(element, "owner", false, warner);
        variable String? temp = null;
        try {
            temp = XMLHelper.getAttrWithDeprecatedForm(element, "kind", "type", warner);
        } catch (MissingPropertyException except) {
            warner.warn(except);
        }
        String kind = temp else "";
        if (kind.empty) {
            warner.warn(MissingPropertyException(element, "kind"));
        }
        Unit retval = XMLHelper.setImage(Unit(
            XMLHelper.getPlayerOrIndependent(element, warner, players), kind,
            XMLHelper.getAttribute(element, "name", ""),
            XMLHelper.getOrGenerateID(element, warner, idFactory)), element, warner);
        retval.portrait = XMLHelper.getAttribute(element, "portrait", "");
        StringBuilder orders = StringBuilder();
        for (event in stream) {
            if (is StartElement event, XMLHelper.isSPStartElement(event)) {
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
    Fortress readFortress(StartElement element, QName parent, JIterable<XMLEvent> stream,
            IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) {
        XMLHelper.requireTag(element, parent, "fortress");
        XMLHelper.requireNonEmptyAttribute(element, "owner", false, warner);
        XMLHelper.requireNonEmptyAttribute(element, "name", false, warner);
        Fortress retval = Fortress(
            XMLHelper.getPlayerOrIndependent(element, warner, players),
            XMLHelper.getAttribute(element, "name", ""),
            XMLHelper.getOrGenerateID(element, warner, idFactory),
            TownSize.parseTownSize(XMLHelper.getAttribute(element, "size", "small")));
        for (event in stream) {
            if (is StartElement event, XMLHelper.isSPStartElement(event)) {
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
        retval.portrait = XMLHelper.getAttribute(element, "portrait", "");
        return XMLHelper.setImage(retval, element, warner);
    }
    readers = map {
        "adventure"->FluidExplorableHandler.readAdventure,
        "portal"->FluidExplorableHandler.readPortal,
        "cave"->FluidExplorableHandler.readCave,
        "battlefield"->FluidExplorableHandler.readBattlefield,
        "ground"->FluidTerrainHandler.readGround,
        "forest"->FluidTerrainHandler.readForest,
        simpleFixtureReader("hill", Hill),
        simpleFixtureReader("oasis", Oasis),
        simpleFixtureReader("sandbar", Sandbar),
        "animal"->FluidUnitMemberHandler.readAnimal,
        simpleHasKindReader("centaur", Centaur),
        simpleHasKindReader("dragon", Dragon),
        simpleHasKindReader("fairy", Fairy),
        simpleHasKindReader("giant", Giant),
        "text"->FluidExplorableHandler.readTextFixture,
        simpleHasKindReader("implement", Implement),
        "resource"->FluidResourceHandler.readResource,
        "cache"->FluidResourceHandler.readCache,
        "grove"->FluidResourceHandler.readGrove,
        "orchard"->FluidResourceHandler.readOrchard,
        "meadow"->FluidResourceHandler.readMeadow,
        "field"->FluidResourceHandler.readField,
        "mine"->FluidResourceHandler.readMine,
        "mineral"->FluidResourceHandler.readMineral,
        "shrub"->FluidResourceHandler.readShrub,
        "stone"->FluidResourceHandler.readStone,
        "worker"->FluidUnitMemberHandler.readWorker,
        "job"->FluidUnitMemberHandler.readJob,
        "skill"->FluidUnitMemberHandler.readSkill,
        "stats"->FluidUnitMemberHandler.readStats,
        "unit"->readUnit,
        "fortress"->readFortress,
        "town"->FluidTownHandler.readTown,
        "city"->FluidTownHandler.readCity,
        "fortification"->FluidTownHandler.readFortification,
        "village"->FluidTownHandler.readVillage,
        "map"->((StartElement element, QName parent, JIterable<XMLEvent> stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory)
            => readMapOrViewTag(element, parent, stream, players, warner, idFactory)),
        "view"->((StartElement element, QName parent, JIterable<XMLEvent> stream,
                IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory)
            => readMapOrViewTag(element, parent, stream, players, warner, idFactory)),
        "river"->FluidTerrainHandler.readRiver,
        "lake"->FluidTerrainHandler.readLake,
        "player"->readPlayer,
        *simpleImmortalReaders
    };
    shared actual Type readXML<Type>(JPath file, JReader istream, JClass<Type> type,
            Warning warner) given Type satisfies Object {
        JIterator<XMLEvent> reader = TypesafeXMLEventReader(istream);
        JIterable<XMLEvent> eventReader =
                IteratorWrapper(IncludingIterator(file, reader));
        IMutablePlayerCollection players = PlayerCollection();
        IDRegistrar idFactory = IDFactory();
        for (event in eventReader) {
            if (is StartElement event, XMLHelper.isSPStartElement(event)) {
                Object retval = readSPObject(event, QName("root"), eventReader, players,
                    warner, idFactory);
//                assert (is Type retval);
                return type.cast(retval);
//                return retval;
            }
        }
        throw XMLStreamException("XML stream didn't contain a start element");
    }
    shared actual IMutableMapNG readMap(JPath file, Warning warner) {
        try (istream = JFiles.newBufferedReader(file)) {
            return readMap(file, istream, warner);
        }
    }
    shared actual IMutableMapNG readMap(JPath file, JReader istream, Warning warner) =>
            readXML<IMutableMapNG>(file, istream, javaClass<IMutableMapNG>(), warner);
}
import ceylon.collection {
    Stack,
    LinkedList
}
import ceylon.interop.java {
    CeylonIterable
}
import ceylon.language.meta {
    type
}

import controller.map.formatexceptions {
    MissingChildException,
    UnwantedChildException,
    MissingPropertyException,
    UnsupportedTagException
}
import controller.map.iointerfaces {
    ISPReader
}
import controller.map.misc {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
    JAppendable=Appendable,
    IllegalArgumentException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
    EndElement,
    Characters
}

import model.map {
    IMutablePlayerCollection,
    IMapNG,
    Player,
    TileFixture,
    IMutableMapNG,
    Point,
    River,
    MapDimensions,
    MapDimensionsImpl,
    SPMapNG,
    PointFactory,
    TileType
}
import model.map.fixtures {
    Ground,
    RiverFixture,
    TextFixture
}
import model.map.fixtures.terrain {
    Forest
}

import util {
    Warning,
    LineEnd
}
"A reader for Strategic Primer maps."
class YAMapReader("The Warning instance to use" Warning warner,
        "The factory for ID numbers" IDRegistrar idRegistrar,
        "The map's collection of players" IMutablePlayerCollection players)
        extends YAAbstractReader<IMapNG>(warner, idRegistrar) {
    "The reader for players"
    YAReader<Player> playerReader = YAPlayerReader(warner, idRegistrar);
    "The readers we'll try sub-tags on"
    value readers = {YAMobileReader(warner, idRegistrar),
        YAResourceReader(warner, idRegistrar), YATerrainReader(warner, idRegistrar),
        YATextReader(warner, idRegistrar), YATownReader(warner, idRegistrar, players),
        YAGroundReader(warner, idRegistrar),
        YAAdventureReader(warner, idRegistrar, players),
        YAPortalReader(warner, idRegistrar), YAExplorableReader(warner, idRegistrar),
        YAUnitReader(warner, idRegistrar, players) };
    "Add a fixture to a point in a map, accounting for the special cases."
    void addFixture(IMutableMapNG map, Point point, TileFixture fixture) {
        if (is Ground fixture) {
            if (exists oldGround = map.getGround(point)) {
                if (fixture.exposed, !oldGround.exposed) {
                    map.setGround(point, fixture);
                    map.addFixture(point, oldGround);
                } else if (fixture != oldGround) {
                    map.addFixture(point, fixture);
                }
            } else {
                map.setGround(point, fixture);
            }
        } else if (is Forest fixture) {
            if (exists oldForest = map.getForest(point)) {
                if (oldForest != fixture) {
                    map.addFixture(point, fixture);
                }
            } else {
                map.setForest(point, fixture);
            }
        } else if (is RiverFixture fixture) {
            // We shouldn't get here, since our parser doesn't use them, but I don't want
            // to lose data if I change that and forget to update its callers
            map.addRivers(point, *fixture);
        } else {
            map.addFixture(point, fixture);
        }
    }
    "Get the first open-tag event in our namespace in the stream."
    StartElement getFirstStartElement(JIterable<XMLEvent> stream, StartElement parent) {
        for (element in stream) {
            if (is StartElement element, isSPStartElement(element)) {
                return element;
            }
        } else {
            throw MissingChildException(parent);
        }
    }
    "Write a newline if needed."
    void eolIfNeeded(Boolean needEol, JAppendable writer) {
        if (needEol) {
            writer.append(LineEnd.lineSep);
        }
    }
    "Parse a river from XML. The caller is now responsible for advancing the stream past
     the closing tag."
    shared River parseRiver(StartElement element, QName parent) {
        requireTag(element, parent, "river", "lake");
        if ("lake" == element.name.localPart.lowercased) {
            return River.lake;
        } else {
            requireNonEmptyParameter(element, "direction", true);
            return River.getRiver(getParameter(element, "direction"));
        }
    }
    "Write a river."
    shared void writeRiver(JAppendable ostream, River obj, Integer indent) {
        if (River.lake == obj) {
            writeTag(ostream, "lake", indent);
        } else {
            writeTag(ostream, "river", indent);
            writeProperty(ostream, "direction", obj.description);
        }
        closeLeafTag(ostream);
    }
    "Parse what should be a [[TileFixture]] from the XML."
    TileFixture parseFixture(StartElement element, QName parent,
            JIterable<XMLEvent> stream) {
        String name = element.name.localPart;
        for (reader in readers) {
            if (reader.isSupportedTag(name)) {
                return reader.read(element, parent, stream);
            }
        } else {
            throw UnwantedChildException(QName(element.name.namespaceURI, "tile"),
                element);
        }
    }
    "Read a map from XML."
    shared actual IMutableMapNG read(StartElement element, QName parent,
            JIterable<XMLEvent> stream) {
        requireTag(element, parent, "map", "view");
        Integer currentTurn;
        StartElement mapTag;
        String outerTag = element.name.localPart;
        if ("view" == outerTag.lowercased) {
            currentTurn = getIntegerParameter(element, "current_turn");
            mapTag = getFirstStartElement(stream, element);
            requireTag(mapTag, element.name, "map");
        } else if ("map" == outerTag.lowercased) {
            currentTurn = 0;
            mapTag = element;
        } else {
            throw UnwantedChildException(QName("xml"), element);
        }
        MapDimensions dimensions = MapDimensionsImpl(getIntegerParameter(mapTag, "rows"),
            getIntegerParameter(mapTag, "columns"),
            getIntegerParameter(mapTag, "version"));
        Stack<QName> tagStack = LinkedList<QName>();
        tagStack.push(element.name);
        tagStack.push(mapTag.name);
        IMutableMapNG retval = SPMapNG(dimensions, players, currentTurn);
        variable Point point = PointFactory.invalidPoint; // TODO: Use Point? instead?
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                String type = event.name.localPart.lowercased;
                if ("player" == type) {
                    assert (exists top = tagStack.top);
                    retval.addPlayer(playerReader.read(event, top, stream));
                } else if ("row" == type) {
                    tagStack.push(event.name);
                    // Deliberately ignore "row"
                    continue;
                } else if ("tile" == type) {
                    if (PointFactory.invalidPoint != point) {
                        throw UnwantedChildException(tagStack.top, event);
                    }
                    tagStack.push(event.name);
                    point = parsePoint(event);
                    // Since tiles have sometimes been *written* without "kind", then
                    // failed to load, be liberal in waht we accept here.
                    if (hasParameter(event, "kind") || hasParameter(event, "type")) {
                        retval.setBaseTerrain(point,
                            TileType.getTileType(
                                getParamWithDeprecatedForm(event, "kind", "type")));
                    } else {
                        warner.warn(MissingPropertyException(event, "kind"));
                    }
                } else if (CeylonIterable(ISPReader.future).contains(type)) {
                    tagStack.push(event.name);
                    warner.warn(UnsupportedTagException(event));
                } else if (PointFactory.invalidPoint == point) {
                    // fixture outside tile
                    throw UnwantedChildException(tagStack.top, event);
                } else if ("lake" == type || "river" == type) {
                    assert (exists top = tagStack.top);
                    retval.addRivers(point, parseRiver(event, top));
                    spinUntilEnd(event.name, stream);
                } else if ("mountain" == type) {
                    tagStack.push(event.name);
                    retval.setMountainous(point, true);
                } else {
                    assert (exists top = tagStack.top);
                    addFixture(retval, point, parseFixture(event, top, stream));
                }
            } else if (is EndElement event) {
                if (exists top = tagStack.top, top == event.name) {
                    tagStack.pop();
                }
                if (element.name == event.name) {
                    break;
                } else if ("tile" == event.name.localPart.lowercased) {
                    point = PointFactory.invalidPoint;
                }
            } else if (is Characters event) {
                String data = event.data.trimmed;
                if (!data.empty) {
                    retval.addFixture(point, TextFixture(data, -1));
                }
            }
        }
        if (hasParameter(mapTag, "current_player")) {
            retval.setCurrentPlayer(players.getPlayer(getIntegerParameter(mapTag,
                "current_player")));
        } else if (hasParameter(element, "current_player")) {
            retval.setCurrentPlayer(players.getPlayer(getIntegerParameter(element,
                "current_player")));
        }
        return retval;
    }
    "Write a child object"
    void writeChild(JAppendable ostream, TileFixture child, Integer tabs) {
        for (reader in readers) {
            if (reader.canWrite(child)) {
                reader.writeRaw(ostream, child, tabs);
                return;
            }
        } else {
            throw IllegalArgumentException("After checking ``readers
                .size`` readers, don't know how to write a ``type(child).declaration
                .name``");
        }
    }
    "Write a map."
    shared actual void write(JAppendable ostream, IMapNG obj, Integer tabs) {
        writeTag(ostream, "view", tabs);
        writeProperty(ostream, "current_player", obj.currentPlayer.playerId);
        writeProperty(ostream, "current_turn", obj.currentTurn);
        finishParentTag(ostream);
        writeTag(ostream, "map", tabs + 1);
        MapDimensions dimensions = obj.dimensions();
        writeProperty(ostream, "version", dimensions.version);
        writeProperty(ostream, "rows", dimensions.rows);
        writeProperty(ostream, "columns", dimensions.columns);
        finishParentTag(ostream);
        for (player in obj.players()) {
            playerReader.write(ostream, player, tabs + 2);
        }
        for (i in 0..(dimensions.rows)) {
            variable Boolean rowEmpty = true;
            for (j in 0..(dimensions.columns)) {
                Point loc = PointFactory.point(i, j);
                TileType terrain = obj.getBaseTerrain(loc);
                if (!obj.isLocationEmpty(loc)) {
                    if (rowEmpty) {
                        rowEmpty = false;
                        writeTag(ostream, "row", tabs + 2);
                        writeProperty(ostream, "index", i);
                        finishParentTag(ostream);
                    }
                    writeTag(ostream, "tile", tabs + 3);
                    writeProperty(ostream, "row", i);
                    writeProperty(ostream, "column", j);
                    if (TileType.notVisible != terrain) {
                        writeProperty(ostream, "kind", terrain.toXML());
                    }
                    ostream.append('>');
                    variable Boolean needEol = true;
                    if (obj.isMountainous(loc)) {
                        eolIfNeeded(true, ostream);
                        needEol = false;
                        writeTag(ostream, "mountain", tabs + 4);
                        closeLeafTag(ostream);
                    }
                    for (river in obj.getRivers(loc)) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeRiver(ostream, river, tabs + 4);
                    }
                    if (exists ground = obj.getGround(loc)) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeChild(ostream, ground, tabs + 4);
                    }
                    if (exists forest = obj.getForest(loc)) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeChild(ostream, forest, tabs + 4);
                    }
                    for (fixture in obj.getOtherFixtures(loc)) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeChild(ostream, fixture, tabs + 4);
                    }
                    if (!needEol) {
                        indent(ostream, tabs + 3);
                    }
                    closeTag(ostream, 0, "tile");
                }
            }
            if (!rowEmpty) {
                closeTag(ostream, tabs + 2, "row");
            }
        }
        closeTag(ostream, tabs + 1, "map");
        closeTag(ostream, tabs, "view");
    }
    shared actual Boolean isSupportedTag(String tag) =>
            {"map", "view"}.contains(tag.lowercased);
    shared actual Boolean canWrite(Object obj) => obj is IMapNG;
}
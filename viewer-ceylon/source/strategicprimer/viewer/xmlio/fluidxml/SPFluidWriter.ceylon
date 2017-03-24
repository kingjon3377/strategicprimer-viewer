import ceylon.interop.java {
    CeylonIterable,
    CeylonMap
}
import ceylon.language.meta.model {
    ClassOrInterface
}
import ceylon.regex {
    Regex,
    regex
}

import controller.map.iointerfaces {
    SPWriter,
    ISPReader
}

import java.io {
    StringWriter,
    IOException
}
import java.lang {
    IllegalArgumentException,
    JAppendable=Appendable,
    IllegalStateException
}
import java.nio.file {
    JPath=Path,
    JFiles=Files
}

import javax.xml.stream {
    XMLStreamWriter,
    XMLOutputFactory,
    XMLStreamException
}

import model.map {
    River,
    IMapNG,
    Player,
    HasKind,
    HasImage,
    IFixture,
    HasPortrait,
    MapDimensions,
    PointFactory,
    Point,
    TileType
}
import model.map.fixtures {
    RiverFixture,
    Ground,
    TextFixture,
    Implement,
    ResourcePile
}
import model.map.fixtures.explorable {
    AdventureFixture,
    Portal,
    Battlefield,
    Cave
}
import model.map.fixtures.mobile {
    Animal,
    Dragon,
    Fairy,
    Giant,
    SimpleImmortal,
    IWorker,
    IUnit,
    Centaur
}
import model.map.fixtures.mobile.worker {
    IJob,
    ISkill,
    WorkerStats
}
import model.map.fixtures.resources {
    CacheFixture,
    Meadow,
    Grove,
    Mine,
    MineralVein,
    Shrub,
    StoneDeposit
}
import model.map.fixtures.terrain {
    Forest,
    Hill,
    Oasis,
    Sandbar
}
import model.map.fixtures.towns {
    Fortress,
    TownSize
}

import strategicprimer.viewer.model.map.fixtures.towns {
    Village,
    AbstractTown
}
Regex snugEndTag = regex("([^ ])/>", true);
"""The main writer-to-XML class in the "fluid XML" implementation."""
shared class SPFluidWriter() satisfies SPWriter {
    alias LocalXMLWriter=>Anything(XMLStreamWriter, Object, Integer);
    late Map<ClassOrInterface<Anything>, LocalXMLWriter> writers;
    void writeSPObjectImpl(XMLStreamWriter ostream, Object obj, Integer indentation) {
        for (type in typeStream(obj)) {
            if (exists writer = writers.get(type)) {
                writer(ostream, obj, indentation);
                return;
            }
        }
    }
    shared actual void writeSPObject(JAppendable ostream, Object obj) {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        StringWriter writer = StringWriter();
        try {
            XMLStreamWriter xsw = xof.createXMLStreamWriter(writer);
            xsw.setDefaultNamespace(ISPReader.namespace);
            writeSPObjectImpl(xsw, obj, 0);
            xsw.writeCharacters("\n");
            xsw.writeEndDocument();
            xsw.flush();
            xsw.close();
        } catch (XMLStreamException except) {
            throw IOException("Failure in creating XML", except);
        }
        ostream.append(snugEndTag.replace(writer.string, "$1 />"));
    }
    shared actual void write(JPath file, IMapNG map) {
        try (writer = JFiles.newBufferedWriter(file)) {
            writeSPObject(writer, map);
        }
    }
    shared actual void write(JAppendable ostream, IMapNG map) =>
            writeSPObject(ostream, map);
    void writePlayer(XMLStreamWriter ostream, Object obj, Integer indentation) {
        if (is Player obj) {
            writeTag(ostream, "player", indentation, true);
            writeIntegerAttribute(ostream, "number", obj.playerId);
            writeAttribute(ostream, "code_name", obj.name);
        } else {
            throw IllegalArgumentException("Can only write Player");
        }
    }
    Entry<ClassOrInterface<Anything>, LocalXMLWriter> simpleFixtureWriter(
            ClassOrInterface<Anything> cls, String tag) {
        void retval(XMLStreamWriter ostream, Object obj, Integer indentation) {
            if (!cls.typeOf(obj)) {
                throw IllegalArgumentException("Can only write ``cls.declaration.name``");
            }
            if (is IFixture obj) {
                writeTag(ostream, tag, indentation, true);
                if (is HasKind obj) {
                    writeAttribute(ostream, "kind", obj.kind);
                }
                writeIntegerAttribute(ostream, "id", obj.id);
                if (is HasImage obj) {
                    writeImage(ostream, obj);
                }
            } else {
                throw IllegalStateException("Can only 'simply' write fixtures");
            }
        }
        return cls->retval;
    }
    void writeUnitOrders(XMLStreamWriter ostream, Integer indentation, Integer turn,
            String tag, String text) {
        // assert (tag == "orders" || tag == "results");
        if (text.empty) {
            return;
        }
        writeTag(ostream, tag, indentation, false);
        if (turn >= 0) {
            writeIntegerAttribute(ostream, "turn", turn);
        }
        ostream.writeCharacters(text);
        ostream.writeEndElement();
    }
    void writeUnit(XMLStreamWriter ostream, Object obj, Integer indentation) {
        if (is IUnit obj) {
            Boolean empty =
                    CeylonIterable(obj).empty && obj.allOrders.empty &&
                        !obj.allResults.empty;
            writeTag(ostream, "unit", indentation, empty);
            writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
            writeNonEmptyAttribute(ostream, "kind", obj.kind);
            writeNonEmptyAttribute(ostream, "name", obj.name);
            writeIntegerAttribute(ostream, "id", obj.id);
            writeImage(ostream, obj);
            if (is HasPortrait obj) {
                writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
            }
            for (turn->orders in CeylonMap(obj.allOrders)) {
                // FIXME: Drop superfluous .string, .intValue() when IUnit is ported
                writeUnitOrders(ostream, indentation + 1, turn.intValue(), "orders",
                    orders.string.trimmed);
            }
            for (turn->results in CeylonMap(obj.allResults)) {
                // FIXME: Drop superfluous .string, .intValue() when IUnit is ported
                writeUnitOrders(ostream, indentation + 1, turn.intValue(), "results",
                    results.string.trimmed);
            }
            for (member in obj) {
                writeSPObjectImpl(ostream, member, indentation + 1);
            }
            if (!empty) {
                indent(ostream, indentation);
                ostream.writeEndElement();
            }
        } else {
            throw IllegalArgumentException("Can only write IUnit");
        }
    }
    void writeFortress(XMLStreamWriter ostream, Object obj, Integer indentation) {
        if (is Fortress obj) {
            writeTag(ostream, "fortress", indentation, false);
            writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
            writeNonEmptyAttribute(ostream, "name", obj.name);
            if (TownSize.small != obj.size()) {
                writeAttribute(ostream, "size", obj.size().string);
            }
            writeIntegerAttribute(ostream, "size", obj.id);
            writeImage(ostream, obj);
            writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
            if (!CeylonIterable(obj).empty) {
                for (member in obj) {
                    writeSPObjectImpl(ostream, member, indentation + 1);
                }
                indent(ostream, indentation);
            }
            ostream.writeEndElement();
        } else {
            throw IllegalArgumentException("Can only write Fortress");
        }
    }
    void writeMap(XMLStreamWriter ostream, Object obj, Integer indentation) {
        if (is IMapNG obj) {
            writeTag(ostream, "view", indentation, false);
            writeIntegerAttribute(ostream, "current_player",
                obj.currentPlayer.playerId);
            writeIntegerAttribute(ostream, "current_turn", obj.currentTurn);
            writeTag(ostream, "map", indentation + 1, false);
            MapDimensions dimensions = obj.dimensions();
            writeIntegerAttribute(ostream, "version", dimensions.version);
            writeIntegerAttribute(ostream, "rows", dimensions.rows);
            writeIntegerAttribute(ostream, "columns", dimensions.columns);
            for (player in obj.players()) {
                writePlayer(ostream, player, indentation + 2);
            }
            for (i in 0..(dimensions.rows)) {
                variable Boolean rowEmpty = true;
                for (j in 0..(dimensions.columns)) {
                    Point loc = PointFactory.point(i, j);
                    TileType terrain = obj.getBaseTerrain(loc);
                    if (obj.isLocationEmpty(loc)) {
                        if (rowEmpty) {
                            writeTag(ostream, "row", indentation + 2, false);
                            rowEmpty = false;
                            writeIntegerAttribute(ostream, "index", i);
                        }
                        writeTag(ostream, "tile", indentation + 3, false);
                        writeIntegerAttribute(ostream, "row", i);
                        writeIntegerAttribute(ostream, "column", j);
                        if (TileType.notVisible != terrain) {
                            writeAttribute(ostream, "kind", terrain.toXML());
                        }
                        variable Boolean anyContents = false;
                        if (obj.isMountainous(loc)) {
                            anyContents = true;
                            writeTag(ostream, "mountain", indentation + 4, true);
                        }
                        for (river in obj.getRivers(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, river, indentation + 4);
                        }
                        if (exists ground = obj.getGround(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, ground, indentation + 4);
                        }
                        if (exists forest = obj.getForest(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, forest, indentation + 4);
                        }
                        for (fixture in obj.getOtherFixtures(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, fixture, indentation + 4);
                        }
                        if (anyContents) {
                            indent(ostream, indentation + 3);
                        }
                        ostream.writeEndElement();
                    }
                }
                if (!rowEmpty) {
                    indent(ostream, indentation + 2);
                    ostream.writeEndElement();
                }
            }
            indent(ostream, indentation + 1);
            ostream.writeEndElement();
            indent(ostream, indentation);
            ostream.writeEndElement();
        } else {
            throw IllegalArgumentException("Can only write IMapNG");
        }
    }
    writers = map<ClassOrInterface<Anything>, LocalXMLWriter> {
        `River`->writeRivers,
        `RiverFixture`->writeRivers,
        `AdventureFixture`->writeAdventure,
        `Portal`->writePortal,
        `Battlefield`->writeBattlefield,
        `Cave`->writeCave,
        `Ground`->writeGround,
        `Forest`->writeForest,
        simpleFixtureWriter(`Hill`, "hill"),
        simpleFixtureWriter(`Oasis`, "oasis"),
        simpleFixtureWriter(`Sandbar`, "sandbar"),
        `Animal`->writeAnimal,
        simpleFixtureWriter(`Centaur`, "centaur"),
        simpleFixtureWriter(`Dragon`, "dragon"),
        simpleFixtureWriter(`Fairy`, "fairy"),
        simpleFixtureWriter(`Giant`, "giant"),
        `SimpleImmortal`->writeSimpleImmortal,
        `TextFixture`->writeTextFixture,
        simpleFixtureWriter(`Implement`, "implement"),
        `ResourcePile`->writeResource,
        `CacheFixture`->writeCache,
        `Meadow`->writeMeadow,
        `Grove`->writeGrove,
        `Mine`->writeMine,
        `MineralVein`->writeMineral,
        simpleFixtureWriter(`Shrub`, "shrub"),
        `StoneDeposit`->writeStone,
        `IWorker`->writeWorker,
        `IJob`->writeJob,
        `ISkill`->writeSkill,
        `WorkerStats`->writeStats,
        `IUnit`->writeUnit,
        `Fortress`->writeFortress,
        `Village`->writeVillage,
        `AbstractTown`->writeTown,
        `IMapNG`->writeMap,
        `Player`->writePlayer
    };
}
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

import controller.map.fluidxml {
    FluidXMLWriter,
    FluidTerrainHandler,
    FluidExplorableHandler,
    FluidUnitMemberHandler,
    FluidResourceHandler,
    FluidTownHandler,
    XMLHelper
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
    Village,
    AbstractTown,
    TownSize
}
Regex snugEndTag = regex("([^ ])/>", true);
"""The main writer-to-XML class in the "fluid XML" implementation."""
shared class SPFluidWriter() satisfies SPWriter {
    alias LocalXMLWriter=>Anything(XMLStreamWriter, Object, Integer);
    late Map<ClassOrInterface<Anything>, LocalXMLWriter> writers;
    void writeSPObjectImpl(XMLStreamWriter ostream, Anything obj, Integer indent) {
        // FIXME: Find a way to implement ClassStream's algorithm using the Ceylon
        // metamodel instead of iterating over keys in the map.
        for (key->writer in writers) {
            if (exists obj, key.typeOf(obj)) {
                writer(ostream, obj, indent);
            }
        }
        throw IllegalArgumentException("Not an object we know how to write");
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
    void writePlayer(XMLStreamWriter ostream, Object obj, Integer indent) {
        if (is Player obj) {
            XMLHelper.writeTag(ostream, "player", indent, true);
            XMLHelper.writeIntegerAttribute(ostream, "number", obj.playerId);
            XMLHelper.writeAttribute(ostream, "code_name", obj.name);
        } else {
            throw IllegalArgumentException("Can only write Player");
        }
    }
    Entry<ClassOrInterface<Anything>, LocalXMLWriter> simpleFixtureWriter(
            ClassOrInterface<Anything> cls, String tag) {
        void retval(XMLStreamWriter ostream, Object obj, Integer indent) {
            if (!cls.typeOf(obj)) {
                throw IllegalArgumentException("Can only write ``cls.declaration.name``");
            }
            if (is IFixture obj) {
                XMLHelper.writeTag(ostream, tag, indent, true);
                if (is HasKind obj) {
                    XMLHelper.writeAttribute(ostream, "kind", obj.kind);
                }
                XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
                if (is HasImage obj) {
                    XMLHelper.writeImage(ostream, obj);
                }
            } else {
                throw IllegalStateException("Can only 'simply' write fixtures");
            }
        }
        return cls->retval;
    }
    void writeUnitOrders(XMLStreamWriter ostream, Integer indent, Integer turn,
            String tag, String text) {
        // assert (tag == "orders" || tag == "results");
        if (text.empty) {
            return;
        }
        XMLHelper.writeTag(ostream, tag, indent, false);
        if (turn >= 0) {
            XMLHelper.writeIntegerAttribute(ostream, "turn", turn);
        }
        ostream.writeCharacters(text);
        ostream.writeEndElement();
    }
    void writeUnit(XMLStreamWriter ostream, Object obj, Integer indent) {
        if (is IUnit obj) {
            Boolean empty =
                    CeylonIterable(obj).empty && obj.allOrders.empty &&
                        !obj.allResults.empty;
            XMLHelper.writeTag(ostream, "unit", indent, empty);
            XMLHelper.writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
            XMLHelper.writeNonEmptyAttribute(ostream, "kind", obj.kind);
            XMLHelper.writeNonEmptyAttribute(ostream, "name", obj.name);
            XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
            XMLHelper.writeImage(ostream, obj);
            if (is HasPortrait obj) {
                XMLHelper.writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
            }
            for (turn->orders in CeylonMap(obj.allOrders)) {
                // FIXME: Drop superfluous .string, .intValue() when IUnit is ported
                writeUnitOrders(ostream, indent + 1, turn.intValue(), "orders",
                    orders.string.trimmed);
            }
            for (turn->results in CeylonMap(obj.allResults)) {
                // FIXME: Drop superfluous .string, .intValue() when IUnit is ported
                writeUnitOrders(ostream, indent + 1, turn.intValue(), "results",
                    results.string.trimmed);
            }
            for (member in obj) {
                writeSPObjectImpl(ostream, member, indent + 1);
            }
            if (!empty) {
                XMLHelper.indent(ostream, indent);
                ostream.writeEndElement();
            }
        } else {
            throw IllegalArgumentException("Can only write IUnit");
        }
    }
    void writeFortress(XMLStreamWriter ostream, Object obj, Integer indent) {
        if (is Fortress obj) {
            XMLHelper.writeTag(ostream, "fortress", indent, false);
            XMLHelper.writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
            XMLHelper.writeNonEmptyAttribute(ostream, "name", obj.name);
            if (TownSize.small != obj.size()) {
                XMLHelper.writeAttribute(ostream, "size", obj.size().string);
            }
            XMLHelper.writeIntegerAttribute(ostream, "size", obj.id);
            XMLHelper.writeImage(ostream, obj);
            XMLHelper.writeNonEmptyAttribute(ostream, "portrait", obj.portrait);
            if (!CeylonIterable(obj).empty) {
                for (member in obj) {
                    writeSPObjectImpl(ostream, member, indent + 1);
                }
                XMLHelper.indent(ostream, indent);
            }
            ostream.writeEndElement();
        } else {
            throw IllegalArgumentException("Can only write Fortress");
        }
    }
    void writeMap(XMLStreamWriter ostream, Object obj, Integer indent) {
        if (is IMapNG obj) {
            XMLHelper.writeTag(ostream, "view", indent, false);
            XMLHelper.writeIntegerAttribute(ostream, "current_player",
                obj.currentPlayer.playerId);
            XMLHelper.writeIntegerAttribute(ostream, "current_turn", obj.currentTurn);
            XMLHelper.writeTag(ostream, "map", indent + 1, false);
            MapDimensions dimensions = obj.dimensions();
            XMLHelper.writeIntegerAttribute(ostream, "version", dimensions.version);
            XMLHelper.writeIntegerAttribute(ostream, "rows", dimensions.rows);
            XMLHelper.writeIntegerAttribute(ostream, "columns", dimensions.columns);
            for (player in obj.players()) {
                writePlayer(ostream, player, indent + 2);
            }
            for (i in 0..(dimensions.rows)) {
                variable Boolean rowEmpty = true;
                for (j in 0..(dimensions.columns)) {
                    Point loc = PointFactory.point(i, j);
                    TileType terrain = obj.getBaseTerrain(loc);
                    if (obj.isLocationEmpty(loc)) {
                        if (rowEmpty) {
                            XMLHelper.writeTag(ostream, "row", indent + 2, false);
                            rowEmpty = false;
                            XMLHelper.writeIntegerAttribute(ostream, "index", i);
                        }
                        XMLHelper.writeTag(ostream, "tile", indent + 3, false);
                        XMLHelper.writeIntegerAttribute(ostream, "row", i);
                        XMLHelper.writeIntegerAttribute(ostream, "column", j);
                        if (TileType.notVisible != terrain) {
                            XMLHelper.writeAttribute(ostream, "kind", terrain.toXML());
                        }
                        variable Boolean anyContents = false;
                        if (obj.isMountainous(loc)) {
                            anyContents = true;
                            XMLHelper.writeTag(ostream, "mountain", indent + 4, true);
                        }
                        for (river in obj.getRivers(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, river, indent + 4);
                        }
                        if (exists ground = obj.getGround(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, ground, indent + 4);
                        }
                        if (exists forest = obj.getForest(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, forest, indent + 4);
                        }
                        for (fixture in obj.getOtherFixtures(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, fixture, indent + 4);
                        }
                        if (anyContents) {
                            XMLHelper.indent(ostream, indent + 3);
                        }
                        ostream.writeEndElement();
                    }
                }
                if (!rowEmpty) {
                    XMLHelper.indent(ostream, indent + 2);
                    ostream.writeEndElement();
                }
            }
            XMLHelper.indent(ostream, indent + 1);
            ostream.writeEndElement();
            XMLHelper.indent(ostream, indent);
            ostream.writeEndElement();
        } else {
            throw IllegalArgumentException("Can only write IMapNG");
        }
    }
    writers = map<ClassOrInterface<Anything>, LocalXMLWriter> {
        `River`->FluidTerrainHandler.writeRivers,
        `RiverFixture`->FluidTerrainHandler.writeRivers,
        `AdventureFixture`->FluidExplorableHandler.writeAdventure,
        `Portal`->FluidExplorableHandler.writePortal,
        `Battlefield`->FluidExplorableHandler.writeBattlefield,
        `Cave`->FluidExplorableHandler.writeCave,
        `Ground`->FluidTerrainHandler.writeGround,
        `Forest`->FluidTerrainHandler.writeForest,
        simpleFixtureWriter(`Hill`, "hill"),
        simpleFixtureWriter(`Oasis`, "oasis"),
        simpleFixtureWriter(`Sandbar`, "sandbar"),
        `Animal`->FluidUnitMemberHandler.writeAnimal,
        simpleFixtureWriter(`Centaur`, "centaur"),
        simpleFixtureWriter(`Dragon`, "dragon"),
        simpleFixtureWriter(`Fairy`, "fairy"),
        simpleFixtureWriter(`Giant`, "giant"),
        `SimpleImmortal`->FluidUnitMemberHandler.writeSimpleImmortal,
        `TextFixture`->FluidExplorableHandler.writeTextFixture,
        simpleFixtureWriter(`Implement`, "implement"),
        `ResourcePile`->FluidResourceHandler.writeResource,
        `CacheFixture`->FluidResourceHandler.writeCache,
        `Meadow`->FluidResourceHandler.writeMeadow,
        `Grove`->FluidResourceHandler.writeGrove,
        `Mine`->FluidResourceHandler.writeMine,
        `MineralVein`->FluidResourceHandler.writeMineral,
        simpleFixtureWriter(`Shrub`, "shrub"),
        `StoneDeposit`->FluidResourceHandler.writeStone,
        `IWorker`->FluidUnitMemberHandler.writeWorker,
        `IJob`->FluidUnitMemberHandler.writeJob,
        `ISkill`->FluidUnitMemberHandler.writeSkill,
        `WorkerStats`->FluidUnitMemberHandler.writeStats,
        `IUnit`->writeUnit,
        `Fortress`->writeFortress,
        `Village`->FluidTownHandler.writeVillage,
        `AbstractTown`->FluidTownHandler.writeTown,
        `IMapNG`->writeMap,
        `Player`->writePlayer
    };
}
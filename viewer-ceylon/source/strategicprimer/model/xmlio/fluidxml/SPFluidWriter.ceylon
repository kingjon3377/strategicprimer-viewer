import ceylon.language.meta.model {
    ClassOrInterface
}
import ceylon.regex {
    Regex,
    regex
}

import java.io {
    StringWriter,
    IOException
}
import java.lang {
    IllegalArgumentException,
    IllegalStateException
}

import javax.xml.stream {
    XMLStreamWriter,
    XMLOutputFactory,
    XMLStreamException
}

import strategicprimer.model.map {
    Point,
    MapDimensions,
    HasKind,
    HasImage,
    Player,
    HasPortrait,
    IFixture,
    River,
    IMapNG,
    TileType,
    pointFactory
}
import strategicprimer.model.map.fixtures {
    Implement,
    ResourcePile,
    RiverFixture,
    TextFixture,
    Ground
}
import strategicprimer.model.map.fixtures.explorable {
    Portal,
    Battlefield,
    AdventureFixture,
    Cave
}
import strategicprimer.model.map.fixtures.mobile {
    Fairy,
    Giant,
    SimpleImmortal,
    IWorker,
    IUnit,
    Centaur,
    Dragon,
    Animal
}
import strategicprimer.model.map.fixtures.mobile.worker {
    IJob,
    WorkerStats,
    ISkill
}
import strategicprimer.model.map.fixtures.resources {
    Grove,
    Meadow,
    CacheFixture,
    Mine,
    StoneDeposit,
    Shrub,
    MineralVein
}
import strategicprimer.model.map.fixtures.terrain {
    Sandbar,
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    TownSize,
    Village,
    AbstractTown,
    Fortress,
    CommunityStats
}
import strategicprimer.model.xmlio {
    SPWriter,
    spNamespace
}
import ceylon.file {
    Path,
    File,
    Nil
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
    shared actual void writeSPObject(Anything(String)|Path arg, Object obj) {
        if (is Anything(String) ostream = arg) {
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            StringWriter writer = StringWriter();
            try {
                XMLStreamWriter xsw = xof.createXMLStreamWriter(writer);
                xsw.setDefaultNamespace(spNamespace);
                writeSPObjectImpl(xsw, obj, 0);
                xsw.writeCharacters("\n");
                xsw.writeEndDocument();
                xsw.flush();
                xsw.close();
            } catch (XMLStreamException except) {
                throw IOException("Failure in creating XML", except);
            }
            ostream(snugEndTag.replace(writer.string, "$1 />"));
        } else if (is Path path = arg) {
            File file;
            value res = path.resource;
            if (is File res) {
                file = res;
            } else if (is Nil res) {
                file = res.createFile();
            } else {
                throw IOException("``path`` exists but is not a file");
            }
            try (writer = file.Overwriter()) {
                writeSPObject(writer.write, obj);
            }
        }
    }
    shared actual void write(Path|Anything(String) arg, IMapNG map) {
        if (is Path path = arg) {
            File file;
            value res = path.resource;
            if (is File res) {
                file = res;
            } else if (is Nil res) {
                file = res.createFile();
            } else {
                throw IOException("``path`` exists but is not a file");
            }
            try (writer = file.Overwriter()) {
                writeSPObject(writer.write, map);
            }
        } else if (is Anything(String) ostream = arg) {
            writeSPObject(ostream, map);
        }
    }
    void writePlayer(XMLStreamWriter ostream, Object obj, Integer indentation) {
        if (is Player obj) {
            writeTag(ostream, "player", indentation, true);
            writeAttributes(ostream, "number"->obj.playerId, "code_name"->obj.name);
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
                    writeAttributes(ostream, "kind"->obj.kind);
                }
                writeAttributes(ostream, "id"->obj.id);
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
            writeAttributes(ostream, "turn"->turn);
        }
        ostream.writeCharacters(text);
        ostream.writeEndElement();
    }
    void writeUnit(XMLStreamWriter ostream, Object obj, Integer indentation) {
        if (is IUnit obj) {
            Boolean nonemptyOrders(Integer->String entry) => !entry.item.empty;
            Boolean empty = obj.empty && obj.allOrders.filter(nonemptyOrders).empty &&
                        obj.allResults.filter(nonemptyOrders).empty;
            writeTag(ostream, "unit", indentation, empty);
            writeAttributes(ostream, "owner"->obj.owner.playerId);
            writeNonEmptyAttributes(ostream, "kind"->obj.kind, "name"->obj.name);
            writeAttributes(ostream, "id"->obj.id);
            writeImage(ostream, obj);
            if (is HasPortrait obj) {
                writeNonEmptyAttributes(ostream, "portrait"->obj.portrait);
            }
            for (turn->orders in obj.allOrders) {
                writeUnitOrders(ostream, indentation + 1, turn, "orders", orders.trimmed);
            }
            for (turn->results in obj.allResults) {
                writeUnitOrders(ostream, indentation + 1, turn, "results",
                    results.trimmed);
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
            writeAttributes(ostream, "owner"->obj.owner.playerId);
            writeNonEmptyAttributes(ostream, "name"->obj.name);
            if (TownSize.small != obj.townSize) {
                writeAttributes(ostream, "size"->obj.townSize.string);
            }
            writeAttributes(ostream, "id"->obj.id);
            writeImage(ostream, obj);
            writeNonEmptyAttributes(ostream, "portrait"->obj.portrait);
            if (!obj.empty) {
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
            writeAttributes(ostream, "current_player"->obj.currentPlayer.playerId,
                "current_turn"->obj.currentTurn);
            writeTag(ostream, "map", indentation + 1, false);
            MapDimensions dimensions = obj.dimensions;
            writeAttributes(ostream, "version"->dimensions.version,
                "rows"->dimensions.rows, "columns"->dimensions.columns);
            for (player in obj.players) {
                writePlayer(ostream, player, indentation + 2);
            }
            for (i in 0:(dimensions.rows)) {
                variable Boolean rowEmpty = true;
                for (j in 0:(dimensions.columns)) {
                    Point loc = pointFactory(i, j);
                    TileType terrain = obj.baseTerrain(loc);
                    if (!obj.locationEmpty(loc)) {
                        if (rowEmpty) {
                            writeTag(ostream, "row", indentation + 2, false);
                            rowEmpty = false;
                            writeAttributes(ostream, "index"->i);
                        }
                        writeTag(ostream, "tile", indentation + 3, false);
                        writeAttributes(ostream, "row"->i, "column"->j);
                        if (TileType.notVisible != terrain) {
                            writeAttributes(ostream, "kind"->terrain.xml);
                        }
                        variable Boolean anyContents = false;
                        if (obj.mountainous(loc)) {
                            anyContents = true;
                            writeTag(ostream, "mountain", indentation + 4, true);
                        }
                        for (river in sort(obj.rivers(loc))) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, river, indentation + 4);
                        }
                        if (exists ground = obj.ground(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, ground, indentation + 4);
                        }
                        if (exists forest = obj.forest(loc)) {
                            anyContents = true;
                            writeSPObjectImpl(ostream, forest, indentation + 4);
                        }
                        for (fixture in obj.otherFixtures(loc)) {
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
        `Player`->writePlayer,
        `CommunityStats`->writeCommunityStats
    };
}
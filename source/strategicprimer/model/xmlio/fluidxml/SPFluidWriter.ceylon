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
import lovelace.util.common {
    todo
}
import strategicprimer.model.xmlio.fluidxml {
	FluidBase { ... }
}
Regex snugEndTag = regex("([^ ])/>", true);
variable Integer currentTurn = -1;
"""The main writer-to-XML class in the "fluid XML" implementation."""
shared class SPFluidWriter() satisfies SPWriter {
    alias LocalXMLWriter=>Anything(XMLStreamWriter, Object, Integer);
    late Map<ClassOrInterface<Anything>, LocalXMLWriter> writers;
    void writeSPObjectImpl(XMLStreamWriter ostream, Object obj, Integer indentation) {
        for (type in TypeStream(obj)) {
            if (exists writer = writers[type]) {
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
                xsw.writeEndDocument();
                xsw.flush();
                xsw.close();
            } catch (XMLStreamException except) {
                throw IOException("Failure in creating XML", except);
            }
            {String+} lines = writer.string.lines;
            for (line in lines) {
                ostream(snugEndTag.replace(line, "$1 />"));
                ostream(operatingSystem.newline);
            }
        } else if (is Path path = arg) {
            File file;
            switch (res = path.resource)
            case (is File) {
                file = res;
            }
            case (is Nil) {
                file = res.createFile();
            }
            else {
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
            switch (res = path.resource)
            case (is File) {
                file = res;
            }
            case (is Nil) {
                file = res.createFile();
            }
            else {
                throw IOException("``path`` exists but is not a file");
            }
            try (writer = file.Overwriter()) {
                writeSPObject(writer.write, map);
            }
        } else if (is Anything(String) ostream = arg) {
            writeSPObject(ostream, map);
        }
    }
    todo("Does this really need to be a class method?")
    void writePlayer(XMLStreamWriter ostream, Player obj, Integer indentation) {
        if (!obj.name.empty) {
            writeTag(ostream, "player", indentation, true);
            writeAttributes(ostream, "number"->obj.playerId, "code_name"->obj.name);
        }
    }
    Entry<ClassOrInterface<Anything>, LocalXMLWriter> simpleFixtureWriter(
            ClassOrInterface<Anything> cls, String tag) {
        void retval(XMLStreamWriter ostream, Object obj, Integer indentation) {
            "Can only write ``cls.declaration.name``"
            assert (cls.typeOf(obj));
            """Can only "simply" write fixtures."""
            assert (is IFixture obj);
            writeTag(ostream, tag, indentation, true);
            if (is HasKind obj) {
                writeAttributes(ostream, "kind"->obj.kind);
            }
            writeAttributes(ostream, "id"->obj.id);
            if (is HasImage obj) {
                writeImage(ostream, obj);
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
    void writeUnit(XMLStreamWriter ostream, IUnit obj, Integer indentation) {
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
    }
    void writeFortress(XMLStreamWriter ostream, Fortress obj, Integer indentation) {
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
    }
    void writeMap(XMLStreamWriter ostream, IMapNG obj, Integer indentation) {
        writeTag(ostream, "view", indentation, false);
        writeAttributes(ostream, "current_player"->obj.currentPlayer.playerId,
            "current_turn"->obj.currentTurn);
        currentTurn = obj.currentTurn;
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
                TileType? terrain = obj.baseTerrain[loc];
                if (!obj.locationEmpty(loc)) {
                    if (rowEmpty) {
                        writeTag(ostream, "row", indentation + 2, false);
                        rowEmpty = false;
                        writeAttributes(ostream, "index"->i);
                    }
                    writeTag(ostream, "tile", indentation + 3, false);
                    writeAttributes(ostream, "row"->i, "column"->j);
                    if (exists terrain) {
                        writeAttributes(ostream, "kind"->terrain.xml);
                    }
                    variable Boolean anyContents = false;
//                        if (obj.mountainous[loc]) { // TODO: syntax sugar once compiler bug fixed
                    if (obj.mountainous.get(loc)) {
                        anyContents = true;
                        writeTag(ostream, "mountain", indentation + 4, true);
                    }
//                        for (river in sort(obj.rivers[loc])) {
                    for (river in sort(obj.rivers.get(loc))) {
                        anyContents = true;
                        writeSPObjectImpl(ostream, river, indentation + 4);
                    }
                    // To avoid breaking map-format-conversion tests, and to
                    // avoid churn in existing maps, put the first Ground and Forest
                    // before other fixtures.
//                    Ground? ground = obj.fixtures[loc].narrow<Ground>().first;
                    Ground? ground = obj.fixtures.get(loc).narrow<Ground>().first;
                    if (exists ground) {
                        anyContents = true;
                        writeSPObjectImpl(ostream, ground, indentation + 4);
                    }
//                    Forest? forest = obj.fixtures[loc].narrow<Forest>().first;
                    Forest? forest = obj.fixtures.get(loc).narrow<Forest>().first;
                    if (exists forest) {
                        anyContents = true;
                        writeSPObjectImpl(ostream, forest, indentation + 4);
                    }
//                        for (fixture in obj.fixtures[loc]) {
                    for (fixture in obj.fixtures.get(loc)) {
                        if ([ground, forest].coalesced.contains(fixture)) {
                            continue;
                        }
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
    }
    writers = map<ClassOrInterface<Anything>, LocalXMLWriter> {
        `River`->castingWriter<River>(fluidTerrainHandler.writeRivers),
        `AdventureFixture`->castingWriter<AdventureFixture>(fluidExplorableHandler.writeAdventure),
        `Portal`->castingWriter<Portal>(fluidExplorableHandler.writePortal),
        `Battlefield`->castingWriter<Battlefield>(fluidExplorableHandler.writeBattlefield),
        `Cave`->castingWriter<Cave>(fluidExplorableHandler.writeCave),
        `Ground`->castingWriter<Ground>(fluidTerrainHandler.writeGround),
        `Forest`->castingWriter<Forest>(fluidTerrainHandler.writeForest),
        simpleFixtureWriter(`Hill`, "hill"),
        simpleFixtureWriter(`Oasis`, "oasis"),
        simpleFixtureWriter(`Sandbar`, "sandbar"),
        `Animal`->castingWriter<Animal>(unitMemberHandler.writeAnimal),
        simpleFixtureWriter(`Centaur`, "centaur"),
        simpleFixtureWriter(`Dragon`, "dragon"),
        simpleFixtureWriter(`Fairy`, "fairy"),
        simpleFixtureWriter(`Giant`, "giant"),
        `SimpleImmortal`->castingWriter<SimpleImmortal>(unitMemberHandler.writeSimpleImmortal),
        `TextFixture`->castingWriter<TextFixture>(fluidExplorableHandler.writeTextFixture),
        `Implement`->castingWriter<Implement>(fluidResourceHandler.writeImplement),
        `ResourcePile`->castingWriter<ResourcePile>(fluidResourceHandler.writeResource),
        `CacheFixture`->castingWriter<CacheFixture>(fluidResourceHandler.writeCache),
        `Meadow`->castingWriter<Meadow>(fluidResourceHandler.writeMeadow),
        `Grove`->castingWriter<Grove>(fluidResourceHandler.writeGrove),
        `Mine`->castingWriter<Mine>(fluidResourceHandler.writeMine),
        `MineralVein`->castingWriter<MineralVein>(fluidResourceHandler.writeMineral),
        `Shrub`->castingWriter<Shrub>(fluidResourceHandler.writeShrub),
        `StoneDeposit`->castingWriter<StoneDeposit>(fluidResourceHandler.writeStone),
        `IWorker`->castingWriter<IWorker>(unitMemberHandler.writeWorker),
        `IJob`->castingWriter<IJob>(unitMemberHandler.writeJob),
        `ISkill`->castingWriter<ISkill>(unitMemberHandler.writeSkill),
        `WorkerStats`->castingWriter<WorkerStats>(unitMemberHandler.writeStats),
        `IUnit`->castingWriter<IUnit>(writeUnit),
        `Fortress`->castingWriter<Fortress>(writeFortress),
        `Village`->castingWriter<Village>(fluidTownHandler.writeVillage),
        `AbstractTown`->castingWriter<AbstractTown>(fluidTownHandler.writeTown),
        `IMapNG`->castingWriter<IMapNG>(writeMap),
        `Player`->castingWriter<Player>(writePlayer),
        `CommunityStats`->castingWriter<CommunityStats>(fluidTownHandler.writeCommunityStats)
    };
}

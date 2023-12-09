package legacy.xmlio.fluidxml;

import org.javatuples.Pair;

import static impl.xmlio.ISPReader.SP_NAMESPACE;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import legacy.map.HasKind;
import legacy.map.HasImage;
import common.map.Player;
import legacy.map.MapDimensions;
import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.River;
import legacy.map.TileType;
import legacy.map.IMapNG;
import legacy.map.TileFixture;
import legacy.map.Direction;

import legacy.map.fixtures.Implement;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.Ground;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.FortressMember;

import legacy.map.fixtures.explorable.Portal;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.AdventureFixture;
import legacy.map.fixtures.explorable.Cave;

import legacy.map.fixtures.mobile.Fairy;
import legacy.map.fixtures.mobile.Giant;
import legacy.map.fixtures.mobile.SimpleImmortal;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Centaur;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.Dragon;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.ImmortalAnimal;

import legacy.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.WorkerStats;
import legacy.map.fixtures.mobile.worker.ISkill;

import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.resources.MineralVein;

import legacy.map.fixtures.terrain.Oasis;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Forest;

import common.map.fixtures.towns.TownSize;
import legacy.map.fixtures.towns.Village;
import legacy.map.fixtures.towns.AbstractTown;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.CommunityStats;

import impl.xmlio.SPWriter;

import lovelace.util.TypeStream;

import static legacy.xmlio.fluidxml.FluidBase.*;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import lovelace.util.ThrowingConsumer;

import java.util.function.Predicate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;

/**
 * The main writer-to-XML class in the "fluid XML" implementation.
 */
public class SPFluidWriter implements SPWriter {
    // Note Ceylon regex was 'global', matching all rather than just the first
    private static final Pattern SNUG_END_TAG = Pattern.compile("([^ ])/>");

    private void writeSPObjectImpl(final XMLStreamWriter ostream, final Object obj, final int indentation)
            throws XMLStreamException {
        for (final Class<?> type : new TypeStream(obj)) {
            if (writers.containsKey(type)) {
                writers.get(type).writeCasting(ostream, obj, indentation);
                return;
            }
        }
        throw new IllegalStateException("No writer present for " + obj.getClass().getName());
    }

    @Override
    public void writeSPObject(final ThrowingConsumer<String, IOException> ostream, final Object obj)
            throws XMLStreamException, IOException {
        final XMLOutputFactory xof = XMLOutputFactory.newInstance();
        final StringWriter writer = new StringWriter();
        final XMLStreamWriter xsw = xof.createXMLStreamWriter(writer);
        xsw.setDefaultNamespace(SP_NAMESPACE);
        writeSPObjectImpl(xsw, obj, 0);
        xsw.writeEndDocument();
        xsw.flush();
        xsw.close();
        for (final String line : writer.toString().split(System.lineSeparator())) {
            ostream.accept(SNUG_END_TAG.matcher(line).replaceAll("$1 />"));
            ostream.accept(System.lineSeparator());
        }
    }

    @Override
    public void writeSPObject(final Path file, final Object obj) throws XMLStreamException, IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeSPObject(writer::write, obj);
        }
    }

    @Override
    public void write(final Path arg, final IMapNG map) throws XMLStreamException, IOException {
        writeSPObject(arg, map);
    }

    @Override
    public void write(final ThrowingConsumer<String, IOException> arg, final IMapNG map)
            throws XMLStreamException, IOException {
        writeSPObject(arg, map);
    }

    /**
     * TODO: Does this really need to be an instance (non-static) method?
     */
    private static void writePlayer(final XMLStreamWriter ostream, final Player obj, final int indentation)
            throws XMLStreamException {
        if (!obj.getName().isEmpty()) {
            writeTag(ostream, "player", indentation, true);
            writeAttributes(ostream, Pair.with("number", obj.getPlayerId()),
                    Pair.with("code_name", obj.getName()));
            writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
            if (obj.getCountry() != null) {
                writeNonEmptyAttributes(ostream, Pair.with("country", obj.getCountry()));
            }
        }
    }

    private record SimpleFixtureWriter<Type>(Class<Type> cls, String tag) implements FluidXMLWriter<Type> {

        @Override
        public void write(final XMLStreamWriter ostream, final Object obj, final int indentation)
                throws XMLStreamException {
            if (!cls.isInstance(obj)) {
                throw new IllegalArgumentException("Can only write " + cls.getName());
            } else if (!(obj instanceof IFixture)) {
                throw new IllegalArgumentException("Can only \"simply\" write fixtures.");
            }
            writeTag(ostream, tag, indentation, true);
            if (obj instanceof final HasKind hk) {
                writeAttributes(ostream, Pair.with("kind", hk.getKind()));
            }
            writeAttributes(ostream, Pair.with("id", ((IFixture) obj).getId()));
            if (obj instanceof final HasImage hi) {
                writeImage(ostream, hi);
            }
        }

        public Class<Type> getType() {
            return cls;
        }
    }

    private static void writeUnitOrders(final XMLStreamWriter ostream, final int indentation, final int turn,
                                        final String tag, final String text) throws XMLStreamException {
        // assert (tag == "orders" || tag == "results");
        if (text.isEmpty()) {
            return;
        }
        writeTag(ostream, tag, indentation, false);
        if (turn >= 0) {
            writeAttributes(ostream, Pair.with("turn", turn));
        }
        ostream.writeCharacters(text);
        ostream.writeEndElement();
    }

    private void writeUnit(final XMLStreamWriter ostream, final IUnit obj, final int indentation)
            throws XMLStreamException {
        final boolean empty = obj.isEmpty() &&
                obj.getAllOrders().values().stream().allMatch(String::isEmpty) &&
                obj.getAllResults().values().stream().allMatch(String::isEmpty);
        writeTag(ostream, "unit", indentation, empty);
        writeAttributes(ostream, Pair.with("owner", obj.owner().getPlayerId()));
        writeNonEmptyAttributes(ostream, Pair.with("kind", obj.getKind()),
                Pair.with("name", obj.getName()));
        writeAttributes(ostream, Pair.with("id", obj.getId()));
        writeImage(ostream, obj);
        writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
        for (final Map.Entry<Integer, String> entry : obj.getAllOrders().entrySet()) {
            writeUnitOrders(ostream, indentation + 1, entry.getKey(), "orders",
                    entry.getValue().strip());
        }
        for (final Map.Entry<Integer, String> entry : obj.getAllResults().entrySet()) {
            writeUnitOrders(ostream, indentation + 1, entry.getKey(), "results",
                    entry.getValue().strip());
        }
        for (final UnitMember member : obj) {
            writeSPObjectImpl(ostream, member, indentation + 1);
        }
        if (!empty) {
            indent(ostream, indentation);
            ostream.writeEndElement();
        }
    }

    private void writeFortress(final XMLStreamWriter ostream, final IFortress obj, final int indentation)
            throws XMLStreamException {
        writeTag(ostream, "fortress", indentation, false);
        writeAttributes(ostream, Pair.with("owner", obj.owner().getPlayerId()));
        writeNonEmptyAttributes(ostream, Pair.with("name", obj.getName()));
        if (TownSize.Small != obj.getTownSize()) {
            writeAttributes(ostream, Pair.with("size", obj.getTownSize().toString()));
        }
        writeAttributes(ostream, Pair.with("id", obj.getId()));
        writeImage(ostream, obj);
        writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
        if (obj.iterator().hasNext()) {
            for (final FortressMember member : obj) {
                writeSPObjectImpl(ostream, member, indentation + 1);
            }
            indent(ostream, indentation);
        }
        ostream.writeEndElement();
    }

    private void writeMap(final XMLStreamWriter ostream, final IMapNG obj, final int indentation)
            throws XMLStreamException {
        writeTag(ostream, "view", indentation, false);
        writeAttributes(ostream, Pair.with("current_player", obj.getCurrentPlayer().getPlayerId()),
                Pair.with("current_turn", obj.getCurrentTurn()));
        writeTag(ostream, "map", indentation + 1, false);
        final MapDimensions dimensions = obj.getDimensions();
        writeAttributes(ostream, Pair.with("version", dimensions.version()),
                Pair.with("rows", dimensions.rows()),
                Pair.with("columns", dimensions.columns()));
        for (final Player player : obj.getPlayers()) {
            writePlayer(ostream, player, indentation + 2);
        }
        final Predicate<Object> isGround = Ground.class::isInstance;
        final Predicate<Object> isForest = Forest.class::isInstance;
        final Function<Object, Ground> groundCast = Ground.class::cast;
        final Function<Object, Forest> forestCast = Forest.class::cast;
        for (int i = 0; i < dimensions.rows(); i++) {
            boolean rowEmpty = true;
            for (int j = 0; j < dimensions.columns(); j++) {
                final Point loc = new Point(i, j);
                final TileType terrain = obj.getBaseTerrain(loc);
                if (!obj.isLocationEmpty(loc)) {
                    if (rowEmpty) {
                        writeTag(ostream, "row", indentation + 2, false);
                        rowEmpty = false;
                        writeAttributes(ostream, Pair.with("index", i));
                    }
                    writeTag(ostream, "tile", indentation + 3, false);
                    writeAttributes(ostream, Pair.with("row", i),
                            Pair.with("column", j));
                    if (terrain != null) {
                        writeAttributes(ostream, Pair.with("kind",
                                terrain.getXml()));
                    }
                    boolean anyContents = false;
                    for (final Player bookmarkPlayer : obj.getAllBookmarks(loc)) {
                        anyContents = true;
                        writeTag(ostream, "bookmark", indentation + 4, true);
                        writeAttributes(ostream,
                                Pair.with("player", bookmarkPlayer.getPlayerId()));
                    }
                    if (obj.isMountainous(loc)) {
                        anyContents = true;
                        writeTag(ostream, "mountain", indentation + 4, true);
                    }
                    // Ceylon code made a point of sorting the rivers; that's hard
                    // to do here, and I think in Java we use an EnumSet, which should
                    // be sorted anyway
                    for (final River river : obj.getRivers(loc)) {
                        anyContents = true;
                        writeSPObjectImpl(ostream, river, indentation + 4);
                    }
                    // Similarly, roads are in an EnumMap, which is automatically sorted
                    for (final Map.Entry<Direction, Integer> entry :
                            obj.getRoads(loc).entrySet()) {
                        writeTag(ostream, "road", indentation + 4, true);
                        writeAttributes(ostream,
                                Pair.with("direction", entry.getKey().toString()),
                                Pair.with("quality", entry.getValue()));
                    }
                    // TODO: Instead of special-casing ground and forest, and to minimize future churn with exploration, sort fixtures in some way.
                    // To avoid breaking map-format-conversion tests, and to
                    // avoid churn in existing maps, put the first Ground and Forest
                    // before other fixtures.
                    final Ground ground = obj.getFixtures(loc).stream()
                            .filter(isGround).map(groundCast)
                            .findFirst().orElse(null);
                    if (ground != null) {
                        anyContents = true;
                        writeSPObjectImpl(ostream, ground, indentation + 4);
                    }
                    final Forest forest = obj.getFixtures(loc).stream()
                            .filter(isForest).map(forestCast)
                            .findFirst().orElse(null);
                    if (forest != null) {
                        anyContents = true;
                        writeSPObjectImpl(ostream, forest, indentation + 4);
                    }
                    for (final TileFixture fixture : obj.getFixtures(loc)) {
                        if (fixture == forest || fixture == ground) {
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
        if (obj.streamLocations().filter(((Predicate<Point>) Point::isValid).negate())
                .map(obj::getFixtures).anyMatch(((Predicate<Collection<TileFixture>>)
                        Collection::isEmpty).negate())) {
            writeTag(ostream, "elsewhere", indentation + 2, false);
            for (final TileFixture fixture : obj.streamLocations()
                    .filter(((Predicate<Point>) Point::isValid).negate())
                    .flatMap(p -> obj.getFixtures(p).stream()).toList()) {
                writeSPObjectImpl(ostream, fixture, indentation + 3);
            }
            indent(ostream, indentation + 2);
            ostream.writeEndElement();
        }
        indent(ostream, indentation + 1);
        ostream.writeEndElement();
        indent(ostream, indentation);
        ostream.writeEndElement();
    }

    private final Map<Class<?>, FluidXMLWriter<?>> writers;

    private static <Type> void addWriterToMap(final Map<Class<?>, FluidXMLWriter<?>> map,
                                              final Class<Type> cls, final FluidXMLWriter<Type> writer) {
        map.put(cls, writer);
    }

    public SPFluidWriter() {
        final Map<Class<?>, FluidXMLWriter<?>> temp = new HashMap<>();
        addWriterToMap(temp, River.class, FluidTerrainHandler::writeRiver);
        addWriterToMap(temp, AdventureFixture.class, FluidExplorableHandler::writeAdventure);
        addWriterToMap(temp, Portal.class, FluidExplorableHandler::writePortal);
        addWriterToMap(temp, Battlefield.class, FluidExplorableHandler::writeBattlefield);
        addWriterToMap(temp, Cave.class, FluidExplorableHandler::writeCave);
        addWriterToMap(temp, Ground.class, FluidTerrainHandler::writeGround);
        addWriterToMap(temp, Forest.class, FluidTerrainHandler::writeForest);
        addWriterToMap(temp, Animal.class, UnitMemberHandler::writeAnimal);
        addWriterToMap(temp, AnimalTracks.class, UnitMemberHandler::writeAnimalTracks);
        for (final SimpleFixtureWriter<?> writer : Arrays.asList(
                new SimpleFixtureWriter<>(Hill.class, "hill"),
                new SimpleFixtureWriter<>(Oasis.class, "oasis"),
                new SimpleFixtureWriter<>(Centaur.class, "centaur"),
                new SimpleFixtureWriter<>(Dragon.class, "dragon"),
                new SimpleFixtureWriter<>(Fairy.class, "fairy"),
                new SimpleFixtureWriter<>(Giant.class, "giant"))) {
            temp.put(writer.getType(), writer);
        }
        addWriterToMap(temp, SimpleImmortal.class, UnitMemberHandler::writeSimpleImmortal);
        addWriterToMap(temp, ImmortalAnimal.class, UnitMemberHandler::writeSimpleImmortal);
        addWriterToMap(temp, TextFixture.class, FluidExplorableHandler::writeTextFixture);
        addWriterToMap(temp, Implement.class, FluidResourceHandler::writeImplement);
        addWriterToMap(temp, IResourcePile.class, FluidResourceHandler::writeResource);
        addWriterToMap(temp, CacheFixture.class, FluidResourceHandler::writeCache);
        addWriterToMap(temp, Meadow.class, FluidResourceHandler::writeMeadow);
        addWriterToMap(temp, Grove.class, FluidResourceHandler::writeGrove);
        addWriterToMap(temp, Mine.class, FluidResourceHandler::writeMine);
        addWriterToMap(temp, MineralVein.class, FluidResourceHandler::writeMineral);
        addWriterToMap(temp, Shrub.class, FluidResourceHandler::writeShrub);
        addWriterToMap(temp, StoneDeposit.class, FluidResourceHandler::writeStone);
        addWriterToMap(temp, IWorker.class, UnitMemberHandler::writeWorker);
        addWriterToMap(temp, IJob.class, UnitMemberHandler::writeJob);
        addWriterToMap(temp, ISkill.class, UnitMemberHandler::writeSkill);
        addWriterToMap(temp, WorkerStats.class, UnitMemberHandler::writeStats);
        addWriterToMap(temp, IUnit.class, this::writeUnit);
        addWriterToMap(temp, IFortress.class, this::writeFortress);
        addWriterToMap(temp, Village.class, FluidTownHandler::writeVillage);
        addWriterToMap(temp, AbstractTown.class, FluidTownHandler::writeTown);
        addWriterToMap(temp, IMapNG.class, this::writeMap);
        addWriterToMap(temp, Player.class, SPFluidWriter::writePlayer);
        addWriterToMap(temp, CommunityStats.class, FluidTownHandler::writeCommunityStats);
        writers = Collections.unmodifiableMap(temp);
    }
}

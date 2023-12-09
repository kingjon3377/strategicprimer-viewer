package legacy.xmlio.fluidxml;

import javax.xml.stream.XMLStreamException;

import org.javatuples.Pair;

import java.io.IOException;
import java.io.BufferedReader;
import java.text.ParseException;
import java.util.function.IntFunction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Deque;
import java.util.LinkedList;

import java.io.Reader;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;

import lovelace.util.IteratorWrapper;
import lovelace.util.TypesafeXMLEventReader;

import legacy.idreg.IDRegistrar;
import legacy.idreg.IDFactory;
import legacy.map.HasKind;
import common.map.Player;
import common.map.PlayerImpl;
import legacy.map.MapDimensions;
import legacy.map.MapDimensionsImpl;
import legacy.map.Point;
import legacy.map.TileType;
import legacy.map.River;
import common.map.IMutablePlayerCollection;
import legacy.map.TileFixture;
import common.map.PlayerCollection;
import legacy.map.IMutableLegacyMap;
import legacy.map.LegacyMap;
import legacy.map.Direction;
import legacy.map.fixtures.FortressMember;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Unit;
import legacy.map.fixtures.mobile.Giant;
import legacy.map.fixtures.mobile.Centaur;
import legacy.map.fixtures.mobile.Fairy;
import legacy.map.fixtures.mobile.Dragon;
import legacy.map.fixtures.mobile.Ogre;
import legacy.map.fixtures.mobile.Troll;
import legacy.map.fixtures.mobile.Sphinx;
import legacy.map.fixtures.mobile.Phoenix;
import legacy.map.fixtures.mobile.Griffin;
import legacy.map.fixtures.mobile.Djinn;
import legacy.map.fixtures.mobile.Simurgh;
import legacy.map.fixtures.mobile.Minotaur;
import legacy.map.fixtures.mobile.Immortal;
import common.map.fixtures.mobile.MaturityModel;
import legacy.map.fixtures.mobile.Snowbird;
import legacy.map.fixtures.mobile.Thunderbird;
import legacy.map.fixtures.mobile.Pegasus;
import legacy.map.fixtures.mobile.Unicorn;
import legacy.map.fixtures.mobile.Kraken;
import legacy.map.fixtures.mobile.ImmortalAnimal;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Oasis;
import legacy.map.fixtures.towns.FortressImpl;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.TownSize;
import legacy.xmlio.IMapReader;
import impl.xmlio.ISPReader;
import common.xmlio.Warning;
import impl.xmlio.exceptions.UnsupportedTagException;
import impl.xmlio.exceptions.MissingChildException;
import impl.xmlio.exceptions.UnwantedChildException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.MapVersionException;

import static legacy.xmlio.fluidxml.FluidBase.*;

import common.xmlio.SPFormatException;

import java.nio.charset.StandardCharsets;

/**
 * The main reader-from-XML class in the 'fluid XML' implementation.
 */
public class SPFluidReader implements IMapReader, ISPReader {
    private Object readSPObject(final StartElement element, final QName parent,
                                final Iterable<XMLEvent> stream, final IMutablePlayerCollection players, final Warning warner,
                                final IDRegistrar idFactory) throws SPFormatException {
        final String namespace = element.getName().getNamespaceURI();
        final String tag = element.getName().getLocalPart().toLowerCase();
        if (namespace.isEmpty() || namespace.equals(SP_NAMESPACE) ||
                namespace.equals(XMLConstants.NULL_NS_URI)) {
            if ("animal".equals(tag) &&
                    Immortal.IMMORTAL_ANIMALS.contains(getAttribute(element, "kind")) &&
                    !getBooleanAttribute(element, "traces", false)) {
                return setImage(ImmortalAnimal.parse(getAttribute(element, "kind"))
                        .apply(getOrGenerateID(element, warner, idFactory)), element, warner);
            } else if (readers.containsKey(tag)) {
                return readers.get(tag).read(element, parent, stream,
                        players, warner, idFactory);
            }
        }
        throw UnsupportedTagException.future(element);
    }

    private record SimpleFixtureReader(String tag, IntFunction<Object> factory) {

        public Object reader(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
                             final IMutablePlayerCollection players, final Warning warner, final IDRegistrar idFactory)
                throws SPFormatException {
            requireTag(element, parent, tag);
            expectAttributes(element, warner, "id", "image");
            spinUntilEnd(element.getName(), stream);
            return setImage(factory.apply(getOrGenerateID(element, warner,
                    idFactory)), element, warner);
        }

        public Pair<String, FluidXMLReader<?>> getPair() {
            return Pair.with(tag, this::reader);
        }
    }

    @FunctionalInterface
    private interface HasKindFactory {
        HasKind apply(String string, int integer);
    }

    private record SimpleHasKindReader(String tag, HasKindFactory factory) {

        public Object reader(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
                             final IMutablePlayerCollection players, final Warning warner, final IDRegistrar idFactory)
                throws SPFormatException {
            requireTag(element, parent, tag);
            expectAttributes(element, warner, "id", "kind", "image");
            spinUntilEnd(element.getName(), stream);
            return setImage(factory.apply(getAttribute(element, "kind"),
                    getOrGenerateID(element, warner, idFactory)), element, warner);
        }

        public Pair<String, FluidXMLReader<?>> getPair() {
            return Pair.with(tag, this::reader);
        }
    }

    private static StartElement firstStartElement(final Iterable<XMLEvent> stream, final StartElement parent)
            throws SPFormatException {
        for (final XMLEvent event : stream) {
            if (event instanceof final StartElement se && isSPStartElement(event)) {
                return se;
            }
        }
        throw new MissingChildException(parent);
    }

    private static boolean isFutureTag(final StartElement tag, final Warning warner) {
        if (FUTURE_TAGS.contains(tag.getName().getLocalPart().toLowerCase())) {
            warner.handle(UnsupportedTagException.future(tag));
            return true;
        } else {
            return false;
        }
    }

    private void parseTileChild(final IMutableLegacyMap map, final StartElement parent,
								final Iterable<XMLEvent> stream, final IMutablePlayerCollection players, final Warning warner,
								final IDRegistrar idFactory, final Point currentTile, final StartElement element)
            throws SPFormatException {
        final String type = element.getName().getLocalPart().toLowerCase();
        if (isFutureTag(element, warner)) {
            return;
        } else if ("sandbar".equals(type)) {
            warner.handle(UnsupportedTagException.obsolete(element));
            return;
        } else if ("tile".equals(type)) {
            throw new UnwantedChildException(parent.getName(), element);
        } else if ("mountain".equals(type)) {
            map.setMountainous(currentTile, true);
            return;
        } else if ("bookmark".equals(type)) {
            expectAttributes(element, warner, "player");
            map.addBookmark(currentTile,
                    players.getPlayer(getIntegerAttribute(element, "player")));
            return;
        } else if ("road".equals(type)) {
            expectAttributes(element, warner, "direction", "quality");
            final Direction direction;
            try {
                direction = Direction.parse(getAttribute(element, "direction"));
            } catch (final IllegalArgumentException except) {
                throw new MissingPropertyException(element, "direction", except);
            }
            if (direction == null) {
                throw new MissingPropertyException(element, "direction");
            }
            map.setRoadLevel(currentTile, direction, getIntegerAttribute(element, "quality"));
            return;
        }
        final Object child = readSPObject(element, parent.getName(), stream, players, warner, idFactory);
        if (child instanceof final River r) {
            map.addRivers(currentTile, r);
        } else if (child instanceof final TileFixture tf) {
            if (tf instanceof final IFortress fort &&
                    map.getFixtures(currentTile).stream()
                            .filter(IFortress.class::isInstance)
                            .map(IFortress.class::cast)
                            .anyMatch(f -> f.owner().equals(fort.owner()))) {
                warner.handle(new UnwantedChildException(parent.getName(), element,
                        "Multiple fortresses owned by same player on same tile"));
            }
            map.addFixture(currentTile, tf);
        } else {
            throw new UnwantedChildException(parent.getName(), element);
        }
    }

    private void parseTile(final IMutableLegacyMap map, final StartElement element, final Iterable<XMLEvent> stream,
						   final IMutablePlayerCollection players, final Warning warner, final IDRegistrar idFactory)
            throws SPFormatException {
        expectAttributes(element, warner, "row", "column", "kind", "type", "mountain");
        final Point loc = new Point(getIntegerAttribute(element, "row"),
                getIntegerAttribute(element, "column"));
        // Tiles have been known to be *written* without "kind" and then fail to load, so
        // let's be liberal in what we accept here, since we can.
        if ((hasAttribute(element, "kind") || hasAttribute(element, "type"))) {
            try {
                map.setBaseTerrain(loc, TileType.parse(getAttrWithDeprecatedForm(element,
                        "kind", "type", warner)));
            } catch (final ParseException except) {
                warner.handle(new MissingPropertyException(element, "kind", except));
            }
        } else {
            warner.handle(new MissingPropertyException(element, "kind"));
        }
        if (getBooleanAttribute(element, "mountain", false)) {
            map.setMountainous(loc, true);
        }
        for (final XMLEvent event : stream) {
            if (event instanceof final StartElement se && isSPStartElement(event)) {
                parseTileChild(map, element, stream, players, warner, idFactory, loc, se);
                continue;
            } else if (event instanceof final EndElement ee && element.getName().equals(ee.getName())) {
                break;
            } else if (event instanceof final Characters c) {
                final String data = c.getData().strip();
                if (!data.isEmpty()) {
                    map.addFixture(loc, new TextFixture(data, -1));
                }
            }
        }
    }

    private void parseElsewhere(final IMutableLegacyMap map, final StartElement element, final Iterable<XMLEvent> stream,
								final IMutablePlayerCollection players, final Warning warner, final IDRegistrar idFactory)
            throws SPFormatException {
        expectAttributes(element, warner);
        final Point loc = Point.INVALID_POINT;
        for (final XMLEvent event : stream) {
            if (event instanceof final StartElement se && isSPStartElement(event)) {
                parseTileChild(map, element, stream, players, warner, idFactory, loc, se);
                continue;
            } else if (event instanceof final EndElement ee && element.getName().equals(ee.getName())) {
                break;
            } else if (event instanceof final Characters c) {
                final String data = c.getData().strip();
                if (!data.isEmpty()) {
                    map.addFixture(loc, new TextFixture(data, -1));
                }
            }
        }
    }

    private IMutableLegacyMap readMapOrViewTag(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
											   final IMutablePlayerCollection players, final Warning warner, final IDRegistrar idFactory)
            throws SPFormatException {
        requireTag(element, parent, "map", "view");
        final int currentTurn;
        final StartElement mapTag;
        switch (element.getName().getLocalPart().toLowerCase()) {
            case "view" -> {
                expectAttributes(element, warner, "current_player", "current_turn");
                currentTurn = getIntegerAttribute(element, "current_turn");
                if (currentTurn >= 0) {
                    MaturityModel.setCurrentTurn(currentTurn);
                }
                mapTag = firstStartElement(stream, element);
                requireTag(mapTag, element.getName(), "map");
                expectAttributes(mapTag, warner, "version", "rows", "columns");
            }
            case "map" -> {
                currentTurn = 0;
                mapTag = element;
                expectAttributes(mapTag, warner, "version", "rows", "columns",
                        "current_player");
            }
            default -> throw new UnwantedChildException(parent, element);
        }
        final MapDimensions dimensions;
        final MapDimensions readDimensions = new MapDimensionsImpl(
                getIntegerAttribute(mapTag, "rows"),
                getIntegerAttribute(mapTag, "columns"),
                getIntegerAttribute(mapTag, "version"));
        if (readDimensions.version() == 2) {
            dimensions = readDimensions;
        } else {
            warner.handle(new MapVersionException(mapTag, readDimensions.version(), 2, 2));
            dimensions = new MapDimensionsImpl(readDimensions.rows(),
                    readDimensions.columns(), 2);
        }
        final Deque<QName> tagStack = new LinkedList<>();
        tagStack.addFirst(element.getName());
        tagStack.addFirst(mapTag.getName());
        final IMutableLegacyMap retval = new LegacyMap(dimensions, players, currentTurn);
        for (final XMLEvent event : stream) {
            final QName stackTop = tagStack.peekFirst();
            if (event instanceof final StartElement se && isSPStartElement(event)) {
                final String type = se.getName().getLocalPart().toLowerCase();
                if ("row".equals(type)) {
                    expectAttributes(se, warner, "index");
                    tagStack.addFirst(se.getName());
                    // Deliberately ignore
                    continue;
                } else if (isFutureTag(se, warner)) {
                    tagStack.addFirst(se.getName());
                    // Deliberately ignore
                    continue;
                } else if ("tile".equals(type)) {
                    parseTile(retval, se, stream, players, warner, idFactory);
                    continue;
                } else if ("elsewhere".equals(type)) {
                    parseElsewhere(retval, se, stream, players, warner, idFactory);
                    continue;
                }
                final Object player = readSPObject(se, stackTop, stream, players, warner, idFactory);
                if (player instanceof final Player p) {
                    retval.addPlayer(p);
                } else {
                    throw new UnwantedChildException(mapTag.getName(), se);
                }
            } else if (event instanceof final EndElement ee) {
                if (ee.getName().equals(stackTop)) {
                    tagStack.removeFirst();
                }
                if (element.getName().equals(ee.getName())) {
                    break;
                }
            } else if (event instanceof final Characters c && !c.getData().isBlank()) {
                warner.handle(UnwantedChildException.childInTag(stackTop,
                        new QName(XMLConstants.NULL_NS_URI, "text"),
                        event.getLocation(),
                        new IllegalStateException("Random text outside any tile")));
            }
        }
        if (hasAttribute(mapTag, "current_player")) {
            retval.setCurrentPlayer(players.getPlayer(
                    getIntegerAttribute(mapTag, "current_player")));
        } else if (hasAttribute(element, "current_player")) {
            retval.setCurrentPlayer(players.getPlayer(
                    getIntegerAttribute(element, "current_player")));
        } else {
            warner.handle(new MissingPropertyException(mapTag, "current_player"));
        }
        retval.setModified(false);
        return retval;
    }

    private static Player readPlayer(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
                                     final IMutablePlayerCollection players, final Warning warner, final IDRegistrar idFactory)
            throws SPFormatException {
        requireTag(element, parent, "player");
        requireNonEmptyAttribute(element, "number", true, warner);
        requireNonEmptyAttribute(element, "code_name", true, warner);
        final String country = getAttribute(element, "country", "");
        expectAttributes(element, warner, "number", "code_name", "country", "portrait");
        // We're thinking about storing "standing orders" in the XML under the <player>
        // tag, and also possibly scientific progress; so as to not require players to
        // upgrade to even read their maps once we start doing so, we *now* only *warn*
        // instead of *dying* if the XML contains that idiom.
        for (final XMLEvent event : stream) {
            if (event instanceof final StartElement se && isSPStartElement(event)) {
                if ("orders".equalsIgnoreCase(se.getName().getLocalPart()) ||
                        "results".equalsIgnoreCase(se.getName().getLocalPart()) ||
                        "science".equalsIgnoreCase(se.getName().getLocalPart())) {
                    warner.handle(new UnwantedChildException(element.getName(), se));
                } else {
                    throw new UnwantedChildException(element.getName(), se);
                }
            } else if (event instanceof final EndElement ee &&
                    element.getName().equals(ee.getName())) {
                break;
            }
        }
        final Player retval;
        if (country.isEmpty()) {
            retval = new PlayerImpl(getIntegerAttribute(element, "number"),
                    getAttribute(element, "code_name"));
        } else {
            retval = new PlayerImpl(getIntegerAttribute(element, "number"),
                    getAttribute(element, "code_name"), country);
        }
        retval.setPortrait(getAttribute(element, "portrait", ""));
        return retval;
    }

    private static void parseOrders(final StartElement element, final IMutableUnit unit, final Iterable<XMLEvent> stream,
                                    final Warning warner) throws SPFormatException {
        expectAttributes(element, warner, "turn");
        final int turn = getIntegerAttribute(element, "turn", -1, warner);
        unit.setOrders(turn, getTextUntil(element.getName(), stream));
    }

    private static void parseResults(final StartElement element, final IMutableUnit unit, final Iterable<XMLEvent> stream,
                                     final Warning warner) throws SPFormatException {
        expectAttributes(element, warner, "turn");
        final int turn = getIntegerAttribute(element, "turn", -1, warner);
        unit.setResults(turn, getTextUntil(element.getName(), stream));
    }

    private IUnit readUnit(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
                           final IMutablePlayerCollection players, final Warning warner, final IDRegistrar idFactory)
            throws SPFormatException {
        requireTag(element, parent, "unit");
        requireNonEmptyAttribute(element, "name", false, warner);
        requireNonEmptyAttribute(element, "owner", false, warner);
        expectAttributes(element, warner, "name", "owner", "id", "kind", "image",
                "portrait", "type");
        String temp = null;
        try {
            temp = getAttrWithDeprecatedForm(element, "kind", "type", warner);
        } catch (final MissingPropertyException except) {
            warner.handle(except);
        }
        final String kind = temp == null ? "" : temp;
        if (kind.isEmpty()) {
            warner.handle(new MissingPropertyException(element, "kind"));
        }
        final Unit retval = setImage(new Unit(
                getPlayerOrIndependent(element, warner, players), kind,
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory)), element, warner);
        retval.setPortrait(getAttribute(element, "portrait", ""));
        final StringBuilder orders = new StringBuilder();
        for (final XMLEvent event : stream) {
            if (event instanceof final StartElement se && isSPStartElement(event)) {
                if ("orders".equalsIgnoreCase(se.getName().getLocalPart())) {
                    parseOrders((StartElement) event, retval, stream, warner);
                    continue;
                } else if ("results".equalsIgnoreCase(se.getName().getLocalPart())) {
                    parseResults((StartElement) event, retval, stream, warner);
                    continue;
                }
                final Object child = readSPObject(se, element.getName(), stream,
                        players, warner, idFactory);
                if (child instanceof final UnitMember um) {
                    retval.addMember(um);
                } else {
                    throw new UnwantedChildException(element.getName(), se);
                }
            } else if (event instanceof final Characters c) {
                orders.append(c.getData());
            } else if (event instanceof final EndElement ee && element.getName().equals(ee.getName())) {
                break;
            }
        }
        final String tempOrders = orders.toString().strip();
        if (!tempOrders.isEmpty()) {
            retval.setOrders(-1, tempOrders);
        }
        return retval;
    }

    private IFortress readFortress(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
                                   final IMutablePlayerCollection players, final Warning warner, final IDRegistrar idFactory)
            throws SPFormatException {
        requireTag(element, parent, "fortress");
        requireNonEmptyAttribute(element, "owner", false, warner);
        requireNonEmptyAttribute(element, "name", false, warner);
        expectAttributes(element, warner, "owner", "name", "id", "size", "status",
                "image", "portrait");
        final TownSize size;
        try {
            size = TownSize.parseTownSize(getAttribute(element, "size", "small"));
        } catch (final IllegalArgumentException except) {
            throw new MissingPropertyException(element, "size", except);
        }
        final IMutableFortress retval = new FortressImpl(
                getPlayerOrIndependent(element, warner, players),
                getAttribute(element, "name", ""),
                getOrGenerateID(element, warner, idFactory), size);
        for (final XMLEvent event : stream) {
            if (event instanceof final StartElement se && isSPStartElement(event)) {
                // We're thinking about storing per-fortress "standing orders" or general
                // regulations, building-progress results, and possibly scientific
                // research progress within fortresses. To ease the transition, we *now*
                // warn, instead of aborting, if the tags we expect to use for this
                // appear in this position in the XML.
                if ("orders".equals(se.getName().getLocalPart()) ||
                        "results".equals(se.getName().getLocalPart()) ||
                        "science".equals(se.getName().getLocalPart())) {
                    warner.handle(new UnwantedChildException(element.getName(), se));
                    continue;
                }
                final Object child = readSPObject(se, element.getName(), stream,
                        players, warner, idFactory);
                if (child instanceof final FortressMember fm) {
                    retval.addMember(fm);
                } else {
                    throw new UnwantedChildException(element.getName(), se);
                }
            } else if (event instanceof final EndElement ee && element.getName().equals(ee.getName())) {
                break;
            }
        }
        retval.setPortrait(getAttribute(element, "portrait", ""));
        return setImage(retval, element, warner);
    }

    private final Map<String, FluidXMLReader<?>> readers;

    public SPFluidReader() {
        final Map<String, FluidXMLReader<?>> temp = new HashMap<>();
        temp.put("adventure", FluidExplorableHandler::readAdventure);
        temp.put("portal", FluidExplorableHandler::readPortal);
        temp.put("cave", FluidExplorableHandler::readCave);
        temp.put("battlefield", FluidExplorableHandler::readBattlefield);
        temp.put("ground", FluidTerrainHandler::readGround);
        temp.put("forest", FluidTerrainHandler::readForest);
        temp.put("animal", UnitMemberHandler::readAnimal);
        temp.put("text", FluidExplorableHandler::readTextFixture);
        temp.put("implement", FluidResourceHandler::readImplement);
        temp.put("resource", FluidResourceHandler::readResource);
        temp.put("cache", FluidResourceHandler::readCache);
        temp.put("grove", FluidResourceHandler::readGrove);
        temp.put("orchard", FluidResourceHandler::readOrchard);
        temp.put("meadow", FluidResourceHandler::readMeadow);
        temp.put("field", FluidResourceHandler::readField);
        temp.put("mine", FluidResourceHandler::readMine);
        temp.put("mineral", FluidResourceHandler::readMineral);
        temp.put("shrub", FluidResourceHandler::readShrub);
        temp.put("stone", FluidResourceHandler::readStone);
        temp.put("worker", UnitMemberHandler::readWorker);
        temp.put("job", UnitMemberHandler::readJob);
        temp.put("skill", UnitMemberHandler::readSkill);
        temp.put("stats", UnitMemberHandler::readStats);
        temp.put("unit", this::readUnit);
        temp.put("fortress", this::readFortress);
        temp.put("town", FluidTownHandler::readTown);
        temp.put("city", FluidTownHandler::readCity);
        temp.put("fortification", FluidTownHandler::readFortification);
        temp.put("village", FluidTownHandler::readVillage);
        temp.put("map", this::readMapOrViewTag);
        temp.put("view", this::readMapOrViewTag);
        temp.put("river", FluidTerrainHandler::readRiver);
        temp.put("lake", FluidTerrainHandler::readLake);
        temp.put("player", SPFluidReader::readPlayer);
        temp.put("population", FluidTownHandler::readCommunityStats);
        for (final SimpleHasKindReader reader : Arrays.asList(
                new SimpleHasKindReader("centaur", Centaur::new),
                new SimpleHasKindReader("dragon", Dragon::new),
                new SimpleHasKindReader("fairy", Fairy::new),
                new SimpleHasKindReader("giant", Giant::new))) {
            temp.put(reader.getPair().getValue0(), reader.getPair().getValue1());
        }
        for (final SimpleFixtureReader reader : Arrays.asList(
                new SimpleFixtureReader("hill", Hill::new),
                new SimpleFixtureReader("oasis", Oasis::new),
                new SimpleFixtureReader("sphinx", Sphinx::new),
                new SimpleFixtureReader("djinn", Djinn::new),
                new SimpleFixtureReader("griffin", Griffin::new),
                new SimpleFixtureReader("minotaur", Minotaur::new),
                new SimpleFixtureReader("ogre", Ogre::new),
                new SimpleFixtureReader("phoenix", Phoenix::new),
                new SimpleFixtureReader("simurgh", Simurgh::new),
                new SimpleFixtureReader("troll", Troll::new),
                new SimpleFixtureReader("snowbird", Snowbird::new),
                new SimpleFixtureReader("thunderbird", Thunderbird::new),
                new SimpleFixtureReader("pegasus", Pegasus::new),
                new SimpleFixtureReader("unicorn", Unicorn::new),
                new SimpleFixtureReader("kraken", Kraken::new))) {
            temp.put(reader.getPair().getValue0(), reader.getPair().getValue1());
        }
        readers = Collections.unmodifiableMap(temp);
    }

    @Override
    public <Type> Type readXML(final Path file, final Reader istream, final Warning warner)
            throws SPFormatException, XMLStreamException, IOException {
        try (final TypesafeXMLEventReader reader = new TypesafeXMLEventReader(istream)) {
            final Iterable<XMLEvent> eventReader = new IteratorWrapper<>(reader);
            final IMutablePlayerCollection players = new PlayerCollection();
            final IDRegistrar idFactory = new IDFactory();
            for (final XMLEvent event : eventReader) {
                if (event instanceof final StartElement se &&
                        isSPStartElement(event)) {
                    return (Type) readSPObject(se, new QName("root"), eventReader, players, warner, idFactory);
                }
            }
        } catch (final IOException except) {
            throw new XMLStreamException(except);
        }
        throw new XMLStreamException("XML stream didn't contain a start element");
    }

    @Override
    public IMutableLegacyMap readMap(final Path file, final Warning warner)
            throws SPFormatException, NoSuchFileException, XMLStreamException, IOException {
        try (final BufferedReader istream = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return readMapFromStream(file, istream, warner);
        } catch (final FileNotFoundException except) {
            final NoSuchFileException wrapper = new NoSuchFileException(file.toString());
            wrapper.initCause(except);
            throw wrapper;
        }
    }

    @Override
    public IMutableLegacyMap readMapFromStream(final Path file, final Reader istream, final Warning warner)
            throws SPFormatException, XMLStreamException, IOException {
        return readXML(file, istream, warner);
    }
}

package impl.xmlio.fluidxml;

import org.javatuples.Pair;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.text.ParseException;
import java.util.stream.StreamSupport;
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
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;

import lovelace.util.IteratorWrapper;
import lovelace.util.MissingFileException;
import lovelace.util.MalformedXMLException;
import lovelace.util.TypesafeXMLEventReader;

import common.idreg.IDRegistrar;
import common.idreg.IDFactory;
import common.map.HasKind;
import common.map.Player;
import common.map.PlayerImpl;
import common.map.MapDimensions;
import common.map.MapDimensionsImpl;
import common.map.Point;
import common.map.TileType;
import common.map.River;
import common.map.IMutablePlayerCollection;
import common.map.TileFixture;
import common.map.PlayerCollection;
import common.map.IMutableMapNG;
import common.map.SPMapNG;
import common.map.Direction;
import common.map.fixtures.FortressMember;
import common.map.fixtures.UnitMember;
import common.map.fixtures.TextFixture;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.mobile.Giant;
import common.map.fixtures.mobile.Centaur;
import common.map.fixtures.mobile.Fairy;
import common.map.fixtures.mobile.Dragon;
import common.map.fixtures.mobile.Ogre;
import common.map.fixtures.mobile.Troll;
import common.map.fixtures.mobile.Sphinx;
import common.map.fixtures.mobile.Phoenix;
import common.map.fixtures.mobile.Griffin;
import common.map.fixtures.mobile.Djinn;
import common.map.fixtures.mobile.Simurgh;
import common.map.fixtures.mobile.Minotaur;
import common.map.fixtures.mobile.Immortal;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.Snowbird;
import common.map.fixtures.mobile.Thunderbird;
import common.map.fixtures.mobile.Pegasus;
import common.map.fixtures.mobile.Unicorn;
import common.map.fixtures.mobile.Kraken;
import common.map.fixtures.mobile.ImmortalAnimal;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.towns.FortressImpl;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.TownSize;
import impl.xmlio.IMapReader;
import impl.xmlio.ISPReader;
import common.xmlio.Warning;
import impl.xmlio.exceptions.UnsupportedTagException;
import impl.xmlio.exceptions.MissingChildException;
import impl.xmlio.exceptions.UnwantedChildException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.MapVersionException;
import static impl.xmlio.fluidxml.FluidBase.*;
import common.xmlio.SPFormatException;

import java.nio.charset.StandardCharsets;

/**
 * The main reader-from-XML class in the 'fluid XML' implementation.
 */
public class SPFluidReader implements IMapReader, ISPReader {
	private Object readSPObject(StartElement element, QName parent,
			Iterable<XMLEvent> stream, IMutablePlayerCollection players, Warning warner,
			IDRegistrar idFactory) throws SPFormatException {
		String namespace = element.getName().getNamespaceURI();
		String tag = element.getName().getLocalPart().toLowerCase();
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

	private static class SimpleFixtureReader {
		private final String tag;
		private final IntFunction<Object> factory;

		public SimpleFixtureReader(String tag, IntFunction<Object> factory) {
			this.tag = tag;
			this.factory = factory;
		}

		public Object reader(StartElement element, QName parent, Iterable<XMLEvent> stream,
				IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) 
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
	private static interface HasKindFactory {
		HasKind apply(String string, int integer);
	}

	private static class SimpleHasKindReader {
		private final String tag;
		private final HasKindFactory factory;
		public SimpleHasKindReader(String tag, HasKindFactory factory) {
			this.tag = tag;
			this.factory = factory;
		}

		public Object reader(StartElement element, QName parent, Iterable<XMLEvent> stream,
				IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) 
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

	private static StartElement firstStartElement(Iterable<XMLEvent> stream, StartElement parent) 
			throws SPFormatException {
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				return (StartElement) event;
			}
		}
		throw new MissingChildException(parent);
	}

	private static boolean isFutureTag(StartElement tag, Warning warner) {
		if (FUTURE_TAGS.contains(tag.getName().getLocalPart().toLowerCase())) {
			warner.handle(UnsupportedTagException.future(tag));
			return true;
		} else {
			return false;
		}
	}

	// TODO: make following methods static if it will compile

	private void parseTileChild(IMutableMapNG map, StartElement parent,
			Iterable<XMLEvent> stream, IMutablePlayerCollection players, Warning warner,
			IDRegistrar idFactory, Point currentTile, StartElement element)
			throws SPFormatException {
		String type = element.getName().getLocalPart().toLowerCase();
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
			Direction direction;
			try {
				direction = Direction.parse(getAttribute(element, "direction"));
			} catch (IllegalArgumentException except) {
				throw new MissingPropertyException(element, "direction", except);
			}
			map.setRoadLevel(currentTile, direction, getIntegerAttribute(element, "quality"));
			return;
		}
		Object child = readSPObject(element, parent.getName(), stream, players, warner, idFactory);
		if (child instanceof River) {
			map.addRivers(currentTile, (River) child);
		} else if (child instanceof TileFixture) {
			if (child instanceof IFortress &&
					StreamSupport.stream(map.getFixtures(currentTile).spliterator(),
							true)
						.filter(IFortress.class::isInstance)
						.map(IFortress.class::cast)
						.anyMatch(f -> f.getOwner()
							.equals(((IFortress) child).getOwner()))) {
				warner.handle(new UnwantedChildException(parent.getName(), element,
					"Multiple fortresses owned by same player on same tile"));
			}
			map.addFixture(currentTile, (TileFixture) child);
		} else {
			throw new UnwantedChildException(parent.getName(), element);
		}
	}

	private void parseTile(IMutableMapNG map, StartElement element, Iterable<XMLEvent> stream,
			IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		expectAttributes(element, warner, "row", "column", "kind", "type");
		Point loc = new Point(getIntegerAttribute(element, "row"),
			getIntegerAttribute(element, "column"));
		// Tiles have been known to be *written* without "kind" and then fail to load, so
		// let's be liberal in what we accept here, since we can.
		if ((hasAttribute(element, "kind") || hasAttribute(element, "type"))) {
			try {
				map.setBaseTerrain(loc, TileType.parse(getAttrWithDeprecatedForm(element,
					"kind", "type", warner)));
			} catch (ParseException except) {
				warner.handle(new MissingPropertyException(element, "kind", except));
			}
		} else {
			warner.handle(new MissingPropertyException(element, "kind"));
		}
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				parseTileChild(map, element, stream, players, warner, idFactory, loc,
					(StartElement) event);
				continue;
			} else if (event instanceof EndElement &&
					element.getName().equals(((EndElement) event).getName())) {
				break;
			} else if (event instanceof Characters) {
				String data = ((Characters) event).getData().trim();
				if (!data.isEmpty()) {
					map.addFixture(loc, new TextFixture(data, -1));
				}
			}
		}
	}

	private void parseElsewhere(IMutableMapNG map, StartElement element, Iterable<XMLEvent> stream,
			IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		expectAttributes(element, warner);
		Point loc = Point.INVALID_POINT;
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				parseTileChild(map, element, stream, players, warner, idFactory, loc,
					(StartElement) event);
				continue;
			} else if (event instanceof EndElement &&
					element.getName().equals(((EndElement) event).getName())) {
				break;
			} else if (event instanceof Characters) {
				String data = ((Characters) event).getData().trim();
				if (!data.isEmpty()) {
					map.addFixture(loc, new TextFixture(data, -1));
				}
			}
		}
	}

	private IMutableMapNG readMapOrViewTag(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		requireTag(element, parent, "map", "view");
		int currentTurn;
		StartElement mapTag;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "view":
			expectAttributes(element, warner, "current_player", "current_turn");
			currentTurn = getIntegerAttribute(element, "current_turn");
			MaturityModel.setCurrentTurn(currentTurn);
			mapTag = firstStartElement(stream, element);
			requireTag(mapTag, element.getName(), "map");
			expectAttributes(mapTag, warner, "version", "rows", "columns");
			break;
		case "map":
			currentTurn = 0;
			mapTag = element;
			expectAttributes(mapTag, warner, "version", "rows", "columns",
				"current_player");
			break;
		default:
			throw new UnwantedChildException(parent, element);
		}
		MapDimensions dimensions;
		MapDimensions readDimensions = new MapDimensionsImpl(
			getIntegerAttribute(mapTag, "rows"),
			getIntegerAttribute(mapTag, "columns"),
			getIntegerAttribute(mapTag, "version"));
		if (readDimensions.getVersion() != 2) {
			warner.handle(new MapVersionException(mapTag, readDimensions.getVersion(), 2, 2));
			dimensions = new MapDimensionsImpl(readDimensions.getRows(),
				readDimensions.getColumns(), 2);
		} else {
			dimensions = readDimensions;
		}
		Deque<QName> tagStack = new LinkedList<>();
		tagStack.addFirst(element.getName());
		tagStack.addFirst(mapTag.getName());
		IMutableMapNG retval = new SPMapNG(dimensions, players, currentTurn);
		for (XMLEvent event : stream) {
			QName stackTop = tagStack.peekFirst();
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				String type = ((StartElement) event).getName().getLocalPart().toLowerCase();
				if ("row".equals(type)) {
					expectAttributes((StartElement) event, warner, "index");
					tagStack.addFirst(((StartElement) event).getName());
					// Deliberately ignore
					continue;
				} else if (isFutureTag((StartElement) event, warner)) {
					tagStack.addFirst(((StartElement) event).getName());
					// Deliberately ignore
					continue;
				} else if ("tile".equals(type)) {
					parseTile(retval, (StartElement) event, stream, players, warner,
						idFactory);
					continue;
				} else if ("elsewhere".equals(type)) {
					parseElsewhere(retval, (StartElement) event, stream, players,
						warner, idFactory);
					continue;
				}
				Object player = readSPObject((StartElement) event, stackTop, stream, players,
					warner, idFactory);
				if (player instanceof Player) {
					retval.addPlayer((Player) player);
				} else {
					throw new UnwantedChildException(mapTag.getName(),
						(StartElement) event);
				}
			} else if (event instanceof EndElement) {
				if (((EndElement) event).getName().equals(stackTop)) {
					tagStack.removeFirst();
				}
				if (element.getName().equals(((EndElement) event).getName())) {
					break;
				}
			} else if (event instanceof Characters &&
					!((Characters) event).getData().trim().isEmpty()) {
				warner.handle(UnwantedChildException.childInTag(stackTop,
					new QName(XMLConstants.NULL_NS_URI, "text"),
					((Characters) event).getLocation(), // TODO: Is cast necessary?
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
		return retval;
	}

	private Player readPlayer(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		requireTag(element, parent, "player");
		requireNonEmptyAttribute(element, "number", true, warner);
		requireNonEmptyAttribute(element, "code_name", true, warner);
		String country = getAttribute(element, "country", "");
		expectAttributes(element, warner, "number", "code_name", "country", "portrait");
		// We're thinking about storing "standing orders" in the XML under the <player>
		// tag, and also possibly scientific progress; so as to not require players to
		// upgrade to even read their maps once we start doing so, we *now* only *warn*
		// instead of *dying* if the XML contains that idiom.
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement(event)) {
				if ("orders".equalsIgnoreCase(((StartElement) event)
							.getName().getLocalPart()) ||
						"results".equalsIgnoreCase(((StartElement) event)
							.getName().getLocalPart()) ||
						"science".equalsIgnoreCase(((StartElement) event)
							.getName().getLocalPart())) {
					warner.handle(new UnwantedChildException(element.getName(),
						(StartElement) event));
				} else {
					throw new UnwantedChildException(element.getName(),
						(StartElement) event);
				}
			} else if (event instanceof EndElement &&
					element.getName().equals(((EndElement) event).getName())) {
				break;
			}
		}
		Player retval = new PlayerImpl(getIntegerAttribute(element, "number"),
			getAttribute(element, "code_name"),
			country.isEmpty() ? null : country);
		retval.setPortrait(getAttribute(element, "portrait", ""));
		return retval;
	}

	private void parseOrders(StartElement element, IMutableUnit unit, Iterable<XMLEvent> stream,
			Warning warner) throws SPFormatException {
		expectAttributes(element, warner, "turn");
		int turn = getIntegerAttribute(element, "turn", -1, warner);
		unit.setOrders(turn, getTextUntil(element.getName(), stream));
	}

	private void parseResults(StartElement element, IMutableUnit unit, Iterable<XMLEvent> stream,
			Warning warner) throws SPFormatException {
		expectAttributes(element, warner, "turn");
		int turn = getIntegerAttribute(element, "turn", -1, warner);
		unit.setResults(turn, getTextUntil(element.getName(), stream));
	}

	private IUnit readUnit(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		requireTag(element, parent, "unit");
		requireNonEmptyAttribute(element, "name", false, warner);
		requireNonEmptyAttribute(element, "owner", false, warner);
		expectAttributes(element, warner, "name", "owner", "id", "kind", "image",
			"portrait", "type");
		String temp = null;
		try {
			temp = getAttrWithDeprecatedForm(element, "kind", "type", warner);
		} catch (MissingPropertyException except) {
			warner.handle(except);
		}
		String kind = temp == null ? "" : temp;
		if (kind.isEmpty()) {
			warner.handle(new MissingPropertyException(element, "kind"));
		}
		Unit retval = setImage(new Unit(
			getPlayerOrIndependent(element, warner, players), kind,
			getAttribute(element, "name", ""),
			getOrGenerateID(element, warner, idFactory)), element, warner);
		retval.setPortrait(getAttribute(element, "portrait", ""));
		StringBuilder orders = new StringBuilder();
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				if ("orders".equalsIgnoreCase(((StartElement) event)
						.getName().getLocalPart())) {
					parseOrders((StartElement) event, retval, stream, warner);
					continue;
				} else if ("results".equalsIgnoreCase(((StartElement) event)
						.getName().getLocalPart())) {
					parseResults((StartElement) event, retval, stream, warner);
					continue;
				}
				Object child = readSPObject((StartElement) event, element.getName(), stream,
					players, warner, idFactory);
				if (child instanceof UnitMember) {
					retval.addMember((UnitMember) child);
				} else {
					throw new UnwantedChildException(element.getName(),
						(StartElement) event);
				}
			} else if (event instanceof Characters) {
				orders.append(((Characters) event).getData());
			} else if (event instanceof EndElement &&
					element.getName().equals(((EndElement) event).getName())) {
				break;
			}
		}
		String tempOrders = orders.toString().trim();
		if (!tempOrders.isEmpty()) {
			retval.setOrders(-1, tempOrders);
		}
		return retval;
	}

	private IFortress readFortress(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IMutablePlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		requireTag(element, parent, "fortress");
		requireNonEmptyAttribute(element, "owner", false, warner);
		requireNonEmptyAttribute(element, "name", false, warner);
		expectAttributes(element, warner, "owner", "name", "id", "size", "status",
			"image", "portrait");
		TownSize size;
		try {
			size = TownSize.parseTownSize(getAttribute(element, "size", "small"));
		} catch (IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		IMutableFortress retval = new FortressImpl(
			getPlayerOrIndependent(element, warner, players),
			getAttribute(element, "name", ""),
			getOrGenerateID(element, warner, idFactory), size);
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				// We're thinking about storing per-fortress "standing orders" or general
				// regulations, building-progress results, and possibly scientific
				// research progress within fortresses. To ease the transition, we *now*
				// warn, instead of aborting, if the tags we expect to use for this
				// appear in this position in the XML.
				if ("orders".equals(((StartElement) event).getName().getLocalPart()) ||
						"results".equals(((StartElement) event)
							.getName().getLocalPart()) ||
						"science".equals(((StartElement) event)
							.getName().getLocalPart())) {
					warner.handle(new UnwantedChildException(element.getName(),
						(StartElement) event));
					continue;
				}
				Object child = readSPObject((StartElement) event, element.getName(), stream,
					players, warner, idFactory);
				if (child instanceof FortressMember) {
					retval.addMember((FortressMember) child);
				} else {
					throw new UnwantedChildException(element.getName(),
						(StartElement) event);
				}
			} else if (event instanceof EndElement &&
					element.getName().equals(((EndElement) event).getName())) {
				break;
			}
		}
		retval.setPortrait(getAttribute(element, "portrait", ""));
		return setImage(retval, element, warner);
	}

	private final Map<String, FluidXMLReader<?>> readers;

	public SPFluidReader() {
		Map<String, FluidXMLReader<?>> temp = new HashMap<>();
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
		temp.put("player", this::readPlayer);
		temp.put("population", FluidTownHandler::readCommunityStats);
		for (SimpleHasKindReader reader : Arrays.asList(
				new SimpleHasKindReader("centaur", Centaur::new),
				new SimpleHasKindReader("dragon", Dragon::new),
				new SimpleHasKindReader("fairy", Fairy::new),
				new SimpleHasKindReader("giant", Giant::new))) {
			temp.put(reader.getPair().getValue0(), reader.getPair().getValue1());
		}
		for (SimpleFixtureReader reader : Arrays.asList(
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
	public <Type> Type readXML(Path file, Reader istream, Warning warner)
			throws SPFormatException, MalformedXMLException {
		try (TypesafeXMLEventReader reader = new TypesafeXMLEventReader(istream)) {
			Iterable<XMLEvent> eventReader = new IteratorWrapper(reader);
			IMutablePlayerCollection players = new PlayerCollection();
			IDRegistrar idFactory = new IDFactory();
			for (XMLEvent event : eventReader) {
				if (event instanceof StartElement &&
						isSPStartElement((StartElement) event)) {
					return (Type) readSPObject((StartElement) event,
						new QName("root"), eventReader, players, warner, idFactory);
				}
			}
		} catch (IOException except) {
			throw new MalformedXMLException(except);
		}
		throw new MalformedXMLException("XML stream didn't contain a start element");
	}

	@Override
	public IMutableMapNG readMap(Path file, Warning warner)
			throws IOException, MissingFileException, SPFormatException, MalformedXMLException {
		try (BufferedReader istream = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			return readMapFromStream(file, istream, warner);
		} catch (FileNotFoundException|NoSuchFileException except) {
			throw new MissingFileException(file, except);
		}
	}

	@Override
	public IMutableMapNG readMapFromStream(Path file, Reader istream, Warning warner)
			throws SPFormatException, MalformedXMLException {
		return this.<IMutableMapNG>readXML(file, istream, warner);
	}
}
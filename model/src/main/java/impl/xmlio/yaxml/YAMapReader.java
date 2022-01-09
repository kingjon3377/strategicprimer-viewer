package impl.xmlio.yaxml;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Characters;
import java.io.IOException;

import lovelace.util.IOConsumer;
import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.Player;
import common.map.MapDimensions;
import common.map.MapDimensionsImpl;
import common.map.Point;
import common.map.River;
import common.map.TileType;
import common.map.IMutablePlayerCollection;
import common.map.TileFixture;
import common.map.IMutableMapNG;
import common.map.IMapNG;
import common.map.SPMapNG;
import common.map.Direction;
import common.map.fixtures.TextFixture;
import common.map.fixtures.Ground;
import common.xmlio.Warning;
import static impl.xmlio.ISPReader.FUTURE_TAGS;
import impl.xmlio.exceptions.MissingChildException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import impl.xmlio.exceptions.UnsupportedTagException;
import impl.xmlio.exceptions.MapVersionException;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.mobile.MaturityModel;
import static common.map.fixtures.mobile.Immortal.IMMORTAL_ANIMALS;
import common.map.fixtures.towns.IFortress;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import lovelace.util.MalformedXMLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * A reader for Strategic Primer maps.
 */
/* package */ class YAMapReader extends YAAbstractReader<IMapNG, IMapNG> {

	/**
	 * @param warner The Warning instance to use
	 * @param idRegistrar The factory for ID numbers
	 * @param players The map's collection of players
	 */
	public YAMapReader(Warning warner, IDRegistrar idRegistrar, IMutablePlayerCollection players) {
		super(warner, idRegistrar);
		playerReader = new YAPlayerReader(warner, idRegistrar);
		readers = Collections.unmodifiableList(Arrays.asList(
			new YAMobileReader(warner, idRegistrar), new YAResourceReader(warner, idRegistrar),
			new YATerrainReader(warner, idRegistrar), new YATextReader(warner, idRegistrar),
			new YATownReader(warner, idRegistrar, players),
			new YAGroundReader(warner, idRegistrar),
			new YAAdventureReader(warner, idRegistrar, players),
			new YAPortalReader(warner, idRegistrar), new YAExplorableReader(warner, idRegistrar),
			new YAUnitReader(warner, idRegistrar, players)));
		this.warner = warner;
		this.players = players;
	}

	private final Warning warner;

	private final IMutablePlayerCollection players; // TODO: IPlayerCollection instead?

	// TODO: Use the one in the Animal maturityModel instead of here?
	private static int currentTurn = -1;

	/**
	 * The reader for players
	 */
	private final YAReader<Player, Player> playerReader;

	/**
	 * The readers we'll try sub-tags on
	 */
	private final List<YAReader<? extends TileFixture, ? extends TileFixture>> readers;

	private final Map<String, YAReader<? extends TileFixture, ? extends TileFixture>>
		readerCache = new HashMap<>();

	private final Map<Class<? extends TileFixture>,
		YAReader<? extends TileFixture, ? extends TileFixture>> writerCache =
			new HashMap<>();

	/**
	 * Get the first open-tag event in our namespace in the stream.
	 */
	private StartElement getFirstStartElement(Iterable<XMLEvent> stream, StartElement parent)
			throws SPFormatException, MalformedXMLException {
		for (XMLEvent element : stream) {
			if (element instanceof StartElement &&
					isSupportedNamespace(((StartElement) element).getName())) {
				return (StartElement) element;
			}
		}
		throw new MissingChildException(parent);
	}

	/**
	 * Write a newline if needed.
	 */
	private void eolIfNeeded(boolean needEol, IOConsumer<String> writer) throws IOException {
		if (needEol) {
			writer.accept(System.lineSeparator());
		}
	}

	/**
	 * Parse a river from XML. The caller is now responsible for advancing
	 * the stream past the closing tag.
	 */
	public River parseRiver(StartElement element, QName parent) throws SPFormatException {
		requireTag(element, parent, "river", "lake");
		if ("lake".equalsIgnoreCase(element.getName().getLocalPart())) {
			expectAttributes(element);
			return River.Lake;
		} else {
			expectAttributes(element, "direction");
			try {
				return River.parse(getParameter(element, "direction"));
			} catch (IllegalArgumentException|ParseException except) {
				throw new MissingPropertyException(element, "direction", except);
			}
		}
	}

	/**
	 * Write a river.
	 */
	public void writeRiver(IOConsumer<String> ostream, River obj, int indent) throws IOException {
		if (River.Lake.equals(obj)) {
			writeTag(ostream, "lake", indent);
		} else {
			writeTag(ostream, "river", indent);
			writeProperty(ostream, "direction", obj.getDescription());
		}
		closeLeafTag(ostream);
	}

	/**
	 * Parse what should be a [[TileFixture]] from the XML.
	 */
	private TileFixture parseFixture(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException, MalformedXMLException {
		String name = element.getName().getLocalPart();
		if (readerCache.containsKey(name.toLowerCase())) {
			return readerCache.get(name.toLowerCase()).read(element, parent, stream);
		}
		for (YAReader<? extends TileFixture, ? extends TileFixture> reader : readers) {
			if (reader.isSupportedTag(name)) {
				readerCache.put(name.toLowerCase(), reader);
				return (TileFixture) reader.read(element, parent, stream);
			}
		}
		if (IMMORTAL_ANIMALS.contains(name.toLowerCase())) {
			if (readerCache.containsKey("animal")) {
				return readerCache.get("animal").read(element, parent, stream);
			} else {
				for (YAReader<? extends TileFixture, ? extends TileFixture> reader : readers) {
					if (reader.isSupportedTag("animal")) {
						return (TileFixture) reader.read(element, parent, stream);
					}
				}
			}
		}
		throw new UnwantedChildException(new QName(element.getName().getNamespaceURI(), "tile"),
			element);
	}

	/**
	 * Read a map from XML.
	 */
	@Override
	public IMutableMapNG read(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException, MalformedXMLException {
		requireTag(element, parent, "map", "view");
		int currentTurn;
		StartElement mapTag;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "view":
			expectAttributes(element, "current_turn", "current_player");
			currentTurn = getIntegerParameter(element, "current_turn");
			MaturityModel.setCurrentTurn(currentTurn);
			mapTag = getFirstStartElement(stream, element);
			requireTag(mapTag, element.getName(), "map");
			expectAttributes(mapTag, "version", "rows", "columns");
			break;
		case "map":
			currentTurn = 0;
			mapTag = element;
			expectAttributes(mapTag, "version", "rows", "columns", "current_player");
			break;
		default:
			throw UnwantedChildException.listingExpectedTags(new QName("xml"), element,
				"map", "view");
		}
		MapDimensions dimensions;
		MapDimensions readDimensions = new MapDimensionsImpl(getIntegerParameter(mapTag, "rows"),
			getIntegerParameter(mapTag, "columns"), getIntegerParameter(mapTag, "version"));
		if (readDimensions.getVersion() == 2) {
			dimensions = readDimensions;
		} else {
			warner.handle(new MapVersionException(mapTag, readDimensions.getVersion(), 2, 2));
			dimensions = new MapDimensionsImpl(readDimensions.getRows(),
				readDimensions.getColumns(), 2);
		}
		final Deque<QName> tagStack = new LinkedList<QName>();
		tagStack.addFirst(element.getName());
		tagStack.addFirst(mapTag.getName());
		final IMutableMapNG retval = new SPMapNG(dimensions, players, currentTurn);
		Point point = null;
		for (XMLEvent event : stream) {
			if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				String type = ((StartElement) event).getName().getLocalPart().toLowerCase();
				if ("player".equals(type)) {
					retval.addPlayer(playerReader.read((StartElement) event,
						tagStack.peekFirst(), stream));
				} else if ("row".equals(type)) {
					expectAttributes((StartElement) event, "index");
					tagStack.addFirst(((StartElement) event).getName());
					// Deliberately ignore "row"
					continue;
				} else if ("tile".equals(type)) {
					if (point != null) {
						throw new UnwantedChildException(tagStack.peekFirst(),
							(StartElement) event);
					}
					expectAttributes((StartElement) event, "row", "column", "kind",
						"type");
					tagStack.addFirst(((StartElement) event).getName());
					// TODO: Just assign to point, maybe?
					final Point localPoint = parsePoint((StartElement) event);
					point = localPoint;
					// Since tiles have sometimes been *written* without "kind", then
					// failed to load, be liberal in waht we accept here.
					if ((hasParameter((StartElement) event, "kind") ||
							hasParameter((StartElement) event, "type"))) {
						try {
							retval.setBaseTerrain(localPoint,
								TileType.parse(getParamWithDeprecatedForm(
									(StartElement) event, "kind",
									"type")));
						} catch (IllegalArgumentException|ParseException except) {
							warner.handle(new MissingPropertyException(
								(StartElement) event, "kind", except));
						}
					} else {
						warner.handle(new MissingPropertyException(
							(StartElement) event, "kind"));
					}
				} else if ("elsewhere".equals(type)) {
					if (point != null) {
						throw new UnwantedChildException(tagStack.peekFirst(),
							(StartElement) event);
					}
					expectAttributes((StartElement) event);
					tagStack.addFirst(((StartElement) event).getName());
					point = Point.INVALID_POINT;
				} else if (FUTURE_TAGS.contains(type)) {
					tagStack.addFirst(((StartElement) event).getName());
					warner.handle(UnsupportedTagException.future((StartElement) event));
				} else if ("sandbar".equals(type)) {
					tagStack.addFirst(((StartElement) event).getName());
					warner.handle(UnsupportedTagException.obsolete((StartElement) event));
				} else if (point != null) {
					if ("lake".equals(type) || "river".equals(type)) {
						retval.addRivers(point,
							parseRiver((StartElement) event,
								tagStack.peekFirst()));
						spinUntilEnd(((StartElement) event).getName(), stream);
					} else if ("mountain".equals(type)) {
						tagStack.addFirst(((StartElement) event).getName());
						retval.setMountainous(point, true);
					} else if ("bookmark".equals(type)) {
						tagStack.addFirst(((StartElement) event).getName());
						expectAttributes((StartElement) event, "player");
						retval.addBookmark(point,
							players.getPlayer(getIntegerParameter(
								(StartElement) event, "player")));
					} else if ("road".equals(type)) {
						tagStack.addFirst(((StartElement) event).getName());
						expectAttributes((StartElement) event, "direction",
							"quality");
						Direction direction;
						try {
							direction = Direction.parse(
								getParameter((StartElement) event,
									"direction"));
						} catch (IllegalArgumentException except) {
							throw new MissingPropertyException(
								(StartElement) event, "direction", except);
						}
						retval.setRoadLevel(point, direction,
							getIntegerParameter((StartElement) event, "quality"));
					} else {
						QName top = tagStack.peekFirst(); // TODO: probably remove
						TileFixture child = parseFixture((StartElement) event,
							tagStack.peekFirst(), stream);
						if (child instanceof IFortress &&
								retval.getFixtures(point).stream()
									.filter(IFortress.class::isInstance)
									.map(IFortress.class::cast)
									.map(IFortress::getOwner)
									.anyMatch(((IFortress) child).getOwner()::equals)) {
							warner.handle(new UnwantedChildException(
								tagStack.peekFirst(), (StartElement) event,
								"Multiple fortresses owned by one player on a tile"));
						}
						retval.addFixture(point, child);
					}
				} else {
					// fixture outside tile
					throw UnwantedChildException.listingExpectedTags(
						tagStack.peekFirst(), (StartElement) event, "tile",
						"elsewhere");
				}
			} else if (event instanceof EndElement) {
				if (!tagStack.isEmpty() &&
						tagStack.peekFirst().equals(((EndElement) event).getName())) {
					tagStack.removeFirst();
				} // **NOT** else if!
				if (element.getName().equals(((EndElement) event).getName())) {
					break;
				} else if ("tile".equalsIgnoreCase(
							((EndElement) event).getName().getLocalPart()) ||
					"elsewhere".equalsIgnoreCase(
						((EndElement) event).getName().getLocalPart())) {
					point = null;
				}
			} else if (event instanceof Characters) {
				String data = ((Characters) event).getData().trim();
				if (!data.isEmpty()) {
					retval.addFixture(point == null ? Point.INVALID_POINT : point,
						new TextFixture(data, -1));
				}
			}
		}
		if (hasParameter(mapTag, "current_player")) {
			retval.setCurrentPlayer(players.getPlayer(getIntegerParameter(mapTag,
				"current_player")));
		} else if (hasParameter(element, "current_player")) {
			retval.setCurrentPlayer(players.getPlayer(getIntegerParameter(element,
				"current_player")));
		} else {
			warner.handle(new MissingPropertyException(mapTag, "current_player"));
		}
		return retval;
	}

	/**
	 * Write a child object
	 */
	private void writeChild(IOConsumer<String> ostream, TileFixture child, int tabs) 
			throws IOException {
		Class<? extends TileFixture> cls = child.getClass();
		if (writerCache.containsKey(cls)) {
			writerCache.get(cls).writeRaw(ostream, child, tabs);
			return;
		}
		for (YAReader<? extends TileFixture, ? extends TileFixture> reader : readers) {
			if (reader.canWrite(child)) {
				writerCache.put(cls, reader);
				reader.writeRaw(ostream, child, tabs);
				return;
			}
		}
		throw new IllegalStateException(String.format(
			"After checking %d readers, don't know how to write a(n) %s",
			readers.size(), cls.getName()));
	}

	// FIXME: Probably remove, as we won't use in the Java port
	private boolean validPoint(Map.Entry<Point, TileFixture> entry) {
		return entry.getKey().isValid();
	}

	/**
	 * Write a map.
	 */
	@Override
	public void write(IOConsumer<String> ostream, IMapNG obj, int tabs) throws IOException {
		writeTag(ostream, "view", tabs);
		writeProperty(ostream, "current_player", obj.getCurrentPlayer().getPlayerId());
		writeProperty(ostream, "current_turn", obj.getCurrentTurn());
		currentTurn = obj.getCurrentTurn();
		finishParentTag(ostream);
		writeTag(ostream, "map", tabs + 1);
		MapDimensions dimensions = obj.getDimensions();
		writeProperty(ostream, "version", dimensions.getVersion());
		writeProperty(ostream, "rows", dimensions.getRows());
		writeProperty(ostream, "columns", dimensions.getColumns());
		finishParentTag(ostream);
		for (Player player : obj.getPlayers()) {
			playerReader.write(ostream, player, tabs + 2);
		}
		for (int i = 0; i < dimensions.getRows(); i++) {
			boolean rowEmpty = true;
			for (int j = 0; j < dimensions.getColumns(); j++) {
				Point loc = new Point(i, j);
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
					if (terrain != null) {
						writeProperty(ostream, "kind", terrain.getXml());
					}
					ostream.accept(">");
					boolean needEol = true;
					for (Player player : obj.getAllBookmarks(loc)) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeTag(ostream, "bookmark", tabs + 4);
						writeProperty(ostream, "player", player.getPlayerId());
						closeLeafTag(ostream);
					}
					if (obj.isMountainous(loc)) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeTag(ostream, "mountain", tabs + 4);
						closeLeafTag(ostream);
					}
					// Rivers are automatically sorted, coming from an EnumSet
					for (River river : obj.getRivers(loc)) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeRiver(ostream, river, tabs + 4);
					}
					// Roads are automatically sorted by direction, coming from an EnumMap
					for (Map.Entry<Direction, Integer> entry :
							obj.getRoads(loc).entrySet()) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeTag(ostream, "road", tabs + 4);
						writeProperty(ostream, "direction",
							entry.getKey().toString());
						writeProperty(ostream, "quality", entry.getValue());
						closeLeafTag(ostream);
					}
					// To avoid breaking map-format-conversion tests, and to
					// avoid churn in existing maps, put the first Ground and Forest
					// before other fixtures.
					Ground ground = StreamSupport.stream(
							obj.getFixtures(loc).spliterator(), false)
						.filter(Ground.class::isInstance).map(Ground.class::cast)
						.findFirst().orElse(null);
					if (ground != null) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeChild(ostream, ground, tabs + 4);
					}
					Forest forest = StreamSupport.stream(
							obj.getFixtures(loc).spliterator(), false)
						.filter(Forest.class::isInstance).map(Forest.class::cast)
						.findFirst().orElse(null);
					if (forest != null) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeChild(ostream, forest, tabs + 4);
					}
					for (TileFixture fixture : obj.getFixtures(loc)) {
						if (fixture.equals(ground) || fixture.equals(forest)) {
							continue;
						}
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
		if (StreamSupport.stream(obj.getLocations().spliterator(), true)
				.filter(((Predicate<Point>) Point::isValid).negate())
				.map(obj::getFixtures)
				.anyMatch(((Predicate<Collection<TileFixture>>) Collection::isEmpty)
					.negate())) {
			writeTag(ostream, "elsewhere", tabs + 2);
			finishParentTag(ostream);
			for (TileFixture fixture : StreamSupport.stream(
					obj.getLocations().spliterator(), true)
					.filter(((Predicate<Point>) Point::isValid).negate())
					.flatMap(p -> obj.getFixtures(p).stream())
					.collect(Collectors.toList())) {
				writeChild(ostream, fixture, tabs + 3);
			}
			closeTag(ostream, tabs + 2, "elsewhere");
		}
		closeTag(ostream, tabs + 1, "map");
		closeTag(ostream, tabs, "view");
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "map".equalsIgnoreCase(tag) || "view".equalsIgnoreCase(tag);
	}

	@Override
	public boolean canWrite(Object obj) {
		return obj instanceof IMapNG;
	}
}
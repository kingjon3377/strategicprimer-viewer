package legacy.xmlio.yaxml;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Characters;
import java.io.IOException;

import legacy.map.IMutableLegacyPlayerCollection;
import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import legacy.idreg.IDRegistrar;
import legacy.map.Player;
import legacy.map.MapDimensions;
import legacy.map.MapDimensionsImpl;
import legacy.map.Point;
import legacy.map.River;
import legacy.map.TileType;
import legacy.map.TileFixture;
import legacy.map.IMutableLegacyMap;
import legacy.map.ILegacyMap;
import legacy.map.LegacyMap;
import legacy.map.Direction;
import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.Ground;
import common.xmlio.Warning;

import static impl.xmlio.ISPReader.FUTURE_TAGS;

import impl.xmlio.exceptions.MissingChildException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import impl.xmlio.exceptions.UnsupportedTagException;
import impl.xmlio.exceptions.MapVersionException;
import legacy.map.fixtures.terrain.Forest;
import common.map.fixtures.mobile.MaturityModel;

import static legacy.map.fixtures.mobile.Immortal.IMMORTAL_ANIMALS;

import legacy.map.fixtures.towns.IFortress;

import java.util.List;
import java.text.ParseException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A reader for Strategic Primer maps.
 */
/* package */ class YAMapReader extends YAAbstractReader<ILegacyMap, ILegacyMap> {

	/**
	 * @param warner      The Warning instance to use
	 * @param idRegistrar The factory for ID numbers
	 * @param players     The map's collection of players
	 */
	public YAMapReader(final Warning warner, final IDRegistrar idRegistrar, final IMutableLegacyPlayerCollection players) {
		super(warner, idRegistrar);
		playerReader = new YAPlayerReader(warner, idRegistrar);
		readers = List.of(new YAMobileReader(warner, idRegistrar), new YAResourceReader(warner, idRegistrar), new YATerrainReader(warner, idRegistrar), new YATextReader(warner, idRegistrar), new YATownReader(warner, idRegistrar, players), new YAGroundReader(warner, idRegistrar), new YAAdventureReader(warner, idRegistrar, players), new YAPortalReader(warner, idRegistrar), new YAExplorableReader(warner, idRegistrar), new YAUnitReader(warner, idRegistrar, players));
		this.warner = warner;
		this.players = players;
	}

	private final Warning warner;

	private final IMutableLegacyPlayerCollection players; // TODO: ILegacyPlayerCollection instead?

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
	private static StartElement getFirstStartElement(final Iterable<XMLEvent> stream, final StartElement parent)
			throws SPFormatException, XMLStreamException {
		for (final XMLEvent element : stream) {
			if (element instanceof final StartElement se && isSupportedNamespace(se.getName())) {
				return se;
			}
		}
		throw new MissingChildException(parent);
	}

	/**
	 * Write a newline if needed.
	 */
	private static void eolIfNeeded(final boolean needEol, final ThrowingConsumer<String, IOException> writer) throws IOException {
		if (needEol) {
			writer.accept(System.lineSeparator());
		}
	}

	/**
	 * Parse a river from XML. The caller is now responsible for advancing
	 * the stream past the closing tag.
	 */
	public River parseRiver(final StartElement element, final QName parent) throws SPFormatException {
		requireTag(element, parent, "river", "lake");
		if ("lake".equalsIgnoreCase(element.getName().getLocalPart())) {
			expectAttributes(element);
			return River.Lake;
		} else {
			expectAttributes(element, "direction");
			try {
				return River.parse(getParameter(element, "direction"));
			} catch (final IllegalArgumentException | ParseException except) {
				throw new MissingPropertyException(element, "direction", except);
			}
		}
	}

	/**
	 * Write a river.
	 */
	public static void writeRiver(final ThrowingConsumer<String, IOException> ostream, final River obj, final int indent) throws IOException {
		if (River.Lake == obj) {
			writeTag(ostream, "lake", indent);
		} else {
			writeTag(ostream, "river", indent);
			writeProperty(ostream, "direction", obj.getDescription());
		}
		closeLeafTag(ostream);
	}

	/**
	 * Parse what should be a {@link TileFixture} from the XML.
	 */
	private TileFixture parseFixture(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		final String name = element.getName().getLocalPart();
		if (readerCache.containsKey(name.toLowerCase())) {
			return readerCache.get(name.toLowerCase()).read(element, parent, stream);
		}
		for (final YAReader<? extends TileFixture, ? extends TileFixture> reader : readers) {
			if (reader.isSupportedTag(name)) {
				readerCache.put(name.toLowerCase(), reader);
				return reader.read(element, parent, stream);
			}
		}
		if (IMMORTAL_ANIMALS.contains(name.toLowerCase())) {
			if (readerCache.containsKey("animal")) {
				return readerCache.get("animal").read(element, parent, stream);
			} else {
				for (final YAReader<? extends TileFixture, ? extends TileFixture> reader : readers) {
					if (reader.isSupportedTag("animal")) {
						return reader.read(element, parent, stream);
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
	@SuppressWarnings("ChainOfInstanceofChecks")
	@Override
	public IMutableLegacyMap read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		requireTag(element, parent, "map", "view");
		final int currentTurn;
		final StartElement mapTag;
		switch (element.getName().getLocalPart().toLowerCase()) {
			case "view" -> {
				expectAttributes(element, "current_turn", "current_player");
				currentTurn = getIntegerParameter(element, "current_turn");
				if (currentTurn >= 0) {
					MaturityModel.setCurrentTurn(currentTurn);
				}
				mapTag = getFirstStartElement(stream, element);
				requireTag(mapTag, element.getName(), "map");
				expectAttributes(mapTag, "version", "rows", "columns");
			}
			case "map" -> {
				currentTurn = 0;
				mapTag = element;
				expectAttributes(mapTag, "version", "rows", "columns", "current_player");
			}
			default -> throw UnwantedChildException.listingExpectedTags(new QName("xml"), element,
					"map", "view");
		}
		final MapDimensions dimensions;
		final MapDimensions readDimensions = new MapDimensionsImpl(getIntegerParameter(mapTag, "rows"),
				getIntegerParameter(mapTag, "columns"), getIntegerParameter(mapTag, "version"));
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
		Point point = null;
		final Predicate<Object> isFortress = IFortress.class::isInstance;
		final Function<Object, IFortress> fortressCast = IFortress.class::cast;
		for (final XMLEvent event : stream) {
			// switch would require break-to-label
			//noinspection IfCanBeSwitch
			if (event instanceof final StartElement se && isSupportedNamespace(se.getName())) {
				final String type = se.getName().getLocalPart().toLowerCase();
				if ("player".equals(type)) {
					retval.addPlayer(playerReader.read(se, tagStack.peekFirst(), stream));
				} else if ("row".equals(type)) {
					expectAttributes(se, "index");
					tagStack.addFirst(se.getName());
					// Deliberately ignore "row"
					continue;
				} else if ("tile".equals(type)) {
					if (!Objects.isNull(point)) {
						throw new UnwantedChildException(tagStack.peekFirst(), se);
					}
					expectAttributes(se, "row", "column", "kind",
							"type", "mountain");
					tagStack.addFirst(se.getName());
					// TODO: Just assign to point, maybe?
					final Point localPoint = parsePoint(se);
					point = localPoint;
					// Since tiles have sometimes been *written* without "kind", then
					// failed to load, be liberal in what we accept here.
					if ((hasParameter(se, "kind") || hasParameter(se, "type"))) {
						try {
							retval.setBaseTerrain(localPoint,
									TileType.parse(getParamWithDeprecatedForm(se, "kind",
											"type")));
						} catch (final IllegalArgumentException | ParseException except) {
							warner.handle(new MissingPropertyException(se, "kind", except));
						}
					} else {
						warner.handle(new MissingPropertyException(se, "kind"));
					}
					if (getBooleanParameter(se, "mountain", false)) {
						retval.setMountainous(localPoint, true);
					}
				} else if ("elsewhere".equals(type)) {
					if (!Objects.isNull(point)) {
						throw new UnwantedChildException(tagStack.peekFirst(), se);
					}
					expectAttributes(se);
					tagStack.addFirst(se.getName());
					point = Point.INVALID_POINT;
				} else if (FUTURE_TAGS.contains(type)) {
					tagStack.addFirst(se.getName());
					warner.handle(UnsupportedTagException.future(se));
				} else if ("sandbar".equals(type)) {
					tagStack.addFirst(se.getName());
					warner.handle(UnsupportedTagException.obsolete(se));
				} else if (!Objects.isNull(point)) {
					switch (type) {
						case "lake", "river" -> {
							retval.addRivers(point,
									parseRiver(se, tagStack.peekFirst()));
							spinUntilEnd(se.getName(), stream);
						}
						case "mountain" -> {
							tagStack.addFirst(se.getName());
							retval.setMountainous(point, true);
						}
						case "bookmark" -> {
							tagStack.addFirst(se.getName());
							expectAttributes(se, "player");
							retval.addBookmark(point,
									players.getPlayer(getIntegerParameter(
											se, "player")));
						}
						case "road" -> {
							tagStack.addFirst(se.getName());
							expectAttributes(se, "direction", "quality");
							final Direction direction;
							try {
								direction = Direction.parse(
										getParameter(se,
												"direction"));
							} catch (final IllegalArgumentException except) {
								throw new MissingPropertyException(se, "direction", except);
							}
							retval.setRoadLevel(point, direction,
									getIntegerParameter(se, "quality"));
						}
						default -> {
							final QName top = tagStack.peekFirst();
							final TileFixture child = parseFixture(se, top, stream);
							if (child instanceof final IFortress f &&
									retval.getFixtures(point).stream()
											.filter(isFortress)
											.map(fortressCast)
											.map(IFortress::owner)
											.anyMatch(Predicate.isEqual(f.owner()))) {
								warner.handle(new UnwantedChildException(top, se,
										"Multiple fortresses owned by one player on a tile"));
							}
							retval.addFixture(point, child);
						}
					}
				} else {
					// fixture outside tile
					throw UnwantedChildException.listingExpectedTags(
							tagStack.peekFirst(), se, "tile",
							"elsewhere");
				}
			} else if (event instanceof final EndElement ee) {
				if (!tagStack.isEmpty() &&
						tagStack.peekFirst().equals(ee.getName())) {
					tagStack.removeFirst();
				} // **NOT** else if!
				if (element.getName().equals(ee.getName())) {
					break;
				} else if ("tile".equalsIgnoreCase(ee.getName().getLocalPart()) ||
						"elsewhere".equalsIgnoreCase(ee.getName().getLocalPart())) {
					point = null;
				}
			} else if (event instanceof final Characters c) {
				final String data = c.getData().strip();
				if (!data.isEmpty()) {
					retval.addFixture(Objects.requireNonNullElse(point, Point.INVALID_POINT),
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
		retval.setModified(false);
		return retval;
	}

	/**
	 * Write a child object
	 */
	private void writeChild(final ThrowingConsumer<String, IOException> ostream, final TileFixture child, final int tabs)
			throws IOException {
		final Class<? extends TileFixture> cls = child.getClass();
		if (writerCache.containsKey(cls)) {
			writerCache.get(cls).writeRaw(ostream, child, tabs);
			return;
		}
		for (final YAReader<? extends TileFixture, ? extends TileFixture> reader : readers) {
			if (reader.canWrite(child)) {
				writerCache.put(cls, reader);
				reader.writeRaw(ostream, child, tabs);
				return;
			}
		}
		throw new IllegalStateException("After checking %d readers, don't know how to write a(n) %s".formatted(
				readers.size(), cls.getName()));
	}

	/**
	 * Write a map.
	 */
	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final ILegacyMap obj, final int tabs) throws IOException {
		writeTag(ostream, "view", tabs);
		writeProperty(ostream, "current_player", obj.getCurrentPlayer().getPlayerId());
		writeProperty(ostream, "current_turn", obj.getCurrentTurn());
		finishParentTag(ostream);
		writeTag(ostream, "map", tabs + 1);
		final MapDimensions dimensions = obj.getDimensions();
		writeProperty(ostream, "version", dimensions.version());
		writeProperty(ostream, "rows", dimensions.rows());
		writeProperty(ostream, "columns", dimensions.columns());
		finishParentTag(ostream);
		for (final Player player : obj.getPlayers()) {
			playerReader.write(ostream, player, tabs + 2);
		}
		final Predicate<Object> isGround = Ground.class::isInstance;
		final Function<Object, Ground> groundCast = Ground.class::cast;
		final Predicate<Object> isForest = Forest.class::isInstance;
		final Function<Object, Forest> forestCast = Forest.class::cast;
		for (int i = 0; i < dimensions.rows(); i++) {
			boolean rowEmpty = true;
			for (int j = 0; j < dimensions.columns(); j++) {
				final Point loc = new Point(i, j);
				final TileType terrain = obj.getBaseTerrain(loc);
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
					if (!Objects.isNull(terrain)) {
						writeProperty(ostream, "kind", terrain.getXml());
					}
					ostream.accept(">");
					boolean needEol = true;
					for (final Player player : obj.getAllBookmarks(loc)) {
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
					for (final River river : obj.getRivers(loc)) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeRiver(ostream, river, tabs + 4);
					}
					// Roads are automatically sorted by direction, coming from an EnumMap
					for (final Map.Entry<Direction, Integer> entry :
							obj.getRoads(loc).entrySet()) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeTag(ostream, "road", tabs + 4);
						writeProperty(ostream, "direction",
								entry.getKey().toString());
						writeProperty(ostream, "quality", entry.getValue());
						closeLeafTag(ostream);
					}
					// TODO: Instead of special-casing ground and forest, and to minimize future churn with exploration, sort fixtures in some way.
					// To avoid breaking map-format-conversion tests, and to
					// avoid churn in existing maps, put the first Ground and Forest
					// before other fixtures.
					final Ground ground = obj.getFixtures(loc).stream()
							.filter(isGround).map(groundCast)
							.findFirst().orElse(null);
					if (!Objects.isNull(ground)) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeChild(ostream, ground, tabs + 4);
					}
					final Forest forest = obj.getFixtures(loc).stream()
							.filter(isForest).map(forestCast)
							.findFirst().orElse(null);
					if (!Objects.isNull(forest)) {
						eolIfNeeded(needEol, ostream);
						needEol = false;
						writeChild(ostream, forest, tabs + 4);
					}
					for (final TileFixture fixture : obj.getFixtures(loc)) {
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
		if (obj.streamLocations().filter(((Predicate<Point>) Point::isValid).negate())
				.map(obj::getFixtures)
				.anyMatch(((Predicate<Collection<TileFixture>>) Collection::isEmpty)
						.negate())) {
			writeTag(ostream, "elsewhere", tabs + 2);
			finishParentTag(ostream);
			for (final TileFixture fixture : obj.streamLocations()
					.filter(((Predicate<Point>) Point::isValid).negate())
					.flatMap(p -> obj.getFixtures(p).stream()).toList()) {
				writeChild(ostream, fixture, tabs + 3);
			}
			closeTag(ostream, tabs + 2, "elsewhere");
		}
		closeTag(ostream, tabs + 1, "map");
		closeTag(ostream, tabs, "view");
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "map".equalsIgnoreCase(tag) || "view".equalsIgnoreCase(tag);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof ILegacyMap;
	}
}

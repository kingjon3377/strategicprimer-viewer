package controller.map.fluidxml;

import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedPropertyException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import controller.map.misc.IDRegistrar;
import controller.map.misc.IncludingIterator;
import controller.map.misc.TypesafeXMLEventReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.StreamSupport;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasKind;
import model.map.IMutableMapNG;
import model.map.IMutablePlayerCollection;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Ground;
import model.map.fixtures.Implement;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Phoenix;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Sphinx;
import model.map.fixtures.mobile.Troll;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.TownSize;
import org.eclipse.jdt.annotation.NonNull;
import util.IteratorWrapper;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttrWithDeprecatedForm;
import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.getPlayerOrIndependent;
import static controller.map.fluidxml.XMLHelper.getTextUntil;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.isSPStartElement;
import static controller.map.fluidxml.XMLHelper.requireNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static model.map.TileType.getTileType;
import static util.EqualsAny.equalsAny;
import static util.NullCleaner.assertNotNull;

/**
 * The main reader-from-XML class in the 'fluid XML' implementation.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SPFluidReader implements IMapReader, ISPReader, FluidXMLReader {
	/**
	 * The collection of readers, mapped to the tags they read.
	 */
	private final Map<String, FluidXMLReader> readers = new HashMap<>();

	/**
	 * Constructor. Set up readers.
	 */
	public SPFluidReader() {
		readers.put("adventure", FluidExplorableHandler::readAdventure);
		readers.put("portal", FluidExplorableHandler::readPortal);
		readers.put("cave", FluidExplorableHandler::readCave);
		readers.put("battlefield", FluidExplorableHandler::readBattlefield);
		readers.put("ground", FluidTerrainHandler::readGround);
		readers.put("forest", FluidTerrainHandler::readForest);
		createSimpleFixtureReader("hill", Hill::new);
		readers.put("mountain", FluidTerrainHandler::readMountain);
		createSimpleFixtureReader("oasis", Oasis::new);
		createSimpleFixtureReader("sandbar", Sandbar::new);
		createSimpleFixtureReader("djinn", Djinn::new);
		createSimpleFixtureReader("griffin", Griffin::new);
		createSimpleFixtureReader("minotaur", Minotaur::new);
		createSimpleFixtureReader("ogre", Ogre::new);
		createSimpleFixtureReader("phoenix", Phoenix::new);
		createSimpleFixtureReader("simurgh", Simurgh::new);
		createSimpleFixtureReader("sphinx", Sphinx::new);
		createSimpleFixtureReader("troll", Troll::new);
		readers.put("animal", FluidUnitMemberHandler::readAnimal);
		createSimpleHasKindReader("centaur", Centaur::new);
		createSimpleHasKindReader("dragon", Dragon::new);
		createSimpleHasKindReader("fairy", Fairy::new);
		createSimpleHasKindReader("giant", Giant::new);
		readers.put("text", FluidExplorableHandler::readTextFixture);
		createSimpleHasKindReader("implement", Implement::new);
		readers.put("resource", FluidResourceHandler::readResource);
		readers.put("cache", FluidResourceHandler::readCache);
		readers.put("grove", FluidResourceHandler::readGrove);
		readers.put("orchard", FluidResourceHandler::readOrchard);
		readers.put("meadow", FluidResourceHandler::readMeadow);
		readers.put("field", FluidResourceHandler::readField);
		readers.put("mine", FluidResourceHandler::readMine);
		readers.put("mineral", FluidResourceHandler::readMineral);
		readers.put("shrub", FluidResourceHandler::readShrub);
		readers.put("stone", FluidResourceHandler::readStone);
		readers.put("worker", FluidUnitMemberHandler::readWorker);
		readers.put("job", FluidUnitMemberHandler::readJob);
		readers.put("skill", FluidUnitMemberHandler::readSkill);
		readers.put("stats", FluidUnitMemberHandler::readStats);
		readers.put("unit", this::readUnit);
		readers.put("fortress", this::readFortress);
		readers.put("town", FluidTownHandler::readTown);
		readers.put("city", FluidTownHandler::readCity);
		readers.put("fortification", FluidTownHandler::readFortification);
		readers.put("village", FluidTownHandler::readVillage);
		readers.put("map", this::readMap);
		readers.put("view", this::readMap);
		readers.put("river", FluidTerrainHandler::readRiver);
		readers.put("lake", FluidTerrainHandler::readLake);
		readers.put("player", SPFluidReader::readPlayer);
	}

	/**
	 * @param stream a stream of XMLEvents
	 * @param parent the parent tag
	 * @return the first start-element in the stream
	 * @throws SPFormatException if no start element in stream
	 */
	private static StartElement getFirstStartElement(final Iterable<XMLEvent> stream,
													 final StartElement parent)
			throws SPFormatException {
		final StartElement retval = StreamSupport
											.stream(stream.spliterator(), false)
											.filter(XMLHelper::isSPStartElement)
											.map(XMLEvent::asStartElement)
											.findFirst()
											.orElseThrow(
													() -> new MissingChildException
																  (parent));
		assert retval != null;
		return retval;
	}

	/**
	 * Read a player from XML. This is here because it's not a good fit for any of the
	 * other classes that collect related methods.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@SuppressWarnings("UnusedParameters")
	private static Player readPlayer(final StartElement element,
									 final QName parent,
									 final Iterable<XMLEvent> stream,
									 final IMutablePlayerCollection players,
									 final Warning warner,
									 final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "player");
		requireNonEmptyAttribute(element, "number", true, warner);
		requireNonEmptyAttribute(element, "code_name", true, warner);
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return new Player(getIntegerAttribute(element, "number"),
								 getAttribute(element, "code_name"));
	}

	/**
	 * @param <T>     A supertype of the object the XML represents
	 * @param file    the file we're reading from
	 * @param istream the stream to read from
	 * @param type    the type of the object the caller wants
	 * @param warner  the Warning instance to use for warnings
	 * @return the wanted object
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException  on SP XML format error
	 */
	@Override
	public <@NonNull T> T readXML(final Path file, final Reader istream,
								  final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final Iterator<XMLEvent> reader = new TypesafeXMLEventReader(istream);
		final Iterable<XMLEvent> eventReader =
				new IteratorWrapper<>(new IncludingIterator(file, reader));
		final IMutablePlayerCollection players = new PlayerCollection();
		final IDRegistrar idFactory = new IDFactory();
		for (final XMLEvent event : eventReader) {
			if (isSPStartElement(event)) {
				final Object retval = readSPObject(
						assertNotNull(event.asStartElement()), new QName("root"),
						eventReader, players, warner, idFactory);
				if (type.isAssignableFrom(retval.getClass())) {
					//noinspection unchecked
					return (T) retval;
				} else {
					throw new IllegalStateException("Reader produced different type than" +
															" we expected");
				}
			}
		}
		throw new XMLStreamException("XML stream didn't contain a start element");
	}

	/**
	 * @param file   the file to read from
	 * @param warner a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws IOException        on I/O error
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException  on SP format problems
	 */
	@Override
	public IMutableMapNG readMap(final Path file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		try (final Reader istream = Files.newBufferedReader(file)) {
			return readMap(file, istream, warner);
		}
	}

	/**
	 * @param file    the file we're reading from
	 * @param istream the stream to read from
	 * @param warner  a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException  on SP format problems
	 */
	@Override
	public IMutableMapNG readMap(final Path file, final Reader istream,
								 final Warning warner)
			throws XMLStreamException, SPFormatException {
		return readXML(file, istream, SPMapNG.class, warner);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactXMLReader";
	}

	@Override
	public Object readSPObject(final StartElement element,
							   final QName parent,
							   final Iterable<XMLEvent> stream,
							   final IMutablePlayerCollection players,
							   final Warning warner,
							   final IDRegistrar idFactory)
			throws SPFormatException, IllegalArgumentException {
		final String namespace = element.getName().getNamespaceURI();
		final String tag = element.getName().getLocalPart().toLowerCase();
		if (namespace.isEmpty() || NAMESPACE.equals(namespace)) {
			if (readers.containsKey(tag)) {
				return assertNotNull(readers.get(tag))
							   .readSPObject(element, parent, stream, players, warner,
									   idFactory);
			} else {
				throw new UnsupportedTagException(element);
			}
		} else {
			throw new UnsupportedTagException(element);
		}
	}

	/**
	 * Create a reader for a simple object having only an ID number and maybe an image,
	 * and add this reader to our collection.
	 *
	 * @param tag     the tag this class should be instantiated from
	 * @param factory the constructor to create an object of the class. Must take the ID
	 *                number in its constructor, and nothing else.
	 */
	private void createSimpleFixtureReader(final String tag, final IntFunction<?>
																	 factory) {
		readers.put(tag, (element, parent, stream, players, warner, idFactory) -> {
			requireTag(element, parent, tag);
			spinUntilEnd(assertNotNull(element.getName()), stream);
			return setImage(factory.apply(getOrGenerateID(element, warner, idFactory)),
					element, warner);
		});
	}

	/**
	 * Create a reader for a simple object having a kind, an ID number, and maybe an
	 * image, and add this reader to our collection.
	 *
	 * @param tag     the tag this class should be instantiated from
	 * @param factory the constructor to create an object of the class. Must take the
	 *                "kind" parameter and the ID number in its constructor, in that
	 *                order, and nothing else.
	 */
	private void createSimpleHasKindReader(final String tag, final HasKindFactory
																	 factory) {
		readers.put(tag, (element, parent, stream, players, warner, idFactory) -> {
			requireTag(element, parent, tag);
			spinUntilEnd(assertNotNull(element.getName()), stream);
			return setImage(factory.create(getAttribute(element, "kind"),
					getOrGenerateID(element, warner, idFactory)), element, warner);
		});
	}

	/**
	 * Read a Unit from XML. This is here to avoid a circular dependency between whatever
	 * class it would be in and this class.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed unit
	 * @throws SPFormatException on SP format problem
	 */
	private Unit readUnit(final StartElement element, final QName parent,
						  final Iterable<XMLEvent> stream,
						  final IMutablePlayerCollection players, final Warning warner,
						  final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "unit");
		requireNonEmptyAttribute(element, "name", false, warner);
		requireNonEmptyAttribute(element, "owner", false, warner);
		String kind;
		try {
			kind = getAttrWithDeprecatedForm(element, "kind", "type", warner);
		} catch (final MissingPropertyException except) {
			warner.warn(except);
			kind = "";
		}
		if (kind.isEmpty()) {
			warner.warn(new MissingPropertyException(element, "kind"));
		}
		final Unit retval =
				setImage(new Unit(getPlayerOrIndependent(element, warner, players),
										 kind, getAttribute(element, "name", ""),
										 getOrGenerateID(element, warner, idFactory)),
						element, warner);
		retval.setPortrait(getAttribute(element, "portrait", ""));
		final StringBuilder orders = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (isSPStartElement(event)) {
				if ("orders".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					parseOrders(event.asStartElement(), retval,
							stream);
					continue;
				} else if ("results".equalsIgnoreCase(
						event.asStartElement().getName().getLocalPart())) {
					parseResults(event.asStartElement(), retval,
							stream);
					continue;
				}
				final Object child =
						readSPObject(event.asStartElement(), element.getName(), stream,
								players, warner, idFactory);
				if (child instanceof UnitMember) {
					retval.addMember((UnitMember) child);
				} else {
					throw new UnwantedChildException(element.getName(),
															event.asStartElement());
				}
			} else if (event.isCharacters()) {
				orders.append(event.asCharacters().getData());
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		final String tempOrders = orders.toString().trim();
		if (!tempOrders.isEmpty()) {
			retval.setOrders(-1, assertNotNull(tempOrders));
		}
		return retval;
	}

	/**
	 * Parse orders for a unit for a specified turn.
	 *
	 * @param element the orders element
	 * @param unit    the unit to whom these orders are directed
	 * @param stream  the stream of further tags.
	 * @throws SPFormatException on SP format problem
	 */
	private static void parseOrders(final StartElement element,
							 final IUnit unit,
							 final Iterable<XMLEvent> stream) throws SPFormatException {
		final int turn = getIntegerAttribute(element, "turn", -1);
		unit.setOrders(turn, getTextUntil(element.getName(), stream));
	}

	/**
	 * Parse results for a unit for a specified turn.
	 *
	 * @param element the results element
	 * @param unit    the unit to whom these orders are directed
	 * @param stream  the stream of further tags.
	 * @throws SPFormatException on SP format problem
	 */
	private static void parseResults(final StartElement element,
							  final IUnit unit,
							  final Iterable<XMLEvent> stream) throws SPFormatException {
		final int turn = getIntegerAttribute(element, "turn", -1);
		unit.setResults(turn, getTextUntil(element.getName(), stream));
	}

	/**
	 * Parse a fortress.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed fortress
	 * @throws SPFormatException on SP format problems
	 */
	@SuppressWarnings("UnusedParameters")
	private Fortress readFortress(final StartElement element,
								  final QName parent,
								  final Iterable<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final Warning warner,
								  final IDRegistrar idFactory)
			throws SPFormatException {
		requireNonEmptyAttribute(element, "owner", false, warner);
		requireNonEmptyAttribute(element, "name", false, warner);
		final Fortress retval =
				new Fortress(getPlayerOrIndependent(element, warner, players),
									getAttribute(element, "name", ""),
									getOrGenerateID(element, warner, idFactory),
									TownSize.parseTownSize(
											getAttribute(element, "size", "small")));
		for (final XMLEvent event : stream) {
			if (isSPStartElement(event)) {
				final Object child =
						readSPObject(event.asStartElement(), element.getName(), stream,
								players, warner, idFactory);
				if (child instanceof FortressMember) {
					retval.addMember((FortressMember) child);
				} else {
					throw new UnwantedChildException(assertNotNull(element.getName()),
															assertNotNull(
																	event.asStartElement
																				  ()));
				}
			} else if (event.isEndElement()
							   &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {
				break;
			}
		}
		retval.setPortrait(getAttribute(element, "portrait", ""));
		return setImage(retval, element, warner);
	}

	/**
	 * Parse a map from XML.
	 *
	 * @param element   the element being parsed
	 * @param parent    the parent tag
	 * @param stream    the stream of further XML elements
	 * @param players   the collection of players
	 * @param warner    to use to report format irregularities
	 * @param idFactory to register ID numbers or get new ones
	 * @return the parsed map
	 * @throws SPFormatException on format error
	 */
	private IMutableMapNG readMap(final StartElement element,
								  final QName parent,
								  final Iterable<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final Warning warner, final IDRegistrar
																idFactory)
			throws SPFormatException {
		requireTag(element, parent, "map", "view");
		final int currentTurn;
		final StartElement mapTag;
		final String outerTag = assertNotNull(element.getName().getLocalPart());
		if ("view".equalsIgnoreCase(outerTag)) {
			currentTurn = getIntegerAttribute(element, "current_turn");
			mapTag = getFirstStartElement(stream, element);
			requireTag(mapTag, element.getName(), "map");
		} else if ("map".equalsIgnoreCase(outerTag)) {
			currentTurn = 0;
			mapTag = element;
		} else {
			throw new UnwantedChildException(new QName("xml"), element);
		}
		final MapDimensions dimensions =
				new MapDimensions(getIntegerAttribute(mapTag, "rows"),
										 getIntegerAttribute(mapTag, "columns"),
										 getIntegerAttribute(mapTag, "version"));
		final Deque<QName> tagStack = new LinkedList<>();
		tagStack.push(element.getName());
		tagStack.push(mapTag.getName());
		final IMutableMapNG retval = new SPMapNG(dimensions, players, currentTurn);
		for (final XMLEvent event : stream) {
			if (isSPStartElement(event)) {
				final StartElement current = event.asStartElement();
				final String type = current.getName().getLocalPart();
				if ("row".equals(type)) {
					tagStack.push(current.getName());
					// Deliberately ignore
					continue;
				} else if ("tile".equals(type)) {
					parseTile(retval, current, stream, players, warner, idFactory);
					continue;
				} else if (equalsAny(type, ISPReader.FUTURE)) {
					// noinspection ObjectAllocationInLoop
					warner.warn(new UnsupportedTagException(current));
					continue;
				}
				final Object child =
						readSPObject(current, tagStack.peek(), stream, players, warner,
								idFactory);
				if (child instanceof Player) {
					retval.addPlayer((Player) child);
				} else {
					throw new UnwantedChildException(mapTag.getName(), current);
				}
			} else if (event.isEndElement()) {
				if (!tagStack.isEmpty() &&
							tagStack.peek().equals(event.asEndElement().getName())) {
					tagStack.pop();
				}
				if (element.getName().equals(event.asEndElement().getName())) {
					break;
				}
			} else if (event.isCharacters()) {
				final String data =
						assertNotNull(event.asCharacters().getData().trim());
				if (!data.isEmpty()) {
					//noinspection ObjectAllocationInLoop
					warner.warn(
							new UnwantedChildException(tagStack.peek(),
															  new QName(NULL_NS_URI,
																			   "text"),
															  event.getLocation(),
															  new IllegalStateException
																	  ("Random text " +
																			   "outside " +
																			   "any " +
																			   "tile")));
				}
			}
		}
		if (hasAttribute(mapTag, "current_player")) {
			retval.setCurrentPlayer(
					players.getPlayer(getIntegerAttribute(mapTag, "current_player")));
		} else if (hasAttribute(element, "current_player")) {
			retval.setCurrentPlayer(
					players.getPlayer(getIntegerAttribute(element, "current_player")));
		}
		return retval;
	}

	/**
	 * Parse the contents of a tile. There are no Tile objects in the current model
	 * framework, but this is cleaner than using a marker variable in the map-reading
	 * loop.
	 *
	 * @param map       the map being read
	 * @param element   the tag currently being read
	 * @param stream    the stream to read more tags from
	 * @param players   the collection of players
	 * @param warner    to report format irregularities
	 * @param idFactory to get ID numbers when needed, and report numbers in use
	 * @throws SPFormatException on malformed or invalid-in-our-domain XML
	 */
	private void parseTile(final IMutableMapNG map,
						   final StartElement element,
						   final Iterable<XMLEvent> stream,
						   final IMutablePlayerCollection players, final Warning warner,
						   final IDRegistrar idFactory) throws SPFormatException {
		final Point point = PointFactory.point(getIntegerAttribute(element, "row"),
				getIntegerAttribute(element, "column"));
		// Tiles have been known to be *written* without "kind" and then fail to load, so
		// let's be liberal in what we accept here, since we can.
		if (hasAttribute(element, "kind") || hasAttribute(element, "type")) {
			map.setBaseTerrain(point, getTileType(
					getAttrWithDeprecatedForm(element, "kind", "type", warner)));
		} else {
			warner.warn(new MissingPropertyException(element, "kind"));
		}
		for (final XMLEvent event : stream) {
			if (isSPStartElement(event)) {
				final StartElement current = event.asStartElement();
				final String type = current.getName().getLocalPart();
				if (equalsAny(type, ISPReader.FUTURE)) {
					//noinspection ObjectAllocationInLoop
					warner.warn(new UnsupportedTagException(current));
					continue;
				} else if ("tile".equals(type)) {
					throw new UnwantedChildException(element.getName(), current);
				}
				final Object child =
						readSPObject(current, element.getName(), stream, players, warner,
								idFactory);
				if (child instanceof River) {
					map.addRivers(point, (River) child);
				} else if (child instanceof Mountain) {
					map.setMountainous(point, true);
				} else if (child instanceof Ground) {
					final Ground ground = (Ground) child;
					final Ground oldGround = map.getGround(point);
					if (oldGround == null) {
						map.setGround(point, ground);
					} else if (ground.isExposed() && !oldGround.isExposed()) {
						map.setGround(point, ground);
						map.addFixture(point, oldGround);
					} else if (!oldGround.equals(ground)) {
						// TODO: Should we do additional ordering of Ground?
						map.addFixture(point, ground);
					}
				} else if (child instanceof Forest) {
					final Forest forest = (Forest) child;
					final Forest oldForest = map.getForest(point);
					if (oldForest == null) {
						map.setForest(point, forest);
					} else if (!oldForest.equals(forest)) {
						// TODO: Should we do additional ordering of Forests?
						map.addFixture(point, forest);
					}
				} else if (child instanceof RiverFixture) {
					for (final River river : (RiverFixture) child) {
						map.addRivers(point, river);
					}
				} else if ((child instanceof StoneDeposit) &&
								   (StoneKind.Laterite ==
											((StoneDeposit) child)
													.stone()) &&
								   (TileType.Jungle !=
											map.getBaseTerrain(
													point))) {
					//noinspection ObjectAllocationInLoop
					warner.warn(new UnsupportedPropertyException(current,
																		"laterite"));
					map.addFixture(point, (TileFixture) child);
				} else if (child instanceof TileFixture) {
					map.addFixture(point, (TileFixture) child);
				} else {
					throw new UnwantedChildException(element.getName(), current);
				}
			} else if (event.isEndElement() &&
							   element.getName().equals(event.asEndElement().getName()
							   )) {

				break;
			} else if (event.isCharacters()) {
				final String data = event.asCharacters().getData().trim();
				if (!data.isEmpty()) {
					//noinspection ObjectAllocationInLoop
					map.addFixture(point, new TextFixture(data, -1));
				}
			}
		}
	}

	/**
	 * An interface for constructors and factory methods to create simple HasKind
	 * instances.
	 */
	@FunctionalInterface
	private interface HasKindFactory {
		/**
		 * The constructor or factory method.
		 *
		 * @param kind  the "kind" property of the object
		 * @param idNum the object's ID #
		 * @return the object
		 */
		HasKind create(String kind, int idNum);
	}
}

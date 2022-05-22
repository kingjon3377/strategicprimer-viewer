package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.EndElement;
import java.io.IOException;

import lovelace.util.LovelaceLogger;
import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.IPlayerCollection;
import common.map.Player;
import common.map.fixtures.mobile.worker.RaceFactory;
import common.map.fixtures.towns.FortressImpl;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.ITownFixture;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.City;
import common.map.fixtures.towns.Fortification;
import common.map.fixtures.towns.Town;
import common.map.fixtures.towns.CommunityStats;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import java.util.Random;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.Collections;
import java.util.Arrays;
import common.map.fixtures.IResourcePile;
import java.util.List;
import common.map.fixtures.FortressMember;
import java.util.Optional;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * A reader for fortresses, villages, and other towns.
 */
/* package */ class YATownReader extends YAAbstractReader<ITownFixture, ITownFixture> {
	public YATownReader(final Warning warner, final IDRegistrar idRegistrar, final IPlayerCollection players) {
		super(warner, idRegistrar);
		resourceReader = new YAResourcePileReader(warner, idRegistrar);
		memberReaders = List.of(new YAUnitReader(warner,
				idRegistrar, players), resourceReader, new YAImplementReader(warner, idRegistrar));
		this.players = players;
		this.warner = warner;
	}

	private final Warning warner;

	private final IPlayerCollection players;

	private final YAReader<IResourcePile, IResourcePile> resourceReader;

	private final List<YAReader<? extends FortressMember, ? extends FortressMember>> memberReaders;

	/**
	 * If the tag has an "owner" parameter, return the player it indicates;
	 * otherwise trigger a warning and return the "independent" player.
	 */
	private Player getOwnerOrIndependent(final StartElement element) throws SPFormatException {
		if (hasParameter(element, "owner")) {
			return players.getPlayer(getIntegerParameter(element, "owner"));
		} else {
			warner.handle(new MissingPropertyException(element, "owner"));
			return players.getIndependent();
		}
	}

	private static List<String> expectedCommunityStatsTags(final String parent) {
		return switch (parent) {
			case "population" -> Arrays.asList("expertise", "claim", "production", "consumption");
			case "claim", "expertise" -> Collections.emptyList();
			case "production", "consumption" -> Collections.singletonList("resource");
			default -> throw new IllegalArgumentException("Impossible CommunityStats parent tag");
		};
	}

	public CommunityStats parseCommunityStats(final StartElement element, final QName parent,
	                                          final Iterable<XMLEvent> stream) throws SPFormatException, XMLStreamException {
		requireTag(element, parent, "population");
		expectAttributes(element, "size");
		final CommunityStats retval = new CommunityStats(getIntegerParameter(element, "size"));
		String current = null;
		final Deque<StartElement> stack = new LinkedList<>();
		stack.addFirst(element);
		for (final XMLEvent event : stream) {
			if (event instanceof EndElement &&
					((EndElement) event).getName().equals(element.getName())) {
				break;
			} else if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				switch (((StartElement) event).getName().getLocalPart().toLowerCase()) {
				case "expertise":
					if (current == null) {
						expectAttributes((StartElement) event, "skill", "level");
						retval.setSkillLevel(getParameter((StartElement) event,
								"skill"),
							getIntegerParameter((StartElement) event, "level"));
						stack.addFirst((StartElement) event);
						current = ((StartElement) event).getName().getLocalPart();
					} else {
						throw UnwantedChildException.listingExpectedTags(
							stack.peekFirst().getName(), (StartElement) event,
								expectedCommunityStatsTags(current).toArray(new String[0]));
					}
					break;
				case "claim":
					if (current == null) {
						expectAttributes((StartElement) event, "resource");
						retval.addWorkedField(getIntegerParameter(
							(StartElement) event, "resource"));
						stack.addFirst((StartElement) event);
						current = ((StartElement) event).getName().getLocalPart();
					} else {
						throw UnwantedChildException.listingExpectedTags(
							stack.peekFirst().getName(), (StartElement) event,
								expectedCommunityStatsTags(current).toArray(new String[0]));
					}
					break;
				case "production":
				case "consumption":
					if (current == null) {
						expectAttributes((StartElement) event);
						stack.addFirst((StartElement) event);
						current = ((StartElement) event).getName().getLocalPart();
					} else {
						throw UnwantedChildException.listingExpectedTags(
								stack.peekFirst().getName(), (StartElement) event,
								expectedCommunityStatsTags(current).toArray(new String[0]));
					}
					break;
				case "resource":
					final Consumer<IResourcePile> lambda;
					if ("production".equals(current)) {
						lambda = retval.getYearlyProduction()::add;
					} else if ("consumption".equals(current)) {
						lambda = retval.getYearlyConsumption()::add;
					} else {
						throw UnwantedChildException.listingExpectedTags(
							stack.peekFirst().getName(), (StartElement) event,
								expectedCommunityStatsTags(current == null ?
										                           "population" : current).toArray(new String[0]));
					}
					lambda.accept(resourceReader.read((StartElement) event,
						stack.peekFirst().getName(), stream));
					break;
				default:
					throw UnwantedChildException.listingExpectedTags(
						stack.isEmpty() ? element.getName() :
							stack.peekFirst().getName(),
						(StartElement) event,
							expectedCommunityStatsTags(current == null ?
									                           "population" : current).toArray(new String[0]));
				}
			} else if (event instanceof EndElement && !stack.isEmpty() &&
					((EndElement) event).getName()
						.equals(stack.peekFirst().getName())) {
				final StartElement top = stack.removeFirst();
				if (top.equals(element)) {
					break;
				} else if (current != null &&
						top.getName().getLocalPart().equals(current)) {
					if ("population".equals(stack.peekFirst().getName()
							.getLocalPart())) {
						current = null;
					} else {
						current = stack.peekFirst().getName().getLocalPart();
					}
				}
			}
		}
		return retval;
	}

	private ITownFixture parseVillage(final StartElement element, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		expectAttributes(element, "status", "name", "race", "image", "portrait", "id", "owner");
		requireNonEmptyParameter(element, "name", false);
		final int idNum = getOrGenerateID(element);
		final TownStatus status;
		try {
			status = TownStatus.parse(getParameter(element, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		final Village retval = new Village(status, getParameter(element, "name", ""), idNum,
			getOwnerOrIndependent(element), getParameter(element, "race",
				RaceFactory.randomRace(new Random(idNum))));
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				if (retval.getPopulation() == null) {
					retval.setPopulation(parseCommunityStats((StartElement) event,
							element.getName(), stream));
				} else {
					throw new UnwantedChildException(element.getName(),
							(StartElement) event);
				}
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		return retval;
	}

	private ITownFixture parseTown(final StartElement element, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		expectAttributes(element, "name", "status", "size", "dc", "id", "image", "owner",
			"portrait");
		requireNonEmptyParameter(element, "name", false);
		final String name = getParameter(element, "name", "");
		final TownStatus status;
		try {
			status = TownStatus.parse(getParameter(element, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		final TownSize size;
		try {
			size = TownSize.parseTownSize(getParameter(element, "size"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		final int dc = getIntegerParameter(element, "dc");
		final int id = getOrGenerateID(element);
		final Player owner = getOwnerOrIndependent(element);
		final AbstractTown retval = switch (element.getName().getLocalPart().toLowerCase()) {
			case "town" -> new Town(status, size, dc, name, id, owner);
			case "city" -> new City(status, size, dc, name, id, owner);
			case "fortification" -> new Fortification(status, size, dc, name, id, owner);
			default -> throw new IllegalArgumentException("Unhandled town tag " +
					                                              element.getName().getLocalPart());
		};
		for (final XMLEvent event : stream) {
			if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				if (retval.getPopulation() == null) {
					retval.setPopulation(parseCommunityStats((StartElement) event,
							element.getName(), stream));
				} else {
					throw new UnwantedChildException(element.getName(),
							(StartElement) event);
				}
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		return retval;
	}

	private ITownFixture parseFortress(final StartElement element, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		expectAttributes(element, "owner", "name", "size", "status", "id", "portrait", "image");
		requireNonEmptyParameter(element, "owner", false);
		requireNonEmptyParameter(element, "name", false);
		final IMutableFortress retval;
		final TownSize size;
		try {
			size = TownSize.parseTownSize(getParameter(element, "size", "small"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		retval = new FortressImpl(getOwnerOrIndependent(element),
			getParameter(element, "name", ""), getOrGenerateID(element), size);
		for (final XMLEvent event : stream) {
			if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				final String memberTag =
					((StartElement) event).getName().getLocalPart().toLowerCase();
				final Optional<YAReader<? extends FortressMember, ? extends FortressMember>>
					reader = memberReaders.stream()
						.filter(yar -> yar.isSupportedTag(memberTag)).findAny();
				if (reader.isPresent()) {
					retval.addMember(reader.get().read((StartElement) event,
						element.getName(), stream));
				} else if ("orders".equals(memberTag) || "results".equals(memberTag) ||
						"science".equals(memberTag)) {
					// We're thinking about storing per-fortress "standing orders" or
					// general regulations, building-progress results, and possibly
					// scientific research progress within fortresses. To ease the
					// transition, we *now* warn, instead of aborting, if the tags we
					// expect to use for this appear in this position in the XML.
					warner.handle(new UnwantedChildException(element.getName(),
						(StartElement) event));
					continue;
				} else {
					throw new UnwantedChildException(element.getName(),
						(StartElement) event);
				}
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		return retval;
	}

	private void writeAbstractTown(final ThrowingConsumer<String, IOException> ostream, final AbstractTown obj, final int tabs)
			throws IOException {
		writeTag(ostream, obj.getKind(), tabs);
		writeProperty(ostream, "status", obj.getStatus().toString());
		writeProperty(ostream, "size", obj.getTownSize().toString());
		writeProperty(ostream, "dc", obj.getDC());
		writeNonemptyProperty(ostream, "name", obj.getName());
		writeProperty(ostream, "id", obj.getId());
		writeProperty(ostream, "owner", obj.getOwner().getPlayerId());
		writeImageXML(ostream, obj);
		writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
		if (obj.getPopulation() == null) {
			closeLeafTag(ostream);
		} else {
			finishParentTag(ostream);
			writeCommunityStats(ostream, obj.getPopulation(), tabs + 1);
			closeTag(ostream, tabs, obj.getKind());
		}
	}

	public void writeCommunityStats(final ThrowingConsumer<String, IOException> ostream, final CommunityStats obj, final int tabs)
			throws IOException {
		writeTag(ostream, "population", tabs);
		writeProperty(ostream, "size", obj.getPopulation());
		finishParentTag(ostream);
		for (final Map.Entry<String, Integer> entry : obj.getHighestSkillLevels().entrySet().stream()
				.sorted(Map.Entry.comparingByKey(Comparator.naturalOrder())).collect(Collectors.toList())) {
			writeTag(ostream, "expertise", tabs + 1);
			writeProperty(ostream, "skill", entry.getKey());
			writeProperty(ostream, "level", entry.getValue());
			closeLeafTag(ostream);
		}
		for (final Integer claim : obj.getWorkedFields()) {
			writeTag(ostream, "claim", tabs + 1);
			writeProperty(ostream, "resource", claim);
			closeLeafTag(ostream);
		}
		if (!obj.getYearlyProduction().isEmpty()) {
			writeTag(ostream, "production", tabs + 1);
			finishParentTag(ostream);
			for (final IResourcePile resource : obj.getYearlyProduction()) {
				resourceReader.write(ostream, resource, tabs + 2);
			}
			closeTag(ostream, tabs + 1, "production");
		}
		if (!obj.getYearlyConsumption().isEmpty()) {
			writeTag(ostream, "consumption", tabs + 1);
			finishParentTag(ostream);
			for (final IResourcePile resource : obj.getYearlyConsumption()) {
				resourceReader.write(ostream, resource, tabs + 2);
			}
			closeTag(ostream, tabs + 1, "consumption");
		}
		closeTag(ostream, tabs, "population");
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return Arrays.asList("village", "fortress", "town", "city", "fortification")
			.contains(tag.toLowerCase());
	}

	@Override
	public ITownFixture read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		requireTag(element, parent, "village", "fortress", "town", "city", "fortification");
		return switch (element.getName().getLocalPart().toLowerCase()) {
			case "village" -> parseVillage(element, stream);
			case "fortress" -> parseFortress(element, stream);
			default -> parseTown(element, stream);
		};
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final ITownFixture obj, final int tabs) throws IOException {
		if (obj instanceof AbstractTown) {
			writeAbstractTown(ostream, (AbstractTown) obj, tabs);
		} else if (obj instanceof Village) {
			writeTag(ostream, "village", tabs);
			writeProperty(ostream, "status", obj.getStatus().toString());
			writeNonemptyProperty(ostream, "name", obj.getName());
			writeProperty(ostream, "id", obj.getId());
			writeProperty(ostream, "owner", obj.getOwner().getPlayerId());
			writeProperty(ostream, "race", ((Village) obj).getRace());
			writeImageXML(ostream, (Village) obj);
			writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
			if (obj.getPopulation() == null) {
				closeLeafTag(ostream);
			} else {
				finishParentTag(ostream);
				writeCommunityStats(ostream, obj.getPopulation(), tabs + 1);
				closeTag(ostream, tabs, "village");
			}
		} else if (obj instanceof IFortress) {
			writeTag(ostream, "fortress", tabs);
			writeProperty(ostream, "owner", obj.getOwner().getPlayerId());
			writeNonemptyProperty(ostream, "name", obj.getName());
			if (TownSize.Small != obj.getTownSize()) {
				writeProperty(ostream, "size", obj.getTownSize().toString());
			}
			writeProperty(ostream, "id", obj.getId());
			writeImageXML(ostream, (IFortress) obj);
			writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
			ostream.accept(">");
			if (((IFortress) obj).iterator().hasNext()) {
				ostream.accept(System.lineSeparator());
				for (final FortressMember member : (IFortress) obj) {
					final Optional<YAReader<? extends FortressMember, ? extends FortressMember>>
						reader = memberReaders.stream()
							.filter(yar -> yar.canWrite(member)).findAny();
					if (reader.isPresent()) {
						reader.get().writeRaw(ostream, member, tabs + 1);
					} else {
						LovelaceLogger.error("Unhandled FortressMember type %s",
							member.getClass().getName());
					}
				}
				indent(ostream, tabs);
			}
			ostream.accept("</fortress>");
			ostream.accept(System.lineSeparator());
		} else {
			throw new IllegalArgumentException("Unhandled town type");
		}
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof ITownFixture;
	}
}

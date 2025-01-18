package legacy.xmlio.fluidxml;

import common.map.fixtures.mobile.worker.RaceFactory;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.TownStatus;
import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.ILegacyPlayerCollection;
import legacy.map.Player;
import legacy.map.fixtures.IMutableResourcePile;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.towns.AbstractTown;
import legacy.map.fixtures.towns.City;
import legacy.map.fixtures.towns.CommunityStats;
import legacy.map.fixtures.towns.CommunityStatsImpl;
import legacy.map.fixtures.towns.Fortification;
import legacy.map.fixtures.towns.Town;
import legacy.map.fixtures.towns.Village;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

/* package */ class FluidTownHandler extends FluidBase {
	private static final QName NULL_QNAME = new QName("null");

	private static interface TownContructor<T> {
		T construct(final TownStatus townStatus, final TownSize size, final int discoverDC,
		                  final String townName, final int id, final Player player);
	}
	@SuppressWarnings("ChainOfInstanceofChecks")
	private static <T extends AbstractTown> T readAbstractTown(final TownContructor<T> factory, final String tag,
	                                                           final StartElement element, final @Nullable Path path,
	                                                           final QName parent, final Iterable<XMLEvent> stream,
	                                                           final ILegacyPlayerCollection players,
	                                                           final Warning warner,
	                                                           final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, path, parent, tag);
		expectAttributes(element, path, warner, "name", "size", "status", "dc", "id",
				"portrait", "image", "owner");
		requireNonEmptyAttribute(element, path, "name", false, warner);
		final TownSize size;
		try {
			size = TownSize.parseTownSize(getAttribute(element, path, "size"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "size", except);
		}
		final TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, path, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "status", except);
		}
		final T fix = factory.construct(status, size, getIntegerAttribute(element, path, "dc"),
				getAttribute(element, "name", ""),
				getOrGenerateID(element, warner, path, idFactory),
				getPlayerOrIndependent(element, path, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			// switch would require break-to-label
			//noinspection IfCanBeSwitch
			if (event instanceof final StartElement se && isSPStartElement(event)) {
				if (Objects.isNull(fix.getPopulation())) {
					fix.setPopulation(readCommunityStats(se, path,
							element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(), se, path);
				}
			} else if (event instanceof final EndElement ee && ee.getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(fix, element, path, warner);
	}

	public static Town readTown(final StartElement element, final @Nullable Path path, final QName parent,
	                            final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                            final Warning warner, final IDRegistrar idFactory) throws SPFormatException {
		return readAbstractTown(Town::new, "town", element, path, parent, stream, players, warner, idFactory);
	}

	public static Fortification readFortification(final StartElement element, final @Nullable Path path,
	                                              final QName parent, final Iterable<XMLEvent> stream,
												  final ILegacyPlayerCollection players, final Warning warner,
												  final IDRegistrar idFactory) throws SPFormatException {
		return readAbstractTown(Fortification::new, "fortification", element, path, parent, stream, players, warner,
				idFactory);
	}

	public static City readCity(final StartElement element, final @Nullable Path path, final QName parent,
	                            final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                            final Warning warner, final IDRegistrar idFactory) throws SPFormatException {
		return readAbstractTown(City::new, "city", element, path, parent, stream, players, warner, idFactory);
	}

	@SuppressWarnings("ChainOfInstanceofChecks")
	public static Village readVillage(final StartElement element, final @Nullable Path path, final QName parent,
	                                  final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                                  final Warning warner, final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, path, parent, "village");
		expectAttributes(element, path, warner, "status", "race", "owner", "id", "image",
				"portrait", "name");
		requireNonEmptyAttribute(element, path, "name", false, warner);
		final int idNum = getOrGenerateID(element, warner, path, idFactory);
		final TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, path, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "status", except);
		}
		final Village retval = new Village(status, getAttribute(element, "name", ""), idNum,
				getPlayerOrIndependent(element, path, warner, players),
				getAttribute(element, "race",
						RaceFactory.randomRace(new Random(idNum))));
		retval.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			// switch would require break-to-label
			//noinspection IfCanBeSwitch
			if (event instanceof final StartElement se && isSPStartElement(event)) {
				if (Objects.isNull(retval.getPopulation())) {
					retval.setPopulation(readCommunityStats(se, path,
							element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(), se, path);
				}
			} else if (event instanceof final EndElement ee && ee.getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(retval, element, path, warner);
	}

	@SuppressWarnings("ChainOfInstanceofChecks")
	public static CommunityStats readCommunityStats(final StartElement element, final @Nullable Path path,
	                                                final QName parent, final Iterable<XMLEvent> stream,
													final ILegacyPlayerCollection players, final Warning warner,
													final IDRegistrar idFactory) throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "population");
		expectAttributes(element, path, warner, "size");
		final CommunityStats retval = new CommunityStatsImpl(getIntegerAttribute(element, path, "size"));
		@Nullable String current = null;
		final Deque<StartElement> stack = new LinkedList<>();
		stack.addFirst(element);
		final Consumer<IMutableResourcePile> addProduction = retval::addYearlyProduction;
		final Consumer<IMutableResourcePile> addConsumption = retval::addYearlyConsumption;
		for (final XMLEvent event : stream) {
			// switch would require break-to-label
			//noinspection IfCanBeSwitch
			if (event instanceof final EndElement ee && ee.getName().equals(element.getName())) {
				break;
			} else if (event instanceof final StartElement se && isSPStartElement(event)) {
				switch (se.getName().getLocalPart().toLowerCase()) {
					case "expertise":
						expectAttributes(se, path, warner, "skill", "level");
						retval.setSkillLevel(getAttribute(se, path, "skill"),
								getIntegerAttribute(se, path, "level"));
						stack.addFirst(se);
						break;
					case "claim":
						expectAttributes(se, path, warner, "resource");
						retval.addWorkedField(getIntegerAttribute(se, path,
								"resource"));
						stack.addFirst(se);
						break;
					case "production":
					case "consumption":
						if (Objects.isNull(current)) {
							expectAttributes(se, path, warner);
							current = se.getName().getLocalPart();
							stack.addFirst(se);
						} else {
							throw new UnwantedChildException(
									Optional.ofNullable(stack.peekFirst()).map(StartElement::getName)
											.orElse(NULL_QNAME), se, path);
						}
						break;
					case "resource":
						final StartElement top = stack.peekFirst();
						final Consumer<IMutableResourcePile> lambda = switch (current) {
							case "production" -> addProduction;
							case "consumption" -> addConsumption;
							case null, default -> throw UnwantedChildException.listingExpectedTags(
									Optional.ofNullable(top).map(StartElement::getName).orElse(NULL_QNAME), se,
									path, "production", "consumption");
						};
						lambda.accept(FluidResourceHandler.readResource(se, path,
								Optional.ofNullable(top).map(StartElement::getName).orElse(NULL_QNAME),
								stream, players, warner, idFactory));
						break;
					default:
						throw UnwantedChildException.listingExpectedTags(
								se.getName(), element, path, "expertise",
								"claim", "production", "consumption", "resource");
				}
			} else if (event instanceof final EndElement ee && !stack.isEmpty() &&
					ee.getName().equals(Objects.requireNonNull(stack.peekFirst()).getName())) {
				final StartElement top = stack.removeFirst();
				if (top.equals(element)) {
					break;
				} else if (top.getName().getLocalPart().equals(current)) {
					current = null;
				}
			}
		}
		return retval;
	}

	public static void writeVillage(final XMLStreamWriter ostream, final Village obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "village", indent, Objects.isNull(obj.getPopulation()));
		writeAttributes(ostream, Pair.with("status", obj.getStatus().toString()));
		writeNonEmptyAttributes(ostream, Pair.with("name", obj.getName()));
		writeAttributes(ostream, Pair.with("id", obj.getId()),
				Pair.with("owner", obj.owner().getPlayerId()),
				Pair.with("race", obj.getRace()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		if (Objects.nonNull(obj.getPopulation())) {
			writeCommunityStats(ostream, obj.getPopulation(), indent);
			ostream.writeEndElement();
		}
	}

	public static void writeTown(final XMLStreamWriter ostream, final AbstractTown obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, obj.getKind(), indent, Objects.isNull(obj.getPopulation()));
		writeAttributes(ostream, Pair.with("status", obj.getStatus().toString()),
				Pair.with("size", obj.getTownSize().toString()), Pair.with("dc", obj.getDC()));
		writeNonEmptyAttributes(ostream, Pair.with("name", obj.getName()));
		writeAttributes(ostream, Pair.with("id", obj.getId()),
				Pair.with("owner", obj.owner().getPlayerId()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		if (Objects.nonNull(obj.getPopulation())) {
			writeCommunityStats(ostream, obj.getPopulation(), indent);
			ostream.writeEndElement();
		}
	}

	public static void writeCommunityStats(final XMLStreamWriter ostream, final CommunityStats obj,
										   final int indent) throws XMLStreamException {
		writeTag(ostream, "population", indent, false);
		writeAttributes(ostream, Pair.with("size", obj.getPopulation()));
		for (final Map.Entry<String, Integer> entry : obj.getHighestSkillLevels().entrySet()
				.stream().sorted(Map.Entry.comparingByKey()).toList()) {
			writeTag(ostream, "expertise", indent + 1, true);
			writeAttributes(ostream, Pair.with("skill", entry.getKey()),
					Pair.with("level", entry.getValue()));
		}
		for (final Integer claim : obj.getWorkedFields()) {
			writeTag(ostream, "claim", indent + 1, true);
			writeAttributes(ostream, Pair.with("resource", claim));
		}
		if (!obj.getYearlyProduction().isEmpty()) {
			writeTag(ostream, "production", indent + 1, false);
			for (final IResourcePile resource : obj.getYearlyProduction()) {
				FluidResourceHandler.writeResource(ostream, resource, indent + 2);
			}
			ostream.writeEndElement();
		}
		if (!obj.getYearlyConsumption().isEmpty()) {
			writeTag(ostream, "consumption", indent + 1, false);
			for (final IResourcePile resource : obj.getYearlyConsumption()) {
				FluidResourceHandler.writeResource(ostream, resource, indent + 2);
			}
			ostream.writeEndElement();
		}
		ostream.writeEndElement();
	}
}

package impl.xmlio.fluidxml;

import org.javatuples.Pair;

import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.EndElement;

import common.idreg.IDRegistrar;
import common.map.IPlayerCollection;
import common.map.fixtures.mobile.worker.RaceFactory;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.City;
import common.map.fixtures.towns.Town;
import common.map.fixtures.towns.Fortification;
import common.map.fixtures.towns.CommunityStats;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import java.util.Random;
import java.util.Deque;
import java.util.LinkedList;
import common.map.fixtures.IMutableResourcePile;
import common.xmlio.SPFormatException;
import common.map.fixtures.IResourcePile;
import javax.xml.stream.XMLStreamException;

import java.util.function.Consumer;

import java.util.Map;
import java.util.stream.Collectors;

/* package */ class FluidTownHandler extends FluidBase {
	// FIXME: Extract a common readAbstractTown() method taking the
	// expected tag and a constructor reference, since readTown(),
	// readFortification(), and readCity() are very nearly identical
	public static Town readTown(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                            final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "town");
		expectAttributes(element, warner, "name", "size", "status", "dc", "id",
			"portrait", "image", "owner");
		requireNonEmptyAttribute(element, "name", false, warner);
		final TownSize size;
		try {
			size = TownSize.parseTownSize(getAttribute(element, "size"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		final TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		final Town fix = new Town(status, size, getIntegerAttribute(element, "dc"),
			getAttribute(element, "name", ""),
			getOrGenerateID(element, warner, idFactory),
			getPlayerOrIndependent(element, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			if (event instanceof StartElement se && isSPStartElement(event)) {
				if (fix.getPopulation() == null) {
					fix.setPopulation(readCommunityStats(se,
						element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(), se);
				}
			} else if (event instanceof EndElement ee && ee.getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(fix, element, warner);
	}

	public static Fortification readFortification(final StartElement element, final QName parent,
	                                              final Iterable<XMLEvent> stream, final IPlayerCollection players, final Warning warner,
	                                              final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "fortification");
		expectAttributes(element, warner, "name", "size", "status", "dc", "id",
			"portrait", "image", "owner");
		requireNonEmptyAttribute(element, "name", false, warner);
		final TownSize size;
		try {
			size = TownSize.parseTownSize(getAttribute(element, "size"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		final TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		final Fortification fix = new Fortification(status, size,
			getIntegerAttribute(element, "dc"),
			getAttribute(element, "name", ""),
			getOrGenerateID(element, warner, idFactory),
			getPlayerOrIndependent(element, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			if (event instanceof StartElement se && isSPStartElement(event)) {
				if (fix.getPopulation() == null) {
					fix.setPopulation(readCommunityStats(se,
						element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(),
						se);
				}
			} else if (event instanceof EndElement ee &&
					ee.getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(fix, element, warner);
	}

	public static City readCity(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                            final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "city");
		expectAttributes(element, warner, "name", "size", "status", "dc", "id",
			"portrait", "image", "owner");
		requireNonEmptyAttribute(element, "name", false, warner);
		final TownSize size;
		try {
			size = TownSize.parseTownSize(getAttribute(element, "size"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		final TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		final City fix = new City(status, size, getIntegerAttribute(element, "dc"),
			getAttribute(element, "name", ""),
			getOrGenerateID(element, warner, idFactory),
			getPlayerOrIndependent(element, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			if (event instanceof StartElement se && isSPStartElement(event)) {
				if (fix.getPopulation() == null) {
					fix.setPopulation(readCommunityStats(se,
						element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(),
						se);
				}
			} else if (event instanceof EndElement ee && ee.getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(fix, element, warner);
	}

	public static Village readVillage(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                                  final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "village");
		expectAttributes(element, warner, "status", "race", "owner", "id", "image",
			"portrait", "name");
		requireNonEmptyAttribute(element, "name", false, warner);
		final int idNum = getOrGenerateID(element, warner, idFactory);
		final TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		final Village retval = new Village(status, getAttribute(element, "name", ""), idNum,
			getPlayerOrIndependent(element, warner, players),
			getAttribute(element, "race",
				RaceFactory.randomRace(new Random(idNum))));
		retval.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			if (event instanceof StartElement se && isSPStartElement(event)) {
				if (retval.getPopulation() == null) {
					retval.setPopulation(readCommunityStats(se,
						element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(), se);
				}
			} else if (event instanceof EndElement ee && ee.getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(retval, element, warner);
	}

	public static CommunityStats readCommunityStats(final StartElement element, final QName parent,
	                                                final Iterable<XMLEvent> stream, final IPlayerCollection players, final Warning warner,
	                                                final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "population");
		expectAttributes(element, warner, "size");
		final CommunityStats retval = new CommunityStats(getIntegerAttribute(element, "size"));
		@Nullable String current = null;
		final Deque<StartElement> stack = new LinkedList<>();
		stack.addFirst(element);
		for (final XMLEvent event : stream) {
			if (event instanceof EndElement ee && ee.getName().equals(element.getName())) {
				break;
			} else if (event instanceof StartElement se && isSPStartElement(event)) {
				switch (se.getName().getLocalPart().toLowerCase()) {
				case "expertise":
					expectAttributes(se, warner, "skill", "level");
					retval.setSkillLevel(getAttribute(se, "skill"),
						getIntegerAttribute(se, "level"));
					stack.addFirst(se);
					break;
				case "claim":
					expectAttributes(se, warner, "resource");
					retval.addWorkedField(getIntegerAttribute(se,
						"resource"));
					stack.addFirst(se);
					break;
				case "production": case "consumption":
					if (current == null) {
						expectAttributes(se, warner);
						current = se.getName().getLocalPart();
						stack.addFirst(se);
					} else {
						throw new UnwantedChildException(
							stack.peekFirst().getName(), se);
					}
					break;
				case "resource":
					final StartElement top = stack.peekFirst();
					final Consumer<IMutableResourcePile> lambda;
					if ("production".equals(current)) {
						lambda = retval.getYearlyProduction()::add;
					} else if ("consumption".equals(current)) {
						lambda = retval.getYearlyConsumption()::add;
					} else {
						throw UnwantedChildException.listingExpectedTags(
							top.getName(), se, "production",
							"consumption");
					}
					lambda.accept(FluidResourceHandler.readResource(se, top.getName(),
						stream, players, warner, idFactory));
					break;
				default:
					throw UnwantedChildException.listingExpectedTags(
						se.getName(), element, "expertise",
						"claim", "production", "consumption", "resource");
				}
			} else if (event instanceof EndElement ee && !stack.isEmpty() &&
					ee.getName().equals(stack.peekFirst().getName())) {
				final StartElement top = stack.peekFirst();
				stack.removeFirst(); // FIXME: Combine with previous line; removeFirst() returns the top ...
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
		writeTag(ostream, "village", indent, obj.getPopulation() == null);
		writeAttributes(ostream, Pair.with("status", obj.getStatus().toString()));
		writeNonEmptyAttributes(ostream, Pair.with("name", obj.getName()));
		writeAttributes(ostream, Pair.with("id", obj.getId()),
			Pair.with("owner", obj.owner().getPlayerId()),
			Pair.with("race", obj.getRace()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		if (obj.getPopulation() != null) {
			writeCommunityStats(ostream, obj.getPopulation(), indent);
			ostream.writeEndElement();
		}
	}

	public static void writeTown(final XMLStreamWriter ostream, final AbstractTown obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, obj.getKind(), indent, obj.getPopulation() == null);
		writeAttributes(ostream, Pair.with("status", obj.getStatus().toString()),
			Pair.with("size", obj.getTownSize().toString()), Pair.with("dc", obj.getDC()));
		writeNonEmptyAttributes(ostream, Pair.with("name", obj.getName()));
		writeAttributes(ostream, Pair.with("id", obj.getId()),
			Pair.with("owner", obj.owner().getPlayerId()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		if (obj.getPopulation() != null) {
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

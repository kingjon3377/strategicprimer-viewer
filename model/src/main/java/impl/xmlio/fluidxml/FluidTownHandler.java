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
import lovelace.util.MalformedXMLException;
import common.map.fixtures.IResourcePile;
import javax.xml.stream.XMLStreamException;

import java.util.function.Consumer;

import java.util.Map;
import java.util.stream.Collectors;

/* package */ class FluidTownHandler extends FluidBase {
	// FIXME: Extract a common readAbstractTown() method taking the
	// expected tag and a constructor reference, since readTown(),
	// readFortification(), and readCity() are very nearly identical
	public static Town readTown(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IPlayerCollection players, Warning warner, IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "town");
		expectAttributes(element, warner, "name", "size", "status", "dc", "id",
			"portrait", "image", "owner");
		requireNonEmptyAttribute(element, "name", false, warner);
		TownSize size;
		try {
			size = TownSize.parseTownSize(getAttribute(element, "size"));
		} catch (IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		Town fix = new Town(status, size, getIntegerAttribute(element, "dc"),
			getAttribute(element, "name", ""),
			getOrGenerateID(element, warner, idFactory),
			getPlayerOrIndependent(element, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				if (fix.getPopulation() == null) {
					fix.setPopulation(readCommunityStats((StartElement) event,
						element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(),
						(StartElement) event);
				}
			} else if (event instanceof EndElement &&
					((EndElement) event).getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(fix, element, warner);
	}

	public static Fortification readFortification(StartElement element, QName parent,
			Iterable<XMLEvent> stream, IPlayerCollection players, Warning warner,
			IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "fortification");
		expectAttributes(element, warner, "name", "size", "status", "dc", "id",
			"portrait", "image", "owner");
		requireNonEmptyAttribute(element, "name", false, warner);
		TownSize size;
		try {
			size = TownSize.parseTownSize(getAttribute(element, "size"));
		} catch (IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		Fortification fix = new Fortification(status, size,
			getIntegerAttribute(element, "dc"),
			getAttribute(element, "name", ""),
			getOrGenerateID(element, warner, idFactory),
			getPlayerOrIndependent(element, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				if (fix.getPopulation() == null) {
					fix.setPopulation(readCommunityStats((StartElement) event,
						element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(),
						(StartElement) event);
				}
			} else if (event instanceof EndElement &&
					((EndElement) event).getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(fix, element, warner);
	}

	public static City readCity(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IPlayerCollection players, Warning warner, IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "city");
		expectAttributes(element, warner, "name", "size", "status", "dc", "id",
			"portrait", "image", "owner");
		requireNonEmptyAttribute(element, "name", false, warner);
		TownSize size;
		try {
			size = TownSize.parseTownSize(getAttribute(element, "size"));
		} catch (IllegalArgumentException except) {
			throw new MissingPropertyException(element, "size", except);
		}
		TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		City fix = new City(status, size, getIntegerAttribute(element, "dc"),
			getAttribute(element, "name", ""),
			getOrGenerateID(element, warner, idFactory),
			getPlayerOrIndependent(element, warner, players));
		fix.setPortrait(getAttribute(element, "portrait", ""));
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				if (fix.getPopulation() == null) {
					fix.setPopulation(readCommunityStats((StartElement) event,
						element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(),
						(StartElement) event);
				}
			} else if (event instanceof EndElement &&
					((EndElement) event).getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(fix, element, warner);
	}

	public static Village readVillage(StartElement element, QName parent, Iterable<XMLEvent> stream,
			IPlayerCollection players, Warning warner, IDRegistrar idFactory) 
			throws SPFormatException {
		requireTag(element, parent, "village");
		expectAttributes(element, warner, "status", "race", "owner", "id", "image",
			"portrait", "name");
		requireNonEmptyAttribute(element, "name", false, warner);
		int idNum = getOrGenerateID(element, warner, idFactory);
		TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		Village retval = new Village(status, getAttribute(element, "name", ""), idNum,
			getPlayerOrIndependent(element, warner, players),
			getAttribute(element, "race",
				RaceFactory.randomRace(new Random(idNum))));
		retval.setPortrait(getAttribute(element, "portrait", ""));
		for (XMLEvent event : stream) {
			if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				if (retval.getPopulation() == null) {
					retval.setPopulation(readCommunityStats((StartElement) event,
						element.getName(), stream, players, warner, idFactory));
				} else {
					throw new UnwantedChildException(element.getName(),
						(StartElement) event);
				}
			} else if (event instanceof EndElement &&
					((EndElement) event).getName().equals(element.getName())) {
				break;
			}
		}
		return setImage(retval, element, warner);
	}

	public static CommunityStats readCommunityStats(StartElement element, QName parent,
			Iterable<XMLEvent> stream, IPlayerCollection players, Warning warner,
			IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "population");
		expectAttributes(element, warner, "size");
		CommunityStats retval = new CommunityStats(getIntegerAttribute(element, "size"));
		@Nullable String current = null;
		Deque<StartElement> stack = new LinkedList<>();
		stack.addFirst(element);
		for (XMLEvent event : stream) {
			if (event instanceof EndElement &&
					((EndElement) event).getName().equals(element.getName())) {
				break;
			} else if (event instanceof StartElement && isSPStartElement((StartElement) event)) {
				switch (((StartElement) event).getName().getLocalPart().toLowerCase()) {
				case "expertise":
					expectAttributes((StartElement) event, warner, "skill", "level");
					retval.setSkillLevel(getAttribute((StartElement) event, "skill"),
						getIntegerAttribute((StartElement) event, "level"));
					stack.addFirst((StartElement) event);
					break;
				case "claim":
					expectAttributes((StartElement) event, warner, "resource");
					retval.addWorkedField(getIntegerAttribute((StartElement) event,
						"resource"));
					stack.addFirst((StartElement) event);
					break;
				case "production": case "consumption":
					if (current == null) {
						expectAttributes((StartElement) event, warner);
						current = ((StartElement) event).getName().getLocalPart();
						stack.addFirst((StartElement) event);
					} else {
						throw new UnwantedChildException(
							stack.peekFirst().getName(), (StartElement) event);
					}
					break;
				case "resource":
					StartElement top = stack.peekFirst();
					final Consumer<IMutableResourcePile> lambda;
					switch (current) {
					case "production":
						lambda = retval.getYearlyProduction()::add;
						break;
					case "consumption":
						lambda = retval.getYearlyConsumption()::add;
						break;
					default:
						throw UnwantedChildException.listingExpectedTags(
							top.getName(), (StartElement) event, "production",
							"consumption");
					}
					lambda.accept(FluidResourceHandler.readResource(
						(StartElement) event, top.getName(),
						stream, players, warner, idFactory));
					break;
				default:
					throw UnwantedChildException.listingExpectedTags(
						((StartElement) event).getName(), element, "expertise",
						"claim", "production", "consumption", "resource");
				}
			} else if (event instanceof EndElement && !stack.isEmpty() &&
					((EndElement) event).getName()
						.equals(stack.peekFirst().getName())) {
				StartElement top = stack.peekFirst();
				stack.removeFirst();
				if (top.equals(element)) {
					break;
				} else if (top.getName().getLocalPart().equals(current)) {
					current = null;
				}
			}
		}
		return retval;
	}

	public static void writeVillage(XMLStreamWriter ostream, Village obj, int indent)
			throws MalformedXMLException {
		writeTag(ostream, "village", indent, obj.getPopulation() == null);
		writeAttributes(ostream, Pair.with("status", obj.getStatus().toString()));
		writeNonEmptyAttributes(ostream, Pair.with("name", obj.getName()));
		writeAttributes(ostream, Pair.with("id", obj.getId()),
			Pair.with("owner", obj.getOwner().getPlayerId()),
			Pair.with("race", obj.getRace()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		if (obj.getPopulation() != null) {
			writeCommunityStats(ostream, obj.getPopulation(), indent);
			try {
				ostream.writeEndElement();
			} catch (XMLStreamException except) {
				throw new MalformedXMLException(except);
			}
		}
	}

	public static void writeTown(XMLStreamWriter ostream, AbstractTown obj, int indent) 
			throws MalformedXMLException {
		writeTag(ostream, obj.getKind(), indent, obj.getPopulation() == null);
		writeAttributes(ostream, Pair.with("status", obj.getStatus().toString()),
			Pair.with("size", obj.getTownSize().toString()), Pair.with("dc", obj.getDC()));
		writeNonEmptyAttributes(ostream, Pair.with("name", obj.getName()));
		writeAttributes(ostream, Pair.with("id", obj.getId()),
			Pair.with("owner", obj.getOwner().getPlayerId()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		if (obj.getPopulation() != null) {
			writeCommunityStats(ostream, obj.getPopulation(), indent);
			try {
				ostream.writeEndElement();
			} catch (XMLStreamException except) {
				throw new MalformedXMLException(except);
			}
		}
	}

	public static void writeCommunityStats(XMLStreamWriter ostream, CommunityStats obj,
			int indent) throws MalformedXMLException {
		writeTag(ostream, "population", indent, false);
		writeAttributes(ostream, Pair.with("size", obj.getPopulation()));
		for (Map.Entry<String, Integer> entry : obj.getHighestSkillLevels().entrySet()
				.stream().sorted((one, two) -> one.getKey().compareTo(two.getKey()))
				.collect(Collectors.toList())) {
			writeTag(ostream, "expertise", indent + 1, true);
			writeAttributes(ostream, Pair.with("skill", entry.getKey()),
				Pair.with("level", entry.getValue()));
		}
		for (Integer claim : obj.getWorkedFields()) {
			writeTag(ostream, "claim", indent + 1, true);
			writeAttributes(ostream, Pair.with("resource", claim));
		}
		if (!obj.getYearlyProduction().isEmpty()) {
			writeTag(ostream, "production", indent + 1, false);
			for (IResourcePile resource : obj.getYearlyProduction()) {
				FluidResourceHandler.writeResource(ostream, resource, indent + 2);
			}
			try {
				ostream.writeEndElement();
			} catch (XMLStreamException except) {
				throw new MalformedXMLException(except);
			}
		}
		if (!obj.getYearlyConsumption().isEmpty()) {
			writeTag(ostream, "consumption", indent + 1, false);
			for (IResourcePile resource : obj.getYearlyConsumption()) {
				FluidResourceHandler.writeResource(ostream, resource, indent + 2);
			}
			try {
				ostream.writeEndElement();
			} catch (XMLStreamException except) {
				throw new MalformedXMLException(except);
			}
		}
		try {
			ostream.writeEndElement();
		} catch (XMLStreamException except) {
			throw new MalformedXMLException(except);
		}
	}
}

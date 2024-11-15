package legacy.xmlio.fluidxml;

import common.map.fixtures.resources.FieldStatus;
import common.map.fixtures.towns.TownStatus;
import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import impl.xmlio.exceptions.DeprecatedPropertyException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.HasExtent;
import legacy.map.ILegacyPlayerCollection;
import legacy.map.fixtures.IMutableResourcePile;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.ResourcePileImpl;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.CultivationStatus;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.StoneKind;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.nio.file.Path;

/* package */ class FluidResourceHandler extends FluidBase {
	public static IMutableResourcePile readResource(final StartElement element, final @Nullable Path path,
	                                                final QName parent, final Iterable<XMLEvent> stream,
	                                                final ILegacyPlayerCollection players, final Warning warner,
	                                                final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, path, parent, "resource");
		expectAttributes(element, path, warner, "quantity", "kind", "contents", "unit",
				"created", "id", "image");
		spinUntilEnd(element.getName(), path, stream);
		final IMutableResourcePile retval = new ResourcePileImpl(
				getOrGenerateID(element, warner, path, idFactory),
				getAttribute(element, path, "kind"),
				getAttribute(element, path, "contents"),
				new LegacyQuantity(getNumericAttribute(element, path, "quantity"), getAttribute(element,
						"unit", "")));
		if (hasAttribute(element, "created")) {
			retval.setCreated(getIntegerAttribute(element, path, "created"));
		}
		return setImage(retval, element, path, warner);
	}

	public static Implement readImplement(final StartElement element, final @Nullable Path path, final QName parent,
										  final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
										  final Warning warner, final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, path, parent, "implement");
		expectAttributes(element, path, warner, "kind", "id", "count", "image");
		spinUntilEnd(element.getName(), path, stream);
		return setImage(new Implement(getAttribute(element, path, "kind"),
				getOrGenerateID(element, warner, path, idFactory),
				getIntegerAttribute(element, "count", 1, warner)), element, path, warner);
	}

	@SuppressWarnings("ChainOfInstanceofChecks")
	public static CacheFixture readCache(final StartElement element, final @Nullable Path path, final QName parent,
										 final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
										 final Warning warner, final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, path, parent, "cache");
		expectAttributes(element, path, warner, "kind", "contents", "id", "image");
		// We want to transition from arbitrary-String 'contents' to sub-tags. As a first
		// step, future-proof *this* version of the suite by only firing a warning if
		// such children are detected, instead of aborting.
		for (final XMLEvent event : stream) {
			// switch would require break-to-label
			//noinspection IfCanBeSwitch
			if (event instanceof final StartElement se && isSPStartElement(event)) {
				if ("resource".equalsIgnoreCase(se.getName().getLocalPart()) ||
						"implement".equalsIgnoreCase(se.getName().getLocalPart())) {
					warner.handle(new UnwantedChildException(element.getName(), se, path));
				} else {
					throw new UnwantedChildException(element.getName(), se, path);
				}
			} else if (event instanceof final EndElement ee &&
					element.getName().equals(ee.getName())) {
				break;
			}
		}
		return setImage(
				new CacheFixture(getAttribute(element, path, "kind"),
						getAttribute(element, path, "contents"),
						getOrGenerateID(element, warner, path, idFactory)),
				element, path, warner);
	}

	public static Grove readGrove(final StartElement element, final @Nullable Path path, final QName parent,
	                              final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                              final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "grove");
		expectAttributes(element, path, warner, "cultivated", "wild", "kind", "tree", "id",
				"image", "count");
		spinUntilEnd(element.getName(), path, stream);
		final boolean cultivated;
		if (hasAttribute(element, "cultivated")) {
			cultivated = getBooleanAttribute(element, path, "cultivated");
		} else if (hasAttribute(element, "wild")) {
			warner.handle(new DeprecatedPropertyException(element, path, "wild", "cultivated"));
			cultivated = !getBooleanAttribute(element, path, "wild");
		} else {
			throw new MissingPropertyException(element, path, "cultivated");
		}
		return setImage(
				new Grove(Grove.GroveType.GROVE, cultivated ? CultivationStatus.CULTIVATED : CultivationStatus.WILD,
						getAttrWithDeprecatedForm(element, path, "kind", "tree", warner),
						getOrGenerateID(element, warner, path, idFactory), getIntegerAttribute(element,
						"count", -1)), element, path, warner);
	}

	public static Grove readOrchard(final StartElement element, final @Nullable Path path, final QName parent,
	                                final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                                final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "orchard");
		expectAttributes(element, path, warner, "cultivated", "wild", "kind", "tree", "id",
				"image", "count");
		spinUntilEnd(element.getName(), path, stream);
		final boolean cultivated;
		if (hasAttribute(element, "cultivated")) {
			cultivated = getBooleanAttribute(element, path, "cultivated");
		} else if (hasAttribute(element, "wild")) {
			warner.handle(new DeprecatedPropertyException(element, path, "wild", "cultivated"));
			cultivated = !getBooleanAttribute(element, path, "wild");
		} else {
			throw new MissingPropertyException(element, path, "cultivated");
		}
		return setImage(
				new Grove(Grove.GroveType.ORCHARD, cultivated ? CultivationStatus.CULTIVATED : CultivationStatus.WILD,
						getAttrWithDeprecatedForm(element, path, "kind", "tree", warner),
						getOrGenerateID(element, warner, path, idFactory), getIntegerAttribute(element,
						"count", -1)), element, path, warner);
	}

	public static Meadow readMeadow(final StartElement element, final @Nullable Path path, final QName parent,
	                                final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                                final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "meadow");
		expectAttributes(element, path, warner, "status", "kind", "cultivated", "id", "image",
				"acres");
		spinUntilEnd(element.getName(), path, stream);
		final int id = getOrGenerateID(element, warner, path, idFactory);
		if (!hasAttribute(element, "status")) {
			warner.handle(new MissingPropertyException(element, path, "status"));
		}
		final FieldStatus status;
		try {
			status = FieldStatus.parse(getAttribute(element, "status",
					FieldStatus.random(id).toString()));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "status", except);
		}
		return setImage(new Meadow(getAttribute(element, path, "kind"), false,
				getBooleanAttribute(element, path, "cultivated") ? CultivationStatus.CULTIVATED :
						CultivationStatus.WILD, id, status,
				getNumericAttribute(element, path, "acres", -1)), element, path, warner);
	}

	public static Meadow readField(final StartElement element, final @Nullable Path path, final QName parent,
	                               final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                               final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "field");
		expectAttributes(element, path, warner, "status", "kind", "cultivated", "id", "image",
				"acres");
		spinUntilEnd(element.getName(), path, stream);
		final int id = getOrGenerateID(element, warner, path, idFactory);
		if (!hasAttribute(element, "status")) {
			warner.handle(new MissingPropertyException(element, path, "status"));
		}
		final FieldStatus status;
		try {
			status = FieldStatus.parse(getAttribute(element, "status",
					FieldStatus.random(id).toString()));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "status", except);
		}
		return setImage(new Meadow(getAttribute(element, path, "kind"), true,
				getBooleanAttribute(element, path, "cultivated") ? CultivationStatus.CULTIVATED :
						CultivationStatus.WILD, id, status,
				getNumericAttribute(element, path, "acres", -1)), element, path, warner);
	}

	public static Mine readMine(final StartElement element, final @Nullable Path path, final QName parent,
	                            final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                            final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "mine");
		expectAttributes(element, path, warner, "status", "kind", "product", "id", "image");
		spinUntilEnd(element.getName(), path, stream);
		final TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, path, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "status", except);
		}
		return setImage(
				new Mine(getAttrWithDeprecatedForm(element, path, "kind", "product", warner),
						status, getOrGenerateID(element, warner, path, idFactory)), element, path,
				warner);
	}

	public static MineralVein readMineral(final StartElement element, final @Nullable Path path, final QName parent,
										  final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
										  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "mineral");
		expectAttributes(element, path, warner, "kind", "mineral", "exposed", "dc", "id", "image");
		spinUntilEnd(element.getName(), path, stream);
		return setImage(
				new MineralVein(
						getAttrWithDeprecatedForm(element, path, "kind", "mineral", warner),
						getBooleanAttribute(element, path, "exposed"),
						getIntegerAttribute(element, path, "dc"),
						getOrGenerateID(element, warner, path, idFactory)), element, path, warner);
	}

	public static Shrub readShrub(final StartElement element, final @Nullable Path path, final QName parent,
	                              final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                              final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "shrub");
		expectAttributes(element, path, warner, "kind", "shrub", "id", "image", "count");
		spinUntilEnd(element.getName(), path, stream);
		return setImage(new Shrub(
				getAttrWithDeprecatedForm(element, path, "kind", "shrub", warner),
				getOrGenerateID(element, warner, path, idFactory), getIntegerAttribute(element,
				"count", -1)), element, path, warner);
	}

	public static StoneDeposit readStone(final StartElement element, final @Nullable Path path, final QName parent,
										 final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
										 final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "stone");
		expectAttributes(element, path, warner, "kind", "stone", "dc", "id", "image");
		spinUntilEnd(element.getName(), path, stream);
		final StoneKind stone;
		try {
			stone = StoneKind.parse(getAttrWithDeprecatedForm(element, path, "kind", "stone", warner));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "kind", except);
		}
		return setImage(
				new StoneDeposit(stone,
						getIntegerAttribute(element, path, "dc"),
						getOrGenerateID(element, warner, path, idFactory)), element, path, warner);
	}

	public static void writeResource(final XMLStreamWriter ostream, final IResourcePile obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "resource", indent, true);
		writeAttributes(ostream, Pair.with("id", obj.getId()), Pair.with("kind", obj.getKind()),
				Pair.with("contents", obj.getContents()),
				Pair.with("quantity", obj.getQuantity().number()),
				Pair.with("unit", obj.getQuantity().units()));
		if (obj.getCreated() >= 0) {
			writeAttributes(ostream, Pair.with("created", obj.getCreated()));
		}
		writeImage(ostream, obj);
	}

	public static void writeImplement(final XMLStreamWriter ostream, final Implement obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "implement", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()), Pair.with("id", obj.getId()));
		if (obj.getCount() > 1) {
			writeAttributes(ostream, Pair.with("count", obj.getCount()));
		}
		writeImage(ostream, obj);
	}

	public static void writeCache(final XMLStreamWriter ostream, final CacheFixture obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "cache", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()),
				Pair.with("contents", obj.getContents()), Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeMeadow(final XMLStreamWriter ostream, final Meadow obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, (obj.isField()) ? "field" : "meadow", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()),
				Pair.with("cultivated", obj.getCultivation() == CultivationStatus.CULTIVATED),
				Pair.with("status", obj.getStatus().toString()), Pair.with("id", obj.getId()));
		if (HasExtent.isPositive(obj.getAcres())) {
			writeAttributes(ostream, Pair.with("acres", obj.getAcres()));
		}
		writeImage(ostream, obj);
	}

	public static void writeGrove(final XMLStreamWriter ostream, final Grove obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, obj.getType().toString(), indent, true);
		writeAttributes(ostream, Pair.with("cultivated", obj.getCultivation() == CultivationStatus.CULTIVATED),
				Pair.with("kind", obj.getKind()), Pair.with("id", obj.getId()));
		if (obj.getPopulation() >= 1) {
			writeAttributes(ostream, Pair.with("count", obj.getPopulation()));
		}
		writeImage(ostream, obj);
	}

	public static void writeMine(final XMLStreamWriter ostream, final Mine obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "mine", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()),
				Pair.with("status", obj.getStatus().toString()), Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeMineral(final XMLStreamWriter ostream, final MineralVein obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "mineral", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()),
				Pair.with("exposed", obj.isExposed()), Pair.with("dc", obj.getDC()),
				Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeStone(final XMLStreamWriter ostream, final StoneDeposit obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "stone", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getStone().toString()),
				Pair.with("dc", obj.getDC()), Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeShrub(final XMLStreamWriter ostream, final Shrub obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "shrub", indent, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()), Pair.with("id", obj.getId()));
		if (obj.getPopulation() >= 1) {
			writeAttributes(ostream, Pair.with("count", obj.getPopulation()));
		}
		writeImage(ostream, obj);
	}
}

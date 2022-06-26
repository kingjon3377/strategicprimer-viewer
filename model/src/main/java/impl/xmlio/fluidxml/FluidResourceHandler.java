package impl.xmlio.fluidxml;

import javax.xml.stream.XMLStreamException;
import org.javatuples.Pair;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.EndElement;

import common.idreg.IDRegistrar;
import common.map.IPlayerCollection;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Implement;
import common.map.fixtures.resources.FieldStatus;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.StoneKind;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.towns.TownStatus;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.DeprecatedPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import common.xmlio.SPFormatException;
import common.map.HasExtent;

/* package */ class FluidResourceHandler extends FluidBase {
	public static IMutableResourcePile readResource(final StartElement element, final QName parent,
	                                                final Iterable<XMLEvent> stream, final IPlayerCollection players, final Warning warner,
	                                                final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "resource");
		expectAttributes(element, warner, "quantity", "kind", "contents", "unit",
			"created", "id", "image");
		spinUntilEnd(element.getName(), stream);
		final IMutableResourcePile retval = new ResourcePileImpl(
			getOrGenerateID(element, warner, idFactory),
			getAttribute(element, "kind"),
			getAttribute(element, "contents"),
			new Quantity(getNumericAttribute(element, "quantity"), getAttribute(element,
				"unit", "")));
		if (hasAttribute(element, "created")) {
			retval.setCreated(getIntegerAttribute(element, "created"));
		}
		return setImage(retval, element, warner);
	}

	public static Implement readImplement(final StartElement element, final QName parent,
	                                      final Iterable<XMLEvent> stream, final IPlayerCollection players, final Warning warner,
	                                      final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "implement");
		expectAttributes(element, warner, "kind", "id", "count", "image");
		spinUntilEnd(element.getName(), stream);
		return setImage(new Implement(getAttribute(element, "kind"),
			getOrGenerateID(element, warner, idFactory),
			getIntegerAttribute(element, "count", 1, warner)), element, warner);
	}

	public static CacheFixture readCache(final StartElement element, final QName parent,
	                                     final Iterable<XMLEvent> stream, final IPlayerCollection players, final Warning warner,
	                                     final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "cache");
		expectAttributes(element, warner, "kind", "contents", "id", "image");
		// We want to transition from arbitrary-String 'contents' to sub-tags. As a first
		// step, future-proof *this* version of the suite by only firing a warning if
		// such children are detected, instead of aborting.
		for (final XMLEvent event : stream) {
			if (event instanceof StartElement se && isSPStartElement(event)) {
				if ("resource".equalsIgnoreCase(se.getName().getLocalPart()) ||
						"implement".equalsIgnoreCase(se.getName().getLocalPart())) {
					warner.handle(new UnwantedChildException(element.getName(), se));
				} else {
					throw new UnwantedChildException(element.getName(), se);
				}
			} else if (event instanceof EndElement ee &&
					element.getName().equals(ee.getName())) {
				break;
			}
		}
		return setImage(
			new CacheFixture(getAttribute(element, "kind"),
				getAttribute(element, "contents"),
				getOrGenerateID(element, warner, idFactory)),
			element, warner);
	}

	public static Grove readGrove(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                              final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "grove");
		expectAttributes(element, warner, "cultivated", "wild", "kind", "tree", "id",
			"image", "count");
		spinUntilEnd(element.getName(), stream);
		final boolean cultivated;
		if (hasAttribute(element, "cultivated")) {
			cultivated = getBooleanAttribute(element, "cultivated");
		} else if (hasAttribute(element, "wild")) {
			warner.handle(new DeprecatedPropertyException(element, "wild", "cultivated"));
			cultivated = !getBooleanAttribute(element, "wild");
		} else {
			throw new MissingPropertyException(element, "cultivated");
		}
		return setImage(
			new Grove(false, cultivated,
				getAttrWithDeprecatedForm(element, "kind", "tree", warner),
				getOrGenerateID(element, warner, idFactory), getIntegerAttribute(element,
					"count", -1)), element, warner);
	}

	public static Grove readOrchard(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                                final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "orchard");
		expectAttributes(element, warner, "cultivated", "wild", "kind", "tree", "id",
			"image", "count");
		spinUntilEnd(element.getName(), stream);
		final boolean cultivated;
		if (hasAttribute(element, "cultivated")) {
			cultivated = getBooleanAttribute(element, "cultivated");
		} else if (hasAttribute(element, "wild")) {
			warner.handle(new DeprecatedPropertyException(element, "wild", "cultivated"));
			cultivated = !getBooleanAttribute(element, "wild");
		} else {
			throw new MissingPropertyException(element, "cultivated");
		}
		return setImage(
			new Grove(true, cultivated,
				getAttrWithDeprecatedForm(element, "kind", "tree", warner),
				getOrGenerateID(element, warner, idFactory), getIntegerAttribute(element,
					"count", -1)), element, warner);
	}

	public static Meadow readMeadow(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                                final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "meadow");
		expectAttributes(element, warner, "status", "kind", "cultivated", "id", "image",
			"acres");
		spinUntilEnd(element.getName(), stream);
		final int id = getOrGenerateID(element, warner, idFactory);
		if (!hasAttribute(element, "status")) {
			warner.handle(new MissingPropertyException(element, "status"));
		}
		final FieldStatus status;
		try {
			status = FieldStatus.parse(getAttribute(element, "status",
				FieldStatus.random(id).toString()));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		return setImage(new Meadow(getAttribute(element, "kind"), false,
			getBooleanAttribute(element, "cultivated"), id, status,
			getNumericAttribute(element, "acres", -1)), element, warner);
	}

	public static Meadow readField(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                               final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "field");
		expectAttributes(element, warner, "status", "kind", "cultivated", "id", "image",
			"acres");
		spinUntilEnd(element.getName(), stream);
		final int id = getOrGenerateID(element, warner, idFactory);
		if (!hasAttribute(element, "status")) {
			warner.handle(new MissingPropertyException(element, "status"));
		}
		final FieldStatus status;
		try {
			status = FieldStatus.parse(getAttribute(element, "status",
				FieldStatus.random(id).toString()));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		return setImage(new Meadow(getAttribute(element, "kind"), true,
			getBooleanAttribute(element, "cultivated"), id, status,
			getNumericAttribute(element, "acres", -1)), element, warner);
	}

	public static Mine readMine(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                            final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "mine");
		expectAttributes(element, warner, "status", "kind", "product", "id", "image");
		spinUntilEnd(element.getName(), stream);
		final TownStatus status;
		try {
			status = TownStatus.parse(getAttribute(element, "status"));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		return setImage(
			new Mine(getAttrWithDeprecatedForm(element, "kind", "product", warner),
				status, getOrGenerateID(element, warner, idFactory)), element,
				warner);
		}

	public static MineralVein readMineral(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                                      final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "mineral");
		expectAttributes(element, warner, "kind", "mineral", "exposed", "dc", "id", "image");
		spinUntilEnd(element.getName(), stream);
		return setImage(
			new MineralVein(
				getAttrWithDeprecatedForm(element, "kind", "mineral", warner),
				getBooleanAttribute(element, "exposed"),
				getIntegerAttribute(element, "dc"),
				getOrGenerateID(element, warner, idFactory)), element, warner);
	}

	public static Shrub readShrub(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                              final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "shrub");
		expectAttributes(element, warner, "kind", "shrub", "id", "image", "count");
		spinUntilEnd(element.getName(), stream);
		return setImage(new Shrub(
			getAttrWithDeprecatedForm(element, "kind", "shrub", warner),
			getOrGenerateID(element, warner, idFactory), getIntegerAttribute(element,
				"count", -1)), element, warner);
	}

	public static StoneDeposit readStone(final StartElement element, final QName parent, final Iterable<XMLEvent> stream,
	                                     final IPlayerCollection players, final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "stone");
		expectAttributes(element, warner, "kind", "stone", "dc", "id", "image");
		spinUntilEnd(element.getName(), stream);
		final StoneKind stone;
		try {
			stone = StoneKind.parse(getAttrWithDeprecatedForm(element, "kind", "stone", warner));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "kind", except);
		}
			return setImage(
				new StoneDeposit(stone,
					getIntegerAttribute(element, "dc"),
					getOrGenerateID(element, warner, idFactory)), element, warner);
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
			Pair.with("cultivated", obj.isCultivated()),
			Pair.with("status", obj.getStatus().toString()), Pair.with("id", obj.getId()));
		if (HasExtent.isPositive(obj.getAcres())) {
			writeAttributes(ostream, Pair.with("acres", obj.getAcres()));
		}
		writeImage(ostream, obj);
	}

	public static void writeGrove(final XMLStreamWriter ostream, final Grove obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, (obj.isOrchard()) ? "orchard" : "grove", indent, true);
		writeAttributes(ostream, Pair.with("cultivated", obj.isCultivated()),
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

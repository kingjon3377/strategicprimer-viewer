package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import legacy.idreg.IDRegistrar;
import legacy.map.HasExtent;
import legacy.map.fixtures.resources.HarvestableFixture;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.StoneKind;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.Grove;
import common.map.fixtures.resources.FieldStatus;
import common.map.fixtures.towns.TownStatus;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.DeprecatedPropertyException;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;

/**
 * A reader for resource-bearing {@link legacy.map.TileFixture}s.
 */
/* package */ final class YAResourceReader extends YAAbstractReader<HarvestableFixture, HarvestableFixture> {
	public YAResourceReader(final Warning warner, final IDRegistrar idRegistrar) {
		super(warner, idRegistrar);
		this.warner = warner;
	}

	private final Warning warner;

	private static final Set<String> SUPPORTED_TAGS = Set.of("cache", "grove", "orchard", "field", "meadow", "mine",
			"mineral", "shrub", "stone");

	private HarvestableFixture createMeadow(final StartElement element, final @Nullable Path path, final boolean field,
	                                        final int idNum)
			throws SPFormatException {
		expectAttributes(element, path, "status", "kind", "id", "cultivated", "image", "acres");
		requireNonEmptyParameter(element, path, "status", false);
		final FieldStatus status;
		try {
			// TODO: add FieldStatus.parse() overload taking FieldStatus as default
			status = FieldStatus.parse(getParameter(element, "status",
					FieldStatus.random(idNum).toString()));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, path, "status", except);
		}
		return new Meadow(getParameter(element, path, "kind"), field,
				getBooleanParameter(element, path, "cultivated"), idNum, status,
				getNumericParameter(element, path, "acres", -1));
	}

	private boolean isCultivated(final StartElement element, final @Nullable Path path) throws SPFormatException {
		if (hasParameter(element, "cultivated")) {
			return getBooleanParameter(element, path, "cultivated");
		} else if (hasParameter(element, "wild")) {
			warner.handle(new DeprecatedPropertyException(element, path, "wild", "cultivated"));
			return !getBooleanParameter(element, path, "wild");
		} else {
			throw new MissingPropertyException(element, path, "cultivated");
		}
	}

	/**
	 * TODO: Inline?
	 */
	private HarvestableFixture createGrove(final StartElement element, final @Nullable Path path, final boolean orchard,
	                                       final int idNum)
			throws SPFormatException {
		expectAttributes(element, path, "kind", "tree", "cultivated", "wild", "id", "image", "count");
		return new Grove(orchard, isCultivated(element, path),
				getParamWithDeprecatedForm(element, path, "kind", "tree"), idNum,
				getIntegerParameter(element, path, "count", -1));
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return SUPPORTED_TAGS.contains(tag.toLowerCase());
	}

	@Override
	public HarvestableFixture read(final StartElement element, final @Nullable Path path, final QName parent,
	                               final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, path, parent, SUPPORTED_TAGS);
		final int idNum = getOrGenerateID(element, path);
		final HarvestableFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
			case "cache" -> {
				expectAttributes(element, path, "kind", "contents", "id", "image");
				retval = new CacheFixture(getParameter(element, path, "kind"),
						getParameter(element, path, "contents"), idNum);
				// We want to transition from arbitrary-String 'contents' to sub-tags. As a
				// first step, future-proof *this* version of the suite by only firing a
				// warning if such children are detected, instead of aborting.
				spinUntilEnd(element.getName(), path, stream, "resource", "implement");
				retval.setImage(getParameter(element, "image", ""));
				return retval;
			}
			case "field" -> retval = createMeadow(element, path, true, idNum);
			case "grove" -> retval = createGrove(element, path, false, idNum);
			case "meadow" -> retval = createMeadow(element, path, false, idNum);
			case "mine" -> {
				expectAttributes(element, path, "status", "kind", "product", "id", "image");
				final TownStatus status;
				try {
					status = TownStatus.parse(getParameter(element, path, "status"));
				} catch (final IllegalArgumentException except) {
					throw new MissingPropertyException(element, path, "status", except);
				}
				retval = new Mine(getParamWithDeprecatedForm(element, path, "kind", "product"),
						status, idNum);
			}
			case "mineral" -> {
				expectAttributes(element, path, "kind", "mineral", "exposed", "id", "dc", "image");
				retval = new MineralVein(getParamWithDeprecatedForm(element, path, "kind", "mineral"),
						getBooleanParameter(element, path, "exposed"),
						getIntegerParameter(element, path, "dc"), idNum);
			}
			case "orchard" -> retval = createGrove(element, path, true, idNum);
			case "shrub" -> {
				expectAttributes(element, path, "kind", "shrub", "id", "image", "count");
				retval = new Shrub(getParamWithDeprecatedForm(element, path, "kind", "shrub"), idNum,
						getIntegerParameter(element, path, "count", -1));
			}
			case "stone" -> {
				expectAttributes(element, path, "kind", "stone", "id", "dc", "image");
				final StoneKind stone;
				try {
					stone = StoneKind.parse(getParamWithDeprecatedForm(element, path, "kind", "stone"));
				} catch (final IllegalArgumentException except) {
					throw new MissingPropertyException(element, path, "kind", except);
				}
				retval = new StoneDeposit(stone, getIntegerParameter(element, path, "dc"), idNum);
			}
			default -> throw new IllegalArgumentException("Unhandled harvestable tag");
		}
		spinUntilEnd(element.getName(), path, stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final HarvestableFixture obj,
	                  final int indent) throws IOException {
		switch (obj) {
			case final CacheFixture c -> {
				writeTag(ostream, "cache", indent);
				writeProperty(ostream, "kind", obj.getKind());
				writeProperty(ostream, "contents", c.getContents());
			}
			case final Meadow m -> {
				writeTag(ostream, m.isField() ? "field" : "meadow", indent);
				writeProperty(ostream, "kind", obj.getKind());
				writeProperty(ostream, "cultivated", Boolean.toString(m.isCultivated()));
				writeProperty(ostream, "status", m.getStatus().toString());
				if (HasExtent.isPositive(m.getAcres())) {
					writeProperty(ostream, "acres", m.getAcres().toString());
				}
			}
			case final Grove g -> {
				writeTag(ostream, g.isOrchard() ? "orchard" : "grove", indent);
				writeProperty(ostream, "cultivated", Boolean.toString(g.isCultivated()));
				writeProperty(ostream, "kind", obj.getKind());
				if (g.getPopulation() >= 1) {
					writeProperty(ostream, "count", g.getPopulation());
				}
			}
			case final Mine m -> {
				writeTag(ostream, "mine", indent);
				writeProperty(ostream, "kind", obj.getKind());
				writeProperty(ostream, "status", m.getStatus().toString());
			}
			case final MineralVein mv -> {
				writeTag(ostream, "mineral", indent);
				writeProperty(ostream, "kind", obj.getKind());
				writeProperty(ostream, "exposed", Boolean.toString(mv.isExposed()));
				writeProperty(ostream, "dc", obj.getDC());
			}
			case final Shrub s -> {
				writeTag(ostream, "shrub", indent);
				writeProperty(ostream, "kind", obj.getKind());
				if (s.getPopulation() >= 1) {
					writeProperty(ostream, "count", s.getPopulation());
				}
			}
			case final StoneDeposit sd -> {
				writeTag(ostream, "stone", indent);
				writeProperty(ostream, "kind", sd.getStone().toString());
				writeProperty(ostream, "dc", obj.getDC());
			}
			default -> throw new IllegalArgumentException("Unhandled HarvestableFixture type");
		}
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof HarvestableFixture;
	}
}

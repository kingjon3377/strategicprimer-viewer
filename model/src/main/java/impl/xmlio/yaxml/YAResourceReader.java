package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.HasExtent;
import common.map.fixtures.resources.HarvestableFixture;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.StoneKind;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.FieldStatus;
import common.map.fixtures.towns.TownStatus;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.DeprecatedPropertyException;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * A reader for resource-bearing {@link common.map.TileFixture}s.
 */
/* package */ class YAResourceReader extends YAAbstractReader<HarvestableFixture, HarvestableFixture> {
	public YAResourceReader(final Warning warner, final IDRegistrar idRegistrar) {
		super(warner, idRegistrar);
		this.warner = warner;
	}

	private final Warning warner;

	private static final Set<String> SUPPORTED_TAGS = Collections.unmodifiableSet(new HashSet<>(
		Arrays.asList("cache", "grove", "orchard", "field", "meadow", "mine", "mineral",
			"shrub", "stone")));

	private HarvestableFixture createMeadow(final StartElement element, final boolean field, final int idNum)
			throws SPFormatException {
		expectAttributes(element, "status", "kind", "id", "cultivated", "image", "acres");
		requireNonEmptyParameter(element, "status", false);
		final FieldStatus status;
		try {
			// TODO: add FieldStatus.parse() overload taking FieldStatus as default
			status = FieldStatus.parse(getParameter(element, "status",
				FieldStatus.random(idNum).toString()));
		} catch (final IllegalArgumentException except) {
			throw new MissingPropertyException(element, "status", except);
		}
		return new Meadow(getParameter(element, "kind"), field,
			getBooleanParameter(element, "cultivated"), idNum, status,
			getNumericParameter(element, "acres", -1));
	}

	private boolean isCultivated(final StartElement element) throws SPFormatException {
		if (hasParameter(element, "cultivated")) {
			return getBooleanParameter(element, "cultivated");
		} else if (hasParameter(element, "wild")) {
			warner.handle(new DeprecatedPropertyException(element, "wild", "cultivated"));
			return !getBooleanParameter(element, "wild");
		} else {
			throw new MissingPropertyException(element, "cultivated");
		}
	}

	/**
	 * TODO: Inline?
	 */
	private HarvestableFixture createGrove(final StartElement element, final boolean orchard, final int idNum)
			throws SPFormatException {
		expectAttributes(element, "kind", "tree", "cultivated", "wild", "id", "image", "count");
		return new Grove(orchard, isCultivated(element),
			getParamWithDeprecatedForm(element, "kind", "tree"), idNum,
			getIntegerParameter(element, "count", -1));
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return SUPPORTED_TAGS.contains(tag.toLowerCase());
	}

	@Override
	public HarvestableFixture read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, SUPPORTED_TAGS.toArray(new String[0]));
		final int idNum = getOrGenerateID(element);
		final HarvestableFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "cache":
			expectAttributes(element, "kind", "contents", "id", "image");
			retval = new CacheFixture(getParameter(element, "kind"),
				getParameter(element, "contents"), idNum);
			// We want to transition from arbitrary-String 'contents' to sub-tags. As a
			// first step, future-proof *this* version of the suite by only firing a
			// warning if such children are detected, instead of aborting.
			spinUntilEnd(element.getName(), stream, "resource", "implement");
			retval.setImage(getParameter(element, "image", ""));
			return retval;
		case "field":
			retval = createMeadow(element, true, idNum);
			break;
		case "grove":
			retval = createGrove(element, false, idNum);
			break;
		case "meadow":
			retval = createMeadow(element, false, idNum);
			break;
		case "mine":
			expectAttributes(element, "status", "kind", "product", "id", "image");
			final TownStatus status;
			try {
				status = TownStatus.parse(getParameter(element, "status"));
			} catch (final IllegalArgumentException except) {
				throw new MissingPropertyException(element, "status", except);
			}
			retval = new Mine(getParamWithDeprecatedForm(element, "kind", "product"),
				status, idNum);
			break;
		case "mineral":
			expectAttributes(element, "kind", "mineral", "exposed", "id", "dc", "image");
			retval = new MineralVein(getParamWithDeprecatedForm(element, "kind", "mineral"),
				getBooleanParameter(element, "exposed"),
				getIntegerParameter(element, "dc"), idNum);
			break;
		case "orchard":
			retval = createGrove(element, true, idNum);
			break;
		case "shrub":
			expectAttributes(element, "kind", "shrub", "id", "image", "count");
			retval = new Shrub(getParamWithDeprecatedForm(element, "kind", "shrub"), idNum,
				getIntegerParameter(element, "count", -1));
			break;
		case "stone":
			expectAttributes(element, "kind", "stone", "id", "dc", "image");
			final StoneKind stone;
			try {
				stone = StoneKind.parse(getParamWithDeprecatedForm(element, "kind", "stone"));
			} catch (final IllegalArgumentException except) {
				throw new MissingPropertyException(element, "kind", except);
			}
			retval = new StoneDeposit(stone, getIntegerParameter(element, "dc"), idNum);
			break;
		default:
			throw new IllegalArgumentException("Unhandled harvestable tag");
		}
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final HarvestableFixture obj, final int indent)
			throws IOException {
		if (obj instanceof CacheFixture) {
			writeTag(ostream, "cache", indent);
			writeProperty(ostream, "kind", ((CacheFixture) obj).getKind());
			writeProperty(ostream, "contents", ((CacheFixture) obj).getContents());
		} else if (obj instanceof Meadow) {
			writeTag(ostream, (((Meadow) obj).isField()) ? "field" : "meadow", indent);
			writeProperty(ostream, "kind", ((Meadow) obj).getKind());
			writeProperty(ostream, "cultivated", Boolean.toString(((Meadow) obj).isCultivated()));
			writeProperty(ostream, "status", ((Meadow) obj).getStatus().toString());
			if (HasExtent.isPositive(((Meadow) obj).getAcres())) {
				writeProperty(ostream, "acres", ((Meadow) obj).getAcres().toString());
			}
		} else if (obj instanceof Grove) {
			writeTag(ostream, (((Grove) obj).isOrchard()) ? "orchard" : "grove", indent);
			writeProperty(ostream, "cultivated", Boolean.toString(((Grove) obj).isCultivated()));
			writeProperty(ostream, "kind", ((Grove) obj).getKind());
			if (((Grove) obj).getPopulation() >= 1) {
				writeProperty(ostream, "count", ((Grove) obj).getPopulation());
			}
		} else if (obj instanceof Mine) {
			writeTag(ostream, "mine", indent);
			writeProperty(ostream, "kind", ((Mine) obj).getKind());
			writeProperty(ostream, "status", ((Mine) obj).getStatus().toString());
		} else if (obj instanceof MineralVein) {
			writeTag(ostream, "mineral", indent);
			writeProperty(ostream, "kind", ((MineralVein) obj).getKind());
			writeProperty(ostream, "exposed", Boolean.toString(((MineralVein) obj).isExposed()));
			writeProperty(ostream, "dc", ((MineralVein) obj).getDC());
		} else if (obj instanceof Shrub) {
			writeTag(ostream, "shrub", indent);
			writeProperty(ostream, "kind", ((Shrub) obj).getKind());
			if (((Shrub) obj).getPopulation() >= 1) {
				writeProperty(ostream, "count", ((Shrub) obj).getPopulation());
			}
		} else if (obj instanceof StoneDeposit) {
			writeTag(ostream, "stone", indent);
			writeProperty(ostream, "kind", ((StoneDeposit) obj).getStone().toString());
			writeProperty(ostream, "dc", ((StoneDeposit) obj).getDC());
		} else {
			throw new IllegalArgumentException("Unhandled HarvestableFixture type");
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

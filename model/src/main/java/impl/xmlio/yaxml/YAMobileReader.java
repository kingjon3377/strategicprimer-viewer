package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.function.IntFunction;
import java.io.IOException;

import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.HasMutableImage;
import common.map.HasImage;
import common.map.HasKind;
import common.map.fixtures.mobile.Centaur;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.SimpleImmortal;
import common.map.fixtures.mobile.Giant;
import common.map.fixtures.mobile.Fairy;
import common.map.fixtures.mobile.Dragon;
import common.map.fixtures.mobile.MobileFixture;
import common.map.fixtures.mobile.Sphinx;
import common.map.fixtures.mobile.Djinn;
import common.map.fixtures.mobile.Griffin;
import common.map.fixtures.mobile.Minotaur;
import common.map.fixtures.mobile.Ogre;
import common.map.fixtures.mobile.Phoenix;
import common.map.fixtures.mobile.Simurgh;
import common.map.fixtures.mobile.Troll;
import common.map.fixtures.mobile.Immortal;
import static common.map.fixtures.mobile.Immortal.IMMORTAL_ANIMALS;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.Snowbird;
import common.map.fixtures.mobile.Thunderbird;
import common.map.fixtures.mobile.Pegasus;
import common.map.fixtures.mobile.Unicorn;
import common.map.fixtures.mobile.Kraken;
import common.map.fixtures.mobile.ImmortalAnimal;
import common.xmlio.Warning;

/**
 * A reader for "mobile fixtures"
 */
/* package */ class YAMobileReader extends YAAbstractReader<MobileFixture, MobileFixture> {
	public YAMobileReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	private static final Map<Class<? extends MobileFixture>, String> TAG_MAP = initTagMap();

	private static Map<Class<? extends MobileFixture>, String> initTagMap() {
		final Map<Class<? extends MobileFixture>, String> retval = new HashMap<>();
		retval.put(Animal.class, "animal");
		retval.put(Centaur.class, "centaur");
		retval.put(Dragon.class, "dragon");
		retval.put(Fairy.class, "fairy");
		retval.put(Giant.class, "giant");
		retval.put(Sphinx.class, "sphinx");
		retval.put(Djinn.class, "djinn");
		retval.put(Griffin.class, "griffin");
		retval.put(Minotaur.class, "minotaur");
		retval.put(Ogre.class, "ogre");
		retval.put(Phoenix.class, "phoenix");
		retval.put(Simurgh.class, "simurgh");
		retval.put(Troll.class, "troll");
		retval.put(Snowbird.class, "snowbird");
		retval.put(Thunderbird.class, "thunderbird");
		retval.put(Pegasus.class, "pegasus");
		retval.put(Unicorn.class, "unicorn");
		retval.put(Kraken.class, "kraken");
		return Collections.unmodifiableMap(retval);
	}

	private static final Set<String> SUPPORTED_TAGS = new HashSet<>(TAG_MAP.values());

	private static final Map<String, IntFunction<? extends Immortal>> SIMPLES = initSimples();

	private static Map<String, IntFunction<? extends Immortal>> initSimples() {
		final Map<String, IntFunction<? extends Immortal>> retval = new HashMap<>();
		retval.put("sphinx", Sphinx::new);
		retval.put("snowbird", Snowbird::new);
		retval.put("thunderbird", Thunderbird::new);
		retval.put("djinn", Djinn::new);
		retval.put("griffin", Griffin::new);
		retval.put("minotaur", Minotaur::new);
		retval.put("ogre", Ogre::new);
		retval.put("phoenix", Phoenix::new);
		retval.put("simurgh", Simurgh::new);
		retval.put("troll", Troll::new);
		retval.put("pegasus", Pegasus::new);
		retval.put("unicorn", Unicorn::new);
		retval.put("kraken", Kraken::new);
		return Collections.unmodifiableMap(retval);
	}

	private MobileFixture createAnimal(final StartElement element) throws SPFormatException {
		final String tag = element.getName().getLocalPart().toLowerCase(); // TODO: drop toLowerCase if we can use equalsIgnoreCase() everywhere below
		final String kind;
		final boolean tracks;
		if ("animal".equalsIgnoreCase(tag)) {
			kind = getParameter(element, "kind");
			// To get the intended meaning of existing maps, we have to parse
			// traces="" as traces="true". If compatibility with existing maps
			// ever becomes unnecessary, I will change the default-value here to
			// simply `false`.
			tracks = getBooleanParameter(element, "traces",
				hasParameter(element, "traces") &&
				getParameter(element, "traces", "").isEmpty());
			if (!tracks) {
				if (IMMORTAL_ANIMALS.contains(kind)) {
					return ImmortalAnimal.parse(kind).apply(getOrGenerateID(element));
				}
				expectAttributes(element, "traces", "id", "count", "talking", "kind",
					"status", "wild", "born", "image");
			}
		} else {
			warnFutureTag(element);
			expectAttributes(element, "id", "count", "image");
			kind = tag;
			tracks = false;
		}
		if (tracks) {
			if ("wild".equals(getParameter(element, "status", "wild"))) {
				expectAttributes(element, "traces", "status", "image", "kind");
			} else {
				expectAttributes(element, "traces", "image", "kind");
			}
				return new AnimalTracks(kind);
		} else {
			// TODO: We'd like default to be 1 inside a unit and -1 outside
			final int count = getIntegerParameter(element, "count", 1);
			return new AnimalImpl(kind,
				getBooleanParameter(element, "talking", false),
				getParameter(element, "status", "wild"), getOrGenerateID(element),
				getIntegerParameter(element, "born", -1), count);
		}
	}

	private MobileFixture readSimple(final String tag, final int idNum) {
		if (!SIMPLES.containsKey(tag)) {
			throw new IllegalArgumentException("Only works for simple immortals");
		}
		return SIMPLES.get(tag).apply(idNum);
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return SUPPORTED_TAGS.stream().anyMatch(tag::equalsIgnoreCase);
	}

	@FunctionalInterface
	private interface StringIntConstructor {
		MobileFixture apply(String str, int num);
	}

	private MobileFixture twoParam(final StartElement element, final StringIntConstructor constr)
			throws SPFormatException {
		expectAttributes(element, "id", "kind", "image");
		return constr.apply(getParameter(element, "kind"), getOrGenerateID(element));
	}

	@Override
	public MobileFixture read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, SUPPORTED_TAGS.toArray(new String[0]));
		final MobileFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "animal":
			retval = createAnimal(element);
			break;
		case "centaur":
			retval = twoParam(element, Centaur::new);
			break;
		case "dragon":
			retval = twoParam(element, Dragon::new);
			break;
		case "fairy":
			retval = twoParam(element, Fairy::new);
			break;
		case "giant":
			retval = twoParam(element, Giant::new);
			break;
		default:
			expectAttributes(element, "image", "id");
			retval = readSimple(element.getName().getLocalPart(), getOrGenerateID(element));
			break;
		}
		spinUntilEnd(element.getName(), stream);
		if (retval instanceof HasMutableImage) {
			((HasMutableImage) retval).setImage(getParameter(element, "image", ""));
		}
		return retval;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final MobileFixture obj, final int indent) throws IOException {
		if (obj instanceof IUnit) {
			throw new IllegalArgumentException("Unit handled elsewhere");
		} else if (obj instanceof AnimalTracks) {
			writeTag(ostream, "animal", indent);
			writeProperty(ostream, "kind", ((AnimalTracks) obj).getKind());
			writeProperty(ostream, "traces", "true");
			writeImageXML(ostream, (HasImage) obj);
		} else if (obj instanceof Animal) {
			writeTag(ostream, "animal", indent);
			writeProperty(ostream, "kind", ((Animal) obj).getKind());
			if (((Animal) obj).isTalking()) {
				writeProperty(ostream, "talking", "true");
			}
			if (!"wild".equals(((Animal) obj).getStatus())) {
				writeProperty(ostream, "status", ((Animal) obj).getStatus());
			}
			writeProperty(ostream, "id", obj.getId());
			if (((Animal) obj).getBorn() >= 0) {
				final Map<String, Integer> maturity = MaturityModel.getMaturityAges();
				final int currentTurn = MaturityModel.getCurrentTurn();
				// Write turn-of-birth if and only if it is fewer turns before the current
				// turn than this kind of animal's age of maturity.
				if (!maturity.containsKey(((Animal) obj).getKind()) ||
							maturity.get(((Animal) obj).getKind()) > (currentTurn - ((Animal) obj).getBorn())) {
					writeProperty(ostream, "born", ((Animal) obj).getBorn());
				}
			}
			if (((Animal) obj).getPopulation() > 1) {
				writeProperty(ostream, "count", ((Animal) obj).getPopulation());
			}
			writeImageXML(ostream, ((Animal) obj));
		} else if (obj instanceof SimpleImmortal || obj instanceof ImmortalAnimal) {
			writeTag(ostream, ((HasKind) obj).getKind(), indent);
			writeProperty(ostream, "id", obj.getId());
			writeImageXML(ostream, (HasImage) obj);
		} else if (TAG_MAP.containsKey(obj.getClass())) {
			writeTag(ostream, TAG_MAP.get(obj.getClass()), indent);
			if (obj instanceof HasKind) {
				writeProperty(ostream, "kind", ((HasKind) obj).getKind());
			}
			writeProperty(ostream, "id", obj.getId());
			if (obj instanceof HasImage) {
				writeImageXML(ostream, (HasImage) obj);
			}
		} else {
			throw new IllegalArgumentException("No tag for " + obj.getShortDescription());
		}
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof MobileFixture && !(obj instanceof IUnit);
	}
}

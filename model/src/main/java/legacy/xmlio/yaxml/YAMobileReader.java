package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.HashSet;
import java.util.function.IntFunction;
import java.io.IOException;

import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import legacy.idreg.IDRegistrar;
import legacy.map.HasMutableImage;
import legacy.map.HasImage;
import legacy.map.HasKind;
import legacy.map.fixtures.mobile.Centaur;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.SimpleImmortal;
import legacy.map.fixtures.mobile.Giant;
import legacy.map.fixtures.mobile.Fairy;
import legacy.map.fixtures.mobile.Dragon;
import legacy.map.fixtures.mobile.MobileFixture;
import legacy.map.fixtures.mobile.Sphinx;
import legacy.map.fixtures.mobile.Djinn;
import legacy.map.fixtures.mobile.Griffin;
import legacy.map.fixtures.mobile.Minotaur;
import legacy.map.fixtures.mobile.Ogre;
import legacy.map.fixtures.mobile.Phoenix;
import legacy.map.fixtures.mobile.Simurgh;
import legacy.map.fixtures.mobile.Troll;
import legacy.map.fixtures.mobile.Immortal;

import static legacy.map.fixtures.mobile.Immortal.IMMORTAL_ANIMALS;

import legacy.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import legacy.map.fixtures.mobile.AnimalImpl;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.Snowbird;
import legacy.map.fixtures.mobile.Thunderbird;
import legacy.map.fixtures.mobile.Pegasus;
import legacy.map.fixtures.mobile.Unicorn;
import legacy.map.fixtures.mobile.Kraken;
import legacy.map.fixtures.mobile.ImmortalAnimal;
import common.xmlio.Warning;
import org.jetbrains.annotations.Nullable;

/**
 * A reader for "mobile fixtures"
 */
/* package */ final class YAMobileReader extends YAAbstractReader<MobileFixture, MobileFixture> {
	public YAMobileReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	private static final Map<Class<? extends MobileFixture>, String> TAG_MAP = initTagMap();

	private static Map<Class<? extends MobileFixture>, String> initTagMap() {
		return Map.ofEntries(Map.entry(Animal.class, "animal"), Map.entry(Centaur.class, "centaur"),
				Map.entry(Dragon.class, "dragon"), Map.entry(Fairy.class, "fairy"),
				Map.entry(Giant.class, "giant"), Map.entry(Sphinx.class, "sphinx"),
				Map.entry(Djinn.class, "djinn"), Map.entry(Griffin.class, "griffin"),
				Map.entry(Minotaur.class, "minotaur"), Map.entry(Ogre.class, "ogre"),
				Map.entry(Phoenix.class, "phoenix"), Map.entry(Simurgh.class, "simurgh"),
				Map.entry(Troll.class, "troll"), Map.entry(Snowbird.class, "snowbird"),
				Map.entry(Thunderbird.class, "thunderbird"), Map.entry(Pegasus.class, "pegasus"),
				Map.entry(Unicorn.class, "unicorn"), Map.entry(Kraken.class, "kraken"));
	}

	private static final Collection<String> SUPPORTED_TAGS = new HashSet<>(TAG_MAP.values());

	private static final Map<String, IntFunction<? extends Immortal>> SIMPLES = initSimples();

	private static Map<String, IntFunction<? extends Immortal>> initSimples() {
		return Map.ofEntries(Map.entry("sphinx", Sphinx::new), Map.entry("snowbird", Snowbird::new),
				Map.entry("thunderbird", Thunderbird::new), Map.entry("djinn", Djinn::new),
				Map.entry("griffin", Griffin::new), Map.entry("minotaur", Minotaur::new),
				Map.entry("ogre", Ogre::new), Map.entry("phoenix", Phoenix::new),
				Map.entry("simurgh", Simurgh::new), Map.entry("troll", Troll::new),
				Map.entry("pegasus", Pegasus::new), Map.entry("unicorn", Unicorn::new),
				Map.entry("kraken", Kraken::new));
	}

	private MobileFixture createAnimal(final StartElement element, final @Nullable Path path) throws SPFormatException {
		final String tag = element.getName().getLocalPart();
		final String kind;
		final boolean tracks;
		if ("animal".equalsIgnoreCase(tag)) {
			kind = getParameter(element, path, "kind");
			// To get the intended meaning of existing maps, we have to parse
			// traces="" as traces="true". If compatibility with existing maps
			// ever becomes unnecessary, I will change the default-value here to
			// simply `false`.
			tracks = getBooleanParameter(element, path, "traces",
					hasParameter(element, "traces") &&
							getParameter(element, "traces", "").isEmpty());
			if (!tracks) {
				if (IMMORTAL_ANIMALS.contains(kind)) {
					return ImmortalAnimal.parse(kind).apply(getOrGenerateID(element, path));
				}
				expectAttributes(element, path, "traces", "id", "count", "talking", "kind",
						"status", "wild", "born", "image");
			}
		} else {
			warnFutureTag(element, path);
			expectAttributes(element, path, "id", "count", "image");
			kind = tag.toLowerCase();
			tracks = false;
		}
		if (tracks) {
			if ("wild".equals(getParameter(element, "status", "wild"))) {
				expectAttributes(element, path, "traces", "status", "image", "kind");
			} else {
				expectAttributes(element, path, "traces", "image", "kind");
			}
			return new AnimalTracks(kind);
		} else {
			// TODO: We'd like default to be 1 inside a unit and -1 outside
			final int count = getIntegerParameter(element, path, "count", 1);
			return new AnimalImpl(kind,
					getBooleanParameter(element, path, "talking", false),
					getParameter(element, "status", "wild"), getOrGenerateID(element, path),
					getIntegerParameter(element, path, "born", -1), count);
		}
	}

	private static MobileFixture readSimple(final String tag, final int idNum) {
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

	private MobileFixture twoParam(final StartElement element, final @Nullable Path path, final StringIntConstructor constr)
			throws SPFormatException {
		expectAttributes(element, path, "id", "kind", "image");
		return constr.apply(getParameter(element, path, "kind"), getOrGenerateID(element, path));
	}

	@Override
	public MobileFixture read(final StartElement element, final @Nullable Path path, final QName parent,
	                          final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, path, parent, SUPPORTED_TAGS);
		final MobileFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
			case "animal" -> retval = createAnimal(element, path);
			case "centaur" -> retval = twoParam(element, path, Centaur::new);
			case "dragon" -> retval = twoParam(element, path, Dragon::new);
			case "fairy" -> retval = twoParam(element, path, Fairy::new);
			case "giant" -> retval = twoParam(element, path, Giant::new);
			default -> {
				expectAttributes(element, path, "image", "id");
				retval = readSimple(element.getName().getLocalPart(), getOrGenerateID(element, path));
			}
		}
		spinUntilEnd(element.getName(), path, stream);
		if (retval instanceof final HasMutableImage hmi) {
			hmi.setImage(getParameter(element, "image", ""));
		}
		return retval;
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final MobileFixture obj, final int indent)
			throws IOException {
		switch (obj) {
			case final IUnit unitMembers -> throw new IllegalArgumentException("Unit handled elsewhere");
			case final AnimalTracks at -> {
				writeTag(ostream, "animal", indent);
				writeProperty(ostream, "kind", at.getKind());
				writeProperty(ostream, "traces", "true");
				writeImageXML(ostream, at);
			}
			case final Animal a -> {
				writeTag(ostream, "animal", indent);
				writeProperty(ostream, "kind", a.getKind());
				if (a.isTalking()) {
					writeProperty(ostream, "talking", "true");
				}
				if (!"wild".equals(a.getStatus())) {
					writeProperty(ostream, "status", a.getStatus());
				}
				writeProperty(ostream, "id", obj.getId());
				if (0 <= a.getBorn()) {
					final Map<String, Integer> maturity = MaturityModel.getMaturityAges();
					final int currentTurn = MaturityModel.getCurrentTurn();
					// Write turn-of-birth if and only if it is fewer turns before the current
					// turn than this kind of animal's age of maturity.
					if (!maturity.containsKey(a.getKind()) ||
							maturity.get(a.getKind()) > (currentTurn - a.getBorn())) {
						writeProperty(ostream, "born", a.getBorn());
					}
				}
				if (1 < a.getPopulation()) {
					writeProperty(ostream, "count", a.getPopulation());
				}
				writeImageXML(ostream, a);
			}
			case final SimpleImmortal simpleImmortal -> {
				writeTag(ostream, ((HasKind) obj).getKind(), indent);
				writeProperty(ostream, "id", obj.getId());
				writeImageXML(ostream, (HasImage) obj);
			}
			case final ImmortalAnimal immortalAnimal -> {
				writeTag(ostream, ((HasKind) obj).getKind(), indent);
				writeProperty(ostream, "id", obj.getId());
				writeImageXML(ostream, (HasImage) obj);
			}
			default -> {
				if (TAG_MAP.containsKey(obj.getClass())) {
					writeTag(ostream, TAG_MAP.get(obj.getClass()), indent);
					if (obj instanceof final HasKind hk) {
						writeProperty(ostream, "kind", hk.getKind());
					}
					writeProperty(ostream, "id", obj.getId());
					if (obj instanceof final HasImage hi) {
						writeImageXML(ostream, hi);
					}
				} else {
					throw new IllegalArgumentException("No tag for " + obj.getShortDescription());
				}
			}
		}
		closeLeafTag(ostream);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof MobileFixture && !(obj instanceof IUnit);
	}
}

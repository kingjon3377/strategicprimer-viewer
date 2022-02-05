package impl.xmlio;

import static lovelace.util.SingletonRandom.SINGLETON_RANDOM;

import org.jetbrains.annotations.Nullable;

import org.javatuples.Pair;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import static lovelace.util.AssertAny.assertAny;
import lovelace.util.MissingFileException;
import lovelace.util.MalformedXMLException;

import common.idreg.DuplicateIDException;
import common.map.HasMutablePortrait;
import common.map.HasPortrait;
import common.map.Player;
import common.map.MutablePlayer;
import common.map.PlayerImpl;
import common.map.MapDimensionsImpl;
import common.map.Point;
import common.map.River;
import common.map.TileType;
import common.map.HasMutableImage;
import common.map.PlayerCollection;
import common.map.IMutableMapNG;
import common.map.IMapNG;
import common.map.HasNotes;
import common.map.SPMapNG;
import common.map.Direction;
import common.map.fixtures.TextFixture;
import common.map.fixtures.Implement;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Ground;
import common.map.fixtures.Quantity;
import common.map.fixtures.explorable.Portal;
import common.map.fixtures.explorable.AdventureFixture;
import common.map.fixtures.explorable.Battlefield;
import common.map.fixtures.explorable.Cave;
import common.map.fixtures.mobile.Centaur;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.mobile.SimpleImmortal;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Giant;
import common.map.fixtures.mobile.Fairy;
import common.map.fixtures.mobile.Dragon;
import common.map.fixtures.mobile.Griffin;
import common.map.fixtures.mobile.Ogre;
import common.map.fixtures.mobile.Worker;
import static common.map.fixtures.mobile.Immortal.IMMORTAL_ANIMALS;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.Skill;
import common.map.fixtures.mobile.worker.RaceFactory;
import common.map.fixtures.resources.FieldStatus;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.StoneKind;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.Town;
import common.map.fixtures.towns.Fortification;
import common.map.fixtures.towns.City;
import common.map.fixtures.towns.FortressImpl;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.CommunityStats;

import static impl.xmlio.ISPReader.SP_NAMESPACE;

import common.xmlio.Warning;
import impl.xmlio.exceptions.UnsupportedTagException;
import impl.xmlio.exceptions.UnwantedChildException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.MissingChildException;
import impl.xmlio.exceptions.DeprecatedPropertyException;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Predicate;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import common.map.HasImage;
import java.math.BigDecimal;
import java.util.EnumSet;
import common.xmlio.SPFormatException;
import common.map.fixtures.mobile.Sphinx;
import common.map.fixtures.mobile.Djinn;
import common.map.fixtures.mobile.Troll;
import common.map.fixtures.mobile.Simurgh;
import common.map.fixtures.mobile.Phoenix;
import common.map.fixtures.mobile.Minotaur;
import common.map.HasKind;

// TODO: Make methods static where possible
// FIXME: A lot of the assertions in this class have expected and actual values backwards
public final class TestXMLIO {
	private static final Set<String> races = Collections.unmodifiableSet(new HashSet<>(RaceFactory.RACES));
	private static final List<String> animalStatuses = Collections.unmodifiableList(Arrays.asList(
		"wild", "semi-domesticated", "domesticated", "tame"));
	private static final List<String> treeTypes = Collections.unmodifiableList(Arrays.asList("oak",
		"larch", "terebinth", "elm", "skybroom", "silver maple"));
	private static final List<String> fieldTypes = Collections.unmodifiableList(Arrays.asList("wheat",
		"amaranth", "bluegrass", "corn", "winter wheat"));
	private static final List<String> minerals = Collections.unmodifiableList(Arrays.asList("coal",
		"platinum", "oil", "diamonds", "natural gas"));

	/**
	 * The "filename" to give to map-readers when they require one.
	 */
	private static final Path FAKE_FILENAME = Paths.get("");

	/**
	 * The map readers to test each other against.
	 *
	 * TODO: Extract interface for ISPReader&amp;IMapReader, so we don't
	 * have to maintain separate names for the same objects.
	 */
	private final List<ISPReader> spReaders = Collections.unmodifiableList(Arrays.asList(
		TestReaderFactory.getOldSPReader(), TestReaderFactory.getNewSPReader()));
	private final List<IMapReader> mapReaders = Collections.unmodifiableList(Arrays.asList(
		TestReaderFactory.getOldMapReader(), TestReaderFactory.getNewMapReader()));

	/**
	 * Assert that the given XML will produce the given kind of warning and
	 * that the warning satisfies the given additional assertions. If
	 * {@link desideratum} is {@code null}, assert that the exception
	 * is always thrown; if not, assert that the XML will fail with
	 * warnings made fatal, but will pass and produce {@link desideratum}
	 * with warnings ignored.
	 *
	 * TODO: Split 'fatal error' and 'warning' cases into separate methods?
	 */
	@SafeVarargs
	private final <Type, Expectation extends Exception> void assertFormatIssue(
			final ISPReader reader, final String xml, @Nullable final Type desideratum,
			final Class<Expectation> exceptionClass, final Consumer<Expectation>... checks)
			throws SPFormatException, MalformedXMLException, IOException {
		if (desideratum != null) { // TODO: invert condition
			try (StringReader stringReader = new StringReader(xml)) {
				Type returned = reader.readXML(FAKE_FILENAME, stringReader,
					Warning.IGNORE);
				assertEquals(desideratum, returned,
					"Parsed value should be as expected with warnings ignored.");
			}
			try (StringReader stringReader = new StringReader(xml)) {
				reader.<Type>readXML(FAKE_FILENAME, stringReader, Warning.DIE);
				fail("Expected a fatal warning");
			} catch (final RuntimeException except) {
				Throwable cause = except.getCause();
				assertTrue(exceptionClass.isInstance(cause), "Exception should be of the right type");
				for (Consumer<Expectation> check : checks) {
					check.accept((Expectation) cause);
				}
			} catch (final Throwable except) {
				assertTrue(exceptionClass.isInstance(except), "Exception should be of the right type");
				for (Consumer<Expectation> check : checks) {
					check.accept((Expectation) except);
				}
			}
		} else {
			try (StringReader stringReader = new StringReader(xml)) {
				reader.<Type>readXML(FAKE_FILENAME, stringReader, Warning.IGNORE);
				fail(String.format("Expected a(n) %s to be thrown",
					exceptionClass.getName()));
			} catch (final Exception except) {
				assertTrue(exceptionClass.isInstance(except), "Exception should be of the right type");
				for (Consumer<Expectation> check : checks) {
					check.accept((Expectation) except);
				}
			}
		}
	}

	/**
	 * Assert that reading the given XML will produce an {@link
	 * UnsupportedTagException}. If it's only supposed to be a warning,
	 * assert that it'll pass with warnings disabled but fail with warnings
	 * made fatal.
	 */
	private <Type> void assertUnsupportedTag(final String xml, final String tag, @Nullable final Type desideratum)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			this.assertFormatIssue(reader, xml, desideratum,
				UnsupportedTagException.class,
				(except) -> assertEquals(tag, except.getTag().getLocalPart(),
					"Unsupported tag was the tag we expected"));
		}
	}

	/**
	 * Assert that reading the given XML will produce an {@link
	 * UnwantedChildException}. If it's only supposed to be a warning,
	 * assert that it'll pass with warnings disabled but fail with warnings
	 * made fatal.
	 */
	private <Type> void assertUnwantedChild(final String xml, @Nullable final Type desideratum)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			this.assertFormatIssue(reader, xml, desideratum,
				UnwantedChildException.class);
		}
	}

	/**
	 * Assert that reading the given XML will give a {@link
	 * MissingPropertyException}. If it's only supposed to be a warning,
	 * assert that it'll pass with warnings disabled but object with them
	 * made fatal.
	 */
	private <Type> void assertMissingProperty(final String xml, final String property,
	                                          @Nullable final Type desideratum)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			this.assertFormatIssue(reader, xml, desideratum,
				MissingPropertyException.class,
				(except) -> assertEquals(property, except.getParam(),
					"Missing property should be the one we're expecting"));
		}
	}

	/**
	 * Assert that reading the given XML will give a MissingChildException.
	 */
	private <Type> void assertMissingChild(final String xml)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			this.<Type, MissingChildException>assertFormatIssue(reader, xml, null,
				MissingChildException.class);
		}
	}

	/**
	 * Assert that reading the given XML will give a {@link
	 * DeprecatedPropertyException}. If it's only supposed to be a warning,
	 * assert that it'll pass with warnings disabled but object with them
	 * made fatal.
	 */
	private <Type> void assertDeprecatedProperty(final String xml, final String deprecated, final String preferred,
	                                             final String tag, @Nullable final Type desideratum)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			this.assertFormatIssue(reader, xml, desideratum,
				DeprecatedPropertyException.class,
				(except) -> {
					assertEquals(deprecated, except.getOld(),
						"Missing property should be the one we're expecting");
					assertEquals(tag, except.getTag().getLocalPart(),
						"Missing property should be on the tag we expect");
					assertEquals(preferred, except.getPreferred(),
						"Preferred form should be as expected");
				});
		}
	}

	/**
	 * Create the XML-serialized representation of an object.
	 *
	 * TODO: It Would Be Nice to get rid of the Boolean parameter, perhaps replacing it with an enum
	 *
	 * @param obj The object to serialize
	 * @param deprecated Whether to use the deprecated i.e. one-generation-back writer
	 */
	private String createSerializedForm(final Object obj, final boolean deprecated)
			throws SPFormatException, MalformedXMLException, IOException {
		final StringBuilder writer = new StringBuilder();
		if (deprecated) {
			TestReaderFactory.getOldWriter().writeSPObject(writer::append, obj);
		} else {
			TestReaderFactory.getNewWriter().writeSPObject(writer::append, obj);
		}
		return writer.toString();
	}

	/**
	 * Assert that the serialized form of the given object will deserialize without error.
	 */
	private void assertSerialization(final String message, final Object obj)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization(message, obj, Warning.DIE);
	}

	/**
	 * Assert that the serialized form of the given object will deserialize without error.
	 */
	private void assertSerialization(final String message, final Object obj, final Warning warner)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			try (StringReader stringReader =
					new StringReader(createSerializedForm(obj, false))) {
				assertEquals(obj, reader.readXML(FAKE_FILENAME, stringReader,
					warner), message);
			}
			try (StringReader stringReader =
					new StringReader(createSerializedForm(obj, true))) {
				assertEquals(obj, reader.readXML(FAKE_FILENAME, stringReader,
					warner), message);
			}
		}
	}

	/**
	 * Assert that the serialized form of the given object, using both
	 * writers, will contain the given string.
	 */
	private void assertSerializedFormContains(final Object obj, final String expected, final String message)
			throws SPFormatException, MalformedXMLException, IOException {
		// TODO: Is there a JUnit assertContains() or similar?
		assertTrue(createSerializedForm(obj, false).contains(expected), message);
		assertTrue(createSerializedForm(obj, true).contains(expected), message);
	}

	/**
	 * Assert that the given object, if serialized and deserialized, will
	 * have its image property preserved. We modify that property, but set
	 * it back to the original value before exiting this method.
	 */
	private void assertImageSerialization(final String message, final HasMutableImage obj)
			throws SPFormatException, MalformedXMLException, IOException {
		String oldImage = obj.getImage();
		for (ISPReader reader : spReaders) {
			for (Boolean deprecated : Arrays.asList(false, true)) {
				obj.setImage("xyzzy"); // TODO: Should randomly generate a string
				try (StringReader stringReader =
						new StringReader(createSerializedForm(obj, deprecated))) {
					assertEquals(obj.getImage(), reader.<HasMutableImage>readXML(
						FAKE_FILENAME, stringReader, Warning.IGNORE).getImage(),
						message);
				}
				obj.setImage(obj.getDefaultImage());
				assertFalse(createSerializedForm(obj, deprecated).contains("image="),
					"Default image should not be written");
				obj.setImage("");
				assertFalse(createSerializedForm(obj, deprecated).contains("image="),
					"Empty image should not be written");
			}
		}
		obj.setImage(oldImage);
	}

	/**
	 * Assert that the given object's notes property will be preserved when serialized and deserialized.
	 *
	 * TODO: Initialize with some notes, or add assertions that some initialization has taken place
	 */
	private void assertNotesSerialization(final String message, final HasNotes obj)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			for (Boolean deprecated : Arrays.asList(false, true)) {
				try (StringReader stringReader =
						new StringReader(createSerializedForm(obj, deprecated))) {
					HasNotes read = reader.readXML(FAKE_FILENAME,
						stringReader, Warning.IGNORE);
					for (Integer player : obj.getNotesPlayers()) {
						assertEquals(obj.getNote(player), read.getNote(player),
							message);
					}
					for (Integer player : read.getNotesPlayers()) {
						assertEquals(obj.getNote(player), read.getNote(player),
							message);
					}
				}
			}
		}
	}

	/**
	 * Assert that the given object, if serialized and deserialized, will
	 * have its portrait property preserved. We modify that property, but
	 * set it back to the original value before exiting this method.
	 */
	private void assertPortraitSerialization(final String message, final HasMutablePortrait obj)
			throws SPFormatException, MalformedXMLException, IOException {
		String oldPortrait = obj.getPortrait();
		for (ISPReader reader : spReaders) {
			for (Boolean deprecated : Arrays.asList(false, true)) {
				obj.setPortrait("xyzzy");
				try (StringReader stringReader =
						new StringReader(createSerializedForm(obj, deprecated))) {
					assertEquals(obj.getPortrait(), reader.<HasPortrait>readXML(
						FAKE_FILENAME, stringReader, Warning.IGNORE).getPortrait(),
						message);
				}
				obj.setPortrait("");
				assertFalse(createSerializedForm(obj, deprecated).contains("portrait="),
					"Empty portrait should not be written");
			}
		}
		obj.setPortrait(oldPortrait);
	}

	private <Type> void assertForwardDeserialization(final String message, final String xml,
	                                                 final Predicate<Type> assertion)
			throws SPFormatException, MalformedXMLException, IOException {
		assertForwardDeserialization(message, xml, assertion, Warning.DIE);
	}

	/**
	 * Assert that a "forward idiom"---an idiom that we do not yet (or,
	 * conversely, anymore) produce, but want to accept---will be handled
	 * properly by both readers.
	 *
	 * TODO: should {@link assertion} be a Consumer instead of a Predicate?
	 *
	 * @param message The assertion message
	 * @param xml The serialized form
	 * @param assertion A lambda to check the state of the deserialized object
	 * @param warner The warning level to use for this assertion
	 */
	private <Type> void assertForwardDeserialization(final String message, final String xml,
	                                                 final Predicate<Type> assertion, final Warning warner)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			try (StringReader stringReader = new StringReader(xml)) {
				assertTrue(assertion.test(reader.readXML(FAKE_FILENAME, stringReader,
					warner)), message);
			}
		}
	}

	/**
	 * Assert that two serialized forms are equivalent, using both readers.
	 * @param message The assertion message to use
	 * @param firstForm The first serialized form
	 * @param secondForm The second serialized form
	 * @param warningLevel The warning level to use
	 */
	private void assertEquivalentForms(final String message, final String firstForm, final String secondForm,
	                                   final Warning warningLevel) throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			try (StringReader firstReader = new StringReader(firstForm);
					StringReader secondReader = new StringReader(secondForm)) {
				assertEquals((Object) reader.readXML(FAKE_FILENAME, firstReader, warningLevel),
					reader.readXML(FAKE_FILENAME, secondReader, warningLevel),
					message);
			}
		}
	}

	/**
	 * Assert that a map is properly deserialized (by the main map-deserialization methods).
	 */
	private void assertMapDeserialization(final String message, final IMapNG expected, final String xml)
			throws SPFormatException, MalformedXMLException, IOException {
		for (IMapReader reader : mapReaders) {
			try (StringReader stringReader = new StringReader(xml)) {
				assertEquals(expected, reader.readMapFromStream(FAKE_FILENAME, stringReader,
					Warning.DIE), message);
			}
		}
	}

	/**
	 * Assert that the given XML will produce warnings about duplicate IDs.
	 */
	private <Type> void assertDuplicateID(final String xml, final Type desideratum)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			this.assertFormatIssue(reader, xml, desideratum,
				DuplicateIDException.class);
		}
	}

	@SafeVarargs
	private static final Predicate<Throwable> instanceOfAny(final Class<? extends Throwable>... types) {
		return (except) -> {
			for (Class<? extends Throwable> type : types) {
				if (type.isInstance(except)) {
					return true;
				}
			}
			if (except instanceof RuntimeException) {
				return instanceOfAny(types).test(except.getCause());
			}
			return false;
		};
	}

	/**
	 * Assert that a given piece of XML will fail to deserialize with XML
	 * format errors, not SP format errors.
	 */
	private void assertInvalid(final String xml)
			throws SPFormatException, MalformedXMLException, IOException {
		for (ISPReader reader : spReaders) {
			this.assertFormatIssue(reader, xml, null, Exception.class,
				except -> assertTrue(instanceOfAny(NoSuchElementException.class,
						IllegalArgumentException.class,
						MalformedXMLException.class,
						MissingFileException.class).test(except),
					String.format(
						"Exception is of an expected type: was %s",
						except.getClass().getName())));
		}
	}

	/**
	 * Encapsulate the given string in a "tile" tag inside a "map" tag.
	 */
	private static String encapsulateTileString(final String str) {
		return String.format("<map version=\"2\" rows=\"2\" columns=\"2\">%n<tile row=\"1\" column=\"1\" kind=\"plains\">%s</tile></map>", str);
	}

	// TODO: Add .limit() after .stream(), and take int to pass to it
	static <T> Collector<T, ?, Stream<T>> toShuffledStream() {
		return Collectors.collectingAndThen(Collectors.toList(), collected -> {
			Collections.shuffle(collected);
			return collected.stream();
		});
	}

	private static Stream<Arguments> testVillageWantsName() {
		return Stream.of(TownStatus.values()).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
				races.stream().collect(toShuffledStream()).limit(3).flatMap(c ->
					Stream.of(true, false).map(d -> Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test that deserializing a {@link Village} without a name will cause a warning.
	 *
	 * TODO: if test fails because boxed Integer and Boolean can't be assigned to unboxed primitives,
	 * change parameter types in this method's signature
	 */
	@ParameterizedTest
	@MethodSource
	public void testVillageWantsName(final TownStatus status, final int id, final String race, final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		final Village village = new Village(status, "", id, new PlayerImpl(-1, ""), race);
		assertMissingProperty(createSerializedForm(village, deprecatedWriter), "name", village);
	}

	private static Stream<Arguments> testBasicVillageSerialization() {
		return treeTypes.stream().collect(toShuffledStream()).limit(2).flatMap(a ->
			Stream.of(TownStatus.values()).flatMap(b ->
				races.stream().collect(toShuffledStream()).limit(3).flatMap(c ->
					SINGLETON_RANDOM.ints().boxed().limit(2).map(d ->
						Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test basic (de)serialization of {@link Village villages}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testBasicVillageSerialization(final String name, final TownStatus status, final String race, final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		Player owner = new PlayerImpl(-1, "");
		Village village = new Village(status, name, id, owner, race);
		assertSerialization("Basic Village serialization",
			new Village(status, name, id, owner, race));
		this.<Village>assertUnwantedChild(String.format(
			"<village status=\"%s\"><village /></village>", status.toString()), null);
		this.<Village>assertMissingProperty("<village />", "status", null);
		this.assertMissingProperty(String.format(
			"<village name=\"%s\" status=\"%s\" />", name, status.toString()), "id",
			new Village(status, name, 0, new PlayerImpl(-1, "Independent"), "dwarf"));
		this.assertMissingProperty(String.format(
				"<village race=\"%s\" name=\"%s\" status=\"%s\" id=\"%d\" />", race,
				name, status.toString(), id),
			"owner", new Village(status, name, id, new PlayerImpl(-1, "Independent"), race));
		assertImageSerialization("Village image property is preserved", village);
		assertPortraitSerialization("Village portrait property is preserved", village);
	}

	private static Stream<Arguments> testVillagePopulationSerialization() {
		return Stream.of(TownStatus.values()).flatMap(a ->
			races.stream().collect(toShuffledStream()).limit(3).map(b ->
				Arguments.of(a, b, SINGLETON_RANDOM.nextInt(), SINGLETON_RANDOM.nextInt(),
					SINGLETON_RANDOM.nextInt(), SINGLETON_RANDOM.nextInt(),
					SINGLETON_RANDOM.nextInt(), SINGLETON_RANDOM.nextInt(),
					SINGLETON_RANDOM.nextInt())));
	}

	/**
	 * Test (de)serialization of {@link Village villages'} {@link CommunityStats population details}."
	 */
	@ParameterizedTest
	@MethodSource
	public void testVillagePopulationSerialization(final TownStatus status, final String race, final int id,
	                                               final int populationSize, final int workedField, final int producedId, final int producedQty,
	                                               final int consumedId, final int consumedQty)
			throws SPFormatException, MalformedXMLException, IOException {
		assumeTrue(populationSize >= 0, "Population can't be negative");
		assumeTrue(workedField >= 0, "Field ID won't ever be negative");
		assumeTrue(producedId >= 0, "Production ID won't ever be negative");
		assumeTrue(producedQty >= 0, "Quantity can't be negative");
		assumeTrue(consumedId >= 0, "Consumption ID won't ever be negative");
		assumeTrue(consumedQty >= 0, "Quantity can't be negative");
		Village village = new Village(status, "villageName", id, new PlayerImpl(-1, ""), race);
		CommunityStats pop = new CommunityStats(populationSize);
		village.setPopulation(pop);
		assertSerialization("Village can have community stats", village);
		// FIXME: That doesn't guarantee that Village#equals checks
		// 'population' ... try testing that it doesn't equal a
		// different one
		pop.addWorkedField(workedField);
		// TODO: Here and below, randomize strings in production, consumption, and skills
		// TODO: We'd like to randomize number of skills, number of worked fields, etc.
		pop.getYearlyProduction().add(new ResourcePileImpl(producedId, "prodKind", "production",
			new Quantity(producedQty, "single units")));
		pop.getYearlyConsumption().add(new ResourcePileImpl(consumedId, "consKind", "consumption",
			new Quantity(consumedQty, "double units")));
		assertSerialization("Village stats can have both production and consumption",
			village);
	}

	// FIXME: Create helper method for 'stream of n ints', other patterns
	private static Stream<Arguments> testCityWantsName() {
		return Stream.of(TownSize.values()).flatMap(a -> Stream.of(TownStatus.values()).flatMap(b ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(c ->
				SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(d ->
					Stream.of(true, false).map(e -> Arguments.of(a, b, c, d, e))))));
	}

	/**
	 * Test that deserializing a {@link City} without a name will cause a warning.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCityWantsName(final TownSize size, final TownStatus status, final int id, final int dc,
	                              final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		City city = new City(status, size, dc, "", id, new PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(city, deprecatedWriter), "name", city);
	}

	// FIXME: Create helper method for 'stream of n ints', other patterns
	private static Stream<Arguments> testCitySerialization() {
		return Stream.of(TownSize.values()).flatMap(a -> Stream.of(TownStatus.values()).flatMap(b ->
			treeTypes.stream().collect(toShuffledStream()).limit(2).map(c ->
				Arguments.of(a, b, SINGLETON_RANDOM.nextInt(),
					SINGLETON_RANDOM.nextInt(), c))));
	}

	/**
	 * Test basic (de)serialization of {@link City cities}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCitySerialization(final TownSize size, final TownStatus status, final int id, final int dc, final String name)
			throws SPFormatException, MalformedXMLException, IOException {
		Player owner = new PlayerImpl(-1, "");
		assertSerialization("City serialization", new City(status, size, dc, name, id, owner));
		City city = new City(status, size, dc, "", id, owner);
		this.<City>assertUnwantedChild(String.format(
			"<city status=\"%s\" size=\"%s\" name=\"%s\" dc=\"%d\"><troll /></city>",
			status.toString(), size.toString(), name, dc), null);
		this.assertMissingProperty(String.format(
			"<city status=\"%s\" size=\"%s\" name=\"%s\" dc=\"%d\" id=\"%d\" />",
			status.toString(), size.toString(), name, dc, id), "owner",
			new City(status, size, dc, name, id, new PlayerImpl(-1, "Independent")));
		assertImageSerialization("City image property is preserved", city);
		assertPortraitSerialization("City portrait property is preserved", city);
	}

	private static Stream<Arguments> testCityPopulationSerialization() {
		return treeTypes.stream().collect(toShuffledStream()).limit(2).flatMap(a ->
			Stream.of(TownSize.values()).flatMap(b -> Stream.of(TownStatus.values()).flatMap(c ->
				races.stream().collect(toShuffledStream()).limit(3).map(d ->
					Arguments.of(a, b, c, d, SINGLETON_RANDOM.nextInt(),
						SINGLETON_RANDOM.nextInt(), SINGLETON_RANDOM.nextInt(),
						SINGLETON_RANDOM.nextInt(), SINGLETON_RANDOM.nextInt(),
						SINGLETON_RANDOM.nextInt(), SINGLETON_RANDOM.nextInt())))));
	}

	/**
	 * Test (de)serialization of {@link City cities'} {@link CommunityStats population details}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCityPopulationSerialization(final String name, final TownSize size, final TownStatus status,
	                                            final String race, final int id, final int dc, final int populationSize, final int workedField,
	                                            final int skillLevel, final int producedId, final int producedQty)
			throws SPFormatException, MalformedXMLException, IOException {
		assumeTrue(populationSize >= 0, "Population can't be negative");
		assumeTrue(workedField >= 0, "Field ID won't ever be negative");
		assumeTrue(skillLevel >= 0, "Skill level can't be negative");
		assumeTrue(producedId >= 0, "Production ID won't ever be negative");
		assumeTrue(producedQty >= 0, "Quantity can't be negative");
		Player owner = new PlayerImpl(-1, "");
		City city = new City(status, size, dc, name, id, owner);
		CommunityStats population = new CommunityStats(populationSize);
		population.addWorkedField(workedField);
		population.setSkillLevel("citySkill", skillLevel);
		population.getYearlyConsumption().add(new ResourcePileImpl(producedId, "cityResource",
			"citySpecific", new Quantity(producedQty, "cityUnit")));
		city.setPopulation(population);
		assertSerialization("Community-stats can be serialized", population);
		assertSerialization("City can have community-stats", city);
		// FIXME: Verify that deserialized has stats, don't rely on City::equals
	}

	/**
	 * Test that deserializing a {@link Fortification} without a name will trigger a warning.
	 */
	@ParameterizedTest
	@MethodSource("testCityWantsName")
	public void testFortificationWantsName(final TownSize size, final TownStatus status, final int id, final int dc,
	                                       final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		Fortification fort = new Fortification(status, size, dc, "", id, new PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(fort, deprecatedWriter), "name", fort);
	}

	/**
	 * Test basic {@link Fortification} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testFortificationSerialization(final TownSize size, final TownStatus status, final int id, final int dc,
	                                           final String name) throws SPFormatException, MalformedXMLException, IOException {
		Player owner = new PlayerImpl(-1, "");
		assertSerialization("Fortification serialization",
			new Fortification(status, size, dc, name, id, owner));
		Fortification fort = new Fortification(status, size, 30, "", 3, owner);
		this.<Fortification>assertUnwantedChild(String.format(
			"<fortification status=\"%s\" size=\"%s\" name=\"%s\" dc=\"%d\"><troll /></fortification>",
			status.toString(), size.toString(), name, dc), null);
		this.assertMissingProperty(String.format(
				"<fortification status=\"%s\" size=\"%s\" name=\"%s\" dc=\"%d\" id=\"%d\" />",
				status.toString(), size.toString(), name, dc, id), "owner",
			new Fortification(status, size, dc, name, id, new PlayerImpl(-1, "Independent")));
		assertImageSerialization("Fortification image property is preserved", fort);
		assertPortraitSerialization("Fortification portrait property is preserved", fort);
	}

	/**
	 * Test (de)serialization of {@link Fortification fortifications'}
	 * {@link CommunityStats population details}.
	 */
	@ParameterizedTest
	@MethodSource("testCityPopulationSerialization")
	public void testFortificationPopulationSerialization(final String name, final TownSize size, final TownStatus status,
	                                                     final String race, final int id, final int dc, final int populationSize, final int workedField,
	                                                     final int skillLevel, final int producedId, final int producedQty)
			throws SPFormatException, MalformedXMLException, IOException {
		assumeTrue(populationSize >= 0, "Population can't be negative");
		assumeTrue(workedField >= 0, "Field ID won't ever be negative");
		assumeTrue(skillLevel >= 0, "Skill level can't be negative");
		assumeTrue(producedId >= 0, "Production ID won't ever be negative");
		assumeTrue(producedQty >= 0, "Quantity can't be negative");
		Player owner = new PlayerImpl(-1, "");
		Fortification fort = new Fortification(status, size, dc, name, id, owner);
		CommunityStats population = new CommunityStats(populationSize);
		population.addWorkedField(workedField);
		population.addWorkedField((workedField * 13) % 31);
		population.setSkillLevel("fortSkill", skillLevel);
		population.getYearlyProduction().add(new ResourcePileImpl(producedId, "fortResource",
			"fortSpecific", new Quantity(1, "fortUnit")));
		fort.setPopulation(population);
		assertSerialization("Fortification can have community-stats", fort);
	}

	/**
	 * Test that deserializing a {@link Town} without a name triggers a warning.
	 */
	@ParameterizedTest
	@MethodSource("testCityWantsName")
	public void testTownWantsName(final TownSize size, final TownStatus status, final int id, final int dc,
	                              final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		Town town = new Town(status, size, dc, "", id, new PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(town, deprecatedWriter), "name", town);
	}

	/**
	 * Test basic {@link Town} (de)serialization.
	 *
	 * TODO: Split and further randomize this and further tests
	 */
	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testTownSerialization(final TownSize size, final TownStatus status, final int id, final int dc, final String name)
			throws SPFormatException, MalformedXMLException, IOException {
		Player owner = new PlayerImpl(-1, "");
		assertSerialization("Town serialization test", new Town(status, size, dc, name, id, owner));
		Town town = new Town(status, size, dc, name, id, owner);
		this.<Town>assertUnwantedChild(String.format(
			"<town status=\"%s\" size=\"%s\" name=\"%s\" dc=\"%s\"><troll /></town>",
			status.toString(), size.toString(), name, dc), null);
		this.assertMissingProperty(String.format(
				"<town status=\"%s\" size=\"%s\" name=\"%s\" dc=\"%d\" id=\"%d\" />",
				status.toString(), size.toString(), name, dc, id), "owner",
			new Town(status, size, dc, name, id, new PlayerImpl(-1, "Independent")));
		assertImageSerialization("Town image property is preserved", town);
		assertPortraitSerialization("Town portrait property is preserved", town);
		CommunityStats population = new CommunityStats(3);
		population.addWorkedField(9);
		population.addWorkedField(23);
		population.setSkillLevel("townSkill", 3);
		population.setSkillLevel("secondSkill", 5);
		population.getYearlyProduction().add(new ResourcePileImpl(5, "townResource", "townSpecific",
			new Quantity(1, "TownUnit")));
		population.getYearlyProduction().add(new ResourcePileImpl(8, "townResource", "secondSpecific",
			new Quantity(2, "townUnit")));
		town.setPopulation(population);
		assertSerialization("Fortification can have community-stats", town);
	}

	/**
	 * Test {@link StoneDeposit} (de)serialization.
	 */
	@ParameterizedTest
	@EnumSource(StoneKind.class)
	public void testStoneSerialization(final StoneKind kind)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("First StoneDeposit test, kind: " + kind, new StoneDeposit(kind, 8, 1));
		assertSerialization("Second StoneDeposit test, kind: " + kind, new StoneDeposit(kind, 15, 2));
		assertImageSerialization("Stone image property is preserved", new StoneDeposit(kind, 10, 3));
	}

	private static Stream<Arguments> testOldStoneIdiom() {
		return Stream.of(StoneKind.values()).flatMap(a -> Stream.of(true, false).map(b ->
			Arguments.of(a, b)));
	}

	/**
	 * Test deserialization of the old XML idiom for {@link StoneDeposit stone deposits}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testOldStoneIdiom(final StoneKind kind, final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		StoneDeposit thirdDeposit = new StoneDeposit(kind, 10, 3);
		this.assertDeprecatedProperty(
			createSerializedForm(thirdDeposit, deprecatedWriter)
				.replace("kind", "stone"), "stone", "kind", "stone", thirdDeposit);
	}

	/**
	 * Test that {@link StoneDeposit} deserialization rejects invalid input.
	 */
	@ParameterizedTest
	@EnumSource(StoneKind.class)
	public void testStoneSerializationErrors(final StoneKind kind)
			throws SPFormatException, MalformedXMLException, IOException {
		this.<StoneDeposit>assertUnwantedChild(String.format(
			"<stone kind=\"%s\" dc=\"10\"><troll /></stone>", kind.toString()), null);
		this.<StoneDeposit>assertMissingProperty(String.format(
			"<stone kind=\"%s\" />", kind.toString()), "dc", null);
		this.<StoneDeposit>assertMissingProperty("<stone dc=\"10\" />", "kind", null);
		this.assertMissingProperty(String.format(
				"<stone kind=\"%s\" dc=\"0\" />", kind.toString()), "id",
			new StoneDeposit(kind, 0, 0));
	}

	/**
	 * A factory to encapsulate rivers in a simple map.
	 */
	private static IMapNG encapsulateRivers(final Point point, final River... rivers) {
		IMutableMapNG retval = new SPMapNG(new MapDimensionsImpl(point.getRow() + 1,
			point.getColumn() + 1, 2), new PlayerCollection(), -1);
		retval.setBaseTerrain(point, TileType.Plains);
		retval.addRivers(point, rivers);
		return retval;
	}

	/**
	 * Create a simple map.
	 */
	private static IMutableMapNG createSimpleMap(final Point dims, final Pair<Point, TileType>... terrain) {
		final IMutableMapNG retval = new SPMapNG(new MapDimensionsImpl(dims.getRow(),
			dims.getColumn(), 2), new PlayerCollection(), -1);
		for (Pair<Point, TileType> pair : terrain) {
			retval.setBaseTerrain(pair.getValue0(), pair.getValue1());
		}
		return retval;
	}

	/**
	 * Test {@link Player} deserialization.
	 *
	 * TODO: Split and randomize
	 */
	@Test
	public void testPlayerSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("First Player serialization test", new PlayerImpl(1, "one"));
		assertSerialization("Second Player serialization test", new PlayerImpl(2, "two"));
		assertSerialization("Player with country", new PlayerImpl(3, "three", "country"));
		this.<Player>assertUnwantedChild(
			"<player code_name=\"one\" number=\"1\"><troll /></player>", null);
		this.<Player>assertMissingProperty("<player code_name=\"one\" />", "number", null);
		this.<Player>assertMissingProperty("<player number=\"1\" />", "code_name", null);
		assertPortraitSerialization("Players can have associated portraits",
			new PlayerImpl(3, "three"));
	}

	/**
	 * Test that {@link River rivers} are properly (de)serialized in the simplest case.
	 */
	@ParameterizedTest
	@EnumSource(River.class)
	public void testSimpleRiverSerialization(final River river)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("River alone", river);
		Point loc = new Point(0, 0);
		assertSerialization("River in tile", encapsulateRivers(loc, river));
	}

	/**
	 * Test {@link River} (de)serialization in more complicated cases,
	 * including ways that have improperly failed in the past.
	 */
	@Test
	public void testRiverSerializationOne()
			throws SPFormatException, MalformedXMLException, IOException {
		this.<IMapNG>assertUnwantedChild(encapsulateTileString("<lake><troll /></lake>"), null);
		this.<IMapNG>assertMissingProperty(encapsulateTileString("<river />"), "direction", null);
		final Set<River> setOne = EnumSet.of(River.North, River.South);
		final Set<River> setTwo = EnumSet.of(River.South, River.North);
		assertEquals(setOne, setTwo, "Rivers added in different order to set");
		assertEquals(
			encapsulateRivers(new Point(1, 1), River.North, River.South),
			encapsulateRivers(new Point(1, 1), River.North, River.South),
			"Tile equality with rivers");
		assertEquals(
			encapsulateRivers(new Point(1, 1), River.East, River.West),
			encapsulateRivers(new Point(1, 1), River.West, River.East),
			"Tile equality with different order of rivers");
		assertSerialization("Two rivers", encapsulateRivers(new Point(1, 2),
			River.North, River.South));
		this.<IMapNG>assertMissingProperty(
			encapsulateTileString("<river direction=\"invalid\" />"), "direction",
			null);
	}

	/**
	 * Test (de)serialization of a single simple tile.
	 *
	 * TODO: Split and randomize
	 */
	@Test
	public void testSimpleTileSerializtion()
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("Simple Tile", createSimpleMap(new Point(1, 1),
			Pair.with(new Point(0, 0), TileType.Desert)));
		final IMutableMapNG firstMap = createSimpleMap(new Point(2, 2),
			Pair.with(new Point(1, 1), TileType.Plains));
		firstMap.addFixture(new Point(1, 1), new Griffin(1));
		assertSerialization("Tile with one fixture", firstMap);
		IMutableMapNG secondMap = createSimpleMap(new Point(3, 3),
			Pair.with(new Point(2, 2), TileType.Steppe));
		secondMap.addFixture(new Point(2, 2),
			new Unit(new PlayerImpl(-1, ""), "unitOne", "firstUnit", 1));
		secondMap.addFixture(new Point(2, 2), new Forest("forestKind", true, 8));
		assertSerialization("Tile with two fixtures", secondMap);
		this.<IMapNG>assertMissingProperty("<map version=\"2\" rows=\"1\" columns=\"1\">" +
			"<tile column=\"0\" kind=\"plains\" /></map>", "row", null);
		this.<IMapNG>assertMissingProperty(
			"<map version=\"2\" rows=\"1\" columns=\"1\"><tile row=\"0\" kind=\"plains\" /></map>",
			"column", null);
		this.<IMapNG>assertMissingProperty(
			"<map version=\"2\" rows=\"1\" columns=\"1\"><tile row=\"0\" column=\"0\" /></map>",
			"kind", new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 0));
		this.<IMapNG>assertUnwantedChild(encapsulateTileString(
			"<tile row=\"2\" column=\"0\" kind=\"plains\" />"), null);
	}

	/**
	 * Further test serialization of a tile's contents.
	 */
	@Test
	public void testTileSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		IMutableMapNG thirdMap = createSimpleMap(new Point(4, 4),
			Pair.with(new Point(3, 3), TileType.Jungle));
		Player playerOne = new PlayerImpl(2, "");
		IMutableFortress fort = new FortressImpl(playerOne, "fortOne", 1,
			TownSize.Small);
		fort.addMember(new Unit(playerOne, "unitTwo", "secondUnit", 2));
		thirdMap.addFixture(new Point(3, 3), fort);
		thirdMap.addFixture(new Point(3, 3), new TextFixture("Random text here", 5));
		thirdMap.addRivers(new Point(3, 3), River.Lake);
		thirdMap.addPlayer(playerOne);
		assertSerialization("More complex tile", thirdMap);
	}

	private static Stream<Arguments> testTileDeprecatedIdiom() {
		return Stream.of(TileType.values()).flatMap(a -> Stream.of(true, false).map(b ->
			Arguments.of(a, b)));
	}

	/**
	 * Test that the deprecated XML idiom for tile types is still supported.
	 */
	@ParameterizedTest
	@MethodSource
	public void testTileDeprecatedIdiom(final TileType terrain, final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		final IMapNG map = createSimpleMap(new Point(5, 5), Pair.with(new Point(4, 4), terrain));
		assertDeprecatedProperty(createSerializedForm(map, deprecatedWriter)
			.replace("kind", "type"), "type", "kind", "tile", map);
	}

	/**
	 * A further test of (de)serialization of a tile.
	 */
	@Test
	public void testTileSerializationTwo()
			throws SPFormatException, MalformedXMLException, IOException {
		IMutableMapNG five = createSimpleMap(new Point(3, 4), Pair.with(new Point(2, 3),
			TileType.Jungle));
		Player player = new PlayerImpl(2, "playerName");
		five.addFixture(new Point(2, 3), new Unit(player, "explorer", "name one", 1));
		five.addFixture(new Point(2, 3), new Unit(player, "explorer", "name two", 2));
		five.addPlayer(player);
		assertEquals(2, five.getFixtures(new Point(2, 3)).size(), "Just checking ...");
		assertSerialization("Multiple units should come through", five);
		String xmlTwoLogical = String.format(
			"<view xmlns=\"%s\" current_player=\"-1\" current_turn=\"-1\">%n" +
				"\t<map version=\"2\" rows=\"3\" columns=\"4\">%n" +
				"\t\t<player number=\"2\" code_name=\"playerName\" />%n" +
				"\t\t<row index=\"2\">%n" +
				"\t\t\t<tile row=\"2\" column=\"3\" kind=\"jungle\">%n" +
				"\t\t\t\t<unit owner=\"2\" kind=\"explorer\" name=\"name one\" id=\"1\" />%n" +
				"\t\t\t\t<unit owner=\"2\" kind=\"explorer\" name=\"name two\" id=\"2\" />%n" +
				"\t\t\t</tile>%n\t\t</row>%n\t</map>%n</view>%n", SP_NAMESPACE);
		assertEquals(createSerializedForm(five, true), xmlTwoLogical, "Multiple units");
		String xmlTwoAlphabetical = String.format(
			"<view current_player=\"-1\" current_turn=\"-1\" xmlns=\"%s\">%n" +
				"\t<map columns=\"4\" rows=\"3\" version=\"2\">%n" +
				"\t\t<player number=\"2\" code_name=\"playerName\" />%n" +
				"\t\t<row index=\"2\">%n" +
				"\t\t\t<tile column=\"3\" kind=\"jungle\" row=\"2\">%n" +
				"\t\t\t\t<unit id=\"1\" kind=\"explorer\" name=\"name one\" owner=\"2\" />%n" +
				"\t\t\t\t<unit id=\"2\" kind=\"explorer\" name=\"name two\" owner=\"2\" />%n" +
				"\t\t\t</tile>%n\t\t</row>%n\t</map>%n</view>%n", SP_NAMESPACE);
		String serializedForm = createSerializedForm(five, false);
		assertAny("Multiple units", () -> assertEquals(xmlTwoLogical, serializedForm),
			() -> assertEquals(xmlTwoAlphabetical, serializedForm),
			() -> assertEquals(xmlTwoLogical.replaceAll("\" />", "\"/>"), serializedForm));
		assertEquals(createSerializedForm(createSimpleMap(new Point(1, 1)), true),
			String.format("<view xmlns=\"%s\" current_player=\"-1\" current_turn=\"-1\">%n" +
				"\t<map version=\"2\" rows=\"1\" columns=\"1\">%n" +
				"\t</map>%n</view>%n", SP_NAMESPACE),
			"Shouldn't print empty not-visible tiles");
		String emptySerializedForm = createSerializedForm(createSimpleMap(new Point(1, 1)), false);
		String firstPossibility = String.format(
			"<view xmlns=\"%s\" current_player=\"-1\" current_turn=\"-1\">%n" +
				"\t<map version=\"2\" rows=\"1\" columns=\"1\">%n" +
				"\t</map>%n</view>%n", SP_NAMESPACE);
		String secondPossibility = String.format(
			"<view current_player=\"-1\" current_turn=\"-1\" xmlns=\"%s\">%n" +
				"\t<map columns=\"1\" rows=\"1\" version=\"2\"/>%n</view>%n",
			SP_NAMESPACE);
		assertAny("Shouldn't print empty non-visible tiles",
			() -> assertEquals(firstPossibility, emptySerializedForm),
			() -> assertEquals(secondPossibility, emptySerializedForm));
	}

	/**
	 * Test that {@link IUnit a unit's} image property is preserved through (de)serialization.
	 */
	@Test
	public void testUnitImageSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		assertImageSerialization("Unit image property is preserved",
			new Unit(new PlayerImpl(5, ""), "herder", "herderName", 9));
	}

	private static Stream<Arguments> testTileSerializationThree() {
		return Stream.of(Arguments.of(true), Arguments.of(false));
	}

	/**
	 * Another test of serialization within a single tile.
	 */
	@ParameterizedTest
	@MethodSource
	public void testTileSerializationThree(final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		final IMutableMapNG six = new SPMapNG(new MapDimensionsImpl(2, 2, 2),
			new PlayerCollection(), 5);
		six.setMountainous(new Point(0, 0), true);
		six.addFixture(new Point(0, 1), new Ground(22, "basalt", false));
		six.addFixture(new Point(1, 0), new Forest("pine", false, 19));
		six.addFixture(new Point(1, 1), new AnimalImpl("beaver", false, "wild", 18));
		assertMissingProperty(createSerializedForm(six, deprecatedWriter), "kind", six);
	}

	/**
	 * Test that tags we intend to possibly support in the future (or
	 * include in the XML for readability, like {@code row}) are
	 * properly skipped when deserializing.
	 */
	@Test
	public void testSkippableSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		assertEquivalentForms("Two maps, one with row tags, one without",
			"<map rows=\"1\" columns=\"1\" version=\"2\" current_player=\"-1\" />",
			"<map rows=\"1\" columns=\"1\" version=\"2\" current_player=\"-1\"><row /></map>",
			Warning.DIE);
		assertEquivalentForms("Two maps, one with future tag, one without",
			"<map rows=\"1\" columns=\"1\" version=\"2\" current_player=\"-1\" />",
			"<map rows=\"1\" columns=\"1\" version=\"2\" current_player=\"-1\"><future /></map>",
			Warning.IGNORE);
		this.<IMapNG>assertUnsupportedTag(
			"<map rows=\"1\" columns=\"1\" version=\"2\" current_player=\"-1\"><future /></map>",
			"future", new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 0));
		final IMutableMapNG expected =
			new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 0);
		expected.setBaseTerrain(new Point(0, 0), TileType.Steppe);
		this.<IMapNG>assertUnsupportedTag(
			"<map rows=\"1\" columns=\"1\" version=\"2\" current_player=\"-1\">" +
				"<tile row=\"0\" column=\"0\" kind=\"steppe\"><futureTag /></tile></map>",
			"futureTag", expected);
	}

	/**
	 * Test that a complex map is properly (de)serialized.
	 */
	@Test
	public void testMapSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		this.<IMapNG>assertUnwantedChild(
			"<map rows=\"1\" columns=\"1\" version=\"2\"><hill /></map>", null);
		final MutablePlayer player = new PlayerImpl(1, "playerOne");
		player.setCurrent(true);
		final IMutableMapNG firstMap = new SPMapNG(new MapDimensionsImpl(1, 1, 2),
			new PlayerCollection(), 0);
		firstMap.addPlayer(player);
		Point loc = new Point(0, 0);
		firstMap.setBaseTerrain(loc, TileType.Plains);
		assertSerialization("Simple Map serialization", firstMap);
		this.<IMapNG>assertMissingProperty("<map version=\"2\" columns=\"1\" />", "rows", null);
		this.<IMapNG>assertMissingProperty("<map version=\"2\" rows=\"1\" />", "columns", null);
		String originalFormOne = createSerializedForm(firstMap, false);
		String originalFormTwo = createSerializedForm(firstMap, true);
		firstMap.setBaseTerrain(new Point(1, 1), null);
		assertEquals(originalFormOne, createSerializedForm(firstMap, false),
			"Explicitly not visible tile is not serialized");
		assertEquals(originalFormTwo, createSerializedForm(firstMap, true),
			"Explicitly not visible tile is not serialized");
		firstMap.setMountainous(loc, true);
		assertSerialization("Map with a mountainous point", firstMap);
		this.<IMapNG>assertMissingProperty(
			"<view current_turn=\"0\"><map version=\"2\" rows=\"1\" columns=\"1\" /></view>",
			"current_player", new SPMapNG(new MapDimensionsImpl(1, 1, 2),
				new PlayerCollection(), 0));
		this.<IMapNG>assertMissingProperty(
			"<view current_player=\"0\"><map version=\"2\" rows=\"1\" columns=\"1\" /></view>",
			"current_turn", null);
		this.<IMapNG>assertMissingChild("<view current_player=\"1\" current_turn=\"0\" />");
		this.<IMapNG>assertMissingChild("<view current_player=\"1\" current_turn=\"13\" />");
		this.<IMapNG>assertUnwantedChild(
			"<view current_player=\"0\" current_turn=\"0\">" +
				"<map version=\"2\" rows=\"1\" columns=\"1\" />" +
				"<map version=\"2\" rows=\"1\" columns=\"1\" /></view>", null);
		this.<IMapNG>assertUnwantedChild(
			"<view current_player=\"0\" current_turn=\"0\"><hill /></view>", null);
		assertMapDeserialization("Proper deserialization of map without view tag", firstMap,
			"<map version=\"2\" rows=\"1\" columns=\"1\" current_player=\"1\">" +
				"<player number=\"1\" code_name=\"playerOne\" />" +
				"<row index=\"0\"><tile row=\"0\" column=\"0\" kind=\"plains\">" +
				"<mountain /></tile></row></map>");
	}

	/**
	 * Test that deserialization handles XML namespaces properly.
	 */
	@Test
	public void testNamespacedSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		final MutablePlayer player = new PlayerImpl(1, "playerOne");
		player.setCurrent(true);
		final IMutableMapNG firstMap =
			new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 0);
		firstMap.addPlayer(player);
		Point loc = new Point(0, 0);
		firstMap.setBaseTerrain(loc, TileType.Steppe);
		assertMapDeserialization("Proper deserialization of namespaced map", firstMap,
			String.format("<map xmlns=\"%s\" version=\"2\" rows=\"1\" columns=\"1\"%n" +
				"current_player=\"1\"><player number=\"1\" code_name=\"playerOne\" />" +
				"<row index=\"0\"><tile row=\"0\" column=\"0\" kind=\"steppe\" />" +
				"</row></map>", SP_NAMESPACE));
		assertMapDeserialization(
			"Proper deserialization of map if another namespace is declared default",
			firstMap, String.format(
				"<sp:map xmlns=\"xyzzy\" xmlns:sp=\"%s\" version=\"2\" rows=\"1\" " +
					"columns=\"1\" current_player=\"1\"><sp:player number=\"1\" " +
					"code_name=\"playerOne\" /><sp:row index=\"0\">" +
					"<sp:tile row=\"0\" column=\"0\" kind=\"steppe\" />" +
					"</sp:row></sp:map>", SP_NAMESPACE));
		assertMapDeserialization("Non-root other-namespace tags ignored", firstMap, String.format(
			"<map xmlns=\"%s\" version=\"2\" rows=\"1\" columns=\"1\" current_player=\"1\" " +
				"xmlns:xy=\"xyzzy\"><player number=\"1\" code_name=\"playerOne\" />" +
				"<xy:xyzzy><row index=\"0\"><tile row=\"0\" column=\"0\" kind=\"steppe\">" +
				"<xy:hill id=\"0\" /></tile></row></xy:xyzzy></map>", SP_NAMESPACE));
		for (ISPReader reader : spReaders) {
			this.<IMapNG, Exception>assertFormatIssue(reader,
				"<map xmlns=\"xyzzy\" version=\"2\" rows=\"1\" columns=\"1\" " +
					"current_player=\"1\">" +
					"<player number=\"1\" code_name=\"playerOne\" /><row index=\"0\">" +
					"<tile row=\"0\" column=\"0\" kind=\"steppe\" /></row></map>", null,
					Exception.class, (except) -> {
						if (except instanceof UnwantedChildException) {
							assertEquals("root", ((UnwantedChildException) except)
								.getTag().getLocalPart(),
								"'Tag' with the unexpected child was what we expected");
							assertEquals(new QName("xyzzy", "map"),
								((UnwantedChildException) except).getChild(),
								"Unwanted child was the one we expected");
						} else if (except instanceof MalformedXMLException) {
							assertEquals(
								"XML stream didn't contain a start element",
								except.getMessage());
						} else {
							fail("Unexpected exception type");
						}
					});
			this.<AdventureFixture, Exception>assertFormatIssue(reader,
				"<adventure xmlns=\"xyzzy\" id=\"1\" brief=\"one\" full=\"two\" />", null,
				Exception.class,
				(except) -> assertTrue(except instanceof UnwantedChildException ||
						except instanceof MalformedXMLException,
					"Exception is of expected type"));
		}
	}

	/**
	 * Test that duplicate IDs are warned about.
	 */
	@Test
	public void testDuplicateID()
			throws SPFormatException, MalformedXMLException, IOException {
		final IMutableMapNG expected =
			new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 0);
		Point point = new Point(0, 0);
		expected.setBaseTerrain(point, TileType.Steppe);
		MutablePlayer player = new PlayerImpl(1, "playerOne");
		player.setCurrent(true);
		expected.addPlayer(player);
		expected.addFixture(point, new Hill(1));
		expected.addFixture(point, new Ogre(1));
		assertDuplicateID("<map version=\"2\" rows=\"1\" columns=\"1\" current_player=\"1\">" +
			"<player number=\"1\" code_name=\"playerOne\" />" +
			"<row index=\"0\">" +
			"<tile row=\"0\" column=\"0\" kind=\"steppe\">" +
			"<hill id=\"1\" /><ogre id=\"1\" /></tile></row></map>", expected);
	}

	/**
	 * Test that the XML-reading code properly rejects several invalid constructs.
	 */
	@Test
	public void testRejectsInvalid()
			throws SPFormatException, MalformedXMLException, IOException {
		assertInvalid("<map version=\"2\" rows=\"1\" columns=\"1\" current_player=\"1\">");
		assertInvalid(
			"<map version=\"2\" rows=\"1\" columns=\"1\" current_player=\"1\"><></map>");
	}

	private static Stream<Arguments> testGroveSerialization() {
		return treeTypes.stream().flatMap(a -> SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
			Stream.of(true, false).flatMap(c -> Stream.of(true, false).map(d ->
				Arguments.of(c, d, a, b)))));
	}

	/**
	 * Test proper {@link Grove} (de)serialization.
	 *
	 * TODO: Split and further randomize
	 */
	@ParameterizedTest
	@MethodSource
	public void testGroveSerialization(final boolean fruit, final boolean cultivated, final String trees, final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("Test of Grove serialization", new Grove(fruit, cultivated, trees, id));
		this.<Grove>assertUnwantedChild("<grove wild=\"true\" kind=\"kind\"><troll /></grove>", null);
		this.<Grove>assertMissingProperty("<grove />", "cultivated", null);
		this.<Grove>assertMissingProperty("<grove wild=\"false\" />", "kind", null);
		this.assertDeprecatedProperty("<grove cultivated=\"true\" tree=\"tree\" id=\"0\" />",
			"tree", "kind", "grove", new Grove(false, true, "tree", 0));
		this.assertMissingProperty("<grove cultivated=\"true\" kind=\"kind\" />", "id",
			new Grove(false, true, "kind", 0));
		this.assertDeprecatedProperty("<grove wild=\"true\" kind=\"tree\" id=\"0\" />",
			"wild", "cultivated", "grove", new Grove(false, false, "tree", 0));
		assertEquivalentForms("Assert that wild is the inverse of cultivated",
			"<grove wild=\"true\" kind=\"tree\" id=\"0\" />",
			"<grove cultivated=\"false\" kind=\"tree\" id=\"0\" />", Warning.IGNORE);
		assertImageSerialization("Grove image property is preserved",
			new Grove(false, false, trees, id));
		assertSerialization("Groves can have 'count' property", new Grove(true, true, trees, id, 4));
	}

	private static Stream<Arguments> testMeadowSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			Stream.of(FieldStatus.values()).flatMap(b ->
				fieldTypes.stream().collect(toShuffledStream()).limit(2).flatMap(c ->
					Stream.of(true, false).flatMap(d ->
						Stream.of(true, false).map(e ->
							Arguments.of(a, b, c, d, e))))));
	}

	/**
	 * Test proper {@link Meadow} (de)serialization."
	 *
	 * TODO: Split; further randomize; use parameters in 'invalid XML' assertions
	 */
	@ParameterizedTest
	@MethodSource
	public void testMeadowSerialization(final int id, final FieldStatus status, final String kind, final boolean field,
	                                    final boolean cultivated)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("Test of Meadow serialization",
			new Meadow(kind, field, cultivated, id, status));
		this.<Meadow>assertUnwantedChild(
			"<meadow kind=\"flax\" cultivated=\"false\"><troll /></meadow>", null);
		this.<Meadow>assertMissingProperty("<meadow cultivated=\"false\" />", "kind", null);
		this.<Meadow>assertMissingProperty("<meadow kind=\"flax\" />", "cultivated", null);
		this.assertMissingProperty("<field kind=\"kind\" cultivated=\"true\" />", "id",
			new Meadow("kind", true, true, 0, FieldStatus.random(0)));
		this.assertMissingProperty("<field kind=\"kind\" cultivated=\"true\" id=\"0\" />",
			"status", new Meadow("kind", true, true, 0, FieldStatus.random(0)));
		assertImageSerialization("Meadow image property is preserved",
			new Meadow(kind, field, cultivated, id, status));
		assertSerialization("Meadows can have acreage numbers",
			new Meadow(kind, field, cultivated, id, status,
				new BigDecimal(5).divide(new BigDecimal(4))));
		assertSerialization("Meadows can have acreage numbers",
			new Meadow(kind, field, cultivated, id, status,
				new BigDecimal(3).divide(new BigDecimal(4))));
	}

	private static Stream<Arguments> testMineSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			minerals.stream().collect(toShuffledStream()).limit(2).flatMap(b ->
				Stream.of(TownStatus.values()).flatMap(c ->
					Stream.of(true, false).map(d ->
						Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test proper {@link Mine} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testMineSerialization(final int id, final String kind, final TownStatus status,
	                                  final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		Mine mine = new Mine(kind, status, id);
		assertSerialization("Test of Mine serialization", mine);
		assertDeprecatedProperty(
			createSerializedForm(mine, deprecatedWriter).replaceAll("kind=", "product="),
			"product", "kind", "mine", mine);
		this.<Mine>assertUnwantedChild(String.format(
			"<mine kind=\"%s\" status=\"%s\"><troll /></mine>", kind, status.toString()), null);
		this.<Mine>assertMissingProperty(String.format("<mine status=\"%s\" />", status.toString()),
			"kind", null);
		this.<Mine>assertMissingProperty(String.format("<mine kind=\"%s\" />", kind), "status", null);
		this.assertMissingProperty(
			String.format("<mine kind=\"%s\" status=\"%s\" />", kind, status.toString()), "id",
			new Mine(kind, status, 0));
		assertImageSerialization("Mine image property is preserved", mine);
	}

	private static Stream<Arguments> testShrubSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			fieldTypes.stream().collect(toShuffledStream()).limit(2).flatMap(b ->
				Stream.of(true, false).map(c -> Arguments.of(a, b, c))));
	}

	/**
	 * Test proper {@link Shrub} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testShrubSerialization(final int id, final String kind, final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		Shrub shrub = new Shrub(kind, id);
		assertSerialization("First test of Shrub serialization", shrub);
		assertDeprecatedProperty(
			createSerializedForm(shrub, deprecatedWriter).replace("kind", "shrub"),
			"shrub", "kind", "shrub", shrub);
		this.<Shrub>assertUnwantedChild(String.format(
			"<shrub kind=\"%s\"><troll /></shrub>", kind), null);
		this.<Shrub>assertMissingProperty("<shrub />", "kind", null);
		this.assertMissingProperty(String.format(
			"<shrub kind=\"%s\" />", kind), "id", new Shrub(kind, 0));
		assertImageSerialization("Shrub image property is preserved", shrub);
		assertSerialization("Shrub can have 'count' property", new Shrub(kind, id, 3));
	}

	private static Stream<Arguments> testTextSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			fieldTypes.stream().collect(toShuffledStream()).limit(2).map(b ->
				Arguments.of(a, b)));
	}

	/**
	 * Test proper {@link TextFixture} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testTextSerialization(final int baseTurn, final String text)
			throws SPFormatException, MalformedXMLException, IOException {
		int turn = baseTurn - 2; // Make sure negative turns occasionally get checked.
		TextFixture testee = new TextFixture(text, turn);
		assertSerialization("Test of TextFixture serialization", testee);
		this.<TextFixture>assertUnwantedChild(String.format(
			"<text turn=\"%d\"><troll /></text>", turn), null);
		assertImageSerialization("Text image property is preserved", testee);
		IMutableMapNG wrapper = createSimpleMap(new Point(1, 1),
			Pair.with(new Point(0, 0), TileType.Plains));
		wrapper.addFixture(new Point(0, 0), new TextFixture(text, -1));
		wrapper.setCurrentTurn(0);
		assertForwardDeserialization("Deprecated text-in-map still works", String.format(
				"<map version=\"2\" rows=\"1\" columns=\"1\" current_player=\"-1\">%n" +
					"<tile row=\"0\" column=\"0\" kind=\"plains\">%s</tile></map>", text),
			wrapper::equals);
	}

	/**
	 * Test that {@link IUnit unit} deserialization requires certain properties to be present.
	 */
	@Test
	public void testUnitHasRequiredProperties()
			throws SPFormatException, MalformedXMLException, IOException {
		this.<IUnit>assertMissingProperty("<unit name=\"name\" />", "owner",
			new Unit(new PlayerImpl(-1, ""), "", "name", 0));
		this.<IUnit>assertMissingProperty("<unit owner=\"1\" name=\"name\" id=\"0\" />", "kind",
			new Unit(new PlayerImpl(1, ""), "", "name", 0));
		this.<IUnit>assertMissingProperty("<unit owner=\"1\" kind=\"\" name=\"name\" id=\"0\" />",
			"kind", new Unit(new PlayerImpl(1, ""), "", "name", 0));
	}

	private static Stream<Arguments> testUnitWarnings() {
		return Stream.of(true, false).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
				treeTypes.stream().collect(toShuffledStream()).limit(2).flatMap(c ->
					fieldTypes.stream().collect(toShuffledStream()).limit(2).map(d ->
						Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test that {@link IUnit unit} deserialization warns about various
	 * deprecated idioms and objects to certain other disallowed idioms.
	 */
	@ParameterizedTest
	@MethodSource
	public void testUnitWarnings(final boolean deprecatedWriter, final int id, final String name, final String kind)
			throws SPFormatException, MalformedXMLException, IOException {
		// TODO: should probably test spaces in name and kind
		this.<IUnit>assertUnwantedChild("<unit><unit /></unit>", null);
		IUnit firstUnit = new Unit(new PlayerImpl(1, ""), kind, name, id);
		assertDeprecatedProperty(
			createSerializedForm(firstUnit, deprecatedWriter).replace("kind", "type"),
			"type", "kind", "unit", firstUnit);
		this.<IUnit>assertMissingProperty("<unit owner=\"2\" kind=\"unit\" />", "name",
			new Unit(new PlayerImpl(2, ""), "unit", "", 0)); // TODO: use provided kind
		assertSerialization("Deserialize unit with no kind properly",
			new Unit(new PlayerImpl(2, ""), "", name, 2), Warning.IGNORE);
		assertMissingProperty("<unit kind=\"kind\" name=\"unitThree\" id=\"3\" />", "owner",
			new Unit(new PlayerImpl(-1, ""), "kind", "unitThree", 3));
		IUnit fourthUnit = new Unit(new PlayerImpl(4, ""), kind, "", id);
		assertMissingProperty(createSerializedForm(fourthUnit, deprecatedWriter), "name",
			fourthUnit);
		assertMissingProperty(String.format(
				"<unit owner=\"4\" kind=\"%s\" name=\"\" id=\"%d\" />", kind, id), "name",
			fourthUnit);
		this.<IUnit>assertMissingProperty(String.format(
			"<unit owner=\"1\" kind=\"%s\" name=\"%s\" />", kind, name), "id",
			new Unit(new PlayerImpl(1, ""), kind, name, 0));
	}

	/**
	 * Test (de)serialization of {@link UnitMember members} of {@link IUnit units}.
	 */
	@Test
	public void testUnitMemberSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		IMutableUnit firstUnit = new Unit(new PlayerImpl(1, ""), "unitType", "unitName", 1);
		firstUnit.addMember(new AnimalImpl("animal", true, "wild", 2));
		assertSerialization("Unit can have an animal as a member", firstUnit);
		firstUnit.addMember(new Worker("worker", "human", 3));
		assertSerialization("Unit can have a worker as a member", firstUnit);
		firstUnit.addMember(
			new Worker("second", "elf", 4, new Job("job", 0, new Skill("skill", 1, 2))));
		assertSerialization("Worker can have jobs", firstUnit);
		assertForwardDeserialization("Explicit specification of default race works",
			"<worker name=\"third\" race=\"human\" id=\"5\" />",
			new Worker("third", "human", 5)::equals);
		assertForwardDeserialization("Implicit default race also works",
			"<worker name=\"fourth\" id=\"6\" />",
			new Worker("fourth", "human", 6)::equals);
		Worker secondWorker = new Worker("sixth", "dwarf", 9);
		secondWorker.setStats(new WorkerStats(0, 0, 1, 2, 3, 4, 5, 6));
		assertSerialization("Worker can have stats", secondWorker);
		assertImageSerialization("Worker image property is preserved", secondWorker);
		secondWorker.addJob(new Job("seventh", 1));
		assertSerialization("Worker can have Job with no skills yet", secondWorker);
		this.<IMapNG>assertUnwantedChild("<map version=\"2\" rows=\"1\" columns=\"1\">" +
			"<tile row=\"0\" column=\"0\" kind=\"plains\">" +
			"<worker name=\"name\" id=\"1\" /></tile></map>", null);
		assertPortraitSerialization("Worker portrait property is preserved", secondWorker);
		secondWorker.setNote(new PlayerImpl(1, ""), "sample notes");
		assertNotesSerialization("Worker notes property is preserved", secondWorker);
	}

	/**
	 * Test (de)serialization of {@link IUnit unit} orders.
	 */
	@Test
	public void testOrdersSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		Player player = new PlayerImpl(0, "");
		IMutableUnit firstUnit = new Unit(player, "kind of unit", "name of unit", 2);
		IMutableUnit secondUnit = new Unit(player, "kind of unit", "name of unit", 2);
		secondUnit.setOrders(-1, "some orders");
		assertEquals(firstUnit, secondUnit, "Orders have no effect on equals");
		assertSerialization("Orders don't mess up deserialization", secondUnit);
		assertSerializedFormContains(secondUnit, "some orders", "Serialized form contains orders");
		secondUnit.setOrders(2, "some other orders");
		assertSerializedFormContains(secondUnit, "some orders",
			"Serialized form contains original orders after adding new orders");
		assertSerializedFormContains(secondUnit, "some other orders",
			"Serialized form contains new orders too");
		secondUnit.setResults(3, "some results");
		assertSerializedFormContains(secondUnit, "some results", "Serialized form contains results");
		secondUnit.setResults(-1, "some other results");
		assertSerializedFormContains(secondUnit, "some results",
			"Serialized form contains original results after adding new results");
		assertSerializedFormContains(secondUnit, "some other results",
			"Serialized form contains new results too");
		this.<IUnit>assertForwardDeserialization("Orders can be read without tags",
			"<unit name=\"name\" kind=\"kind\" id=\"1\" owner=\"-1\">Orders orders</unit>",
			(unit) -> unit.getOrders(-1).equals("Orders orders"));
	}

	private static Stream<Arguments> testQuoting() {
		return SINGLETON_RANDOM.ints().boxed().limit(3).map(n -> Arguments.of(n));
	}

	/**
	 * Test that XML metacharacters are properly quoted (i.e. don't break
	 * the reader but are properly deserialized) when they appear in text
	 * that must be serialized.
	 */
	@ParameterizedTest
	@MethodSource
	public void testQuoting(final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		Player player = new PlayerImpl(0, "");
		Unit unit = new Unit(player, "kind of unit", "name of unit", id);
		unit.setOrders(4, "I <3 & :( \"meta'");
		unit.setResults(5, "2 --> 1");
		// FIXME: assertSerialization() relies on equals(), which doesn't check orders IIRC
		assertSerialization(
			"Serialization preserves XML meta-characters in orders and results", unit);
		unit.setOrders(3, "1 << 2");
		unit.setResults(-1, "\"quote this\"");
		assertSerialization("This works even if such characters occur more than once", unit);
		unit.setName("\"Can't quote this ><>&\"");
		assertSerialization("Data stored in XML attributes is quoted", unit);
	}

	/**
	 * Test that {@link IUnit units'} {@link HasPortrait#portrait
	 * portraits} are preserved in (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testQuoting")
	public void testUnitPortraitSerialization(final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		Unit unit = new Unit(new PlayerImpl(1, ""), "kind", "name", id);
		unit.setPortrait("portraitFile");
		assertSerialization("Portrait doesn't mess up serialization", unit);
		assertSerializedFormContains(unit, "portraitFile", "Serialized form contains portrait");
		assertPortraitSerialization("Unit portrait property is preserved", unit);
	}

	private static Stream<Arguments> testAdventureSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).map(b -> Arguments.of(a, b)));
	}

	/**
	 * Test that {@link AdventureFixture adventure hooks} are properly (de)serialized.
	 */
	@ParameterizedTest
	@MethodSource
	public void testAdventureSerialization(final int idOne, final int idTwo)
			throws SPFormatException, MalformedXMLException, IOException {
		Player independent = new PlayerImpl(1, "independent");
		AdventureFixture first = new AdventureFixture(independent, "first hook brief",
			"first hook full", idOne);
		AdventureFixture second = new AdventureFixture(new PlayerImpl(2, "player"),
			"second hook brief", "second hook full", idTwo);
		assertNotEquals(first, second, "Two different hooks are not equal");
		IMutableMapNG wrapper = createSimpleMap(new Point(1, 1),
			Pair.with(new Point(0, 0), TileType.Plains));
		wrapper.addPlayer(independent);
		wrapper.addFixture(new Point(0, 0), first);
		assertSerialization("First AdventureFixture serialization test", wrapper);
		assertSerialization("Second AdventureFixture serialization test", second);
		assertSerialization("AdventureFixture with empty descriptions",
			new AdventureFixture(new PlayerImpl(3, "third"), "", "", idOne));
		// TODO: split portals into separate test method
		Portal third = new Portal("portal dest", new Point(1, 2), idOne);
		Portal fourth = new Portal("portal dest two", new Point(2, 1), idTwo);
		assertNotEquals(third, fourth, "Two different portals are not equal");
		wrapper.addFixture(new Point(0, 0), fourth);
		assertSerialization("First Portal serialization test", wrapper);
		assertSerialization("Second Portal serialization test", fourth);
	}

	/**
	 * Test that {@link IFortress fortress} contents other than units are properly (de)serialized.
	 *
	 * TODO: Split resource details testing into a separate test
	 */
	@Test
	public void testFortressMemberSerialization()
			throws SPFormatException, MalformedXMLException, IOException {
		IMutableFortress firstFort = new FortressImpl(new PlayerImpl(1, ""), "fortName", 1,
			TownSize.Small);
		firstFort.addMember(new Implement("implKind", 2));
		assertSerialization("Fortress can have an Implement as a member", firstFort);
		firstFort.addMember(new Implement("implKindTwo", 8));
		assertSerialization("Implement can be more than one in one object", firstFort);
		firstFort.addMember(new ResourcePileImpl(3, "generalKind", "specificKind",
			new Quantity(10, "each")));
		assertSerialization("Fortress can have a ResourcePile as a member", firstFort);
		IMutableResourcePile resource = new ResourcePileImpl(4, "generalKind", "specificKind",
			new Quantity(15, "pounds"));
		resource.setCreated(5); // TODO: Provide constructor taking this field
		assertSerialization("Resource pile can know what turn it was created", resource);
		assertSerialization("Resource pile can have non-integer quantity", new ResourcePileImpl(5,
			"resourceKind", "specificKind2",
			new Quantity(new BigDecimal(3).divide(new BigDecimal(2)), "cubic feet")));
	}

	private static Stream<Arguments> testAnimalTracksSerialization() {
		return treeTypes.stream().collect(toShuffledStream()).limit(2).map(t -> Arguments.of(t));
	}

	/**
	 * Test that {@link AnimalTracks animal tracks} are properly
	 * (de)serialized, including that the old now-deprecated XML idiom is
	 * still read properly.
	 */
	@ParameterizedTest
	@MethodSource
	public void testAnimalTracksSerialization(final String kind)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("Test of animal-track serialization", new AnimalTracks(kind));
		this.<AnimalTracks>assertUnwantedChild(
			"<animal kind=\"tracks\" traces=\"true\"><troll /></animal>", null);
		this.<AnimalTracks>assertMissingProperty("<animal traces=\"true\" />", "kind", null);
		assertImageSerialization("Animal-track image property is preserved",
			new AnimalTracks(kind));
		assertEquivalentForms("Former idiom still works",
			"<animal kind=\"kind\" status=\"wild\" traces=\"\" />",
			"<animal kind=\"kind\" status=\"wild\" traces=\"true\" />",
			Warning.DIE);
	}

	private static Stream<Arguments> testAnimalSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			animalStatuses.stream().flatMap(b ->
				treeTypes.stream().collect(toShuffledStream()).limit(2).flatMap(c ->
					Stream.of(true, false).map(d -> Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test {@link Animal} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testAnimalSerialization(final int id, final String status, final String kind, final boolean talking)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("Test of Animal serialization",
			new AnimalImpl(kind, talking, status, id));
		this.<Animal>assertUnwantedChild(String.format(
			"<animal kind=\"%s\"><troll /></animal>", kind), null);
		this.<Animal>assertMissingProperty("<animal />", "kind", null);
		this.<Animal>assertForwardDeserialization("Forward-looking in re talking",
			String.format("<animal kind=\"%s\" talking=\"false\" id=\"%d\" />", kind, id),
			new AnimalImpl(kind, false, "wild", id)::equals);
		this.<Animal>assertMissingProperty(String.format("<animal kind=\"%s\" talking=\"%b\" />",
			kind, talking), "id", new AnimalImpl(kind, talking, "wild", 0));
		this.<Animal>assertMissingProperty(String.format(
			"<animal kind=\"%s\" id=\"nonNumeric\" />", kind), "id", null);
		this.<Animal>assertForwardDeserialization("Explicit default status of animal",
			String.format("<animal kind=\"%s\" status=\"wild\" id=\"%d\" />", kind, id),
			new AnimalImpl(kind, false, "wild", id)::equals);
		assertImageSerialization("Animal image property is preserved",
			new AnimalImpl(kind, talking, status, id));
		this.<Animal>assertForwardDeserialization("Namespaced attribute", String.format(
			"<animal xmlns:sp=\"%s\" sp:kind=\"%s\" sp:talking=\"%b\" sp:traces=\"false\"" +
				" sp:status=\"%s\" sp:id=\"%d\" />", SP_NAMESPACE, kind, talking,
						status, id),
			new AnimalImpl(kind, talking, status, id)::equals);
		assertEquivalentForms("Supports 'traces=\"false\"'",
			String.format("<animal kind=\"%s\" status=\"%s\" id=\"%d\" />", kind,
					status, id),
			String.format("<animal kind=\"%s\" traces=\"false\" status=\"%s\" id=\"%d\" />",
				kind, status, id), Warning.DIE);
		assertSerialization("Animal age is preserved",
			new AnimalImpl("youngKind", talking, status, id, 8));
		assertSerialization("Animal population count is preserved",
			new AnimalImpl(kind, talking, status, id, -1, 55));
		assertNotEquals(new AnimalImpl(kind, talking, status, id, -1),
			new AnimalImpl(kind, talking, status, id, 8),
			"But animal age is checked in equals()");
		assertNotEquals(new AnimalImpl(kind, talking, status, id, -1, 1),
			new AnimalImpl(kind, talking, status, id, -1, 2),
			"Animal population count is checked in equals()");
	}

	private static Stream<Arguments> testImmortalAnimalDeserialization() {
		return IMMORTAL_ANIMALS.stream().flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).map(b -> Arguments.of(a, b)));
	}

	/**
	 * Test that the former and current idioms for "immortal animals" produce equivalent results.
	 */
	@ParameterizedTest
	@MethodSource
	public void testImmortalAnimalDeserialization(final String animal, final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		assertEquivalentForms(animal + " as animal deserializes to immortal",
			String.format("<%s id=\"%d\" />", animal, id),
			String.format("<animal kind=\"%s\" id=\"%d\" />", animal, id), Warning.DIE);
	}

	private static Stream<Arguments> testCacheSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(3).flatMap(a ->
			treeTypes.stream().collect(toShuffledStream()).limit(2).flatMap(b ->
				fieldTypes.stream().collect(toShuffledStream()).limit(2).map(c ->
					Arguments.of(a, b, c))));
	}

	/**
	 * Test {@link CacheFixture} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCacheSerialization(final int id, final String kind, final String contents)
			throws SPFormatException, MalformedXMLException, IOException {
		CacheFixture testee = new CacheFixture(kind, contents, id);
		assertSerialization("Test of Cache serialization", testee);
		this.<CacheFixture>assertUnwantedChild(String.format(
			"<cache kind=\"%s\" contents=\"%s\"><troll /></cache>", kind, contents), null);
		this.<CacheFixture>assertMissingProperty(
			String.format("<cache contents=\"%s\" />", contents), "kind", null);
		this.<CacheFixture>assertMissingProperty(
			String.format("<cache kind=\"%s\" />", kind), "contents", null);
		this.assertMissingProperty(
			String.format("<cache kind=\"%s\" contents=\"%s\" />", kind, contents),
			"id", new CacheFixture(kind, contents, 0));
		assertImageSerialization("Cache image property is preserved", testee);
	}

	private static Stream<Arguments> testCentaurSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(3).flatMap(a ->
			treeTypes.stream().collect(toShuffledStream()).limit(2).map(b ->
				Arguments.of(a, b)));
	}

	/**
	 * Test {@link Centaur} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCentaurSerialization(final int id, final String kind)
			throws SPFormatException, MalformedXMLException, IOException {
		Centaur testee = new Centaur(kind, id);
		assertSerialization("Test of Centaur serialization", testee);
		this.<Centaur>assertUnwantedChild(String.format(
			"<centaur kind=\"%s\"><troll /></centaur>", kind), null);
		this.<Centaur>assertMissingProperty("<centaur />", "kind", null);
		this.assertMissingProperty(String.format("<centaur kind=\"%s\" />", kind), "id",
			new Centaur(kind, 0));
		assertImageSerialization("Centaur image property is preserved", testee);
	}

	/**
	 * Test {@link Dragon} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCentaurSerialization")
	public void testDragonSerialization(final int id, final String kind)
			throws SPFormatException, MalformedXMLException, IOException {
		Dragon testee = new Dragon(kind, id);
		assertSerialization("Test of Dragon serialization", testee);
		assertSerialization("Dragon with no kind (de-)serialization", new Dragon("", id));
		this.<Dragon>assertUnwantedChild("<dragon kind=\"ice\"><hill /></dragon>", null);
		this.<Dragon>assertMissingProperty("<dragon />", "kind", null);
		this.assertMissingProperty(String.format("<dragon kind=\"%s\" />", kind), "id",
			new Dragon(kind, 0));
		assertImageSerialization("Dragon image property is preserved", testee);
	}

	/**
	 * Test {@link Fairy} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCentaurSerialization")
	public void testFairySerialization(final int id, final String kind)
			throws SPFormatException, MalformedXMLException, IOException {
		Fairy testee = new Fairy(kind, id);
		assertSerialization("Test of Fairy serialization", testee);
		this.<Fairy>assertUnwantedChild(String.format("<fairy kind=\"%s\"><hill /></fairy>", kind),
			null);
		this.<Fairy>assertMissingProperty("<fairy />", "kind", null);
		this.assertMissingProperty(String.format("<fairy kind=\"%s\" />", kind), "id",
			new Fairy(kind, 0));
		assertImageSerialization("Fairy image property is preserved", testee);
	}

	private static Stream<Arguments> testForestSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(3).flatMap(a ->
			treeTypes.stream().collect(toShuffledStream()).limit(2).flatMap(b ->
				Stream.of(true, false).map(c -> Arguments.of(a, b, c))));
	}

	/**
	 * Test {@link Forest} (de)serialization.
	 *
	 * TODO: Split non-randomizable parts out
	 */
	@ParameterizedTest
	@MethodSource
	public void testForestSerialization(final int id, final String kind, final boolean rows)
			throws SPFormatException, MalformedXMLException, IOException {
		Forest testee = new Forest(kind, rows, id);
		assertSerialization("Test of Forest serialization", testee);
		this.<Forest>assertUnwantedChild(String.format(
			"<forest kind=\"%s\"><hill /></forest>", kind), null);
		this.<Forest>assertMissingProperty("<forest />", "kind", null);
		assertImageSerialization("Forest image property is preserved", testee);
		Point loc = new Point(0, 0);
		IMutableMapNG map = createSimpleMap(new Point(1, 1), Pair.with(loc, TileType.Plains));
		map.addFixture(loc, new Forest("trees", false, 4));
		map.addFixture(loc, new Forest("secondForest", true, 5));
		assertSerialization("Map with multiple Forests on a tile", map);
		assertEquivalentForms("Duplicate Forests ignored",
			encapsulateTileString("<forest kind=\"trees\" id=\"4\" />" +
				"<forest kind=\"second\" rows=\"true\" id=\"5\" />"),
			encapsulateTileString("<forest kind=\"trees\" id=\"4\" />" +
				"<forest kind=\"trees\" id=\"4\" />" +
				"<forest kind=\"second\" rows=\"true\" id=\"5\" />"),
			Warning.IGNORE);
		assertEquivalentForms("Deserialization now supports 'rows=false'",
			encapsulateTileString(String.format("<forest kind=\"trees\" id=\"%d\" />", id)),
			encapsulateTileString(String.format(
				"<forest kind=\"trees\" rows=\"false\" id=\"%d\" />", id)),
			Warning.IGNORE);
		assertSerialization("Forests can have acreage numbers", new Forest(kind,
			rows, id, new BigDecimal(3).divide(new BigDecimal(2))));
	}

	private static Stream<Arguments> testFortressSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			Stream.of(TownSize.values()).map(b -> Arguments.of(a, b)));
	}

	/**
	 * Test {@link IFortress fortress} (de)serialization in the simplest cases.
	 */
	@ParameterizedTest
	@MethodSource
	public void testFortressSerialization(final int id, final TownSize size)
			throws SPFormatException, MalformedXMLException, IOException {
		// Can't give player names because our test environment doesn't
		// let us pass a set of players in
		Player firstPlayer = new PlayerImpl(1, "");
		assertSerialization(String.format("First test of %s Fortress serialization", size.toString()),
			new FortressImpl(firstPlayer, "one", id, size));
		assertSerialization(String.format("Second test of %s Fortress serialization",
			size.toString()), new FortressImpl(firstPlayer, "two", id, size));
		Player secondPlayer = new PlayerImpl(2, "");
		IMutableFortress five = new FortressImpl(secondPlayer, "five", id, TownSize.Small);
		five.addMember(new Unit(secondPlayer, "unitOne", "unitTwo", 1));
		assertSerialization("Fifth test of Fortress serialization", five);
		this.<IFortress>assertUnwantedChild("<fortress><hill /></fortress>", null);
		this.<IFortress>assertMissingProperty("<fortress />", "owner",
			new FortressImpl(new PlayerImpl(-1, ""), "", 0, TownSize.Small));
		this.<IFortress>assertMissingProperty("<fortress owner=\"1\" />", "name",
			new FortressImpl(new PlayerImpl(1, ""), "", 0, TownSize.Small));
		this.<IFortress>assertMissingProperty("<fortress owner=\"1\" name=\"name\" />",
			"id", new FortressImpl(new PlayerImpl(1, ""), "name", 0, TownSize.Small));
		assertImageSerialization("Fortress image property is preserved", five);
	}

	/**
	 * Test {@link Giant} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCentaurSerialization")
	public void testGiantSerialization(final int id, final String kind)
			throws SPFormatException, MalformedXMLException, IOException {
		Giant testee = new Giant(kind, id);
		assertSerialization("Test of Giant serialization", testee);
		this.<Giant>assertUnwantedChild(String.format(
			"<giant kind=\"%s\"><hill /></giant>", kind), null);
		this.<Giant>assertMissingProperty("<giant />", "kind", null);
		this.assertMissingProperty(String.format(
			"<giant kind=\"%s\" />", kind), "id", new Giant(kind, 0));
		assertImageSerialization("Giant image property is preserved", testee);
	}

	private static Stream<Arguments> testGroundSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(3).map(a -> Arguments.of(a));
	}

	/**
	 * Test {@link Ground} (de)serialization
	 *
	 * TODO: Randomize, condense, perhaps split
	 */
	@ParameterizedTest
	@MethodSource
	public void testGroundSerialization(final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("First test of Ground serialization", new Ground(id, "one", true));
		Point loc = new Point(0, 0);
		IMutableMapNG map = createSimpleMap(new Point(1, 1), Pair.with(loc, TileType.Plains));
		map.addFixture(loc, new Ground(-1, "four", true));
		assertSerialization("Test that reader handles ground as a fixture", map);
		assertForwardDeserialization("Duplicate Ground ignored",
			"<view current_turn=\"-1\" current_player=\"-1\">" +
				"<map version=\"2\" rows=\"1\" columns=\"1\">" +
				"<tile row=\"0\" column=\"0\" kind=\"plains\">" +
				"<ground kind=\"four\" exposed=\"true\" />" +
				"<ground kind=\"four\" exposed=\"true\" /></tile></map></view>",
			map::equals);
		map.addFixture(loc, new Ground(-1, "five", false));
		assertForwardDeserialization("Exposed Ground made main",
			"<view current_turn=\"-1\" current_player=\"-1\">" +
				"<map version=\"2\" rows=\"1\" columns=\"1\">" +
				"<tile row=\"0\" column=\"0\" kind=\"plains\">" +
				"<ground kind=\"five\" exposed=\"false\" />" +
				"<ground kind=\"four\" exposed=\"true\" /></tile></map></view>",
			map::equals);
		assertForwardDeserialization("Exposed Ground left as main",
			"<view current_turn=\"-1\" current_player=\"-1\">" +
				"<map version=\"2\" rows=\"1\" columns=\"1\">" +
				"<tile row=\"0\" column=\"0\" kind=\"plains\">" +
				"<ground kind=\"four\" exposed=\"true\" />" +
				"<ground kind=\"five\" exposed=\"false\" /></tile></map></view>",
			map::equals);
		this.<Ground>assertUnwantedChild(
			"<ground kind=\"sand\" exposed=\"true\"><hill /></ground>", null);
		this.<Ground>assertMissingProperty("<ground />", "kind", null);
		this.<Ground>assertMissingProperty("<ground kind=\"ground\" />", "exposed", null);
		this.assertDeprecatedProperty(
			"<ground ground=\"ground\" exposed=\"true\" />", "ground", "kind", "ground",
			new Ground(-1, "ground", true));
		assertImageSerialization("Ground image property is preserved",
			new Ground(id, "five", true));
	}

	/**
	 * Test that the code reading various fixtures whose only properties
	 * are ID and image properly objects when the XML tries to give them
	 * child tags.
	 *
	 * TODO: Convert to property/source-based testing passing in each of these tags to a single assertion
	 */
	@Test
	public void testSimpleSerializationNoChildren()
			throws SPFormatException, MalformedXMLException, IOException {
		this.<SimpleImmortal>assertUnwantedChild("<djinn><troll /></djinn>", null);
		this.<SimpleImmortal>assertUnwantedChild("<griffin><djinn /></griffin>", null);
		this.<Hill>assertUnwantedChild("<hill><griffin /></hill>", null);
		this.<SimpleImmortal>assertUnwantedChild("<minotaur><troll /></minotaur>", null);
		this.<Oasis>assertUnwantedChild("<oasis><troll /></oasis>", null);
		this.<SimpleImmortal>assertUnwantedChild("<ogre><troll /></ogre>", null);
		this.<SimpleImmortal>assertUnwantedChild("<phoenix><troll /></phoenix>", null);
		this.<SimpleImmortal>assertUnwantedChild("<simurgh><troll /></simurgh>", null);
		this.<SimpleImmortal>assertUnwantedChild("<sphinx><troll /></sphinx>", null);
		this.<SimpleImmortal>assertUnwantedChild("<troll><troll /></troll>", null);
	}

	private static Stream<Arguments> testSimpleImageSerialization() {
		return Stream.<IntFunction<? extends HasMutableImage>>of(Sphinx::new, Djinn::new,
			Griffin::new, Minotaur::new, Ogre::new, Phoenix::new, Simurgh::new,
			Troll::new, Hill::new, Oasis::new).flatMap(a ->
				SINGLETON_RANDOM.ints().boxed().limit(3).map(b -> Arguments.of(a, b)));
	}

	/**
	 * Test that various fixtures whose only properties are ID and image
	 * have their image property properly (de)serialized.
	 */
	@ParameterizedTest
	@MethodSource
	public void testSimpleImageSerialization(final IntFunction<? extends HasMutableImage> constructor,
	                                         final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		HasMutableImage item = constructor.apply(id);
		assertImageSerialization("Image property is preserved", item);
	}

	/**
	 * Test that various fixtures whose only properties are ID and image are properly (de)serialized.
	 */
	@ParameterizedTest
	@MethodSource("testSimpleImageSerialization")
	public void testSimpleSerialization(final IntFunction<? extends HasImage> constructor, final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		HasImage item = constructor.apply(id);
		if (item instanceof HasKind) {
			assertSerialization(((HasKind) item).getKind() + " serialization", item);
			this.assertMissingProperty(String.format("<%s />",
				((HasKind) item).getKind()), "id", (HasKind) constructor.apply(0));
		} else if (item instanceof Hill) {
			assertSerialization("Hill serialization", item);
			this.assertMissingProperty("<hill />", "id", new Hill(0));
		} else if (item instanceof Oasis) {
			assertSerialization("Hill serialization", item);
			this.assertMissingProperty("<oasis />", "id", new Oasis(0));
		} else {
			fail("Unhandled type");
		}
	}

	private static Stream<Arguments> testCaveSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).map(b -> Arguments.of(a, b)));
	}


	/**
	 * Test {@link Cave} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCaveSerialization(final int dc, final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("Cave serialization test", new Cave(dc, id));
		this.<Cave>assertUnwantedChild(String.format("<cave dc=\"%d\"><troll /></cave>", dc), null);
		this.<Cave>assertMissingProperty("<cave />", "dc", null);
		this.assertMissingProperty(String.format("<cave dc=\"%d\" />", dc), "id",
			new Cave(dc, 0));
		assertImageSerialization("Cave image property is preserved", new Cave(dc, id));
	}

	private static Stream<Arguments> testMineralSerialization() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(a ->
			SINGLETON_RANDOM.ints().boxed().limit(2).flatMap(b ->
				minerals.stream().collect(toShuffledStream()).flatMap(c ->
					Stream.of(true, false).flatMap(d ->
						Stream.of(true, false).map(e ->
							Arguments.of(a, b, c, d, e))))));
	}

	/**
	 * Test {@link MineralVein} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testMineralSerialization(final int dc, final int id, final String kind, final boolean exposed,
	                                     final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		MineralVein secondVein = new MineralVein(kind, exposed, dc, id);
		assertSerialization("MineralVein serialization", secondVein);
		assertDeprecatedProperty(
			createSerializedForm(secondVein, deprecatedWriter).replace("kind", "mineral"),
			"mineral", "kind", "mineral", secondVein);
		this.<MineralVein>assertUnwantedChild(
			String.format(
				"<mineral kind=\"%s\" exposed=\"%b\" dc=\"%d\"><hill/></mineral>",
				kind, exposed, dc),
			null);
		this.<MineralVein>assertMissingProperty(
			String.format("<mineral dc=\"%d\" exposed=\"%b\" />", dc, exposed),
			"kind", null);
		this.<MineralVein>assertMissingProperty(
			String.format("<mineral kind=\"%s\" exposed=\"%b\" />", kind, exposed), "dc", null);
		this.<MineralVein>assertMissingProperty(
			String.format("<mineral dc=\"%d\" kind=\"%s\" />", dc, kind),
			"exposed", null);
		this.assertMissingProperty(
			String.format("<mineral kind=\"%s\" exposed=\"%b\" dc=\"%d\" />", kind, exposed, dc),
			"id", new MineralVein(kind, exposed, dc, 0));
		assertImageSerialization("Mineral image property is preserved", secondVein);
	}

	/**
	 * Test {@link Battlefield} serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCaveSerialization")
	public void testBattlefieldSerialization(final int dc, final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		assertSerialization("Battlefield serialization test", new Battlefield(dc, id));
		this.<Battlefield>assertUnwantedChild(
			String.format("<battlefield dc=\"%d\"><hill /></battlefield>", dc), null);
		this.<Battlefield>assertMissingProperty("<battlefield />", "dc", null);
		this.assertMissingProperty(
			String.format("<battlefield dc=\"%d\" />", dc), "id", new Battlefield(dc, 0));
		assertImageSerialization("Battlefield image property is preserved",
			new Battlefield(dc, id));
	}

	/**
	 * Test that XML-reading code can handle numbers containing commas.
	 *
	 * TODO: Randomize somehow
	 */
	@Test
	public void testCommaSeparators()
			throws SPFormatException, MalformedXMLException, IOException {
		assertEquivalentForms("ID numbers can contain commas",
			"<hill id=\"1,002\" />", "<hill id=\"1002\" />", Warning.DIE);
	}

	/**
	 * Test that the old, now removed, "sandbar" tag produces only a warning if present in XML.
	 */
	@Test
	public void testOldSandbars()
			throws SPFormatException, MalformedXMLException, IOException {
		assertUnsupportedTag(
			"<view current_player=\"-1\" current_turn=\"-1\">" +
				"<map version=\"2\" rows=\"1\" columns=\"1\">" +
				"<tile row=\"0\" column=\"0\" kind=\"plains\">" +
				"<sandbar id=\"0\" /></tile></map></view>", "sandbar",
			createSimpleMap(new Point(1, 1), Pair.with(new Point(0, 0), TileType.Plains)));
	}

	private static Stream<Arguments> testElsewhere() {
		return SINGLETON_RANDOM.ints().boxed().limit(2).map(a -> Arguments.of(a));
	}

	/**
	 * Test that maps can store units (or other fixtures) with a location of "elsewhere".
	 */
	@ParameterizedTest
	@MethodSource
	public void testElsewhere(final int id)
			throws SPFormatException, MalformedXMLException, IOException {
		IMutableMapNG map = createSimpleMap(new Point(1, 1));
		map.addFixture(Point.INVALID_POINT, new Ogre(id));
		assertSerialization(
			"Map with fixture \"elsewhere\" should be properly serialized", map);
	}

	private static Stream<Arguments> testBookmarkSerialization() {
		return Stream.of(true, false).flatMap(a -> Stream.of(true, false).map(b ->
			Arguments.of(a, b)));
	}

	/**
	 * Test serialization of players' bookmarks.
	 */
	@ParameterizedTest
	@MethodSource
	public void testBookmarkSerialization(final boolean deprecatedReader, final boolean deprecatedWriter)
			throws SPFormatException, MalformedXMLException, IOException {
		IMutableMapNG map = new SPMapNG(new MapDimensionsImpl(1, 1, 2), new PlayerCollection(), 1);
		Player player = map.getPlayers().getPlayer(1);
		map.setCurrentPlayer(player);
		assertFalse(map.getBookmarks().contains(new Point(0, 0)),
			"Map by default doesn't have a bookmark");
		assertTrue(map.getBookmarks().isEmpty(), "Map by default has no bookmarks");
		map.setBaseTerrain(new Point(0, 0), TileType.Plains);
		map.addBookmark(new Point(0, 0));
		IMapReader reader = mapReaders.get((deprecatedReader) ? 0 : 1);
		IMapNG deserialized;
		try (StringReader stringReader =
				new StringReader(createSerializedForm(map, deprecatedWriter))) {
			deserialized = reader.readMapFromStream(FAKE_FILENAME, stringReader, Warning.DIE);
		}
		assertFalse(map == deserialized, "Deserialization doesn't just return the input");
		assertTrue(deserialized.getBookmarks().contains(new Point(0, 0)),
			"Deserialized map has the bookmark we saved");
	}

	private static Stream<Arguments> testRoadSerialization() {
		return Stream.of(Direction.values()).flatMap(a ->
			SINGLETON_RANDOM.ints(8).boxed().limit(1).flatMap(b ->
				Stream.of(Direction.values()).flatMap(c ->
					SINGLETON_RANDOM.ints(8).boxed().limit(1).map(d ->
						Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test serialization of roads.
	 */
	@ParameterizedTest
	@MethodSource
	public void testRoadSerialization(final Direction directionOne, final int qualityOne, final Direction directionTwo,
	                                  final int qualityTwo)
			throws SPFormatException, MalformedXMLException, IOException {
		assumeFalse(directionOne.equals(directionTwo),  "We can't have the same direction twice");
		assumeTrue(qualityOne >= 0, "Road quality can't be negative");
		assumeTrue(qualityTwo >= 0, "Road quality can't be negative");
		IMutableMapNG map = createSimpleMap(new Point(1, 1),
			Pair.with(new Point(0, 0), TileType.Plains));
		if (!Direction.Nowhere.equals(directionOne)) {
			map.setRoadLevel(new Point(0, 0), directionOne, qualityOne);
		}
		if (!Direction.Nowhere.equals(directionTwo)) {
			map.setRoadLevel(new Point(0, 0), directionTwo, qualityTwo);
		}
		assertSerialization("Map with roads is serialized properly.", map, Warning.DIE);
	}
}

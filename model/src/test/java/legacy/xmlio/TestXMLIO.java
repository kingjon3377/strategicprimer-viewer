package legacy.xmlio;

import static impl.xmlio.ISPReader.FUTURE_TAGS;
import static lovelace.util.SingletonRandom.SINGLETON_RANDOM;

import impl.xmlio.SPWriter;
import legacy.map.LegacyPlayerCollection;
import legacy.map.TileFixture;
import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.UnitMember;
import common.map.fixtures.mobile.MaturityModel;

import javax.xml.stream.XMLStreamException;

import impl.xmlio.ISPReader;
import lovelace.util.AssertAny;
import org.jetbrains.annotations.Nullable;

import org.javatuples.Pair;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import static lovelace.util.AssertAny.assertAny;

import common.idreg.DuplicateIDException;
import legacy.map.HasMutablePortrait;
import common.map.HasPortrait;
import legacy.map.Player;
import legacy.map.MutablePlayer;
import legacy.map.PlayerImpl;
import legacy.map.MapDimensionsImpl;
import legacy.map.Point;
import legacy.map.River;
import legacy.map.TileType;
import legacy.map.HasMutableImage;
import legacy.map.IMutableLegacyMap;
import legacy.map.ILegacyMap;
import legacy.map.HasNotes;
import legacy.map.LegacyMap;
import legacy.map.Direction;
import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.IMutableResourcePile;
import legacy.map.fixtures.ResourcePileImpl;
import legacy.map.fixtures.Ground;
import legacy.map.fixtures.explorable.Portal;
import legacy.map.fixtures.explorable.AdventureFixture;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.Cave;
import legacy.map.fixtures.mobile.Centaur;
import legacy.map.fixtures.mobile.Unit;
import legacy.map.fixtures.mobile.SimpleImmortal;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Giant;
import legacy.map.fixtures.mobile.Fairy;
import legacy.map.fixtures.mobile.Dragon;
import legacy.map.fixtures.mobile.Griffin;
import legacy.map.fixtures.mobile.Ogre;
import legacy.map.fixtures.mobile.Worker;

import static legacy.map.fixtures.mobile.Immortal.IMMORTAL_ANIMALS;

import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalImpl;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.WorkerStats;
import legacy.map.fixtures.mobile.worker.Skill;
import common.map.fixtures.mobile.worker.RaceFactory;
import common.map.fixtures.resources.FieldStatus;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.StoneKind;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.terrain.Oasis;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Forest;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.TownSize;
import legacy.map.fixtures.towns.Village;
import legacy.map.fixtures.towns.Town;
import legacy.map.fixtures.towns.Fortification;
import legacy.map.fixtures.towns.City;
import legacy.map.fixtures.towns.FortressImpl;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.IMutableFortress;
import legacy.map.fixtures.towns.CommunityStats;
import legacy.map.fixtures.towns.CommunityStatsImpl;

import static impl.xmlio.ISPReader.SP_NAMESPACE;

import common.xmlio.Warning;
import impl.xmlio.exceptions.UnsupportedTagException;
import impl.xmlio.exceptions.UnwantedChildException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.MissingChildException;
import impl.xmlio.exceptions.DeprecatedPropertyException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Predicate;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import legacy.map.HasImage;

import java.math.BigDecimal;
import java.util.EnumSet;

import common.xmlio.SPFormatException;
import legacy.map.fixtures.mobile.Sphinx;
import legacy.map.fixtures.mobile.Djinn;
import legacy.map.fixtures.mobile.Troll;
import legacy.map.fixtures.mobile.Simurgh;
import legacy.map.fixtures.mobile.Phoenix;
import legacy.map.fixtures.mobile.Minotaur;
import legacy.map.HasKind;

// TODO: Make methods static where possible
// FIXME: A lot of the assertions in this class have expected and actual values backwards
public final class TestXMLIO {
	private static final Set<String> RACES = Set.copyOf(RaceFactory.RACES);
	private static final List<String> ANIMAL_STATUSES = List.of("wild", "semi-domesticated", "domesticated", "tame");
	private static final List<String> TREE_TYPES =
			List.of("oak", "larch", "terebinth", "elm", "skybroom", "silver maple");
	private static final List<String> FIELD_TYPES = List.of("wheat", "amaranth", "bluegrass", "corn", "winter wheat");
	private static final List<String> MINERALS = List.of("coal", "platinum", "oil", "diamonds", "natural gas");

	/**
	 * The "filename" to give to map-readers when they require one.
	 */
	private static final Path FAKE_FILENAME = Paths.get("");
	private static final Pattern SPACED_SELF_CLOSING_TAG = Pattern.compile("\" />");
	private static final Pattern KIND_EQUALS_PATTERN = Pattern.compile("kind=");

	/**
	 * The map readers to test each other against.
	 *
	 * TODO: Extract interface for ISPReader&amp;IMapReader, so we don't
	 * have to maintain separate names for the same objects.
	 */
	private final List<ISPReader> spReaders = List.of(TestReaderFactory.getOldSPReader(),
			TestReaderFactory.getNewSPReader());
	private static final List<IMapReader> MAP_READERS = List.of(TestReaderFactory.getOldMapReader(),
			TestReaderFactory.getNewMapReader());

	/**
	 * Assert that the given XML will produce the given kind of warning and
	 * that the warning satisfies the given additional assertions. If
	 * "desideratum" is {@code null}, assert that the exception
	 * is always thrown; if not, assert that the XML will fail with
	 * warnings made fatal, but will pass and produce "desideratum"
	 * with warnings ignored.
	 *
	 * TODO: Split 'fatal error' and 'warning' cases into separate methods?
	 */
	@SafeVarargs
	private static <Type, Expectation extends Exception> void assertFormatIssue(
			final ISPReader reader, final String xml, final @Nullable Type desideratum,
			final Class<Expectation> exceptionClass, final Consumer<Expectation>... checks)
			throws SPFormatException, XMLStreamException, IOException {
		if (Objects.isNull(desideratum)) {
			try (final StringReader stringReader = new StringReader(xml)) {
				reader.<Type>readXML(FAKE_FILENAME, stringReader, Warning.IGNORE);
				fail("Expected a(n) %s to be thrown".formatted(exceptionClass.getName()));
			} catch (final RuntimeException except) {
				final Throwable cause = except.getCause();
				assertInstanceOf(exceptionClass, cause, "Exception should be of the right type");
				for (final Consumer<Expectation> check : checks) {
					check.accept(exceptionClass.cast(cause));
				}
			} catch (final Exception except) {
				assertInstanceOf(exceptionClass, except, "Exception should be of the right type");
				for (final Consumer<Expectation> check : checks) {
					check.accept(exceptionClass.cast(except));
				}
			}
		} else {
			try (final StringReader stringReader = new StringReader(xml)) {
				final Type returned = reader.readXML(FAKE_FILENAME, stringReader,
						Warning.IGNORE);
				assertEquals(desideratum, returned,
						"Parsed value should be as expected with warnings ignored.");
			}
			try (final StringReader stringReader = new StringReader(xml)) {
				reader.<Type>readXML(FAKE_FILENAME, stringReader, Warning.DIE);
				fail("Expected a fatal warning");
			} catch (final RuntimeException except) {
				final Throwable cause = except.getCause();
				assertInstanceOf(exceptionClass, cause, "Exception should be of the right type");
				for (final Consumer<Expectation> check : checks) {
					check.accept(exceptionClass.cast(cause));
				}
			} catch (final Throwable except) {
				assertInstanceOf(exceptionClass, except, "Exception should be of the right type");
				for (final Consumer<Expectation> check : checks) {
					check.accept(exceptionClass.cast(except));
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
	private <Type> void assertUnsupportedTag(final String xml, final String tag, final @Nullable Type desideratum)
			throws SPFormatException, XMLStreamException, IOException {
		final Consumer<UnsupportedTagException> assertion =
				(except) -> assertEquals(tag, except.getTag().getLocalPart(),
						"Unsupported tag was the tag we expected");
		for (final ISPReader reader : spReaders) {
			assertFormatIssue(reader, xml, desideratum,
					UnsupportedTagException.class,
					assertion);
		}
	}

	/**
	 * Assert that reading the given XML will produce an {@link
	 * UnwantedChildException}. If it's only supposed to be a warning,
	 * assert that it'll pass with warnings disabled but fail with warnings
	 * made fatal.
	 */
	private <Type> void assertUnwantedChild(final String xml, final @Nullable Type desideratum)
			throws SPFormatException, XMLStreamException, IOException {
		for (final ISPReader reader : spReaders) {
			assertFormatIssue(reader, xml, desideratum,
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
	                                          final @Nullable Type desideratum)
			throws SPFormatException, XMLStreamException, IOException {
		final Consumer<MissingPropertyException> assertion =
				(except) -> assertEquals(property, except.getParam(),
						"Missing property should be the one we're expecting");
		for (final ISPReader reader : spReaders) {
			assertFormatIssue(reader, xml, desideratum,
					MissingPropertyException.class,
					assertion);
		}
	}

	/**
	 * Assert that reading the given XML will give a MissingChildException.
	 */
	private <Type> void assertMissingChild(final String xml)
			throws SPFormatException, XMLStreamException, IOException {
		for (final ISPReader reader : spReaders) {
			TestXMLIO.<Type, MissingChildException>assertFormatIssue(reader, xml, null,
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
	                                             final String tag, final @Nullable Type desideratum)
			throws SPFormatException, XMLStreamException, IOException {
		final Consumer<DeprecatedPropertyException> expectedProperty =
				(except) -> assertEquals(deprecated, except.getOld(),
						"Missing property should be the one we're expecting");
		final Consumer<DeprecatedPropertyException> expectedTag =
				(except) -> assertEquals(tag, except.getTag().getLocalPart(),
						"Missing property should be on the tag we expect");
		final Consumer<DeprecatedPropertyException> expectedPreferred =
				(except) -> assertEquals(preferred, except.getPreferred(),
						"Preferred form should be as expected");
		for (final ISPReader reader : spReaders) {
			assertFormatIssue(reader, xml, desideratum,
					DeprecatedPropertyException.class,
					expectedProperty, expectedTag, expectedPreferred);
		}
	}

	// TODO: Reformat methods using these extracted helpers
	private static Stream<Integer> integers(final long count) {
		return SINGLETON_RANDOM.ints(count).boxed();
	}

	private static final List<Boolean> BOOLS = List.of(true, false);

	private static final List<SPWriter> WRITERS = List.of(TestReaderFactory.getOldWriter(),
			TestReaderFactory.getNewWriter());

	private static Stream<Boolean> bools() {
		return BOOLS.stream();
	}

	private static Stream<SPWriter> writers() {
		return Stream.of(TestReaderFactory.getOldWriter(), TestReaderFactory.getNewWriter());
	}

	/**
	 * Create the XML-serialized representation of an object.
	 *
	 * @param obj        The object to serialize
	 * @param spWriter   The writer to use to create the serialized form
	 */
	private static String createSerializedForm(final Object obj, final SPWriter spWriter)
			throws XMLStreamException, IOException {
		final StringBuilder writer = new StringBuilder();
		spWriter.writeSPObject(writer::append, obj);
		return writer.toString();
	}

	/**
	 * Assert that the serialized form of the given object will deserialize without error.
	 */
	private void assertSerialization(final String message, final Object obj)
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization(message, obj, Warning.DIE);
	}

	/**
	 * Assert that the serialized form of the given object will deserialize without error.
	 */
	private void assertSerialization(final String message, final Object obj, final Warning warner)
			throws SPFormatException, XMLStreamException, IOException {
		for (final ISPReader reader : spReaders) {
			for (final SPWriter writer : WRITERS) {
				try (final StringReader stringReader =
						     new StringReader(createSerializedForm(obj, writer))) {
					assertEquals(obj, reader.readXML(FAKE_FILENAME, stringReader,
							warner), message);
				}
			}
		}
	}

	/**
	 * Assert that the serialized form of the given object, using both
	 * writers, will contain the given string.
	 */
	private static void assertSerializedFormContains(final Object obj, final String expected, final String message)
			throws XMLStreamException, IOException {
		for (final SPWriter writer : WRITERS) {
			// TODO: Is there a JUnit assertContains() or similar?
			assertTrue(createSerializedForm(obj, writer).contains(expected), message);
		}
	}

	/**
	 * Assert that the given object, if serialized and deserialized, will
	 * have its image property preserved. We modify that property, but set
	 * it back to the original value before exiting this method.
	 */
	private void assertImageSerialization(final String message, final HasMutableImage obj)
			throws SPFormatException, XMLStreamException, IOException {
		final String oldImage = obj.getImage();
		for (final ISPReader reader : spReaders) {
			for (final SPWriter writer : WRITERS) {
				obj.setImage("xyzzy"); // TODO: Should randomly generate a string
				try (final StringReader stringReader =
						     new StringReader(createSerializedForm(obj, writer))) {
					assertEquals(obj.getImage(), reader.<HasMutableImage>readXML(
									FAKE_FILENAME, stringReader, Warning.IGNORE).getImage(),
							message);
				}
				obj.setImage(obj.getDefaultImage());
				assertFalse(createSerializedForm(obj, writer).contains("image="),
						"Default image should not be written");
				obj.setImage("");
				assertFalse(createSerializedForm(obj, writer).contains("image="),
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
			throws SPFormatException, XMLStreamException, IOException {
		for (final ISPReader reader : spReaders) {
			for (final SPWriter writer : WRITERS) {
				try (final StringReader stringReader =
						     new StringReader(createSerializedForm(obj, writer))) {
					final HasNotes read = reader.readXML(FAKE_FILENAME,
							stringReader, Warning.IGNORE);
					for (final Integer player : obj.getNotesPlayers()) {
						assertEquals(obj.getNote(player), read.getNote(player),
								message);
					}
					for (final Integer player : read.getNotesPlayers()) {
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
			throws SPFormatException, XMLStreamException, IOException {
		final String oldPortrait = obj.getPortrait();
		for (final ISPReader reader : spReaders) {
			for (final SPWriter writer : WRITERS) {
				obj.setPortrait("xyzzy");
				try (final StringReader stringReader =
						     new StringReader(createSerializedForm(obj, writer))) {
					assertEquals(obj.getPortrait(), reader.<HasPortrait>readXML(
									FAKE_FILENAME, stringReader, Warning.IGNORE).getPortrait(),
							message);
				}
				obj.setPortrait("");
				assertFalse(createSerializedForm(obj, writer).contains("portrait="),
						"Empty portrait should not be written");
			}
		}
		obj.setPortrait(oldPortrait);
	}

	private <Type> void assertForwardDeserialization(final String message, final String xml,
	                                                 final Predicate<Type> assertion)
			throws SPFormatException, XMLStreamException, IOException {
		assertForwardDeserialization(message, xml, assertion, Warning.DIE);
	}

	private <Type> void assertForwardDeserializationEquality(final String message, final String xml,
	                                                         final Type expected)
			throws SPFormatException, XMLStreamException, IOException {
		assertForwardDeserializationEquality(message, xml, expected, Warning.DIE);
	}

	/**
	 * Assert that a "forward idiom"---an idiom that we do not yet (or,
	 * conversely, anymore) produce, but want to accept---will be handled
	 * properly by both readers.
	 *
	 * TODO: should "assertion" be a Consumer instead of a Predicate?
	 *
	 * @param message  The assertion message
	 * @param xml      The serialized form
	 * @param expected A value that should be "equal to" the deserialized object.
	 * @param warner   The warning level to use for this assertion
	 */
	private <Type> void assertForwardDeserializationEquality(final String message, final String xml,
	                                                         final Type expected, final Warning warner)
			throws SPFormatException, XMLStreamException, IOException {
		for (final ISPReader reader : spReaders) {
			try (final StringReader stringReader = new StringReader(xml)) {
				assertEquals(expected, reader.readXML(FAKE_FILENAME, stringReader, warner), message);
			}
		}
	}

	/**
	 * Assert that a "forward idiom"---an idiom that we do not yet (or,
	 * conversely, anymore) produce, but want to accept---will be handled
	 * properly by both readers.
	 *
	 * TODO: should "assertion" be a Consumer instead of a Predicate?
	 *
	 * @param message   The assertion message
	 * @param xml       The serialized form
	 * @param assertion A lambda to check the state of the deserialized object
	 * @param warner    The warning level to use for this assertion
	 */
	private <Type> void assertForwardDeserialization(final String message, final String xml,
	                                                 final Predicate<Type> assertion, final Warning warner)
			throws SPFormatException, XMLStreamException, IOException {
		for (final ISPReader reader : spReaders) {
			try (final StringReader stringReader = new StringReader(xml)) {
				assertTrue(assertion.test(reader.readXML(FAKE_FILENAME, stringReader,
						warner)), message);
			}
		}
	}

	/**
	 * Assert that two serialized forms are equivalent, using both readers.
	 *
	 * @param message      The assertion message to use
	 * @param firstForm    The first serialized form
	 * @param secondForm   The second serialized form
	 * @param warningLevel The warning level to use
	 */
	private void assertEquivalentForms(final String message, final String firstForm, final String secondForm,
	                                   final Warning warningLevel)
			throws SPFormatException, XMLStreamException, IOException {
		for (final ISPReader reader : spReaders) {
			try (final StringReader firstReader = new StringReader(firstForm);
			     final StringReader secondReader = new StringReader(secondForm)) {
				assertEquals((Object) reader.readXML(FAKE_FILENAME, firstReader, warningLevel),
						reader.readXML(FAKE_FILENAME, secondReader, warningLevel),
						message);
			}
		}
	}

	/**
	 * Assert that a map is properly deserialized (by the main map-deserialization methods).
	 */
	private void assertMapDeserialization(final String message, final ILegacyMap expected, final String xml)
			throws SPFormatException, XMLStreamException, IOException {
		for (final IMapReader reader : MAP_READERS) {
			try (final StringReader stringReader = new StringReader(xml)) {
				assertEquals(expected, reader.readMapFromStream(FAKE_FILENAME, stringReader,
						Warning.DIE), message);
			}
		}
	}

	/**
	 * Assert that the given XML will produce warnings about duplicate IDs.
	 */
	private <Type> void assertDuplicateID(final String xml, final Type desideratum)
			throws SPFormatException, XMLStreamException, IOException {
		for (final ISPReader reader : spReaders) {
			assertFormatIssue(reader, xml, desideratum,
					DuplicateIDException.class);
		}
	}

	/**
	 * @deprecated Use {@link AssertAny} and {@link Assertions#assertInstanceOf}.
	 */
	@Deprecated
	@SafeVarargs
	private static Predicate<Throwable> instanceOfAny(final Class<? extends Throwable>... types) {
		return (except) -> {
			for (final Class<? extends Throwable> type : types) {
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
			throws SPFormatException, XMLStreamException, IOException {
		final Consumer<Exception> assertion = // TODO: Extract 'assert instance of any' helper method (in lovelace-util)
				except -> assertAny("Exception is of an expected type: was %s".formatted(except.getClass().getName()),
						() -> assertInstanceOf(NoSuchElementException.class, except),
						() -> assertInstanceOf(IllegalArgumentException.class, except),
						() -> assertInstanceOf(XMLStreamException.class, except)
				);
		for (final ISPReader reader : spReaders) {
			assertFormatIssue(reader, xml, null, Exception.class,
					assertion);
		}
	}

	/**
	 * Encapsulate the given string in a "tile" tag inside a "map" tag.
	 */
	private static String encapsulateTileString(final String str) {
		return """
				<map version="2" rows="2" columns="2">
				<tile row="1" column="1" kind="plains">%s</tile></map>""".formatted(str);
	}

	private static <T> Collector<T, ?, Stream<T>> toShuffledStream(final long limit) {
		return Collectors.collectingAndThen(Collectors.toList(), collected -> {
			Collections.shuffle(collected);
			return collected.stream().limit(limit);
		});
	}

	private static Stream<Arguments> testVillageWantsName() {
		return Stream.of(TownStatus.values()).flatMap(a ->
				integers(2).flatMap(b ->
						RACES.stream().collect(toShuffledStream(3)).flatMap(c ->
								writers().map(d -> Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test that deserializing a {@link Village} without a name will cause a warning.
	 *
	 * TODO: if test fails because boxed Integer and Boolean can't be assigned to unboxed primitives,
	 * change parameter types in this method's signature
	 */
	@ParameterizedTest
	@MethodSource
	public void testVillageWantsName(final TownStatus status, final int id, final String race,
	                                 final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final Village village = new Village(status, "", id, new PlayerImpl(-1, ""), race);
		assertMissingProperty(createSerializedForm(village, writer), "name", village);
	}

	private static Stream<Arguments> testBasicVillageSerialization() {
		return TREE_TYPES.stream().collect(toShuffledStream(2)).flatMap(a ->
				Stream.of(TownStatus.values()).flatMap(b ->
						RACES.stream().collect(toShuffledStream(3)).flatMap(c ->
								integers(2).map(d ->
										Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test basic (de)serialization of {@link Village villages}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testBasicVillageSerialization(final String name, final TownStatus status, final String race,
	                                          final int id) throws SPFormatException, XMLStreamException, IOException {
		final Player owner = new PlayerImpl(-1, "");
		final Village village = new Village(status, name, id, owner, race);
		assertSerialization("Basic Village serialization",
				new Village(status, name, id, owner, race));
		this.<Village>assertUnwantedChild("""
				<village status="%s"><village /></village>""".formatted(status), null);
		this.<Village>assertMissingProperty("<village />", "status", null);
		assertMissingProperty("""
						<village name="%s" status="%s" />""".formatted(name, status), "id",
				new Village(status, name, 0, new PlayerImpl(-1, "Independent"), "dwarf"));
		assertMissingProperty("""
						<village race="%s" name="%s" status="%s" id="%d" />""".formatted(race, name, status, id),
				"owner", new Village(status, name, id, new PlayerImpl(-1, "Independent"), race));
		assertImageSerialization("Village image property is preserved", village);
		assertPortraitSerialization("Village portrait property is preserved", village);
	}

	// FIXME: Pass some bound to each of these nextInt() calls, instead of calling assume() so much in the test method.
	private static Stream<Arguments> testVillagePopulationSerialization() {
		return Stream.of(TownStatus.values()).flatMap(a ->
				RACES.stream().collect(toShuffledStream(3)).map(b ->
						Arguments.of(a, b, randomInteger(), randomInteger(),
								randomInteger(), randomInteger(),
								randomInteger(), randomInteger(),
								randomInteger())));
	}

	/**
	 * @return a random integer for parameterized tests.
	 */
	private static int randomInteger() {
		return SINGLETON_RANDOM.nextInt(Integer.MAX_VALUE);
	}

	@AfterEach
	public void resetCurrentTurn() {
		MaturityModel.resetCurrentTurn();
	}

	/**
	 * Test (de)serialization of {@link Village villages'} {@link CommunityStats population details}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testVillagePopulationSerialization(final TownStatus status, final String race, final int id,
	                                               final int populationSize, final int workedField,
	                                               final int producedId, final int producedQty,
	                                               final int consumedId, final int consumedQty)
			throws SPFormatException, XMLStreamException, IOException {
		assumeTrue(populationSize >= 0, "Population can't be negative");
		assumeTrue(workedField >= 0, "Field ID won't ever be negative");
		assumeTrue(producedId >= 0, "Production ID won't ever be negative");
		assumeTrue(producedQty >= 0, "Quantity can't be negative");
		assumeTrue(consumedId >= 0, "Consumption ID won't ever be negative");
		assumeTrue(consumedQty >= 0, "Quantity can't be negative");
		final Village village = new Village(status, "villageName", id, new PlayerImpl(-1, ""), race);
		final CommunityStats pop = new CommunityStatsImpl(populationSize);
		village.setPopulation(pop);
		assertSerialization("Village can have community stats", village);
		// FIXME: That doesn't guarantee that Village#equals checks
		// 'population' ... try testing that it doesn't equal a
		// different one
		pop.addWorkedField(workedField);
		// TODO: Here and below, randomize strings in production, consumption, and skills
		// TODO: We'd like to randomize number of skills, number of worked fields, etc.
		pop.addYearlyProduction(new ResourcePileImpl(producedId, "prodKind", "production",
				new LegacyQuantity(producedQty, "single units")));
		pop.addYearlyConsumption(new ResourcePileImpl(consumedId, "consKind", "consumption",
				new LegacyQuantity(consumedQty, "double units")));
		assertSerialization("Village stats can have both production and consumption",
				village);
	}

	// TODO: reformat
	private static Stream<Arguments> testCityWantsName() {
		return Stream.of(TownSize.values()).flatMap(a -> Stream.of(TownStatus.values()).flatMap(b ->
				integers(2).flatMap(c ->
						integers(2).flatMap(d ->
								writers().map(e -> Arguments.of(a, b, c, d, e))))));
	}

	/**
	 * Test that deserializing a {@link City} without a name will cause a warning.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCityWantsName(final TownSize size, final TownStatus status, final int id, final int dc,
	                              final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final City city = new City(status, size, dc, "", id, new PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(city, writer), "name", city);
	}

	// TODO: reformat
	private static Stream<Arguments> testCitySerialization() {
		return Stream.of(TownSize.values()).flatMap(a -> Stream.of(TownStatus.values()).flatMap(b ->
				TREE_TYPES.stream().collect(toShuffledStream(2)).map(c ->
						Arguments.of(a, b, randomInteger(),
								randomInteger(), c))));
	}

	/**
	 * Test basic (de)serialization of {@link City cities}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCitySerialization(final TownSize size, final TownStatus status, final int id, final int dc,
	                                  final String name) throws SPFormatException, XMLStreamException, IOException {
		final Player owner = new PlayerImpl(-1, "");
		assertSerialization("City serialization", new City(status, size, dc, name, id, owner));
		final City city = new City(status, size, dc, "", id, owner);
		this.<City>assertUnwantedChild("""
				<city status="%s" size="%s" name="%s" dc="%d"><troll /></city>"""
				.formatted(status, size, name, dc), null);
		assertMissingProperty("""
						<city status="%s" size="%s" name="%s" dc="%d" id="%d" />"""
						.formatted(status, size, name, dc, id), "owner",
				new City(status, size, dc, name, id, new PlayerImpl(-1, "Independent")));
		assertImageSerialization("City image property is preserved", city);
		assertPortraitSerialization("City portrait property is preserved", city);
	}

	private static Stream<Arguments> testCityPopulationSerialization() {
		return TREE_TYPES.stream().collect(toShuffledStream(2)).flatMap(a ->
				Stream.of(TownSize.values()).flatMap(b -> Stream.of(TownStatus.values()).flatMap(c ->
						RACES.stream().collect(toShuffledStream(3)).map(d ->
								Arguments.of(a, b, c, d, randomInteger(),
										randomInteger(), randomInteger(),
										randomInteger(), randomInteger(),
										randomInteger(), randomInteger())))));
	}

	/**
	 * Test (de)serialization of {@link City cities'} {@link CommunityStats population details}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCityPopulationSerialization(final String name, final TownSize size, final TownStatus status,
	                                            final String race, final int id, final int dc, final int populationSize,
	                                            final int workedField, final int skillLevel, final int producedId,
	                                            final int producedQty)
			throws SPFormatException, XMLStreamException, IOException {
		assumeTrue(populationSize >= 0, "Population can't be negative");
		assumeTrue(workedField >= 0, "Field ID won't ever be negative");
		assumeTrue(skillLevel >= 0, "Skill level can't be negative");
		assumeTrue(producedId >= 0, "Production ID won't ever be negative");
		assumeTrue(producedQty >= 0, "Quantity can't be negative");
		final Player owner = new PlayerImpl(-1, "");
		final City city = new City(status, size, dc, name, id, owner);
		final CommunityStats population = new CommunityStatsImpl(populationSize);
		population.addWorkedField(workedField);
		population.setSkillLevel("citySkill", skillLevel);
		population.addYearlyConsumption(new ResourcePileImpl(producedId, "cityResource",
				"citySpecific", new LegacyQuantity(producedQty, "cityUnit")));
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
	                                       final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final Fortification fort = new Fortification(status, size, dc, "", id, new PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(fort, writer), "name", fort);
	}

	/**
	 * Test basic {@link Fortification} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testFortificationSerialization(final TownSize size, final TownStatus status, final int id, final int dc,
	                                           final String name)
			throws SPFormatException, XMLStreamException, IOException {
		final Player owner = new PlayerImpl(-1, "");
		assertSerialization("Fortification serialization",
				new Fortification(status, size, dc, name, id, owner));
		final Fortification fort = new Fortification(status, size, 30, "", 3, owner);
		this.<Fortification>assertUnwantedChild("""
				<fortification status="%s" size="%s" name="%s" dc="%s"><troll /><fortification>"""
				.formatted(status, size, name, dc), null);
		assertMissingProperty("""
						<fortification status="%s" size="%s" name="%s" dc="%s" id="%d" />""".formatted(
						status, size, name, dc, id), "owner",
				new Fortification(status, size, dc, name, id, new PlayerImpl(-1, "Independent")));
		assertImageSerialization("Fortification image property is preserved", fort);
		assertPortraitSerialization("Fortification portrait property is preserved", fort);
	}

	/**
	 * Test (de)serialization of {@link Fortification fortifications'}
	 * {@link CommunityStats population details}.
	 */
	@SuppressWarnings("MagicNumber")
	@ParameterizedTest
	@MethodSource("testCityPopulationSerialization")
	public void testFortificationPopulationSerialization(final String name, final TownSize size,
	                                                     final TownStatus status, final String race, final int id,
	                                                     final int dc, final int populationSize, final int workedField,
	                                                     final int skillLevel, final int producedId,
	                                                     final int producedQty)
			throws SPFormatException, XMLStreamException, IOException {
		assumeTrue(populationSize >= 0, "Population can't be negative");
		assumeTrue(workedField >= 0, "Field ID won't ever be negative");
		assumeTrue(skillLevel >= 0, "Skill level can't be negative");
		assumeTrue(producedId >= 0, "Production ID won't ever be negative");
		assumeTrue(producedQty >= 0, "Quantity can't be negative");
		final Player owner = new PlayerImpl(-1, "");
		final Fortification fort = new Fortification(status, size, dc, name, id, owner);
		final CommunityStats population = new CommunityStatsImpl(populationSize);
		population.addWorkedField(workedField);
		population.addWorkedField((workedField * 13) % 31);
		population.setSkillLevel("fortSkill", skillLevel);
		population.addYearlyProduction(new ResourcePileImpl(producedId, "fortResource",
				"fortSpecific", new LegacyQuantity(1, "fortUnit")));
		fort.setPopulation(population);
		assertSerialization("Fortification can have community-stats", fort);
	}

	/**
	 * Test that deserializing a {@link Town} without a name triggers a warning.
	 */
	@ParameterizedTest
	@MethodSource("testCityWantsName")
	public void testTownWantsName(final TownSize size, final TownStatus status, final int id, final int dc,
	                              final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final Town town = new Town(status, size, dc, "", id, new PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(town, writer), "name", town);
	}

	/**
	 * Test basic {@link Town} (de)serialization.
	 *
	 * TODO: Split and further randomize this and further tests
	 */
	@SuppressWarnings("MagicNumber")
	@ParameterizedTest
	@MethodSource("testCitySerialization")
	public void testTownSerialization(final TownSize size, final TownStatus status, final int id, final int dc,
	                                  final String name) throws SPFormatException, XMLStreamException, IOException {
		final Player owner = new PlayerImpl(-1, "");
		assertSerialization("Town serialization test", new Town(status, size, dc, name, id, owner));
		final Town town = new Town(status, size, dc, name, id, owner);
		this.<Town>assertUnwantedChild("""
				<town status="%s" size="%s" name="%s" dc="%s"><troll /></town>""".formatted(
				status, size, name, dc), null);
		assertMissingProperty("""
						<town status="%s" size="%s" name="%s" dc="%d" id="%d" />""".formatted(
						status, size, name, dc, id), "owner",
				new Town(status, size, dc, name, id, new PlayerImpl(-1, "Independent")));
		assertImageSerialization("Town image property is preserved", town);
		assertPortraitSerialization("Town portrait property is preserved", town);
		final CommunityStats population = new CommunityStatsImpl(3);
		population.addWorkedField(9);
		population.addWorkedField(23);
		population.setSkillLevel("townSkill", 3);
		population.setSkillLevel("secondSkill", 5);
		population.addYearlyProduction(new ResourcePileImpl(5, "townResource", "townSpecific",
				new LegacyQuantity(1, "TownUnit")));
		population.addYearlyProduction(new ResourcePileImpl(8, "townResource", "secondSpecific",
				new LegacyQuantity(2, "townUnit")));
		town.setPopulation(population);
		assertSerialization("Fortification can have community-stats", town);
	}

	/**
	 * Test {@link StoneDeposit} (de)serialization.
	 */
	@SuppressWarnings("MagicNumber")
	@ParameterizedTest
	@EnumSource(StoneKind.class)
	public void testStoneSerialization(final StoneKind kind)
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("First StoneDeposit test, kind: " + kind, new StoneDeposit(kind, 8, 1));
		assertSerialization("Second StoneDeposit test, kind: " + kind, new StoneDeposit(kind, 15, 2));
		assertImageSerialization("Stone image property is preserved", new StoneDeposit(kind, 10, 3));
	}

	private static Stream<Arguments> testOldStoneIdiom() {
		return Stream.of(StoneKind.values()).flatMap(a -> writers().map(b ->
				Arguments.of(a, b)));
	}

	/**
	 * Test deserialization of the old XML idiom for {@link StoneDeposit stone deposits}.
	 */
	@ParameterizedTest
	@MethodSource
	public void testOldStoneIdiom(final StoneKind kind, final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final StoneDeposit thirdDeposit = new StoneDeposit(kind, 10, 3);
		assertDeprecatedProperty(createSerializedForm(thirdDeposit, writer)
				.replace("kind", "stone"), "stone", "kind", "stone", thirdDeposit);
	}

	/**
	 * Test that {@link StoneDeposit} deserialization rejects invalid input.
	 */
	@ParameterizedTest
	@EnumSource(StoneKind.class)
	public void testStoneSerializationErrors(final StoneKind kind)
			throws SPFormatException, XMLStreamException, IOException {
		this.<StoneDeposit>assertUnwantedChild("""
				<stone kind="%s" dc="10"><troll /></stone>""".formatted(kind), null);
		this.<StoneDeposit>assertMissingProperty("""
				<stone kind="%s" />""".formatted(kind), "dc", null);
		this.<StoneDeposit>assertMissingProperty("""
				<stone dc="10" />""", "kind", null);
		assertMissingProperty("""
						<stone kind="%s" dc="0" />""".formatted(kind), "id",
				new StoneDeposit(kind, 0, 0));
	}

	/**
	 * A factory to encapsulate rivers in a simple map.
	 */
	private static ILegacyMap encapsulateRivers(final Point point, final River... rivers) {
		final IMutableLegacyMap retval = new LegacyMap(new MapDimensionsImpl(point.row() + 1,
				point.column() + 1, 2), new LegacyPlayerCollection(), -1);
		retval.setBaseTerrain(point, TileType.Plains);
		retval.addRivers(point, rivers);
		return retval;
	}

	/**
	 * Create a simple map.
	 */
	@SafeVarargs
	private static IMutableLegacyMap createSimpleMap(final Point dims, final Pair<Point, TileType>... terrain) {
		final IMutableLegacyMap retval = new LegacyMap(new MapDimensionsImpl(dims.row(),
				dims.column(), 2), new LegacyPlayerCollection(), -1);
		for (final Pair<Point, TileType> pair : terrain) {
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
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("First Player serialization test", new PlayerImpl(1, "one"));
		assertSerialization("Second Player serialization test", new PlayerImpl(2, "two"));
		assertSerialization("Player with country", new PlayerImpl(3, "three", "country"));
		this.<Player>assertUnwantedChild("""
				<player code_name="one" number="1"><troll /></player>""", null);
		this.<Player>assertMissingProperty("""
				<player code_name="one" />""", "number", null);
		this.<Player>assertMissingProperty("""
				<player number="1" />""", "code_name", null);
		assertPortraitSerialization("Players can have associated portraits",
				new PlayerImpl(3, "three"));
	}

	/**
	 * Test that {@link River rivers} are properly (de)serialized in the simplest case.
	 */
	@ParameterizedTest
	@EnumSource(River.class)
	public void testSimpleRiverSerialization(final River river)
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("River alone", river);
		final Point loc = new Point(0, 0);
		assertSerialization("River in tile", encapsulateRivers(loc, river));
	}

	/**
	 * Test {@link River} (de)serialization in more complicated cases,
	 * including ways that have improperly failed in the past.
	 */
	@Test
	public void testRiverSerializationOne()
			throws SPFormatException, XMLStreamException, IOException {
		this.<ILegacyMap>assertUnwantedChild(encapsulateTileString("<lake><troll /></lake>"), null);
		this.<ILegacyMap>assertMissingProperty(encapsulateTileString("<river />"), "direction", null);
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
		this.<ILegacyMap>assertMissingProperty(
				encapsulateTileString("""
						<river direction="invalid" />"""), "direction",
				null);
	}

	/**
	 * Test (de)serialization of a single simple tile.
	 *
	 * TODO: Split and randomize
	 */
	@Test
	public void testSimpleTileSerializtion()
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("Simple Tile", createSimpleMap(new Point(1, 1),
				Pair.with(new Point(0, 0), TileType.Desert)));
		final IMutableLegacyMap firstMap = createSimpleMap(new Point(2, 2),
				Pair.with(new Point(1, 1), TileType.Plains));
		firstMap.addFixture(new Point(1, 1), new Griffin(1));
		assertSerialization("Tile with one fixture", firstMap);
		final IMutableLegacyMap secondMap = createSimpleMap(new Point(3, 3),
				Pair.with(new Point(2, 2), TileType.Steppe));
		secondMap.addFixture(new Point(2, 2),
				new Unit(new PlayerImpl(-1, ""), "unitOne", "firstUnit", 1));
		secondMap.addFixture(new Point(2, 2), new Forest("forestKind", true, 8));
		assertSerialization("Tile with two fixtures", secondMap);
		this.<ILegacyMap>assertMissingProperty("""
						<map version="2" rows="1" columns="1"><tile column="0" kind="plains" /></map>""",
				"row", null);
		this.<ILegacyMap>assertMissingProperty("""
						<map version="2" rows="1" columns="1"><tile row="0" kind="plains" /></map>""",
				"column", null);
		this.<ILegacyMap>assertMissingProperty("""
						<map version="2" rows="1" columns="1"><tile row="0" column="0" /></map>""",
				"kind", new LegacyMap(new MapDimensionsImpl(1, 1, 2), new LegacyPlayerCollection(), 0));
		this.<ILegacyMap>assertUnwantedChild(encapsulateTileString("""
				<tile row="2" column="0" kind="plains" />"""), null);
	}

	/**
	 * Further test serialization of a tile's contents.
	 */
	@Test
	public void testTileSerialization()
			throws SPFormatException, XMLStreamException, IOException {
		final IMutableLegacyMap thirdMap = createSimpleMap(new Point(4, 4),
				Pair.with(new Point(3, 3), TileType.Jungle));
		final Player playerOne = new PlayerImpl(2, "");
		final IMutableFortress fort = new FortressImpl(playerOne, "fortOne", 1,
				TownSize.Small);
		fort.addMember(new Unit(playerOne, "unitTwo", "secondUnit", 2));
		thirdMap.addFixture(new Point(3, 3), fort);
		thirdMap.addFixture(new Point(3, 3), new TextFixture("Random text here", 5));
		thirdMap.addRivers(new Point(3, 3), River.Lake);
		thirdMap.addPlayer(playerOne);
		assertSerialization("More complex tile", thirdMap);
	}

	private static Stream<Arguments> testTileDeprecatedIdiom() {
		return Stream.of(TileType.values()).flatMap(a -> writers().map(b ->
				Arguments.of(a, b)));
	}

	/**
	 * Test that the deprecated XML idiom for tile types is still supported.
	 */
	@ParameterizedTest
	@MethodSource
	public void testTileDeprecatedIdiom(final TileType terrain, final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final ILegacyMap map = createSimpleMap(new Point(5, 5), Pair.with(new Point(4, 4), terrain));
		assertDeprecatedProperty(createSerializedForm(map, writer)
				.replace("kind", "type"), "type", "kind", "tile", map);
	}

	/**
	 * A further test of (de)serialization of a tile.
	 */
	@Test
	public void testTileSerializationTwo()
			throws SPFormatException, XMLStreamException, IOException {
		final IMutableLegacyMap five = createSimpleMap(new Point(3, 4), Pair.with(new Point(2, 3),
				TileType.Jungle));
		final Player player = new PlayerImpl(2, "playerName");
		five.addFixture(new Point(2, 3), new Unit(player, "explorer", "name one", 1));
		five.addFixture(new Point(2, 3), new Unit(player, "explorer", "name two", 2));
		five.addPlayer(player);
		assertEquals(2, five.getFixtures(new Point(2, 3)).size(), "Just checking ...");
		assertSerialization("Multiple units should come through", five);
		final String xmlTwoLogical = """
				<view xmlns="%s" current_player="-1" current_turn="-1">
				\t<map version="2" rows="3" columns="4">
				\t\t<player number="2" code_name="playerName" />
				\t\t<row index="2">
				\t\t\t<tile row="2" column="3" kind="jungle">
				\t\t\t\t<unit owner="2" kind="explorer" name="name one" id="1" />
				\t\t\t\t<unit owner="2" kind="explorer" name="name two" id="2" />
				\t\t\t</tile>
				\t\t</row>
				\t</map>
				</view>
				""".formatted(SP_NAMESPACE);
		assertEquals(createSerializedForm(five, TestReaderFactory.getOldWriter()), xmlTwoLogical, "Multiple units");
		final String xmlTwoAlphabetical = """
				<view current_player="-1" current_turn="-1" xmlns="%s">
				\t<map columns="4" rows="3" version="2">
				\t\t<player number="2" code_name="playerName" />
				\t\t<row index="2">
				\t\t\t<tile column="3" kind="jungle" row="2">
				\t\t\t\t<unit id="1" kind="explorer" name="name one" owner="2" />
				\t\t\t\t<unit id="2" kind="explorer" name="name two" owner="2" />
				\t\t\t</tile>
				\t\t</row>
				\t</map>
				</view>
				""".formatted(SP_NAMESPACE);
		final String serializedForm = createSerializedForm(five, TestReaderFactory.getNewWriter());
		assertAny("Multiple units", () -> assertEquals(xmlTwoLogical, serializedForm, "Logical form matches"),
				() -> assertEquals(xmlTwoAlphabetical, serializedForm, "Alphabetical form matches"),
				() -> assertEquals(SPACED_SELF_CLOSING_TAG.matcher(xmlTwoLogical).replaceAll("\"/>"), serializedForm,
						"Logical form with tags snugged matches"));
		assertEquals("""
						<view xmlns="%s" current_player="-1" current_turn="-1">
						\t<map version="2" rows="1" columns="1">
						\t</map>
						</view>
						""".formatted(SP_NAMESPACE),
				createSerializedForm(createSimpleMap(new Point(1, 1)), TestReaderFactory.getOldWriter()),
				"Shouldn't print empty not-visible tiles");
		final String emptySerializedForm = createSerializedForm(createSimpleMap(new Point(1, 1)),
				TestReaderFactory.getNewWriter());
		final String firstPossibility = """
				<view xmlns="%s" current_player="-1" current_turn="-1">
				\t<map version="2" rows="1" columns="1">
				\t</map>
				</view>
				""".formatted(SP_NAMESPACE);
		final String secondPossibility = """
				<view current_player="-1" current_turn="-1" xmlns="%s">
				\t<map columns="1" rows="1" version="2" />
				</view>
				""".formatted(SP_NAMESPACE);
		assertAny("Shouldn't print empty non-visible tiles",
				() -> assertEquals(firstPossibility, emptySerializedForm, "First empty form matches"),
				() -> assertEquals(secondPossibility, emptySerializedForm, "Second empty form matches"));
	}

	/**
	 * Test that {@link IUnit a unit's} image property is preserved through (de)serialization.
	 */
	@Test
	public void testUnitImageSerialization()
			throws SPFormatException, XMLStreamException, IOException {
		assertImageSerialization("Unit image property is preserved",
				new Unit(new PlayerImpl(5, ""), "herder", "herderName", 9));
	}

	private static Stream<Arguments> testTileSerializationThree() {
		return writers().map(Arguments::of);
	}

	/**
	 * Another test of serialization within a single tile.
	 */
	@SuppressWarnings("MagicNumber")
	@ParameterizedTest
	@MethodSource
	public void testTileSerializationThree(final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final IMutableLegacyMap six = new LegacyMap(new MapDimensionsImpl(2, 2, 2),
				new LegacyPlayerCollection(), 5);
		six.setMountainous(new Point(0, 0), true);
		six.addFixture(new Point(0, 1), new Ground(22, "basalt", false));
		six.addFixture(new Point(1, 0), new Forest("pine", false, 19));
		six.addFixture(new Point(1, 1), new AnimalImpl("beaver", false, "wild", 18));
		assertMissingProperty(createSerializedForm(six, writer), "kind", six);
	}

	private static Stream<Arguments> testSkippableSerialization() {
		return FUTURE_TAGS.stream().map(Arguments::of);
	}
	/**
	 * Test that the {@code row} tag is properly skipped when deserializing
	 */
	@Test
	public void testRowDeserialization() throws SPFormatException, XMLStreamException, IOException {
		assertEquivalentForms("Two maps, one with skippable tags, one without",
				"""
						<map rows="1" columns="1" version="2" current_player="-1" />""",
				"""
						<map rows="1" columns="1" version="2" current_player="-1"><row /></map>""",
				Warning.DIE);
	}

	/**
	 * Test that tags we intend to possibly support in the future (or
	 * include in the XML for readability, like {@code row}) are
	 * properly skipped when deserializing.
	 */
	@ParameterizedTest
	@MethodSource
	public void testSkippableSerialization(final String tag)
			throws SPFormatException, XMLStreamException, IOException {
		assertEquivalentForms("Two maps, one with skippable tags, one without",
				"""
						<map rows="1" columns="1" version="2" current_player="-1" />""",
				"""
						<map rows="1" columns="1" version="2" current_player="-1"><%s /></map>""".formatted(tag),
				Warning.IGNORE);
		this.<ILegacyMap>assertUnsupportedTag("""
						<map rows="1" columns="1" version="2" current_player="-1"><%s /></map>""".formatted(tag),
				tag, new LegacyMap(new MapDimensionsImpl(1, 1, 2), new LegacyPlayerCollection(), 0));
		final IMutableLegacyMap expected =
				new LegacyMap(new MapDimensionsImpl(1, 1, 2), new LegacyPlayerCollection(), 0);
		expected.setBaseTerrain(new Point(0, 0), TileType.Steppe);
		this.<ILegacyMap>assertUnsupportedTag("""
					<map rows="1" columns="1" version="2" current_player="-1">
					<tile row="0" column="0" kind="steppe"><%s /></tile></map>""".formatted(tag),
				tag, expected);
	}

	/**
	 * Test that a complex map is properly (de)serialized.
	 */
	@Test
	public void testMapSerialization()
			throws SPFormatException, XMLStreamException, IOException {
		this.<ILegacyMap>assertUnwantedChild("""
				<map rows="1" columns="1" version="2"><hill /></map>""", null);
		final MutablePlayer player = new PlayerImpl(1, "playerOne");
		player.setCurrent(true);
		final IMutableLegacyMap firstMap = new LegacyMap(new MapDimensionsImpl(1, 1, 2),
				new LegacyPlayerCollection(), 0);
		firstMap.addPlayer(player);
		final Point loc = new Point(0, 0);
		firstMap.setBaseTerrain(loc, TileType.Plains);
		assertSerialization("Simple Map serialization", firstMap);
		this.<ILegacyMap>assertMissingProperty("""
				<map version="2" columns="1" />""", "rows", null);
		this.<ILegacyMap>assertMissingProperty("""
				<map version="2" rows="1" />""", "columns", null);
		final String originalFormOne = createSerializedForm(firstMap, TestReaderFactory.getOldWriter());
		final String originalFormTwo = createSerializedForm(firstMap, TestReaderFactory.getNewWriter());
		firstMap.setBaseTerrain(new Point(1, 1), null);
		assertEquals(originalFormOne, createSerializedForm(firstMap, TestReaderFactory.getOldWriter()),
				"Explicitly not visible tile is not serialized");
		assertEquals(originalFormTwo, createSerializedForm(firstMap, TestReaderFactory.getNewWriter()),
				"Explicitly not visible tile is not serialized");
		firstMap.setMountainous(loc, true);
		assertSerialization("Map with a mountainous point", firstMap);
		this.<ILegacyMap>assertMissingProperty("""
						<view current_turn="0"><map version="2" rows="1" columns="1" /></view>""",
				"current_player", new LegacyMap(new MapDimensionsImpl(1, 1, 2),
						new LegacyPlayerCollection(), 0));
		this.<ILegacyMap>assertMissingProperty("""
						<view current_player="0"><map version="2" rows="1" columns="1"></view>""",
				"current_turn", null);
		this.<ILegacyMap>assertMissingChild("""
				<view current_player="1" current_turn="0" />""");
		this.<ILegacyMap>assertMissingChild("""
				<view current_player="1" current_turn="13" />""");
		this.<ILegacyMap>assertUnwantedChild("""
				<view current_player="0" current_turn="0">
				<map version="2" rows="1" columns="1" />
				<map version="2" rows="1" columns="1" />
				</view>""", null);
		this.<ILegacyMap>assertUnwantedChild("""
				<view current_player="0" current_turn="0"><hill /></view>""", null);
		assertMapDeserialization("Proper deserialization of map without view tag", firstMap,
				"""
						<map version="2" rows="1" columns="1" current_player="1">
						<player number="1" code_name="playerOne" />
						<row index="0"><tile row="0" column="0" kind="plains"><mountain /></tile></row></map>""");
	}

	/**
	 * Test that deserialization handles XML namespaces properly.
	 */
	@Test
	public void testNamespacedSerialization()
			throws SPFormatException, XMLStreamException, IOException {
		final MutablePlayer player = new PlayerImpl(1, "playerOne");
		player.setCurrent(true);
		final IMutableLegacyMap firstMap =
				new LegacyMap(new MapDimensionsImpl(1, 1, 2), new LegacyPlayerCollection(), 0);
		firstMap.addPlayer(player);
		final Point loc = new Point(0, 0);
		firstMap.setBaseTerrain(loc, TileType.Steppe);
		assertMapDeserialization("Proper deserialization of namespaced map", firstMap,
				"""
						<map xmlns="%s" version="2" rows="1" columns="1" current_player="1">
						<player number="1" code_name="playerOne" />
						<row index="0"><tile row="0" column="0" kind="steppe" /></row></map>"""
						.formatted(SP_NAMESPACE));
		assertMapDeserialization(
				"Proper deserialization of map if another namespace is declared default",
				firstMap, """
						<sp:map xmlns="xyzzy" xmlns:sp="%s" version="2" rows="1" columns="1" current_player="1">
						<sp:player number="1" code_name="playerOne" /><sp:row index="0">
						<sp:tile row="0" column="0" kind="steppe" /></sp:row></sp:map>""".formatted(SP_NAMESPACE));
		assertMapDeserialization("Non-root other-namespace tags ignored", firstMap,
				"""
						<map xmlns="%s" version="2" rows="1" columns="1" current_player="1" xmlns:xy="xyzzy">
						<player number="1" code_name="playerOne" /><xy:xyzzy><row index="0">
						<tile row="0" column="0" kind="steppe"><xy:hill id="0" /></tile></row></xy:xyzzy></map>"""
						.formatted(SP_NAMESPACE));
		final Consumer<Exception> adventureAssertion =
				except -> assertAny("Exception is of expected type: was %s".formatted(except.getClass().getName()),
						() -> assertInstanceOf(UnwantedChildException.class, except),
						() -> assertInstanceOf(XMLStreamException.class, except)
				);
		for (final ISPReader reader : spReaders) {
			TestXMLIO.<ILegacyMap, Exception>assertFormatIssue(reader,
					"""
							<map xmlns="xyzzy" version="2" rows="1" columns="1" current_player="1">
							<player number="1" code_name="playerOne" /><row index="0">
							<tile row="0" column="0" kind="steppe" /></row></map>""", null,
					Exception.class, (except) -> {
						switch (except) {
							case final UnwantedChildException uce -> {
								assertEquals("root", uce.getTag().getLocalPart(),
										"'Tag' with the unexpected child was what we expected");
								assertEquals(new QName("xyzzy", "map"),
										((UnwantedChildException) except).getChild(),
										"Unwanted child was the one we expected");
							}
							case final XMLStreamException xmlStreamException -> assertEquals(
									"XML stream didn't contain a start element",
									except.getMessage(), "Exception message matches");
							default -> fail("Unexpected exception type");
						}
					});
			TestXMLIO.<AdventureFixture, Exception>assertFormatIssue(reader, """
							<adventure xmlns="xyzzy" id="1" brief="one" full="two" />""", null,
					Exception.class,
					adventureAssertion);
		}
	}

	/**
	 * Test that duplicate IDs are warned about.
	 */
	@Test
	public void testDuplicateID()
			throws SPFormatException, XMLStreamException, IOException {
		final IMutableLegacyMap expected =
				new LegacyMap(new MapDimensionsImpl(1, 1, 2), new LegacyPlayerCollection(), 0);
		final Point point = new Point(0, 0);
		expected.setBaseTerrain(point, TileType.Steppe);
		final MutablePlayer player = new PlayerImpl(1, "playerOne");
		player.setCurrent(true);
		expected.addPlayer(player);
		expected.addFixture(point, new Hill(1));
		expected.addFixture(point, new Ogre(1));
		assertDuplicateID("""
				<map version="2" rows="1" columns="1" current_player="1">
				<player number="1" code_name="playerOne" />
				<row index="0"><tile row="0" column="0" kind="steppe"><hill id="1" />
				<ogre id="1" /></tile></row></map>""", expected);
	}

	/**
	 * Test that the XML-reading code properly rejects several invalid constructs.
	 */
	@Test
	public void testRejectsInvalid()
			throws SPFormatException, XMLStreamException, IOException {
		assertInvalid("""
				<map version="2" rows="1" columns="1" current_player="1">""");
		assertInvalid("""
				<map version="2" rows="1" columns="1" current_player="1"><></map>""");
	}

	private static Stream<Arguments> testGroveSerialization() {
		return TREE_TYPES.stream().flatMap(a -> integers(2).flatMap(b ->
				bools().flatMap(c -> bools().map(d ->
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
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("Test of Grove serialization", new Grove(fruit, cultivated, trees, id));
		this.<Grove>assertUnwantedChild("""
				<grove wild="true" kind="kind"><troll /></grove>""", null);
		this.<Grove>assertMissingProperty("<grove />", "cultivated", null);
		this.<Grove>assertMissingProperty("""
				<grove wild="false" />""", "kind", null);
		assertDeprecatedProperty("""
						<grove cultivated="true" tree="tree" id="0" />""",
				"tree", "kind", "grove", new Grove(false, true, "tree", 0));
		assertMissingProperty("""
						<grove cultivated="true" kind="kind" />""", "id",
				new Grove(false, true, "kind", 0));
		assertDeprecatedProperty("""
						<grove wild="true" kind="tree" id="0" />""",
				"wild", "cultivated", "grove", new Grove(false, false, "tree", 0));
		assertEquivalentForms("Assert that wild is the inverse of cultivated",
				"""
						<grove wild="true" kind="tree" id="0" />""",
				"""
						<grove cultivated="false" kind="tree" id="0" />""", Warning.IGNORE);
		assertImageSerialization("Grove image property is preserved",
				new Grove(false, false, trees, id));
		assertSerialization("Groves can have 'count' property", new Grove(true, true, trees, id, 4));
	}

	private static Stream<Arguments> testMeadowSerialization() {
		return integers(2).flatMap(a ->
				Stream.of(FieldStatus.values()).flatMap(b ->
						FIELD_TYPES.stream().collect(toShuffledStream(2)).flatMap(c ->
								bools().flatMap(d ->
										bools().map(e ->
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
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("Test of Meadow serialization",
				new Meadow(kind, field, cultivated, id, status));
		this.<Meadow>assertUnwantedChild("""
				<meadow kind="flax" cultivated="false"><troll /></meadow>""", null);
		this.<Meadow>assertMissingProperty("""
				<meadow cultivated="false" />""", "kind", null);
		this.<Meadow>assertMissingProperty("""
				<meadow kind="flax" />""", "cultivated", null);
		assertMissingProperty("""
						<field kind="kind" cultivated="true" />""", "id",
				new Meadow("kind", true, true, 0, FieldStatus.random(0)));
		assertMissingProperty("""
						<field kind="kind" cultivated="true" id="0" />""",
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
		return integers(2).flatMap(a ->
				MINERALS.stream().collect(toShuffledStream(2)).flatMap(b ->
						Stream.of(TownStatus.values()).flatMap(c ->
								writers().map(d ->
										Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test proper {@link Mine} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testMineSerialization(final int id, final String kind, final TownStatus status,
	                                  final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final Mine mine = new Mine(kind, status, id);
		assertSerialization("Test of Mine serialization", mine);
		assertDeprecatedProperty(
				KIND_EQUALS_PATTERN.matcher(createSerializedForm(mine, writer)).replaceAll("product="),
				"product", "kind", "mine", mine);
		this.<Mine>assertUnwantedChild("""
				<mine kind="%s" status="%s"><troll /></mine>""".formatted(kind, status), null);
		this.<Mine>assertMissingProperty("""
						<mine status="%s" />""".formatted(status),
				"kind", null);
		this.<Mine>assertMissingProperty("""
				<mine kind="%s" />""".formatted(kind), "status", null);
		assertMissingProperty("""
						<mine kind="%s" status="%s" />""".formatted(kind, status), "id",
				new Mine(kind, status, 0));
		assertImageSerialization("Mine image property is preserved", mine);
	}

	private static Stream<Arguments> testShrubSerialization() {
		return integers(2).flatMap(a ->
				FIELD_TYPES.stream().collect(toShuffledStream(2)).flatMap(b ->
						writers().map(c -> Arguments.of(a, b, c))));
	}

	/**
	 * Test proper {@link Shrub} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testShrubSerialization(final int id, final String kind, final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final Shrub shrub = new Shrub(kind, id);
		assertSerialization("First test of Shrub serialization", shrub);
		assertDeprecatedProperty(createSerializedForm(shrub, writer).replace("kind", "shrub"),
				"shrub", "kind", "shrub", shrub);
		this.<Shrub>assertUnwantedChild("""
				<shrub kind="%s"><troll /></shrub>""".formatted(kind), null);
		this.<Shrub>assertMissingProperty("<shrub />", "kind", null);
		assertMissingProperty("""
				<shrub kind="%s" />""".formatted(kind), "id", new Shrub(kind, 0));
		assertImageSerialization("Shrub image property is preserved", shrub);
		assertSerialization("Shrub can have 'count' property", new Shrub(kind, id, 3));
	}

	private static Stream<Arguments> testTextSerialization() {
		return integers(2).flatMap(a ->
				FIELD_TYPES.stream().collect(toShuffledStream(2)).map(b ->
						Arguments.of(a, b)));
	}

	/**
	 * Test proper {@link TextFixture} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testTextSerialization(final int baseTurn, final String text)
			throws SPFormatException, XMLStreamException, IOException {
		final int turn = baseTurn - 2; // Make sure negative turns occasionally get checked.
		final HasMutableImage testee = new TextFixture(text, turn);
		assertSerialization("Test of TextFixture serialization", testee);
		this.<TextFixture>assertUnwantedChild("""
				<text turn="%d"><troll></text>""".formatted(turn), null);
		assertImageSerialization("Text image property is preserved", testee);
		final IMutableLegacyMap wrapper = createSimpleMap(new Point(1, 1),
				Pair.with(new Point(0, 0), TileType.Plains));
		wrapper.addFixture(new Point(0, 0), new TextFixture(text, -1));
		wrapper.setCurrentTurn(0);
		assertForwardDeserializationEquality("Deprecated text-in-map still works",
				"""
						<map version="2" rows="1" columns="1" current_player="-1">
						<tile row="0" column="0" kind="plains">%s</tile></map>""".formatted(text),
				wrapper);
	}

	/**
	 * Test that {@link IUnit unit} deserialization requires certain properties to be present.
	 */
	@Test
	public void testUnitHasRequiredProperties()
			throws SPFormatException, XMLStreamException, IOException {
		this.<IUnit>assertMissingProperty("""
						<unit name="name" />""", "owner",
				new Unit(new PlayerImpl(-1, ""), "", "name", 0));
		this.<IUnit>assertMissingProperty("""
						<unit owner="1" name="name" id="0" />""", "kind",
				new Unit(new PlayerImpl(1, ""), "", "name", 0));
		this.<IUnit>assertMissingProperty("""
						<unit owner="1" kind="" name="name" id="0" />""",
				"kind", new Unit(new PlayerImpl(1, ""), "", "name", 0));
	}

	private static Stream<Arguments> testUnitWarnings() {
		return writers().flatMap(a ->
				integers(2).flatMap(b ->
						TREE_TYPES.stream().collect(toShuffledStream(2)).flatMap(c ->
								FIELD_TYPES.stream().collect(toShuffledStream(2)).map(d ->
										Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test that {@link IUnit unit} deserialization warns about various
	 * deprecated idioms and objects to certain other disallowed idioms.
	 */
	@ParameterizedTest
	@MethodSource
	public void testUnitWarnings(final SPWriter writer, final int id, final String name, final String kind)
			throws SPFormatException, XMLStreamException, IOException {
		// TODO: should probably test spaces in name and kind
		this.<IUnit>assertUnwantedChild("<unit><unit /></unit>", null);
		final IUnit firstUnit = new Unit(new PlayerImpl(1, ""), kind, name, id);
		assertDeprecatedProperty(createSerializedForm(firstUnit, writer).replace("kind", "type"),
				"type", "kind", "unit", firstUnit);
		this.<IUnit>assertMissingProperty("""
						<unit owner="2" kind="unit" />""", "name",
				new Unit(new PlayerImpl(2, ""), "unit", "", 0)); // TODO: use provided kind
		assertSerialization("Deserialize unit with no kind properly",
				new Unit(new PlayerImpl(2, ""), "", name, 2), Warning.IGNORE);
		assertMissingProperty("""
						<unit kind="kind" name="unitThree" id="3" />""", "owner",
				new Unit(new PlayerImpl(-1, ""), "kind", "unitThree", 3));
		final IUnit fourthUnit = new Unit(new PlayerImpl(4, ""), kind, "", id);
		assertMissingProperty(createSerializedForm(fourthUnit, writer), "name",
				fourthUnit);
		assertMissingProperty("""
						<unit owner="4" kind="%s" name="" id="%d" />""".formatted(kind, id), "name",
				fourthUnit);
		this.<IUnit>assertMissingProperty("""
						<unit owner="1" kind="%s" name="%s" />""".formatted(kind, name), "id",
				new Unit(new PlayerImpl(1, ""), kind, name, 0));
	}

	/**
	 * Test (de)serialization of {@link UnitMember members} of {@link IUnit units}.
	 */
	@SuppressWarnings("MagicNumber")
	@Test
	public void testUnitMemberSerialization()
			throws SPFormatException, XMLStreamException, IOException {
		final IMutableUnit firstUnit = new Unit(new PlayerImpl(1, ""), "unitType", "unitName", 1);
		firstUnit.addMember(new AnimalImpl("animal", true, "wild", 2));
		assertSerialization("Unit can have an animal as a member", firstUnit);
		firstUnit.addMember(new Worker("worker", "human", 3));
		assertSerialization("Unit can have a worker as a member", firstUnit);
		firstUnit.addMember(
				new Worker("second", "elf", 4, new Job("job", 0, new Skill("skill", 1, 2))));
		assertSerialization("Worker can have jobs", firstUnit);
		assertForwardDeserializationEquality("Explicit specification of default race works",
				"""
						<worker name="third" race="human" id="5" />""",
				new Worker("third", "human", 5));
		assertForwardDeserializationEquality("Implicit default race also works",
				"""
						<worker name="fourth" id="6" />""",
				new Worker("fourth", "human", 6));
		final Worker secondWorker = new Worker("sixth", "dwarf", 9);
		secondWorker.setStats(new WorkerStats(0, 0, 1, 2, 3, 4, 5, 6));
		assertSerialization("Worker can have stats", secondWorker);
		assertImageSerialization("Worker image property is preserved", secondWorker);
		secondWorker.addJob(new Job("seventh", 1));
		assertSerialization("Worker can have Job with no skills yet", secondWorker);
		secondWorker.setMount(new AnimalImpl("animal", false, "tame", 11));
		assertSerialization("Worker can have a mount", secondWorker);
		secondWorker.addEquipment(new Implement("implKind", 12));
		secondWorker.addEquipment(new Implement("implKindTwo", 13));
		assertSerialization("Worker can have equipment", secondWorker);
		this.<ILegacyMap>assertUnwantedChild("""
				<map version="2" rows="1" columns="1">
				<tile row="0" column="0" kind="plains">
				<worker name="name" id="1" /></tile></map>""", null);
		assertPortraitSerialization("Worker portrait property is preserved", secondWorker);
		secondWorker.setNote(new PlayerImpl(1, ""), "sample notes");
		assertNotesSerialization("Worker notes property is preserved", secondWorker);
	}

	/**
	 * Test (de)serialization of {@link IUnit unit} orders.
	 */
	@Test
	public void testOrdersSerialization()
			throws SPFormatException, XMLStreamException, IOException {
		final Player player = new PlayerImpl(0, "");
		final IMutableUnit firstUnit = new Unit(player, "kind of unit", "name of unit", 2);
		final IMutableUnit secondUnit = new Unit(player, "kind of unit", "name of unit", 2);
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
				"""
						<unit name="name" kind="kind" id="1" owner="-1">Orders orders</unit>""",
				(unit) -> "Orders orders".equals(unit.getOrders(-1)));
	}

	private static Stream<Arguments> testQuoting() {
		return integers(3).map(Arguments::of);
	}

	/**
	 * Test that XML metacharacters are properly quoted (i.e. don't break
	 * the reader but are properly deserialized) when they appear in text
	 * that must be serialized.
	 */
	@ParameterizedTest
	@MethodSource
	public void testQuoting(final int id)
			throws SPFormatException, XMLStreamException, IOException {
		final Player player = new PlayerImpl(0, "");
		final Unit unit = new Unit(player, "kind of unit", "name of unit", id);
		unit.setOrders(4, """
				I <3 & :( "meta'""");
		unit.setResults(5, "2 --> 1");
		// FIXME: assertSerialization() relies on equals(), which doesn't check orders IIRC
		assertSerialization(
				"Serialization preserves XML meta-characters in orders and results", unit);
		unit.setOrders(3, "1 << 2");
		unit.setResults(-1, """
				"quote this"\s""");
		assertSerialization("This works even if such characters occur more than once", unit);
		unit.setName("""
				"Can't quote this ><>&"\s""");
		assertSerialization("Data stored in XML attributes is quoted", unit);
	}

	/**
	 * Test that {@link IUnit units'} {@link HasPortrait#getPortrait
	 * portraits} are preserved in (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testQuoting")
	public void testUnitPortraitSerialization(final int id)
			throws SPFormatException, XMLStreamException, IOException {
		final HasMutablePortrait unit = new Unit(new PlayerImpl(1, ""), "kind", "name", id);
		unit.setPortrait("portraitFile");
		assertSerialization("Portrait doesn't mess up serialization", unit);
		assertSerializedFormContains(unit, "portraitFile", "Serialized form contains portrait");
		assertPortraitSerialization("Unit portrait property is preserved", unit);
	}

	private static Stream<Arguments> testAdventureSerialization() {
		return integers(2).flatMap(a ->
				integers(2).map(b -> Arguments.of(a, b)));
	}

	/**
	 * Test that {@link AdventureFixture adventure hooks} are properly (de)serialized.
	 */
	@ParameterizedTest
	@MethodSource
	public void testAdventureSerialization(final int idOne, final int idTwo)
			throws SPFormatException, XMLStreamException, IOException {
		final Player independent = new PlayerImpl(1, "independent");
		final TileFixture first = new AdventureFixture(independent, "first hook brief",
				"first hook full", idOne);
		final AdventureFixture second = new AdventureFixture(new PlayerImpl(2, "player"),
				"second hook brief", "second hook full", idTwo);
		assertNotEquals(first, second, "Two different hooks are not equal");
		final IMutableLegacyMap wrapper = createSimpleMap(new Point(1, 1),
				Pair.with(new Point(0, 0), TileType.Plains));
		wrapper.addPlayer(independent);
		wrapper.addFixture(new Point(0, 0), first);
		assertSerialization("First AdventureFixture serialization test", wrapper);
		assertSerialization("Second AdventureFixture serialization test", second);
		assertSerialization("AdventureFixture with empty descriptions",
				new AdventureFixture(new PlayerImpl(3, "third"), "", "", idOne));
		// TODO: split portals into separate test method
		final Portal third = new Portal("portal dest", new Point(1, 2), idOne);
		final TileFixture fourth = new Portal("portal dest two", new Point(2, 1), idTwo);
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
			throws SPFormatException, XMLStreamException, IOException {
		final IMutableFortress firstFort = new FortressImpl(new PlayerImpl(1, ""), "fortName", 1,
				TownSize.Small);
		firstFort.addMember(new Implement("implKind", 2));
		assertSerialization("Fortress can have an Implement as a member", firstFort);
		firstFort.addMember(new Implement("implKindTwo", 8));
		assertSerialization("Implement can be more than one in one object", firstFort);
		firstFort.addMember(new ResourcePileImpl(3, "generalKind", "specificKind",
				new LegacyQuantity(10, "each")));
		assertSerialization("Fortress can have a ResourcePile as a member", firstFort);
		final IMutableResourcePile resource = new ResourcePileImpl(4, "generalKind", "specificKind",
				new LegacyQuantity(15, "pounds"));
		resource.setCreated(5); // TODO: Provide constructor taking this field
		assertSerialization("Resource pile can know what turn it was created", resource);
		assertSerialization("Resource pile can have non-integer quantity", new ResourcePileImpl(5,
				"resourceKind", "specificKind2",
				new LegacyQuantity(new BigDecimal(3).divide(new BigDecimal(2)), "cubic feet")));
	}

	private static Stream<Arguments> testAnimalTracksSerialization() {
		return TREE_TYPES.stream().collect(toShuffledStream(2)).map(Arguments::of);
	}

	/**
	 * Test that {@link AnimalTracks animal tracks} are properly
	 * (de)serialized, including that the old now-deprecated XML idiom is
	 * still read properly.
	 */
	@ParameterizedTest
	@MethodSource
	public void testAnimalTracksSerialization(final String kind)
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("Test of animal-track serialization", new AnimalTracks(kind));
		this.<AnimalTracks>assertUnwantedChild("""
				<animal kind="tracks" traces="true"><troll /></animal>""", null);
		this.<AnimalTracks>assertMissingProperty("""
				<animal traces="true" />""", "kind", null);
		assertImageSerialization("Animal-track image property is preserved",
				new AnimalTracks(kind));
		assertEquivalentForms("Former idiom still works",
				"""
						<animal kind="kind" status="wild" traces="" />""",
				"""
						<animal kind="kind" status="wild" traces="true" />""",
				Warning.DIE);
	}

	private static Stream<Arguments> testAnimalSerialization() {
		return integers(2).flatMap(a ->
				ANIMAL_STATUSES.stream().flatMap(b ->
						TREE_TYPES.stream().collect(toShuffledStream(2)).flatMap(c ->
								bools().map(d -> Arguments.of(a, b, c, d)))));
	}

	/**
	 * Test {@link Animal} (de)serialization.
	 */
	@SuppressWarnings("MagicNumber")
	@ParameterizedTest
	@MethodSource
	public void testAnimalSerialization(final int id, final String status, final String kind, final boolean talking)
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("Test of Animal serialization",
				new AnimalImpl(kind, talking, status, id));
		this.<Animal>assertUnwantedChild("""
				<animal kind="%s"><troll /></animal>""".formatted(kind), null);
		this.<Animal>assertMissingProperty("<animal />", "kind", null);
		this.<Animal>assertForwardDeserializationEquality("Forward-looking in re talking",
				"""
						<animal kind="%s" talking="false" id="%d" />""".formatted(kind, id),
				new AnimalImpl(kind, false, "wild", id));
		this.<Animal>assertMissingProperty("""
						<animal kind="%s" talking="%b" />""".formatted(kind, talking), "id",
				new AnimalImpl(kind, talking, "wild", 0));
		this.<Animal>assertMissingProperty("""
				<animal kind="%s" id="nonNumeric" />""".formatted(kind), "id", null);
		this.<Animal>assertForwardDeserializationEquality("Explicit default status of animal",
				"""
						<animal kind="%s" status="wild" id="%d" />""".formatted(kind, id),
				new AnimalImpl(kind, false, "wild", id));
		assertImageSerialization("Animal image property is preserved",
				new AnimalImpl(kind, talking, status, id));
		this.<Animal>assertForwardDeserializationEquality("Namespaced attribute",
				"""
						<animal xmlns:sp="%s" sp:kind="%s" sp:talking="%b" sp:traces="false" sp:status="%s" \
						sp:id="%d" />"""
						.formatted(SP_NAMESPACE, kind, talking, status, id),
				new AnimalImpl(kind, talking, status, id));
		assertEquivalentForms("""
						Supports 'traces="false"'""",
				"""
						<animal kind="%s" status="%s" id="%d" />""".formatted(kind, status, id),
				"""
						<animal kind="%s" traces="false" status="%s" id="%d" />""".formatted(
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
				integers(2).map(b -> Arguments.of(a, b)));
	}

	/**
	 * Test that the former and current idioms for "immortal animals" produce equivalent results.
	 */
	@ParameterizedTest
	@MethodSource
	public void testImmortalAnimalDeserialization(final String animal, final int id)
			throws SPFormatException, XMLStreamException, IOException {
		assertEquivalentForms(animal + " as animal deserializes to immortal",
				"""
						<%s id="%d" />""".formatted(animal, id),
				"""
						<animal kind="%s" id="%d" />""".formatted(animal, id), Warning.DIE);
	}

	private static Stream<Arguments> testCacheSerialization() {
		return integers(3).flatMap(a ->
				TREE_TYPES.stream().collect(toShuffledStream(2)).flatMap(b ->
						FIELD_TYPES.stream().collect(toShuffledStream(2)).map(c ->
								Arguments.of(a, b, c))));
	}

	/**
	 * Test {@link CacheFixture} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCacheSerialization(final int id, final String kind, final String contents)
			throws SPFormatException, XMLStreamException, IOException {
		final HasMutableImage testee = new CacheFixture(kind, contents, id);
		assertSerialization("Test of Cache serialization", testee);
		this.<CacheFixture>assertUnwantedChild("""
				<cache kind="%s" contents="%s"><troll /></cache>""".formatted(kind, contents), null);
		this.<CacheFixture>assertMissingProperty("""
				<cache contents="%s" />""".formatted(contents), "kind", null);
		this.<CacheFixture>assertMissingProperty("""
				<cache kind="%s" />""".formatted(kind), "contents", null);
		assertMissingProperty("""
						<cache kind="%s" contents="%s" />""".formatted(kind, contents),
				"id", new CacheFixture(kind, contents, 0));
		assertImageSerialization("Cache image property is preserved", testee);
	}

	private static Stream<Arguments> testCentaurSerialization() {
		return integers(3).flatMap(a ->
				TREE_TYPES.stream().collect(toShuffledStream(2)).map(b ->
						Arguments.of(a, b)));
	}

	/**
	 * Test {@link Centaur} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCentaurSerialization(final int id, final String kind)
			throws SPFormatException, XMLStreamException, IOException {
		final HasMutableImage testee = new Centaur(kind, id);
		assertSerialization("Test of Centaur serialization", testee);
		this.<Centaur>assertUnwantedChild("""
				<centaur kind="%s"><troll /></centaur>""".formatted(kind), null);
		this.<Centaur>assertMissingProperty("<centaur />", "kind", null);
		assertMissingProperty("""
						<centaur kind="%s" />""".formatted(kind), "id",
				new Centaur(kind, 0));
		assertImageSerialization("Centaur image property is preserved", testee);
	}

	/**
	 * Test {@link Dragon} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCentaurSerialization")
	public void testDragonSerialization(final int id, final String kind)
			throws SPFormatException, XMLStreamException, IOException {
		final HasMutableImage testee = new Dragon(kind, id);
		assertSerialization("Test of Dragon serialization", testee);
		assertSerialization("Dragon with no kind (de-)serialization", new Dragon("", id));
		this.<Dragon>assertUnwantedChild("""
				<dragon kind="ice"><hill /></dragon>""", null);
		this.<Dragon>assertMissingProperty("<dragon />", "kind", null);
		assertMissingProperty("""
						<dragon kind="%s" />""".formatted(kind), "id",
				new Dragon(kind, 0));
		assertImageSerialization("Dragon image property is preserved", testee);
	}

	/**
	 * Test {@link Fairy} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCentaurSerialization")
	public void testFairySerialization(final int id, final String kind)
			throws SPFormatException, XMLStreamException, IOException {
		final HasMutableImage testee = new Fairy(kind, id);
		assertSerialization("Test of Fairy serialization", testee);
		this.<Fairy>assertUnwantedChild("""
						<fairy kind="%s"><hill /></fairy>""".formatted(kind),
				null);
		this.<Fairy>assertMissingProperty("<fairy />", "kind", null);
		assertMissingProperty("""
						<fairy kind="%s" />""".formatted(kind), "id",
				new Fairy(kind, 0));
		assertImageSerialization("Fairy image property is preserved", testee);
	}

	private static Stream<Arguments> testForestSerialization() {
		return integers(3).flatMap(a ->
				TREE_TYPES.stream().collect(toShuffledStream(2)).flatMap(b ->
						bools().map(c -> Arguments.of(a, b, c))));
	}

	/**
	 * Test {@link Forest} (de)serialization.
	 *
	 * TODO: Split non-randomizable parts out
	 */
	@ParameterizedTest
	@MethodSource
	public void testForestSerialization(final int id, final String kind, final boolean rows)
			throws SPFormatException, XMLStreamException, IOException {
		final HasMutableImage testee = new Forest(kind, rows, id);
		assertSerialization("Test of Forest serialization", testee);
		this.<Forest>assertUnwantedChild("""
				<forest kind="%s"><hill /></forest>""".formatted(kind), null);
		this.<Forest>assertMissingProperty("<forest />", "kind", null);
		assertImageSerialization("Forest image property is preserved", testee);
		final Point loc = new Point(0, 0);
		final IMutableLegacyMap map = createSimpleMap(new Point(1, 1), Pair.with(loc, TileType.Plains));
		map.addFixture(loc, new Forest("trees", false, 4));
		map.addFixture(loc, new Forest("secondForest", true, 5));
		assertSerialization("Map with multiple Forests on a tile", map);
		assertEquivalentForms("Duplicate Forests ignored",
				encapsulateTileString("""
						<forest kind="trees" id="4" />
						<forest kind="second" rows="true" id="5" />"""),
				encapsulateTileString("""
						<forest kind="trees" id="4" />
						<forest kind="trees" id="4" />
						<forest kind="second" rows="true" id="5" />"""),
				Warning.IGNORE);
		assertEquivalentForms("Deserialization now supports 'rows=false'",
				encapsulateTileString("""
						<forest kind="trees" id="%d" />""".formatted(id)),
				encapsulateTileString("""
						<forest kind="trees" rows="false" id="%d" />""".formatted(id)),
				Warning.IGNORE);
		assertSerialization("Forests can have acreage numbers", new Forest(kind,
				rows, id, new BigDecimal(3).divide(new BigDecimal(2))));
	}

	private static Stream<Arguments> testFortressSerialization() {
		return integers(2).flatMap(a ->
				Stream.of(TownSize.values()).map(b -> Arguments.of(a, b)));
	}

	/**
	 * Test {@link IFortress fortress} (de)serialization in the simplest cases.
	 */
	@ParameterizedTest
	@MethodSource
	public void testFortressSerialization(final int id, final TownSize size)
			throws SPFormatException, XMLStreamException, IOException {
		// Can't give player names because our test environment doesn't
		// let us pass a set of players in
		final Player firstPlayer = new PlayerImpl(1, "");
		assertSerialization("First test of %s Fortress serialization".formatted(size),
				new FortressImpl(firstPlayer, "one", id, size));
		assertSerialization("Second test of %s Fortress serialization".formatted(size), new FortressImpl(firstPlayer,
				"two", id, size));
		final Player secondPlayer = new PlayerImpl(2, "");
		final IMutableFortress five = new FortressImpl(secondPlayer, "five", id, TownSize.Small);
		five.addMember(new Unit(secondPlayer, "unitOne", "unitTwo", 1));
		assertSerialization("Fifth test of Fortress serialization", five);
		this.<IFortress>assertUnwantedChild("<fortress><hill /></fortress>", null);
		this.<IFortress>assertMissingProperty("<fortress />", "owner",
				new FortressImpl(new PlayerImpl(-1, ""), "", 0, TownSize.Small));
		this.<IFortress>assertMissingProperty("""
						<fortress owner="1" />""", "name",
				new FortressImpl(new PlayerImpl(1, ""), "", 0, TownSize.Small));
		this.<IFortress>assertMissingProperty("""
						<fortress owner="1" name="name" />""",
				"id", new FortressImpl(new PlayerImpl(1, ""), "name", 0, TownSize.Small));
		assertImageSerialization("Fortress image property is preserved", five);
	}

	/**
	 * Test {@link Giant} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCentaurSerialization")
	public void testGiantSerialization(final int id, final String kind)
			throws SPFormatException, XMLStreamException, IOException {
		final HasMutableImage testee = new Giant(kind, id);
		assertSerialization("Test of Giant serialization", testee);
		this.<Giant>assertUnwantedChild("""
				<giant kind="%s"><hill /></giant>""".formatted(kind), null);
		this.<Giant>assertMissingProperty("<giant />", "kind", null);
		assertMissingProperty("""
				<giant kind="%s" />""".formatted(kind), "id", new Giant(kind, 0));
		assertImageSerialization("Giant image property is preserved", testee);
	}

	private static Stream<Arguments> testGroundSerialization() {
		return integers(3).map(Arguments::of);
	}

	/**
	 * Test {@link Ground} (de)serialization
	 *
	 * TODO: Randomize, condense, perhaps split
	 */
	@ParameterizedTest
	@MethodSource
	public void testGroundSerialization(final int id)
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("First test of Ground serialization", new Ground(id, "one", true));
		final Point loc = new Point(0, 0);
		final IMutableLegacyMap map = createSimpleMap(new Point(1, 1), Pair.with(loc, TileType.Plains));
		map.addFixture(loc, new Ground(-1, "four", true));
		assertSerialization("Test that reader handles ground as a fixture", map);
		assertForwardDeserializationEquality("Duplicate Ground ignored", """
						<view current_turn="-1" current_player="-1">
						<map version="2" rows="1" columns="1">
						<tile row="0" column="0" kind="plains">
						<ground kind="four" exposed="true" />
						<ground kind="four" exposed="true" /></tile></map></view>""",
				map);
		map.addFixture(loc, new Ground(-1, "five", false));
		assertForwardDeserializationEquality("Exposed Ground made main", """
						<view current_turn="-1" current_player="-1">
						<map version="2" rows="1" columns="1">
						<tile row="0" column="0" kind="plains">
						<ground kind="five" exposed="false" />
						<ground kind="four" exposed="true" /></tile></map></view>""",
				map);
		assertForwardDeserializationEquality("Exposed Ground left as main", """
						<view current_turn="-1" current_player="-1">
						<map version="2" rows="1" columns="1">
						<tile row="0" column="0" kind="plains">
						<ground kind="four" exposed="true" />
						<ground kind="five" exposed="false" /></tile></map></view>""",
				map);
		this.<Ground>assertUnwantedChild("""
				<ground kind="sand" exposed="true"><hill /></ground>""", null);
		this.<Ground>assertMissingProperty("<ground />", "kind", null);
		this.<Ground>assertMissingProperty("""
				<ground kind="ground" />""", "exposed", null);
		assertDeprecatedProperty("""
						<ground ground="ground" exposed="true" />""", "ground", "kind", "ground",
				new Ground(-1, "ground", true));
		assertImageSerialization("Ground image property is preserved",
				new Ground(id, "five", true));
	}

	private static Stream<Arguments> testSimpleSerializationNoChildren() {
		return Stream.of("djinn", "griffin", "hill", "minotaur", "oasis", "ogre", "phoenix", "simurgh",
				"sphinx", "troll").map(Arguments::of);
	}
	/**
	 * Test that the code reading various fixtures whose only properties
	 * are ID and image properly objects when the XML tries to give them
	 * child tags.
	 */
	@ParameterizedTest
	@MethodSource
	public void testSimpleSerializationNoChildren(String tag)
			throws SPFormatException, XMLStreamException, IOException {
		final String inner;
		if ("troll".equals(tag)) {
			inner = "oasis";
		} else {
			inner = "troll";
		}
		this.<SimpleImmortal>assertUnwantedChild("<%s><%s /></%s>".formatted(tag, inner, tag), null);
	}

	private static Stream<Arguments> testSimpleImageSerialization() {
		return Stream.<IntFunction<? extends HasMutableImage>>of(Sphinx::new, Djinn::new,
				Griffin::new, Minotaur::new, Ogre::new, Phoenix::new, Simurgh::new,
				Troll::new, Hill::new, Oasis::new).flatMap(a ->
				integers(3).map(b -> Arguments.of(a, b)));
	}

	/**
	 * Test that various fixtures whose only properties are ID and image
	 * have their image property properly (de)serialized.
	 */
	@ParameterizedTest
	@MethodSource
	public void testSimpleImageSerialization(final IntFunction<? extends HasMutableImage> constructor,
	                                         final int id)
			throws SPFormatException, XMLStreamException, IOException {
		final HasMutableImage item = constructor.apply(id);
		assertImageSerialization("Image property is preserved", item);
	}

	/**
	 * Test that various fixtures whose only properties are ID and image are properly (de)serialized.
	 */
	@ParameterizedTest
	@MethodSource("testSimpleImageSerialization")
	public void testSimpleSerialization(final IntFunction<? extends HasImage> constructor, final int id)
			throws SPFormatException, XMLStreamException, IOException {
		final HasImage item = constructor.apply(id);
		switch (item) {
			case final HasKind hk -> {
				assertSerialization(hk.getKind() + " serialization", item);
				assertMissingProperty("<%s />".formatted(hk.getKind()), "id", (HasKind) constructor.apply(0));
			}
			case final Hill hill -> {
				assertSerialization("Hill serialization", item);
				assertMissingProperty("<hill />", "id", new Hill(0));
			}
			case final Oasis oasis -> {
				assertSerialization("Hill serialization", item);
				assertMissingProperty("<oasis />", "id", new Oasis(0));
			}
			default -> fail("Unhandled type");
		}
	}

	private static Stream<Arguments> testCaveSerialization() {
		return integers(2).flatMap(a ->
				integers(2).map(b -> Arguments.of(a, b)));
	}


	/**
	 * Test {@link Cave} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testCaveSerialization(final int dc, final int id)
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("Cave serialization test", new Cave(dc, id));
		this.<Cave>assertUnwantedChild("""
				<cave dc="%d"><troll /></cave>""".formatted(dc), null);
		this.<Cave>assertMissingProperty("<cave />", "dc", null);
		assertMissingProperty("""
						<cave dc="%d" />""".formatted(dc), "id",
				new Cave(dc, 0));
		assertImageSerialization("Cave image property is preserved", new Cave(dc, id));
	}

	private static Stream<Arguments> testMineralSerialization() {
		return integers(2).flatMap(a ->
				integers(2).flatMap(b ->
						MINERALS.stream().flatMap(c ->
								bools().flatMap(d ->
										writers().map(e ->
												Arguments.of(a, b, c, d, e))))));
	}

	/**
	 * Test {@link MineralVein} (de)serialization.
	 */
	@ParameterizedTest
	@MethodSource
	public void testMineralSerialization(final int dc, final int id, final String kind, final boolean exposed,
	                                     final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final MineralVein secondVein = new MineralVein(kind, exposed, dc, id);
		assertSerialization("MineralVein serialization", secondVein);
		assertDeprecatedProperty(
				createSerializedForm(secondVein, writer).replace("kind", "mineral"),
				"mineral", "kind", "mineral", secondVein);
		this.<MineralVein>assertUnwantedChild("""
						<mineral kind="%s" exposed="%b" dc="%d"><hill /></mineral>""".formatted(kind, exposed, dc),
				null);
		this.<MineralVein>assertMissingProperty("""
						<mineral dc="%d" exposed="%b" />""".formatted(dc, exposed),
				"kind", null);
		this.<MineralVein>assertMissingProperty("""
				<mineral kind="%s" exposed="%b" />""".formatted(kind, exposed), "dc", null);
		this.<MineralVein>assertMissingProperty("""
						<mineral dc="%d" kind="%s" />""".formatted(dc, kind),
				"exposed", null);
		assertMissingProperty("""
						<mineral kind="%s" exposed="%b" dc="%d" />""".formatted(kind, exposed, dc),
				"id", new MineralVein(kind, exposed, dc, 0));
		assertImageSerialization("Mineral image property is preserved", secondVein);
	}

	/**
	 * Test {@link Battlefield} serialization.
	 */
	@ParameterizedTest
	@MethodSource("testCaveSerialization")
	public void testBattlefieldSerialization(final int dc, final int id)
			throws SPFormatException, XMLStreamException, IOException {
		assertSerialization("Battlefield serialization test", new Battlefield(dc, id));
		this.<Battlefield>assertUnwantedChild("""
				<battlefield dc="%d"><hill /></battlefield>""".formatted(dc), null);
		this.<Battlefield>assertMissingProperty("<battlefield />", "dc", null);
		assertMissingProperty("""
				<battlefield dc="%d" />""".formatted(dc), "id", new Battlefield(dc, 0));
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
			throws SPFormatException, XMLStreamException, IOException {
		assertEquivalentForms("ID numbers can contain commas", """
				<hill id="1,002" />""", """
				<hill id="1002" />""", Warning.DIE);
	}

	/**
	 * Test that the old, now removed, "sandbar" tag produces only a warning if present in XML.
	 */
	@Test
	public void testOldSandbars()
			throws SPFormatException, XMLStreamException, IOException {
		assertUnsupportedTag("""
						<view current_player="-1" current_turn="-1">
						<map version="2" rows="1" columns="1">
						<tile row="0" column="0" kind="plains">
						<sandbar id="0" /></tile></map></view>""", "sandbar",
				createSimpleMap(new Point(1, 1), Pair.with(new Point(0, 0), TileType.Plains)));
	}

	private static Stream<Arguments> testElsewhere() {
		return integers(2).map(Arguments::of);
	}

	/**
	 * Test that maps can store units (or other fixtures) with a location of "elsewhere".
	 */
	@ParameterizedTest
	@MethodSource
	public void testElsewhere(final int id)
			throws SPFormatException, XMLStreamException, IOException {
		final IMutableLegacyMap map = createSimpleMap(new Point(1, 1));
		map.addFixture(Point.INVALID_POINT, new Ogre(id));
		assertSerialization(
				"Map with fixture 'elsewhere' should be properly serialized", map);
	}

	private static Stream<Arguments> testBookmarkSerialization() {
		return MAP_READERS.stream().flatMap(a -> writers().map(b ->
				Arguments.of(a, b)));
	}

	/**
	 * Test serialization of players' bookmarks.
	 */
	@ParameterizedTest
	@MethodSource
	public void testBookmarkSerialization(final IMapReader reader, final SPWriter writer)
			throws SPFormatException, XMLStreamException, IOException {
		final IMutableLegacyMap map = new LegacyMap(new MapDimensionsImpl(1, 1, 2), new LegacyPlayerCollection(), 1);
		final Player player = map.getPlayers().getPlayer(1);
		map.setCurrentPlayer(player);
		assertFalse(map.getBookmarks().contains(new Point(0, 0)),
				"Map by default doesn't have a bookmark");
		assertTrue(map.getBookmarks().isEmpty(), "Map by default has no bookmarks");
		map.setBaseTerrain(new Point(0, 0), TileType.Plains);
		map.addBookmark(new Point(0, 0));
		final ILegacyMap deserialized;
		try (final StringReader stringReader =
				     new StringReader(createSerializedForm(map, writer))) {
			deserialized = reader.readMapFromStream(FAKE_FILENAME, stringReader, Warning.DIE);
		}
		assertNotSame(map, deserialized, "Deserialization doesn't just return the input");
		assertTrue(deserialized.getBookmarks().contains(new Point(0, 0)),
				"Deserialized map has the bookmark we saved");
	}

	private static Stream<Arguments> testRoadSerialization() {
		return Stream.of(Direction.values()).flatMap(a ->
				Stream.of(Direction.values()).map(b ->
						Arguments.of(a, SINGLETON_RANDOM.nextInt(8), b,
								SINGLETON_RANDOM.nextInt(8))));
	}

	/**
	 * Test serialization of roads.
	 */
	@ParameterizedTest
	@MethodSource
	public void testRoadSerialization(final Direction directionOne, final int qualityOne, final Direction directionTwo,
	                                  final int qualityTwo)
			throws SPFormatException, XMLStreamException, IOException {
		assumeFalse(directionOne == directionTwo, "We can't have the same direction twice");
		assumeTrue(qualityOne >= 0, "Road quality can't be negative");
		assumeTrue(qualityTwo >= 0, "Road quality can't be negative");
		final IMutableLegacyMap map = createSimpleMap(new Point(1, 1),
				Pair.with(new Point(0, 0), TileType.Plains));
		if (Direction.Nowhere != directionOne) {
			map.setRoadLevel(new Point(0, 0), directionOne, qualityOne);
		}
		if (Direction.Nowhere != directionTwo) {
			map.setRoadLevel(new Point(0, 0), directionTwo, qualityTwo);
		}
		assertSerialization("Map with roads is serialized properly.", map, Warning.DIE);
	}

	/**
	 * Test that the in-tile-tag and pseudo-fixture representations of
	 * mountains are both read properly.
	 */
	@Test
	public void testMountainSerialization() throws SPFormatException, XMLStreamException,
			IOException {
		assertEquivalentForms("Old and new representation of mountains",
				encapsulateTileString("<mountain />"), """
						<map version="2" rows="2" columns="2">
						<tile row="1" column="1" kind="plains" mountain="true" />
						</map>""",
				Warning.IGNORE); // missing current_player
		// TODO: Verify that they actually set the mountain in the map,
		// and it is not set by default
	}
}

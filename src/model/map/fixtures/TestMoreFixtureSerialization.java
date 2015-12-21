package model.map.fixtures;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import model.map.BaseTestFixtureSerialization;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.PointFactory;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Portal;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import org.junit.Test;
import util.NullCleaner;
import util.Warning;
import util.Warning.Action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Another class to test serialization of TileFixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TestMoreFixtureSerialization extends
		BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String OWNER_PROPERTY = "owner";
	/**
	 * The default race for a worker.
	 */
	private static final String DEFAULT_RACE = "human";
	/**
	 * Extracted constant.
	 */
	private static final String NAME_PROPERTY = "name";
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Extracted constant.
	 */
	private static final String STATUS_PROPERTY = "status";
	private static final Pattern KIND_PATTERN =
			Pattern.compile(KIND_PROPERTY, Pattern.LITERAL);

	/**
	 * Test serialization of Groves.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testGroveSerialization() throws XMLStreamException,
			                                            SPFormatException, IOException {
		assertSerialization("First test of Grove serialization, reflection",
				new Grove(true, true, "firstGrove", 1), Grove.class);
		assertSerialization("Second test of Grove serialization, reflection",
				new Grove(true, false, "secondGrove", 2), Grove.class);
		assertSerialization("Third test of Grove serialization, reflection",
				new Grove(false, true, "thirdGrove", 3), Grove.class);
		assertSerialization("Fourth test of Grove serialization, reflection",
				new Grove(false, false, "four", 4), Grove.class);
		assertUnwantedChild(
				"<grove wild=\"true\" kind=\"kind\"><troll /></grove>",
				Grove.class, false);
		assertMissingProperty("<grove />", Grove.class, "cultivated", false);
		assertMissingProperty("<grove wild=\"false\" />", Grove.class, "kind",
				false);
		assertDeprecatedProperty(
				"<grove cultivated=\"true\" tree=\"tree\" id=\"0\" />",
				Grove.class, "tree", true);
		assertMissingProperty("<grove cultivated=\"true\" kind=\"kind\" />",
				Grove.class, "id", true);
		assertDeprecatedProperty(
				"<grove wild=\"true\" kind=\"tree\" id=\"0\" />", Grove.class,
				"wild", true);
		assertEquivalentForms(
				"Assert that wild is the inverse of cultivated",
				"<grove wild=\"true\" kind=\"tree\" id=\"0\" />",
				"<grove cultivated=\"false\" kind=\"tree\" id=\"0\" />",
				Grove.class, Action.Ignore);
		assertImageSerialization("Grove image property is preserved",
				new Grove(false, false, "five", 5), Grove.class);
	}

	/**
	 * Test serialization of Meadows, including error-checking.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMeadowSerialization() throws XMLStreamException,
			                                             SPFormatException, IOException {
		assertSerialization("First test of Meadow serialization, reflection",
				new Meadow("firstMeadow", true, true, 1, FieldStatus.Fallow),
				Meadow.class);
		assertSerialization(
				"Second test of Meadow serialization, reflection",
				new Meadow("secondMeadow", true, false, 2, FieldStatus.Seeding),
				Meadow.class);
		assertSerialization("Third test of Meadow serialization, reflection",
				new Meadow("three", false, true, 3, FieldStatus.Growing),
				Meadow.class);
		assertSerialization("Fourth test of Meadow serialization, reflection",
				new Meadow("four", false, false, 4, FieldStatus.Bearing),
				Meadow.class);
		assertUnwantedChild(
				"<meadow kind=\"flax\" cultivated=\"false\"><troll /></meadow>",
				Meadow.class, false);
		assertMissingProperty("<meadow cultivated=\"false\" />", Meadow.class,
				KIND_PROPERTY, false);
		assertMissingProperty("<meadow kind=\"flax\" />", Meadow.class,
				"cultivated", false);
		assertMissingProperty("<field kind=\"kind\" cultivated=\"true\" />",
				Meadow.class, "id", true);
		assertMissingProperty(
				"<field kind=\"kind\" cultivated=\"true\" id=\"0\" />",
				Meadow.class, "status", true);
		assertImageSerialization("Meadow image property is preserved",
				new Meadow("five", false, false, 5, FieldStatus.Fallow),
				Meadow.class);
	}

	/**
	 * Test serialization of Mines.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMineSerialization() throws XMLStreamException,
			                                           SPFormatException, IOException {
		assertSerialization("First test of Mine serialization", new Mine("one",
				                                                                TownStatus.Active,
				                                                                1),
				Mine.class);
		assertSerialization("Second test of Mine serialization", new Mine(
				                                                                 "two",
				                                                                 TownStatus.Abandoned,
				                                                                 2),
				Mine.class);
		assertSerialization("Third test of Mine serialization", new Mine(
				                                                                "three",
				                                                                TownStatus.Burned,
				                                                                3),
				Mine.class);
		final Mine fourthMine = new Mine("four", TownStatus.Ruined, 4);
		assertSerialization("Fourth test of Mine serialization", fourthMine,
				Mine.class);
		final String oldKindProperty = "product"; // NOPMD
		assertDeprecatedDeserialization("Deprecated Mine idiom", fourthMine,
				NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(fourthMine, true))
								.replaceAll(
										Matcher.quoteReplacement(oldKindProperty))),
				Mine.class,
				oldKindProperty);
		assertDeprecatedDeserialization("Deprecated Mine idiom", fourthMine,
				NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(fourthMine, false))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				Mine.class,
				oldKindProperty);
		assertUnwantedChild(
				"<mine kind=\"gold\" status=\"active\"><troll /></mine>",
				Mine.class, false);
		assertMissingProperty("<mine status=\"active\"/>", Mine.class,
				KIND_PROPERTY, false);
		assertMissingProperty("<mine kind=\"gold\"/>", Mine.class,
				STATUS_PROPERTY, false);
		assertMissingProperty("<mine kind=\"kind\" status=\"active\" />",
				Mine.class, "id", true);
		assertImageSerialization("Mine image property is preserved", fourthMine,
				Mine.class);
	}

	/**
	 * Test serialization of Shrubs.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testShrubSerialization() throws XMLStreamException,
			                                            SPFormatException, IOException {
		assertSerialization("First test of Shrub serialization", new Shrub(
				                                                                  "one",
				                                                                  1),
				Shrub.class);
		final Shrub secondShrub = new Shrub("two", 2);
		assertSerialization("Second test of Shrub serialization", secondShrub,
				Shrub.class);
		final String oldKindProperty = "shrub"; // NOPMD
		assertDeprecatedDeserialization("Deserialization of mangled shrub",
				secondShrub, NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(secondShrub, true))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				Shrub.class,
				oldKindProperty);
		assertDeprecatedDeserialization("Deserialization of mangled shrub",
				secondShrub, NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(secondShrub, false))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				Shrub.class,
				oldKindProperty);
		assertUnwantedChild("<shrub kind=\"shrub\"><troll /></shrub>",
				Shrub.class, false);
		assertMissingProperty("<shrub />", Shrub.class, KIND_PROPERTY, false);
		assertMissingProperty("<shrub kind=\"kind\" />", Shrub.class, "id",
				true);
		assertImageSerialization("Shrub image property is preserved", secondShrub,
				Shrub.class);
	}

	/**
	 * Test serialization of TextFixtures.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testTextSerialization() throws XMLStreamException,
			                                           SPFormatException, IOException {
		final TextFixture firstText = new TextFixture("one", -1);
		assertSerialization(
				"First test of TextFixture serialization, reflection", firstText,
				TextFixture.class);
		final TextFixture secondText = new TextFixture("two", 2);
		assertSerialization(
				"Second test of TextFixture serialization, reflection", secondText,
				TextFixture.class);
		final TextFixture thirdText = new TextFixture("three", 10);
		assertSerialization(
				"Third test of TextFixture serialization, reflection", thirdText,
				TextFixture.class);
		assertUnwantedChild("<text turn=\"1\"><troll /></text>",
				TextFixture.class, false);
		assertImageSerialization("Text image property is preserved", thirdText,
				TextFixture.class);
	}

	/**
	 * Test Village serialization.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testVillageSerialization() throws XMLStreamException,
			                                              SPFormatException,
			                                                  IOException {
		final Player owner = new Player(-1, "");
		for (final TownStatus status : TownStatus.values()) {
			assert status != null;
			final Village firstVillage =
					new Village(status, "villageOne", 1, owner, // NOPMD
							           "human");
			assertSerialization("First Village serialization test, " + status,
					firstVillage, Village.class);
			final Village secondVillage =
					new Village(status, "villageTwo", 2, owner, // NOPMD
							           "dwarf");
			assertSerialization("2nd Village serialization test,  " + status,
					secondVillage, Village.class);
		}
		final Village thirdVillage = new Village(TownStatus.Abandoned, "", 3, owner,
				                                        "elf");
		assertMissingPropertyDeserialization(
				"Village serialization with no or empty name does The Right Thing",
				thirdVillage, createSerializedForm(thirdVillage, true), Village.class,
				NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Village serialization with no or empty name does The Right Thing",
				thirdVillage, createSerializedForm(thirdVillage, false), Village.class,
				NAME_PROPERTY);
		assertUnwantedChild("<village status=\"active\"><village /></village>",
				Village.class, false);
		assertMissingProperty("<village />", Village.class, STATUS_PROPERTY,
				false);
		assertMissingProperty("<village name=\"name\" status=\"active\" />",
				Village.class, "id", true);
		assertMissingProperty(
				"<village name=\"name\" status=\"active\" id=\"0\" />",
				Village.class, OWNER_PROPERTY, true);
		assertImageSerialization("Village image property is preserved", thirdVillage,
				Village.class);
	}

	/**
	 * Test that a Unit should have an ownerr and kind.
	 *
	 * @throws SPFormatException  always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testUnitHasRequiredProperties() throws XMLStreamException,
			                                                   SPFormatException {
		assertMissingProperty("<unit name=\"name\" />", Unit.class,
				OWNER_PROPERTY, true);
		assertMissingProperty("<unit owner=\"\" name=\"name\" />", Unit.class,
				OWNER_PROPERTY, true);
		assertMissingProperty("<unit owner=\"1\" name=\"name\" id=\"0\" />",
				Unit.class, KIND_PROPERTY, true);
		assertMissingProperty(
				"<unit owner=\"1\" kind=\"\" name=\"name\" id=\"0\" />",
				Unit.class, KIND_PROPERTY, true);
	}

	/**
	 * Test that a Unit should have an owner, and other errors and warnings.
	 *
	 * @throws SPFormatException  always
	 * @throws XMLStreamException never
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testUnitWarnings() throws XMLStreamException,
			                                      SPFormatException,
			                                      IOException { // NOPMD
		assertUnwantedChild("<unit><unit /></unit>", Unit.class, false);
		final Unit firstUnit = new Unit(new Player(1, ""), "unitType", "unitName", 1);
		final String oldKindProperty = "type"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				firstUnit, NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(firstUnit, true))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				Unit.class,
				oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				firstUnit, NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(firstUnit, false))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				Unit.class,
				oldKindProperty);
		assertMissingProperty("<unit owner=\"2\" kind=\"unit\" />", Unit.class,
				NAME_PROPERTY, true);
		assertSerialization(
				"Deserialize unit with no kind properly, reflection", new Unit(
						                                                              new Player(2,
								                                                                        ""),

						                                                              "",
						                                                              NAME_PROPERTY,
						                                                              2),
				Unit.class,
				new Warning(Action.Ignore));
		assertMissingPropertyDeserialization(
				"Deserialize unit with no owner properly", new Unit(new Player(
						                                                              -1,
						                                                              ""),
						                                                   "kind",
						                                                   "unitThree",
						                                                   3),
				"<unit kind=\"kind\" name=\"unitThree\" id=\"3\" />",
				Unit.class, OWNER_PROPERTY);
		final Unit fourthUnit = new Unit(new Player(3, ""), "unitKind", "", 4);
		assertMissingPropertyDeserialization(
				"Deserialize unit with no name properly", fourthUnit,
				createSerializedForm(fourthUnit, true), Unit.class, NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Deserialize unit with no name properly", fourthUnit,
				createSerializedForm(fourthUnit, false), Unit.class, NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Deserialize unit with empty name properly", fourthUnit,
				"<unit owner=\"3\" kind=\"unitKind\" name=\"\" id=\"4\" />",
				Unit.class, NAME_PROPERTY);
		assertMissingProperty(
				"<unit owner=\"1\" kind=\"kind\" name=\"name\" />", Unit.class,
				"id", true);
	}

	/**
	 * Test unit-member serialization.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testUnitMemberSerialization() throws XMLStreamException,
			                                                 SPFormatException,
			                                                 IOException {
		final Unit firstUnit = new Unit(new Player(1, ""), "unitType", "unitName", 1);
		firstUnit.addMember(new Animal("animal", false, true, "wild", 2));
		assertSerialization("Unit can have an animal as a member", firstUnit,
				Unit.class);
		firstUnit.addMember(new Worker("worker", DEFAULT_RACE, 3));
		assertSerialization("Unit can have a worker as a member", firstUnit,
				Unit.class);
		firstUnit.addMember(new Worker("second", "elf", 4, new Job("job", 0,
				                                                          new Skill
						                                                          ("skill",
						                                                                   1,
						                                                                   2))));
		assertSerialization("Worker can have jobs", firstUnit, Unit.class);
		assertForwardDeserialization(
				"Explicit specification of default race works", new Worker(
						                                                          "third",
						                                                          DEFAULT_RACE,
						                                                          5),
				"<worker name=\"third\" race=\"human\" id=\"5\" />",
				Worker.class);
		assertDeprecatedDeserialization(
				"'miscellaneous' skill with level should be warned about",
				new Worker("4th", DEFAULT_RACE, 6, new Job("5th", 0,
						                                          new Skill
								                                          ("miscellaneous",
								                                                   1,
								                                                   0))),
				"<worker name=\"4th\" id=\"6\"><job name=\"5th\" level=\"0\">"
						+ "<skill name=\"miscellaneous\" level=\"1\" hours=\"0\"/>"
						+ "</job></worker>", Worker.class, "miscellaneous");
		assertSerialization(
				"but 'miscellaneous' skill without levels causes no warnings",
				new Worker("sixth", DEFAULT_RACE, 7, new Job("seventh", 0,
						                                            new Skill
								                                            ("miscellaneous",
								                                                     0,
								                                                     20))),
				Worker.class,
				new Warning(Action.Die));
		assertSerialization("and levels in another skill cause no warnings",
				new Worker("fourth", DEFAULT_RACE, 8, new Job("fifth", 0,
						                                             new Skill
								                                             ("odd-skill",
								                                                      1,
								                                                      0))),
				Worker.class,
				new Warning(Action.Die));
		final Worker secondWorker = new Worker("sixth", "dwarf", 9);
		secondWorker.setStats(new WorkerStats(0, 0, 1, 2, 3, 4, 5, 6));
		assertSerialization("Worker can have skills", secondWorker, Worker.class);
		assertImageSerialization("Worker image property is preserved", secondWorker,
				Worker.class);
	}

	/**
	 * Test serialization of units' orders.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testOrdersSerialization() throws XMLStreamException,
			                                             SPFormatException, IOException {
		final Player player = new Player(0, "");
		final Unit firstUnit = new Unit(player, "kind of unit", "name of unit", 2);
		final Unit secondUnit = new Unit(player, "kind of unit", "name of unit", 2);
		secondUnit.setOrders("some orders");
		assertEquals("Orders have no effect on equals", firstUnit, secondUnit);
		assertSerialization("Orders don't mess up deserialization", secondUnit,
				Unit.class, new Warning(Action.Die));
		assertTrue("Serialized form contains orders",
				createSerializedForm(secondUnit, true).contains("some orders"));
		assertTrue("Serialized form contains orders",
				createSerializedForm(secondUnit, false).contains("some orders"));

	}

	/**
	 * Test serialization of adventure hooks and portals.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testAdventureSerialization() throws XMLStreamException,
			                                                SPFormatException,
			                                                IOException {
		final Player independent = new Player(1, "independent");
		final TileFixture firstAdventure =
				new AdventureFixture(independent,
						                    "first hook brief", "first hook full", 1);
		final AdventureFixture secondAdventure =
				new AdventureFixture(new Player(2, "player"),
						                    "second hook brief", "second hook full", 2);
		assertFalse("Two different hooks are not equal",
				firstAdventure.equals(secondAdventure));
		final SPMapNG wrapper =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(),
						           -1);
		wrapper.addPlayer(independent);
		wrapper.setBaseTerrain(PointFactory.point(0, 0), TileType.Plains);
		wrapper.addFixture(PointFactory.point(0, 0), firstAdventure);
		assertSerialization("First adventure hook serialization test", wrapper,
				SPMapNG.class);
		assertSerialization("Second adventure hook serialization test", secondAdventure,
				AdventureFixture.class);
		final TileFixture thirdPortal =
				new Portal("portal dest", PointFactory.point(1, 2), 3);
		final Portal fourthPortal =
				new Portal("portal dest two", PointFactory.point(2, 1), 4);
		assertFalse("TWo different portals are not equal",
				thirdPortal.equals(fourthPortal));
		wrapper.addFixture(PointFactory.point(0, 0), thirdPortal);
		assertSerialization("First portal serialization test", wrapper,
				SPMapNG.class);
		assertSerialization("Second portal serialization test", fourthPortal,
				Portal.class);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestMoreFixtureSerialization";
	}

	/**
	 * Test serialization of fortress members other than units.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testFortressMemberSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		final Fortress firstFort = new Fortress(new Player(1, ""), "fortName", 1);
		firstFort.addMember(new Implement(2, "implKind"));
		assertSerialization("Fortress can have an Implement as a member", firstFort,
				Fortress.class);
		firstFort.addMember(
				new ResourcePile(3, "generalKind", "specificKind", 10, "each"));
		assertSerialization("Fortress can have a Resource Pile as a member",
				firstFort, Fortress.class);
		final ResourcePile resource =
				new ResourcePile(4, "generalKind", "specificKind", 15, "pounds");
		resource.setCreated(5);
		assertSerialization("Resource pile can know what turn it was created", resource,
				ResourcePile.class);
	}
}

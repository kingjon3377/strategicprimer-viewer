package model.map.fixtures;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import model.map.BaseTestFixtureSerialization;
import model.map.HasMutableImage;
import model.map.HasPortrait;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Portal;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.IJob;
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
import org.junit.Test;
import util.NullCleaner;
import util.Warning;

import static model.map.PointFactory.point;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Another class to test serialization of TileFixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
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
	/**
	 * Pre-compiled pattern for matching "kind".
	 */
	private static final Pattern KIND_PATTERN = NullCleaner
			.assertNotNull(Pattern.compile(KIND_PROPERTY, Pattern.LITERAL));

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
				new Grove(true, true, "firstGrove", 1));
		assertSerialization("Second test of Grove serialization, reflection",
				new Grove(true, false, "secondGrove", 2));
		assertSerialization("Third test of Grove serialization, reflection",
				new Grove(false, true, "thirdGrove", 3));
		assertSerialization("Fourth test of Grove serialization, reflection",
				new Grove(false, false, "four", 4));
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
				Grove.class, Warning.Ignore);
		assertImageSerialization("Grove image property is preserved",
				new Grove(false, false, "five", 5));
	}

	/**
	 * Test serialization of Meadows, including error-checking.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMeadowSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Meadow serialization, reflection",
				new Meadow("firstMeadow", true, true, 1, FieldStatus.Fallow));
		assertSerialization(
				"Second test of Meadow serialization, reflection",
				new Meadow("secondMeadow", true, false, 2, FieldStatus.Seeding));
		assertSerialization("Third test of Meadow serialization, reflection",
				new Meadow("three", false, true, 3, FieldStatus.Growing));
		assertSerialization("Fourth test of Meadow serialization, reflection",
				new Meadow("four", false, false, 4, FieldStatus.Bearing));
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
				new Meadow("five", false, false, 5, FieldStatus.Fallow));
	}

	/**
	 * Test serialization of Mines.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMineSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Mine serialization",
				new Mine("one", TownStatus.Active, 1));
		assertSerialization("Second test of Mine serialization",
				new Mine("two", TownStatus.Abandoned, 2));
		assertSerialization("Third test of Mine serialization",
				new Mine("three", TownStatus.Burned, 3));
		final HasMutableImage fourthMine = new Mine("four", TownStatus.Ruined, 4);
		assertSerialization("Fourth test of Mine serialization", fourthMine);
		final String oldKindProperty = "product";
		assertDeprecatedDeserialization("Deprecated Mine idiom", fourthMine,
				NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(fourthMine, true))
								.replaceAll(
										Matcher.quoteReplacement(oldKindProperty))),
				oldKindProperty);
		assertDeprecatedDeserialization("Deprecated Mine idiom", fourthMine,
				NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(fourthMine, false))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
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
		assertImageSerialization("Mine image property is preserved", fourthMine);
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
		assertSerialization("First test of Shrub serialization", new Shrub("one", 1));
		final HasMutableImage secondShrub = new Shrub("two", 2);
		assertSerialization("Second test of Shrub serialization", secondShrub);
		final String oldKindProperty = "shrub";
		assertDeprecatedDeserialization("Deserialization of mangled shrub",
				secondShrub, NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(secondShrub, true))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				oldKindProperty);
		assertDeprecatedDeserialization("Deserialization of mangled shrub",
				secondShrub, NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(secondShrub, false))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				oldKindProperty);
		assertUnwantedChild("<shrub kind=\"shrub\"><troll /></shrub>",
				Shrub.class, false);
		assertMissingProperty("<shrub />", Shrub.class, KIND_PROPERTY, false);
		assertMissingProperty("<shrub kind=\"kind\" />", Shrub.class, "id",
				true);
		assertImageSerialization("Shrub image property is preserved", secondShrub);
	}

	/**
	 * Test serialization of TextFixtures.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testTextSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		final TileFixture firstText = new TextFixture("one", -1);
		assertSerialization("First test of TextFixture serialization, reflection",
				firstText);
		final TextFixture secondText = new TextFixture("two", 2);
		assertSerialization("Second test of TextFixture serialization, reflection",
				secondText);
		final HasMutableImage thirdText = new TextFixture("three", 10);
		assertSerialization("Third test of TextFixture serialization, reflection",
				thirdText);
		assertUnwantedChild("<text turn=\"1\"><troll /></text>",
				TextFixture.class, false);
		assertImageSerialization("Text image property is preserved", thirdText);
		final IMutableMapNG wrapper =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(), 0);
		wrapper.setBaseTerrain(point(0, 0), TileType.Plains);
		wrapper.addFixture(point(0, 0), firstText);
		assertForwardDeserialization("Deprecated text-in-map still works", wrapper,
				"<map version=\"2\" rows=\"1\" columns=\"1\"><tile row=\"0\" " +
						"column=\"0\" kind=\"plains\">one</tile></map>");
	}


	/**
	 * Test that a Unit should have an owner and kind.
	 *
	 * @throws SPFormatException  always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testUnitHasRequiredProperties()
			throws XMLStreamException, SPFormatException {
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
	public void testUnitWarnings()
			throws XMLStreamException, SPFormatException, IOException {
		assertUnwantedChild("<unit><unit /></unit>", Unit.class, false);
		final IUnit firstUnit = new Unit(new Player(1, ""), "unitType", "unitName", 1);
		final String oldKindProperty = "type";
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				firstUnit, NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(firstUnit, true))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				firstUnit, NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(firstUnit, false))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				oldKindProperty);
		assertMissingProperty("<unit owner=\"2\" kind=\"unit\" />", Unit.class,
				NAME_PROPERTY, true);
		assertSerialization("Deserialize unit with no kind properly",
				new Unit(new Player(2, ""), "", NAME_PROPERTY, 2),
				Warning.Ignore);
		assertMissingPropertyDeserialization("Deserialize unit with no owner properly",
				new Unit(new Player(-1, ""), "kind", "unitThree", 3),
				"<unit kind=\"kind\" name=\"unitThree\" id=\"3\" />", OWNER_PROPERTY);
		final IUnit fourthUnit = new Unit(new Player(3, ""), "unitKind", "", 4);
		assertMissingPropertyDeserialization(
				"Deserialize unit with no name properly", fourthUnit,
				createSerializedForm(fourthUnit, true), NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Deserialize unit with no name properly", fourthUnit,
				createSerializedForm(fourthUnit, false), NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Deserialize unit with empty name properly", fourthUnit,
				"<unit owner=\"3\" kind=\"unitKind\" name=\"\" id=\"4\" />",
				NAME_PROPERTY);
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
	public void testUnitMemberSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		final IUnit firstUnit = new Unit(new Player(1, ""), "unitType", "unitName", 1);
		firstUnit.addMember(new Animal("animal", false, true, "wild", 2));
		assertSerialization("Unit can have an animal as a member", firstUnit);
		firstUnit.addMember(new Worker("worker", DEFAULT_RACE, 3));
		assertSerialization("Unit can have a worker as a member", firstUnit);
		firstUnit.addMember(new Worker("second", "elf", 4, new Job("job", 0,
																		new Skill("skill",
																						1,
																						2))));
		assertSerialization("Worker can have jobs", firstUnit);
		assertForwardDeserialization("Explicit specification of default race works",
				new Worker("third", DEFAULT_RACE, 5),
				"<worker name=\"third\" race=\"human\" id=\"5\" />");
		assertDeprecatedDeserialization(
				"'miscellaneous' skill with level should be warned about",
				new Worker("4th", DEFAULT_RACE, 6,
								new Job("5th", 0, new Skill("miscellaneous", 1, 0))),
				"<worker name=\"4th\" id=\"6\"><job name=\"5th\" level=\"0\">"
						+ "<skill name=\"miscellaneous\" level=\"1\" hours=\"0\"/>"
						+ "</job></worker>", "miscellaneous");
		assertSerialization("but 'miscellaneous' skill without levels causes no warnings",
				new Worker("sixth", DEFAULT_RACE, 7, new Job("seventh", 0,
																	new Skill("miscellaneous",
																					0,
																					20))),
				Warning.Die);
		assertSerialization("and levels in another skill cause no warnings",
				new Worker("fourth", DEFAULT_RACE, 8,
								new Job("fifth", 0, new Skill("odd-skill", 1, 0))),
				Warning.Die);
		final Worker secondWorker = new Worker("sixth", "dwarf", 9);
		secondWorker.setStats(new WorkerStats(0, 0, 1, 2, 3, 4, 5, 6));
		assertSerialization("Worker can have skills", secondWorker);
		assertImageSerialization("Worker image property is preserved", secondWorker);
		secondWorker.addJob(new Job("seventh", 1));
		assertSerialization("Worker can have a Job with no skills yet", secondWorker);
		assertUnwantedChild(
				"<map version=\"2\" rows=\"1\" columns=\"1\"><tile row=\"0\" " +
						"column=\"0\"><worker name=\"name\" id=\"1\" " +
						"/></tile></row></map>",
				IMapNG.class, false);
		assertPortraitSerialization("Worker portrait property is preserved",
				secondWorker);
	}

	/**
	 * Test serialization of units' orders.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testOrdersSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		final Player player = new Player(0, "");
		final IUnit secondUnit = new Unit(player, "kind of unit", "name of unit", 2);
		secondUnit.setOrders("some orders");
		final IUnit firstUnit = new Unit(player, "kind of unit", "name of unit", 2);
		assertThat("Orders have no effect on equals", secondUnit, equalTo(firstUnit));
		assertSerialization("Orders don't mess up deserialization", secondUnit,
				Warning.Die);
		assertThat("Serialized form contains orders",
				createSerializedForm(secondUnit, true), containsString("some orders"));
		assertThat("Serialized form contains orders",
				createSerializedForm(secondUnit, false), containsString("some orders"));

	}
	/**
	 * Test serialization of portraits.
	 * @throws XMLStreamException never
	 * @throws IOException never
	 * @throws SPFormatException never
	 */
	@Test
	public void testUnitPortraitSerialization()
			throws XMLStreamException, IOException, SPFormatException {
		final HasPortrait unit = new Unit(new Player(1, ""), "kind", "name", 2);
		unit.setPortrait("portraitFile");
		assertSerialization("Portrait doesn't mess up serialization", unit, Warning.Die);
		assertThat("Serialized form contains portrait", createSerializedForm(unit, true),
				containsString("portraitFile"));
		assertThat("Serialized form contains portrait", createSerializedForm(unit, false),
				containsString("portraitFile"));
		assertPortraitSerialization("Unit portrait property is preservedd", unit);
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
		assertThat("Two different hooks are not equal", secondAdventure,
				not(equalTo(firstAdventure)));
		final IMutableMapNG wrapper =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(), -1);
		wrapper.addPlayer(independent);
		wrapper.setBaseTerrain(point(0, 0), TileType.Plains);
		wrapper.addFixture(point(0, 0), firstAdventure);
		assertSerialization("First adventure hook serialization test", wrapper);
		assertSerialization("Second adventure hook serialization test", secondAdventure);
		assertSerialization("Adventure hook with empty descriptions",
				new AdventureFixture(new Player(3, "third"), "", "", 4));
		final TileFixture thirdPortal =
				new Portal("portal dest", point(1, 2), 3);
		final Portal fourthPortal =
				new Portal("portal dest two", point(2, 1), 4);
		assertThat("Two different portals are not equal", fourthPortal,
				not(equalTo(thirdPortal)));
		wrapper.addFixture(point(0, 0), thirdPortal);
		assertSerialization("First portal serialization test", wrapper);
		assertSerialization("Second portal serialization test", fourthPortal);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
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
		firstFort.addMember(new Implement("implKind", 2));
		assertSerialization("Fortress can have an Implement as a member", firstFort);
		firstFort.addMember(
				new ResourcePile(3, "generalKind", "specificKind", 10, "each"));
		assertSerialization("Fortress can have a Resource Pile as a member",
				firstFort);
		final ResourcePile resource =
				new ResourcePile(4, "generalKind", "specificKind", 15, "pounds");
		resource.setCreated(5);
		assertSerialization("Resource pile can know what turn it was created", resource);
	}
	/**
	 * Test that desired warnings get fired when workers have "suspicious" skills.
	 * @throws IOException on I/O error creating serialized form
	 * @throws XMLStreamException on trying to read malformed XML
	 * @throws SPFormatException on trying to read improper SP XML
	 */
	@Test
	public void testSuspiciousSkills()
			throws IOException, XMLStreamException, SPFormatException {
		final IJob job = new Job("jobName", 0, new Skill("hunting", 1, 0));
		final IWorker worker = new Worker("workerName", "race", 1, job);
		assertUnwantedChild(createSerializedForm(worker, false), Worker.class, true);
		job.addSkill(new Skill("secondSkill", 1, 0));
		assertSerialization("No warning if 'suspicious' skill isn't only one in a Job",
				worker);
	}
}

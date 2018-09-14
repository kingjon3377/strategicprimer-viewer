import ceylon.decimal {
    decimalNumber
}
import ceylon.test {
    assertEquals,
    fail,
    assertTrue,
    assertFalse,
    parameters,
    test,
    assertNotEquals,
	assertThatException
}

import java.io {
    StringReader,
	FileNotFoundException
}
import java.lang {
    IllegalArgumentException
}
import java.nio.file {
    JPath=Path,
    JPaths=Paths
}
import java.util {
    NoSuchElementException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamException
}

import lovelace.util.common {
    todo,
    assertAny,
	enumeratedParameter,
	defer,
	simpleSet,
	randomlyGenerated
}

import strategicprimer.model.idreg {
    DuplicateIDException
}
import strategicprimer.model.map {
    Point,
    Player,
    HasPortrait,
    River,
    TileType,
    IMutableMapNG,
    IMapNG,
    HasMutableImage,
    MutablePlayer,
    PlayerImpl,
    SPMapNG,
    MapDimensionsImpl,
    PlayerCollection
}
import strategicprimer.model.map.fixtures {
    TextFixture,
    Implement,
    ResourcePile,
    Ground,
    Quantity
}
import strategicprimer.model.map.fixtures.explorable {
    Portal,
    AdventureFixture,
    Battlefield,
    Cave
}
import strategicprimer.model.map.fixtures.mobile {
    Centaur,
    Unit,
    Worker,
    SimpleImmortal,
    IUnit,
    Giant,
    Fairy,
    Dragon,
    Animal,
    Griffin,
    Ogre,
	AnimalImpl,
	AnimalTracks,
	immortalAnimals
}
import strategicprimer.model.map.fixtures.mobile.worker {
    Job,
    WorkerStats,
    Skill,
    raceFactory
}
import strategicprimer.model.map.fixtures.resources {
    FieldStatus,
    Grove,
    CacheFixture,
    StoneKind,
    Mine,
    StoneDeposit,
    Shrub,
    MineralVein,
    Meadow
}
import strategicprimer.model.map.fixtures.terrain {
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    TownStatus,
    TownSize,
    Village,
    Town,
    Fortification,
    City,
    Fortress,
    CommunityStats
}
import strategicprimer.model.xmlio {
    ISPReader,
    warningLevels,
    Warning,
    IMapReader,
    spNamespace,
    testReaderFactory
}
import strategicprimer.model.xmlio.exceptions {
    UnsupportedTagException,
    UnwantedChildException,
    MissingPropertyException,
    MissingChildException,
    DeprecatedPropertyException
}
import ceylon.language.meta.declaration {
    OpenClassType
}

// Unfortunately, encapsulating anything referred to by parameters()
// results in a compile error about it being a "metamodel reference to local declaration"
{String*} races = raceFactory.races.distinct;
String[] animalStatuses = ["wild", "semi-domesticated", "domesticated", "tame"];
String[] treeTypes = ["oak", "larch", "terebinth"]; // TODO: for these, maybe have longer lists and randomly select 3?
String[] fieldTypes = ["wheat", "amaranth", "bluegrass"];
String[] minerals = ["coal", "platinum", "oil"];
object xmlTests {
	JPath fakeFilename = JPaths.get("");
	[ISPReader+] readers = [testReaderFactory.oldReader, testReaderFactory.newReader];
	"Assert that the given XML will produce the given kind of warning and that the warning
	 satisfies the given additional assertions. If [[desideratum]] is [[null]], assert that
	 the exception is always thrown; if not, assert that the XML will fail with warnings made
	 fatal, but will pass and produce [[desideratum]] with warnings ignored."
	void assertFormatIssue<Type, Expectation>(ISPReader reader, String xml,
	        Type? desideratum, Anything(Expectation) checks = noop)
	        given Expectation satisfies Exception given Type satisfies Object {
	    if (exists desideratum) {
	        if (desideratum is Callable<Anything, Nothing>) {
	            warningLevels.warn.handle(AssertionError(
	                "assertFormatIssue() given Callable as desideratum: put 'null' first?"));
	        }
	        try (stringReader = StringReader(xml)) {
	            Type returned = reader.readXML<Type>(fakeFilename, stringReader,
					warningLevels.ignore);
	            assertEquals(returned, desideratum,
					"Parsed value should be as expected with warnings ignored.");
	        }
	        try (stringReader = StringReader(xml)) {
	            reader.readXML<Type>(fakeFilename, stringReader, warningLevels.die);
	            fail("Expected a fatal warning");
	        } catch (Expectation except) {
                checks(except);
	        }
	    } else {
	        try (stringReader = StringReader(xml)) {
	            reader.readXML<Type>(fakeFilename, stringReader, warningLevels.ignore);
	            fail("Expected a(n) `` `Expectation`.string `` to be thrown");
	        } catch (Expectation except) {
                checks(except);
	        }
	    }
	}
	"Assert that reading the given XML will produce an [[UnsupportedTagException]]. If it's
	 only supposed to be a warning, assert that it'll pass with warnings disabled but fail
	 with warnings made fatal."
	void assertUnsupportedTag<Type>(String xml, String tag, Type? desideratum)
	        given Type satisfies Object {
	    for (reader in readers) {
	        assertFormatIssue<Type, UnsupportedTagException>(reader, xml, desideratum,
				(UnsupportedTagException except) => assertEquals(except.tag.localPart,
	                tag, "Unsupported tag was the tag we expected"));
	    }
	}

	"Assert that reading the given XML will produce an UnwantedChildException. If it's
	 only supposed to be a warning, assert that it'll pass with warnings disabled but
	 fail with warnings made fatal."
	void assertUnwantedChild<Type>(String xml, Type? desideratum)
	        given Type satisfies Object {
	    for (reader in readers) {
	        assertFormatIssue<Type, UnwantedChildException>(reader, xml, desideratum);
	    }
	}

	"Assert that reading the given XML will give a MissingPropertyException. If it's
	 only supposed to be a warning, assert that it'll pass with warnings disabled but
	 object with them made fatal."
	void assertMissingProperty<Type>(String xml, String property,
	        Type? desideratum) given Type satisfies Object {
	    for (reader in readers) {
	        assertFormatIssue<Type, MissingPropertyException>(reader, xml, desideratum,
	                    (except) => assertEquals(except.param, property,
	                "Missing property should be the one we're expecting"));
	    }
	}

	"Assert that reading the given XML will give a MissingChildException."
	void assertMissingChild<Type>(String xml) given Type satisfies Object {
	    for (reader in readers) {
	        assertFormatIssue<Type, MissingChildException>(reader, xml, null);
	    }
	}

	"Assert that reading the given XML will give a DeprecatedPropertyException. If it's only
	 supposed to be a warning, assert that it'll pass with warnings disabled but object with
	 them made fatal."
	void assertDeprecatedProperty<Type>(String xml, String deprecated, String preferred,
	        String tag, Type? desideratum) given Type satisfies Object {
	    for (reader in readers) {
	        assertFormatIssue<Type, DeprecatedPropertyException>(reader, xml, desideratum,
	                    (except) {
	            assertEquals(except.old, deprecated,
	                "Missing property should be the one we're expecting");
	            assertEquals(except.tag.localPart, tag,
	                "Missing property should be on the tag we expect");
	            assertEquals(except.preferred, preferred,
	                "Preferred form should be as expected");
	        });
	    }
	}

	"Create the XML-serialized representation of an object."
	String createSerializedForm(
	        "The object to serialize"
	        Object obj,
	        "Whether to use the deprecated i.e. one-generation-back writer"
	        Boolean deprecated) {
	    StringBuilder writer = StringBuilder();
	    if (deprecated) {
	        testReaderFactory.oldWriter.writeSPObject(writer.append, obj);
	    } else {
	        testReaderFactory.newWriter.writeSPObject(writer.append, obj);
	    }
	    return writer.string;
	}
	"Assert that the serialized form of the given object will deserialize without error."
	void assertSerialization(String message, Object obj,
	        Warning warner = warningLevels.die) {
	    for (reader in readers) {
	        for (deprecated in `Boolean`.caseValues) {
	            try (stringReader = StringReader(createSerializedForm(obj,
	                    deprecated))) {
	                assertEquals(reader.readXML<Object>(fakeFilename, stringReader, warner),
	                    obj, message);
	            }
	        }
	    }
	}
	"Assert that the serialized form of the given object, using both writers, will contain
	 the given string."
	void assertSerializedFormContains(Object obj, String expected, String message) {
	    for (deprecated in `Boolean`.caseValues) {
	        assertTrue(createSerializedForm(obj, deprecated).contains(expected), message);
	    }
	}

	"Assert that the given object, if serialized and deserialized, will have its image
	 property preserved. We modify that property, but set it back to the original value
	 before exiting this method."
	void assertImageSerialization(String message, HasMutableImage obj) {
	    String oldImage = obj.image;
	    for (reader in readers) {
	        for (deprecated in `Boolean`.caseValues) {
	            obj.image = "xyzzy";
	            try (stringReader = StringReader(createSerializedForm(obj, deprecated))) {
	                assertEquals(reader.readXML<HasMutableImage>(fakeFilename, stringReader,
	                    warningLevels.ignore).image, obj.image, message);
	            }
	            obj.image = obj.defaultImage;
	            assertFalse(createSerializedForm(obj, deprecated).contains("image="),
	                "Default image should not be written");
	            obj.image = "";
	            assertFalse(createSerializedForm(obj, deprecated).contains("image="),
	                "Empty image should not be written");
	        }
	    }
	    obj.image = oldImage;
	}

	"Assert that the given object, if serialized and deserialized, will have its portrait
	 property preserved. We modify that property, but set it back to the original value
	 before exiting this method."
	void assertPortraitSerialization(String message, HasPortrait obj) {
	    String oldPortrait = obj.portrait;
	    for (reader in readers) {
	        for (deprecated in `Boolean`.caseValues) {
	            obj.portrait = "xyzzy";
	            try (stringReader = StringReader(createSerializedForm(obj, deprecated))) {
	                assertEquals(reader.readXML<HasPortrait>(fakeFilename, stringReader,
	                    warningLevels.ignore).portrait, obj.portrait, message);
	            }
	            obj.portrait = "";
	            assertFalse(createSerializedForm(obj, deprecated).contains("portrait="),
	                "Empty portrait should not be written");
	        }
	    }
	    obj.portrait = oldPortrait;
	}

	"""Assert that a "forward idiom"---an idiom that we do not yet (or, conversely, anymore)
	   produce, but want to accept---will be handled properly by both readers."""
	void assertForwardDeserialization<Type>(
	        "The assertion message"
	        String message,
	        "The serialized form"
	        String xml,
	        "A lambda to check the state of the deserialized object"
	        todo("Should this be Anything(Type) instead?")
	        Boolean(Type) assertion) given Type satisfies Object {
	    for (reader in readers) {
	        try (stringReader = StringReader(xml)) {
	            assertTrue(assertion(reader.readXML<Type>(fakeFilename, stringReader,
	                warningLevels.die)), message);
	        }
	    }
	}

	"Assert that two serialized forms are equivalent, using both readers."
	void assertEquivalentForms(
	        "The assertion message to use"
	        String message,
	        "The first serialized form"
	        String firstForm,
	        "The second serialized form"
	        String secondForm,
	        "The warning level to use"
	        Warning warningLevel) {
	    for (reader in readers) {
	        try (firstReader = StringReader(firstForm),
	                secondReader = StringReader(secondForm)) {
	            assertEquals(reader.readXML<Object>(fakeFilename, secondReader,
						warningLevel),
	                reader.readXML<Object>(fakeFilename, firstReader, warningLevel),
					message);
	        }
	    }
	}

	"Assert that a map is properly deserialized (by the main map-deserialization methods)."
	void assertMapDeserialization(String message, IMapNG expected, String xml) {
	    for (reader in readers) {
	        assert (is IMapReader reader);
	        try (stringReader = StringReader(xml)) {
	            assertEquals(reader.readMapFromStream(fakeFilename, stringReader,
	                warningLevels.die), expected, message);
	        }
	    }
	}

	"Assert that the given XML will produce warnings about duplicate IDs."
	void assertDuplicateID<Type>(String xml, Type desideratum) given Type satisfies Object {
	    for (reader in readers) {
	        assertFormatIssue<Type, DuplicateIDException>(reader, xml, desideratum);
	    }
	}

	"Assert that a given piece of XML will fail to deserialize with XML format errors, not
	 SP format errors."
	void assertInvalid(String xml) {
	    for (reader in readers) {
	        assertFormatIssue<Object, NoSuchElementException|IllegalArgumentException|
	                XMLStreamException|FileNotFoundException|MissingPropertyException>(
				reader,
	            xml, null, (Exception exception) {
	                if (is MissingPropertyException exception) {
	                    assert (exception.tag.localPart == "include",
							exception.param == "file");
	                }
	            });
	    }
	}

	"""Encapsulate the given string in a "tile" tag inside a "map" tag."""
	String encapsulateTileString(String str) {
	    return "<map version=\"2\" rows=\"2\" columns=\"2\">
	            <tile row=\"1\" column=\"1\" kind=\"plains\">``str``</tile></map>";
	}
	test
	shared void testVillageWantsName(
			enumeratedParameter(`class TownStatus`) TownStatus status,
			randomlyGenerated(2) Integer id,
			parameters(`value races`) String race,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
		Village village = Village(status, "", id, PlayerImpl(-1, ""), race);
		assertMissingProperty(createSerializedForm(village, deprecatedWriter), "name", village);
	}
	test
	shared void testBasicVillageSerialization(parameters(`value treeTypes`) String name,
			enumeratedParameter(`class TownStatus`) TownStatus status,
			parameters(`value races`) String race, randomlyGenerated(1) Integer id) {
		Player owner = PlayerImpl(-1, "");
		Village village = Village(status, name, id, owner, race);
		assertSerialization("Basic Village serialization", Village(status, name, id,
			owner, race));
		assertUnwantedChild<Village>("<village status=\"``status``\"><village /></village>",
			null);
		assertMissingProperty<Village>("<village />", "status", null);
		assertMissingProperty<Village>("<village name=\"``name``\" status=\"``status``\" />",
			"id", Village(status, name, 0, PlayerImpl(-1, "Independent"), "human"));
		assertMissingProperty<Village>(
			"<village race=\"``race``\" name=\"``name``\" status=\"``status``\" id=\"``id``\" />",
			"owner", Village(status, name, id, PlayerImpl(-1, "Independent"), race));
		assertImageSerialization("Village image property is preserved", village);
		assertPortraitSerialization("Village portrait property is preserved", village);
	}
	test
	shared void testVillagePopulationSerialization(
			enumeratedParameter(`class TownStatus`) TownStatus status,
			parameters(`value races`) String race, randomlyGenerated(1) Integer id,
			randomlyGenerated(1) Integer populationSize,
			randomlyGenerated(1) Integer workedField,
			randomlyGenerated(1) Integer producedId,
			randomlyGenerated(1) Integer producedQty,
			randomlyGenerated(1) Integer consumedId,
			randomlyGenerated(1) Integer consumedQty) {
		Village village = Village(status, "villageName", id, PlayerImpl(-1, ""), race);
		CommunityStats pop = CommunityStats(populationSize);
	    village.population = pop;
	    assertSerialization("Village can have community stats", village);
	    pop.addWorkedField(workedField);
		// TODO: Here and below, randomize strings in production and consumption (and skills)
		// TODO: We'd like to randomize number of skills, number of worked fields, etc.
	    pop.yearlyProduction.add(ResourcePile(producedId, "prodKind", "production",
	        Quantity(producedQty, "single units")));
	    pop.yearlyConsumption.add(ResourcePile(consumedId, "consKind", "consumption",
			Quantity(consumedQty, "double units")));
	    assertSerialization("Village stats can have both production and consumption",
	        village);
	}
	test
	shared void testCityWantsName(enumeratedParameter(`class TownSize`) TownSize size,
			enumeratedParameter(`class TownStatus`) TownStatus status,
			randomlyGenerated(2) Integer id,
			randomlyGenerated(2) Integer dc,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
		City city = City(status, size, dc, "", id, PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(city, deprecatedWriter), "name", city);
	}
	test
	shared void testCitySerialization(enumeratedParameter(`class TownSize`) TownSize size,
			enumeratedParameter(`class TownStatus`) TownStatus status,
			randomlyGenerated(1) Integer id, randomlyGenerated(1) Integer dc,
			parameters(`value treeTypes`) String name) {
	    Player owner = PlayerImpl(-1, "");
	    assertSerialization("City serialization", City(status, size, dc, name, id, owner));
	    City city = City(status, size, dc, "", id, owner);
	    assertUnwantedChild<City>(
	        "<city status=\"``status``\" size=\"``size``\" name=\"name\" dc=\"``dc``\">
	         <troll /></city>", null);
	    assertMissingProperty<City>(
	        "<city status=\"``status``\" size=\"``size``\"
	         name=\"name\" dc=\"``dc``\" id=\"``id``\" />", "owner",
	        City(status, size, dc, "name", id, PlayerImpl(-1, "Independent")));
	    assertImageSerialization("City image property is preserved", city);
	    assertPortraitSerialization("City portrait property is preserved", city);
	}
	test
	shared void testCityPopulationSerialization(parameters(`value treeTypes`) String name,
			enumeratedParameter(`class TownSize`) TownSize size,
			enumeratedParameter(`class TownStatus`) TownStatus status,
			parameters(`value races`) String race, randomlyGenerated(1) Integer id,
			randomlyGenerated(1) Integer dc,
			randomlyGenerated(1) Integer populationSize,
			randomlyGenerated(1) Integer workedField,
			randomlyGenerated(1) Integer skillLevel,
			randomlyGenerated(1) Integer producedId,
			randomlyGenerated(1) Integer producedQty) {
		Player owner = PlayerImpl(-1, "");
		City city = City(status, size, dc, name, id, owner);
	    CommunityStats population = CommunityStats(populationSize);
	    population.addWorkedField(workedField);
	    population.setSkillLevel("citySkill", skillLevel);
	    population.yearlyConsumption.add(ResourcePile(producedId, "cityResource",
			"citySpecific", Quantity(producedQty, "cityUnit")));
	    city.population = population;
	    assertSerialization("Community-stats can be serialized", population);
	    assertSerialization("City can have community-stats", city);
	}

	test
	shared void testFortificationWantsName(
			enumeratedParameter(`class TownSize`) TownSize size,
			enumeratedParameter(`class TownStatus`) TownStatus status,
			randomlyGenerated(2) Integer id,
			randomlyGenerated(2) Integer dc,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
		Fortification fort = Fortification(status, size, dc, "", id, PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(fort, deprecatedWriter), "name", fort);
	}
	test
	shared void testFortificationSerialization( // TODO: split and further randomize this and further tests
			enumeratedParameter(`class TownSize`) TownSize size,
			enumeratedParameter(`class TownStatus`) TownStatus status) {
	    Player owner = PlayerImpl(-1, "");
	    assertSerialization("First Fortification serialization test, status ``status
	        ``, size ``size``", Fortification(status, size, 10, "one", 0, owner));
	    assertSerialization(
	        "Second Fortification serialization test, status ``status``, size ``size``",
	        Fortification(status, size, 40, "two", 1, owner));
	    Fortification thirdFort = Fortification(status, size, 30, "", 3, owner);
	    assertUnwantedChild<Fortification>(
	        "<fortification status=\"``status``\" size=\"``size``\" name=\"name\" dc=\"0\">
	         <troll /></fortification>", null);
	    assertMissingProperty<Fortification>(
	        "<fortification status=\"``status``\" size=\"``size``\"
	         name=\"name\" dc=\"0\" id=\"0\" />", "owner",
			Fortification(status, size, 0, "name", 0,
	            PlayerImpl(-1, "Independent")));
	    assertImageSerialization("Fortification image property is preserved", thirdFort);
	    assertPortraitSerialization("Fortification portrait property is preserved",
	        thirdFort);
	    Fortification fourthFort = Fortification(status, size, 40, "fortName", 4, owner);
	    CommunityStats population = CommunityStats(3);
	    population.addWorkedField(7);
	    population.addWorkedField(12);
	    population.setSkillLevel("fortSkill", 3);
	    population.yearlyProduction.add(ResourcePile(5, "fortResource", "fortSpecific",
	        Quantity(1, "fortUnit")));
	    fourthFort.population = population;
	    assertSerialization("Fortification can have community-stats", fourthFort);
	}

	test
	shared void testTownWantsName(enumeratedParameter(`class TownSize`) TownSize size,
			enumeratedParameter(`class TownStatus`) TownStatus status,
			randomlyGenerated(2) Integer id,
			randomlyGenerated(2) Integer dc,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
		Town town = Town(status, size, dc, "", id, PlayerImpl(-1, ""));
		assertMissingProperty(createSerializedForm(town, deprecatedWriter), "name", town);
	}
	test
	shared void testTownSerialization(enumeratedParameter(`class TownSize`) TownSize size,
			enumeratedParameter(`class TownStatus`) TownStatus status) {
	    Player owner = PlayerImpl(-1, "");
	    assertSerialization("First Town serialization test, status ``status``, size ``
			size``", Town(status, size, 10, "one", 0, owner));
	    assertSerialization(
	        "Second Town serialization test, status ``status``, size ``size``", Town(status,
	            size, 40, "two", 1, owner));
	    Town thirdTown = Town(status, size, 30, "", 3, owner);
	    assertUnwantedChild<Town>(
	        "<town status=\"``status``\" size=\"``size``\" name=\"name\" dc=\"0\">
	         <troll /></town>", null);
	    assertMissingProperty<Town>(
	        "<town status=\"``status``\" size=\"``size``\"
	         name=\"name\" dc=\"0\" id=\"0\" />", "owner", Town(status, size, 0, "name", 0,
	            PlayerImpl(-1, "Independent")));
	    assertImageSerialization("Town image property is preserved", thirdTown);
	    assertPortraitSerialization("Town portrait property is preserved", thirdTown);
	    Town  fourthTown = Town (status, size, 40, "townName", 4, owner);
	    CommunityStats population = CommunityStats(3);
	    population.addWorkedField(9);
	    population.addWorkedField(23);
	    population.setSkillLevel("townSkill", 3);
	    population.setSkillLevel("secondSkill", 5);
	    population.yearlyProduction.add(ResourcePile(5, "townResource", "townSpecific",
	        Quantity(1, "TownUnit")));
	    population.yearlyProduction.add(ResourcePile(8, "townResource", "secondSpecific",
	        Quantity(2, "townUnit")));
	    fourthTown.population = population;
	    assertSerialization("Fortification can have community-stats", fourthTown);
	}

	test
	enumeratedParameter(`class StoneKind`)
	shared void testStoneSerialization(StoneKind kind) {
	    assertSerialization("First StoneDeposit test, kind: ``kind``",
	        StoneDeposit(kind, 8, 1));
	    assertSerialization("Second StoneDeposit test, kind: ``kind``",
	        StoneDeposit(kind, 15, 2));
	    assertImageSerialization("Stone image property is preserved",
	        StoneDeposit(kind, 10, 3));
	}

	test
	shared void testOldStoneIdiom(enumeratedParameter(`class StoneKind`) StoneKind kind,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
	    StoneDeposit thirdDeposit = StoneDeposit(kind, 10, 3);
        assertDeprecatedProperty<StoneDeposit>(createSerializedForm(thirdDeposit,
			deprecatedWriter).replace("kind", "stone"), "stone", "kind", "stone",
				thirdDeposit);
	}

	test
	enumeratedParameter(`class StoneKind`)
	shared void testStoneSerializationErrors(StoneKind kind) {
	    assertUnwantedChild<StoneDeposit>(
	        "<stone kind=\"``kind``\" dc=\"10\"><troll /></stone>", null);
	    assertMissingProperty<StoneDeposit>("<stone kind=\"``kind``\" />", "dc", null);
	    assertMissingProperty<StoneDeposit>("""<stone dc="10" />""", "kind", null);
	    assertMissingProperty<StoneDeposit>("<stone kind=\"``kind``\" dc=\"0\" />", "id",
	        StoneDeposit(kind, 0, 0));
	}

	"A factory to encapsulate rivers in a simple map."
	IMapNG encapsulateRivers(Point point, River* rivers) {
	    IMutableMapNG retval = SPMapNG(MapDimensionsImpl(point.row + 1, point.column + 1, 2),
	        PlayerCollection(), -1);
	    retval.baseTerrain[point] = TileType.plains;
	    retval.addRivers(point, *rivers);
	    return retval;
	}

	"Create a simple map."
	IMutableMapNG createSimpleMap(Point dims, <Point->TileType>* terrain) {
	    IMutableMapNG retval = SPMapNG(MapDimensionsImpl(dims.row, dims.column, 2),
	        PlayerCollection(), -1);
	    for (loc->type in terrain) {
	        retval.baseTerrain[loc] = type;
	    }
	    return retval;
	}

	test
	shared void testPlayerSerialization() {
	    assertSerialization("First Player serialization test", PlayerImpl(1, "one"));
	    assertSerialization("Second Player serialization test", PlayerImpl(2, "two"));
	    assertUnwantedChild<Player>(
	        """<player code_name="one" number="1"><troll /></player>""", null);
	    assertMissingProperty<Player>("""<player code_name="one" />""", "number", null);
	    assertMissingProperty<Player>("""<player number="1" />""", "code_name", null);
	    assertPortraitSerialization("Players can have associated portraits",
	        PlayerImpl(3, "three"));
	}

	test
	enumeratedParameter(`class River`)
	shared void testSimpleRiverSerialization(River river) {
	    assertSerialization("River alone", river);
	    Point loc = Point(0, 0);
	    assertSerialization("River in tile", encapsulateRivers(loc, river));
	}

	test
	shared void testRiverSerializationOne() {
	    assertUnwantedChild<IMapNG>(encapsulateTileString("<lake><troll /></lake>"), null);
	    assertMissingProperty<IMapNG>(encapsulateTileString("<river />"), "direction", null);
	    Set<River> setOne = simpleSet(River.north, River.south);
	    Set<River> setTwo = simpleSet(River.south, River.north);
	    assertEquals(setOne, setTwo, "Rivers added in different order to set");
	    assertEquals(
	        encapsulateRivers(Point(1, 1), River.north, River.south),
	        encapsulateRivers(Point(1, 1), River.north, River.south),
	        "Tile equality with rivers");
	    assertEquals(
	        encapsulateRivers(Point(1, 1), River.east, River.west),
	        encapsulateRivers(Point(1, 1), River.west, River.east),
	        "Tile equality with different order of rivers");
	    assertSerialization("Two rivers", encapsulateRivers(Point(1, 2),
	        River.north, River.south));
	    assertMissingProperty<IMapNG>(
	        encapsulateTileString("""<river direction="invalid" />"""), "direction", null);
	}

	test
	shared void testSimpleTileSerializtion() {
	    assertSerialization("Simple Tile", createSimpleMap(Point(1, 1),
	        Point(0, 0)->TileType.desert));
	    IMutableMapNG firstMap = createSimpleMap(Point(2, 2),
	        Point(1, 1)->TileType.plains);
	    firstMap.addFixture(Point(1, 1), Griffin(1));
	    assertSerialization("Tile with one fixture", firstMap);
	    IMutableMapNG secondMap = createSimpleMap(Point(3, 3),
	        Point(2, 2)->TileType.steppe);
	    secondMap.addFixture(Point(2, 2),
	        Unit(PlayerImpl(-1, ""), "unitOne", "firstUnit", 1));
	    secondMap.addFixture(Point(2, 2), Forest("forestKind", true, 8));
	    assertSerialization("Tile with two fixtures", secondMap);
	    assertMissingProperty<IMapNG>(
	        """<map version="2" rows="1" columns="1">
	           <tile column="0" kind="plains" /></map>""", "row", null);
	    assertMissingProperty<IMapNG>(
	        """<map version="2" rows="1" columns="1"><tile row="0" kind="plains" /></map>""",
	        "column", null);
	    assertMissingProperty<IMapNG>(
	        """<map version="2" rows="1" columns="1"><tile row="0" column="0" /></map>""",
	        "kind", SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 0));
	    assertUnwantedChild<IMapNG>(encapsulateTileString(
	        """<tile row="2" column="0" kind="plains" />"""), null);
	}

	test
	shared void testTileSerialization() {
	    IMutableMapNG thirdMap = createSimpleMap(Point(4, 4),
	        Point(3, 3)->TileType.jungle);
	    Player playerOne = PlayerImpl(2, "");
	    Fortress fort = Fortress(playerOne, "fortOne", 1,
	        TownSize.small);
	    fort.addMember(Unit(playerOne, "unitTwo", "secondUnit", 2));
	    thirdMap.addFixture(Point(3, 3), fort);
	    thirdMap.addFixture(Point(3, 3),
	        TextFixture("Random text here", 5));
	    thirdMap.addRivers(Point(3, 3), River.lake);
	    thirdMap.addPlayer(playerOne);
	    assertSerialization("More complex tile", thirdMap);
	}
	test
	shared void testTileDeprecatedIdiom(
			enumeratedParameter(`class TileType`) TileType terrain,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
		IMapNG map = createSimpleMap(Point(5, 5), Point(4, 4)->terrain);
		assertDeprecatedProperty(createSerializedForm(map, deprecatedWriter)
			.replace("kind", "type"), "type", "kind", "tile", map);
	}

	test
	shared void testTileSerializationTwo() {
	    IMutableMapNG five = createSimpleMap(Point(3, 4),
	        Point(2, 3)->TileType.jungle);
	    Player player = PlayerImpl(2, "playerName");
	    five.addFixture(Point(2, 3),
	        Unit(player, "explorer", "name one", 1));
	    five.addFixture(Point(2, 3),
	        Unit(player, "explorer", "name two", 2));
	    five.addPlayer(player);
	    assertEquals(five.fixtures[Point(2, 3)]?.size, 2,
	        "Just checking ...");
	    assertSerialization("Multiple units should come through", five);
	    String xmlTwoLogical =
	            "<view xmlns=\"``spNamespace``\" current_player=\"-1\" current_turn=\"-1\">
	             \t<map version=\"2\" rows=\"3\" columns=\"4\">
	             \t\t<player number=\"2\" code_name=\"playerName\" />
	             \t\t<row index=\"2\">
	             \t\t\t<tile row=\"2\" column=\"3\" kind=\"jungle\">
	             \t\t\t\t<unit owner=\"2\" kind=\"explorer\" name=\"name one\" id=\"1\" />
	             \t\t\t\t<unit owner=\"2\" kind=\"explorer\" name=\"name two\" id=\"2\" />
	             \t\t\t</tile>
	             \t\t</row>
	             \t</map>
	             </view>
	             ";
	    assertEquals(createSerializedForm(five, true), xmlTwoLogical, "Multiple units");
	    String xmlTwoAlphabetical =
	            "<view current_player=\"-1\" current_turn=\"-1\" xmlns=\"``spNamespace``\">
	              \t<map columns=\"4\" rows=\"3\" version=\"2\">
	              \t\t<player number=\"2\" code_name=\"playerName\" />
	              \t\t<row index=\"2\">
	              \t\t\t<tile column=\"3\" kind=\"jungle\" row=\"2\">
	              \t\t\t\t<unit id=\"1\" kind=\"explorer\" name=\"name one\" owner=\"2\" />
	              \t\t\t\t<unit id=\"2\" kind=\"explorer\" name=\"name two\" owner=\"2\" />
	              \t\t\t</tile>
	              \t\t</row>
	              \t</map>
	              </view>
	              ";
	    String serializedForm = createSerializedForm(five, false);
	    assertAny([defer(assertEquals, [serializedForm, xmlTwoLogical]),
	                defer(assertEquals, [serializedForm, xmlTwoAlphabetical]),
	                defer(assertEquals, [serializedForm,
			            xmlTwoLogical.replace("\" />", "\"/>")])], "Multiple units");
	    assertEquals(createSerializedForm(createSimpleMap(Point(1, 1)), true),
	        "<view xmlns=\"``spNamespace``\" current_player=\"-1\" current_turn=\"-1\">
	         \t<map version=\"2\" rows=\"1\" columns=\"1\">
	         \t</map>
	         </view>
	         ", "Shouldn't print empty not-visible tiles");
	    String emptySerializedForm = createSerializedForm(
			createSimpleMap(Point(1, 1)), false);
	    String firstPossibility =
	            "<view xmlns=\"``spNamespace``\" current_player=\"-1\" current_turn=\"-1\">
	             \t<map version=\"2\" rows=\"1\" columns=\"1\">
	             \t</map>
	             </view>
	             ";
	    String secondPossibility =
	            "<view current_player=\"-1\" current_turn=\"-1\" xmlns=\"``spNamespace``\">
	             \t<map columns=\"1\" rows=\"1\" version=\"2\"/>
	             </view>
	             ";
	    assertAny([defer(assertEquals, [emptySerializedForm, firstPossibility]),
	                defer(assertEquals, [emptySerializedForm, secondPossibility])],
	        "Shouldn't print empty not-visible tiles");
	}

	test
	shared void testUnitImageSerialization() {
	    assertImageSerialization("Unit image property is preserved",
	        Unit(PlayerImpl(5, ""), "herder",
	            "herderName", 9));
	}

	test
	shared void testTileSerializationThree(
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
	    IMutableMapNG six = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), 5);
	    six.mountainous[Point(0, 0)] = true;
	    six.addFixture(Point(0, 1), Ground(22, "basalt", false));
	    six.addFixture(Point(1, 0), Forest("pine", false, 19));
	    six.addFixture(Point(1, 1), AnimalImpl("beaver", false, "wild", 18));
        assertMissingProperty(createSerializedForm(six, deprecatedWriter), "kind", six);
	}

	test
	shared void testSkippableSerialization() {
	    assertEquivalentForms("Two maps, one with row tags, one without",
	        """<map rows="1" columns="1" version="2" current_player="-1" />""",
	        """<map rows="1" columns="1" version="2" current_player="-1"><row /></map>""",
	        warningLevels.die);
	    assertEquivalentForms("Two maps, one with future tag, one without",
	        """<map rows="1" columns="1" version="2" current_player="-1" />""",
	        """<map rows="1" columns="1" version="2" current_player="-1"><future /></map>""",
	        warningLevels.ignore);
	    assertUnsupportedTag<IMapNG>(
	        """<map rows="1" columns="1" version="2" current_player="-1"><future /></map>""",
	        "future", SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 0));
	    IMutableMapNG expected = SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 0);
	    expected.baseTerrain[Point(0, 0)] = TileType.steppe;
	    assertUnsupportedTag<IMapNG>(
	        """<map rows="1" columns="1" version="2" current_player="-1">
	           <tile row="0" column="0" kind="steppe"><futureTag /></tile></map>""",
	        "futureTag", expected);
	}

	test
	shared void testMapSerialization() {
	    assertUnwantedChild<IMapNG>(
	        """<map rows="1" columns="1" version="2"><hill /></map>""", null);
	    MutablePlayer player = PlayerImpl(1, "playerOne");
	    player.current = true;
	    IMutableMapNG firstMap = SPMapNG(MapDimensionsImpl(1, 1, 2),
	        PlayerCollection(), 0);
	    firstMap.addPlayer(player);
	    Point loc = Point(0, 0);
	    firstMap.baseTerrain[loc] = TileType.plains;
	    assertSerialization("Simple Map serialization", firstMap);
	    assertMissingProperty<IMapNG>("""<map version="2" columns="1" />""", "rows", null);
	    assertMissingProperty<IMapNG>("""<map version="2" rows="1" />""", "columns", null);
	    String originalFormOne = createSerializedForm(firstMap, false);
	    String originalFormTwo = createSerializedForm(firstMap, true);
	    firstMap.baseTerrain[Point(1, 1)] = null;
	    assertEquals(createSerializedForm(firstMap, false), originalFormOne,
	        "Explicitly not visible tile is not serialized");
	    assertEquals(createSerializedForm(firstMap, true), originalFormTwo,
	        "Explicitly not visible tile is not serialized");
	    firstMap.mountainous[loc] = true;
	    assertSerialization("Map with a mountainous point", firstMap);
	    assertMissingProperty<IMapNG>(
	        """<view current_turn="0"><map version="2" rows="1" columns="1" /></view>""",
	        "current_player", SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 0));
	    assertMissingProperty<IMapNG>(
	        """<view current_player="0"><map version="2" rows="1" columns="1" /></view>""",
	        "current_turn", null);
	    assertMissingChild<IMapNG>("""<view current_player="1" current_turn="0" />""");
	    assertMissingChild<IMapNG>("""<view current_player="1" current_turn="13" />""");
	    assertUnwantedChild<IMapNG>(
	        """<view current_player="0" current_turn="0">
	           <map version="2" rows="1" columns="1" />
	           <map version="2" rows="1" columns="1" /></view>""", null);
	    assertUnwantedChild<IMapNG>(
	        """<view current_player="0" current_turn="0"><hill /></view>""", null);
	    assertMapDeserialization("Proper deserialization of map without view tag", firstMap,
	        """<map version="2" rows="1" columns="1" current_player="1">
	           <player number="1" code_name="playerOne" />
	           <row index="0"><tile row="0" column="0" kind="plains"><mountain /></tile></row>
	           </map>""");
	}

	test
	shared void testNamespacedSerialization() {
	    MutablePlayer player = PlayerImpl(1, "playerOne");
	    player.current = true;
	    IMutableMapNG firstMap = SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 0);
	    firstMap.addPlayer(player);
	    Point loc = Point(0, 0);
	    firstMap.baseTerrain[loc] = TileType.steppe;
	    assertMapDeserialization("Proper deserialization of namespaced map", firstMap,
	        "<map xmlns=\"``spNamespace``\" version=\"2\" rows=\"1\" columns=\"1\"
	         current_player=\"1\"><player number=\"1\" code_name=\"playerOne\" /><row
	         index=\"0\"><tile row=\"0\" column=\"0\" kind=\"steppe\" /></row></map>");
	    assertMapDeserialization(
	        "Proper deserialization of map if another namespace is declared default",
	        firstMap,
	        "<sp:map xmlns=\"xyzzy\" xmlns:sp=\"``spNamespace``\" version=\"2\"
	         rows=\"1\" columns=\"1\" current_player=\"1\"><sp:player number=\"1\"
	         code_name=\"playerOne\" /><sp:row index=\"0\"><sp:tile row=\"0\" column=\"0\"
	         kind=\"steppe\" /></sp:row></sp:map>");
	    assertMapDeserialization("Non-root other-namespace tags ignored", firstMap,
	        "<map xmlns=\"``spNamespace``\" version=\"2\" rows=\"1\" columns=\"1\"
	         current_player=\"1\" xmlns:xy=\"xyzzy\"><player number=\"1\"
	         code_name=\"playerOne\" /><xy:xyzzy><row index=\"0\"><tile row=\"0\"
	         column=\"0\" kind=\"steppe\"><xy:hill id=\"0\" /></tile></row></xy:xyzzy></map>
	         ");
	    for (reader in readers) {
		        assertFormatIssue<IMapNG,UnwantedChildException|XMLStreamException>(reader,
		            """<map xmlns="xyzzy" version="2" rows="1" columns="1" current_player="1">
		               <player number="1" code_name="playerOne" /><row index="0"><tile row="0"
		               column="0" kind="steppe" /></row></map>""", null, (except) {
			                switch (except)
		                    case (is UnwantedChildException) {
			                    assertEquals(except.tag.localPart, "root",
			                        "'Tag' that had the unexpected child was what we expected");
			                    assertEquals(except.child, QName("xyzzy", "map"),
			                        "Unwanted child was the one we expected");
		                    }
		                    case (is XMLStreamException) {
		                        assertThatException(except)
		                                .hasMessage(
											"XML stream didn't contain a start element");
		                    }
		            });
	            assertFormatIssue<AdventureFixture,UnwantedChildException|XMLStreamException>(
					reader, """<adventure xmlns="xyzzy" id="1" brief="one" full="two" />""", null);
	        }
	}

	test
	shared void testInclude() {
	    assertForwardDeserialization<SimpleImmortal>("Reading Ogre via <include>",
	        """<include file="string:&lt;ogre id=&quot;1&quot; /&gt;" />""",
	        Ogre(1).equals);
		String exceptionMessage =
				"""ParseError at [row,col]:[1,10]
				   Message: XML document structures must start and end within the same entity.""";
	    for (reader in readers) {
	        assertFormatIssue<Object, FileNotFoundException>(reader,
	            """<include file="nosuchfile" />""", null);
	        assertFormatIssue<Object, XMLStreamException>(reader,
	            """<include file="string:&lt;nonsense" />""", null,
		        (except) => assertThatException(except).hasMessage(exceptionMessage));
	    }
	}

	test
	shared void testDuplicateID() {
		IMutableMapNG expected = SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 0);
		Point point = Point(0, 0);
		expected.baseTerrain[point] = TileType.steppe;
		value player = PlayerImpl(1, "playerOne");
		player.current = true;
		expected.addPlayer(player);
		expected.addFixture(point, Hill(1));
		expected.addFixture(point, Ogre(1));
	    assertDuplicateID(
	        """<map version="2" rows="1" columns="1" current_player="1">
	           <player number="1" code_name="playerOne" />
	           <row index="0">
	           <tile row="0" column="0" kind="steppe">
	           <hill id="1" /><ogre id="1" /></tile></row></map>""", expected);
	}

	test
	shared void testRejectsInvalid() {
	    assertInvalid("""<map version="2" rows="1" columns="1" current_player="1">""");
	    assertInvalid(
	        """<map version="2" rows="1" columns="1" current_player="1"><></map>""");
	    assertInvalid("""<include file="nonexistent" />""");
	    assertInvalid("""<include file="string:&lt;ogre id=&quot;1&quot;&gt;" />""");
	    assertInvalid("<include />");
	    assertInvalid("""<include file="nonexistent">""");
	    assertInvalid("""<include file="string:&lt;""");
	    assertInvalid("""<include file="" />""");
	    assertInvalid("""<include file="string:&lt;xyzzy" />""");
	}

	test
	shared void testGroveSerialization(enumeratedParameter(`class Boolean`) Boolean fruit,
			enumeratedParameter(`class Boolean`) Boolean cultivated,
			parameters(`value treeTypes`) String trees,
			randomlyGenerated(2) Integer id) {
		// Using [[races]] for tree kinds because I don't want to loop over multiples within one
		// test, I don't want to define yet another top-level object for [[parameters]], and
		// so long as they're a series of different Strings it doesn't matter anyway.
        assertSerialization("Test of [[Grove]] serialization",
            Grove(fruit, cultivated, trees, id));
	    assertUnwantedChild<Grove>("""<grove wild="true" kind="kind"><troll /></grove>""",
	        null);
	    assertMissingProperty<Grove>("<grove />", "cultivated", null);
	    assertMissingProperty<Grove>("""<grove wild="false" />""", "kind", null);
	    assertDeprecatedProperty<Grove>("""<grove cultivated="true" tree="tree" id="0" />""",
	        "tree", "kind", "grove", Grove(false, true, "tree", 0));
	    assertMissingProperty<Grove>("""<grove cultivated="true" kind="kind" />""", "id",
	        Grove(false, true, "kind", 0));
	    assertDeprecatedProperty<Grove>("""<grove wild="true" kind="tree" id="0" />""",
	        "wild", "cultivated", "grove", Grove(false, false, "tree", 0));
	    assertEquivalentForms("Assert that wild is the inverse of cultivated",
	        """<grove wild="true" kind="tree" id="0" />""",
	        """<grove cultivated="false" kind="tree" id="0" />""", warningLevels.ignore);
	    assertImageSerialization("Grove image property is preserved", Grove(false, false,
	        trees, id));
	    assertSerialization("Groves can have 'count' property",
			Grove(true, true, trees, id, 4));
	}

	test
	shared void testMeadowSerialization(randomlyGenerated(2) Integer id,
			enumeratedParameter(`class FieldStatus`) FieldStatus status,
			parameters(`value fieldTypes`) String kind,
			enumeratedParameter(`class Boolean`) Boolean field,
			enumeratedParameter(`class Boolean`) Boolean cultivated) {
        assertSerialization("Test of [[Meadow]] serialization",
            Meadow(kind, field, cultivated, id, status));
	    assertUnwantedChild<Meadow>(
	        """<meadow kind="flax" cultivated="false"><troll /></meadow>""", null);
	    assertMissingProperty<Meadow>("""<meadow cultivated="false" />""", "kind", null);
	    assertMissingProperty<Meadow>("""<meadow kind="flax" />""", "cultivated", null);
	    assertMissingProperty<Meadow>("""<field kind="kind" cultivated="true" />""", "id",
	        Meadow("kind", true, true, 0, FieldStatus.random(0)));
	    assertMissingProperty<Meadow>("""<field kind="kind" cultivated="true" id="0" />""",
	        "status", Meadow("kind", true, true, 0, FieldStatus.random(0)));
	    assertImageSerialization("Meadow image property is preserved",
	        Meadow(kind, field, cultivated, id, status));
	    assertSerialization("Meadows can have acreage numbers",
			Meadow(kind, field, cultivated, id, status,
				decimalNumber(5).divided(decimalNumber(4))));
	    assertSerialization("Meadows can have acreage numbers",
			Meadow(kind, field, cultivated, id, status,
				decimalNumber(3).divided(decimalNumber(4))));
	}

	test
	shared void testMineSerialization(randomlyGenerated(2) Integer id,
			parameters(`value minerals`) String kind,
			enumeratedParameter(`class TownStatus`) TownStatus status,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
        Mine mine = Mine(kind, status, id);
        assertSerialization("Test of [[Mine]] serialization",
            mine);
        assertDeprecatedProperty(createSerializedForm(mine, deprecatedWriter)
            .replace("kind=", "product="), "product", "kind", "mine", mine);
	    assertUnwantedChild<Mine>("""<mine kind="gold" status="active"><troll /></mine>""",
	        null);
	    assertMissingProperty<Mine>("""<mine status="active" />""", "kind", null);
	    assertMissingProperty<Mine>("""<mine kind="gold" />""", "status", null);
	    assertMissingProperty<Mine>("""<mine kind="kind" status="active" />""", "id",
	        Mine("kind", TownStatus.active, 0));
	    assertImageSerialization("Mine image property is preserved", mine);
	}

	test
	shared void testShrubSerialization(randomlyGenerated(2) Integer id,
			parameters(`value fieldTypes`) String kind,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
		Shrub shrub = Shrub(kind, id);
	    assertSerialization("First test of Shrub serialization", shrub);
        assertDeprecatedProperty(createSerializedForm(shrub, deprecatedWriter)
            .replace("kind", "shrub"), "shrub", "kind", "shrub", shrub);
	    assertUnwantedChild<Shrub>("""<shrub kind="shrub"><troll /></shrub>""", null);
	    assertMissingProperty<Shrub>("<shrub />", "kind", null);
	    assertMissingProperty<Shrub>("""<shrub kind="kind" />""", "id", Shrub("kind", 0));
	    assertImageSerialization("Shrub image property is preserved", shrub);
	    assertSerialization("Shrub can have 'count' property", Shrub(kind, id, 3));
	}

	test
	shared void testTextSerialization() {
	    assertSerialization("First test of [[TextFixture]] serialization",
	        TextFixture("one", -1));
	    assertSerialization("Second test of [[TextFixture]] serialization",
	        TextFixture("two", 2));
	    TextFixture third = TextFixture("three", 10);
	    assertSerialization("Third test of [[TextFixture]] serialization", third);
	    assertUnwantedChild<TextFixture>("""<text turn="1"><troll /></text>""", null);
	    assertImageSerialization("Text image property is preserved", third);
	    IMutableMapNG wrapper = createSimpleMap(Point(1, 1),
	        Point(0, 0)->TileType.plains);
	    wrapper.addFixture(Point(0, 0), TextFixture("one", -1));
	    wrapper.currentTurn = 0;
	    assertForwardDeserialization("Deprecated text-in-map still works",
	        """<map version="2" rows="1" columns="1" current_player="-1">
	           <tile row="0" column="0" kind="plains">one</tile></map>""",
	        wrapper.equals);
	}

	test
	shared void testUnitHasRequiredProperties() {
	    assertMissingProperty<IUnit>("""<unit name="name" />""", "owner",
			Unit(PlayerImpl(-1, ""), "", "name", 0));
	    assertMissingProperty<IUnit>("""<unit owner="1" name="name" id="0" />""", "kind",
	        Unit(PlayerImpl(1, ""), "", "name", 0));
	    assertMissingProperty<IUnit>("""<unit owner="1" kind="" name="name" id="0" />""",
	        "kind", Unit(PlayerImpl(1, ""), "", "name", 0));
	}

	test
	shared void testUnitWarnings(
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter,
			randomlyGenerated(2) Integer id,
			parameters(`value treeTypes`) String name,
			parameters(`value fieldTypes`) String kind) {
		// TODO: should probably test spaces in name and kind
		assertUnwantedChild<IUnit>("<unit><unit /></unit>", null);
	    IUnit firstUnit = Unit(PlayerImpl(1, ""), kind, name, id);
        assertDeprecatedProperty(createSerializedForm(firstUnit, deprecatedWriter)
            .replace("kind", "type"), "type", "kind", "unit", firstUnit);
	    assertMissingProperty<IUnit>("""<unit owner="2" kind="unit" />""", "name",
	        Unit(PlayerImpl(2, ""), "unit", "", 0));
	    assertSerialization("Deserialize unit with no kind properly", Unit(PlayerImpl(2, ""),
	        "", name, 2), warningLevels.ignore);
	    assertMissingProperty("""<unit kind="kind" name="unitThree" id="3" />""", "owner",
	        Unit(PlayerImpl(-1, ""), "kind", "unitThree", 3));
	    IUnit fourthUnit = Unit(PlayerImpl(4, ""), kind, "", id);
        assertMissingProperty(createSerializedForm(fourthUnit, deprecatedWriter), "name",
            fourthUnit);
	    assertMissingProperty(
			"<unit owner=\"4\" kind=\"``kind``\" name=\"\" id=\"``id``\" />", "name",
	        fourthUnit);
	    assertMissingProperty<IUnit>(
			"<unit owner=\"1\" kind=\"``kind``\" name=\"``name``\" />", "id",
	        Unit(PlayerImpl(1, ""), kind, name, 0));
	}

	test
	shared void testUnitMemberSerialization() {
	    IUnit firstUnit = Unit(PlayerImpl(1, ""), "unitType", "unitName", 1);
	    firstUnit.addMember(AnimalImpl("animal", true, "wild", 2));
	    assertSerialization("Unit can have an animal as a member", firstUnit);
	    firstUnit.addMember(Worker("worker", "human", 3));
	    assertSerialization("Unit can have a worker as a member", firstUnit);
	    firstUnit.addMember(Worker("second", "elf", 4, Job("job", 0, Skill("skill", 1, 2))));
	    assertSerialization("Worker can have jobs", firstUnit);
	    assertForwardDeserialization("Explicit specification of default race works",
	        """<worker name="third" race="human" id="5" />""",
	        Worker("third", "human", 5).equals);
	    assertForwardDeserialization("Implicit default race also works",
	        """<worker name="fourth" id="6" />""",
	        Worker("fourth", "human", 6).equals);
	    Worker secondWorker = Worker("sixth", "dwarf", 9);
	    secondWorker.stats = WorkerStats(0, 0, 1, 2, 3, 4, 5, 6);
	    assertSerialization("Worker can have stats", secondWorker);
	    assertImageSerialization("Worker image property is preserved", secondWorker);
	    secondWorker.addJob(Job("seventh", 1));
	    assertSerialization("Worker can have Job with no skills yet", secondWorker);
	    assertUnwantedChild<IMapNG>("""<map version="2" rows="1" columns="1">
	                                   <tile row="0" column="0" kind="plains">
	                                   <worker name="name" id="1" /></tile></map>""", null);
	    assertPortraitSerialization("Worker portrait property is preserved", secondWorker);
	}

	test
	shared void testOrdersSerialization() {
	    Player player = PlayerImpl(0, "");
	    IUnit firstUnit = Unit(player, "kind of unit", "name of unit", 2);
	    IUnit secondUnit = Unit(player, "kind of unit", "name of unit", 2);
	    secondUnit.setOrders(-1, "some orders");
	    assertEquals(firstUnit, secondUnit, "Orders have no effect on equals");
	    assertSerialization("Orders don't mess up deserialization", secondUnit);
	    assertSerializedFormContains(secondUnit, "some orders",
	        "Serialized form contains orders");
	    secondUnit.setOrders(2, "some other orders");
	    assertSerializedFormContains(secondUnit, "some orders",
	        "Serialized form contains original orders after adding new orders");
	    assertSerializedFormContains(secondUnit, "some other orders",
	        "Serialized form contains new orders too");
	    secondUnit.setResults(3, "some results");
	    assertSerializedFormContains(secondUnit, "some results",
	        "Serialized form contains results");
	    secondUnit.setResults(-1, "some other results");
	    assertSerializedFormContains(secondUnit, "some results",
	        "Serialized form contains original results after adding new results");
	    assertSerializedFormContains(secondUnit, "some other results",
	        "Serialized form contains new results too");
	    assertForwardDeserialization<IUnit>("Orders can be read without tags",
	        """<unit name="name" kind="kind" id="1" owner="-1">Orders orders</unit>""",
	                (unit) => unit.getOrders(-1) == "Orders orders");
	}

	test
	shared void testQuoting(randomlyGenerated(3) Integer id) {
	    Player player = PlayerImpl(0, "");
	    Unit unit = Unit(player, "kind of unit", "name of unit", id);
	    unit.setOrders(4, """I <3 & :( "meta'""");
	    unit.setResults(5, "2 --> 1");
	    assertSerialization(
	        "Serialization preserves XML meta-characters in orders and results", unit);
	    unit.setOrders(3, "1 << 2");
	    unit.setResults(-1, "\"quote this\"");
	    assertSerialization("This works even if such characters occur more than once", unit);
	    unit.name = "\"Can't quote this ><>&\"";
	    assertSerialization("Data stored in XML attributes is quoted", unit);
	}

	test
	shared void testUnitPortraitSerialization(randomlyGenerated(3) Integer id) {
	    Unit unit = Unit(PlayerImpl(1, ""), "kind", "name", id);
	    unit.portrait = "portraitFile";
	    assertSerialization("Portrait doesn't mess up serialization", unit);
	    assertSerializedFormContains(unit, "portraitFile",
	        "Serialized form contains portrait");
	    assertPortraitSerialization("Unit portrait property is preserved", unit);
	}

	test
	shared void testAdventureSerialization(
			randomlyGenerated(2) Integer idOne,
			randomlyGenerated(2) Integer idTwo) {
	    Player independent = PlayerImpl(1, "independent");
	    AdventureFixture first = AdventureFixture(independent, "first hook brief",
	        "first hook full", idOne);
	    AdventureFixture second = AdventureFixture(PlayerImpl(2, "player"),
	        "second hook brief", "second hook full", idTwo);
	    assertNotEquals(first, second, "Two different hooks are not equal");
	    IMutableMapNG wrapper = createSimpleMap(Point(1, 1),
	        Point(0, 0)->TileType.plains);
	    wrapper.addPlayer(independent);
	    wrapper.addFixture(Point(0, 0), first);
	    assertSerialization("First [[AdventureFixture]] serialization test", wrapper);
	    assertSerialization("Second [[AdventureFixture]] serialization test", second);
	    assertSerialization("[[AdventureFixture]] with empty descriptions",
	        AdventureFixture(PlayerImpl(3, "third"), "", "", idOne));
	    Portal third = Portal("portal dest", Point(1, 2), idOne);
	    Portal fourth = Portal("portal dest two", Point(2, 1), idTwo);
	    assertNotEquals(third, fourth, "Two different portals are not equal");
	    wrapper.addFixture(Point(0, 0), fourth);
	    assertSerialization("First [[Portal]] serialization test", wrapper);
	    assertSerialization("Second [[Portal]] serialization test", fourth);
	}

	test
	shared void testFortressMemberSerialization() {
	    Fortress firstFort = Fortress(PlayerImpl(1, ""), "fortName", 1, TownSize.small);
	    firstFort.addMember(Implement("implKind", 2));
	    assertSerialization("[[Fortress]] can have an [[Implement]] as a member", firstFort);
	    firstFort.addMember(Implement("implKindTwo", 8));
	    assertSerialization("[[Implement]] can be more than one in one object", firstFort);
	    firstFort.addMember(ResourcePile(3, "generalKind", "specificKind",
	        Quantity(10, "each")));
	    assertSerialization("[[Fortress]] can have a [[ResourcePile]] as a member",
	        firstFort);
	    ResourcePile resource = ResourcePile(4, "generalKind", "specificKind",
	        Quantity(15, "pounds"));
	    resource.created = 5;
	    assertSerialization("Resource pile can know what turn it was created", resource);
	    assertSerialization("Resource pile can have non-integer quantity", ResourcePile(5,
	        "resourceKind", "specificKind2",
	        Quantity(decimalNumber(3) / decimalNumber(2), "cubic feet")));
	}

	test
	shared void testAnimalTracksSerialization(parameters(`value treeTypes`) String kind) {
		assertSerialization("Test of animal-track serialization", AnimalTracks(kind));
		assertUnwantedChild<AnimalTracks>(
			"""<animal kind="tracks" traces="true"><troll /></animal>""", null);
		assertMissingProperty<AnimalTracks>("""<animal traces="true" />""", "kind", null);
		assertImageSerialization("Animal-track image property is preserved",
			AnimalTracks(kind));
		assertEquivalentForms("""Former idiom still works""",
			"<animal kind=\"kind\" status=\"wild\" traces=\"\" />",
			"<animal kind=\"kind\" status=\"wild\" traces=\"true\" />",
			warningLevels.die);
	}
	test
	shared void testAnimalSerialization(randomlyGenerated(2) Integer id,
			parameters(`value animalStatuses`) String status,
			enumeratedParameter(`class Boolean`) Boolean talking) {
        assertSerialization("Test of [[Animal]] serialization",
            AnimalImpl("animalKind", talking, status, id));
	    assertUnwantedChild<Animal>("""<animal kind="animal"><troll /></animal>"""", null);
	    assertMissingProperty<Animal>("<animal />", "kind", null);
	    assertForwardDeserialization<Animal>("Forward-looking in re talking",
	        "<animal kind=\"animalFive\" talking=\"false\" id=\"``id``\" />",
	        AnimalImpl("animalFive", false, "wild", id).equals);
	    assertMissingProperty<Animal>("""<animal kind="animalSix" talking="true" />""", "id",
	        AnimalImpl("animalSix", true, "wild", 0));
	    assertMissingProperty<Animal>("""<animal kind="animalEight" id="nonNumeric" />""",
	        "id", null);
	    assertForwardDeserialization<Animal>("Explicit default status of animal",
	        "<animal kind=\"animalSeven\" status=\"wild\" id=\"``id``\" />",
	        AnimalImpl("animalSeven", false, "wild", id).equals);
	    assertImageSerialization("Animal image property is preserved",
	        AnimalImpl("animalFour", true, "status", id));
	    assertForwardDeserialization<Animal>("Namespaced attribute",
	        "<animal xmlns:sp=\"``spNamespace``\" sp:kind=\"animalNine\"
	         sp:talking=\"true\" sp:traces=\"false\" sp:status=\"tame\" sp:id=\"``id``\" />",
	        AnimalImpl("animalNine", true, "tame", id).equals);
	    assertEquivalentForms("""Supports 'traces="false"'""",
	        "<animal kind=\"kind\" status=\"wild\" id=\"``id``\" />",
	        "<animal kind=\"kind\" traces=\"false\" status=\"wild\" id=\"``id``\" />",
	        warningLevels.die);
	    assertSerialization("Animal age is preserved",
	        AnimalImpl("youngKind", talking, status, id, 8));
	    assertSerialization("Animal population count is preserved",
	        AnimalImpl("population", talking, status, id, -1, 55));
	    assertNotEquals(AnimalImpl("animal", talking, status, id, -1),
	        AnimalImpl("animal", talking, status, id, 8),
	        "But animal age is checked in equals()");
	    assertNotEquals(AnimalImpl("animal", talking, status, id, -1, 1),
	        AnimalImpl("animal", talking, status, id, -1, 2),
	        "Animal population count is checked in equals()");
	}

	test
	shared void testImmortalAnimalDeserialization(
			parameters(`value immortalAnimals`) String animal,
			randomlyGenerated(2) Integer id,
			randomlyGenerated(2) Integer count) =>
		assertUnsupportedTag("<``animal`` id=\"``id``\" count=\"``count``\" />",
			animal, AnimalImpl(animal, false, "wild", id, -1, count));

	test
	shared void testCacheSerialization(randomlyGenerated(3) Integer id) {
	    assertSerialization("First test of Cache serialization", CacheFixture("kindOne",
	        "contentsOne", id));
	    assertSerialization("Second test of Cache serialization", CacheFixture("kindTwo",
	        "contentsTwo", id));
	    assertUnwantedChild<CacheFixture>(
	        """<cache kind="kind" contents="contents"><troll /></cache>""", null);
	    assertMissingProperty<CacheFixture>("""<cache contents="contents" />""", "kind",
	        null);
	    assertMissingProperty<CacheFixture>("""<cache kind="kind" />""", "contents", null);
	    assertMissingProperty<CacheFixture>("""<cache kind="kind" contents="contents" />""",
	        "id", CacheFixture("kind", "contents", 0));
	    assertImageSerialization("Cache image property is preserved",
	        CacheFixture("kindThree", "contentsThree", id));
	}

	test
	shared void testCentaurSerialization(randomlyGenerated(3) Integer id) {
	    assertSerialization("First test of Centaur serialization",
	        Centaur("firstCentaur", id));
	    assertSerialization("Second test of Centaur serialization",
	        Centaur("secondCentaur", id));
	    assertUnwantedChild<Centaur>("""<centaur kind="forest"><troll /></centaur>""",
	        null);
	    assertMissingProperty<Centaur>("<centaur />", "kind", null);
	    assertMissingProperty<Centaur>("""<centaur kind="kind" />""", "id",
			Centaur("kind", 0));
	    assertImageSerialization("Centaur image property is preserved",
	        Centaur("thirdCentaur", id));
	}

	test
	shared void testDragonSerialization(randomlyGenerated(3) Integer id) {
	    assertSerialization("First test of Dragon serialization", Dragon("", id));
	    assertSerialization("Second test of Dragon serialization",
			Dragon("secondDragon", id));
	    assertUnwantedChild<Dragon>("""<dragon kind="ice"><hill /></dragon>""", null);
	    assertMissingProperty<Dragon>("<dragon />", "kind", null);
	    assertMissingProperty<Dragon>("""<dragon kind="kind" />""", "id", Dragon("kind", 0));
	    assertImageSerialization("Dragon image property is preserved",
	        Dragon("thirdDragon", id));
	}

	test
	shared void testFairySerialization(randomlyGenerated(3) Integer id) {
	    assertSerialization("First test of Fairy serialization", Fairy("oneFairy", id));
	    assertSerialization("Second test of Fairy serialization", Fairy("twoFairy", id));
	    assertUnwantedChild<Fairy>("""<fairy kind="great"><hill /></fairy>""", null);
	    assertMissingProperty<Fairy>("<fairy />", "kind", null);
	    assertMissingProperty<Fairy>("""<fairy kind="kind" />""", "id", Fairy("kind", 0));
	    assertImageSerialization("Fairy image property is preserved",
	        Fairy("threeFairy", id));
	}

	test
	shared void testForestSerialization(randomlyGenerated(3) Integer id) {
	    assertSerialization("First test of Forest serialization",
	        Forest("firstForest", false, id));
	    assertSerialization("Second test of Forest serialization",
	        Forest("secondForest", true, id));
	    assertUnwantedChild<Forest>("""<forest kind="trees"><hill /></forest>""", null);
	    assertMissingProperty<Forest>("<forest />", "kind", null);
	    assertImageSerialization("Forest image property is preserved",
	        Forest("thirdForest", true, id));
	    Point loc = Point(0, 0);
	    IMutableMapNG map = createSimpleMap(Point(1, 1),
	        loc->TileType.plains);
	    map.addFixture(loc, Forest("trees", false, 4));
	    map.addFixture(loc, Forest("secondForest", true, 5));
	    assertSerialization("Map with multiple Forests on a tile", map);
	    assertEquivalentForms("Duplicate Forests ignored",
	        encapsulateTileString(
	            """<forest kind="trees" id="4" />
	               <forest kind="second" rows="true" id="5" />"""),
	        encapsulateTileString(
	            """<forest kind="trees" id="4" />
	               <forest kind="trees" id="4" />
	               <forest kind="second" rows="true" id="5" />"""),
	        warningLevels.ignore);
	    assertEquivalentForms("Deserialization now supports 'rows=false'",
	        encapsulateTileString("<forest kind=\"trees\" id=\"``id``\" />"),
	        encapsulateTileString("<forest kind=\"trees\" rows=\"false\" id=\"``id``\" />"),
	        warningLevels.ignore);
	    assertSerialization("Forests can have acreage numbers", Forest("thirdForest",
			false, 6, decimalNumber(3).divided(decimalNumber(2))));
	}

	test
	shared void testFortressSerialization(
			randomlyGenerated(2) Integer id,
			enumeratedParameter(`class TownSize`) TownSize size) {
	    // Can't give player names because our test environment doesn't let us
	    // pass a set of players in
	    Player firstPlayer = PlayerImpl(1, "");
        assertSerialization("First test of ``size`` Fortress serialization",
            Fortress(firstPlayer, "one", id, size));
        assertSerialization("Second test of ``size`` Fortress serialization",
            Fortress(firstPlayer, "two", id, size));
	    Player secondPlayer = PlayerImpl(2, "");
	    Fortress five = Fortress(secondPlayer, "five", id, TownSize.small);
	    five.addMember(Unit(secondPlayer, "unitOne", "unitTwo", 1));
	    assertSerialization("Fifth test of Fortress serialization", five);
	    assertUnwantedChild<Fortress>("<fortress><hill /></fortress>", null);
	    assertMissingProperty<Fortress>("<fortress />", "owner",
	        Fortress(PlayerImpl(-1, ""), "", 0, TownSize.small));
	    assertMissingProperty<Fortress>("""<fortress owner="1" />""", "name",
	        Fortress(PlayerImpl(1, ""), "", 0, TownSize.small));
	    assertMissingProperty<Fortress>("""<fortress owner="1" name="name" />""",
	        "id", Fortress(PlayerImpl(1, ""), "name", 0, TownSize.small));
	    assertImageSerialization("Fortress image property is preserved", five);
	}

	test
	shared void testGiantSerialization(randomlyGenerated(3) Integer id) {
	    assertSerialization("Test of Giant serialization", Giant("one", id));
	    assertSerialization("Second test of Giant serialization", Giant("two", id));
	    assertUnwantedChild<Giant>("""<giant kind="hill"><hill /></giant>""", null);
	    assertMissingProperty<Giant>("<giant />", "kind", null);
	    assertMissingProperty<Giant>("""<giant kind="kind" />""", "id", Giant("kind", 0));
	    assertImageSerialization("Giant image property is preserved", Giant("three", id));
	}

	test
	shared void testGroundSerialization(randomlyGenerated(3) Integer id) {
	    assertSerialization("First test of Ground serialization", Ground(id, "one", true));
	    Point loc = Point(0, 0);
	    IMutableMapNG map = createSimpleMap(Point(1, 1),
	        loc->TileType.plains);
	    map.addFixture(loc, Ground(-1, "four", true));
	    assertSerialization("Test that reader handles ground as a fixture", map);
	    assertForwardDeserialization("Duplicate Ground ignored",
	        """<view current_turn="-1" current_player="-1">
	           <map version="2" rows="1" columns="1"><tile row="0" column="0" kind="plains">
	           <ground kind="four" exposed="true" /><ground kind="four" exposed="true" />
	           </tile></map></view>""",
	        map.equals);
	    map.addFixture(loc, Ground(-1, "five", false));
	    assertForwardDeserialization("Exposed Ground made main",
	        """<view current_turn="-1" current_player="-1">
	           <map version="2" rows="1" columns="1"><tile row="0" column="0" kind="plains">
	           <ground kind="five" exposed="false" /><ground kind="four" exposed="true" />
	           </tile></map></view>""",
	        map.equals);
	    assertForwardDeserialization("Exposed Ground left as main",
	        """<view current_turn="-1" current_player="-1">
	           <map version="2" rows="1" columns="1"><tile row="0" column="0" kind="plains">
	           <ground kind="four" exposed="true" /><ground kind="five" exposed="false" />
	           </tile></map></view>""",
	        map.equals);
	    assertUnwantedChild<Ground>(
	        """<ground kind="sand" exposed="true"><hill /></ground>""", null);
	    assertMissingProperty<Ground>("<ground />", "kind", null);
	    assertMissingProperty<Ground>("""<ground kind="ground" />""", "exposed", null);
	    assertDeprecatedProperty<Ground>(
	        """<ground ground="ground" exposed="true" />""", "ground", "kind", "ground",
	        Ground(-1, "ground", true));
	    assertImageSerialization("Ground image property is preserved",
	        Ground(id, "five", true));
	}

	test
	shared void testSimpleSerializationNoChildren() {
	    assertUnwantedChild<SimpleImmortal>("<djinn><troll /></djinn>", null);
	    assertUnwantedChild<SimpleImmortal>("<griffin><djinn /></griffin>", null);
	    assertUnwantedChild<Hill>("<hill><griffin /></hill>", null);
	    assertUnwantedChild<SimpleImmortal>("<minotaur><troll /></minotaur>", null);
	    assertUnwantedChild<Oasis>("<oasis><troll /></oasis>", null);
	    assertUnwantedChild<SimpleImmortal>("<ogre><troll /></ogre>", null);
	    assertUnwantedChild<SimpleImmortal>("<phoenix><troll /></phoenix>", null);
	    assertUnwantedChild<SimpleImmortal>("<simurgh><troll /></simurgh>", null);
	    assertUnwantedChild<SimpleImmortal>("<sphinx><troll /></sphinx>", null);
	    assertUnwantedChild<SimpleImmortal>("<troll><troll /></troll>", null);
	}

	test
	shared void testSimpleImageSerialization(randomlyGenerated(3) Integer id) {
		for (type in `class SimpleImmortal`.caseTypes.narrow<OpenClassType>()
				.map(OpenClassType.declaration)) {
			assert (is SimpleImmortal item = type.instantiate([], id));
			assertImageSerialization("``item.kind``  image property is preserved",
				item);
	    }
	    assertImageSerialization("Hill image property is preserved", Hill(id));
	    assertImageSerialization("Oasis image property is preserved", Oasis(id));
	}

	test
	shared void testSimpleSerialization(randomlyGenerated(3) Integer id) {
	    for (type in `class SimpleImmortal`.caseTypes.narrow<OpenClassType>()
					.map(OpenClassType.declaration)) {
	        assert (is SimpleImmortal item = type.instantiate([], 0));
	        assertSerialization("``item.kind``  serialization", type.instantiate([], id));
	        assertMissingProperty<SimpleImmortal>("<``item.kind`` />", "id", item);
	    }
	    assertSerialization("Hill serialization", Hill(id));
	    assertMissingProperty<Hill>("<hill />", "id", Hill(0));
	    assertSerialization("Oasis serialization", Oasis(id));
	    assertMissingProperty<Oasis>("<oasis />", "id", Oasis(0));
	}


	test
	shared void testCaveSerialization(randomlyGenerated(2) Integer dc,
			randomlyGenerated(2) Integer id) {
	    assertSerialization("Cave serialization test", Cave(dc, id));
	    assertUnwantedChild<Cave>("<cave dc=\"``dc``\"><troll /></cave>", null);
	    assertMissingProperty<Cave>("<cave />", "dc", null);
	    assertMissingProperty<Cave>("<cave dc=\"``dc``\" />", "id", Cave(dc, 0));
	    assertImageSerialization("Cave image property is preserved", Cave(dc, id));
	}

	test
	shared void testMineralSerialization(
			randomlyGenerated(2) Integer dc,
			randomlyGenerated(2) Integer id,
			parameters(`value minerals`) String kind,
			enumeratedParameter(`class Boolean`) Boolean exposed,
			enumeratedParameter(`class Boolean`) Boolean deprecatedWriter) {
	    MineralVein secondVein = MineralVein(kind, exposed, dc, id);
	    assertSerialization("Second MineralEvent serialization test", secondVein);
        assertDeprecatedProperty(createSerializedForm(secondVein, deprecatedWriter)
                .replace("kind", "mineral"), "mineral", "kind", "mineral", secondVein);
	    assertUnwantedChild<MineralVein>(
	        "<mineral kind=\"tin\" exposed=\"false\" dc=\"``dc``\"><hill/></mineral>", null);
	    assertMissingProperty<MineralVein>("<mineral dc=\"``dc``\" exposed=\"false\" />",
	        "kind", null);
	    assertMissingProperty<MineralVein>("""<mineral kind="gold" exposed="false" />""",
	        "dc", null);
	    assertMissingProperty<MineralVein>("<mineral dc=\"``dc``\" kind=\"gold\" />",
	        "exposed", null);
	    assertMissingProperty<MineralVein>(
	        "<mineral kind=\"``kind``\" exposed=\"``exposed``\" dc=\"``dc``\" />", "id",
	        MineralVein(kind, exposed, dc, 0));
	    assertImageSerialization("Mineral image property is preserved", secondVein);
	}

	test
	shared void testBattlefieldSerialization(
			randomlyGenerated(2) Integer dc,
			randomlyGenerated(2) Integer id) {
	    assertSerialization("Battlefield serialization test", Battlefield(dc, id));
	    assertUnwantedChild<Battlefield>("<battlefield dc=\"``dc``\"><hill /></battlefield>",
	        null);
	    assertMissingProperty<Battlefield>("<battlefield />", "dc", null);
	    assertMissingProperty<Battlefield>("<battlefield dc=\"``dc``\" />", "id",
	        Battlefield(dc, 0));
	    assertImageSerialization("Battlefield image property is preserved",
	        Battlefield(dc, id));
	}

	test
	shared void testCommaSeparators() {
	    assertEquivalentForms("ID numbers can contain commas", """<hill id="1,002" />""",
	        """<hill id="1002" />""", warningLevels.die);
	}

	test
	shared void testOldSandbars() =>
		assertUnsupportedTag("""<view current_player="-1" current_turn="-1">
		                        <map version="2" rows="1" columns="1">
		                        <tile row="0" column="0" kind="plains">
		                        <sandbar id="0" /></tile></map></view>""", "sandbar",
			createSimpleMap(Point(1, 1), Point(0, 0)->TileType.plains));
}

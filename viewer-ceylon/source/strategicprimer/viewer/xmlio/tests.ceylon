import ceylon.interop.java {
    javaClass,
    javaClassFromInstance
}
import ceylon.math.float {
    random
}
import ceylon.test {
    assertEquals,
    fail,
    assertTrue,
    assertFalse,
    parameters,
    test,
    assertNotEquals
}

import controller.map.formatexceptions {
    UnsupportedTagException,
    UnwantedChildException,
    MissingPropertyException,
    DeprecatedPropertyException,
    MissingChildException
}
import controller.map.iointerfaces {
    ISPReader
}
import controller.map.misc {
    DuplicateIDException
}

import java.io {
    StringReader,
    StringWriter
}
import java.lang {
    IllegalArgumentException,
    JInteger=Integer
}
import java.math {
    BigDecimal
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
    todo
}

import model.map {
    HasMutableImage,
    HasPortrait,
    Player,
    PlayerImpl,
    River,
    MapDimensionsImpl,
    PlayerCollection,
    Point,
    TileType,
    PointFactory,
    MutablePlayer
}
import strategicprimer.viewer.model.map {
    SPMapNG,
    IMutableMapNG,
    IMapNG
}
import model.map.fixtures {
    RiverFixture,
    TextFixture,
    Ground,
    Implement,
    ResourcePile
}
import model.map.fixtures.explorable {
    AdventureFixture,
    Portal,
    Cave,
    Battlefield
}
import model.map.fixtures.mobile {
    SimpleImmortal,
    Unit,
    Animal,
    IUnit,
    Worker,
    Centaur,
    Dragon,
    Fairy,
    Giant
}
import model.map.fixtures.mobile.worker {
    Job,
    Skill,
    WorkerStats
}
import model.map.fixtures.resources {
    StoneKind,
    StoneDeposit,
    Grove,
    FieldStatus,
    Meadow,
    Shrub,
    CacheFixture,
    MineralVein
}
import strategicprimer.viewer.model.map.fixtures.resources {
    Mine
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Sandbar,
    Oasis,
    Hill,
    Forest
}

import strategicprimer.viewer.drivers.advancement {
    races
}
import strategicprimer.viewer.model.map.fixtures.towns {
    TownStatus,
    TownSize,
    Village,
    Town,
    Fortification,
    City,
    Fortress
}
import strategicprimer.viewer.xmlio {
    testReaderFactory
}

import util {
    Warning,
    FatalWarningException,
    Quantity
}
JPath fakeFilename = JPaths.get("");
ISPReader oldReader = testReaderFactory.oldReader;
ISPReader newReader = testReaderFactory.newReader;
// TODO: In various methods, instead of taking "Boolean warning", take `Desideratum?`,
// either what the XML should deserialize to or null to indicate the error is fatal.

"Assert that the given XML will produce the given kind of warning and that it satisfies
 the given additional assertions. If it's only supposed to be a warning, asser that the
 XML will pass with warnings disabled but fail with warnings made fatal."
void assertFormatIssue<Desideratum, Expectation>(ISPReader reader, String xml,
        Boolean warning, Anything(Expectation) checks = (Expectation warning) {} )
        given Desideratum satisfies Object given Expectation satisfies Exception {
    if (warning) {
        try (stringReader = StringReader(xml)) {
            reader.readXML(fakeFilename, stringReader, javaClass<Desideratum>(),
                Warning.ignore);
        }
        try (stringReader = StringReader(xml)) {
            reader.readXML(fakeFilename, stringReader,
                javaClass<Desideratum>(), Warning.die);
            fail("Expected a fatal warning");
        } catch (FatalWarningException except) {
            assert (is Expectation cause = except.cause);
            checks(cause);
        }
    } else {
        try (stringReader = StringReader(xml)) {
            reader.readXML(fakeFilename, stringReader, javaClass<Desideratum>(),
                Warning.ignore);
            fail("Expected an exception to be thrown");
//        } catch (Expectation except) {
        } catch (Exception except) {
            if (is Expectation except) {
                checks(except);
            } else {
                throw (except);
            }
        }
    }
}
"Assert that reading the given XML will produce an [[UnsupportedTagException]]. If it's
 only supposed to be a warning, assert that it'll pass with warnings disabled but fail
 with warnings made fatal."
void assertUnsupportedTag<Type>(String xml, String tag, Boolean warning)
        given Type satisfies Object {
    for (reader in {oldReader, newReader}) {
        assertFormatIssue<Type,UnsupportedTagException>(reader, xml, warning,
                    (UnsupportedTagException except) => assertEquals(except.tag.localPart,
                tag, "Unsupported tag was the tag we expected"));
    }
}

"Assert that reading the given XML will produce an UnwantedChildException. If it's
 only supposed to be a warning, assert that it'll pass with warnings disabled but
 fail with warnings made fatal."
void assertUnwantedChild<Type>(String xml, Boolean warning)
        given Type satisfies Object {
    for (reader in {oldReader, newReader}) {
        assertFormatIssue<Type,UnwantedChildException>(reader, xml, warning);
    }
}

"Assert that reading the given XML will give a MissingPropertyException. If it's
 only supposed to be a warning, assert that it'll pass with warnings disabled but
 object with them made fatal."
void assertMissingProperty<Type>(String xml, String property,
        Boolean warning) given Type satisfies Object {
    for (reader in {oldReader, newReader}) {
        assertFormatIssue<Type,MissingPropertyException>(reader, xml, warning,
                    (except) => assertEquals(except.param, property,
                "Missing property should be the one we're expecting"));
    }
}

"Assert that reading the given XML will give a MissingChildException."
void assertMissingChild<Type>(String xml) given Type satisfies Object {
    for (reader in {oldReader, newReader}) {
        assertFormatIssue<Type,MissingChildException>(reader, xml, false);
    }
}

"Assert that reading the given XML will give a DeprecatedPropertyException. If it's only
 supposed to be a warning, assert that it'll pass with warnings disabled but object with
 them made fatal."
void assertDeprecatedProperty<Type>(String xml, String deprecated, String preferred,
        String tag, Boolean warning) given Type satisfies Object {
    for (reader in {oldReader, newReader}) {
        assertFormatIssue<Type,DeprecatedPropertyException>(reader, xml, warning,
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
    StringWriter writer = StringWriter();
    if (deprecated) {
        testReaderFactory.oldWriter.writeSPObject(writer, obj);
    } else {
        testReaderFactory.newWriter.writeSPObject(writer, obj);
    }
    return writer.string;
}
"Assert that the serialized form of the given object will deserialize without error."
void assertSerialization(String message, Object obj,
        Warning warner = Warning.die) {
    for (reader in {oldReader, newReader}) {
        for (deprecated in `Boolean`.caseValues) {
            try (stringReader = StringReader(createSerializedForm(obj,
                    deprecated))) {
                assertEquals(reader.readXML(fakeFilename, stringReader,
                    javaClassFromInstance(obj), warner), obj, message);
            }
        }
    }
}

"Assert that the given object, if serialized and deserialized, will have its image
 property preserved. We modify that property, but set it back to the original value before
 exiting this method."
void assertImageSerialization(String message, HasMutableImage obj) {
    String oldImage = obj.image;
    for (reader in {oldReader, newReader}) {
        for (deprecated in {true, false}) {
            obj.setImage("xyzzy");
            try (stringReader = StringReader(createSerializedForm(obj, deprecated))) {
                assertEquals(reader.readXML(fakeFilename, stringReader,
                    javaClassFromInstance(obj), Warning.ignore).image, obj.image,
                    message);
            }
            obj.setImage(obj.defaultImage);
            assertFalse(createSerializedForm(obj, deprecated).contains("image="),
                "Default image should not be written");
            obj.setImage("");
            assertFalse(createSerializedForm(obj, deprecated).contains("image="),
                "Empty image should not be written");
        }
    }
    obj.setImage(oldImage);
}

"Assert that the given object, if serialized and deserialized, will have its portrait
 property preserved. We modify that property, but set it back to the original value before
 exiting this method."
void assertPortraitSerialization(String message, HasPortrait obj) {
    String oldPortrait = obj.portrait;
    for (reader in {oldReader, newReader}) {
        for (deprecated in {true, false}) {
            obj.portrait = "xyzzy";
            try (stringReader = StringReader(createSerializedForm(obj, deprecated))) {
                assertEquals(reader.readXML(fakeFilename, stringReader,
                    javaClassFromInstance(obj), Warning.ignore).portrait, obj.portrait,
                    message);
            }
            obj.portrait = "";
            assertFalse(createSerializedForm(obj, deprecated).contains("portrait="),
                "Empty portrait should not be written");
        }
    }
    obj.portrait = oldPortrait;
}

"Assert that a deprecated idiom deserializes properly if warnings are ignored, but is
 warned about."
void assertDeprecatedDeserialization<Type>(
        "The message to use for assertions"
        String message,
        "The object we expect the deserialized form to equal"
        Type expected,
        "The serialized form"
        String xml,
        "The deprecated property"
        String property,
        "The preferred form of that property"
        String preferred,
        "What tag the property is on"
        String tag) given Type satisfies Object {
    for (reader in {oldReader, newReader}) {
        try (stringReader = StringReader(xml)) {
            assertEquals(reader.readXML(fakeFilename, stringReader,
                javaClass<Type>(), Warning.ignore), expected,
                message);
        }
    }
    assertDeprecatedProperty<Type>(xml, property, preferred, tag, true);
}

"Assert that a serialized form with a recommended but not required property missing
 deserializes properly if warnings are ignored, but is warned about."
void assertMissingPropertyDeserialization<Type>(
        "The assertion message"
        String message,
        "The object we expect the deserialized form to equal"
        Type expected,
        "The serialized form"
        String xml,
        "The missing property"
        String property) given Type satisfies Object {
    for (reader in {oldReader, newReader}) {
        try (stringReader = StringReader(xml)) {
            assertEquals(reader.readXML(fakeFilename, stringReader, javaClass<Type>(),
                Warning.ignore), expected, message);
        }
    }
    assertMissingProperty<Type>(xml, property, true);
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
    for (reader in {oldReader, newReader}) {
        try (stringReader = StringReader(xml)) {
            assertTrue(assertion(reader.readXML(fakeFilename, stringReader,
                javaClass<Type>(), Warning.die)), message);
        }
    }
}

"Assert that two serialized forms are equivalent, using both readers."
void assertEquivalentForms<Type>(
        "The assertion message to use"
        String message,
        "The first serialized form"
        String firstForm,
        "The second serialized form"
        String secondForm,
        "The warning level to use"
        Warning warningLevel) given Type satisfies Object {
    for (reader in {oldReader, newReader}) {
        try (firstReader = StringReader(firstForm),
                secondReader = StringReader(secondForm)) {
            assertEquals(reader.readXML(fakeFilename, secondReader,
                javaClass<Type>(), warningLevel), reader.readXML(fakeFilename,
                firstReader, javaClass<Type>(), warningLevel), message);
        }
    }
}

"Assert that a map is properly deserialized (by the main map-deserialization methods)."
void assertMapDeserialization(String message, IMapNG expected, String xml) {
    for (reader in {oldReader, newReader}) {
        assert (is IMapReader reader);
        try (stringReader = StringReader(xml)) {
            assertEquals(reader.readMapFromStream(fakeFilename, stringReader,
                Warning.die), expected, message);
        }
    }
}

"Assert that the given XML will produce warnings about duplicate IDs."
void assertDuplicateID(String xml) {
    for (reader in {oldReader, newReader}) {
        assertFormatIssue<Object, DuplicateIDException>(reader, xml, true);
    }
}

"Assert that a given piece of XML will fail to deserialize with XML format errors, not
 SP format errors."
void assertInvalid(String xml) {
    for (reader in {oldReader, newReader}) {
        assertFormatIssue<Object, NoSuchElementException|IllegalArgumentException>(reader,
            xml, false);
    }
}

"""Encapsulate the given string in a "tile" tag inside a "map" tag."""
String encapsulateTileString(String str) {
    return "<map version=\"2\" rows=\"2\" columns=\"2\">
            <tile row=\"1\" column=\"1\" kind=\"plains\">``str``</tile></map>";
}

// TODO: use Iterable.distinct instead of set().
{[TownStatus, String]*} villageParameters = `TownStatus`.caseValues.product(set {*races});

test
parameters(`value villageParameters`)
void testVillageSerialization(TownStatus status, String race) {
    Player owner = PlayerImpl(-1, "");
    assertSerialization("First Village serialization test, ``status``",
        Village(status, "villageOne", 1, owner, race));
    assertSerialization("Second Village serialization test, ``status``",
        Village(status, "villageTwo", 2, owner, race));
    Village thirdVillage = Village(status, "", 3, owner, race);
    for (deprecated in {true, false}) {
        assertMissingPropertyDeserialization(
            "Village serialization with no or empty name does The Right Thing", thirdVillage,
            createSerializedForm(thirdVillage, deprecated), "name");
    }
    assertUnwantedChild<Village>("<village status=\"``status``\">village /></village>",
        false);
    assertMissingProperty<Village>("<village />", "status", false);
    assertMissingProperty<Village>("<village name=\"name\" status=\"``status``\" />",
        "id", true);
    assertMissingProperty<Village>(
        "<village name=\"name\" status=\"``status``\" id=\"0\" />", "owner", true);
    assertImageSerialization("Village image property is preserved", thirdVillage);
    assertPortraitSerialization("Village portrait property is preserved", thirdVillage);
}

{[TownSize, TownStatus]*} townParameters =
        `TownSize`.caseValues.product(`TownStatus`.caseValues);

test
parameters(`value townParameters`)
void testCitySerialization(TownSize size, TownStatus status) {
    Player owner = PlayerImpl(-1, "");
    assertSerialization("First City serialization test, status ``status``, size ``size``",
        City(status, size, 10, "oneCity", 0, owner));
    assertSerialization(
        "Second City serialization test, status ``status``, size ``size``", City(status,
            size, 40, "twoCity", 1, owner));
    City thirdCity = City(status, size, 30, "", 3, owner);
    assertSerialization("Serialization of City without a name", thirdCity,
        Warning.ignore);
    for (deprecation in {true, false}) {
        assertMissingProperty<City>(createSerializedForm(thirdCity, deprecation), "name",
            true);
    }
    assertUnwantedChild<City>(
        "<city status=\"``status``\" size=\"``size``\" name=\"name\" dc=\"0\">
         <troll /></city>", false);
    assertMissingProperty<City>(
        "<city status=\"``status``\" size=\"``size``\"
         name=\"name\" dc=\"0\" id=\"0\" />", "owner", true);
    assertImageSerialization("City image property is preserved", thirdCity);
    assertPortraitSerialization("City portrait property is preserved", thirdCity);
}

test
parameters(`value townParameters`)
void testFortificationSerialization(TownSize size, TownStatus status) {
    Player owner = PlayerImpl(-1, "");
    assertSerialization("First Fortification serialization test, status ``status``, size ``size``",
        Fortification(status, size, 10, "one", 0, owner));
    assertSerialization(
        "Second Fortification serialization test, status ``status``, size ``size``",
        Fortification(status, size, 40, "two", 1, owner));
    Fortification thirdFort = Fortification(status, size, 30, "", 3, owner);
    assertSerialization("Serialization of Fortification without a name", thirdFort,
        Warning.ignore);
    for (deprecation in {true, false}) {
        assertMissingProperty<Fortification>(createSerializedForm(thirdFort, deprecation),
            "name", true);
    }
    assertUnwantedChild<Fortification>(
        "<fortification status=\"``status``\" size=\"``size``\" name=\"name\" dc=\"0\">
         <troll /></fortification>", false);
    assertMissingProperty<Fortification>(
        "<fortification status=\"``status``\" size=\"``size``\"
         name=\"name\" dc=\"0\" id=\"0\" />", "owner", true);
    assertImageSerialization("Fortification image property is preserved", thirdFort);
    assertPortraitSerialization("Fortification portrait property is preserved", thirdFort);
}

test
parameters(`value townParameters`)
void testTownSerialization(TownSize size, TownStatus status) {
    Player owner = PlayerImpl(-1, "");
    assertSerialization("First Town serialization test, status ``status``, size ``size``",
        Town(status, size, 10, "one", 0, owner));
    assertSerialization(
        "Second Town serialization test, status ``status``, size ``size``", Town(status,
            size, 40, "two", 1, owner));
    Town thirdTown = Town(status, size, 30, "", 3, owner);
    assertSerialization("Serialization of Town without a name", thirdTown,
        Warning.ignore);
    for (deprecation in {true, false}) {
        assertMissingProperty<Town>(createSerializedForm(thirdTown, deprecation), "name",
            true);
    }
    assertUnwantedChild<Town>(
        "<town status=\"``status``\" size=\"``size``\" name=\"name\" dc=\"0\">
         <troll /></town>", false);
    assertMissingProperty<Town>(
        "<town status=\"``status``\" size=\"``size``\"
         name=\"name\" dc=\"0\" id=\"0\" />", "owner", true);
    assertImageSerialization("Town image property is preserved", thirdTown);
    assertPortraitSerialization("Town portrait property is preserved", thirdTown);
}

{[StoneKind]*} stoneParameters = {*StoneKind.values()}.map((val) => [val]);

test
parameters(`value stoneParameters`)
void testStoneSerialization(StoneKind kind) {
    assertSerialization("First StoneDeposit test, kind: ``kind``",
        StoneDeposit(kind, 8, 1));
    assertSerialization("Second StoneDeposit test, kind: ``kind``",
        StoneDeposit(kind, 15, 2));
    assertImageSerialization("Stone image property is preserved",
        StoneDeposit(kind, 10, 3));
}

test
parameters(`value stoneParameters`)
void testOldStoneIdiom(StoneKind kind) {
    StoneDeposit thirdDeposit = StoneDeposit(kind, 10, 3);
    for (deprecated in {true, false}) {
        assertDeprecatedDeserialization<StoneDeposit>(
            "Deserialization of deprecated stone idiom", thirdDeposit,
            createSerializedForm(thirdDeposit, deprecated).replace("kind", "stone"),
            "stone", "kind", "stone");
    }
}

test
parameters(`value stoneParameters`)
void testStoneSerializationErrors(StoneKind kind) {
    assertUnwantedChild<StoneDeposit>(
        "<stone kind=\"``kind``\" dc=\"10\"><troll /></stone>", false);
    assertMissingProperty<StoneDeposit>("<stone kind=\"``kind``\" />", "dc", false);
    assertMissingProperty<StoneDeposit>("""<stone dc="10" />""", "kind", false);
    assertMissingProperty<StoneDeposit>("<stone kind=\"``kind``\" dc=\"0\" />", "id",
        true);
}

"A factory to encapsulate rivers in a simple map."
IMapNG encapsulateRivers(Point point, River* rivers) {
    IMutableMapNG retval = SPMapNG(MapDimensionsImpl(point.row + 1, point.col + 1, 2),
        PlayerCollection(), -1);
    retval.setBaseTerrain(point, TileType.plains);
    retval.addRivers(point, *rivers);
    return retval;
}

"Create a simple map."
IMutableMapNG createSimpleMap(Point dims, <Point->TileType>* terrain) {
    IMutableMapNG retval = SPMapNG(MapDimensionsImpl(dims.row, dims.col, 2),
        PlayerCollection(), -1);
    for (loc->type in terrain) {
        retval.setBaseTerrain(loc, type);
    }
    return retval;
}

test
void testPlayerSerialization() {
    assertSerialization("First Player serialization test", PlayerImpl(1, "one"));
    assertSerialization("Second Player serialization test", PlayerImpl(2, "two"));
    assertUnwantedChild<Player>(
        """<player code_name="one" number="1"><troll /></player>""", false);
    assertMissingProperty<Player>("""<player code_name="one" />""", "number", false);
    assertMissingProperty<Player>("""<player number="1" />""", "code_name", false);
}

{[River]*} riverParameters = {*River.values()}.map((val) => [val]);

test
parameters(`value riverParameters`)
void testSimpleRiverSerialization(River river) {
    assertSerialization("River alone", river);
    Point loc = PointFactory.point(0, 0);
    assertSerialization("River in tile", encapsulateRivers(loc, river));
}

test
void testRiverSerializationOne() {
    assertUnwantedChild<IMapNG>(encapsulateTileString("<lake><troll /></lake>"), false);
    assertMissingProperty<IMapNG>(encapsulateTileString("<river />"), "direction", false);
    Set<River> setOne = set { River.north, River.south };
    Set<River> setTwo = set { River.south, River.north };
    assertEquals(setOne, setTwo, "Rivers added in different order to set");
    assertEquals(RiverFixture(River.south, River.north),
        RiverFixture(River.north, River.south),
        "Rivers added in different order to fixture");
    RiverFixture fixOne = RiverFixture(River.north);
    fixOne.addRiver(River.south);
    RiverFixture fixTwo = RiverFixture(River.south);
    fixTwo.addRiver(River.north);
    assertEquals(fixOne, fixTwo, "Rivers added separately");
    assertEquals(
        encapsulateRivers(PointFactory.point(1, 1), River.north, River.south),
        encapsulateRivers(PointFactory.point(1, 1), River.north, River.south),
        "Tile equality with rivers");
    assertEquals(
        encapsulateRivers(PointFactory.point(1, 1), River.east, River.west),
        encapsulateRivers(PointFactory.point(1, 1), River.west, River.east),
        "Tile equality with different order of rivers");
    assertSerialization("Two rivers", encapsulateRivers(PointFactory.point(1, 2),
        River.north, River.south));
    assertInvalid(encapsulateTileString("""<river direction="invalid" />"""));
}

test
void testSimpleTileSerializtion() {
    assertSerialization("Simple Tile", createSimpleMap(PointFactory.point(1, 1),
        PointFactory.point(0, 0)->TileType.desert));
    IMutableMapNG firstMap = createSimpleMap(PointFactory.point(2, 2),
        PointFactory.point(1, 1)->TileType.plains);
    firstMap.addFixture(PointFactory.point(1, 1),
        SimpleImmortal(SimpleImmortal.SimpleImmortalKind.griffin, 1));
    assertSerialization("Tile with one fixture", firstMap);
    IMutableMapNG secondMap = createSimpleMap(PointFactory.point(3, 3),
        PointFactory.point(2, 2)->TileType.steppe);
    secondMap.addFixture(PointFactory.point(2, 2),
        Unit(PlayerImpl(1, ""), "unitOne", "firstUnit", 1));
    secondMap.setForest(PointFactory.point(2, 2), Forest("forestKind", true, 8));
    assertSerialization("Tile with two fixtures", secondMap);
    assertMissingProperty<IMapNG>(
        """<map version="2" rows="1" columns="1">
           <tile column="0" kind="plains" /></map>""", "row", false);
    assertMissingProperty<IMapNG>(
        """<map verision="2" rows="1" columns="1"><tile row="0" kind="plains" /></map>""",
        "column", false);
    assertMissingProperty<IMapNG>(
        """<map version="2" rows="1" columns="1"><tile row="0" column="0" /></map>""",
        "kind", true);
    assertUnwantedChild<IMapNG>(encapsulateTileString(
        """<tile row="2" column="0" kind="plains" />"""), false);
}

test
void testTileSerialization() {
    IMutableMapNG thirdMap = createSimpleMap(PointFactory.point(4, 4),
        PointFactory.point(3, 3)->TileType.jungle);
    Fortress fort = Fortress(PlayerImpl(2, ""), "fortOne", 1,
        TownSize.small);
    fort.addMember(Unit(PlayerImpl(2, ""), "unitTwo", "secondUnit", 2));
    thirdMap.addFixture(PointFactory.point(3, 3), fort);
    thirdMap.addFixture(PointFactory.point(3, 3),
        TextFixture("Random text here", 5));
    thirdMap.addRivers(PointFactory.point(3, 3), River.lake);
    assertSerialization("More complex tile", thirdMap);
    IMutableMapNG fourthMap = createSimpleMap(PointFactory.point(5, 5),
        PointFactory.point(4, 4)->TileType.plains);
    for (deprecated in {true, false}) {
        assertDeprecatedDeserialization<IMapNG>(
            "Deserialization of deprecated tile-type idiom", fourthMap,
            createSerializedForm(fourthMap, deprecated)
                .replace("kind", "type"), "type", "kind", "tile");
    }
}

test
void testTileSerializationTwo() {
    IMutableMapNG five = createSimpleMap(PointFactory.point(3, 4),
        PointFactory.point(2, 3)->TileType.jungle);
    five.addFixture(PointFactory.point(2, 3),
        Unit(PlayerImpl(2, ""), "explorer", "name one", 1));
    five.addFixture(PointFactory.point(2, 3),
        Unit(PlayerImpl(2, ""), "explorer", "name two", 2));
    assertEquals(five.getOtherFixtures(PointFactory.point(2, 3)).size, 2,
        "Just checking ...");
    assertSerialization("Multiple units should come through", five);
    String xmlTwoLogical =
            "<view xmlns=\"``ISPReader
                .namespace``\" current_player=\"-1\" current_turn=\"-1\">
             \t<map version=\"2\" rows=\"3\" columns=\"4\">
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
            "<view current_player=\"-1\" current_turn=\"-1\" xmlns=\"``ISPReader
                .namespace``\">
              \t<map columns=\"4\" rows=\"3\" version=\"2\">
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
    assertTrue(serializedForm == xmlTwoLogical || serializedForm == xmlTwoAlphabetical ||
            serializedForm == xmlTwoLogical.replace("\" />", "\"/>"),
        "Multiple units");
    assertEquals(createSerializedForm(createSimpleMap(PointFactory.point(1, 1),
            PointFactory.point(0, 0)->TileType.notVisible), true),
        "<view xmlns=\"``ISPReader.namespace`` current_player=\"-1\" current_turn=\"-1\">
         \t<map version=\"2\" rows=\"1\" columns=\"1\">
         \t</map>
         </view>
         ", "Shouldn't print empty not-visible tiles");
    String emptySerializedForm = createSerializedForm(createSimpleMap(PointFactory.point(1, 1),
        PointFactory.point(0, 0)->TileType.notVisible), false);
    String firstPossibility =
            "<view xmlns=\"``ISPReader
                .namespace``\" current_player=\"-1\" current_turn=\"-1\">
             \t<map version=\"2\" rows=\"1\" columns=\"1\">
             \t</map>
             </view>
             ";
    String secondPossibility =
            "<view current_player=\"-1\" current_turn=\"-1\" xmlns=\"``ISPReader
                .namespace``\">
             \t<map columns=\"1\" rows=\"1\" version=\"2\"/>
             </view>
             ";
    assertTrue(emptySerializedForm == firstPossibility ||
            emptySerializedForm == secondPossibility,
        "Shouldn't print empty not-visible tiles");
    // TODO: this last assertion doesn't belong in this test
    assertImageSerialization("Unit image property is preserved",
        Unit(PlayerImpl(5, ""), "herder",
            "herderName", 9));
}

test
void testTileSerializationThree() {
    IMutableMapNG six = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), 5);
    six.setMountainous(PointFactory.point(0, 0), true);
    six.setGround(PointFactory.point(0, 1), Ground(22, "basalt", false));
    six.setForest(PointFactory.point(1, 0), Forest("pine", false, 19));
    six.addFixture(PointFactory.point(1, 1), Animal("beaver", false, false, "wild", 18));
    for (deprecated in {true, false}) {
        assertMissingPropertyDeserialization("Not-visible tiles with contents are serialized",
            six, createSerializedForm(six, deprecated), "kind");
    }
}

test
void testSkippableSerialization() {
    assertEquivalentForms<IMapNG>("Two maps, one with row tags, one without",
        """<map rows="1" columns="1" version="2" />""",
        """<map rows="1" columns="1" version="2"><row /></map>""", Warning.die);
    assertEquivalentForms<IMapNG>("Two maps, one with future tag, one without",
        """<map rows="1" columns="1" version="2" />""",
        """<map rows="1" columns="1" version="2"><future /></map>""", Warning.ignore);
    assertUnsupportedTag<IMapNG>(
        """<map rows="1" columns="1" version="2"><future /></map>""", "future", true);
    assertUnsupportedTag<IMapNG>(
        """<map rows="1" columns="1" version="2">
           <tile row="0" column="0" kind="steppe"><futureTag /></tile></map>""",
        "futureTag", true);
}

test
void testMapSerialization() {
    assertUnwantedChild<IMapNG>(
        """<map rows="1" columns="1" version="2"><hill /></map>""", false);
    MutablePlayer player = PlayerImpl(1, "playerOne");
    player.setCurrent(true);
    IMutableMapNG firstMap = SPMapNG(MapDimensionsImpl(1, 1, 2),
        PlayerCollection(), -1);
    firstMap.addPlayer(player);
    Point loc = PointFactory.point(0, 0);
    firstMap.setBaseTerrain(loc, TileType.plains);
    assertSerialization("Simple Map serialization", firstMap);
    assertMissingProperty<IMapNG>("""<map version="2" columns="1" />""", "rows", false);
    assertMissingProperty<IMapNG>("""<map version="2" rows="1" />""", "columns", false);
    String originalFormOne = createSerializedForm(firstMap, false);
    String originalFormTwo = createSerializedForm(firstMap, true);
    firstMap.setBaseTerrain(PointFactory.point(1, 1), TileType.notVisible);
    assertEquals(createSerializedForm(firstMap, false), originalFormOne,
        "Explicitly not visible tile is not serialized");
    assertEquals(createSerializedForm(firstMap, true), originalFormTwo,
        "Explicitly not visible tile is not serialized");
    firstMap.setMountainous(loc, true);
    assertSerialization("Map with a mountainous point", firstMap);
    assertMissingProperty<IMapNG>(
        """<view current_turn="0"><map version="2" rows="1" columns="1" /></view>""",
        "current_player", false);
    assertMissingProperty<IMapNG>(
        """<view current_player="0"><map version="2" rows="1" columns="1" /></view>""",
        "current_turn", false);
    assertMissingChild<IMapNG>("""<view current_player="1" current_turn="0" />""");
    assertMissingChild<IMapNG>("""<view current_player="1" current_turn="13" />""");
    assertUnwantedChild<IMapNG>(
        """<view current_player="0" current_turn="0">
           <map version="2" rows="1" columns="1" />
           <map version="2" rows="1" columns="1" /></view>""", false);
    assertUnwantedChild<IMapNG>(
        """<view current_player="0" current_turn="0"><hill /></view>""", false);
    assertMapDeserialization("Proper deserialization of map without view tag", firstMap,
        """<map version="2" rows="1" columns="1" current_player="1">
           <player number="1" code_name="playerOne" />
           <row index="0"><tile row="0" column="0" kind="plains" /></row></map>""");
}

test
void testNamespacedSerialization() {
    MutablePlayer player = PlayerImpl(1, "playerOne");
    player.setCurrent(true);
    IMutableMapNG firstMap = SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 0);
    firstMap.addPlayer(player);
    Point loc = PointFactory.point(0, 0);
    firstMap.setBaseTerrain(loc, TileType.steppe);
    assertMapDeserialization("Proper deserialization of namespaced map", firstMap,
        "<map xmlns=\"``ISPReader.namespace``\" version=\"2\" rows=\"1\" columns=\"1\"
         current_player=\"1\"><player number=\"1\" code_name=\"playerOne\" /><row
         index=\"0\"><tile row=\"0\" column=\"0\" kind=\"steppe\" /></row></map>");
    assertMapDeserialization(
        "Proper deserialization of map if another namespace is declared default",
        firstMap,
        "<sp:map xmlns=\"xyzzy\" xmlns:sp=\"``ISPReader.namespace``\" version=\"2\"
         rows=\"1\" columns=\"1\" current_player=\"1\"><sp:player number=\"1\"
         code_name=\"playerOne\" /><sp:row index=\"0\"><sp:tile row=\"0\" column=\"0\"
         kind=\"steppe\" /></sp:row></sp:map>");
    assertMapDeserialization("Non-root other-namespace tags ignored", firstMap,
        "<map xmlns=\"``ISPReader.namespace``\" version=\"2\" rows=\"1\" columns=\"1\"
         current_player=\"1\" xmlns:xy=\"xyzzy\"><player number=\"1\"
         code_name=\"playerOne\" /><xy:xyzzy><row index=\"0\"><tile row=\"0\"
         column=\"0\" kind=\"steppe\"><xy:hill id=\"0\" /></tile></row></map>");
    try {
        assertMapDeserialization("Root tag must be in supported namespace", firstMap,
            """<map xmlns="xyzzy" version="2" rows="1" columns="1" current_player="1">
               <player number="1" code_name="playerOne" /><row index="0"><tile row="0"
               column="0" kind="steppe" /></row></map>""");
        fail("Map in an unsupported namespace shouldn't be accepted");
    } catch (UnwantedChildException except) {
        assertEquals(except.tag.localPart, "root",
            "'Tag' that had the unexpected child was what we expected");
        assertEquals(except.child, QName("xyzzy", "map"),
            "Unwanted child was the one we expected");
    } catch (XMLStreamException except) {
        assertEquals(except.message, "XML stream didn't contain a start element",
            "Reason for stream exception was as expected");
    }
    try {
        assertUnwantedChild<AdventureFixture>(
            """<adventure xmlns="xyzzy" id="1" brief="one" full="two" />""", false);
    } catch (XMLStreamException except) {
        // pass()
    }
}

test
void testInclude() {
    assertForwardDeserialization<SimpleImmortal>("Reading Ogre via <include>",
        """<include file="string:&lt;ogre id=&quot;1&quot; /&gt;" />""",
        SimpleImmortal(SimpleImmortal.SimpleImmortalKind.ogre, 1).equals);
}

test
void testDuplicateID() {
    assertDuplicateID(
        """<map version="2" rows="1" columns="1" current_player="1">
           <player number="1" code_name="playerOne" />
           <row index="0">
           <tile row="0" column="0" kind="steppe">
           <hill id="1" /><ogre id="1" /></tile></row></map>""");
}

test
void testRejectsInvalid() {
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
void testGroveSerialization() {
    variable Integer i = 0;
    variable String[] trees = ["firstGrove", "secondGrove", "thirdGrove", "fourthGrove"];
    for (fruit in {true, false}) {
        for (cultivation in {true, false}) {
            assertSerialization("Test of [[Grove]] serialization",
                Grove(fruit, cultivation, trees.first else "", i++));
            trees = trees.rest;
        }
    }
    assertUnwantedChild<Grove>("""<grove wild="true" kind="kind"><troll /></grove>""",
        false);
    assertMissingProperty<Grove>("<grove />", "cultivated", false);
    assertMissingProperty<Grove>("""<grove wild="false" />""", "kind", false);
    assertDeprecatedProperty<Grove>("""<grove cultivated="true" tree="tree" id="0" />""",
        "tree", "kind", "grove", true);
    assertMissingProperty<Grove>("""<grove cultivated="true" kind="kind" />""", "id",
        true);
    assertDeprecatedProperty<Grove>("""<grove wild="true" kind="tree" id="0" />""",
        "wild", "cultivated", "grove", true);
    assertEquivalentForms<Grove>("Assert that wild is the inverse of cultivated",
        """<grove wild="true" kind="tree" id="0" />""",
        """<grove cultivated="false" kind="tree"id="0" />""", Warning.ignore);
    assertImageSerialization("Grove image property is preserved", Grove(false, false,
        "five", 5));
}

test
void testMeadowSerialization() {
    variable Integer i = 0;
    variable {FieldStatus*} statuses = {*FieldStatus.values()}.cycled;
    variable String[] names = ["first", "second", "third", "fourth"];
    for (field in {true, false}) {
        for (cultivated in {true, false}) {
            assertSerialization("Test of [[Meadow]] serialization",
                Meadow(names.first, field, cultivated, i++, statuses.first));
            names = names.rest;
            statuses = statuses.rest;
        }
    }
    assertUnwantedChild<Meadow>(
        """<meadow kind="flax" cultivated="false"><troll /></meadow>""", false);
    assertMissingProperty<Meadow>("""<meadow cultivated="false" />""", "kind", false);
    assertMissingProperty<Meadow>("""<meadow kind="flax" />""", "cultivated", false);
    assertMissingProperty<Meadow>("""<field kind="kind" cultivated="true" />""", "id",
        true);
    assertMissingProperty<Meadow>("""<field kind="kind" cultivated="true" id="0" />""",
        "status", true);
    assertImageSerialization("Meadow image property is preserved",
        Meadow("five", false, false, 5, FieldStatus.fallow));
}

test
void testMineSerialization() {
    variable Integer i = 0;
    variable String[] names = ["one", "two", "three", "four"];
    for (status in `TownStatus`.caseValues) {
        assert (exists name = names.first);
        assertSerialization("Test of [[Mine]] serialization",
            Mine(name, status, i++));
        names = names.rest;
    }
    Mine mine = Mine("four", TownStatus.ruined, 4);
    for (deprecation in {true, false}) {
        assertDeprecatedDeserialization("Deprecated [[Mine]] idiom", mine,
            createSerializedForm(mine, deprecation).replace("kind=", "product="),
            "product", "kind", "mine");
    }
    assertUnwantedChild<Mine>("""<mine kind="gold" status="active"><troll /></mine>""",
        false);
    assertMissingProperty<Mine>("""<mine status="active" />""", "kind", false);
    assertMissingProperty<Mine>("""<mine kind="gold" />""", "status", false);
    assertMissingProperty<Mine>("""<mine kind="kind" status="active" />""", "id", true);
    assertImageSerialization("Mine image property is preserved", mine);
}

test
void testShrubSerialization() {
    assertSerialization("First test of Shrub serialization", Shrub("one", 0));
    Shrub secondShrub = Shrub("two", 2);
    assertSerialization("Second test of Shrub serialization", secondShrub);
    for (deprecation in {true, false}) {
        assertDeprecatedDeserialization("Deserialization of mangled shrub", secondShrub,
            createSerializedForm(secondShrub, deprecation).replace("kind", "shrub"),
            "shrub", "kind", "shrub");
    }
    assertUnwantedChild<Shrub>("""<shrub kind="shrub"><troll /></shrub>""", false);
    assertMissingProperty<Shrub>("<shrub />", "kind", false);
    assertMissingProperty("""<shrub kind="kind" />""", "id", true);
    assertImageSerialization("Shrub image property is preserved", secondShrub);
}

test
void testTextSerialization() {
    assertSerialization("First test of [[TextFixture]] serialization",
        TextFixture("one", -1));
    assertSerialization("Second test of [[TextFixture]] serialization",
        TextFixture("two", 2));
    TextFixture third = TextFixture("three", 10);
    assertSerialization("Third test of [[TextFixture]] serialization", third);
    assertUnwantedChild<TextFixture>("""<text turn="1"><troll /></text>""", false);
    assertImageSerialization("Text image property is preserved", third);
    IMutableMapNG wrapper = createSimpleMap(PointFactory.point(1, 1),
        PointFactory.point(0, 0)->TileType.plains);
    wrapper.addFixture(PointFactory.point(0, 0), TextFixture("one", -1));
    assertForwardDeserialization("Deprecated text-in-map still works",
        """<map version="2" rows="1" columns="1">
           <tile row="0" column="0" kind="plains">one</tile></map>""", wrapper.equals);
}

test
void testUnitHasRequiredProperties() {
    assertMissingProperty<IUnit>("""<unit name="name" />""", "owner", true);
    assertMissingProperty<IUnit>("""<unit owner="" name="name" />""", "owner", true);
    assertMissingProperty<IUnit>("""<unit owner="1" name="name" id="0" />""", "kind",
        true);
    assertMissingProperty<IUnit>("""<unit onwer="1" kind="" name="name" id="0" />""",
        "kind", true);
}

test
void testUnitWarnings() {
    assertUnwantedChild<IUnit>("<unit><unit /></unit>", false);
    IUnit firstUnit = Unit(PlayerImpl(1, ""), "unitType",
        "unitName", 1);
    for (deprecated in {true, false}) {
        assertDeprecatedDeserialization(
            """Deserialize propertly with deprecated use of "type" for unit kind""",
            firstUnit,
            createSerializedForm(firstUnit, deprecated).replace("kind", "type"), "type",
            "kind", "unit");
    }
    assertMissingProperty<IUnit>("""<unit owner="2" kind="unit" />""", "name", true);
    assertSerialization("Deserialize unit with no kind properly", Unit(PlayerImpl(2, ""),
        "", "name", 2), Warning.ignore);
    assertMissingPropertyDeserialization("Deserialize unit with no owner properly",
        Unit(PlayerImpl(-1, ""), "kind", "unitThree", 3),
        """<unit kind="kind" name="unitThree" id="3" />""", "owner");
    IUnit fourthUnit = Unit(PlayerImpl(4, ""), "unitKind", "", 4);
    for (deprecated in {true, false}) {
        assertMissingPropertyDeserialization("Deserialize unit with no name properly",
            fourthUnit, createSerializedForm(fourthUnit, deprecated), "name");
    }
    assertMissingPropertyDeserialization("Deserialize unit with empty name properly",
        fourthUnit, """<unit owner="4" kind="unitKind" name="" id="4" />""", "name");
    assertMissingProperty<IUnit>("""<unit owner="1" kind="kind" name="name" />""", "id",
        true);
}

test
void testUnitMemberSerialization() {
    IUnit firstUnit = Unit(PlayerImpl(1, ""), "unitType", "unitName", 1);
    firstUnit.addMember(Animal("animal", false, true, "wild", 2));
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
                                   <worker name="name" id="1" /></tile></map>""", false);
    assertPortraitSerialization("Worker portrait property is preserved", secondWorker);
}

test
void testOrdersSerialization() {
    Player player = PlayerImpl(0, "");
    IUnit firstUnit = Unit(player, "kind of unit", "name of unit", 2);
    IUnit secondUnit = Unit(player, "kind of unit", "name of unit", 2);
    secondUnit.setOrders(-1, "some orders");
    assertEquals(firstUnit, secondUnit, "Orders have no effect on equals");
    assertSerialization("Orders don't mess up deserialization", secondUnit);
    for (deprecated in {true, false}) {
        assertTrue(createSerializedForm(secondUnit, deprecated).contains("some orders"),
            "Serialized form contains orders");
    }
    secondUnit.setOrders(2, "some other orders");
    for (deprecated in {true, false}) {
        String serialized = createSerializedForm(secondUnit, deprecated);
        assertTrue(serialized.contains("some orders"),
            "Serialized form contains original orders");
        assertTrue(serialized.contains("some other orders"),
            "Serialized form contains new orders too.");
    }
    secondUnit.setResults(3, "some results");
    for (deprecated in {true, false}) {
        assertTrue(createSerializedForm(secondUnit, deprecated).contains("some results"),
            "Serialized form contains results");
    }
    secondUnit.setResults(-1, "some other results");
    for (deprecated in {true, false}) {
        String serialized = createSerializedForm(secondUnit, deprecated);
        assertTrue(serialized.contains("some results"),
            "Serialized form contains original results");
        assertTrue(serialized.contains("some other results"),
            "Serialized form contains new results too");
    }
    assertForwardDeserialization<IUnit>("Orders can be read without tags",
        """<unit name="name" kind="kind" id="1" owner="-1">Orders orders</unit>""",
                (unit) => unit.getOrders(-1) == "Orders orders");
}

test
void testQuoting() {
    Player player = PlayerImpl(0, "");
    Unit unit = Unit(player, "kind of unit", "name of unit", 2);
    unit.setOrders(4, """I <3 & :( "meta'""");
    unit.setResults(5, "2 --> 1");
    assertSerialization(
        "Serialization preserves XML meta-characters in orders and results", unit);
    unit.setOrders(3, "1 << 2");
    unit.setResults(-1, "\"quote this\"");
    assertSerialization("This works even if such characters occur more than once", unit);
    unit.setName("\"Can't quote this ><>&\"");
    assertSerialization("Data stored in XML attributes is quoted", unit);
}

test
void testUnitPortraitSerialization() {
    Unit unit = Unit(PlayerImpl(1, ""), "kind", "name", 2);
    unit.portrait = "portraitFile";
    assertSerialization("Portrait doesn't mess up serialization", unit);
    for (deprecation in {true, false}) {
        assertTrue(createSerializedForm(unit, deprecation).contains("portraitFile"),
            "Serialized form contains portrait");
    }
    assertPortraitSerialization("Unit portrait property is preserved", unit);
}

test
void testAdventureSerialization() {
    Player independent = PlayerImpl(1, "independent");
    AdventureFixture first = AdventureFixture(independent, "first hook brief",
        "first hook full", 1);
    AdventureFixture second = AdventureFixture(PlayerImpl(2, "player"),
        "second hook brief", "second hook full", 2);
    assertNotEquals(first, second, "Two different hooks are not equal");
    IMutableMapNG wrapper = createSimpleMap(PointFactory.point(1, 1),
        PointFactory.point(0, 0)->TileType.plains);
    wrapper.addPlayer(independent);
    wrapper.addFixture(PointFactory.point(0, 0), first);
    assertSerialization("First [[AdventureFixture]] serialization test", wrapper);
    assertSerialization("Second [[AdventureFixture]] serialization test", second);
    assertSerialization("[[AdventureFixture]] with empty descriptions",
        AdventureFixture(PlayerImpl(3, "third"), "", "", 4));
    Portal third = Portal("portal dest", PointFactory.point(1, 2), 3);
    Portal fourth = Portal("portal dest two", PointFactory.point(2, 1), 4);
    assertNotEquals(third, fourth, "Two different portals are not equal");
    wrapper.addFixture(PointFactory.point(0, 0), third);
    assertSerialization("First [[Portal]] serialization test", wrapper);
    assertSerialization("Second [[Portal]] serialization test", fourth);
}

test
void testFortressMemberSerialization() {
    Fortress firstFort = Fortress(PlayerImpl(1, ""), "fortName", 1, TownSize.small);
    firstFort.addMember(Implement("implKind", 2));
    assertSerialization("[[Fortress]] can have an [[Implement]] as a member", firstFort);
    firstFort.addMember(ResourcePile(3, "generalKind", "specificKind",
        Quantity(JInteger(10), "each")));
    assertSerialization("[[Fortress]] can have a [[ResourcePile]] as a member",
        firstFort);
    ResourcePile resource = ResourcePile(4, "generalKind", "specificKind",
        Quantity(JInteger(15), "pounds"));
    resource.created = 5;
    assertSerialization("Resource pile can know what turn it was created", resource);
    assertSerialization("Resource pile can have non-integer quantity", ResourcePile(5, "resourceKind", "specificKind2",
        Quantity(BigDecimal("1.5"), "cubic feet")));
}

test
void testAnimalSerialization() {
    variable Integer i = 0;
    String[] statuses = ["wild", "semi-domesticated", "domesticated", "tame"];
    for (tracks in {false, true}) {
        for (talking in {false, true}) {
            assertSerialization("Test of [[Animal]] serialization",
                Animal("animalKind", tracks, talking,
                    statuses[i], i));
            i++;
        }
    }
    assertUnwantedChild<Animal>("""<animal kind="animal"><troll /></animal>"""", false);
    assertMissingProperty<Animal>("<animal />", "kind", false);
    assertForwardDeserialization<Animal>("Forward-looking in re talking",
        """<animal kind="animalFive" talking="false" id="3" />""",
        Animal("animalFive", false, false, "wild", 3).equals);
    assertMissingProperty<Animal>("""<animal kind="animalSix" talking="true" />""", "id",
        true);
    assertMissingProperty<Animal>("""<animal kind="animalEight" id="nonNumeric" />""",
        "id", false);
    assertForwardDeserialization<Animal>("Explicit default status of animal",
        """<animal kind="animalSeven" status="wild", id="4" />""",
        Animal("animalSeven", false, false, "wild", 4).equals);
    assertImageSerialization("Animal image property is preserved",
        Animal("animalFour", true, true, "status", 8));
    assertForwardDeserialization<Animal>("Namespaced attribute",
        "<animal xmlns:sp=\"``ISPReader.namespace``\" sp:kind=\"animalNine\"
         sp:talking=\"true\" sp:traces=\"true\" sp:status=\"tame\" sp:id=\"5\" />",
        Animal("animalNine", true, true, "tame", 5).equals);
}

test
void testCacheSerialization() {
    assertSerialization("First test of Cache serialization", CacheFixture("kindOne",
        "contentsOne", 1));
    assertSerialization("Second test of Cache serialization", CacheFixture("kindTwo",
        "contentsTwo", 2));
    assertUnwantedChild<CacheFixture>(
        """<cache kind="kind" contents="contents"><troll /></cache>""", false);
    assertMissingProperty<CacheFixture>("""<cache contents="contents" />""", "kind",
        false);
    assertMissingProperty<CacheFixture>("""<cache kind="kind" />""", "contents", false);
    assertMissingProperty<CacheFixture>("""<cache kind="kind" contents="contents" />""",
        "id", true);
    assertImageSerialization("Cache image property is preserved",
        CacheFixture("kindThree", "contentsThree", 3));
}

test
void testCentaurSerialization() {
    assertSerialization("First test of Centaur serialization",
        Centaur("firstCentaur", 0));
    assertSerialization("Second test of Centaur serialization",
        Centaur("secondCentaur", 1));
    assertUnwantedChild<Centaur>("""<centaur kind="forest"><troll /></centaur>""",
        false);
    assertMissingProperty<Centaur>("<centaur />", "kind", false);
    assertMissingProperty<Centaur>("""<centaur kind="kind" />""", "id", true);
    assertImageSerialization("Centaur image property is preserved",
        Centaur("thirdCentaur", 2));
}

test
void testDragonSerialization() {
    assertSerialization("First test of Dragon serialization", Dragon("", 1));
    assertSerialization("Second test of Dragon serialization", Dragon("secondDragon", 2));
    assertUnwantedChild<Dragon>("""<dragon kind="ice"><hill /></dragon>""", false);
    assertMissingProperty<Dragon>("<dragon />", "kind", false);
    assertMissingProperty<Dragon>("""<dragon kind="kind" />""", "id", true);
    assertImageSerialization("Dragon image property is preserved",
        Dragon("thirdDragon", 3));
}

test
void testFairySerialization() {
    assertSerialization("First test of Fairy serialization", Fairy("oneFairy", 1));
    assertSerialization("Second test of Fairy serialization", Fairy("twoFairy", 2));
    assertUnwantedChild<Fairy>("""<fairy kind="great"><hill /></fairy>""", false);
    assertMissingProperty<Fairy>("<fairy />", "kind", false);
    assertMissingProperty<Fairy>("""<fairy kind="kind" />""", "id", true);
    assertImageSerialization("Fairy image property is preserved", Fairy("threeFairy", 3));
}

test
void testForestSerialization() {
    assertSerialization("First test of Forest serialization",
        Forest("firstForest", false, 1));
    assertSerialization("Second test of Forest serialization",
        Forest("secondForest", true, 2));
    assertUnwantedChild<Forest>("""<forest kind="trees"><hill /></forest>""", false);
    assertMissingProperty<Forest>("<forest />", "kind", false);
    assertImageSerialization("Forest image property is preserved",
        Forest("thirdForest", true, 3));
    Point loc = PointFactory.point(0, 0);
    IMutableMapNG map = createSimpleMap(PointFactory.point(1, 1),
        loc->TileType.plains);
    map.setForest(loc, Forest("trees", false, 4));
    map.addFixture(loc, Forest("secondForest", true, 5));
    assertSerialization("Map with multiple Forests on a tile", map);
    assertEquivalentForms<IMutableMapNG>("Duplicate Forests ignored",
        encapsulateTileString(
            """<forest kind="trees" id="4" />
               <forest kind="second" rows="true" id="5" />"""),
        encapsulateTileString(
            """<forest kind="trees" id="4" />
               <forest kind="trees" id="4" />
               <forest kind="second" rows="true" id="5" />"""),
        Warning.ignore);
}

test
void testFortressSerialization() {
    // Can't give player names because our test environment doesn't let us
    // pass a set of players in
    Player firstPlayer = PlayerImpl(1, "");
    assertSerialization("First test of Fortress serialization",
        Fortress(firstPlayer, "one", 1, TownSize.small));
    assertSerialization("Second test of Fortress serialization",
        Fortress(firstPlayer, "two", 2, TownSize.medium));
    Player secondPlayer = PlayerImpl(2, "");
    assertSerialization("Third test of Fortress serialization",
        Fortress(secondPlayer, "three", 3, TownSize.large));
    assertSerialization("Fourth test of Fortress serialization",
        Fortress(secondPlayer, "four", 4, TownSize.small));
    Fortress five = Fortress(secondPlayer, "five", 5, TownSize.small);
    five.addMember(Unit(secondPlayer, "unitOne", "unitTwo", 1));
    assertSerialization("Fifth test of Fortress serialization", five);
    assertUnwantedChild<Fortress>("<fortress><hill /></fortress>", false);
    assertMissingProperty<Fortress>("<fortress />", "owner", true);
    assertMissingProperty<Fortress>("""<fortress owner="1" />""", "name", true);
    assertMissingProperty<Fortress>("""<fortress owner="1" name="name" />""",
        "id", true);
    assertImageSerialization("Fortress image property is preserved", five);
}

test
void testGiantSerialization() {
    assertSerialization("First test of Giant serialization", Giant("one", 1));
    assertSerialization("Second test of Giant serialization", Giant("two", 2));
    assertUnwantedChild<Giant>("""<giant kind="hill"><hill /></giant>""", false);
    assertMissingProperty<Giant>("<giant />", "kind", false);
    assertMissingProperty<Giant>("""<giant kind="kind" />""", "id", true);
    assertImageSerialization("Giant image property is preserved", Giant("three", 3));
}

test
void testGroundSerialization() {
    assertSerialization("First test of Ground serialization", Ground(1, "one", true));
    assertSerialization("Second test of Ground serialization", Ground(2, "two", true));
    assertSerialization("Third test of Ground serialization", Ground(3, "three", false));
    Point loc = PointFactory.point(0, 0);
    IMutableMapNG map = createSimpleMap(PointFactory.point(1, 1),
        loc->TileType.plains);
    map.setGround(loc, Ground(-1, "four", true));
    assertSerialization("Test that reader handles ground as a fixture", map);
    assertForwardDeserialization("Duplicate Ground ignored",
        """<view current_turn="-1"><map version="2" rows="1" columns="1">
           <tile row="0" column="0" kind="plains"><ground kind="four" exposed="true" />
           <ground kind="four" exposed="true" /></tile></map></view>""",
        map.equals);
    map.addFixture(loc, Ground(-1, "five", false));
    assertForwardDeserialization("Exposed Ground made main",
        """<view current_turn="-1"><map version="2" rows="1" columns="1">
           <tile row="0" column="0" kind="plains"><ground kind="five" exposed="false" />
           <ground kind="four" exposed="true" /></tile></map></view>""",
        map.equals);
    assertForwardDeserialization("Exposed Ground left as main",
        """<view current_turn="-1"><map version="2" rows="1" columns="1">
           <tile row="0" column="0" kind="plains"><ground kind="four" exposed="true" />
           <ground kind="five" exposed="false" /></tile></map></view>""",
        map.equals);
    assertUnwantedChild<Ground>(
        """<ground kind="sand" exposed="true"><hill /></ground>""", false);
    assertMissingProperty<Ground>("<ground />", "kind", false);
    assertMissingProperty<Ground>("""<ground kind="ground" />""", "exposed", false);
    assertDeprecatedProperty<Ground>(
        """<ground ground="ground" exposed="true" />""", "ground", "kind", "ground",
        true);
    assertImageSerialization("Ground image property is preserved",
        Ground(5, "five", true));
}

test
void testSimpleSerializationNoChildren() {
    assertUnwantedChild<SimpleImmortal>("<djinn><troll /></djinn>", false);
    assertUnwantedChild<SimpleImmortal>("<griffin><djinn /></griffin>", false);
    assertUnwantedChild<Hill>("<hill><griffin /></hill>", false);
    assertUnwantedChild<SimpleImmortal>("<minotaur><troll /></minotaur>", false);
    assertUnwantedChild<Oasis>("<oasis><troll /></oasis>", false);
    assertUnwantedChild<SimpleImmortal>("<ogre><troll /></ogre>", false);
    assertUnwantedChild<SimpleImmortal>("<phoenix><troll /></phoenix>", false);
    assertUnwantedChild<Sandbar>("<sandbar><troll /></sandbar>", false);
    assertUnwantedChild<SimpleImmortal>("<simurgh><troll /></simurgh>", false);
    assertUnwantedChild<SimpleImmortal>("<sphinx><troll /></sphinx>", false);
    assertUnwantedChild<SimpleImmortal>("<troll><troll /></troll>", false);
}

test
void testSimpleImageSerialization() {
    for (kind in SimpleImmortal.SimpleImmortalKind.values()) {
        assertImageSerialization("``kind``  image property is preserved",
            SimpleImmortal(kind, (random() * 1048576).integer));
    }
    assertImageSerialization("Hill image property is preserved", Hill(3));
    assertImageSerialization("Oasis image property is preserved", Oasis(3));
    assertImageSerialization("Sandbar image property is preserved", Sandbar(3));
}

test
void testSimpleSerialization() {
    for (kind in SimpleImmortal.SimpleImmortalKind.values()) {
        assertSerialization("``kind``  serialization",
            SimpleImmortal(kind, (random() * 1048576).integer));
    }
    assertMissingProperty<SimpleImmortal>("<djinn />", "id", true);
    assertMissingProperty<SimpleImmortal>("<griffin />", "id", true);
    assertSerialization("Hill serialization", Hill(1));
    assertSerialization("Hill serialization", Hill(2));
    assertMissingProperty<Hill>("<hill />", "id", true);
    assertMissingProperty<SimpleImmortal>("<minotaur />", "id", true);
    assertSerialization("Oasis serialization", Oasis(1));
    assertSerialization("Oasis serialization", Oasis(2));
    assertMissingProperty<Oasis>("<oasis />", "id", true);
    assertMissingProperty<SimpleImmortal>("<ogre />", "id", true);
    assertMissingProperty<SimpleImmortal>("<phoenix />", "id", true);
    assertSerialization("Sandbar serialization", Sandbar(1));
    assertSerialization("Sandbar serialization", Sandbar(2));
    assertMissingProperty<Sandbar>("<sandbar />", "id", true);
    assertMissingProperty<SimpleImmortal>("<simurgh />", "id", true);
    assertMissingProperty<SimpleImmortal>("<sphinx />", "id", true);
    assertMissingProperty<SimpleImmortal>("<troll />", "id", true);
}

test
todo("Randomly generate ID and DC numbers")
void testCaveSerialization() {
    assertSerialization("First CaveEvent serialization test, reflection", Cave(10, 0));
    assertSerialization("Second CaveEvent serialization test, reflection", Cave(30, 1));
    assertUnwantedChild<Cave>("""<cave dc="10"><troll /></cave>""", false);
    assertMissingProperty<Cave>("<cave />", "dc", false);
    assertMissingProperty<Cave>("""<cave dc="10" />""", "id", true);
    assertImageSerialization("Cave image property is preserved", Cave(20, 2));
}

test
void testMineralSerialization() {
    assertSerialization("First MineralEvent serialization test",
        MineralVein("one", true, 10, 1));
    MineralVein secondVein = MineralVein("two", false, 35, 2);
    assertSerialization("Second MineralEvent serialization test", secondVein);
    for (deprecation in {true, false}) {
        assertDeprecatedDeserialization("Deserialization of deprecated Mineral idiom",
            secondVein, createSerializedForm(secondVein, deprecation)
                .replace("kind", "mineral"),
            "mineral", "kind", "mineral");
    }
    assertUnwantedChild<MineralVein>(
        """<mineral kind="gold" exposed="false" dc="0"><troll /></mineral>""", false);
    assertMissingProperty<MineralVein>("""<mineral dc="0" exposed="false" />""", "kind",
        false);
    assertMissingProperty<MineralVein>("""<mineral kind="gold" exposed="false" />""",
        "dc", false);
    assertMissingProperty<MineralVein>("""<mineral dc="0" kind="gold" />""",
        "exposed", false);
    assertMissingProperty<MineralVein>(
        """<mineral kind="kind" exposed="true" dc="0" />""", "id", true);
    assertImageSerialization("Mineral image property is preserved", secondVein);
}

test
void testBattlefieldSerialization() {
    assertSerialization("First BattlefieldEvent serialization test", Battlefield(10, 0));
    assertSerialization("Second BattlefieldEvent serialization test",
        Battlefield(30, 1));
    assertUnwantedChild<Battlefield>("""<battlefield dc="10"><troll /></battlefield>""",
        false);
    assertMissingProperty<Battlefield>("<battlefield />", "dc", false);
    assertMissingProperty<Battlefield>("""<battlefield dc="10" />""", "id", true);
    assertImageSerialization("Battlefield image property is preserved",
        Battlefield(20, 2));
}
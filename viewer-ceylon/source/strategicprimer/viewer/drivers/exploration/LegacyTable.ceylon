import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.interop.java {
    CeylonIterator,
    JavaSet,
    javaString
}

import java.lang {
    JString=String
}
import java.util {
    JSet=Set
}
import java.util.stream {
    Stream
}

import model.exploration.old {
    EncounterTable
}
import model.map {
    IEvent,
    TileFixture,
    IFixture,
    Player,
    TileType,
    MapDimensions,
    Point,
    PlayerImpl
}
import model.map.fixtures.explorable {
    Battlefield,
    Cave
}
import model.map.fixtures.resources {
    MineralKind,
    MineralVein,
    StoneKind,
    StoneDeposit
}
import model.map.fixtures.towns {
    TownStatus,
    TownSize,
    City,
    Fortification,
    Town
}
"""An [[EncounterTable]] for legacy "events"."""
class LegacyTable() satisfies EncounterTable {
    {IEvent*} createEvents() {
        Player player = PlayerImpl(-1, "Independent");
        MutableList<IEvent> retval = ArrayList<IEvent>();
        retval.add(Battlefield(0, -1));
        retval.add(Cave(0, -1));
        for (status in TownStatus.values()) {
            for (size in TownSize.values()) {
                retval.add(City(status, size, 0, "", 0, player));
                retval.add(Fortification(status, size, 0, "", 0, player));
                retval.add(Town(status, size, 0, "", 0, player));
            }
        }
        for (mineral in MineralKind.values()) {
            retval.add(MineralVein(mineral.string, true, 0, 0));
            retval.add(MineralVein(mineral.string, false, 0, 0));
        }
        object temp satisfies IEvent {
            shared actual TileFixture copy(Boolean zero) => nothing;
            shared actual Integer dc => nothing;
            shared actual Boolean equalsIgnoringID(IFixture? fix) => nothing;
            shared actual Integer id => nothing;
            shared actual String plural() => nothing;
            shared actual String shortDesc() => nothing;
            shared actual String text => "Nothing intersting here ...";
        }
        retval.add(temp);
        for (stone in StoneKind.values()) {
            retval.add(StoneDeposit(stone, 0, 0));
        }
        return retval;
    }
    {String*} data = createEvents().map(IEvent.text);
    shared actual String generateEvent(Point point, TileType terrain,
            Stream<TileFixture> fixtures, MapDimensions mapDimensions) {
        object temp satisfies Iterable<TileFixture> {
            shared actual Iterator<TileFixture> iterator() =>
                    CeylonIterator(fixtures.iterator());
        }
        for (fixture in temp) {
            if (is IEvent fixture) {
                return fixture.text;
            }
        }
        return "Nothing interesting here ...";
    }
    shared actual JSet<JString> allEvents() =>
            JavaSet(set { *data.map(javaString) });
}
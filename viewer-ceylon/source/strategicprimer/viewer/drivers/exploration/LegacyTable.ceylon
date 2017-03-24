import ceylon.collection {
    MutableList,
    ArrayList
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
    TownStatus
}
import strategicprimer.viewer.model.map.fixtures.towns {
    City,
    Fortification,
    Town,
    TownSize
}
"""An [[EncounterTable]] for legacy "events"."""
class LegacyTable() satisfies EncounterTable {
    {IEvent*} createEvents() {
        Player player = PlayerImpl(-1, "Independent");
        MutableList<IEvent> retval = ArrayList<IEvent>();
        retval.add(Battlefield(0, -1));
        retval.add(Cave(0, -1));
        for (status in TownStatus.values()) {
            for (size in `TownSize`.caseValues) {
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
            {TileFixture*} fixtures, MapDimensions mapDimensions) {
        for (fixture in fixtures) {
            if (is IEvent fixture) {
                return fixture.text;
            }
        }
        return "Nothing interesting here ...";
    }
    shared actual Set<String> allEvents => set { *data };
}
import ceylon.collection {
    MutableList,
    ArrayList
}
import strategicprimer.viewer.model.map {
    TileType
}
import strategicprimer.viewer.model.map.fixtures {
    IEvent
}
import model.map {
    TileFixture,
    IFixture,
    Player,
    MapDimensions,
    Point,
    PlayerImpl
}
import strategicprimer.viewer.model.map.fixtures.explorable {
    Battlefield,
    Cave
}
import strategicprimer.viewer.model.map.fixtures.resources {
    MineralVein,
    MineralKind,
    StoneDeposit,
    StoneKind
}
import strategicprimer.viewer.model.map.fixtures.towns {
    City,
    Fortification,
    Town,
    TownSize,
    TownStatus
}
"""An [[EncounterTable]] for legacy "events"."""
class LegacyTable() satisfies EncounterTable {
    {IEvent*} createEvents() {
        Player player = PlayerImpl(-1, "Independent");
        MutableList<IEvent> retval = ArrayList<IEvent>();
        retval.add(Battlefield(0, -1));
        retval.add(Cave(0, -1));
        for (status in `TownStatus`.caseValues) {
            for (size in `TownSize`.caseValues) {
                retval.add(City(status, size, 0, "", 0, player));
                retval.add(Fortification(status, size, 0, "", 0, player));
                retval.add(Town(status, size, 0, "", 0, player));
            }
        }
        for (mineral in `MineralKind`.caseValues) {
            retval.add(MineralVein(mineral.string, true, 0, 0));
            retval.add(MineralVein(mineral.string, false, 0, 0));
        }
        suppressWarnings("expressionTypeNothing")
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
        for (stone in `StoneKind`.caseValues) {
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
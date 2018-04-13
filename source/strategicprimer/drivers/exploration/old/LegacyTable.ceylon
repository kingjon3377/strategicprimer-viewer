import ceylon.collection {
    MutableList,
    ArrayList
}

import strategicprimer.model.map {
    Point,
    MapDimensions,
    Player,
    IFixture,
    TileFixture,
    TileType,
    PlayerImpl
}
import strategicprimer.model.map.fixtures {
    IEvent
}
import strategicprimer.model.map.fixtures.explorable {
    Battlefield,
    Cave
}
import strategicprimer.model.map.fixtures.resources {
    MineralVein,
    StoneDeposit,
    StoneKind
}
import strategicprimer.model.map.fixtures.towns {
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
        assert (exists mineralKindFile = `module strategicprimer.drivers.exploration.old`
            .resourceByPath("mineral_kinds.txt"));
        String mineralKindsList = mineralKindFile.textContent();
        for (mineral in mineralKindsList.lines.map(String.trimmed)) {
            retval.add(MineralVein(mineral.string, true, 0, 0));
            retval.add(MineralVein(mineral.string, false, 0, 0));
        }
        suppressWarnings("expressionTypeNothing")
        object temp satisfies IEvent { // TODO: Move to top level of class
            shared actual TileFixture copy(Boolean zero) => nothing;
            shared actual Integer dc => nothing;
            shared actual Boolean equalsIgnoringID(IFixture fix) => nothing;
            shared actual Integer id => nothing;
            shared actual String plural => nothing;
            shared actual String shortDescription => nothing;
            shared actual String text => "Nothing intersting here ..."; // FIXME: Typo in second word
        }
        retval.add(temp);
        for (stone in `StoneKind`.caseValues) {
            retval.add(StoneDeposit(stone, 0, 0));
        }
        return retval;
    }
    {String*} data = createEvents().map(IEvent.text);
    shared actual String generateEvent(Point point, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions mapDimensions) {
        for (fixture in fixtures) { // TODO: Replace with fixtures.narrow<IEvent>().map(IEvent.text).first else ...
            if (is IEvent fixture) {
                return fixture.text;
            }
        }
        return "Nothing interesting here ...";
    }
    shared actual Set<String> allEvents => set(data);
}

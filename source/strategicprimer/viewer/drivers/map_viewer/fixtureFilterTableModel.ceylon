import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassModel
}

import java.lang {
    JString=String,
    JBoolean=Boolean,
    JClass=Class,
    Types,
    ArrayIndexOutOfBoundsException
}

import javax.swing.table {
    AbstractTableModel
}

import lovelace.util.common {
    Reorderable,
    Comparator,
    matchingPredicate,
    matchingValue
}

import strategicprimer.model.map {
    TileFixture,
    Player
}
import strategicprimer.model.map.fixtures {
    TextFixture,
    Ground
}
import strategicprimer.model.map.fixtures.explorable {
    Cave,
    Battlefield,
    Portal,
    AdventureFixture
}

import strategicprimer.model.map.fixtures.mobile {
    Animal,
    Dragon,
    Centaur,
    Fairy,
    Giant,
    IUnit,
    Troll,
    Simurgh,
    Ogre,
    Minotaur,
    Griffin,
    Sphinx,
    Phoenix,
    Djinn,
    AnimalTracks
}
import strategicprimer.model.map.fixtures.resources {
    Grove,
    Meadow,
    MineralVein,
    CacheFixture,
    Mine,
    StoneDeposit,
    Shrub
}
import strategicprimer.model.map.fixtures.terrain {
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    Village,
    AbstractTown,
    Fortress,
    TownStatus
}
import strategicprimer.drivers.common {
    FixtureMatcher
}
"A class to allow the Z-order of fixtures to be represented as a table."
shared class FixtureFilterTableModel extends AbstractTableModel
        satisfies Reorderable&ZOrderFilter&{FixtureMatcher*}&Comparator<TileFixture> {
    MutableList<FixtureMatcher> matchers;
    shared new () extends AbstractTableModel() {
        matchers = ArrayList<FixtureMatcher>();
        // Can't use our preferred initialization form because an Iterable can only be spread
        // as the *last* argument.
        for (arg in [
                FixtureMatcher.complements<IUnit>(matchingPredicate(not(Player.independent),
                    IUnit.owner), "Units", "Independent Units"),
                FixtureMatcher.trivialMatcher(`Fortress`, "Fortresses"),
                FixtureMatcher.complements<AbstractTown>(matchingValue(TownStatus.active,
                        AbstractTown.status),
                    "Active Cities, Towns, & Fortifications", "Ruined, Abandoned, & Burned Communities"),
                // TODO: break up by owner beyond owned/independent
                FixtureMatcher.complements<Village>(matchingPredicate(Player.independent,
                    Village.owner), "Independent Villages", "Villages With Suzerain"),
                FixtureMatcher.trivialMatcher(`Mine`), FixtureMatcher.trivialMatcher(`Troll`),
                FixtureMatcher.trivialMatcher(`Simurgh`), FixtureMatcher.trivialMatcher(`Ogre`),
                FixtureMatcher.trivialMatcher(`Minotaur`), FixtureMatcher.trivialMatcher(`Griffin`),
                FixtureMatcher.trivialMatcher(`Sphinx`, "Sphinxes"),
                FixtureMatcher.trivialMatcher(`Phoenix`, "Phoenixes"),
                FixtureMatcher.trivialMatcher(`Djinn`, "Djinni"),
                FixtureMatcher.trivialMatcher(`Centaur`),
                FixtureMatcher.trivialMatcher(`Fairy`, "Fairies"),
                FixtureMatcher.trivialMatcher(`Giant`), FixtureMatcher.trivialMatcher(`Dragon`),
                FixtureMatcher.trivialMatcher(`Cave`), FixtureMatcher.trivialMatcher(`Battlefield`),
                FixtureMatcher.trivialMatcher(`Animal`),
                FixtureMatcher.trivialMatcher(`AnimalTracks`),
                FixtureMatcher.trivialMatcher(`StoneDeposit`, "Stone Deposits"),
                FixtureMatcher.trivialMatcher(`MineralVein`, "Mineral Veins"),
                FixtureMatcher.complements<Grove>(Grove.orchard, "Orchards", "Groves"),
                FixtureMatcher.trivialMatcher(`TextFixture`, "Arbitrary-Text Notes"),
                FixtureMatcher.trivialMatcher(`Portal`),
                FixtureMatcher.trivialMatcher(`AdventureFixture`, "Adventures"),
                FixtureMatcher.trivialMatcher(`CacheFixture`, "Caches"),
                FixtureMatcher.trivialMatcher(`Oasis`, "Oases"),
                FixtureMatcher.trivialMatcher(`Forest`),
                FixtureMatcher.complements<Meadow>(Meadow.field, "Fields", "Meadows"),
                FixtureMatcher.trivialMatcher(`Shrub`), FixtureMatcher.trivialMatcher(`Hill`),
                FixtureMatcher.complements<Ground>(Ground.exposed, "Ground (exposed)", "Ground")
            ]) {
            if (is {FixtureMatcher*} arg) {
                matchers.addAll(arg);
            } else {
                matchers.add(arg);
            }
        }
    }
    shared actual Integer rowCount => matchers.size;
    shared actual Integer columnCount => 2;
    shared actual Object getValueAt(Integer rowIndex, Integer columnIndex) {
        if (exists matcher = matchers[rowIndex]) {
            switch (columnIndex)
            case (0) { return JBoolean.valueOf(matcher.displayed); }
            case (1) { return matcher.description; }
            else { throw ArrayIndexOutOfBoundsException(columnIndex); }
        } else {
            throw ArrayIndexOutOfBoundsException(rowIndex);
        }
    }
    shared actual String getColumnName(Integer column) {
        switch (column)
        case (0) { return "Visible"; }
        case (1) { return "Category"; }
        else { return super.getColumnName(column); }
    }
    shared actual JClass<out Object> getColumnClass(Integer columnIndex) {
        switch (columnIndex)
        case (0) { return Types.classForType<JBoolean>(); }
        case (1) { return Types.classForType<JString>(); }
        else { return Types.classForType<Object>(); }
    }
    shared actual Boolean isCellEditable(Integer rowIndex, Integer columnIndex) =>
            columnIndex == 0;
    shared actual void setValueAt(Object val, Integer rowIndex, Integer columnIndex) {
        if (columnIndex == 0, exists matcher = matchers[rowIndex]) {
            if (is Boolean val) {
                matcher.displayed = val;
                fireTableCellUpdated(rowIndex, 0);
            } else if (is JBoolean val) {
                matcher.displayed = val.booleanValue();
                fireTableCellUpdated(rowIndex, 0);
            }
        }
    }
    shared actual void reorder(Integer fromIndex, Integer toIndex) {
        if (fromIndex != toIndex) {
            matchers.move(fromIndex, toIndex);
            fireTableRowsDeleted(fromIndex, fromIndex);
            fireTableRowsInserted(toIndex, toIndex);
        }
    }
    shared actual Boolean shouldDisplay(TileFixture fixture) {
        for (matcher in matchers) {
            if (matcher.matches(fixture)) {
                return matcher.displayed;
            }
        }
        ClassModel<TileFixture> cls = type(fixture);
        matchers.add(FixtureMatcher.trivialMatcher(cls, fixture.plural));
        Integer size = matchers.size;
        fireTableRowsInserted(size - 1, size - 1);
        return true;
    }
    shared actual Iterator<FixtureMatcher> iterator() => matchers.iterator();
    shared actual Comparison compare(TileFixture first, TileFixture second) {
        for (matcher in matchers) {
            if (!matcher.displayed) {
                continue;
            }
            if (matcher.matches(first)) {
                if (matcher.matches(second)) {
                    return equal;
                } else {
                    return smaller;
                }
            } else if (matcher.matches(second)) {
                return larger;
            }
        }
        return equal;
    }
}
